package com.seamfix.nano.tools;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.ejb.AccessTimeout;
import javax.ejb.Asynchronous;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.jboss.logging.Logger;

import com.nano.jpa.entity.Borrow;
import com.nano.jpa.entity.Borrow_;
import com.nano.jpa.entity.Dealing;
import com.nano.jpa.entity.Dealing_;
import com.nano.jpa.entity.Loan;
import com.nano.jpa.entity.Loan_;
import com.nano.jpa.entity.OtherDealing;
import com.nano.jpa.entity.OtherDealing_;
import com.nano.jpa.entity.Payment;
import com.nano.jpa.entity.Payment_;
import com.nano.jpa.entity.Settlement;
import com.nano.jpa.entity.SettlementTrail;
import com.nano.jpa.entity.Settlement_;
import com.nano.jpa.entity.Subscriber;
import com.nano.jpa.entity.Subscriber_;
import com.nano.jpa.entity.ras.SubscriberAssessment;
import com.nano.jpa.entity.ras.SubscriberAssessment_;
import com.nano.jpa.enums.DealType;
import com.nano.jpa.enums.Merchant;
import com.nano.jpa.enums.OperationType;
import com.nano.jpa.enums.PaymentStatus;
import com.nano.jpa.enums.ReturnMode;
import com.nano.jpa.enums.SettlementType;

@Singleton
@Lock(LockType.READ)
@AccessTimeout(unit = TimeUnit.MINUTES, value = 3)
public class DbManager {
	
	private Logger log = Logger.getLogger(getClass());

	private CriteriaBuilder criteriaBuilder ;

	@PersistenceContext(unitName = "nano-jpa")
	private EntityManager entityManager ;

	@PostConstruct
	public void init(){
		criteriaBuilder = entityManager.getCriteriaBuilder();
	}
	
	/**
	 * Retrieve referenceNo logged at point of Loan request.
	 * if null, generate new referenceNo to be used
	 * 
	 * @param msisdn subscriber unique reference
	 * @param vendorid vendor unique reference
	 * @param timestamp time of event recorded in CDR log
	 * @param operationType type of operation covered by transaction
	 * @return generated referenceNo
	 */
	public String retrieveLoanReferenceByMSISDN(String msisdn, String vendorid, Timestamp timestamp, OperationType operationType) {
		// TODO Auto-generated method stub
		
		if (!operationType.equals(OperationType.LOAN))
			return "";
		
		return getReferenceNoFromLatestLoanRequestByMSISDNAndTimestamp(msisdn, timestamp);
	}
	
	/**
	 * Fetch {@link Loan} by MSISDN and date properties.
	 * 
	 * @param msisdn subscriber unique reference
	 * @param timestamp time of event recorded in CDR log
	 * @return Loan reference number
	 */
	private String getReferenceNoFromLatestLoanRequestByMSISDNAndTimestamp(String msisdn, Timestamp timestamp) {
		// TODO Auto-generated method stub
		
		CriteriaQuery<String> criteriaQuery = criteriaBuilder.createQuery(String.class);
		Root<Loan> root = criteriaQuery.from(Loan.class);
		
		Join<Loan, Subscriber> subscriber = root.join(Loan_.subscriber);
		
		criteriaQuery.select(root.get(Loan_.referenceNo));
		criteriaQuery.where(criteriaBuilder.and(
				criteriaBuilder.equal(subscriber.get(Subscriber_.msisdn), msisdn), 
				criteriaBuilder.lessThanOrEqualTo(root.get(Loan_.date), timestamp)
				));
		criteriaQuery.orderBy(criteriaBuilder.desc(root.get(Loan_.date)));
		
		try {
			return entityManager.createQuery(criteriaQuery).setMaxResults(1).getSingleResult();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.warn("No Loan instance found for MSISDN:" + msisdn + " before or by:" + timestamp);
		}
		
		return null;
	}
	
