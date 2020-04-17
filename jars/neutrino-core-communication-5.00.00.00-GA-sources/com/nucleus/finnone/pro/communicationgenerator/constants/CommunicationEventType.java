package com.nucleus.finnone.pro.communicationgenerator.constants;

import javax.persistence.Entity;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.genericparameter.entity.GenericParameter;

@Entity
@DynamicUpdate
@DynamicInsert
public class CommunicationEventType extends GenericParameter{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public static final String BOUNCE_EVENT = "BOU";
	
	public static final String RESCHEDULING_EVENT="RESCHEDULING";

	public static final String FIXED_TO_FLOAT="FTFC";
	
	public static final String TERMINATION_EVENT_MATURE="TERM_MC";

	public static final String TERMINATION_EVENT_EARLY="TERM_EC";
	public static final String WELCOME_LETTER_EVENT="WEL";
	public static final String FORECLOSURE_STATEMENT_EVENT="FCSTMT";
	public static final String BALANCE_CONFIRMATION_EVENT="BALCONF";
	public static final String SWAP_LETTER_EVENT="SWAP";

	public static final String DISBURSAL_EMI_EVENT = "DISB_EMI";
	public static final String DISBURSAL_PEMI_EVENT = "DISB_PEMI";
	public static final String PERMIT_RENEWAL="PERMIT_RENEW";
	
	public static final String RC_EVENT="RC";
	
	public static final String EXHAUST_LETTER_TRANSACTION ="PDCEXH";
	
	public static final String AMOUNT_RECEIVED_VIA_CASH_SMS_EVENT="CASHRD";
	public static final String AMOUNT_RECEIVED_VIA_CHEQUE_SMS_EVENT="CHQRD";
	public static final String DISBURSEMENT_INTIMATION_SMS_EVENT="DISBINT";
	public static final String EMI_DUE_REMINDER_SMS_EVENT="INSTLRM";
	public static final String PAYMENT_NOT_RECEIVED_SMS_EVENT="PAYNOTRD";
	public static final String EMI_BOUNCED_SMS="EMIBOU";
	public static final String LIST_OF_DOCUMENT_EVENT="LOD";
	public static final String IT_CERTIFICATE_EVENT="ITCERT";
	public static final String FRR_EVENT="FRR";
	public static final String RTO_CONFIRMATION_LETTER_EVENT="RTO_CONF";

	public static final String SETTLEMENT_TERMINATION_EVENT_MATURE="STERM_MC";

	public static final String SETTLEMENT_TERMINATION_EVENT_EARLY="STERM_EC";
	public static final String RESCH_CONVERSION_EVENT="CONVERSION";
	public static final String RESCH_ANCHOR_CHANGE_EVENT="ANCHOR_CHANGE";
	
}
