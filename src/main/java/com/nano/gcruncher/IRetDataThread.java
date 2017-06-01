package com.nano.gcruncher;

import java.math.BigDecimal;
import java.sql.Timestamp;

import org.apache.commons.lang.time.StopWatch;
import org.jboss.logging.Logger;

import com.nano.jpa.entity.Subscriber;
import com.nano.jpa.enums.ReturnMode;
import com.seamfix.nano.cache.InfinispanObjectBucket;
import com.seamfix.nano.tools.QueryManager;

public class IRetDataThread implements Runnable {
	
	private Logger log = Logger.getLogger(getClass());
	
	private QueryManager queryManager ;
	
	private BigDecimal accountleft ; 
	private BigDecimal afaccountleft ;
	private BigDecimal borrowvaluesaf ;
	private BigDecimal borrowvaluesbf ;
	private Long brandid ;
	private BigDecimal etupenaltyaf ;
	private BigDecimal etupenaltybf ;
	private BigDecimal returnamount ;
	private int returnmode ;
	private Long subcosid ; 
	
	private long timestamp ;
	
	private String msisdn ;
	private String serialno ;
	private String triggermsisdn ;
	private String vendorid;
	
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
	public IRetDataThread(QueryManager queryManager, 
			InfinispanObjectBucket infinispanBucketCache, 
			BigDecimal accountleft, BigDecimal afaccountleft, BigDecimal borrowvaluesaf, BigDecimal borrowvaluesbf, long brandid, BigDecimal etupenaltyaf, BigDecimal etupenaltybf, 
			BigDecimal returnamount, int returnmode, long subcosid, 
			long timestamp, 
			String msisdn, String serialno, String triggermsisdn, String vendorid) {
		// TODO Auto-generated constructor stub
		
		this.accountleft = accountleft;
		this.afaccountleft = afaccountleft;
		this.borrowvaluesaf = borrowvaluesaf;
		this.borrowvaluesbf = borrowvaluesbf;
		this.brandid = brandid;
		this.etupenaltyaf = etupenaltyaf;
		this.etupenaltybf = etupenaltybf;
		this.msisdn = msisdn;
		this.queryManager = queryManager;
		this.returnamount = returnamount;
		this.returnmode = returnmode;
		this.serialno = serialno;
		this.subcosid = subcosid;
		this.timestamp = timestamp;
		this.triggermsisdn = triggermsisdn;
		this.vendorid = vendorid;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		
		//Subscriber subscriber = queryManager.createSubscriber(msisdn);
		
		if (queryManager.getPaymentByBorrowAndTimestamp(msisdn, new Timestamp(timestamp)) != null)
			return ;
		
		startRechargeDataLoading(returnamount, borrowvaluesaf);
	}

	private void startRechargeDataLoading(BigDecimal returnamount2, BigDecimal borrowvaluesaf2) {
		// TODO Auto-generated method stub
		
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		
		ReturnMode returnMode = returnmode == 1 ? ReturnMode.RECHARGE : ReturnMode.TRANSFER;

		queryManager.persistPaymentRecord(returnMode, 
				borrowvaluesbf, borrowvaluesaf, afaccountleft, accountleft, etupenaltyaf, etupenaltybf, brandid, subcosid, 
				timestamp, serialno, msisdn, returnamount, triggermsisdn, vendorid);
		
		stopWatch.stop();
		log.info("Time taken to comlete RET data crunching:" + stopWatch.getTime() + "ms");
	}

}
