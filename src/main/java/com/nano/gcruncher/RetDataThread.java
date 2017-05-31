package com.nano.gcruncher;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import org.apache.commons.lang.time.StopWatch;
import org.jboss.logging.Logger;

import com.nano.jpa.entity.Borrow;
import com.nano.jpa.entity.Subscriber;
import com.nano.jpa.entity.ras.BorrowableAmount;
import com.nano.jpa.enums.EventType;
import com.nano.jpa.enums.Merchant;
import com.nano.jpa.enums.PaymentStatus;
import com.nano.jpa.enums.ReturnMode;
import com.seamfix.nano.cache.InfinispanObjectBucket;
import com.seamfix.nano.enums.SercomStandardResp;
import com.seamfix.nano.enums.SmppResponse;
import com.seamfix.nano.tools.MessageModel;
import com.seamfix.nano.tools.QueryManager;

public class RetDataThread implements Callable<Map<String, Object>> {

private Logger log = Logger.getLogger(getClass());
	
	private QueryManager queryManager ;
	private InfinispanObjectBucket infinispanBucketCache ;
	
	private BigDecimal accountleft ; 
	private BigDecimal afaccountleft ;
	private BigDecimal borrowvaluesaf ;
	private BigDecimal borrowvaluesbf ;
	private long brandid ;
	private BigDecimal etupenaltyaf ;
	private BigDecimal etupenaltybf ;
	private BigDecimal returnamount ;
	private int returnmode ;
	private long subcosid ; 
	
	private long timestamp ;
	
	private String msisdn ;
	private String serialno ;
	private String triggermsisdn ;
	
	/**
	 * Instantiate new instance of CALLABLE thread.
	 * 
	 * @param queryManager - {@link QueryManager} EJB
	 * @param infinispanBucketCache - {@link InfinispanObjectBucket} EJB
	 * @param accountleft - subscriber account balance before transaction
	 * @param afaccountleft - subscriber account balance after transaction
	 * @param borrowvaluesaf - subscriber pending balance after transaction
	 * @param borrowvaluesbf - subscriber pending balance before transaction
	 * @param brandid - transaction brand identification
	 * @param etupenaltyaf - calculated penalty after transaction
	 * @param etupenaltybf - calculated penalty before transaction
	 * @param returnamount - reimbursed amount
	 * @param returnmode - transaction medium
	 * @param subcosid - transaction sub-cos identification
	 * @param timestamp - transaction time-stamp
	 * @param msisdn - subscriber MSISDN
	 * @param serialno - transaction serial number
	 * @param triggermsisdn - MSISDN responsible for trigerring transaction
	 */
	public RetDataThread(QueryManager queryManager, 
			InfinispanObjectBucket infinispanBucketCache, 
			BigDecimal accountleft, BigDecimal afaccountleft, BigDecimal borrowvaluesaf, BigDecimal borrowvaluesbf, long brandid, BigDecimal etupenaltyaf, BigDecimal etupenaltybf, 
			BigDecimal returnamount, int returnmode, long subcosid, 
			long timestamp, 
			String msisdn, String serialno, String triggermsisdn) {
		// TODO Auto-generated constructor stub
		
		this.accountleft = accountleft;
		this.afaccountleft = afaccountleft;
		this.borrowvaluesaf = borrowvaluesaf;
		this.borrowvaluesbf = borrowvaluesbf;
		this.brandid = brandid;
		this.etupenaltyaf = etupenaltyaf;
		this.etupenaltybf = etupenaltybf;
		this.infinispanBucketCache = infinispanBucketCache;
		this.msisdn = msisdn;
		this.queryManager = queryManager;
		this.returnamount = returnamount;
		this.returnmode = returnmode;
		this.serialno = serialno;
		this.subcosid = subcosid;
		this.timestamp = timestamp;
		this.triggermsisdn = triggermsisdn;
	}
	
	@Override
	public Map<String, Object> call() throws Exception {
		// TODO Auto-generated method stub
		
		Subscriber subscriber = queryManager.createSubscriber(msisdn);
		Borrow borrow = infinispanBucketCache.getOutstandingTransactionFromCache(subscriber.getMsisdn());

		if (borrow == null)
			return null;
		
		if (queryManager.getPaymentByBorrowAndTimestamp(borrow, new Timestamp(timestamp)) != null)
			return null;
		
		if (borrow.getProcessedTimestamp().after(new Timestamp(timestamp)))
			return null;

		return startRechargeDataLoading(borrow, subscriber, returnamount, borrowvaluesaf);
	}
	
