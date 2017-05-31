package com.nano.gcruncher.model;

import java.math.BigDecimal;
import java.sql.Timestamp;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

import com.nano.jpa.entity.parent.IEntity_;
import com.nano.jpa.enums.BorrowStatus;
import com.nano.jpa.enums.PaymentStatus;

@Generated(value="Dali", date="2017-05-10T17:32:39.987+0100")
@StaticMetamodel(IBorrow.class)
public class IBorrow_ extends IEntity_ {
	public static volatile SingularAttribute<IBorrow, BigDecimal> amountRequested;
	public static volatile SingularAttribute<IBorrow, BigDecimal> principal;
	public static volatile SingularAttribute<IBorrow, BigDecimal> amountApproved;
	public static volatile SingularAttribute<IBorrow, BigDecimal> charge;
	public static volatile SingularAttribute<IBorrow, Long> homeAreaNumber;
	public static volatile SingularAttribute<IBorrow, BigDecimal> amountOwedBeforeBorrow;
	public static volatile SingularAttribute<IBorrow, BigDecimal> amountOwedAfterBorrowed;
	public static volatile SingularAttribute<IBorrow, BigDecimal> balanceBeforeBorrow;
	public static volatile SingularAttribute<IBorrow, BigDecimal> balanceAfterBorrow;
	public static volatile SingularAttribute<IBorrow, BorrowStatus> status;
	public static volatile SingularAttribute<IBorrow, PaymentStatus> paymentStatus;
	public static volatile SingularAttribute<IBorrow, String> referenceNo;
	public static volatile SingularAttribute<IBorrow, Timestamp> receivedTimestamp;
	public static volatile SingularAttribute<IBorrow, BigDecimal> currentPendingBalance;
	public static volatile SingularAttribute<IBorrow, BigDecimal> recoveredCharge;
	public static volatile SingularAttribute<IBorrow, String> serialNo;
	public static volatile SingularAttribute<IBorrow, Timestamp> processedTimestamp;
	public static volatile SingularAttribute<IBorrow, Long> brandId;
	public static volatile SingularAttribute<IBorrow, Long> subCosId;
	public static volatile SingularAttribute<IBorrow, String> msisdn;
	public static volatile SingularAttribute<IBorrow, String> vendorId;
}
