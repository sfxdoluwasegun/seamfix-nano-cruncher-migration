package com.seamfix.nano.jaxb;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

// TODO: Auto-generated Javadoc
/**
 * The Class AlternateRespPojo.
 *
 * @author segz
 */

@SuppressWarnings("serial")
@XmlRootElement(name = "response")
@XmlAccessorType(XmlAccessType.FIELD)
public class AlternateRespPojo implements Serializable {
	
	private String responseCode ;
	private String responseString ;
	
	public AlternateRespPojo(){
		// TODO Auto generated constructor stub
	}
	
	public AlternateRespPojo(String responseCode, 
			String responseString){
		
		this.responseCode = responseCode;
		this.setResponseString(responseString);
	}
	
	public String getResponseCode() {
		return responseCode;
	}
	
	public void setResponseCode(String responseCode) {
		this.responseCode = responseCode;
	}

	public String getResponseString() {
		return responseString;
	}

	public void setResponseString(String responseString) {
		this.responseString = responseString;
	}

}