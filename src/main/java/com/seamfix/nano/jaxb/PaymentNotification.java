package com.seamfix.nano.jaxb;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.nano.jpa.enums.EventType;
import com.nano.jpa.enums.ReturnMode;

@SuppressWarnings("serial")
@XmlRootElement(name = "request")
@XmlAccessorType(XmlAccessType.FIELD)
public class PaymentNotification implements Serializable {
	
	private String msisdn ;
	private String vendorid ;
	private String creditgranted ;
	private String amountdebited ;
	private String outstandingdebt ;
	private String statusCode ;
	private String errorDescription ;
	private String returnMode ;

	@XmlElement(nillable = true)
	private String transactionId ;
	
	@XmlElement(nillable = true)
	private String eventType ;
	
	public PaymentNotification(){
		// TODO Auto generated constructor stub
	}
	
	/**
	 * Instantiates a new payment notification.
	 * 
	 * @param msisdn - subscriber MSISDN
	 * @param vendorid - vendor identification
	 * @param creditgranted - amount loaned to subscriber
	 * @param amountdebited - amount deducted from subscriber balance
	 * @param outstandingdebt - outstanding loan amount
	 * @param statusCode - response status code
	 * @param errorDescription - response description
	 * @param returnMode - reimbursement mode
	 * @param transactionId - transaction identity
	 * @param eventType - registered event type
	 */
	public PaymentNotification(String msisdn, 
			String vendorid, String creditgranted, String amountdebited, String outstandingdebt, 
			String statusCode, String errorDescription, 
			ReturnMode returnMode, String transactionId, EventType eventType){
		
		this.msisdn = msisdn;
		this.vendorid = vendorid;
		this.creditgranted = creditgranted;
		this.amountdebited = amountdebited;
		this.outstandingdebt = outstandingdebt;
		this.statusCode = statusCode;
		this.errorDescription = errorDescription;
		this.returnMode = returnMode.getMode();
		this.setTransactionId(transactionId);
		this.setEventType(eventType.getValue());
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		
		return "amountdebited:" + this.amountdebited 
				+ " creditgranted:" + this.creditgranted 
				+ " eventType:" + this.eventType 
				+ " msisdn:" + this.msisdn 
				+ " outstandingdebt:" + this.outstandingdebt 
				+ " returnMode:" + this.returnMode 
				+ " statusCode:" + this.statusCode 
				+ " statusDescription:" + this.errorDescription 
				+ " transactionId:" + this.transactionId 
				+ " vendorid:" + this.vendorid;
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

	public String getStatusCode() {
		return statusCode;
	}

	public void setStatusCode(String statusCode) {
		this.statusCode = statusCode;
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

	public String getTransactionId() {
		return transactionId;
	}

	public void setTransactionId(String transactionId) {
		this.transactionId = transactionId;
	}

	public String getEventType() {
		return eventType;
	}

	public void setEventType(String eventType) {
		this.eventType = eventType;
	}

}