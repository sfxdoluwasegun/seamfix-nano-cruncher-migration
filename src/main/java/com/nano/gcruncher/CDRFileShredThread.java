package com.nano.gcruncher;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.concurrent.Callable;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.time.StopWatch;
import org.jboss.logging.Logger;

import com.seamfix.nano.cache.InfinispanObjectBucket;
import com.seamfix.nano.enums.OperationType;
import com.seamfix.nano.jbeans.ApplicationBean;
import com.seamfix.nano.jbeans.CDRbean;
import com.seamfix.nano.tools.QueryManager;

public class CDRFileShredThread implements Callable<CDRbean> {

	private Logger log = Logger.getLogger(getClass());

	private String linedata ;

	private QueryManager queryManager ;
	private InfinispanObjectBucket cache ;
	private ApplicationBean appBean ;

	/**
	 * Initiate new CALLABLE thread instance.
	 * 
	 * @param queryManager - {@link QueryManager} singleton EJB
	 * @param appBean - {@link ApplicationBean} singleton EJB
	 * @param cache - {@link InfinispanObjectBucket} singleton EJB
	 * @param linedata - CDR line transaction
	 */
	public CDRFileShredThread(QueryManager queryManager, ApplicationBean appBean, 
			InfinispanObjectBucket cache, String linedata) {
		// TODO Auto-generated constructor stub

		this.appBean = appBean;
		this.cache = cache;
		this.linedata = linedata;
		this.queryManager = queryManager;
	}

	@Override
	public CDRbean call() throws Exception {
		// TODO Auto-generated method stub

		String[] cdrdata = linedata.split("\\|");
		return queueCDRLineForProcessing(cdrdata, linedata);
	}

