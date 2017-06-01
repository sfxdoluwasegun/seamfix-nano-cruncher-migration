package com.nano.gcruncher;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import org.apache.commons.lang.time.StopWatch;
import org.jboss.logging.Logger;

import com.nano.jpa.entity.Dealing;
import com.nano.jpa.entity.Subscriber;
import com.nano.jpa.entity.ras.SubscriberAssessment;
import com.nano.jpa.enums.EventType;
import com.nano.jpa.enums.OperationType;
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
	
	private Timestamp timestamp ;
	private Timestamp etuGraceDate ;
	private Timestamp forceRepayDate ;
	private Timestamp entryDate ;
	
	private OperationType operationType ;
	private QueryManager queryManager ;
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
		this.queryManager = queryManager;
		this.repayment = repayment;
		this.repayPoundage = repayPoundage;
		this.subid = subid;
		this.timestamp = timestamp;
		this.transid = transid;
	}

	@Override
	public Map<String, Object> call() throws Exception {
		// TODO Auto-generated method stub
		
		Subscriber subscriber = queryManager.createSubscriber(msisdn);
		
		if (dbManager.getDealingByMSISDNAndOperationTimeAndOperationType(msisdn, timestamp, operationType) != null)
			return null;
		
		if (appBean.getVendorid().equalsIgnoreCase(loanVendorId))
			return persistNanoDeal(subscriber);
		else
			return persistNONNanoDeal();
	}

	private Map<String, Object> persistNONNanoDeal() {
		// TODO Auto-generated method stub
		
		if (dbManager.getOtherDealingByMSISDNAndOperationTimeAndOperationType(msisdn, timestamp, operationType) != null)
			return null;
		
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		
		dbManager.persistOtherDealing(balanceType, changeBalance, currentBalance, entryDate, etuAmount, 
				etuGraceDate, forceRepayDate, initialEtuAmount, initialLoanAmount, initialLoanPoundage, loanAmount, loanBalanceType, 
				loanPoundage, loanVendorId, msisdn, offering, operationType, queryManager, repayment, repayPoundage, 
				subid, timestamp, transid);
		
		stopWatch.stop();
		log.info("Time taken to comlete CDR data crunching:" + stopWatch.getTime() + "ms");
		
		return null;
	}

	private Map<String, Object> persistNanoDeal(Subscriber subscriber) {
		// TODO Auto-generated method stub
		
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		
		Dealing dealing = dbManager.persistDealing(balanceType, changeBalance, currentBalance, entryDate, etuAmount, 
				etuGraceDate, forceRepayDate, initialEtuAmount, initialLoanAmount, initialLoanPoundage, loanAmount, loanBalanceType, 
				loanPoundage, loanVendorId, msisdn, offering, operationType, queryManager, repayment, repayPoundage, 
				subid, timestamp, transid);
		
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

	private Map<String, Object> handleRepaymentPostProcessing(Subscriber subscriber, Dealing dealing) {
		// TODO Auto-generated method stub
		
		Map<String, Object> model = new HashMap<String, Object>();
		MessageModel messageModel = new MessageModel();
		
		Map<String, Object> response = new HashMap<>();

		if (loanAmount.compareTo(BigDecimal.ZERO) == 0){
			model.put("amount", repayment);
			model.put("template", SmppResponse.FULLY_COVERED.getResponse());
			
			response.put("eventType", EventType.RECOVERY);

			queryManager.clearSubscriberDebt(subscriber);
			log.info("removing borrow transaction from cache for msisdn:" + msisdn);
		}else{
			model.put("amount", repayment);
			model.put("outstanding", loanAmount);
			model.put("template", SmppResponse.PARTLY_COVERED.getResponse());
			
			response.put("eventType", EventType.PARTIAL);
		}
		
		SubscriberAssessment subscriberAssessment = queryManager.getSubscriberAssessmentBySubscriber(subscriber);
		if (subscriberAssessment != null){
			BigDecimal creditStatus = subscriberAssessment.getCreditStatus();
			subscriberAssessment.setCreditStatus(creditStatus.add(repayment));
			queryManager.update(subscriberAssessment);
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
		response.put("referenceNo", subscriberAssessment.getLoanRef() != null ? subscriberAssessment.getLoanRef() : dealing.getReferenceNo());
		response.put("returnMode", operationType.equals(OperationType.REPAYMENT) ? ReturnMode.RECHARGE : ReturnMode.TRANSFER);
		
		return response;
	}

}