	/**
	 * Fetch earliest {@link IBorrow} by MSISDN, and vendorId, paymentStatus and processedTimesStamp properties.
	 * 
	 * @param msisdn subscriber unique reference
	 * @param vendorid vendor unique reference
	 * @param timestamp time of event recorded in CDR log
	 * @return IBorrow record
	 */
	@Deprecated
	Borrow getEarliestBorrowByMSISDNAndVendorIdAndPaymentTimestamp(String msisdn, String vendorid,
			Timestamp timestamp) {
		// TODO Auto-generated method stub
		
		CriteriaQuery<Borrow> criteriaQuery = criteriaBuilder.createQuery(Borrow.class);
		Root<Borrow> root = criteriaQuery.from(Borrow.class);
		
		criteriaQuery.select(root);
		criteriaQuery.where(criteriaBuilder.and(
				criteriaBuilder.equal(root.get(Borrow_.msisdn), msisdn), 
				criteriaBuilder.equal(root.get(Borrow_.merchant), Merchant.fromVendorId(vendorid)), 
				criteriaBuilder.notEqual(root.get(Borrow_.paymentStatus), PaymentStatus.COMPLETE), 
				criteriaBuilder.lessThanOrEqualTo(root.get(Borrow_.processedTimestamp), timestamp)
				));
		criteriaQuery.orderBy(criteriaBuilder.asc(root.get(Borrow_.processedTimestamp)));
		
		try {
			return entityManager.createQuery(criteriaQuery).setMaxResults(1).getSingleResult();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.warn("No pending IBorrow instance found for MSISDN:" + msisdn + " and vendorId:" + vendorid + " earlier than:" + timestamp);
		}
		
		return null;
	}
	
	/**
	 * Fetch {@link SubscriberAssessment} by MSISDN.
	 * 
	 * @param msisdn subscriber unique reference
	 * @return SubscriberAssessment record
	 */
	public SubscriberAssessment getSubscriberAssessmentBySubscriber(String msisdn) {
		// TODO Auto-generated method stub

		CriteriaQuery<SubscriberAssessment> criteriaQuery = criteriaBuilder.createQuery(SubscriberAssessment.class);
		Root<SubscriberAssessment> root = criteriaQuery.from(SubscriberAssessment.class);
		
		Join<SubscriberAssessment, Subscriber> subscriber = root.join(SubscriberAssessment_.subscriber);

		criteriaQuery.select(root);
		criteriaQuery.where(criteriaBuilder.equal(subscriber.get(Subscriber_.msisdn), msisdn));

		try {
			return entityManager.createQuery(criteriaQuery).getSingleResult();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.error("No subscriberAssessment instance found for subscriber:" + msisdn);
		}

		return null;
	}
	
	/**
	 * Fetch {@link Borrow} by referenceNo property.
	 * 
	 * @param referenceNo transaction unique reference
	 * @return Borrow record
	 */
	public Borrow getBorrowByReferenceNo(String referenceNo) {
		// TODO Auto-generated method stub
		
		CriteriaQuery<Borrow> criteriaQuery = criteriaBuilder.createQuery(Borrow.class);
		Root<Borrow> root = criteriaQuery.from(Borrow.class);

		criteriaQuery.select(root);
		criteriaQuery.where(criteriaBuilder.equal(root.get(Borrow_.referenceNo), referenceNo));

		try {
			return entityManager.createQuery(criteriaQuery).getSingleResult();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.error("No Borrow was found for referenceNo:" + referenceNo);
		}

		return null;
	}
	
	/**
	 * Fetch {@link Dealing} by MSISDN, operationTime and operationType properties.
	 * 
	 * @param msisdn subscriber unique MSISDN
	 * @param timestamp time stamp of transaction
	 * @param operationType operation type
	 * @return Dealing record
	 */
	public Dealing getDealingByMSISDNAndOperationTimeAndOperationType(String msisdn, Timestamp timestamp,
			OperationType operationType) {
		// TODO Auto-generated method stub

		CriteriaQuery<Dealing> criteriaQuery = criteriaBuilder.createQuery(Dealing.class);
		Root<Dealing> root = criteriaQuery.from(Dealing.class);

		criteriaQuery.select(root);
		criteriaQuery.where(criteriaBuilder.and(
				criteriaBuilder.equal(root.get(Dealing_.msisdn), msisdn),
				criteriaBuilder.equal(root.get(Dealing_.operationTime), timestamp), 
				criteriaBuilder.equal(root.get(Dealing_.operationType), operationType)
				));

		try {
			return entityManager.createQuery(criteriaQuery).getSingleResult();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.warn("No Dealing instance found for msisdn:" + msisdn + " timestamp:" + timestamp + " and operationType" + operationType);
		}

		return null;
	}

