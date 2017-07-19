package com.nano.gcruncher;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import org.apache.commons.lang.time.StopWatch;
import org.jboss.logging.Logger;

import com.nano.jpa.entity.Borrow;
import com.nano.jpa.entity.Dealing;
import com.nano.jpa.entity.Payment;
import com.nano.jpa.entity.Subscriber;
import com.nano.jpa.enums.EventType;
import com.nano.jpa.enums.Merchant;
import com.nano.jpa.enums.OperationType;
import com.nano.jpa.enums.PaymentStatus;
import com.nano.jpa.enums.ReturnMode;
import com.seamfix.nano.enums.SercomStandardResp;
import com.seamfix.nano.enums.SmppResponse;
import com.seamfix.nano.jbeans.ApplicationBean;
import com.seamfix.nano.tools.DbManager;
import com.seamfix.nano.tools.MessageModel;
import com.seamfix.nano.tools.QueryManager;

public class CDRDataThread implements Callable<Map<String, Object>> {
	
	private Logger log = Logger.getLogger(getClass());
	
	private long subid ;
	private long loanBalanceType ;
	private long transid ;
	private long balanceType ;
	
	private String msisdn ;
	private String offering ;
	private String loanVendorId ;
	private String referenceNumber;
	
	private Timestamp timestamp ;
	private Timestamp etuGraceDate ;
	private Timestamp forceRepayDate ;
	private Timestamp entryDate ;
	
	private OperationType operationType ;
	private DbManager dbManager ;
	private ApplicationBean appBean ;
	
	private BigDecimal initialLoanAmount ;
	private BigDecimal initialLoanPoundage ;
	private BigDecimal loanAmount ;
	private BigDecimal loanPoundage ;
	private BigDecimal repayment ;
	private BigDecimal repayPoundage ;
	private BigDecimal initialEtuAmount ;
	private BigDecimal etuAmount ;
	private BigDecimal currentBalance ;
	private BigDecimal changeBalance ;

	private ReturnMode returnMode;

	public CDRDataThread(QueryManager queryManager, DbManager dbManager, ApplicationBean appBean, long balanceType, BigDecimal changeBalance, BigDecimal currentBalance, Timestamp entryDate,
			BigDecimal etuAmount, Timestamp etuGraceDate, Timestamp forceRepayDate, BigDecimal initialEtuAmount,
			BigDecimal initialLoanAmount, BigDecimal initialLoanPoundage, BigDecimal loanAmount,
			long loanBalanceType, BigDecimal loanPoundage, String loanVendorId, String msisdn, String offering,
			OperationType operationType, BigDecimal repayment, BigDecimal repayPoundage, long subid,
			Timestamp timestamp, long transid) {
		// TODO Auto-generated constructor stub
		
		this.appBean = appBean;
		this.balanceType = balanceType;
		this.changeBalance = changeBalance;
		this.currentBalance = currentBalance;
		this.dbManager = dbManager;
		this.entryDate = entryDate;
		this.etuAmount = etuAmount;
		this.etuGraceDate = etuGraceDate;
		this.forceRepayDate = forceRepayDate;
		this.initialEtuAmount = initialEtuAmount;
		this.initialLoanAmount = initialLoanAmount;
		this.initialLoanPoundage = initialLoanPoundage;
		this.loanAmount = loanAmount;
		this.loanBalanceType = loanBalanceType;
		this.loanPoundage = loanPoundage;
		this.loanVendorId = loanVendorId;
		this.msisdn = msisdn;
		this.offering = offering;
		this.operationType = operationType;
		this.referenceNumber = dbManager.retrieveLoanReferenceByMSISDN(msisdn, loanVendorId, timestamp, operationType);
		this.repayment = repayment;
		this.repayPoundage = repayPoundage;
		this.returnMode = operationType.equals(OperationType.REPAYMENT) ? ReturnMode.RECHARGE : ReturnMode.TRANSFER;
		this.subid = subid;
		this.timestamp = timestamp;
		this.transid = transid;
	}

