package com.nano.gcruncher;

import java.math.BigDecimal;
import java.sql.Timestamp;
import org.apache.commons.lang.time.StopWatch;
import org.jboss.logging.Logger;

import com.seamfix.nano.cache.InfinispanObjectBucket;
import com.seamfix.nano.tools.QueryManager;

public class IBorDataThread implements Runnable {
	
	private Logger log = Logger.getLogger(getClass());

	private QueryManager queryManager ;
	
	private BigDecimal accountleft ;
	private BigDecimal bfaccountleft ;
	private BigDecimal bfborrowvalues ;
	private BigDecimal borrowvalues ;
	private BigDecimal charge ;
	
	private Long brandid ;
	private Long homeareanumber ;
	private Long subcosid ;
	
	private String msisdn ;
	private String vendorid;
	private String referenceNo;
	private String serialno;
	
	private long timestamp ;
	
	public IBorDataThread(QueryManager queryManager, 
			InfinispanObjectBucket infinispanBucketCache, 
			BigDecimal accountleft, BigDecimal bfaccountleft, BigDecimal bfborrowvalues, BigDecimal borrowvalues, BigDecimal charge, long brandid, long homeareanumber, long subcosid, 
			String msisdn, String serialid, String vendorid, String referenceno, 
			long timestamp) {
		// TODO Auto-generated constructor stub
		
		this.accountleft = accountleft;
		this.bfaccountleft = bfaccountleft;
		this.bfborrowvalues = bfborrowvalues;
		this.borrowvalues = borrowvalues;
		this.brandid = brandid;
		this.homeareanumber = homeareanumber;
		this.msisdn = msisdn;
		this.queryManager = queryManager;
		this.referenceNo = referenceno;
		this.serialno = serialid;
		this.subcosid = subcosid;
		this.timestamp = timestamp;
		this.vendorid = vendorid;
		this.charge = charge;
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		
		//queryManager.createSubscriber(msisdn);

		if (isBorrowCreated(msisdn, bfborrowvalues, timestamp))
			return ;
		
		startBorrowDataLoading();
	}

	private void startBorrowDataLoading() {
		// TODO Auto-generated method stub
		
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		
		queryManager.persistBorrowInstance(borrowvalues, bfborrowvalues, accountleft, bfaccountleft, charge, brandid, homeareanumber, msisdn, timestamp, referenceNo, serialno, subcosid, vendorid);
		
		stopWatch.stop();
		log.info("Time taken to comlete BOR data crunching:" + stopWatch.getTime() + "ms");
	}

	private boolean isBorrowCreated(String msisdn, BigDecimal bfborrowvalues, long timestamp2) {
		// TODO Auto-generated method stub

		if (queryManager.getBorrowBySubscriberAndPrincipalAndTimeStamp(msisdn, bfborrowvalues, new Timestamp(timestamp)) != null)
			return true;

		return false;
	}

}
