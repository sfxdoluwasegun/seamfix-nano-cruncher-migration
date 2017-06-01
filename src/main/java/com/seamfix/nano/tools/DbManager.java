package com.seamfix.nano.tools;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
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
import javax.persistence.criteria.Root;

import org.jboss.logging.Logger;

import com.nano.jpa.entity.Dealing;
import com.nano.jpa.entity.Dealing_;
import com.nano.jpa.entity.OtherDealing;
import com.nano.jpa.entity.OtherDealing_;
import com.nano.jpa.enums.DealType;
import com.nano.jpa.enums.OperationType;

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
	 * Fetch Dealing by MSISDN, operationTime and operationType properties.
	 * 
	 * @param msisdn
	 * @param timestamp
	 * @param operationType
	 * @return {@link Dealing}
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
	 * Fetch OtherDealing by MSISDN, operationTime and operationType properties.
	 * 
	 * @param msisdn
	 * @param timestamp
	 * @param operationType
	 * @return {@link OtherDealing}
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
	 * Create new Dealing instance.
	 * 
	 * @param balanceType
	 * @param changeBalance
	 * @param currentBalance
	 * @param entryDate
	 * @param etuAmount
	 * @param etuGraceDate
	 * @param forceRepayDate
	 * @param initialEtuAmount
	 * @param initialLoanAmount
	 * @param initialLoanPoundage
	 * @param loanAmount
	 * @param loanBalanceType
	 * @param loanPoundage
	 * @param loanVendorId
	 * @param msisdn
	 * @param offering
	 * @param operationType
	 * @param queryManager
	 * @param repayment
	 * @param repayPoundage
	 * @param subid
	 * @param timestamp
	 * @param transid
	 * @return {@link Dealing}
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public Dealing persistDealing(long balanceType, BigDecimal changeBalance,
			BigDecimal currentBalance, Timestamp entryDate, BigDecimal etuAmount, Timestamp etuGraceDate,
			Timestamp forceRepayDate, BigDecimal initialEtuAmount, BigDecimal initialLoanAmount,
			BigDecimal initialLoanPoundage, BigDecimal loanAmount, long loanBalanceType, BigDecimal loanPoundage,
			String loanVendorId, String msisdn, String offering, OperationType operationType, QueryManager queryManager,
			BigDecimal repayment, BigDecimal repayPoundage, long subid, Timestamp timestamp, long transid) {
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
		dealing.setReferenceNo(transid);
		dealing.setSubscriberId(subid);
		dealing.setTimestamp(Timestamp.valueOf(LocalDateTime.now()));
		dealing.setVendorid(loanVendorId);

		return (Dealing) create(dealing);
	}

	/**
	 * Create new OtherDealing instance.
	 * 
	 * @param balanceType
	 * @param changeBalance
	 * @param currentBalance
	 * @param entryDate
	 * @param etuAmount
	 * @param etuGraceDate
	 * @param forceRepayDate
	 * @param initialEtuAmount
	 * @param initialLoanAmount
	 * @param initialLoanPoundage
	 * @param loanAmount
	 * @param loanBalanceType
	 * @param loanPoundage
	 * @param loanVendorId
	 * @param msisdn
	 * @param offering
	 * @param operationType
	 * @param queryManager
	 * @param repayment
	 * @param repayPoundage
	 * @param subid
	 * @param timestamp
	 * @param transid
	 */
	@Asynchronous
	public void persistOtherDealing(long balanceType, BigDecimal changeBalance, BigDecimal currentBalance,
			Timestamp entryDate, BigDecimal etuAmount, Timestamp etuGraceDate, Timestamp forceRepayDate,
			BigDecimal initialEtuAmount, BigDecimal initialLoanAmount, BigDecimal initialLoanPoundage,
			BigDecimal loanAmount, long loanBalanceType, BigDecimal loanPoundage, String loanVendorId, String msisdn,
			String offering, OperationType operationType, QueryManager queryManager, BigDecimal repayment,
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