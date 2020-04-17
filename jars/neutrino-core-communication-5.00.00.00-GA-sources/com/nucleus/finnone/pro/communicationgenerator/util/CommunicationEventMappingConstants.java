package com.nucleus.finnone.pro.communicationgenerator.util;

public class CommunicationEventMappingConstants {

	public static final String TYPE_SIZE = "typeSize";

	public static final String COMM_EVENT_MAPPING_HDR = "CommunicationEventMappingHeader";
	public static final String MASTER_ID = "masterID";
	public static final String PARENT_ID = "parentId";
	public static final String COMM_EVENT_MAPPING = "communicationEventMapping";
	public static final String AUTOCOMPLETE = "autocomplete";
	public static final String APPROVAL_STATUS = "approvalStatus";
	public static final String ENTITY = "entity";
	public static final String ERROR = "error";
	public static final String VIEWABLE = "viewable";
	public static final String COMMUNICATION_EVENT_MAPPING_ALREADY_EXIST = "label.COMMUNICATION_EVENT_MAPPING_ALREADY_EXIST";
	public static final String COMMUNICATION_EVENT_MAPPING_QUERY = "Select ad.id from COL_ACCOUNT_DTL ad ";
	public static final String SOURCE_PRODUCTS = "sourceProducts";
	public static final String COMM_CODE_DISABLED = "commCodeDisabled";
	public static final String SOURCE_PRODUCT_DISABLED = "sourceProductDisabled";
	public static final String EVENT_CODE_DISABLED = "eventCodeDisabled";
	public static final String TEMPLATE_GRID_CURRENT_INDEX = "templateGridCurrentIndex";
	public static final String TEMPLATE_GRID_SIZE = "templateGridSize";
	public static final String COMM_TEMPLATE_MAP_ACCORD_INDEX = "commTemplateMapAccordIndex";
	public static final String COMM_TEMPLATE_MAP_ACCORD_SIZE = "commTemplateMapAccordSize";
	public static final String SELECT_CLAUSE = "queryBaseSelectClause";
	public static final String EDIT = "edit";
	public static final String COMM_CODE_ID = "communicationCodeId";
	public static final String COMM_TEMPLATE_MAP_INDEX = "commTemplateMapIndex";
	public static final String END_SIZE = "endSize";
	public static final String COMM_TEMPLATE_MAP_ACCORD_NEXT_INDEX = "commTemplateMapAccordNextIndex";
	public static final String EVENT_CODE_ID = "eventCodeId";
	public static final String SOURCE_PRODUCT_ID = "sourceProductId";
	public static final String COMMM_MASTER_ID = "communicationMasterId";
	public static final String APPROVAL_STATUS_TYPES = "approvalStatusTypes";
	public static final String ACTIVE_FLAG = "activeFlag";
	public static final String COMM_TYPE_ID = "communicationTypeId";
	public static final String ATTACHMENT_TEMPLATES = "attachmentTemplates";
	public static final String MAKER_COMMUNICATIONEVENTMAPPINGHEADER = "hasAuthority('MAKER_COMMUNICATIONEVENTMAPPINGHEADER')";
	public static final String VIEW_COMMUNICATIONEVENTMAPPINGHEADER = "hasAuthority('VIEW_COMMUNICATIONEVENTMAPPINGHEADER')";
	public static final String IS_EMAIL = "isEmail";
	public static final String COMM_TYPE_NAME = "communicationTypeName";
	public static final String ID_LENGTH_NINETEEN = "Numeric(19,0)";
	public static final String ID_LENGTH_FOUR = "Numeric(4,0)";
	
	public static final String CRITERIA_BASE_SELECT_CLAUSE = "select event.id from COM_COMMN_EVENT_REQUEST_LOG event";
	public static final  String QUERY_NATIVE_SELECT_COMM_EVENT_REQ_LOG = "select * from COM_COMMN_EVENT_REQUEST_LOG communicationEventRequestLog  Where communicationEventRequestLog.id in ( ";
	public static final  String QUERY_NATIVE_APPEND_SOURCE_PRODUCT = "and communicationEventRequestLog.SOURCE_PRODUCT_ID = :sourceProduct ";
	public static final  String QUERY_NATIVE_APPEND_STATUS = "and communicationEventRequestLog.STATUS= :status ";
	public static final  String QUERY_NATIVE_APPEND_EVENT_CODE = "and communicationEventRequestLog.EVENT_CODE= :eventCode ";
	public static final  String QUERY_NATIVE_APPEND_SUBJECT_URI = " and communicationEventRequestLog.SUBJECTURI= :subjectURI ";
	public static final  String QUERY_NATIVE_APPEND_APPLICABLE_PRIMARY_URI = " and communicationEventRequestLog.APPLICABLE_PRIMARY_ENTITYURI= :applicablePrimaryEntityURI ";
	public static final  String QUERY_NATIVE_APPEND_SUBJECT_REFERENCE = " and communicationEventRequestLog.SUBJECT_REFERENCE_NUMBER= :subjectReference ";
	
	public static final String EVENT_CODE = "eventCode";
	
}
