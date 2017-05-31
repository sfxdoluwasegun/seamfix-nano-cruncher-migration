package com.seamfix.nano.enums;

public enum SercomStandardResp {
	
	CDEBT("1001", "Have outstanding debt x", ErrorResponseType.CLIENT), 
	LIFETIME("1002", "Network lifetime x lower", ErrorResponseType.CLIENT), 
	CACTIVE("1003", "Subscriber not in active base", ErrorResponseType.CLIENT), 
	AMOUNT("1004", "Invalid amount x requested", ErrorResponseType.CLIENT), 
	DUPLICATE("1005", "Transaction already submitted (Duplicate)", ErrorResponseType.CLIENT), 
	SDEBT("2001", "Subscriber has existing loan", ErrorResponseType.SERCOM), 
	LINE("2002", "Staff line cannot get loan", ErrorResponseType.SERCOM), 
	POSTPAID("2003", "Post paid line cannot get loan", ErrorResponseType.SERCOM), 
	VENDORID("2004", "Invalid Vendor ID: {0}", ErrorResponseType.SERCOM), 
	SSYSERR("2005", "Sysyem Error. Please try again later", ErrorResponseType.SERCOM), 
	DIAMETER("3001", "Diameter Failure", ErrorResponseType.EVC), 
	UNKNOWN("3002", "Unknown", ErrorResponseType.EVC), 
	FUNDS("3003", "Insufficient funds in trade partnernano-req account", ErrorResponseType.EVC), 
	VOLUMES("3004", "System volumes exceeded", ErrorResponseType.EVC), 
	CONFIGURATION("3005", "Loan apploication failed because no loan amount is configured in the system", ErrorResponseType.EVC), 
	FAILED("3006", "EVC failed to respond", ErrorResponseType.EVC), 
	EACTIVE("3007", "Loan application failed because the subscriber is not in the active state", ErrorResponseType.EVC), 
	EXISTS("6001", "Subscriber does not exist", ErrorResponseType.OCS), 
	DATA("6002", "Internal Error. No Data is Found", ErrorResponseType.OCS), 
	NPE("6003", "Internal Error. Null Pointer Exception", ErrorResponseType.OCS), 
	MODE3("6004", "Access mode 3 does not exist", ErrorResponseType.OCS), 
	QUEUE("6005", "The queue does not exist", ErrorResponseType.OCS), 
	TQUEUE("6006", "Failed to find the target queue", ErrorResponseType.OCS), 
	ROUTING("6007", "No routing information corresponds to the serivce", ErrorResponseType.OCS), 
	OSYSERR("6008", "System Error. Unknown Error", ErrorResponseType.OCS), 
	SUCCESS("0", "Successful Transaction", ErrorResponseType.OCS);
	
	private String code ;
	private String descriptipon ;
	
	private ErrorResponseType type ;
	
	private SercomStandardResp(String code, String description, ErrorResponseType type) {
		// TODO Auto-generated constructor stub
		
		this.code = code;
		this.descriptipon = description;
		this.type = type;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getDescriptipon() {
		return descriptipon;
	}

	public void setDescriptipon(String descriptipon) {
		this.descriptipon = descriptipon;
	}

	public ErrorResponseType getType() {
		return type;
	}

	public void setType(ErrorResponseType type) {
		this.type = type;
	}

}
