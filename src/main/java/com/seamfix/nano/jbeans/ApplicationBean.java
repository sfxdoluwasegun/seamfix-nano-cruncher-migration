package com.seamfix.nano.jbeans;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.jboss.logging.Logger;

import com.nano.jpa.entity.Nano;
import com.nano.jpa.enums.MerchantData;
import com.seamfix.nano.tools.QueryManager;

@ApplicationScoped
public class ApplicationBean {
	
	private Logger log = Logger.getLogger(getClass());
	
	@Inject
	private QueryManager queryManager ;
	
	private String vendorid = "" ;
	
	@PostConstruct
	public void init(){
		
		Nano nano = queryManager.getNanoByName(MerchantData.NANO.getName());
		if (nano != null){
			vendorid = nano.getVendorId();
			log.info("Nano vendorid:" + vendorid);
		}
	}

	public String getVendorid() {
		return vendorid;
	}

	public void setVendorid(String vendorid) {
		this.vendorid = vendorid;
	}

}
