package com.nano.gcruncher;

import java.math.BigDecimal;
import java.sql.Timestamp;

import org.apache.commons.lang.time.StopWatch;
import org.jboss.logging.Logger;

import com.nano.jpa.entity.Borrow;
import com.nano.jpa.entity.Subscriber;
import com.nano.jpa.enums.BorrowStatus;
import com.nano.jpa.enums.PaymentStatus;
import com.seamfix.nano.cache.InfinispanObjectBucket;
import com.seamfix.nano.tools.QueryManager;

public class BorDataThread implements Runnable {
	
private Logger log = Logger.getLogger(getClass());
	
	private QueryManager queryManager ;
	private InfinispanObjectBucket infinispanBucketCache ;
	
	private BigDecimal accountleft ;
	private BigDecimal bfaccountleft ;
	private BigDecimal bfborrowvalues ;
	private BigDecimal borrowvalues ;
	private long brandid ;
	private long homeareanumber ;
	private long subcosid ;
	
	private String msisdn ;
	
	private long timestamp ;
	
	public BorDataThread(QueryManager queryManager, 
			InfinispanObjectBucket infinispanBucketCache, 
			BigDecimal accountleft, BigDecimal bfaccountleft, BigDecimal bfborrowvalues, BigDecimal borrowvalues, long brandid, long homeareanumber, long subcosid, 
			String msisdn, String serialid, String vendorid, 
			long timestamp) {
		// TODO Auto-generated constructor stub
		
		this.accountleft = accountleft;
		this.bfaccountleft = bfaccountleft;
		this.bfborrowvalues = bfborrowvalues;
		this.borrowvalues = borrowvalues;
		this.brandid = brandid;
		this.homeareanumber = homeareanumber;
		this.infinispanBucketCache = infinispanBucketCache;
		this.msisdn = msisdn;
		this.queryManager = queryManager;
		this.subcosid = subcosid;
		this.timestamp = timestamp;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		
		Subscriber subscriber = queryManager.createSubscriber(msisdn);

		if (isBorrowCreated(subscriber, borrowvalues, bfborrowvalues, timestamp))
			return ;

		Borrow borrow = infinispanBucketCache.getOutstandingTransactionFromCache(subscriber.getMsisdn());

		if (borrow != null && borrow.getReceivedTimestamp() == null)
			startBorrowDataLoading(borrow, subscriber, borrowvalues, bfborrowvalues, bfaccountleft, accountleft, brandid, homeareanumber, subcosid, timestamp);
		
		stopWatch.stop();
		log.info("Time taken to comlete BOR data crunching:" + stopWatch.getTime() + "ms");
	}
	
	/**
	 * Start borrow data loading.
	 * 
	 * @param borrow - loan transaction details
	 * @param subscriber - subscriber detail
	 * @param amountOwedAfterBorrowed - subscriber pending loan after transaction
	 * @param amountOwedBeforeBorrow - subscriber pending loan before transaction
	 * @param balanceBeforeBorrow - subscriber account balance before transaction
	 * @param balanceAfterBorrow - subscriber account balance after transaction
	 * @param brandid - brand identification of transaction
	 * @param homeareanumber - home area number within transaction
	 * @param subcosid - transaction sub-cos identification
	 * @param timestamp - transaction time-stamp
	 */
	private void startBorrowDataLoading(Borrow borrow, 
			Subscriber subscriber, 
			BigDecimal amountOwedAfterBorrowed, BigDecimal amountOwedBeforeBorrow, BigDecimal balanceBeforeBorrow, BigDecimal balanceAfterBorrow, 
			Long brandid, Long homeareanumber, Long subcosid, 
			long timestamp) {
		// TODO Auto-generated method stub

		log.debug("startBorrowDataLoading:" + subscriber.getMsisdn() + " pk:" + borrow.getPk());
		String msisdn = subscriber.getMsisdn();

		borrow.setAmountOwedAfterBorrowed(amountOwedAfterBorrowed);
		borrow.setAmountOwedBeforeBorrow(amountOwedBeforeBorrow);
		borrow.setBalanceAfterBorrow(balanceAfterBorrow);
		borrow.setBalanceBeforeBorrow(balanceBeforeBorrow);
		borrow.setBrandId(Integer.parseInt(brandid.toString()));
		borrow.setHomeAreaNumber(Integer.parseInt(homeareanumber.toString()));
		borrow.setReceivedTimestamp(new Timestamp(timestamp));
		borrow.setSubCosId(Integer.parseInt(subcosid.toString()));
		borrow.setStatus(BorrowStatus.RECEIVED);

		queryManager.update(borrow);
		log.debug("finished BorrowDataLoading:" + subscriber.getMsisdn());

		if (infinispanBucketCache.isBorrowSorted(msisdn, PaymentStatus.COMPLETE))
			infinispanBucketCache.removeTransactionFromCache(msisdn, borrow);
		else
			infinispanBucketCache.updateTransaction(msisdn, borrow);
	}
	
	/**
	 * Checks if is borrow created using optimized <code>MapMessage</code> JMS object
	 * 
	 * @param subscriber - subscriber detail
	 * @param borrowvalues - current loan amount obtained by subscriber
	 * @param bfborrowvalues - loan amount recorded for subscriber before transaction
	 * @param timestamp - time-stamp subscriber received loan value
	 * @return true if exists
	 */
	public boolean isBorrowCreated(Subscriber subscriber, 
			BigDecimal borrowvalues, BigDecimal bfborrowvalues, 
			long timestamp){

		BigDecimal principal = borrowvalues.subtract(bfborrowvalues);

		if (queryManager.getBorrowBySubscriberAndPrincipalAndTimeStamp(subscriber, principal, new Timestamp(timestamp)) != null)
			return true;

		return false;
	}

}