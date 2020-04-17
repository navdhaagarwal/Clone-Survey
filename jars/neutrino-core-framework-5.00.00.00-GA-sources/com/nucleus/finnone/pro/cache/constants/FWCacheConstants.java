package com.nucleus.finnone.pro.cache.constants;

public final class FWCacheConstants {

	public static final String CACHE_MANAGER = "CACHE_MANAGER";
	
	/**
	 * WARNING: Don't change value of CACHE_IDENTIFER_DELIMITER.
	 * Changing it to any other value will impact many critical functionalities
	 */
	public static final String CACHE_IDENTIFER_DELIMITER = "-";
	public static final String EMPTY_CACHE_NAME = "EMPTY";
	public static final String KEY_DELIMITER = "$";
	public static final String TIMEOUT_SET = "TIMEOUT_SET";
	public static final String REGEX_DELIMITER = "\\$";
	public static final String EMPTY_VALUE = "$EMPTY_VALUE$";
	public static final String IMPACTED_CACHE_MAP = "IMPACTED_CACHE_MAP";
	
	public static final String MASTER_CACHE = "MASTER_CACHE";
  	public static final String MASTER_CACHE_INDIVIDUAL = "MASTER_CACHE_INDIVIDUAL";
  	public static final String COMMUNICATION_EVENT_MAPPING_CACHE="COMMUNICATION_EVENT_MAPPING_CACHE";
	public static final String FW_CACHE_REGION = "FW_CACHE_REGION";
	
	//Miscellaneous Cache Group
	public static final String MISCELLANEOUS_CACHE_GROUP = "MISCELLANEOUS_CACHE_GROUP";
	
	// Session Registry Caches
	public static final String SESSION_REGISTRY_PRINCIPALS_CACHE = "SESSION_REGISTRY_PRINCIPALS_CACHE";
	public static final String SESSION_REGISTRY_SESSION_IDS_CACHE = "SESSION_REGISTRY_SESSION_IDS_CACHE";
	
	
	// Parameter Caches
	public static final String PARAMETER_CACHE_GROUP="PARAMETER_CACHE_GROUP";
	public static final String PARAMETER_CACHE_ID = "PARAMETER_CACHE_ID";
	public static final String PARAMETER_BY_TYPE_AND_NAME = "PARAMETER_BY_TYPE_AND_NAME";
	public static final String OG_PARAMETER_BY_OG = "OG_PARAMETER_BY_OG";
	public static final String DECRYPTED_PARAM_SCRIPT_ID = "DECRYPTED_PARAM_SCRIPT_ID";
	public static final String SCRIPTPARAMETER_EVALUATOR_BY_PARAM_ID = "SCRIPTPARAMETER_EVALUATOR_BY_PARAM_ID";
	
	//Currency Constants
	public static final String CURRENCY_CACHE_GROUP = "CURRENCY_CACHE_GROUP";
	public static final String CURRENCY_CACHE_ISO = "CURRENCY_CACHE_ISO";
    public static final String CURRENCY_CACHE_BY_ID = "CURRENCY_CACHE_BY_ID";
    public static final String CURRENCY_CACHE_APPROVED_ACTIVE = "CURRENCY_CACHE_APPROVED_ACTIVE";
    public static final String CURRENCY_CONVERSION_RATE = "CURRENCY_CONVERSION_RATE";
    public static final String CURRENCY_COMMON_PROPS = "CURRENCY_COMMON_PROPS";
    
 // ScriptRule Caches
    public static final String SCRIPTRULE_EVALUATOR_CACHE_GROUP="SCRIPTRULE_EVALUATOR_CACHE_GROUP";
 	public static final String SCRIPTRULE_EVALUATOR_BY_SCRIPTRULE_ID = "SCRIPTRULE_EVALUATOR_BY_SCRIPTRULE_ID"; 

 // Configuration Caches
 	public static final String CONFIGURATION_CACHE_GROUP="CONFIGURATION_CACHE_GROUP";
 	public static final String CONFIGURATION_DISTINCT_MODIFIABLE_PROPERTYKEY = "CONFIGURATION_DISTINCT_MODIFIABLE_PROPERTYKEY";
 	public static final String CONFIGURATION_DISTINCT_PROPERTKEY = "CONFIGURATION_DISTINCT_PROPERTKEY";
 	public static final String CONFIGURATION_GROUP_CACHE_ASSOCIATED_ENTITY = "CONFIGURATION_GROUP_CACHE_ASSOCIATED_ENTITY";
 	public static final String CONFIGURATION_GROUP_ID = "CONFIGURATION_GROUP_ID";
 	public static final String ENTITYURI_PROPKEY_CONFIG_MAP = "ENTITYURI_PROPKEY_CONFIG_MAP";
 	public static final String ENTITYURI_PROPKEY_CONFIGVO_MAP = "ENTITYURI_PROPKEY_CONFIGVO_MAP";
 	
 	
 // Communication Cache
 	public static final String COMMUNICATION_CACHE_GROUP="COMMUNICATION_CACHE_GROUP";
 	public static final String COMM_DATA_PREP_DTL="COMM_DATA_PREP_DTL";
    public static final String COMMUNICATION_MST="COMMUNICATION_MST";
    public static final String COMMUNICATION_TEMPLATE="COMMUNICATION_TEMPLATE";
    public static final String COMMUNICATION_RETRY_ATTEMPT_CONFIG = "COMMUNICATION_RETRY_ATTEMPT_CONFIG";
    public static final String COMMUNICATION_CODE_ADDITIONAL_METHODS = "COMMUNICATION_CODE_ADDITIONAL_METHODS";
    
