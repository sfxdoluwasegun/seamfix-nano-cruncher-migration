package com.nano.gcruncher.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import com.nano.jpa.entity.parent.IEntity;
import com.nano.jpa.enums.ReturnMode;


@Entity
public class IPayment extends IEntity implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@Column(name = "TRIGGER_MSISDN", nullable = true)
	private String triggerMsisdn;
	
	@Enumerated(EnumType.STRING)
	@Column(name = "RETURN_MODE", nullable = true)
	private ReturnMode returnMode;
	
	@Column(name = "AMT_PAID", nullable = false)
	private BigDecimal amountPaid;
	
	@Column(name = "BALANCE_BEFORE", nullable = true)
	private BigDecimal balanceBeforePayment;
	
	@Column(name = "BALANCE_AFTER", nullable = true)
	private BigDecimal balanceAfterPayment;
	
	@Column(name = "AMT_OWED_BEFORE", nullable = true)
	private BigDecimal amountOwedBeforePayment;
	
	@Column(name = "AMT_OWED_AFTER", nullable = true)
	private BigDecimal amountOwedAfterPayment;
	
	@Column(name = "PENALTY_BEFORE", nullable = true)
	private BigDecimal loanPenaltyBeforePayment;
	
	@Column(name = "PENALTY_AFTER", nullable = true)
	private BigDecimal loanPenaltyAfterPayment;
	
	@Column(nullable = true)
	private Timestamp rechargeTime ;
	
	@Column(nullable = true)
	private BigDecimal rechargeAmount;
	
	@Column(name = "SERIAL_NO", nullable = true)
	private String serialNo;
	
	@Column(name = "PROCESSED_TIMESTAMP", nullable = true)
	private Timestamp processedTimestamp;
	
	@Column(name = "BRAND_ID", nullable = true)
	private Long brandId;
	
	@Column(name = "SUB_COS_ID", nullable = true)
	private Long subCosId;
	
	@Column(nullable = false)
	private String msisdn ;
	
	@Column(name = "vendor_id", nullable = true)
	private String vendorId ;

	public String getSerialNo() {
		return serialNo;
	}

	public void setSerialNo(String serialNo) {
		this.serialNo = serialNo;
	}

	public Timestamp getProcessedTimestamp() {
		return processedTimestamp;
	}

	public void setProcessedTimestamp(Timestamp processedTimestamp) {
		this.processedTimestamp = processedTimestamp;
	}

	public Long getBrandId() {
		return brandId;
	}

	public void setBrandId(Long brandId) {
		this.brandId = brandId;
	}

	public Long getSubCosId() {
		return subCosId;
	}

	public void setSubCosId(Long subCosId) {
		this.subCosId = subCosId;
	}

	public String getTriggerMsisdn() {
		return triggerMsisdn;
	}

	public void setTriggerMsisdn(String triggerMsisdn) {
		this.triggerMsisdn = triggerMsisdn;
	}

	public ReturnMode getReturnMode() {
		return returnMode;
	}

	public void setReturnMode(ReturnMode returnMode) {
		this.returnMode = returnMode;
	}

	public BigDecimal getAmountPaid() {
		return amountPaid;
	}

	public void setAmountPaid(BigDecimal amountPaid) {
		this.amountPaid = amountPaid;
	}

	public BigDecimal getBalanceBeforePayment() {
		return balanceBeforePayment;
	}

	public void setBalanceBeforePayment(BigDecimal balanceBeforePayment) {
		this.balanceBeforePayment = balanceBeforePayment;
	}

	public BigDecimal getBalanceAfterPayment() {
		return balanceAfterPayment;
	}

	public void setBalanceAfterPayment(BigDecimal balanceAfterPayment) {
		this.balanceAfterPayment = balanceAfterPayment;
	}

	public BigDecimal getAmountOwedBeforePayment() {
		return amountOwedBeforePayment;
	}

	public void setAmountOwedBeforePayment(BigDecimal amountOwedBeforePayment) {
		this.amountOwedBeforePayment = amountOwedBeforePayment;
	}

	public BigDecimal getAmountOwedAfterPayment() {
		return amountOwedAfterPayment;
	}

	public void setAmountOwedAfterPayment(BigDecimal amountOwedAfterPayment) {
		this.amountOwedAfterPayment = amountOwedAfterPayment;
	}

	public BigDecimal getLoanPenaltyBeforePayment() {
		return loanPenaltyBeforePayment;
	}

	public void setLoanPenaltyBeforePayment(BigDecimal loanPenaltyBeforePayment) {
		this.loanPenaltyBeforePayment = loanPenaltyBeforePayment;
	}

	public BigDecimal getLoanPenaltyAfterPayment() {
		return loanPenaltyAfterPayment;
	}

	public void setLoanPenaltyAfterPayment(BigDecimal loanPenaltyAfterPayment) {
		this.loanPenaltyAfterPayment = loanPenaltyAfterPayment;
	}

	public BigDecimal getRechargeAmount() {
		return rechargeAmount;
	}

	public void setRechargeAmount(BigDecimal rechargeAmount) {
		this.rechargeAmount = rechargeAmount;
	}

	public Timestamp getRechargeTime() {
		return rechargeTime;
	}

	public void setRechargeTime(Timestamp rechargeTime) {
		this.rechargeTime = rechargeTime;
	}

	public String getMsisdn() {
		return msisdn;
	}

	public void setMsisdn(String msisdn) {
		this.msisdn = msisdn;
	}

	public String getVendorId() {
		return vendorId;
	}

	public void setVendorId(String vendorId) {
		this.vendorId = vendorId;
	}

}
