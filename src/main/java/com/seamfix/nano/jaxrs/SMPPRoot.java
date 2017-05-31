package com.seamfix.nano.jaxrs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import javax.ejb.Stateless;
import javax.inject.Inject;
import org.jboss.logging.Logger;

import com.nano.jpa.entity.Settings;
import com.nano.jpa.entity.Subscriber;
import com.nano.jpa.enums.SettingType;
import com.seamfix.nano.enums.GeneralSettings;
import com.seamfix.nano.jaxb.RespPojo;
import com.seamfix.nano.tools.QueryManager;

/**
 * The Class SMPPRoot.
 *
 * @author segz
 */

@Stateless
public class SMPPRoot {

	@Inject
	private QueryManager queryManager ;

	private Logger log = Logger.getLogger(getClass());

	/**
	 * Push messages to SMSC gateway via KANNEL sendSms.
	 * 
	 * @param subscriber - subscriber detail
	 * @param message - message to be forwarded to subscriber
	 * @return {@link RespPojo}
	 */
	public RespPojo smscNotification(Subscriber subscriber, 
			String message){

		RespPojo response = null;
		URL url = null;
		BufferedReader bufferedReader = null;

		String smscResponse = null;
		String targetPoint = getSMSCTargetPoint();

		if (targetPoint == null)
			return response;

		String uri = new StringBuilder(targetPoint).append("to=%s&text=%s").toString();

		try {
			uri = String.format(uri, subscriber.getMsisdn(), URLEncoder.encode(message, "ISO-8859-1"));
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			log.error("", e1);
		}

		try {
			url = new URL(uri);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			log.error("", e);
		}

		try {
			HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
			bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));

			smscResponse = bufferedReader.readLine();

			if (smscResponse != null && smscResponse.contains("Sent"))
				return new RespPojo("0", smscResponse);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			log.error("", e);
		} finally {
			if (bufferedReader != null)
				try {
					bufferedReader.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					log.error("", e);
				}
		}

		return new RespPojo("1", "message sending failed");
	}

	/**
	 * Gets the target point.
	 *
	 * @return the target point
	 */
	private String getSMSCTargetPoint(){

		Settings setting = queryManager.getSettingsByName(GeneralSettings.SMSC.getName());
		if (setting != null)
			return setting.getValue();
		else
			return queryManager
					.createSettings(GeneralSettings.SMSC.getName(), 
							GeneralSettings.SMSC.getValue(), 
							GeneralSettings.SMSC.getDescription(), SettingType.GENERAL).getValue();
	}

}