 // File Consolidator Constants BaseRecordFormatDetailFromUserRecordIdentifierCode
    public static final String FILE_CONSOLIDATOR_CACHE_GROUP = "FILE_CONSOLIDATOR_CACHE_GROUP";
  	public static final String USER_RECORD_IDENTIFIER_BASE_RECORD_IDENTIFIER_CODE = "USER_RECORD_IDENTIFIER_BASE_RECORD_IDENTIFIER_CODE";
  	

// Generic Parameter Caches
  	public static final String GENERIC_PARAMETER_CACHE_GROUP = "GENERIC_PARAMETER_CACHE_GROUP";
 	public static final String GENERIC_PARAMETER_CODE_ENTITY = "GENERIC_PARAMETER_CODE_ENTITY";
 	public static final String GENERIC_PARAMETER_NAME_ENTITY = "GENERIC_PARAMETER_NAME_ENTITY";
 	public static final String GENERIC_PARAMETER_AUTHCODE_ENTITIES = "GENERIC_PARAMETER_AUTHCODE_ENTITIES";
 	public static final String GENERIC_PARAMETER_PARENTCODE_ENTITIES = "GENERIC_PARAMETER_PARENTCODE_ENTITIES";
 	public static final String GENERIC_PARAMETER_TYPE_ENTITIES = "GENERIC_PARAMETER_TYPE_ENTITIES";
 	

// WorkflowConfiguration Caches
 	public static final String WORKFLOW_CONFIG_CACHE_GROUP = "WORKFLOW_CONFIG_CACHE_GROUP";
	public static final String PROCESSING_STAGE_FOR_STAGE_AND_CONFIGTYPE = "PROCESSING_STAGE_FOR_STAGE_AND_CONFIGTYPE";
	public static final String FORM_BEAN_CACHE_FOR_STAGE_AND_WORKFLOW_CONFIG_TYPE = "FORM_BEAN_CACHE_FOR_STAGE_AND_WORKFLOW_CONFIG_TYPE";
//OrganizationBranchList Cache
	public static final String ORGANIZATION_BRANCH_CACHE_GROUP= "ORGANIZATION_BRANCH_CACHE_GROUP";
	public static final String ORGANIZATION_BRANCH_INFO_CACHE= "ORGANIZATION_BRANCH_INFO_CACHE";
	
// User-Role-Authority Caches
	public static final String USER_CACHE_GROUP = "USER_CACHE_GROUP";
	public static final String USERNAME_USERS_ID_CACHE = "USERNAME_USERS_ID_CACHE";
  	public static final String AUTHCODE_AUTHORITY_ID_CACHE = "AUTHCODE_AUTHORITY_ID_CACHE";
  	public static final String USERID_USERPROFILE_ID_CACHE = "USERID_USERPROFILE_ID_CACHE";
  	public static final String USER_GRANTED_AUTHORITIES_CACHE = "USER_GRANTED_AUTHORITIES_CACHE";
  	public static final String USERID_USER_AUTHORITIES_CACHE = "USERID_USER_AUTHORITIES_CACHE";
  	public static final String USERID_AUTHCODE_CACHE = "USERID_AUTHCODE_CACHE";
  	
  	//UserMailNotificationCache
  	public static final String USER_MAIL_NOTIFICATION_COUNT_CACHE = "USER_MAIL_NOTIFICATION_COUNT_CACHE";
  	
  	/**
  	 * For caches which has small set of objects so instead of creating those many caches use this cache and put key in
  	 * com.nucleus.finnone.pro.cache.constants.FWCommonCacheKeys
  	 * 
  	 */
  	public static final String FW_COMMON_CACHE_GROUP = "FW_COMMON_CACHE_GROUP";
  	public static final String FW_COMMON_CACHE = "FW_COMMON_CACHE";
  	
  	public static final String HOTKEY_CACHE_GROUP = "HOTKEY_CACHE_GROUP";
  	public static final String FW_HOTKEY_CACHE = "FW_HOTKEY_CACHE";
  	
  	public static final String FC_CACHE = "FC_CACHE";
  	
  	
  	public static final String PHONE_TAG_DATA = "PHONE_TAG_DATA";
  	
  	public static final String SSO_PHRASE_MAP = "SSO_PHRASE_MAP";
  	public static final String SERVICE_TICKET_TO_SESSION_CACHE = "SERVICE_TICKET_TO_SESSION_CACHE";
	public static final String SESSION_ID_TO_SERVICE_TICKET_CACHE = "SESSION_ID_TO_SERVICE_TICKET_CACHE";
	//License Detail cache
    public static final String LICENSE_DETAIL_CACHE = "LICENSE_DETAIL_CACHE";

    public static final String RULE_EXPRESSION_GROUP_CACHE = "RULE_EXPRESSION_GROUP_CACHE";

    public static final String RULE_DISTINCT_CONDITION_CACHE = "RULE_DISTINCT_CONDITION_CACHE";

    public static final String RULE_EXPRESSION_CNF_META_DATA_CACHE = "RULE_EXPRESSION_CNF_META_DATA_CACHE";
    
    
    //BroadcastMessage cache
    public static final String BROADCAST_MESSAGE_CACHE = "BROADCAST_MESSAGE_CACHE";

    //Session Attribute Store Cache
	public static final String SESSION_ATTRIBUTE_STORE_CACHE = "SESSION_ATTRIBUTE_STORE_CACHE";
  	
	public static final String API_SECURITY_KEY_CACHE = "API_SECURITY_KEY_CACHE";
	
	//Oauth token details
	public static final String OAUTH_TOKEN_DETAILS_CACHE = "OAUTH_TOKEN_DETAILS_CACHE";


	private FWCacheConstants() {
		super();
	}
}
