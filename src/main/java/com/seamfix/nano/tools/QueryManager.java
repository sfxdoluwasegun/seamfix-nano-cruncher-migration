package com.seamfix.nano.tools;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.ejb.AccessTimeout;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.jboss.logging.Logger;

import com.nano.gcruncher.model.IBorrow;
import com.nano.gcruncher.model.IBorrow_;
import com.nano.gcruncher.model.IPayment;
import com.nano.gcruncher.model.IPayment_;
import com.nano.jpa.entity.Borrow;
import com.nano.jpa.entity.Nano;
import com.nano.jpa.entity.Nano_;
import com.nano.jpa.entity.Payment;
import com.nano.jpa.entity.Settings;
import com.nano.jpa.entity.Settings_;
import com.nano.jpa.entity.Settlement;
import com.nano.jpa.entity.SettlementTrail;
import com.nano.jpa.entity.Settlement_;
import com.nano.jpa.entity.Subscriber;
import com.nano.jpa.entity.Subscriber_;
import com.nano.jpa.entity.ras.BorrowableAmount;
import com.nano.jpa.entity.ras.BorrowableAmount_;
import com.nano.jpa.entity.ras.SubscriberAssessment;
import com.nano.jpa.entity.ras.SubscriberAssessment_;
import com.nano.jpa.enums.MerchantData;
import com.nano.jpa.enums.OperationType;
import com.nano.jpa.enums.PayType;
import com.nano.jpa.enums.PaymentStatus;
import com.nano.jpa.enums.ReturnMode;
import com.nano.jpa.enums.SettingType;
import com.nano.jpa.enums.SettlementType;
import com.seamfix.nano.jbeans.ApplicationBean;

@Singleton
@Lock(LockType.READ)
@AccessTimeout(unit = TimeUnit.MINUTES, value = 3)
public class QueryManager {

	private Logger log = Logger.getLogger(getClass());

	private CriteriaBuilder criteriaBuilder ;

	@PersistenceContext(unitName = "nano-ext")
	private EntityManager entityManager ;

	@Inject
	private ApplicationBean appBean;

	@PostConstruct
	public void init(){
		criteriaBuilder = entityManager.getCriteriaBuilder();
	}
	
	/**
	 * Fetch Borrow by referenceNo property.
	 * 
	 * @param referenceNo
	 * @return {@link Borrow}
	 */
	public IBorrow getBorrowByReferenceNo(String referenceNo) {
		// TODO Auto-generated method stub
		
		CriteriaQuery<IBorrow> criteriaQuery = criteriaBuilder.createQuery(IBorrow.class);
		Root<IBorrow> root = criteriaQuery.from(IBorrow.class);

		criteriaQuery.select(root);
		criteriaQuery.where(criteriaBuilder.equal(root.get(IBorrow_.referenceNo), referenceNo));

		try {
			return entityManager.createQuery(criteriaQuery).getSingleResult();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.error("No Borrow was found for referenceNo:" + referenceNo);
		}

		return null;
	}

	/**
	 * Creates or fetches a unique {@link Subscriber} record.
	 *
	 * @param msisdn - subscriber MSISDN
	 * @return {@link Subscriber}
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
	 * Fetch BorrowableAmount by amount property.
	 * 
	 * @param amount - standard amount which can be borrowed
	 * @return {@link BorrowableAmount}
	 */
	public BorrowableAmount getBorrowableAmountByAmount(int amount){

		CriteriaQuery<BorrowableAmount> criteriaQuery = criteriaBuilder.createQuery(BorrowableAmount.class);
		Root<BorrowableAmount> root = criteriaQuery.from(BorrowableAmount.class);

		criteriaQuery.select(root);
		criteriaQuery.where(criteriaBuilder.equal(root.get(BorrowableAmount_.amount), amount));

		try {
			return entityManager.createQuery(criteriaQuery).getSingleResult();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.error("No BorrowableAmount instance found amount:" + amount);
		}

		return null;
	}