	/**
	 * Fetch {@link OtherDealing} by MSISDN, operationTime and operationType properties.
	 * 
	 * @param msisdn subscriber unique MSISDN
	 * @param timestamp time stamp of transaction
	 * @param operationType operation type
	 * @return OtherDealing record
	 */
	public OtherDealing getOtherDealingByMSISDNAndOperationTimeAndOperationType(String msisdn, Timestamp timestamp,
			OperationType operationType) {
		// TODO Auto-generated method stub

		CriteriaQuery<OtherDealing> criteriaQuery = criteriaBuilder.createQuery(OtherDealing.class);
		Root<OtherDealing> root = criteriaQuery.from(OtherDealing.class);

		criteriaQuery.select(root);
		criteriaQuery.where(criteriaBuilder.and(
				criteriaBuilder.equal(root.get(OtherDealing_.msisdn), msisdn),
				criteriaBuilder.equal(root.get(OtherDealing_.timestamp), timestamp), 
				criteriaBuilder.equal(root.get(OtherDealing_.operationType), operationType)
				));

		try {
			return entityManager.createQuery(criteriaQuery).getSingleResult();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.warn("No OtherDealing instance found for msisdn:" + msisdn + " timestamp:" + timestamp + " and operationType" + operationType);
		}

		return null;
	}