	/**
	 * Start RECHARGE data.
	 * 
	 * @param borrow - loan transaction detail
	 * @param subscriber - subscriber detail
	 * @param amountPaid - amount reimbursed
	 * @param borrowvaluesaf - subscriber pending balance after transaction
	 */
	private Map<String, Object> startRechargeDataLoading(Borrow borrow, Subscriber subscriber, 
			BigDecimal amountPaid, BigDecimal borrowvaluesaf) {

		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		
		BorrowableAmount borrowableAmount = queryManager.getBorrowableAmountByAmount((borrow.getAmountApproved().multiply(BigDecimal.valueOf(100))).intValue());
		
		String msisdn = subscriber.getMsisdn();
		String paymentRef = "", amountdebited = "", outstandingDebt = "";
		
		ReturnMode returnMode = returnmode == 1 ? ReturnMode.RECHARGE : ReturnMode.TRANSFER;

		double servicePercentage = 15D;

		if (borrowableAmount != null)
			servicePercentage = borrowableAmount.getServiceFee().doubleValue();

		BigDecimal loan = borrow.getCurrentPendingBalance();

		Map<String, Object> model = new HashMap<String, Object>();
		MessageModel messageModel = new MessageModel();
		
		Map<String, Object> response = new HashMap<>();

		if (amountPaid.compareTo(loan) >= 0){
			model.put("amount", amountPaid);
			model.put("template", SmppResponse.FULLY_COVERED.getResponse());
			
			response.put("eventType", EventType.RECOVERY);

			queryManager.updateBorrowDataWithPaymentRecord(borrow, PaymentStatus.COMPLETE, amountPaid, servicePercentage, returnMode, 
					borrowvaluesbf, borrowvaluesaf, afaccountleft, etupenaltyaf, etupenaltybf, brandid, subcosid, accountleft, 
					timestamp, serialno, msisdn);

			queryManager.clearSubscriberDebt(borrow.getSubscriber());
			infinispanBucketCache.removeTransactionFromCache(msisdn, borrow);
			log.info("removing borrow transaction from cache for msisdn:" + msisdn);
		}else{
			model.put("amount", amountPaid);
			model.put("outstanding", borrowvaluesaf);
			model.put("template", SmppResponse.PARTLY_COVERED.getResponse());
			
			response.put("eventType", EventType.PARTIAL);

			queryManager.updateBorrowDataWithPaymentRecord(borrow, PaymentStatus.COMPLETE, amountPaid, servicePercentage, returnMode, 
					borrowvaluesbf, borrowvaluesaf, afaccountleft, etupenaltyaf, etupenaltybf, brandid, subcosid, accountleft, 
					timestamp, serialno, msisdn);
			infinispanBucketCache.updateTransaction(msisdn, borrow);
		}

		long notificationpk = 0;

		Long pk = queryManager.getPaymentPKByBorrowAndTimestamp(borrow, new Timestamp(timestamp));
		if (pk.compareTo(0L) == 0){
			log.error("Payment object not created successfully");
			return null;
		}

		log.info("finished RechargeDataLoading:" + subscriber.getMsisdn());
		messageModel.setModel(model);

		try {
			notificationpk = pk;

			paymentRef = new StringBuilder(triggermsisdn).append("^").append(new Timestamp(timestamp).getTime()).toString();
			amountdebited = String.valueOf((amountPaid.multiply(BigDecimal.valueOf(100D))).toBigInteger());
			outstandingDebt = String.valueOf((borrow.getAmountApproved().subtract(borrow.getCurrentPendingBalance())).multiply(BigDecimal.valueOf(100D)).toBigInteger());

			response.put("subscriber", subscriber);
			response.put("messageModel", messageModel);
			response.put("paymentRef", paymentRef);
			response.put("notificationpk", notificationpk);
			response.put("borrow", borrow);
			response.put("sercomResponse", SercomStandardResp.SUCCESS);
			response.put("amountdebited", amountdebited);
			response.put("outstandingDebt", outstandingDebt);
			response.put("notificationpk", notificationpk);
			response.put("referenceNo", borrow.getReferenceNo());
			response.put("returnMode", returnMode);
			
			stopWatch.stop();
			log.info("Time taken to comlete RET data crunching:" + stopWatch.getTime() + "ms");
			
			return response;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.error("", e);
		}
		
		stopWatch.stop();
		log.info("Time taken to comlete RET data crunching:" + stopWatch.getTime() + "ms");
		
		return null;
	}
	
	/**
	 * Checks if payment is created using <code>MapMessage</code> for optimization
	 * 
	 * @param subscriber - subscriber detail
	 * @param merchant - Merchant for which transaction belongs to
	 * @param returnamount - reimbursed amount
	 * @param timestamp - transaction time-stamp
	 * @return true if exists
	 */
	public boolean isPaymentCreated(Subscriber subscriber, 
			Merchant merchant, 
			BigDecimal returnamount, 
			long timestamp){

		if (queryManager.getPaymentBySubscriberAndAmountAndTimestamp(subscriber, returnamount, new Timestamp(timestamp), merchant) != null)
			return true;

		return false;
	}

}