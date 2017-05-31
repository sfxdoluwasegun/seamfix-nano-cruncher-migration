package com.seamfix.nano.cache;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Timestamp;
import java.util.Base64;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Resource;
import javax.ejb.Stateless;

import org.infinispan.Cache;
import org.jboss.logging.Logger;

import com.nano.jpa.entity.Borrow;
import com.nano.jpa.entity.Subscriber;
import com.nano.jpa.enums.BorrowStatus;
import com.nano.jpa.enums.PaymentStatus;

@Stateless
public class InfinispanObjectBucket {

	private Logger log = Logger.getLogger(getClass());

	@Resource(lookup = "java:jboss/infinispan/replicated_cache/debtors")
	private Cache<String, String> cache;

	/**
	 * Add transaction to cache. If Transaction already exists in cache, refresh with details of passed argument.
	 * 
	 * @param msisdn - {@link Subscriber} MSISDN, key to cache
	 * @param borrow - {@link Borrow} details; cached value
	 */
	public void addTransactionToCache(String msisdn, Borrow borrow) {
		log.info("Adding borrow:" + borrow.getReferenceNo() + " to bucketCache for " + msisdn);
		String serializedObject = getSerializedObject(borrow);
		if (cache.get(msisdn) == null)
			cache.putIfAbsent(msisdn, serializedObject);
		else
			cache.replace(msisdn, serializedObject);
	}
	
	/**
	 * Remove transaction from cache.
	 * 
	 * @param msisdn - {@link Subscriber} MSISDN, key to cache
	 * @param borrow - {@link Borrow} details; cached value
	 */
	public void removeTransactionFromCache(String msisdn, Borrow borrow) {
		// TODO Auto-generated method stub
		try {
			log.info("Removing borrow:" + borrow.getPk() + " from bucketCache for " + msisdn);
			//cache.remove(msisdn, borrow);
			cache.remove(msisdn);
		} catch (NullPointerException e) {
			// TODO Auto-generated catch block
			log.error("", e);
		}
	}
	
	/**
	 * Fetch transaction data from cache.
	 * 
	 * @param msisdn - {@link Subscriber} MSISDN, key to cache
	 * @return {@link Borrow} - cached value
	 */
	public Borrow getOutstandingTransactionFromCache(String msisdn) {
		// TODO Auto-generated method stub
		try {
			Borrow borrow = getDeserializedObject(cache.get(msisdn));
			return borrow;
		} catch (NullPointerException e) {
			// TODO Auto-generated catch block
			return null;
		}
	}
	
	/**
	 * Refresh details of cache with passed arguments.
	 * 
	 * @param msisdn - {@link Subscriber} MSISDN, key to cache
	 * @param borrow - {@link Borrow} details; cached value
	 */
	public void updateTransaction(String msisdn, Borrow borrow) {
		// TODO Auto-generated method stub
		try {
			String serializedObject = getSerializedObject(borrow);
			cache.replace(msisdn, serializedObject);
		} catch (NullPointerException e) {
			// TODO Auto-generated catch block
			log.error("", e);
		}
	}
	
	/**
	 * Confirm if subscriber data exists in cache.
	 * 
	 * @param subscriber - subscriber details
	 * @return true if exists
	 */
	public boolean isSuscriberInBucketList(Subscriber subscriber) {
		// TODO Auto-generated method stub
		return cache.containsKey(subscriber.getMsisdn());
	}
	
	/**
	 * Confirm version of transaction cached.
	 * 
	 * @param msisdn - {@link Subscriber} MSISDN, key to cache
	 * @param timestamp - borrow CDR received time-stamp value
	 * @return true if up to date
	 */
	public boolean isBorrowUpdated(String msisdn, Timestamp timestamp) {
		// TODO Auto-generated method stub
		if (cache.containsKey(msisdn)){
			Borrow borrow = getOutstandingTransactionFromCache(msisdn);
			if (borrow != null 
					&& borrow.getReceivedTimestamp() != null 
					&& borrow.getReceivedTimestamp().equals(timestamp))
				return true;
		}
		return false;
	}
	
	/**
	 * Confirm if transaction has been paid by {@link Subscriber}.
	 * 
	 * @param msisdn - {@link Subscriber} MSISDN, key to cache
	 * @param paymentStatus - indicates how well a loan has been redeemed
	 * @return true if sorted
	 */
	public boolean isBorrowSorted(String msisdn, PaymentStatus paymentStatus) {
		// TODO Auto-generated method stub
		if (cache.containsKey(msisdn)){
			Borrow borrow = getOutstandingTransactionFromCache(msisdn);
			if (borrow != null 
					&& borrow.getPaymentStatus().equals(paymentStatus))
				return true;
		}
		return false;
	}
	
	/**
	 * Confirm if transaction has been received by {@link Subscriber}.
	 * 
	 * @param msisdn - {@link Subscriber} MSISDN, ket to cache entry
	 * @return true if received
	 */
	public boolean isBorrowRecieved(String msisdn) {
		// TODO Auto-generated method stub
		if (cache.containsKey(msisdn)){
			Borrow borrow = getOutstandingTransactionFromCache(msisdn);
			if (borrow != null 
					&& borrow.getStatus().equals(BorrowStatus.RECEIVED))
				return true;
		}
		return false;
	}
	
	/**
	 * Fetch list of all keys cached keys.
	 * 
	 * @return list
	 */
	public List<String> getAllKeysFromCache() {
		// TODO Auto-generated method stub
		Set<String> keys = cache.keySet();
		if (keys != null)
			return keys.stream().collect(Collectors.toList());
		return new LinkedList<String>();
	}
	
	/**
	 * Serialize {@link Borrow} to encoded string.
	 * 
	 * @param borrow
	 * @return encoded string
	 */
	private String getSerializedObject(Borrow borrow) {
		// TODO Auto-generated method stub
		if (borrow == null)
			return null;
		ObjectOutputStream objectOutputStream = null;
		try {
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
			objectOutputStream.writeObject(borrow);
			return Base64.getEncoder().encodeToString(byteArrayOutputStream.toByteArray());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			log.error("", e);
		} finally {
			try {
				if (objectOutputStream != null)
					objectOutputStream.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				log.error("", e);
			}
		}
		return null;
	}
	
	/**
	 * Get Borrow from DE-serialized String.
	 * 
	 * @param string
	 * @return {@link Borrow}
	 */
	private Borrow getDeserializedObject(String string) {
		// TODO Auto-generated method stub
		if (string == null)
			return null;
		byte[] data = Base64.getDecoder().decode(string);
		ObjectInputStream objectInputStream = null;
		try {
			objectInputStream = new ObjectInputStream(new ByteArrayInputStream(data));
			return (Borrow) objectInputStream.readObject();
		} catch (IOException | ClassNotFoundException e) {
			// TODO Auto-generated catch block
			log.error("", e);
		} finally {
			try {
				if (objectInputStream != null)
					objectInputStream.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				log.error("", e);
			}
		}
		return null;
	}

}