	private CDRbean queueCDRLineForProcessing(String[] cdrdata, String filetxn) {
		// TODO Auto-generated method stub

		if (cdrdata == null)
			return null;

		String msisdn = cdrdata[1];
		String vendorid = (cdrdata[33] == null || cdrdata[33].isEmpty()) ? "" : cdrdata[33];
		OperationType operationType = OperationType.fromCode(cdrdata[3]);

		/*if (cache.getOutstandingTransactionFromCache(queryManager.formatMisisdn(msisdn.trim())) == null){
			*//**
			 * Subscriber is not on watch-list, however confirm if CDR vendor ID matches.
			 * If vendor ID matches write transaction to list of 'exceptions' before quitting process.
			 *//*
			if (operationType.equals(OperationType.RECHARGE) || operationType.equals(OperationType.TRANSFER)){
				if (!vendorid.isEmpty() && appBean.getVendorid().equalsIgnoreCase(vendorid))
					writeTransactionToFile(filetxn, "com/nano/etls/exception_cdr/");
			}
			return null;
		}

		if (!vendorid.isEmpty() && !appBean.getVendorid().equalsIgnoreCase(vendorid)){
			*//**
			 * SUbscriber is on debtors watch-list but CDR vendor ID doesn't match.
			 * Write file transaction to list of 'suspect' transactions and quit process.
			 * Suspected transactions would have to be visited manually and treated as support issues.
			 *//*
			if (operationType.equals(OperationType.RECHARGE) || operationType.equals(OperationType.TRANSFER))
				writeTransactionToFile(filetxn, "com/nano/etls/suspect_cdr/");
			
			return null;
		}*/

		/**
		 * Subscriber is on watch-list and CDR vendor ID matches.
		 * Transaction should be processed through.
		 */

		StopWatch stopWatch = new StopWatch();
		stopWatch.start();

		long subcriberId = (cdrdata[0] == null || cdrdata[0].isEmpty()) ? 0 : Long.parseLong(cdrdata[0]);

		LocalDateTime localDateTime = LocalDateTime.parse(cdrdata[2], DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
		Timestamp operationTime = Timestamp.valueOf(localDateTime);

		long loanBalanceType = (cdrdata[4] == null || cdrdata[4].isEmpty()) ? 0 : Long.parseLong(cdrdata[4]);
		BigDecimal initialLoanAmount = (cdrdata[5] == null || cdrdata[5].isEmpty()) ? BigDecimal.ZERO : BigDecimal.valueOf(Long.parseLong(cdrdata[5])).divide(BigDecimal.valueOf(100));
		BigDecimal serviceCharge = (cdrdata[6] == null || cdrdata[6].isEmpty()) ? BigDecimal.ZERO : BigDecimal.valueOf(Long.parseLong(cdrdata[6])).divide(BigDecimal.valueOf(100));
		BigDecimal pendingLoan = (cdrdata[7] == null || cdrdata[7].isEmpty()) ? BigDecimal.ZERO : BigDecimal.valueOf(Long.parseLong(cdrdata[7])).divide(BigDecimal.valueOf(100));
		BigDecimal pendingServiceCharge = (cdrdata[8] == null || cdrdata[8].isEmpty()) ? BigDecimal.ZERO : BigDecimal.valueOf(Long.parseLong(cdrdata[8])).divide(BigDecimal.valueOf(100));
		BigDecimal amountPaid = (cdrdata[9] == null || cdrdata[9].isEmpty()) ? BigDecimal.ZERO : BigDecimal.valueOf(Long.parseLong(cdrdata[9])).divide(BigDecimal.valueOf(100));
		BigDecimal recoverdServiceCharge = (cdrdata[10] == null || cdrdata[10].isEmpty()) ? BigDecimal.ZERO : BigDecimal.valueOf(Long.parseLong(cdrdata[10])).divide(BigDecimal.valueOf(100));

		Timestamp etuGraceDate = null;
		Timestamp forceRepaymentDate = null;
		Timestamp entryDate = null;
		
		try {
			LocalDateTime etuGrace = LocalDateTime.parse(cdrdata[11], DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
			etuGraceDate = Timestamp.valueOf(etuGrace);
			
			LocalDateTime forceRepayment = LocalDateTime.parse(cdrdata[12], DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
			forceRepaymentDate = Timestamp.valueOf(forceRepayment);
			
			LocalDateTime entry = LocalDateTime.parse(cdrdata[14], DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
			entryDate = Timestamp.valueOf(entry);
		} catch (DateTimeParseException e) {
			// TODO Auto-generated catch block
			log.error("DateTimePasreException:" + cdrdata[11]);
		}

		long transid = (cdrdata[13] == null || cdrdata[13].isEmpty()) ? 0 : Long.parseLong(cdrdata[13]);

		String offering = cdrdata[15];
		BigDecimal initEtuAmount = (cdrdata[16] == null || cdrdata[16].isEmpty()) ? BigDecimal.ZERO : BigDecimal.valueOf(Long.parseLong(cdrdata[16])).divide(BigDecimal.valueOf(100));
		BigDecimal etuAmount = (cdrdata[17] == null || cdrdata[17].isEmpty()) ? BigDecimal.ZERO : BigDecimal.valueOf(Long.parseLong(cdrdata[17])).divide(BigDecimal.valueOf(100));
		long balanceType = (cdrdata[18] == null || cdrdata[18].isEmpty()) ? 0 : Long.parseLong(cdrdata[18]);
		BigDecimal currentBalance = (cdrdata[19] == null || cdrdata[19].isEmpty()) ? BigDecimal.ZERO : BigDecimal.valueOf(Long.parseLong(cdrdata[19])).divide(BigDecimal.valueOf(100));
		BigDecimal changeBalance = (cdrdata[20] == null || cdrdata[20].isEmpty()) ? BigDecimal.ZERO : BigDecimal.valueOf(Long.parseLong(cdrdata[20])).divide(BigDecimal.valueOf(100));
		
		/*if (operationType.equals(OperationType.RECHARGE) || operationType.equals(OperationType.TRANSFER))
			writeTransactionToFile(filetxn, "com/nano/etls/recharge_cdr/");*/

		stopWatch.stop();
		log.info("Time taken to complete CDR file crunching:" + stopWatch.getTime() + "ms");

		return new CDRbean(subcriberId, queryManager.formatMisisdn(msisdn), operationTime, operationType, loanBalanceType, initialLoanAmount, 
				serviceCharge, pendingLoan, pendingServiceCharge, amountPaid, recoverdServiceCharge, etuGraceDate, 
				forceRepaymentDate, transid, entryDate, offering, initEtuAmount, etuAmount, balanceType, currentBalance, changeBalance, vendorid);
	}

	/**
	 * Write payment information to {@link File} for quick audit purpose.
	 * 
	 * @param transaction - CDR line transaction
	 */
	private void writeTransactionToFile(String transaction, String path){

		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

		String filename = new StringBuilder(path).append("cdr^")
				.append(dateTimeFormatter.format(LocalDateTime.now())).append(".txt").toString();
		File file = new File(filename);

		try {
			FileUtils.writeStringToFile(file, transaction, StandardCharsets.UTF_8, true);
			log.info("writing payment details to file:" + filename);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			log.error("", e);
		}

		if (!file.exists())
			log.error("Error creating payment file");
	}

}