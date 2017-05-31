package com.seamfix.nano.enums;

// TODO: Auto-generated Javadoc
/**
 * The Enum SercomResponse.
 *
 * @author segz
 */

public enum SercomResponse {
	
	UNWORTH("1", "Subscriber is not deemed loan worthy"),
	INSUFFICIENT("2", "Unable to grant loan at this time"),
	EVC("3", "Unsuccessful EVC recharge"), 
	UNIQUE("4", "Non unique loan request transaction ID"), 
	INTERNAL("5", "Internal Server Error"), 
	EXCEEDS("6", "Subscriber cannot ge granted requested loan amount"), 
	SUCCESS("0", "Successful Transaction ");
	
	private String status ;
	private String descritpion ;
	
	private SercomResponse(String status, 
			String description){
		this.status = status;
		this.descritpion = description;
	}
	
	/**
	 * Retrieve enumeration by status string.
	 *
	 * @param status - enumeration status value
	 * @return {@link SercomResponse} enumeration
	 */
	public static SercomResponse fromStatus(String status){
		if (status != null && !status.isEmpty()){
			for (SercomResponse sercomResponse : SercomResponse.values()){
				if (sercomResponse.getStatus().equalsIgnoreCase(status))
					return sercomResponse;
			}
		}
		return null;
	}
	
	public String getStatus() {
		return status;
	}
	
	public void setStatus(String status) {
		this.status = status;
	}
	
	public String getDescritpion() {
		return descritpion;
	}
	
	public void setDescritpion(String descritpion) {
		this.descritpion = descritpion;
	}

}