	/**
	 * Create new {@link Dealing} instance.
	 * 
	 * @param balanceType transaction balance type
	 * @param changeBalance differential in subscribers balance
	 * @param currentBalance subscribers current balance post transaction
	 * @param entryDate time stamp of file generation
	 * @param etuAmount amount subscriber is penalized
	 * @param etuGraceDate penalty grace period
	 * @param forceRepayDate date at which loan is forcefully obtained from subscriber
	 * @param initialEtuAmount penalized amount before transaction
	 * @param initialLoanAmount penalized amount after transaction
	 * @param initialLoanPoundage service charge due before transaction
	 * @param loanAmount pending loan amount
	 * @param loanBalanceType loan balance type
	 * @param loanPoundage service charge due after transaction
	 * @param loanVendorId vendor to which transaction belongs
	 * @param msisdn subscribe unique MSISDN
	 * @param offering offering
	 * @param operationType operation which triggered transaction
	 * @param repayment amount reimbursed
	 * @param repayPoundage service charged reimbursed
	 * @param subid subscriber identity
	 * @param timestamp time stamp of transaction
	 * @param transid transaction identifier
	 * @param referenceNumber transaction unique reference
	 * @return Dealing record
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public Dealing persistDealing(long balanceType, BigDecimal changeBalance,
			BigDecimal currentBalance, Timestamp entryDate, BigDecimal etuAmount, Timestamp etuGraceDate,
			Timestamp forceRepayDate, BigDecimal initialEtuAmount, BigDecimal initialLoanAmount,
			BigDecimal initialLoanPoundage, BigDecimal loanAmount, long loanBalanceType, BigDecimal loanPoundage,
			String loanVendorId, String msisdn, String offering, OperationType operationType, 
			BigDecimal repayment, BigDecimal repayPoundage, long subid, Timestamp timestamp, long transid, String referenceNumber) {
		// TODO Auto-generated method stub

		DealType dealType = operationType.equals(OperationType.LOAN) ? DealType.CREDIT : DealType.DEBIT ;

		Dealing dealing = new Dealing();
		dealing.setAccountBook(loanBalanceType);
		dealing.setBalanceType(balanceType);
		dealing.setChangeBalance(changeBalance);
		dealing.setCharge(initialLoanPoundage);
		dealing.setChargePayment(repayPoundage);
		dealing.setCurrentBalance(currentBalance);
		dealing.setDealType(dealType);
		dealing.setEtuDate(etuGraceDate);
		dealing.setExpiryDate(forceRepayDate);
		dealing.setGenerationTimestamp(entryDate);
		dealing.setInitEtuAmount(initialEtuAmount);
		dealing.setLoanAmount(initialLoanAmount);
		dealing.setMsisdn(msisdn);
		dealing.setOfferingCode(offering);
		dealing.setOperationTime(timestamp);
		dealing.setOperationType(operationType);
		dealing.setPaymentAmount(repayment);
		dealing.setPendingBalance(loanAmount);
		dealing.setPendingCharge(loanPoundage);
		dealing.setReferenceNo(referenceNumber);
		dealing.setSubscriberId(subid);
		dealing.setTimestamp(Timestamp.valueOf(LocalDateTime.now()));
		dealing.setTransactionId(transid);
		dealing.setVendorid(loanVendorId);

		return (Dealing) create(dealing);
	}

	/**
	 * Create new {@link OtherDealing} instance.
	 * 
	 * @param balanceType transaction balance type
	 * @param changeBalance differential in subscribers balance
	 * @param currentBalance subscribers current balance post transaction
	 * @param entryDate time stamp of file generation
	 * @param etuAmount amount subscriber is penalized
	 * @param etuGraceDate penalty grace period
	 * @param forceRepayDate date at which loan is forcefully obtained from subscriber
	 * @param initialEtuAmount penalized amount before transaction
	 * @param initialLoanAmount penalized amount after transaction
	 * @param initialLoanPoundage service charge due before transaction
	 * @param loanAmount pending loan amount
	 * @param loanBalanceType loan balance type
	 * @param loanPoundage service charge due after transaction
	 * @param loanVendorId vendor to which transaction belongs
	 * @param msisdn subscribe unique MSISDN
	 * @param offering offering
	 * @param operationType operation which triggered transaction
	 * @param repayment amount reimbursed
	 * @param repayPoundage service charged reimbursed
	 * @param subid subscriber identity
	 * @param timestamp time stamp of transaction
	 * @param transid transaction identifier
	 */
	@Asynchronous
	public void persistOtherDealing(long balanceType, BigDecimal changeBalance, BigDecimal currentBalance,
			Timestamp entryDate, BigDecimal etuAmount, Timestamp etuGraceDate, Timestamp forceRepayDate,
			BigDecimal initialEtuAmount, BigDecimal initialLoanAmount, BigDecimal initialLoanPoundage,
			BigDecimal loanAmount, long loanBalanceType, BigDecimal loanPoundage, String loanVendorId, String msisdn,
			String offering, OperationType operationType, BigDecimal repayment,
			BigDecimal repayPoundage, long subid, Timestamp timestamp, long transid) {
		// TODO Auto-generated method stub

		OtherDealing dealing = new OtherDealing();
		dealing.setAccountBook(loanBalanceType);
		dealing.setBalanceType(balanceType);
		dealing.setChangeBalance(changeBalance);
		dealing.setCharge(initialLoanPoundage);
		dealing.setChargePayment(repayPoundage);
		dealing.setCurrentBalance(currentBalance);
		dealing.setEtuDate(etuGraceDate);
		dealing.setExpiryDate(forceRepayDate);
		dealing.setGenerationTimestamp(entryDate);
		dealing.setInitEtuAmount(initialEtuAmount);
		dealing.setLoanAmount(initialLoanAmount);
		dealing.setMsisdn(msisdn);
		dealing.setOfferingCode(offering);
		dealing.setOperationType(operationType);
		dealing.setPaymentAmount(repayment);
		dealing.setPendingBalance(loanAmount);
		dealing.setPendingCharge(loanPoundage);
		dealing.setReferenceNo(transid);
		dealing.setSubscriberId(subid);
		dealing.setTimestamp(Timestamp.valueOf(LocalDateTime.now()));
		dealing.setVendorid(loanVendorId);

		create(dealing);
	}
	
	/**
	 * Fetch {@link Borrow} by subscriber, principal and timestamp properties.
	 *
	 * @param subscriber subscriber detail
	 * @param principal principal amount given as loan
	 * @param receivedTimestamp time-stamp subscriber received loan value
	 * @return Borrow record
	 */
	public Borrow getBorrowBySubscriberAndPrincipalAndTimeStamp(Subscriber subscriber, 
			BigDecimal principal, Timestamp receivedTimestamp){

		CriteriaQuery<Borrow> criteriaQuery = criteriaBuilder.createQuery(Borrow.class);
		Root<Borrow> root = criteriaQuery.from(Borrow.class);

		Predicate predicate = criteriaBuilder.and(
				criteriaBuilder.equal(root.get(Borrow_.principal), principal), 
				criteriaBuilder.equal(root.get(Borrow_.receivedTimestamp), receivedTimestamp), 
				criteriaBuilder.equal(root.get(Borrow_.msisdn), subscriber.getMsisdn())
				);

		criteriaQuery.select(root);
		criteriaQuery.where(predicate);

		try {
			return entityManager.createQuery(criteriaQuery).getSingleResult();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.error("No Borrow was found for subscriber:" + subscriber.getMsisdn() + " with principal:" + principal + " and receivedTimestamp:" + receivedTimestamp);
		}

		return null;
	}
	
