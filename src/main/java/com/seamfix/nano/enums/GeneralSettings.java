package com.seamfix.nano.enums;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

// TODO: Auto-generated Javadoc
/**
 * The Enum GeneralSettings.
 *
 * @author segz
 */

public enum GeneralSettings {
	
	EVCD("EVC DEMO ENDPOINT", "http://10.130.0.81:7777/CommonDataModelProcessSoap", "EVC EDP for recharging a subscriber account on demo request"), 
	EVCL("EVC LIVE ENDPOINT", "http://10.12.10.66:40002/CommonDataModelProcessSoap", "EVC EDP for recharging a subscriber account on LIVE request"), 
	LIVE("MAKE LIVE EVC", "false", "This determines whether EVC recharge requests are forwarded to the Demo or Live platform"), 
	SERCOM("SERVICOM NOTIFY EVENT EDP", "http://10.161.6.11:8080/easycreditrs/ecws/ecservice", "Servicom EDP for notification on subscriber request or payback"), 
	SMPP("SERVICOM SMPP EDP", "http://10.161.6.11:8080/smppjaxrs/smppws/smppservice", "Servicom EDP for SMS notifications"), 
	SMSC("SMSC GATEWAY SENDSMS", "http://10.161.19.23:13013/cgi-bin/sendsms?username=seamfix&password=passwd&", "SMSC Kannel integration sendsms URI"), 
	TEST1("STARTUP EVC TESTS", "false", "This determines if the EVC webservice is tested on server startup"), 
	TEST2("STARTUP SERCOM TESTS", "false", "This determines if the Sercom webservice is tested on server startup"), 
	TEST3("STARTUP SMPP TESTS", "false", "This determines if the SMPP webservice is tested on server startup"), 
	SMARTLOAN("GRANT SMART LOAN AMOUNT", "false", "This determines if the maximum borrowable amount should be granted for Smart Loans"), 
	STAGING("USE STAGING SERVER", "false", "This determines if the service is run using Seamfix test services or Etisalat standard API"), 
	RAS("BYPASS RAS CHECK", "false", "This determines if loan requests should be passed through RAS criteria checks or not"), 
	RECHARGE_TYPE("DEFAULT RECHARGE TYPE CODE", "010", "This determines the default recharge type used in processing EVC requests");
	
	private String name ;	
	private String description ;
	private String value ;
	
	private GeneralSettings(String name, 
			String value, String description){
		
		this.setDescription(description);
		this.setName(name);
		this.setValue(value);
	}
	
	/**
	 * Retrieve enumeration from name string
	 *
	 * @param name - string to use in fetching enumeration
	 * @return enumerations
	 */
	public static GeneralSettings fromName(String name){
		if (name != null && !name.isEmpty())
			for (GeneralSettings generalSettings : GeneralSettings.values()){
				if (generalSettings.getName().equalsIgnoreCase(name))
					return generalSettings;
			}
		
		return null;
	}
	
	/**
	 * Retrieve list of all listed enumerations.
	 *
	 * @return the list
	 */
	public static List<String> literals(){
		List<String> literals = new ArrayList<String>();

		for (GeneralSettings generalSettings : GeneralSettings.values()){
			literals.add(generalSettings.getName());
		}
		
		Collections.sort(literals, new Comparator<String>() {
			public int compare(String a, String b){
				return a.compareToIgnoreCase(b);
			}
		});
		
		return literals;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

}