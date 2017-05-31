package com.seamfix.nano.enums;

import com.nano.jpa.enums.SmsMessageId;

// TODO: Auto-generated Javadoc
/**
 * The Enum SmppResponse.
 *
 * @author segz
 */

public enum SmppResponse {
	
	CREDIT_PROVIDED("msg_credit_provided.ftl", "MsgCreditProvided", SmsMessageId.MSG_CREDIT_PROVIDED), 
	MSG_TARRIF_TYPE("msg_tarrif_types_scoring.ftl", "MsgTariffTypesScoring ", SmsMessageId.MSG_TARIFF_TYPES_SCORING), 
	NO_DEBT_SCORING("msg_no_debt_scoring.ftl", "MsgNoDebtsScoring", SmsMessageId.MSG_NO_DEBTS_SCORING), 
	NTWK_LIFETIME("msg_ntwk_lifetime_scoring.ftl", "MsgNetworkLifetimeScoring", SmsMessageId.MSG_NETWORK_LIFETIME_SCORING), 
	TOPUP_AMOUNT("msg_topup_amt_scoring.ftl", "MsgTopupsAmountScoring", SmsMessageId.MSG_TOPUPS_AMOUNT_SCORING), 
	PARTLY_COVERED("msg_partly_covered.ftl", "MsgPartlyCovered", SmsMessageId.MSG_PARTLY_COVERED), 
	FULLY_COVERED("msg_fully_covered.ftl", "MsgFullyCovered", SmsMessageId.MSG_FULLY_COVERED), 
	HAS_DEBT("msg_has_debt.ftl", "MsgHasDebts", SmsMessageId.MSG_HAS_DEBTS), 
	HAS_NO_DEBT("msg_has_no_debt.ftl", "MsgHasNoDebt", SmsMessageId.MSG_HAS_NO_DEBT), 
	UNRECOGNIZED_REQ("msg_unrecognized_req.ftl", "MsgUnrecognizedRequest", SmsMessageId.MSG_UNRECOGNIZED_REQUEST), 
	ANOTHER_REQ_PROCESS("msg_another_req_process.ftl", "MsgAnotherRequestProcess", SmsMessageId.MSG_ANOTHER_REQUEST_PROCESS), 
	COMMON_ERROR("msg_common_err.ftl", "MsgCommmonError", SmsMessageId.MSG_COMMON_ERROR),
	COMMON_SUCCESS("msg_common_success.ftl", "MsgCommmonSuccess", null), 
	BAL_CHECK("msg_bal_check_scoring.ftl", "MsgBalanceCheckScoring", SmsMessageId.MSG_BALANCE_CHECK_SCORING), 
	LIFE_CYCLE_STATUS("msg_life_cycle_status.ftl", "MsgLifecycleStatusScoring", SmsMessageId.MSG_LIFECYCLE_STATUS_SCORING), 
	BLACKLIST("msg_blacklist.scoring.ftl", "MsgBlackListScoring", SmsMessageId.MSG_BLACKLIST_SCORING), 
	REMOVE_FROM_BLACKLIST("msg_remove_blacklist.ftl", "MsgRemoveFromBlackList", SmsMessageId.MSG_REMOVE_FROM_BLACKLIST), 
	MSG_HELP("msg_help.ftl", "MsgHelp", SmsMessageId.MSG_HELP), 
	CREDIT_PROVIDED_SIMULATION("msg_credit_simulation.ftl", "MsgCreditProvidedSimulate", SmsMessageId.MSG_CREDIT_PROVIDED_SIMULATE), 
	NO_DEBT_SIMULATION("msg_no_debt_simulation.ftl", "MsgNoDebtsScoringSimulate", SmsMessageId.MSG_NO_DEBT_SCORING_SIMULATE), 
	NTWK_LIFETIME_SIMULATION("msg_ntwk_lifetime_simulation.ftl", "MsgNetworkLifetimeScoringSimulate", SmsMessageId.MSG_NETWORK_LIFETIME_SCORING_SIMULATE), 
	TOPUP_AMT_SIMULATION("msg_topup_amt_simulation.ftl", "MsgTopupsAmountScoringSimulate", SmsMessageId.MSG_TOPUPS_AMOUNT_SCORING_SIMULATE), 
	TOP_UP_COUNT("msg_toup_count.ftl", "MsgTopupCountScoring", SmsMessageId.MSG_TOPUP_COUNT_SCORING), 
	SMART_LOAN_REQUEST("msg_smart_loan.ftl", "Smart Loan Request", SmsMessageId.SMART_LOAN_REQUEST), 
	EVC_INELIGIBLE("msg_evc_ineligible.ftl", "EvcSystemsVolumeExceeded", SmsMessageId.EVC_SYSTEMS_VOLUMES_EXCEEDED);
	
	private String response ;
	private String name ;
	private SmsMessageId smsMessageId ;
	
	private SmppResponse(String response, 
			String name, SmsMessageId smsMessageId){
		
		this.response = response;
		this.name = name;
		this.setSmsMessageId(smsMessageId);
	}
	
	/**
	 * Retrieve enumeration from name string.
	 *
	 * @param name - enumeration name value
	 * @return {@link SmppResponse} enumeration
	 */
	public static SmppResponse fromName(String name){
		if (name != null && !name.isEmpty()){
			for (SmppResponse smppResponse : SmppResponse.values()){
				if (smppResponse.getName().equalsIgnoreCase(name))
					return smppResponse;
			}
		}
		return null;
	}
	
	/**
	 * Retrieve enumeration from sms message ID.
	 *
	 * @param smsMessageId - the sms message ID value
	 * @return {@link SmppResponse} enumeration
	 */
	public static SmppResponse fromSmsMessageId(SmsMessageId smsMessageId){
		if (smsMessageId != null){
			for (SmppResponse smppResponse : SmppResponse.values()){
				if (smppResponse.getSmsMessageId().equals(smsMessageId))
					return smppResponse;
			}
		}
		return null;
	}
	
	public String getResponse() {
		return response;
	}

	public void setResponse(String response) {
		this.response = response;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public SmsMessageId getSmsMessageId() {
		return smsMessageId;
	}

	public void setSmsMessageId(SmsMessageId smsMessageId) {
		this.smsMessageId = smsMessageId;
	}

}