	@Override
	public Map<String, Object> call() throws Exception {
		// TODO Auto-generated method stub
		
		Subscriber subscriber = dbManager.createSubscriber(msisdn);
		
		if (operationType.equals(OperationType.LOAN))
			loadBorrowData();
		else
			loadPaymentDataWithReconcilliation();
		
		if (dbManager.getDealingByMSISDNAndOperationTimeAndOperationType(msisdn, timestamp, operationType) != null)
			return null;
		
		if (appBean.getVendorid().equalsIgnoreCase(loanVendorId))
			return persistNanoDeal(subscriber);
		else
			return persistNONNanoDeal();
	}

	/**
	 * Write CDR log for Non-NANO transaction to persistence.
	 * 
	 * @return null
	 */
	private Map<String, Object> persistNONNanoDeal() {
		// TODO Auto-generated method stub
		
		if (dbManager.getOtherDealingByMSISDNAndOperationTimeAndOperationType(msisdn, timestamp, operationType) != null)
			return null;
		
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		
		dbManager.persistOtherDealing(balanceType, changeBalance, currentBalance, entryDate, etuAmount, 
				etuGraceDate, forceRepayDate, initialEtuAmount, initialLoanAmount, initialLoanPoundage, loanAmount, loanBalanceType, 
				loanPoundage, loanVendorId, msisdn, offering, operationType, repayment, repayPoundage, 
				subid, timestamp, transid);
		
		stopWatch.stop();
		log.info("Time taken to comlete CDR data crunching:" + stopWatch.getTime() + "ms");
		
		return null;
	}

	/**
	 * Write CDR log for NANO transaction to persistence.
	 * 
	 * @param subscriber
	 * @return null
	 */
	private Map<String, Object> persistNanoDeal(Subscriber subscriber) {
		// TODO Auto-generated method stub
		
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		
		Dealing dealing = dbManager.persistDealing(balanceType, changeBalance, currentBalance, entryDate, etuAmount, 
				etuGraceDate, forceRepayDate, initialEtuAmount, initialLoanAmount, initialLoanPoundage, loanAmount, loanBalanceType, 
				loanPoundage, loanVendorId, msisdn, offering, operationType, repayment, repayPoundage, 
				subid, timestamp, transid, referenceNumber);
		
		if (operationType.equals(OperationType.REPAYMENT) 
				|| operationType.equals(OperationType.TRANSFER) 
				|| operationType.equals(OperationType.FORCIBLE)){
			stopWatch.stop();
			log.info("Time taken to comlete CDR data crunching:" + stopWatch.getTime() + "ms");
			return handleRepaymentPostProcessing(subscriber, dealing);
		}
		
		stopWatch.stop();
		log.info("Time taken to comlete CDR data crunching:" + stopWatch.getTime() + "ms");
		
		return null;
	}

	/**
	 * Handle loan repayment.
	 * 
	 * @param subscriber
	 * @param dealing
	 * @return Map response containing details for Sercom/SMPP notifications
	 */
	private Map<String, Object> handleRepaymentPostProcessing(Subscriber subscriber, Dealing dealing) {
		// TODO Auto-generated method stub
		
		Map<String, Object> model = new HashMap<String, Object>();
		MessageModel messageModel = new MessageModel();
		
		Map<String, Object> response = new HashMap<>();

		if (loanAmount.compareTo(BigDecimal.ZERO) == 0){
			model.put("amount", repayment.add(repayPoundage));
			model.put("template", SmppResponse.FULLY_COVERED.getResponse());
			
			response.put("eventType", EventType.RECOVERY);

			dbManager.clearSubscriberDebt(subscriber);
			log.info("removing borrow transaction from cache for msisdn:" + msisdn);
		}else{
			model.put("amount", repayment.add(repayPoundage));
			model.put("outstanding", loanAmount);
			model.put("template", SmppResponse.PARTLY_COVERED.getResponse());
			
			response.put("eventType", EventType.PARTIAL);
		}
		
		messageModel.setModel(model);

		String paymentRef = new StringBuilder(msisdn).append("^").append(timestamp.getTime()).toString();
		String amountdebited = String.valueOf((repayment.multiply(BigDecimal.valueOf(100D))).toBigInteger());
		String outstandingDebt = String.valueOf((loanAmount.multiply(BigDecimal.valueOf(100D))).toBigInteger());

		response.put("subscriber", subscriber);
		response.put("messageModel", messageModel);
		response.put("paymentRef", paymentRef);
		response.put("notificationpk", dealing.getPk());
		response.put("sercomResponse", SercomStandardResp.SUCCESS);
		response.put("amountdebited", amountdebited);
		response.put("outstandingDebt", outstandingDebt);
		response.put("referenceNo", referenceNumber);
		response.put("returnMode", returnMode);
		
		return response;
	}
	