	/**
	 * Fetch {@link Borrow} by subscriber, principal and timestamp properties.
	 *
	 * @param msisdn subscriber unique MSISDN
	 * @param principal principal amount given as loan
	 * @param receivedTimestamp time-stamp subscriber received loan value
	 * @return Borrow record
	 */
	public Borrow getBorrowBySubscriberAndPrincipalAndTimeStamp(String msisdn, 
			BigDecimal principal, Timestamp receivedTimestamp){

		CriteriaQuery<Borrow> criteriaQuery = criteriaBuilder.createQuery(Borrow.class);
		Root<Borrow> root = criteriaQuery.from(Borrow.class);

		Predicate predicate = criteriaBuilder.and(
				criteriaBuilder.equal(root.get(Borrow_.principal), principal), 
				criteriaBuilder.equal(root.get(Borrow_.receivedTimestamp), receivedTimestamp), 
				criteriaBuilder.equal(root.get(Borrow_.msisdn), msisdn)
				);

		criteriaQuery.select(root);
		criteriaQuery.where(predicate);

		try {
			return entityManager.createQuery(criteriaQuery).getSingleResult();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.error("No Borrow was found for subscriber:" + msisdn + " with principal:" + principal + " and receivedTimestamp:" + receivedTimestamp);
		}

		return null;
	}
	
	/**
	 * Fetch {@link Borrow} by subscriber, principal and timestamp properties.
	 *
	 * @param msisdn subscriber unique MSISDN
	 * @param principal principal amount given as loan
	 * @param receivedTimestamp time-stamp subscriber received loan value
	 * @param merchant vendor to which transaction belongs
	 * @return Borrow record
	 */
	public Borrow getBorrowBySubscriberAndPrincipalAndTimeStamp(String msisdn, 
			BigDecimal principal, Timestamp receivedTimestamp, Merchant merchant){

		CriteriaQuery<Borrow> criteriaQuery = criteriaBuilder.createQuery(Borrow.class);
		Root<Borrow> root = criteriaQuery.from(Borrow.class);

		Predicate predicate = criteriaBuilder.and(
				criteriaBuilder.equal(root.get(Borrow_.principal), principal), 
				criteriaBuilder.equal(root.get(Borrow_.receivedTimestamp), receivedTimestamp), 
				criteriaBuilder.equal(root.get(Borrow_.msisdn), msisdn), 
				criteriaBuilder.equal(root.get(Borrow_.merchant), merchant)
				);

		criteriaQuery.select(root);
		criteriaQuery.where(predicate);

		try {
			return entityManager.createQuery(criteriaQuery).getSingleResult();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.error("No Borrow was found for subscriber:" + msisdn + " with principal:" + principal + " and receivedTimestamp:" + receivedTimestamp + " and merchant:" + merchant);
		}

		return null;
	}
	
