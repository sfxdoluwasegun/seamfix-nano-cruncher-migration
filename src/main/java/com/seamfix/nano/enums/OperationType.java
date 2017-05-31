package com.seamfix.nano.enums;

public enum OperationType {
	
	LOAN("L", "loan"), 
	RECHARGE("R", "loan payment by recharge"), 
	TRANSFER("T", "loan payment by transfer"), 
	ADJUSTMENT("A", "loan payment by adjustment"), 
	FORCIBLE("F", "forcible loan payment"), 
	CANCELLED("C", "loan payment cancellation due to recharge reversal") ;
	
	private String code ;
	private String description ;
	
	private OperationType(String code, String description) {
		// TODO Auto-generated constructor stub
		
		this.code = code;
		this.description = description;
	}
	
	public static OperationType fromCode(String code){
		
		if (code == null)
			return null;
		
		for (OperationType operationType : OperationType.values()){
			if (operationType.getCode().equalsIgnoreCase(code))
				return operationType;
		}
		
		return null;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

}