	/**
	 * Handle borrow data loading.
	 */
	private void loadBorrowData() {
		// TODO Auto-generated method stub
		
		BigDecimal ammountapproved = initialLoanAmount.add(initialLoanPoundage);

		Borrow borrow = new Borrow();
		borrow.setAmountApproved(ammountapproved);
		borrow.setAmountOwedAfterBorrowed(ammountapproved);
		borrow.setAmountOwedBeforeBorrow(BigDecimal.ZERO);
		borrow.setAmountRequested(ammountapproved);
		borrow.setBalanceAfterBorrow(currentBalance.add(changeBalance));
		borrow.setBalanceBeforeBorrow(currentBalance);
		borrow.setCharge(initialLoanPoundage);
		borrow.setCurrentPendingBalance(ammountapproved);
		borrow.setMsisdn(msisdn);
		borrow.setPaymentStatus(PaymentStatus.NONE);
		borrow.setPrincipal(initialLoanAmount);
		borrow.setProcessedTimestamp(Timestamp.valueOf(LocalDateTime.now()));
		borrow.setReceivedTimestamp(timestamp);
		borrow.setRecoveredCharge(BigDecimal.ZERO);
		borrow.setReferenceNo(referenceNumber);
		borrow.setSubCosId(subid);
		borrow.setMerchant(Merchant.fromVendorId(loanVendorId));

		dbManager.create(borrow);
	}
	
	/**
	 * Handle payment data loading.
	 */
	private void loadPaymentDataWithReconcilliation() {
		// TODO Auto-generated method stub
		
		Payment payment = new Payment();
		payment.setAmountOwedAfterPayment(loanAmount);
		payment.setAmountOwedBeforePayment(initialLoanAmount);
		payment.setAmountPaid(changeBalance);
		payment.setBalanceAfterPayment(currentBalance);
		payment.setBalanceBeforePayment(currentBalance.add(changeBalance));
		payment.setLoanPenaltyAfterPayment(etuAmount);
		payment.setLoanPenaltyBeforePayment(initialEtuAmount);
		payment.setMsisdn(msisdn);
		payment.setProcessedTimestamp(Timestamp.valueOf(LocalDateTime.now()));
		payment.setRechargeAmount(repayment);
		payment.setRechargeTime(timestamp);
		payment.setReferenceNo(referenceNumber);
		payment.setReturnMode(returnMode);
		payment.setSubCosId(subid);
		payment.setMerchant(Merchant.fromVendorId(loanVendorId));

		dbManager.create(payment);
		doBorrowReconcilliation();
	}

	/**
	 * Reconcile {@link Borrow} log.
	 */
	private void doBorrowReconcilliation() {
		// TODO Auto-generated method stub
		
		Borrow borrow = dbManager.getBorrowByReferenceNo(referenceNumber);
		
		if (borrow == null)
			return;
		
		PaymentStatus paymentStatus = loanAmount.compareTo(BigDecimal.ZERO) < 1 ? PaymentStatus.COMPLETE : PaymentStatus.PARTIAL ;
		
		borrow.setCurrentPendingBalance(loanAmount);
		borrow.setPaymentStatus(paymentStatus);
		borrow.setRecoveredCharge(initialLoanAmount.subtract(loanAmount));
		
		dbManager.update(borrow);
	}

}