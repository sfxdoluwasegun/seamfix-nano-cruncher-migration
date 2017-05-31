package com.seamfix.nano.jaxrs;

import java.net.URI;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import org.jboss.logging.Logger;

import com.nano.jpa.entity.Settings;
import com.nano.jpa.entity.Subscriber;
import com.nano.jpa.enums.EventType;
import com.nano.jpa.enums.ReturnMode;
import com.nano.jpa.enums.SettingType;
import com.seamfix.nano.enums.GeneralSettings;
import com.seamfix.nano.enums.SercomStandardResp;
import com.seamfix.nano.jaxb.AlternateRespPojo;
import com.seamfix.nano.jaxb.PaymentNotification;
import com.seamfix.nano.tools.QueryManager;

/**
 * The Class SCMRoot.
 *
 * @author segz
 */

@Stateless
public class SCMRoot {

	@Inject
	private QueryManager queryManager ;
	
	private Logger logger = Logger.getLogger(getClass());

	/**
	 * Revert subscribers re-payment.
	 *
	 * @param vendorId - vendor identification from CDR transaction
	 * @param subscriber - subscriber detail
	 * @param sercomStandardResp - enumeration containing standard response status and codes
	 * @param amountdebited - amount retrieved from subscriber balance
	 * @param outstandingDebt - pending loan balance
	 * @param referenceNo - transaction reference number from loan request
	 * @param eventType - transaction summary description
	 * @param returnMode - transaction medium as captured from CDR
	 * @return {@link AlternateRespPojo}
	 */
	public AlternateRespPojo revertSubscribersRepayment(String vendorId, 
			Subscriber subscriber, 
			SercomStandardResp sercomStandardResp, String amountdebited, String outstandingDebt, String referenceNo, EventType eventType, ReturnMode returnMode){

		AlternateRespPojo response = null;

		URI targetPoint = getTargetPoint();

		if (targetPoint == null)
			return response;
		
		PaymentNotification paymentNotification = new PaymentNotification(subscriber.getMsisdn(), 
				vendorId, "0", amountdebited, outstandingDebt, sercomStandardResp.getCode(), sercomStandardResp.getDescriptipon(), returnMode, referenceNo, eventType);
		
		Client client = null;
		try {
			client = ClientBuilder.newClient();
			response = client.target(targetPoint).path("/notifyEvent")
					.request(MediaType.APPLICATION_XML)
					.post(Entity.xml(paymentNotification), AlternateRespPojo.class);
		} catch (WebApplicationException e) {
			logger.error("", e);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.error("", e);
		}finally{
			if (client != null)
				client.close();
		}

		return response;
	}

	/**
	 * Gets the target point.
	 *
	 * @return the target point
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	private URI getTargetPoint(){
		URI uri = null;

		Settings setting = queryManager.createSettings(GeneralSettings.SERCOM.getName(), 
						GeneralSettings.SERCOM.getValue(), GeneralSettings.SERCOM.getDescription(), SettingType.GENERAL);
		
		if (setting != null)
			uri = getBaseURI(setting.getValue());

		return uri;
	}

	/**
	 * Gets the base URI.
	 *
	 * @param url - service end-point address
	 * @return {@link URI}
	 */
	private URI getBaseURI(String url) {
		return UriBuilder.fromUri(url).build();
	}

}