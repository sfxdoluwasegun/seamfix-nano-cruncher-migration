package com.nano.gcruncher;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.annotation.Resource;
import javax.ejb.Asynchronous;
import javax.ejb.Stateless;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.inject.Inject;

import org.jboss.logging.Logger;

import com.nano.jpa.entity.Borrow;
import com.nano.jpa.entity.Subscriber;
import com.nano.jpa.enums.EventType;
import com.nano.jpa.enums.MerchantData;
import com.nano.jpa.enums.ReturnMode;
import com.seamfix.nano.cache.InfinispanObjectBucket;
import com.seamfix.nano.enums.OperationType;
import com.seamfix.nano.enums.SercomStandardResp;
import com.seamfix.nano.jbeans.ApplicationBean;
import com.seamfix.nano.jbeans.CDRbean;
import com.seamfix.nano.tools.MessageModel;
import com.seamfix.nano.tools.NotificationManager;
import com.seamfix.nano.tools.QueryManager;

import freemarker.template.TemplateException;

@Stateless
public class CDRCruncher {
	
	private Logger log = Logger.getLogger(getClass());

	@Resource
	private ManagedExecutorService managedExecutorService ;
	
	@Inject
	private QueryManager queryManager ;
	
	@Inject
	private ApplicationBean appBean ;
	
	@Inject
	private InfinispanObjectBucket cache ;

	@Inject
	private NotificationManager notificationManager;
	
	/**
	 * Iterate through file transactions and push for crunching.
	 * 
	 * @param file - CDR file
	 * @throws IOException - I/O exception
	 */
	@Asynchronous
	public void handleFile(File file) throws IOException{
		
		List<String> lines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
		for (String line : lines){
			doCDRDataCrunch(line);
		}
	}
	
	/**
	 * Handle parallel crunching of RET file line transaction.
	 * 
	 * @param linedata - CDR transaction line
	 */
	private void doCDRDataCrunch(String linedata){

		CDRFileShredThread cdrFileShredThread = new CDRFileShredThread(queryManager, appBean, cache, linedata);
		Future<CDRbean> job = managedExecutorService.submit(cdrFileShredThread);
		
		try {
			CDRbean cdRbean = job.get();
			if (cdRbean == null)
				return;
			
			if (cdRbean.getOperationType().equals(OperationType.LOAN))
				handleBorrowLoading(cdRbean);
			
			if (cdRbean.getOperationType().equals(OperationType.RECHARGE) 
					|| cdRbean.getOperationType().equals(OperationType.TRANSFER))
				handleRechargeLoading(cdRbean);
			
		} catch (InterruptedException | ExecutionException e) {
			// TODO Auto-generated catch block
			log.error("", e);
		}
		
	}

	/**
	 * Implement RUNNABLE task to perform BORROR data loading and persistence.
	 * 
	 * @param cdRbean
	 */
	private void handleBorrowLoading(CDRbean cdRbean) {
		// TODO Auto-generated method stub
		
		IBorDataThread iBorDataThread = new IBorDataThread(queryManager, cache, cdRbean.getCurrentBalance(), cdRbean.getCurrentBalance().add(cdRbean.getChangeBalance()), 
				cdRbean.getInitialLoanAmount(), cdRbean.getLoanAmount(), cdRbean.getInitialLoanPoundage(), cdRbean.getTransid(), cdRbean.getSubid(), cdRbean.getSubid(), cdRbean.getMsisdn(), "", 
				cdRbean.getLoanVendorId(), "", cdRbean.getTimestamp().getTime());
		managedExecutorService.execute(iBorDataThread);
		
		/*BorDataThread borDataThread = new BorDataThread(queryManager, cache, cdRbean.getCurrentBalance(), cdRbean.getCurrentBalance().add(cdRbean.getChangeBalance()), 
				cdRbean.getInitialLoanAmount(), cdRbean.getLoanAmount(), cdRbean.getTransid(), cdRbean.getSubid(), cdRbean.getSubid(), cdRbean.getMsisdn(), "", 
				cdRbean.getLoanVendorId(), cdRbean.getTimestamp().getTime());
		managedExecutorService.execute(borDataThread);*/
	}

	/**
	 * Implement CALLABLE task to perform RECHARGE data loading and persistence.
	 * 
	 * @param cdRbean 
	 * @throws ExecutionException 
	 * @throws InterruptedException 
	 * 
	 */
	private void handleRechargeLoading(CDRbean cdRbean) throws InterruptedException, ExecutionException {
		// TODO Auto-generated method stub
		
		int returnmode = cdRbean.getOperationType().equals(OperationType.RECHARGE) ? 1 : 2 ;
		
		IRetDataThread iRetDataThread = new IRetDataThread(queryManager, cache, cdRbean.getCurrentBalance().add(cdRbean.getChangeBalance()), cdRbean.getCurrentBalance(), 
				cdRbean.getLoanAmount(), cdRbean.getInitialLoanAmount(), cdRbean.getSubid(), cdRbean.getEtuAmount(), cdRbean.getInitialEtuAmount(), cdRbean.getRepayment(), 
				returnmode, 0, cdRbean.getTimestamp().getTime(), cdRbean.getMsisdn(), "", cdRbean.getMsisdn(), cdRbean.getLoanVendorId());
		managedExecutorService.execute(iRetDataThread);
		
		/*RetDataThread retDataThread = new RetDataThread(queryManager, cache, cdRbean.getCurrentBalance().add(cdRbean.getChangeBalance()), cdRbean.getCurrentBalance(), 
				cdRbean.getLoanAmount(), cdRbean.getInitialLoanAmount(), cdRbean.getSubid(), cdRbean.getEtuAmount(), cdRbean.getInitialEtuAmount(), cdRbean.getRepayment(), 
				returnmode, 0, cdRbean.getTimestamp().getTime(), cdRbean.getMsisdn(), "", cdRbean.getMsisdn());
		
		Future<Map<String, Object>> job2 = managedExecutorService.submit(retDataThread);
		Map<String, Object> response = job2.get();
		
		if (response == null)
			return;
		
		try {
			notificationManager.doSMPPRevert((Subscriber) response.get("subscriber"), null, (MessageModel) response.get("messageModel"), 
					(String) response.get("paymentRef"), (Long) response.get("notificationpk"));
		} catch (IOException | TemplateException e) {
			// TODO Auto-generated catch block
			log.error("", e);
		}
		
		notificationManager.doSercomRevert((Borrow) response.get("borrow"), 
				queryManager.getNanoByName(MerchantData.NANO.getName()), (SercomStandardResp) response.get("sercomResponse"), 
				(String) response.get("paymentRef"), (String) response.get("amountdebited"), (String) response.get("outstandingDebt"), 
				(Long) response.get("notificationpk"), (String) response.get("referenceNo"), (EventType) response.get("eventType"), (ReturnMode) response.get("returnMode"));*/
	}

}