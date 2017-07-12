package com.seamfix.nano.jbeans;

import java.math.BigDecimal;
import java.sql.Timestamp;

import com.nano.jpa.enums.OperationType;

public class CDRbean {
	
	private long subid ;
	private String msisdn ;
	private Timestamp timestamp ;
	private OperationType operationType ;
	private long loanBalanceType ;
	private BigDecimal initialLoanAmount ;
	private BigDecimal initialLoanPoundage ;
	private BigDecimal loanAmount ;
	private BigDecimal loanPoundage ;
	private BigDecimal repayment ;
	private BigDecimal repayPoundage ;
	private Timestamp etuGraceDate ;
	private Timestamp forceRepayDate ;
	private long transid ;
	private Timestamp entryDate ;
	private String offering ;
	private BigDecimal initialEtuAmount ;
	private BigDecimal etuAmount ;
	private long balanceType ;
	private BigDecimal currentBalance ;
	private BigDecimal changeBalance ;
	private String loanVendorId ;
	
	/**
	 * Instantiates new instance of CDRbean.
	 * 
	 * @param subid - subscriber ID
	 * @param msisdn - subscriber primary identifier
	 * @param timestamp - operation time (yyyyMMddHHmmss)
	 * @param operationType - operation type
	 * @param loanBalanceType - loan account book type
	 * @param initialLoanAmount - initial loan amount
	 * @param initialLoanPoundage - initial loan handling fee
	 * @param loanAmount - unpaid loan amount
	 * @param loanPoundage - unpaid loan handling fee
	 * @param repayment - loan payment amount
	 * @param repayPoundage - payment amount of the loan handling fee
	 * @param etuGraceDate - loan payment deadline
	 * @param forceRepayDate - forcible payment date
	 * @param transid - sequence number of a transaction
	 * @param entryDate - time when a record is created
	 * @param offering - primary offering code
	 * @param initialEtuAmount - initial loan penalty
	 * @param etuAmount - loan penalty
	 * @param balanceType - ID of the balance type
	 * @param currentBalance - current balance
	 * @param changeBalance - change amount of the balance
	 * @param loanVendorId - vendor identification
	 */
	public CDRbean(long subid, String msisdn, Timestamp timestamp, OperationType operationType, long loanBalanceType, 
			BigDecimal initialLoanAmount, BigDecimal initialLoanPoundage, BigDecimal loanAmount, BigDecimal loanPoundage, BigDecimal repayment, BigDecimal repayPoundage, 
			Timestamp etuGraceDate, Timestamp forceRepayDate, long transid, Timestamp entryDate, String offering, 
			BigDecimal initialEtuAmount, BigDecimal etuAmount, long balanceType, BigDecimal currentBalance, BigDecimal changeBalance, String loanVendorId) {
		// TODO Auto-generated constructor stub
		
		this.balanceType = balanceType;
		this.changeBalance = changeBalance;
		this.currentBalance = currentBalance;
		this.entryDate = entryDate;
		this.etuAmount = etuAmount;
		this.etuGraceDate = etuGraceDate;
		this.forceRepayDate = forceRepayDate;
		this.initialEtuAmount = initialEtuAmount;
		this.initialLoanAmount = initialLoanAmount;
		this.initialLoanPoundage = initialLoanPoundage;
		this.setLoanAmount(loanAmount);
		this.loanBalanceType = loanBalanceType;
		this.loanPoundage = loanPoundage;
		this.loanVendorId = loanVendorId;
		this.msisdn = msisdn;
		this.offering = offering;
		this.operationType = operationType;
		this.repayment = repayment;
		this.repayPoundage = repayPoundage;
		this.subid = subid;
		this.timestamp = timestamp;
		this.transid = transid;
	}
	
	public long getSubid() {
		return subid;
	}
	
	public void setSubid(long subid) {
		this.subid = subid;
	}

	public Timestamp getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Timestamp timestamp) {
		this.timestamp = timestamp;
	}

	public OperationType getOperationType() {
		return operationType;
	}

	public void setOperationType(OperationType operationType) {
		this.operationType = operationType;
	}

	public long getLoanBalanceType() {
		return loanBalanceType;
	}

	public void setLoanBalanceType(long loanBalanceType) {
		this.loanBalanceType = loanBalanceType;
	}

	public BigDecimal getInitialLoanAmount() {
		return initialLoanAmount;
	}

	public void setInitialLoanAmount(BigDecimal initialLoanAmount) {
		this.initialLoanAmount = initialLoanAmount;
	}

	public BigDecimal getInitialLoanPoundage() {
		return initialLoanPoundage;
	}

	public void setInitialLoanPoundage(BigDecimal initialLoanPoundage) {
		this.initialLoanPoundage = initialLoanPoundage;
	}

	public BigDecimal getLoanPoundage() {
		return loanPoundage;
	}

	public void setLoanPoundage(BigDecimal loanPoundage) {
		this.loanPoundage = loanPoundage;
	}

	public BigDecimal getRepayment() {
		return repayment;
	}

	public void setRepayment(BigDecimal repayment) {
		this.repayment = repayment;
	}

	public BigDecimal getRepayPoundage() {
		return repayPoundage;
	}

	public void setRepayPoundage(BigDecimal repayPoundage) {
		this.repayPoundage = repayPoundage;
	}

	public Timestamp getEtuGraceDate() {
		return etuGraceDate;
	}

	public void setEtuGraceDate(Timestamp etuGraceDate) {
		this.etuGraceDate = etuGraceDate;
	}

	public Timestamp getForceRepayDate() {
		return forceRepayDate;
	}

	public void setForceRepayDate(Timestamp forceRepayDate) {
		this.forceRepayDate = forceRepayDate;
	}

	public long getTransid() {
		return transid;
	}

	public void setTransid(long transid) {
		this.transid = transid;
	}

	public Timestamp getEntryDate() {
		return entryDate;
	}

	public void setEntryDate(Timestamp entryDate) {
		this.entryDate = entryDate;
	}

	public String getOffering() {
		return offering;
	}

	public void setOffering(String offering) {
		this.offering = offering;
	}

	public BigDecimal getInitialEtuAmount() {
		return initialEtuAmount;
	}

	public void setInitialEtuAmount(BigDecimal initialEtuAmount) {
		this.initialEtuAmount = initialEtuAmount;
	}

	public BigDecimal getEtuAmount() {
		return etuAmount;
	}

	public void setEtuAmount(BigDecimal etuAmount) {
		this.etuAmount = etuAmount;
	}

	public long getBalanceType() {
		return balanceType;
	}

	public void setBalanceType(long balanceType) {
		this.balanceType = balanceType;
	}

	public BigDecimal getCurrentBalance() {
		return currentBalance;
	}

	public void setCurrentBalance(BigDecimal currentBalance) {
		this.currentBalance = currentBalance;
	}

	public BigDecimal getChangeBalance() {
		return changeBalance;
	}

	public void setChangeBalance(BigDecimal changeBalance) {
		this.changeBalance = changeBalance;
	}

	public String getLoanVendorId() {
		return loanVendorId;
	}

	public void setLoanVendorId(String loanVendorId) {
		this.loanVendorId = loanVendorId;
	}

	public String getMsisdn() {
		return msisdn;
	}

	public void setMsisdn(String msisdn) {
		this.msisdn = msisdn;
	}

	public BigDecimal getLoanAmount() {
		return loanAmount;
	}

	public void setLoanAmount(BigDecimal loanAmount) {
		this.loanAmount = loanAmount;
	}

}