	/**
	 * Update {@link Borrow} with payment record using <code>MapMessage</code> for optimization.
	 * Persist {@link Payment} record.
	 * 
	 * @param borrow loan transaction detail
	 * @param paymentStatus reimbursement status
	 * @param returnAmount amount received as reimbursement
	 * @param servicePercentage amount charged for service
	 * @param returnMode transaction medium
	 * @param amountOwedBeforePayment pending balance before transaction
	 * @param amountOwedAfterPayment pending balance after transaction
	 * @param balanceAfterPayment subscriber account after transaction
	 * @param loanPenaltyAfterPayment penalty calculated after transaction
	 * @param loanPenaltyBeforePayment penalty calculated before transaction
	 * @param brandid transaction brand identification
	 * @param subcosid transaction subCos identification
	 * @param balanceBeforePayment subscriber account before transaction
	 * @param timestamp transaction time-stamp
	 * @param serialno transaction serial number
	 * @param triggermsisdn MSISDN responsible for transaction trigger
	 */
	@Lock(LockType.WRITE)
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void updateBorrowDataWithPaymentRecord(Borrow borrow, 
			PaymentStatus paymentStatus, 
			BigDecimal returnAmount, double servicePercentage, ReturnMode returnMode, 
			BigDecimal amountOwedBeforePayment, BigDecimal amountOwedAfterPayment, BigDecimal balanceAfterPayment, BigDecimal loanPenaltyAfterPayment, 
			BigDecimal loanPenaltyBeforePayment, Long brandid, Long subcosid, BigDecimal balanceBeforePayment, 
			long timestamp, 
			String serialno, String triggermsisdn){

		BigDecimal currentPendingBalance = borrow.getCurrentPendingBalance().subtract(returnAmount);

		if (currentPendingBalance.compareTo(BigDecimal.ZERO) < 0)
			currentPendingBalance = BigDecimal.ZERO;

		if (returnAmount.compareTo(borrow.getCurrentPendingBalance()) > 0)
			returnAmount = currentPendingBalance ;

		BigDecimal totalamountpaid = borrow.getAmountApproved().subtract(currentPendingBalance);
		BigDecimal recoveredCharge = BigDecimal.valueOf((servicePercentage/100D)).multiply(totalamountpaid) ;

		borrow.setCurrentPendingBalance(currentPendingBalance);
		borrow.setPaymentStatus(paymentStatus);
		borrow.setRecoveredCharge(recoveredCharge);

		Payment payment = initializePaymentForLoan(borrow, returnAmount, returnMode, Merchant.NANO, 
				amountOwedBeforePayment, amountOwedAfterPayment, balanceAfterPayment, loanPenaltyAfterPayment, loanPenaltyBeforePayment, 
				Integer.parseInt(brandid.toString()), Integer.parseInt(subcosid.toString()), balanceBeforePayment, timestamp, serialno, triggermsisdn);

		BigDecimal bulk = BigDecimal.valueOf((servicePercentage/100D)).multiply(returnAmount);
		createSettlementTrail(bulk, payment, SettlementType.INTEREST);

		update(borrow);
	}
	
	/**
	 * Create {@link SettlementTrail} for {@link Payment} interest.
	 * 
	 * @param bulk total transaction sum
	 * @param payment payment detail
	 * @param settlementType enumeration flag for determining settlement category
	 */
	public void createSettlementTrail(BigDecimal bulk, 
			Payment payment, SettlementType settlementType){

		List<Settlement> settlements = getSettlementBySettlementType(settlementType);
		if (settlements == null)
			return;

		for (Settlement settlement : settlements){
			BigDecimal amount = BigDecimal.valueOf((settlement.getPercentage()/100D)).multiply(bulk);

			SettlementTrail settlementTrail = new SettlementTrail();
			settlementTrail.setAccountNumber(settlement.getAccountNumber());
			settlementTrail.setAmount(amount);
			settlementTrail.setBank(settlement.getBankData().getName());
			settlementTrail.setCreditor(settlement.getCreditor());
			settlementTrail.setPayment(payment);
			settlementTrail.setSettlementType(settlementType);
			settlementTrail.setTimestamp(Timestamp.valueOf(LocalDateTime.now()));

			create(settlementTrail);
		}
	}
	
	/**
	 * Fetch {@link Settlement} by {@link SettlementType} property.
	 *
	 * @param settlementType enumeration declaring settlement category
	 * @return Settlement record
	 */
	public List<Settlement> getSettlementBySettlementType(SettlementType settlementType){

		CriteriaQuery<Settlement> criteriaQuery = criteriaBuilder.createQuery(Settlement.class);
		Root<Settlement> root = criteriaQuery.from(Settlement.class);

		criteriaQuery.select(root);
		criteriaQuery.where(criteriaBuilder.and(
				criteriaBuilder.equal(root.get(Settlement_.deactivated), false), 
				criteriaBuilder.equal(root.get(Settlement_.settlementType), settlementType)
				));

		try {
			return entityManager.createQuery(criteriaQuery).getResultList();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.error("No Settlement instance found for settlementType:" + settlementType.name());
		}

		return null;
	}
	
