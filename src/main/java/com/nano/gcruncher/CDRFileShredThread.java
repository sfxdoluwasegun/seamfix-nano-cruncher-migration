package com.nano.gcruncher;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.concurrent.Callable;

import org.apache.commons.lang.time.StopWatch;
import org.jboss.logging.Logger;

import com.nano.jpa.enums.OperationType;
import com.seamfix.nano.jbeans.CDRbean;
import com.seamfix.nano.tools.QueryManager;

public class CDRFileShredThread implements Callable<CDRbean> {

	private Logger log = Logger.getLogger(getClass());

	private String linedata ;

	private QueryManager queryManager ;
	

	/**
	 * Initiate new CALLABLE thread instance.
	 * 
	 * @param queryManager - {@link QueryManager} singleton EJB
	 * @param linedata - CDR line transaction
	 */
	public CDRFileShredThread(QueryManager queryManager, String linedata) {
		// TODO Auto-generated constructor stub

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
		OperationType operationType = OperationType.fromName(cdrdata[3]);

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
		} catch (DateTimeParseException e) {
			// TODO Auto-generated catch block
			log.error("DateTimePasreException:" + cdrdata[11]);
		}
		
		try {
			LocalDateTime forceRepayment = LocalDateTime.parse(cdrdata[12], DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
			forceRepaymentDate = Timestamp.valueOf(forceRepayment);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.error("DateTimePasreException:" + cdrdata[12]);
		}
		
		try {
			LocalDateTime entry = LocalDateTime.parse(cdrdata[14], DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
			entryDate = Timestamp.valueOf(entry);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.error("DateTimePasreException:" + cdrdata[14]);
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

}