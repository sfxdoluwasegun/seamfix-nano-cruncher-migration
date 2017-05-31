package com.seamfix.nano.tools;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;

import org.jboss.logging.Logger;

import com.nano.jpa.enums.MerchantData;

@Startup
@Singleton
public class StartupManager {
	
	private Logger log = Logger.getLogger(getClass());
	
	@Inject
	private QueryManager queryManager ;
	
	@PostConstruct
	public void start(){
		log.info("Starting up CDR Cruncher EJB");
		queryManager.getNanoByName(MerchantData.NANO.getName());
	}

}
