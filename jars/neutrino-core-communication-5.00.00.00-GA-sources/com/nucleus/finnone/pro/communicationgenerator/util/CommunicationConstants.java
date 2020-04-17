package com.nucleus.finnone.pro.communicationgenerator.util;

import com.nucleus.mail.entity.MailMessageExchangeRecord;
import com.nucleus.message.entity.ShortMessageExchangeRecord;
import com.nucleus.message.entity.WhatsAppExchangeRecord;

public final class CommunicationConstants {

	public static final Character INITIAL_STATUS = Character.valueOf('I');
	public static final String MANDATORY_FEILDS_MISSING = "";
	public static final String PHONE_NUMBER_TYPE_PHONE = "Phone";
	public static final String PHONE_NUMBER_TYPE_MOBILE = "Mobile";
	public static final Long ONE = Long.valueOf(1);
	public static final Long TWENTY_FOUR = Long.valueOf(24);
	public static final Long SIXTY = Long.valueOf(60);
	public static final int ONE_HUNDRED = 100;
	public static final Long ONE_THOUSAND = Long.valueOf(1000);
	public static final String CONFIGURATION_QUERY = "Configuration.getPropertyValueFromPropertyKey";
	public static final String COMMUNICATION_SCHEDULER_BATCH = "config.communication.batch";
	public static final String COMMUNICATION_MERGE_CREATE = "config.communication.merge.create";
	public static final int DEFAULT_BATCH_SIZE = 1000;
	public static final int START_POSITION_ZERO = 0;
	public static final String DEFAULD_CREATER = "System";
	public static final Character ATTACHMENT_SEPARATOR=Character.valueOf('#');
	public static final String MAIN_TEMPLATE="maintemplate";
	public static final String ATTACHED_TEMPLATE="attachedtemplate";
	public static final String IS_COMPUTE_PSWD="computePasswordForEncryption";
	public static final String COMPUTED_PSWD="computedPasswordForEncryption";
	public static final Class<?> CACHED_MAIL_CLASS = MailMessageExchangeRecord.class;
	public static final Class<?> CACHED_SMS_CLASS = ShortMessageExchangeRecord.class;
	public static final Class<?> CACHED_WHATSAPP_CLASS = WhatsAppExchangeRecord.class;
	public static final String IMMEDIATE = "IMMEDIATE";
	public static final String ON_DEMAND = "ON_DEMAND";
	public static final String BULK = "BULK";
	public static final String ERROR_COMM_TEMPL_MAP_EMPTY = "msg.8000020";
	private CommunicationConstants() {

	}

}
