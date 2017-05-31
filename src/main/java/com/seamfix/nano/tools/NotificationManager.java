package com.seamfix.nano.tools;

import java.io.IOException;
import java.io.StringWriter;
import java.sql.Timestamp;
import java.util.Calendar;

import javax.ejb.Asynchronous;
import javax.ejb.Stateless;
import javax.inject.Inject;

import org.jboss.logging.Logger;

import com.nano.jpa.entity.Loan;
import com.nano.jpa.entity.Nano;
import com.nano.jpa.entity.SercomNotification;
import com.nano.jpa.entity.SmppNotification;
import com.nano.jpa.entity.Subscriber;
import com.nano.jpa.enums.EventType;
import com.nano.jpa.enums.NotificationType;
import com.nano.jpa.enums.ReturnMode;
import com.seamfix.nano.enums.SercomResponseCodes;
import com.seamfix.nano.enums.SercomStandardResp;
import com.seamfix.nano.jaxb.AlternateRespPojo;
import com.seamfix.nano.jaxb.RespPojo;
import com.seamfix.nano.jaxrs.SCMRoot;
import com.seamfix.nano.jaxrs.SMPPRoot;

import freemarker.template.TemplateException;

/**
 * Handle notifications on transaction status to web services.
 * 
 * @author segz
 *
 */

@Stateless
public class NotificationManager {
	
	private Logger log = Logger.getLogger(getClass());
	
	@Inject
	private SCMRoot sercom ;

	@Inject
	private SMPPRoot smpp ;

	@Inject
	private MessageTemplate messageTemplate ;

	@Inject
	private QueryManager queryManager ;

	/**
	 * Send notification to SERCOM on loan request/query status.
	 * 
	 * @param subscriber - details of subscriber
	 * @param nano - NANO merchant detail
	 * @param sercomStandardResp - enumeration containing standard response properties
	 * @param paymentRef - payment reference from EVC
	 * @param amountdebited - amount deducted from subscribers account
	 * @param outstandingDebt - pending loan amount
	 * @param notificationpk - notification log reference to loan
	 * @param referenceNo - transaction reference number
	 * @param eventType - CDR captured event type
	 * @param returnmode - transaction medium
	 */
	@Asynchronous
	public void doSercomRevert(Subscriber subscriber, 
			Nano nano, SercomStandardResp sercomStandardResp, 
			String paymentRef, String amountdebited, String outstandingDebt, 
			long notificationpk, String referenceNo, EventType eventType, ReturnMode returnmode){

		AlternateRespPojo response = sercom.revertSubscribersRepayment(nano.getVendorId(), 
				subscriber, sercomStandardResp, 
				amountdebited, outstandingDebt, referenceNo, eventType, returnmode);

		if (response == null)
			return ;
		
		logSercomResponse(response.getResponseCode(), response.getResponseString(),  
				sercomStandardResp.getCode() + ":" + sercomStandardResp.getDescriptipon(), 
				paymentRef, notificationpk);
		
		if (response.getResponseCode().equalsIgnoreCase(SercomResponseCodes.FAIL.getCode()) 
				|| response.getResponseCode().equalsIgnoreCase(SercomResponseCodes.ERROR.getCode()) 
				|| response.getResponseCode().equalsIgnoreCase(SercomResponseCodes.INACTIVE.getCode())){
			//handle failure
		}
	}
	
	/**
	 * Log SERCOM response.
	 * 
	 * @param status - transaction status
	 * @param description - transaction response description
	 * @param message - response message
	 * @param paymentRef - payment reference from EVC
	 * @param notificationpk - notification log reference to loan
	 */
	public void logSercomResponse(String status, 
			String description, String message, String paymentRef, 
			long notificationpk){
		
		String response = status + ":" + description;
		
		try {
			SercomNotification sercomNotification = new SercomNotification();
			sercomNotification.setMessage(message);
			sercomNotification.setNotificationPk(notificationpk);
			sercomNotification.setNotificationTime(new Timestamp(Calendar.getInstance().getTime().getTime()));
			sercomNotification.setNotificationType(NotificationType.PAYMENT);
			sercomNotification.setResponse(response);
			sercomNotification.
			setPaymentref(paymentRef);
			
			queryManager.create(sercomNotification);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.error("", e);
		}
	}

	/**
	 * Handle SMPP revert notification.
	 * 
	 * @param subscriber - subscriber details
	 * @param loan - loan request details
	 * @param messageModel - message template model
	 * @param paymentRef - payment reference from EVC
	 * @param notificationpk - notification log reference to loan
	 * @throws IOException - I/O exception
	 * @throws TemplateException - free-marker template exception
	 */
	@Asynchronous
	public void doSMPPRevert(Subscriber subscriber, 
			Loan loan, MessageModel messageModel, 
			String paymentRef, 
			long notificationpk) throws IOException, TemplateException{

		StringWriter stringWriter = null;
		RespPojo response = null;

		stringWriter = messageTemplate.transform(messageModel.getModel());

		if (stringWriter != null)
			response = smpp.smscNotification(subscriber, stringWriter.toString());
		
		//response = smpp.smppNotifiation(smppCapsule.getSubscriber(), stringWriter.toString(), nano.getServiceId(), NanoConstants.MESSAGE_TYPE_TEXT, nano.getSmppShortCode());

		if (response == null)
			return ;
		
		if (stringWriter != null)
			logSmppRevertResponse(response.getCode(), response.getDescription(), stringWriter.toString(), 
					paymentRef, notificationpk);
		else
			logSmppRevertResponse(response.getCode(), response.getDescription(), null, 
					paymentRef, notificationpk);
	}
	
	/**
	 * Log SMPP revert response.
	 * 
	 * @param status - transaction status
	 * @param description - response description
	 * @param sms - message forwarded
	 * @param paymentRef - payment reference from EVC
	 * @param notificationpk - notification log reference to loan
	 */
	public void logSmppRevertResponse(String status, 
			String description, String sms, String paymentRef, 
			long notificationpk){
		
		String response = status + ":" + description;
		
		try {
			SmppNotification smppNotification = new SmppNotification();
			smppNotification.setNotificationPk(notificationpk);
			smppNotification.setNotificationTime(new Timestamp(Calendar.getInstance().getTime().getTime()));
			smppNotification.setNotificationType(NotificationType.PAYMENT);
			smppNotification.setResponse(response);
			smppNotification.setSms(sms);
			smppNotification.
			setPaymentref(paymentRef);
			
			queryManager.create(smppNotification);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.error("", e);
		}
	}

}