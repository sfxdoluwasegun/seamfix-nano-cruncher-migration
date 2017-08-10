package com.nano.gcruncher;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.annotation.Resource;
import javax.ejb.Asynchronous;
import javax.ejb.Stateless;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.inject.Inject;

import org.jboss.logging.Logger;

import com.seamfix.nano.jbeans.CDRbean;
import com.seamfix.nano.tools.ApplicationBean;
import com.seamfix.nano.tools.DbManager;
import com.seamfix.nano.tools.QueryManager;

@Stateless
public class CDRCruncher {
	
	private Logger log = Logger.getLogger(getClass());

	@Resource
	private ManagedExecutorService managedExecutorService ;
	
	@Inject
	private QueryManager queryManager ;
	
	@Inject
	private DbManager dbManager ;
	
	@Inject
	private ApplicationBean appBean ;
	
	/**
	 * Iterate through file transactions and push for crunching.
	 * 
	 * @param file CDR file
	 * @throws IOException I/O exception
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
	 * @param linedata CDR transaction line
	 */
	private void doCDRDataCrunch(String linedata){

		CDRFileShredThread cdrFileShredThread = new CDRFileShredThread(queryManager, linedata);
		Future<CDRbean> job = managedExecutorService.submit(cdrFileShredThread);
		
		try {
			CDRbean cdRbean = job.get();
			if (cdRbean == null)
				return;
			
			handleCDRLoading(cdRbean);
		} catch (InterruptedException | ExecutionException e) {
			// TODO Auto-generated catch block
			log.error("", e);
		}
		
	}

	/**
	 * 
	 * @param cdRbean
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	private void handleCDRLoading(CDRbean cdRbean) throws InterruptedException, ExecutionException {
		// TODO Auto-generated method stub
		
		CDRDataThread cdrDataThread = new CDRDataThread(queryManager, dbManager, appBean, cdRbean.getBalanceType(), cdRbean.getChangeBalance(), cdRbean.getCurrentBalance(), cdRbean.getEntryDate(), 
				cdRbean.getEtuAmount(), cdRbean.getEtuGraceDate(), cdRbean.getForceRepayDate(), cdRbean.getInitialEtuAmount(), cdRbean.getInitialLoanAmount(), cdRbean.getInitialLoanPoundage(), 
				cdRbean.getLoanAmount(), cdRbean.getLoanBalanceType(), cdRbean.getLoanPoundage(), cdRbean.getLoanVendorId(), cdRbean.getMsisdn(), cdRbean.getOffering(), 
				cdRbean.getOperationType(), cdRbean.getRepayment(), cdRbean.getRepayPoundage(), cdRbean.getSubid(), cdRbean.getTimestamp(), cdRbean.getTransid());
		
		managedExecutorService.execute(cdrDataThread);
	}

}