	/**
	 * Creates {@link Payment} info for a loan using <code>MapMessage</code> for optimization.
	 * 
	 * @param borrow - loan transaction details
	 * @param returnAmount - amount recovered from CDR transaction
	 * @param returnMode - transaction medium
	 * @param merchant - merchant details
	 * @param amountOwedBeforePayment - pending balance before transaction
	 * @param amountOwedAfterPayment - pending balance after transaction
	 * @param balanceAfterPayment - subscriber account after transaction
	 * @param loanPenaltyAfterPayment - penalty calculated before transaction
	 * @param loanPenaltyBeforePayment - penalty calculated after transaction
	 * @param brandid - transaction brand identification
	 * @param subcosid - transaction subCos identification
	 * @param balanceBeforePayment - subscriber account before transaction
	 * @param timestamp - transaction time-stamp
	 * @param serialno - transaction serial number
	 * @param triggermsisdn - MSISDN that triggered transaction
	 * @return Payment record
	 */
	public Payment initializePaymentForLoan(Borrow borrow, 
			BigDecimal returnAmount, ReturnMode returnMode, Merchant merchant, 
			BigDecimal amountOwedBeforePayment, BigDecimal amountOwedAfterPayment, BigDecimal balanceAfterPayment, BigDecimal loanPenaltyAfterPayment, BigDecimal loanPenaltyBeforePayment, 
			int brandid, int subcosid, BigDecimal balanceBeforePayment, 
			long timestamp, 
			String serialno, String triggermsisdn){

		Payment	payment = new Payment();
		payment.setAmountOwedAfterPayment(amountOwedAfterPayment);
		payment.setAmountOwedBeforePayment(amountOwedBeforePayment);
		payment.setAmountPaid(returnAmount);
		payment.setBalanceAfterPayment(balanceAfterPayment);
		payment.setBalanceBeforePayment(balanceBeforePayment);
		payment.setLoanPenaltyAfterPayment(loanPenaltyAfterPayment);
		payment.setLoanPenaltyBeforePayment(loanPenaltyBeforePayment);
		payment.setMerchant(merchant);
		payment.setProcessedTimestamp(new Timestamp(Calendar.getInstance().getTime().getTime()));
		payment.setRechargeAmount((balanceAfterPayment.add(returnAmount)).subtract(balanceBeforePayment));
		payment.setRechargeTime(new Timestamp(timestamp));
		payment.setReturnMode(returnMode);
		payment.setSerialNo(serialno);
		payment.setSubCosId(Long.valueOf(subcosid));

		return payment;
	}
	
	/**
	 * Fetch {@link Payment} by subscriber, amount and timestamp properties.
	 *
	 * @param subscriber subscriber detail
	 * @param amountPaid reimbursement amount
	 * @param timestamp transaction time-stamp
	 * @param merchant merchant for transaction
	 * @return Payment record
	 */
	@Lock(LockType.WRITE)
	public Payment getPaymentBySubscriberAndAmountAndTimestamp(Subscriber subscriber, 
			BigDecimal amountPaid, Timestamp timestamp, 
			Merchant merchant){

		CriteriaQuery<Payment> criteriaQuery = criteriaBuilder.createQuery(Payment.class);
		Root<Payment> root = criteriaQuery.from(Payment.class);

		Predicate predicate = criteriaBuilder.and(
				criteriaBuilder.equal(root.get(Payment_.rechargeTime), timestamp), 
				criteriaBuilder.equal(root.get(Payment_.amountPaid), amountPaid), 
				criteriaBuilder.equal(root.get(Payment_.msisdn), subscriber.getMsisdn())
				);

		criteriaQuery.select(root);
		if (merchant != null)
			criteriaQuery.where(predicate, criteriaBuilder.equal(root.get(Payment_.merchant), merchant));
		else
			criteriaQuery.where(predicate);

		try {
			return entityManager.createQuery(criteriaQuery).getSingleResult();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.error("No payment was found for subscriber:" + subscriber.getMsisdn() + " with rechargeTime:" + timestamp + " and amount:" + amountPaid);
		}

		return null;
	}
	
