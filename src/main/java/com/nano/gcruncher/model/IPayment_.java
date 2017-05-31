package com.nano.gcruncher.model;

import java.math.BigDecimal;
import java.sql.Timestamp;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

import com.nano.jpa.entity.parent.IEntity_;
import com.nano.jpa.enums.ReturnMode;

@Generated(value="Dali", date="2017-05-10T17:31:59.342+0100")
@StaticMetamodel(IPayment.class)
public class IPayment_ extends IEntity_ {
	public static volatile SingularAttribute<IPayment, String> triggerMsisdn;
	public static volatile SingularAttribute<IPayment, ReturnMode> returnMode;
	public static volatile SingularAttribute<IPayment, BigDecimal> amountPaid;
	public static volatile SingularAttribute<IPayment, BigDecimal> balanceBeforePayment;
	public static volatile SingularAttribute<IPayment, BigDecimal> balanceAfterPayment;
	public static volatile SingularAttribute<IPayment, BigDecimal> amountOwedBeforePayment;
	public static volatile SingularAttribute<IPayment, BigDecimal> amountOwedAfterPayment;
	public static volatile SingularAttribute<IPayment, BigDecimal> loanPenaltyBeforePayment;
	public static volatile SingularAttribute<IPayment, BigDecimal> loanPenaltyAfterPayment;
	public static volatile SingularAttribute<IPayment, Timestamp> rechargeTime;
	public static volatile SingularAttribute<IPayment, BigDecimal> rechargeAmount;
	public static volatile SingularAttribute<IPayment, String> serialNo;
	public static volatile SingularAttribute<IPayment, Timestamp> processedTimestamp;
	public static volatile SingularAttribute<IPayment, Long> brandId;
	public static volatile SingularAttribute<IPayment, Long> subCosId;
	public static volatile SingularAttribute<IPayment, String> msisdn;
	public static volatile SingularAttribute<IPayment, String> vendorId;
}
