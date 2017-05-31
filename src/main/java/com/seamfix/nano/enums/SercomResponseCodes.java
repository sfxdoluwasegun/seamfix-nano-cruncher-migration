package com.seamfix.nano.enums;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

// TODO: Auto-generated Javadoc
/**
 * The Enum SercomResponseCodes.
 *
 * @author segz
 */

public enum SercomResponseCodes {
	
	SUCCESS("0"), 
	FAIL("1"), 
	INVALIDVENDOR("2"), 
	INVALIDPARAMS("3"), 
	INELIGIBLE("4"), 
	INACTIVE("100"), 
	ERROR("999");
	
	private String code ;
	
	private SercomResponseCodes(String code){
		
		this.setCode(code);
	}
	
	/**
	 * Retrieve enumeration from code string
	 *
	 * @param code - the enumeration code value
	 * @return {@link SercomResponseCodes} enumeration
	 */
	public static SercomResponseCodes fromCode(String code){
		if (code != null && !code.isEmpty())
			for (SercomResponseCodes sercomResponseCodes : SercomResponseCodes.values()){
				if (sercomResponseCodes.getCode().equalsIgnoreCase(code))
					return sercomResponseCodes;
			}
		
		return null;
	}
	
	/**
	 * Retrieve list of all declared enumerations.
	 *
	 * @return the list
	 */
	public static List<String> literals(){
		List<String> literals = new ArrayList<String>();

		for (SercomResponseCodes sercomResponseCodes : SercomResponseCodes.values()){
			literals.add(sercomResponseCodes.getCode());
		}
		
		Collections.sort(literals, new Comparator<String>() {
			public int compare(String a, String b){
				return a.compareToIgnoreCase(b);
			}
		});
		
		return literals;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

}