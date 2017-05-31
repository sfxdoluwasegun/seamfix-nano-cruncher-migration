package com.nano.gcruncher.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import com.nano.jpa.entity.parent.IEntity;
import com.nano.jpa.enums.BorrowStatus;
import com.nano.jpa.enums.PaymentStatus;

/**
 * Represents amount borrowed by a subscriber.
 *
 * @author Diyan Segs
 */

@Entity
public class IBorrow extends IEntity implements Serializable{
	
	private static final long serialVersionUID = 3608351065714760656L;
	
	@Column(name = "AMT_REQUESTED", nullable = false)
	private BigDecimal amountRequested;
	
	@Column(nullable = false)
	private BigDecimal principal;
	
	@Column(nullable = true)
	private BigDecimal amountApproved;
	
	@Column(nullable = true)
	private BigDecimal charge;
	
	@Column(name = "HOME_AREA_NO", nullable = true)
	private Long homeAreaNumber;
	
	@Column(name = "AMT_OWED_BEFORE", nullable = true)
	private BigDecimal amountOwedBeforeBorrow;
	
	@Column(name = "AMT_OWED_AFTER", nullable = true)
	private BigDecimal amountOwedAfterBorrowed;
	
	@Column(name = "BALANCE_BEFORE", nullable = true)
	private BigDecimal balanceBeforeBorrow;
	
	@Column(name = "BALANCE_AFTER", nullable = true)
	private BigDecimal balanceAfterBorrow;
	
	@Enumerated(EnumType.STRING)
	@Column(name = "STATUS", nullable = false)
	private BorrowStatus status = BorrowStatus.RECEIVED;
	
	@Enumerated(EnumType.STRING)
	@Column(name = "PAYMENT_STATUS", nullable = false)
	private PaymentStatus paymentStatus = PaymentStatus.NONE;
	
	@Column(nullable = false)
	private String referenceNo ;
	
	@Column(nullable = true)
	private Timestamp receivedTimestamp;
	
	@Column(nullable = true)
	private BigDecimal currentPendingBalance ;
	
	@Column(nullable = true)
	private BigDecimal recoveredCharge ;
	
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

	public Long getHomeAreaNumber() {
		return homeAreaNumber;
	}

	public void setHomeAreaNumber(Long homeAreaNumber) {
		this.homeAreaNumber = homeAreaNumber;
	}

	public BigDecimal getAmountOwedBeforeBorrow() {
		return amountOwedBeforeBorrow;
	}

	public void setAmountOwedBeforeBorrow(BigDecimal amountOwedBeforeBorrow) {
		this.amountOwedBeforeBorrow = amountOwedBeforeBorrow;
	}

	public BigDecimal getAmountOwedAfterBorrowed() {
		return amountOwedAfterBorrowed;
	}

	public void setAmountOwedAfterBorrowed(BigDecimal amountOwedAfterBorrowed) {
		this.amountOwedAfterBorrowed = amountOwedAfterBorrowed;
	}

	public BigDecimal getBalanceBeforeBorrow() {
		return balanceBeforeBorrow;
	}

	public void setBalanceBeforeBorrow(BigDecimal balanceBeforeBorrow) {
		this.balanceBeforeBorrow = balanceBeforeBorrow;
	}

	public BigDecimal getBalanceAfterBorrow() {
		return balanceAfterBorrow;
	}

	public void setBalanceAfterBorrow(BigDecimal balanceAfterBorrow) {
		this.balanceAfterBorrow = balanceAfterBorrow;
	}

	public BorrowStatus getStatus() {
		return status;
	}

	public void setStatus(BorrowStatus status) {
		this.status = status;
	}

	public PaymentStatus getPaymentStatus() {
		return paymentStatus;
	}

	public void setPaymentStatus(PaymentStatus paymentStatus) {
		this.paymentStatus = paymentStatus;
	}

	public String getReferenceNo() {
		return referenceNo;
	}

	public void setReferenceNo(String referenceNo) {
		this.referenceNo = referenceNo;
	}

	public BigDecimal getAmountRequested() {
		return amountRequested;
	}

	public void setAmountRequested(BigDecimal amountRequested) {
		this.amountRequested = amountRequested;
	}

	public BigDecimal getPrincipal() {
		return principal;
	}

	public void setPrincipal(BigDecimal principal) {
		this.principal = principal;
	}

	public BigDecimal getAmountApproved() {
		return amountApproved;
	}

	public void setAmountApproved(BigDecimal amountApproved) {
		this.amountApproved = amountApproved;
	}

	public BigDecimal getCurrentPendingBalance() {
		return currentPendingBalance;
	}

	public void setCurrentPendingBalance(BigDecimal currentPendingBalance) {
		this.currentPendingBalance = currentPendingBalance;
	}

	public BigDecimal getRecoveredCharge() {
		return recoveredCharge;
	}

	public void setRecoveredCharge(BigDecimal recoveredCharge) {
		this.recoveredCharge = recoveredCharge;
	}

	public BigDecimal getCharge() {
		return charge;
	}

	public void setCharge(BigDecimal charge) {
		this.charge = charge;
	}

	public Timestamp getReceivedTimestamp() {
		return receivedTimestamp;
	}

	public void setReceivedTimestamp(Timestamp receivedTimestamp) {
		this.receivedTimestamp = receivedTimestamp;
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