	/**
	 * Fetch {@link Payment} by subscriber, amount and timestamp properties.
	 *
	 * @param subscriber subscriber detail
	 * @param amountPaid reimbursement amount
	 * @param timestamp transaction time-stamp
	 * @param merchant merchant for transaction
	 * @return Payment record
	 */
	@Lock(LockType.WRITE)
	public Payment getPaymentBySubscriberAndAmountAndTimestamp(String msisdn, 
			BigDecimal amountPaid, Timestamp timestamp, 
			Merchant merchant){

		CriteriaQuery<Payment> criteriaQuery = criteriaBuilder.createQuery(Payment.class);
		Root<Payment> root = criteriaQuery.from(Payment.class);

		Predicate predicate = criteriaBuilder.and(
				criteriaBuilder.equal(root.get(Payment_.rechargeTime), timestamp), 
				criteriaBuilder.equal(root.get(Payment_.amountPaid), amountPaid), 
				criteriaBuilder.equal(root.get(Payment_.msisdn), msisdn)
				);

		criteriaQuery.select(root);
		if (merchant != null)
			criteriaQuery.where(predicate, criteriaBuilder.equal(root.get(Payment_.merchant), merchant));
		else
			criteriaQuery.where(predicate);

		try {
			return entityManager.createQuery(criteriaQuery).getSingleResult();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.error("No payment was found for subscriber:" + msisdn + " with rechargeTime:" + timestamp + " and amount:" + amountPaid);
		}

		return null;
	}
	
	/**
	 * Fetch {@link Subscriber} by MSISDN property.
	 * 
	 * @param msisdn subscriber MSISDN
	 * @return Subscriber record
	 */
	@Lock(LockType.WRITE)
	public Subscriber getSubscriberByMsisdn(String msisdn){

		CriteriaQuery<Subscriber> criteriaQuery = criteriaBuilder.createQuery(Subscriber.class);
		Root<Subscriber> root = criteriaQuery.from(Subscriber.class);

		criteriaQuery.select(root);
		criteriaQuery.where(criteriaBuilder.equal(root.get(Subscriber_.msisdn), formatMisisdn(msisdn)));

		try {
			return entityManager.createQuery(criteriaQuery).getSingleResult();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.warn("No subscriber instance was found with msisdn:" + msisdn);;
		}

		return null;
	}
	
	/**
	 * Creates or fetches a unique {@link Subscriber} record.
	 *
	 * @param msisdn subscriber MSISDN
	 * @return Subscriber record
	 */
	@Lock(LockType.WRITE)
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public Subscriber createSubscriber(String msisdn){

		Subscriber subscriber = getSubscriberByMsisdn(formatMisisdn(msisdn));

		if (subscriber != null)
			return subscriber;

		subscriber = new Subscriber();
		subscriber.setInDebt(false);
		subscriber.setAutoRecharge(false);
		subscriber.setMsisdn(formatMisisdn(msisdn));

		return (Subscriber) create(subscriber);
	}
	
	/**
	 * Clear subscriber debt.
	 *
	 * @param subscriber subscriber detail
	 */
	public void clearSubscriberDebt(Subscriber subscriber){
		subscriber.setInDebt(false);
		update(subscriber);
	}
	
	/**
	 * Change MSISDN to uniform database format.
	 *
	 * @param msisdn subscriber unique reference
	 * @return formatted MSISDN
	 */
	public String formatMisisdn(String msisdn){

		if (msisdn.startsWith("234"))
			msisdn = "0" + msisdn.substring(3, msisdn.length());

		if (msisdn.startsWith("+234"))
			msisdn = "0" + msisdn.substring(4, msisdn.length());

		if (!msisdn.startsWith("0"))
			msisdn = "0" + msisdn;

		return msisdn;
	}
	
	/**
	 * Persist entity and add entity instance to {@link EntityManager}.
	 * 
	 * @param <T> - inherent java type
	 * @param entity - entity instance for this operation
	 * @return persisted entity instance
	 */
	public <T> Object create(T entity){

		entityManager.persist(entity);

		try {
			return entity;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.error("", e);
		}
		return null;
	}

	/**
	 * Merge the state of the given entity into the current {@link PersistenceContext}.
	 * 
	 * @param <T> - inherent java type
	 * @param entity - entity instance for this operation
	 * @return the managed instance that the state was merged to
	 */
	public <T> Object update(T entity){

		entityManager.merge(entity);
		try {
			return entity;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.error("", e);
		}

		return null;
	}

}