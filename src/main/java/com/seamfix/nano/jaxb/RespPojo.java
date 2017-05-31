package com.seamfix.nano.jaxb;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

// TODO: Auto-generated Javadoc
/**
 * The Class RespPojo.
 */
@SuppressWarnings("serial")
@XmlRootElement(name = "response")
@XmlAccessorType(XmlAccessType.FIELD)
public class RespPojo implements Serializable {
	
	private String description = null;
	private String code = null;
	
	public RespPojo(){
		// TODO Auto generated constructor stub
	}
	
	public RespPojo(String description, 
			String code){
		
		this.setDescription(description);
		this.setCode(code);
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}
	
}