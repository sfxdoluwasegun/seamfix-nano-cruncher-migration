package com.seamfix.nano.jaxb;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import com.nano.jpa.enums.ReturnMode;


/**
 * The Class RequestNotification.
 *
 * @author segz
 */

@SuppressWarnings("serial")
@XmlRootElement(name = "request")
@XmlAccessorType(XmlAccessType.FIELD)
public class RequestNotification implements Serializable {
	

	private String msisdn ;
	private String vendorid ;
	private String creditgranted ;
	private String amountdebited ;
	private String outstandingdebt ;
	private String statusCode ;
	private String errorDescription ;
	private String returnMode ;
	
	public RequestNotification(){
		// TODO Auto generated constructor stub
	}
	
	/**
	 * Instantiates a new request notification.
	 *
	 * @param msisdn - subscriber msisdn
	 * @param vendorid - vendor identification
	 * @param creditgranted - loan amount granted to subscriber
	 * @param amountdebited - amount deducted from subscriber balance
	 * @param outstandingdebt - amount pending reimbursement
	 * @param statuscode - response status code
	 * @param errorDescription - response description
	 * @param returnMode - CDR flagged return mode
	 */
	public RequestNotification(String msisdn, 
			String vendorid, String creditgranted, String amountdebited, String outstandingdebt, 
			String statuscode, String errorDescription, 
			ReturnMode returnMode){
		
		this.msisdn = msisdn;
		this.vendorid = vendorid;
		this.creditgranted = creditgranted;
		this.amountdebited = amountdebited;
		this.outstandingdebt = outstandingdebt;
		this.statusCode = statuscode;
		this.errorDescription = errorDescription;
		this.returnMode = returnMode.getMode();
	}
	
	public String getMsisdn() {
		return msisdn;
	}
	
	public void setMsisdn(String msisdn) {
		this.msisdn = msisdn;
	}

	public String getVendorid() {
		return vendorid;
	}

	public void setVendorid(String vendorid) {
		this.vendorid = vendorid;
	}

	public String getCreditgranted() {
		return creditgranted;
	}

	public void setCreditgranted(String creditgranted) {
		this.creditgranted = creditgranted;
	}

	public String getAmountdebited() {
		return amountdebited;
	}

	public void setAmountdebited(String amountdebited) {
		this.amountdebited = amountdebited;
	}

	public String getOutstandingdebt() {
		return outstandingdebt;
	}

	public void setOutstandingdebt(String outstandingdebt) {
		this.outstandingdebt = outstandingdebt;
	}

	public String getErrorDescription() {
		return errorDescription;
	}

	public void setErrorDescription(String errorDescription) {
		this.errorDescription = errorDescription;
	}

	public String getReturnMode() {
		return returnMode;
	}

	public void setReturnMode(String returnMode) {
		this.returnMode = returnMode;
	}

	public String getStatusCode() {
		return statusCode;
	}

	public void setStatusCode(String statusCode) {
		this.statusCode = statusCode;
	}

}