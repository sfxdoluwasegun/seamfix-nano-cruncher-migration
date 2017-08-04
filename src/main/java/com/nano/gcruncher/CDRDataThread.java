package com.nano.gcruncher;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import org.apache.commons.lang.time.StopWatch;
import org.jboss.logging.Logger;

import com.nano.jpa.entity.Borrow;
import com.nano.jpa.entity.Payment;
import com.nano.jpa.entity.Subscriber;
import com.nano.jpa.enums.Merchant;
import com.nano.jpa.enums.OperationType;
import com.nano.jpa.enums.PaymentStatus;
import com.nano.jpa.enums.ReturnMode;
import com.seamfix.nano.tools.ApplicationBean;
import com.seamfix.nano.tools.DbManager;
import com.seamfix.nano.tools.QueryManager;

public class CDRDataThread implements Runnable {
	
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

	public void run() {
		// TODO Auto-generated method stub
		
		Subscriber subscriber = dbManager.createSubscriber(msisdn);
		
		if (operationType.equals(OperationType.LOAN))
			loadBorrowData();
		else
			loadPaymentDataWithReconcilliation();
		
		if (appBean.getVendorid().equalsIgnoreCase(loanVendorId))
			persistNanoDeal(subscriber);
		else
			persistNONNanoDeal();
	}

	/**
	 * Write CDR log for Non-NANO transaction to persistence.
	 * 
	 * @return null
	 */
	private void persistNONNanoDeal() {
		// TODO Auto-generated method stub
		
		if (dbManager.getOtherDealingByMSISDNAndOperationTimeAndOperationType(msisdn, timestamp, operationType) != null)
			return ;
		
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		
		dbManager.persistOtherDealing(balanceType, changeBalance, currentBalance, entryDate, etuAmount, 
				etuGraceDate, forceRepayDate, initialEtuAmount, initialLoanAmount, initialLoanPoundage, loanAmount, loanBalanceType, 
				loanPoundage, loanVendorId, msisdn, offering, operationType, repayment, repayPoundage, 
				subid, timestamp, transid);
		
		stopWatch.stop();
		log.info("Time taken to comlete CDR data crunching:" + stopWatch.getTime() + "ms");
	}

	/**
	 * Write CDR log for NANO transaction to persistence.
	 * 
	 * @param subscriber
	 * @return null
	 */
	private void persistNanoDeal(Subscriber subscriber) {
		// TODO Auto-generated method stub
		
		if (dbManager.getDealingByMSISDNAndOperationTimeAndOperationType(msisdn, timestamp, operationType) != null)
			return;
		
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		
		dbManager.persistDealing(balanceType, changeBalance, currentBalance, entryDate, etuAmount, 
				etuGraceDate, forceRepayDate, initialEtuAmount, initialLoanAmount, initialLoanPoundage, loanAmount, loanBalanceType, 
				loanPoundage, loanVendorId, msisdn, offering, operationType, repayment, repayPoundage, 
				subid, timestamp, transid, referenceNumber);
		
		stopWatch.stop();
		log.info("Time taken to comlete CDR data crunching:" + stopWatch.getTime() + "ms");
	}
	
	/**
	 * Handle borrow data loading.
	 */
	private void loadBorrowData() {
		// TODO Auto-generated method stub
		
		if (dbManager.getBorrowBySubscriberAndPrincipalAndTimeStamp(msisdn, initialLoanAmount, timestamp, Merchant.fromVendorId(loanVendorId)) != null)
			return;
		
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
		
		if (dbManager.getPaymentBySubscriberAndAmountAndTimestamp(msisdn, changeBalance, timestamp, Merchant.fromVendorId(loanVendorId)) != null)
			return;
		
		Payment payment = new Payment();
		payment.setAmountOwedAfterPayment(loanAmount);
		payment.setAmountOwedBeforePayment(initialLoanAmount);
		payment.setAmountPaid(changeBalance);
		payment.setBalanceAfterPayment(currentBalance);
		payment.setBalanceBeforePayment(currentBalance.add(changeBalance));
		payment.setLoanPenaltyAfterPayment(etuAmount);
		payment.setLoanPenaltyBeforePayment(initialEtuAmount);
		payment.setMsisdn(msisdn);
		payment.setOperationType(operationType);
		payment.setPoundagePaid(repayPoundage);
		payment.setPoundagBeforePayment(initialLoanPoundage);
		payment.setPoundageAfterPayment(loanPoundage);
		payment.setProcessedTimestamp(Timestamp.valueOf(LocalDateTime.now()));
		payment.setRechargeAmount(repayment);
		payment.setRechargeTime(timestamp);
		payment.setReturnMode(returnMode);
		payment.setSubCosId(subid);
		payment.setMerchant(Merchant.fromVendorId(loanVendorId));

		dbManager.create(payment);
	}

}