	/**
	 * Update {@link Borrow} with payment record using <code>MapMessage</code> for optimization.
	 * Persist {@link Payment} record.
	 * 
	 * @param borrow - loan transaction detail
	 * @param paymentStatus - reimbursement status
	 * @param amountPaid - amount received as reimbursement
	 * @param servicePercentage - amount charged for service
	 */
	public void updateBorrowDataWithPaymentRecord(Borrow borrow, 
			PaymentStatus paymentStatus, 
			BigDecimal amountPaid, double servicePercentage){

		BigDecimal currentPendingBalance = borrow.getCurrentPendingBalance().subtract(amountPaid);
		if (currentPendingBalance.compareTo(BigDecimal.ZERO) < 0)
			currentPendingBalance = BigDecimal.ZERO;

		BigDecimal totalamountpaid = borrow.getAmountApproved().subtract(currentPendingBalance);
		BigDecimal recoveredCharge = BigDecimal.valueOf((servicePercentage/100D)).multiply(totalamountpaid) ;

		borrow.setCurrentPendingBalance(currentPendingBalance);
		borrow.setPaymentStatus(paymentStatus);
		borrow.setRecoveredCharge(recoveredCharge);

		update(borrow);
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
	 * Persist entity and add entity instance to {@link EntityManager}.
	 * 
	 * @param <T> inherent java type
	 * @param entity entity instance for operation
	 * @return persisted entity instance
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public <T> Object createInNewTransaction(T entity){

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
	 * Fetch NANO by name property.
	 * 
	 * @param name name value
	 * @return {@link Nano}
	 */
	public Nano getNanoByName(String name){

		CriteriaQuery<Nano> criteriaQuery = criteriaBuilder.createQuery(Nano.class);
		Root<Nano> root = criteriaQuery.from(Nano.class);

		criteriaQuery.select(root);
		criteriaQuery.where(criteriaBuilder.equal(root.get(Nano_.name), name));

		try {
			return entityManager.createQuery(criteriaQuery).getSingleResult();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.error("No Nano instance was found with name:" + name);
		}

		return createDefaultNano(name);
	}

	/**
	 * Creates the default {@link Nano} instance.
	 *
	 * @param name name value
	 * @return {@link Nano}
	 */
	private Nano createDefaultNano(String name){

		Nano nano = new Nano();
		nano.setBalance(new BigDecimal(25000000));
		nano.setDemoEncryption("Nanocredit");
		nano.setDemoSourceId("00112234");
		nano.setDemoUsername("Nanocredit");
		nano.setLastUpdated(new Timestamp(Calendar.getInstance().getTime().getTime()));
		nano.setLiveEncryption("Seamfix");
		nano.setLiveSourceId("1122345");
		nano.setLiveUsername("Seamfix");
		nano.setName(name);
		nano.setServiceId(MerchantData.NANO.getServiceid());
		nano.setSmppShortCode(MerchantData.NANO.getShortCode());
		nano.setVendorId(MerchantData.NANO.getVendorid());
		nano.setUri("http://nanoairtime.com");
		nano.setPoolCap(BigDecimal.valueOf(25000000));

		return (Nano) create(nano);
	}

	/**
	 * Fetch Settings by name property.
	 * 
	 * @param name name value
	 * @return {@link Settings}
	 */
	public Settings getSettingsByName(String name){

		CriteriaQuery<Settings> criteriaQuery = criteriaBuilder.createQuery(Settings.class);
		Root<Settings> root = criteriaQuery.from(Settings.class);

		criteriaQuery.select(root);
		criteriaQuery.where(criteriaBuilder.equal(root.get(Settings_.name), name));

		try {
			return entityManager.createQuery(criteriaQuery).getSingleResult();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.error("No Setting instance found with name:" + name);
		}

		return null;
	}

	/**
	 * Create a new Setting instance or return existing.
	 * 
	 * @param name setting name
	 * @param value setting value
	 * @param description setting description
	 * @param settingType enumeration declaring setting category
	 * @return {@link Settings}
	 */
	public Settings createSettings(String name, 
			String value, String description, SettingType settingType){

		Settings settings = getSettingsByName(name);

		if (settings != null)
			return settings;

		settings = new Settings();
		settings.setDescription(description);
		settings.setName(name);
		settings.setType(settingType);
		settings.setValue(value);

		return (Settings) create(settings);
	}

	/**
	 * Fetch {@link Settlement} by {@link SettlementType} property.
	 *
	 * @param settlementType enumeration declaring settlement category
	 * @return {@link Settlement}
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
	 * Fetch {@link Subscriber} by MSISDN property.
	 * 
	 * @param msisdn subscriber MSISDN
	 * @return {@link Subscriber}
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
	 * Persist temporary borrow table to test CDR-5 crunching implementation.
	 * 
	 * @param borrowvalues borrow values after loan fulfillment
	 * @param bfborrowvalues borrow values before loan fulfillment
	 * @param accountleft subscriber balance after loan fulfillment
	 * @param bfaccountleft subscriber balance before loan fulfillment
	 * @param charge service charge on loan request
	 * @param brandid brand identifier
	 * @param homeareanumber subscriber home-area number
	 * @param msisdn subscriber unique reference
	 * @param timestamp time of event recorded in CDR log
	 * @param referenceNo unique transaction reference
	 * @param serialno transaction serial number
	 * @param subcosid subCos identifier
	 * @param vendorid vendor unique reference
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void persistBorrowInstance(BigDecimal borrowvalues, BigDecimal bfborrowvalues, BigDecimal accountleft, BigDecimal bfaccountleft, BigDecimal charge, 
			Long brandid, Long homeareanumber, String msisdn, long timestamp, String referenceNo, String serialno, Long subcosid, String vendorid) {
		// TODO Auto-generated method stub

		BigDecimal ammountapproved = bfborrowvalues.add(charge);

		IBorrow borrow = new IBorrow();
		borrow.setAmountApproved(ammountapproved);
		borrow.setAmountOwedAfterBorrowed(ammountapproved);
		borrow.setAmountOwedBeforeBorrow(BigDecimal.ZERO);
		borrow.setAmountRequested(ammountapproved);
		borrow.setBalanceAfterBorrow(bfaccountleft);
		borrow.setBalanceBeforeBorrow(accountleft);
		borrow.setBrandId(brandid);
		borrow.setCharge(charge);
		borrow.setCurrentPendingBalance(ammountapproved);
		borrow.setHomeAreaNumber(homeareanumber);
		borrow.setMsisdn(msisdn);
		borrow.setPaymentStatus(PaymentStatus.NONE);
		borrow.setPrincipal(bfaccountleft.subtract(accountleft));
		borrow.setProcessedTimestamp(Timestamp.valueOf(LocalDateTime.now()));
		borrow.setReceivedTimestamp(new Timestamp(timestamp));
		borrow.setRecoveredCharge(BigDecimal.ZERO);
		borrow.setReferenceNo(referenceNo);
		borrow.setSerialNo(serialno);
		borrow.setSubCosId(subcosid);
		borrow.setVendorId(vendorid);

		create(borrow);
	}

	/**
	 * Persist temporary payment table to test CDR-5 crunching implementation.
	 * 
	 * @param returnMode transaction medium
	 * @param amountOwedBeforePayment amount owed by subscriber before payment 
	 * @param amountOwedAfterPayment amount still owed by subscriber after payment
	 * @param balanceAfterPayment subscriber account balance after payment
	 * @param balanceBeforePayment subscriber account balance before payment
	 * @param loanPenaltyAfterPayment penalty incurred on loan after payment
	 * @param loanPenaltyBeforePayment penalty incurred on loan before payment
	 * @param brandid brand identifier
	 * @param subcosid subCos identifier
	 * @param timestamp time of event recorded in CDR log
	 * @param serialno transaction serial number
	 * @param msisdn subscriber unique reference
	 * @param returnamount amount returned for loan recovery
	 * @param triggermsisdn MSISDN responsible for triggering transaction
	 * @param vendorId vendor unique reference
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void persistPaymentRecord(ReturnMode returnMode, BigDecimal amountOwedBeforePayment,
			BigDecimal amountOwedAfterPayment, BigDecimal balanceAfterPayment, BigDecimal balanceBeforePayment, BigDecimal loanPenaltyAfterPayment, BigDecimal loanPenaltyBeforePayment, long brandid, long subcosid,
			long timestamp, String serialno, String msisdn, BigDecimal returnamount, String triggermsisdn, String vendorId) {
		// TODO Auto-generated method stub

		IPayment payment = new IPayment();
		payment.setAmountOwedAfterPayment(amountOwedAfterPayment);
		payment.setAmountOwedBeforePayment(amountOwedBeforePayment);
		payment.setAmountPaid(balanceBeforePayment.subtract(balanceAfterPayment));
		payment.setBalanceAfterPayment(balanceAfterPayment);
		payment.setBalanceBeforePayment(balanceBeforePayment);
		payment.setBrandId(brandid);
		payment.setLoanPenaltyAfterPayment(loanPenaltyAfterPayment);
		payment.setLoanPenaltyBeforePayment(loanPenaltyBeforePayment);
		payment.setMsisdn(formatMisisdn(msisdn));
		payment.setProcessedTimestamp(Timestamp.valueOf(LocalDateTime.now()));
		payment.setRechargeAmount(returnamount);
		payment.setRechargeTime(new Timestamp(timestamp));
		payment.setReturnMode(returnMode);
		payment.setSerialNo(serialno);
		payment.setSubCosId(subcosid);
		payment.setTriggerMsisdn(triggermsisdn);
		payment.setVendorId(vendorId);

		create(payment);
	}

	/**
	 * Fetch {@link IBorrow} by subscriber, principal and time-stamp properties.
	 *
	 * @param msisdn subscriber MSISDN
	 * @param principal principal amount given as loan
	 * @param receivedTimestamp time-stamp subscriber received loan value
	 * @return {@link (IBorrow}
	 */
	public IBorrow getBorrowBySubscriberAndPrincipalAndTimeStamp(String msisdn, 
			BigDecimal principal, Timestamp receivedTimestamp){

		CriteriaQuery<IBorrow> criteriaQuery = criteriaBuilder.createQuery(IBorrow.class);
		Root<IBorrow> root = criteriaQuery.from(IBorrow.class);

		Predicate predicate = criteriaBuilder.and(
				criteriaBuilder.equal(root.get(IBorrow_.principal), principal), 
				criteriaBuilder.equal(root.get(IBorrow_.receivedTimestamp), receivedTimestamp), 
				criteriaBuilder.equal(root.get(IBorrow_.msisdn), msisdn)
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
	 * Fetch {@link Payment} by subscriber, amount and time-stamp properties.
	 *
	 * @param msisdn subscriber MSISDN
	 * @param timestamp transaction time-stamp
	 * @return {@link Payment}
	 */
	@Lock(LockType.WRITE)
	public IPayment getPaymentByBorrowAndTimestamp(String msisdn, 
			Timestamp timestamp){

		CriteriaQuery<IPayment> criteriaQuery = criteriaBuilder.createQuery(IPayment.class);
		Root<IPayment> root = criteriaQuery.from(IPayment.class);

		criteriaQuery.select(root);
		criteriaQuery.where(criteriaBuilder.and(
				criteriaBuilder.equal(root.get(IPayment_.rechargeTime), timestamp), 
				criteriaBuilder.equal(root.get(IPayment_.msisdn), msisdn)
				));

		try {
			return entityManager.createQuery(criteriaQuery).getSingleResult();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.error("No payment was found for msisdn:" + msisdn + " with rechargeTime:" + timestamp);
		}

		return null;
	}

	/**
	 * Persist entity and add entity instance to {@link EntityManager}.
	 * 
	 * @param <T> inherent java type
	 * @param entity entity instance for this operation
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
	 * @param <T> inherent java type
	 * @param entity entity instance for this operation
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
	 * Fetch SubscriberAssessment by {@link Subscriber}.
	 * 
	 * @param subscriber details of subscriber
	 * @return {@link SubscriberAssessment}
	 */
	public SubscriberAssessment getSubscriberAssessmentBySubscriber(Subscriber subscriber) {
		// TODO Auto-generated method stub

		CriteriaQuery<SubscriberAssessment> criteriaQuery = criteriaBuilder.createQuery(SubscriberAssessment.class);
		Root<SubscriberAssessment> root = criteriaQuery.from(SubscriberAssessment.class);

		criteriaQuery.select(root);
		criteriaQuery.where(criteriaBuilder.equal(root.get(SubscriberAssessment_.subscriber), subscriber));

		try {
			return entityManager.createQuery(criteriaQuery).getSingleResult();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.error("No subscriberAssessment instance found for subscriber:" + subscriber.getPk());
		}

		return null;
	}
	
	/**
	 * Fetch SubscriberAssessment by MSISDN.
	 * 
	 * @param msisdn subscriber unique reference
	 * @return {@link SubscriberAssessment}
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
	 * Create a fresh SubscriberAssessment.
	 * 
	 * @param subscriber details of subscriber
	 * @param subscriberState details of current state of subscriber account
	 * @return {@link SubscriberAssessment}
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public SubscriberAssessment createNewAssessment(Subscriber subscriber){

		SubscriberAssessment subscriberAssessment = getSubscriberAssessmentBySubscriber(subscriber);
		if (subscriberAssessment != null)
			return subscriberAssessment;

		subscriberAssessment = new SubscriberAssessment();
		subscriberAssessment.setAgeOnNetwork(0);
		subscriberAssessment.setInDebt(subscriber.isInDebt());
		subscriberAssessment.setLastProcessed(Timestamp.valueOf(LocalDateTime.now()));
		subscriberAssessment.setNumberOfTopUps(0);
		subscriberAssessment.setSubscriber(subscriber);
		subscriberAssessment.setTopUpDuration(0);
		subscriberAssessment.setTopUpValueDuration(0);
		subscriberAssessment.setTotalTopUpValue(0);
		subscriberAssessment.setTariffPlan(PayType.PREPAID);

		return (SubscriberAssessment) create(subscriberAssessment);
	}
	
	/**
	 * Fetch earliest {@link IBorrow} by MSISDN, and vendorId, paymentStatus and processedTimesStamp properties.
	 * 
	 * @param msisdn subscriber unique reference
	 * @param vendorid vendor unique reference
	 * @param timestamp time of event recorded in CDR log
	 * @return {@link IBorrow}
	 */
	private IBorrow getEarliestBorrowByMSISDNAndVendorIdAndPaymentTimestamp(String msisdn, String vendorid,
			Timestamp timestamp) {
		// TODO Auto-generated method stub
		
		CriteriaQuery<IBorrow> criteriaQuery = criteriaBuilder.createQuery(IBorrow.class);
		Root<IBorrow> root = criteriaQuery.from(IBorrow.class);
		
		criteriaQuery.select(root);
		criteriaQuery.where(criteriaBuilder.and(
				criteriaBuilder.equal(root.get(IBorrow_.msisdn), msisdn), 
				criteriaBuilder.equal(root.get(IBorrow_.vendorId), vendorid), 
				criteriaBuilder.notEqual(root.get(IBorrow_.paymentStatus), PaymentStatus.COMPLETE), 
				criteriaBuilder.lessThanOrEqualTo(root.get(IBorrow_.processedTimestamp), timestamp)
				));
		criteriaQuery.orderBy(criteriaBuilder.asc(root.get(IBorrow_.processedTimestamp)));
		
		try {
			return entityManager.createQuery(criteriaQuery).setMaxResults(1).getSingleResult();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.warn("No pending IBorrow instance found for MSISDN:" + msisdn + " and vendorId:" + vendorid + " earlier than:" + timestamp);
		}
		
		return null;
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
		
		if (operationType.equals(OperationType.LOAN))
			return retrieveLoanReference(msisdn, vendorid, timestamp);
		else
			return retrieveLoanReferenceForPayment(msisdn, vendorid, timestamp);
	}

	/**
	 * Retrieve reference number to be used for {@link Payment} record.
	 * 
	 * @param msisdn subscriber unique reference
	 * @param vendorid vendor unique reference
	 * @param timestamp time of event recorded in CDR log
	 * @return generated referenceNo
	 */
	private String retrieveLoanReferenceForPayment(String msisdn, String vendorid, Timestamp timestamp) {
		// TODO Auto-generated method stub
		
		IBorrow borrow = getEarliestBorrowByMSISDNAndVendorIdAndPaymentTimestamp(msisdn, vendorid, timestamp);
		if (borrow == null)
			return null;
		
		return borrow.getReferenceNo();
	}

	/**
	 * Retrieve reference number to be used for {@link Borrow} record.
	 * 
	 * @param msisdn subscriber unique reference
	 * @param vendorid vendor unique reference
	 * @param timestamp time of event recorded in CDR log
	 * @return generated referenceNo
	 */
	private String retrieveLoanReference(String msisdn, String vendorid, Timestamp timestamp) {
		// TODO Auto-generated method stub
		
		if (appBean.getVendorid().equalsIgnoreCase(vendorid))
			return generateReference("OTHERS|");
		
		SubscriberAssessment subscriberAssessment = getSubscriberAssessmentBySubscriber(msisdn);
		if (subscriberAssessment != null){
			Timestamp timestamp2 = subscriberAssessment.getLoanTime();
			String referenceNo =  subscriberAssessment.getLoanRef();
			if (!timestamp.after(timestamp2) && referenceNo != null)
				return referenceNo;
		}
		
		return generateReference("NANO|");
	}

	/**
	 * Generate unique borrow reference number.
	 * 
	 * @param prefix prefix to be appended to generated reference number
	 * @return unique referenceNo
	 */
	private String generateReference(String prefix) {
		// TODO Auto-generated method stub
		
		String referenceNo = new StringBuilder(prefix).append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSS"))).toString();
		
		while (getBorrowByReferenceNo(referenceNo) != null) {
			referenceNo = new StringBuilder(prefix).append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSS"))).toString();
		}
		
		return referenceNo;
	}

}