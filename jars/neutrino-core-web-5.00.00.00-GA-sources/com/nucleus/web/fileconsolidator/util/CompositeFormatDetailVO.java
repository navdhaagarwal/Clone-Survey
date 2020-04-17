package com.nucleus.web.fileconsolidator.util;

import com.nucleus.NeutrinoUUIDGenerator;
import com.nucleus.finnone.pro.fileconsolidator.domainobject.entities.FileUploadDownloadBaseFormatDetail;
import com.nucleus.finnone.pro.fileconsolidator.domainobject.entities.FileUploadDownloadUserFormatDetail;
import com.nucleus.finnone.pro.fileconsolidator.util.DataTypeEnum;
import com.nucleus.finnone.pro.fileconsolidator.util.FileUploadDownloadConstants;
import org.apache.commons.lang.StringUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CompositeFormatDetailVO implements Serializable {

    public static final String HTML_ID_PREFIX = "formatDetail_";
    public static final String SELECT_ID_KEY = "sId";
    public static final String SELECT_VALUE_KEY = "sValue";
    public static final String SELECT_SELECTED_KEY = "sSelected";

    private String id = StringUtils.EMPTY; //This is html id

    private String baseFormatId = StringUtils.EMPTY; //Long

    private String userFormatId = StringUtils.EMPTY; //Long

    private String baseFormatDetailId = StringUtils.EMPTY;

    private String userFormatDetailId = StringUtils.EMPTY;

    private String baseRecordIdentifierId = StringUtils.EMPTY;

    private String userRecordIdentifierId = StringUtils.EMPTY;

    private String formatDetailId = StringUtils.EMPTY; //Long

    private String recordIdentifierCode = StringUtils.EMPTY; // Merge

    private String paddingStrategyId = StringUtils.EMPTY;

    private String propertyName = StringUtils.EMPTY;

    private String fieldName = StringUtils.EMPTY;

    private String fieldDescription = StringUtils.EMPTY;

    private String dataType = DataTypeEnum.STRING.getDescription();

    private String groupOrder = StringUtils.EMPTY; //Integer

    private String fieldI18Key = StringUtils.EMPTY;

    private String defaultSequence = StringUtils.EMPTY; //Long

    private String keyFieldFlag = StringUtils.EMPTY;  //Character

    private String baseMandatoryFlag = StringUtils.EMPTY; //TODO Convert to mandatoryFlagEditable Boolean  //Character

    private Boolean isBaseMandatory = Boolean.FALSE;

    private List<Map<String,String>> fieldTypeSelectOptions;

    private String compositeMandatoryFlag = FileUploadDownloadConstants.OPTIONAL_FLAG; //Optional/mandatory merge //Character

    private String formatMask = StringUtils.EMPTY; //Merge

    private String length = StringUtils.EMPTY; //Merge //Integer

    private String fieldOrder = StringUtils.EMPTY; //Merge //Long

    private String defaultValue = StringUtils.EMPTY; //Merge

    private String trackingFieldFlag = StringUtils.EMPTY; //Merge //Character

    private String trackingFieldOrder = StringUtils.EMPTY; //Merge //Long

    private String leftOfDecimalLength = StringUtils.EMPTY; //Merge //Integer

    private String rightOfDecimalLength = StringUtils.EMPTY; //Merge //Integer

    private void generateUniqueId(){
        this.id = HTML_ID_PREFIX + (new NeutrinoUUIDGenerator()).generateUuid().replace("-","");
    }

    public CompositeFormatDetailVO (FileUploadDownloadUserFormatDetail userFormatDetail){
        this(userFormatDetail.getBaseFormatDetail());

        this.userFormatId = defaultStringIfNull(userFormatDetail.getFormatId(),StringUtils.EMPTY);
        this.formatDetailId = defaultStringIfNull(userFormatDetail.getId(),StringUtils.EMPTY);
        this.userFormatDetailId = defaultStringIfNull(userFormatDetail.getId(),StringUtils.EMPTY);
        this.userRecordIdentifierId = defaultStringIfNull(userFormatDetail.getUserRecordIdentifier().getId(),StringUtils.EMPTY);
        //Replace Merge

        //Default Merge
        this.recordIdentifierCode = defaultStringIfNull(userFormatDetail.getUserRecordIdentifier().getRecordIdentifierCode(),recordIdentifierCode);
        this.length = defaultStringIfNull(userFormatDetail.getLength(),length);
        this.fieldOrder = defaultStringIfNull(userFormatDetail.getFieldOrder(),fieldOrder);
        this.defaultValue = defaultStringIfNull(userFormatDetail.getDefaultValue(),defaultValue); //String
        this.leftOfDecimalLength = defaultStringIfNull(userFormatDetail.getLeftOfDecimalLength(),leftOfDecimalLength);
        this.rightOfDecimalLength = defaultStringIfNull(userFormatDetail.getRightOfDecimalLength(),rightOfDecimalLength);
        this.paddingStrategyId = defaultStringIfNull(userFormatDetail.getPaddingStrategyId(),paddingStrategyId);

        this.compositeMandatoryFlag = defaultStringIfNull(userFormatDetail.getOptionalMandatoryFlag(),compositeMandatoryFlag); //Character
        this.trackingFieldFlag = defaultStringIfNull(userFormatDetail.getTrackingFieldFlag(),trackingFieldFlag); //Character
        this.trackingFieldOrder = defaultStringIfNull(userFormatDetail.getTrackingFieldOrder(),trackingFieldOrder);
        this.formatMask = defaultStringIfNull(userFormatDetail.getFormatMask(),formatMask);

    }

    public CompositeFormatDetailVO (FileUploadDownloadBaseFormatDetail baseFormatDetail){
        this();

        if(baseFormatDetail != null){

            this.baseFormatId = defaultStringIfNull(baseFormatDetail.getHeaderId(),baseFormatId);
            this.formatDetailId = defaultStringIfNull(baseFormatDetail.getId(),formatDetailId);
            this.baseFormatDetailId = defaultStringIfNull(baseFormatDetail.getId(),baseFormatDetailId);
            this.paddingStrategyId = defaultStringIfNull(baseFormatDetail.getPaddingStrategyId(),paddingStrategyId);
            this.baseRecordIdentifierId = defaultStringIfNull(baseFormatDetail.getSystemRecordIdentifier().getId(),baseRecordIdentifierId);

            this.propertyName = defaultStringIfNull(baseFormatDetail.getPropertyName(),propertyName);
            this.fieldName = defaultStringIfNull(baseFormatDetail.getFieldName(),fieldName);
            this.dataType = defaultStringIfNull(baseFormatDetail.getDataType(),dataType);
            this.groupOrder = defaultStringIfNull(baseFormatDetail.getGroupOrder(),groupOrder);
            this.fieldI18Key = defaultStringIfNull(baseFormatDetail.getFieldI18Key(),fieldI18Key);
            this.defaultSequence = defaultStringIfNull(baseFormatDetail.getDefaultSequence(),defaultSequence);
            this.keyFieldFlag = defaultStringIfNull(baseFormatDetail.getKeyFieldFlag(),keyFieldFlag);
            this.baseMandatoryFlag = defaultStringIfNull(baseFormatDetail.getMandatoryFlag(),baseMandatoryFlag);
            this.isBaseMandatory = this.baseMandatoryFlag.equals(String.valueOf(FileUploadDownloadConstants.MANDATORY_FLAG));

            this.recordIdentifierCode = defaultStringIfNull(baseFormatDetail.getSystemRecordIdentifier().getRecordIdentifierCode(),recordIdentifierCode);
            this.formatMask = defaultStringIfNull(baseFormatDetail.getFormatMask(),formatMask);
            this.compositeMandatoryFlag = defaultStringIfNull(baseFormatDetail.getMandatoryFlag(),compositeMandatoryFlag);
            this.length = defaultStringIfNull(baseFormatDetail.getLength(),length);
            this.fieldOrder = defaultStringIfNull(baseFormatDetail.getFieldOrder(),fieldOrder);
            this.defaultValue = defaultStringIfNull(baseFormatDetail.getDefaultValue(),defaultValue);
            this.trackingFieldFlag = defaultStringIfNull(baseFormatDetail.getTrackingFieldFlag(),trackingFieldFlag);
            this.trackingFieldOrder = defaultStringIfNull(baseFormatDetail.getTrackingFieldOrder(),trackingFieldOrder);
            this.leftOfDecimalLength = defaultStringIfNull(baseFormatDetail.getLeftOfDecimalLength(),leftOfDecimalLength);
            this.rightOfDecimalLength = defaultStringIfNull(baseFormatDetail.getRightOfDecimalLength(),rightOfDecimalLength);
        }else{
            //TODO Localize
            setFieldName(FileConsolidatorGridConstants.UFD_IGNORE_FIELD_MSG);
            setFieldOrder(String.valueOf(0L));
        }
    }

    protected CompositeFormatDetailVO (){
        generateUniqueId();
        initializeFieldTypeSelectOptions();
    }

    protected static String defaultStringIfNull(Object obj, String defaultString){
        if(obj == null){
            return defaultString;
        }else if (obj instanceof  String){
            return (String)obj;
        } else{
            return String.valueOf(obj);
        }
    }


    private void initializeFieldTypeSelectOptions(){

        Map optionalOption = new HashMap<String,String>();
        optionalOption.put(SELECT_ID_KEY, FileUploadDownloadConstants.OPTIONAL_FLAG);
        optionalOption.put(SELECT_VALUE_KEY,"Optional");
        Map mandatoryOption = new HashMap<String,String>();
        mandatoryOption.put(SELECT_ID_KEY, FileUploadDownloadConstants.MANDATORY_FLAG);
        mandatoryOption.put(SELECT_VALUE_KEY,"Mandatory");

        this.fieldTypeSelectOptions = new ArrayList();
        this.fieldTypeSelectOptions.add(mandatoryOption);
        this.fieldTypeSelectOptions.add(optionalOption);
    }


    public void updateOptionalMandatorySelectedOption() {
        for(Map<String,String> fieldTypeSelectOption : this.fieldTypeSelectOptions){
            if(String.valueOf(fieldTypeSelectOption.get(SELECT_ID_KEY)).equals(String.valueOf(this.compositeMandatoryFlag))){
                fieldTypeSelectOption.put(SELECT_SELECTED_KEY,"Y");
            }
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getBaseFormatId() {
        return baseFormatId;
    }

    public void setBaseFormatId(String baseFormatId) {
        this.baseFormatId = baseFormatId;
    }

    public String getUserFormatId() {
        return userFormatId;
    }

    public void setUserFormatId(String userFormatId) {
        this.userFormatId = userFormatId;
    }

    public String getFormatDetailId() {
        return formatDetailId;
    }

    public void setFormatDetailId(String formatDetailId) {
        this.formatDetailId = formatDetailId;
    }

    public String getRecordIdentifierCode() {
        return recordIdentifierCode;
    }

    public void setRecordIdentifierCode(String recordIdentifierCode) {
        this.recordIdentifierCode = recordIdentifierCode;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public String getGroupOrder() {
        return groupOrder;
    }

    public void setGroupOrder(String groupOrder) {
        this.groupOrder = groupOrder;
    }

    public String getFieldI18Key() {
        return fieldI18Key;
    }

    public void setFieldI18Key(String fieldI18Key) {
        this.fieldI18Key = fieldI18Key;
    }

    public String getDefaultSequence() {
        return defaultSequence;
    }

    public void setDefaultSequence(String defaultSequence) {
        this.defaultSequence = defaultSequence;
    }

    public String getKeyFieldFlag() {
        return keyFieldFlag;
    }

    public void setKeyFieldFlag(String keyFieldFlag) {
        this.keyFieldFlag = keyFieldFlag;
    }

    public String getBaseMandatoryFlag() {
        return baseMandatoryFlag;
    }

    public void setBaseMandatoryFlag(String baseMandatoryFlag) {
        this.baseMandatoryFlag = baseMandatoryFlag;
    }

    public String getFormatMask() {
        return formatMask;
    }

    public void setFormatMask(String formatMask) {
        this.formatMask = formatMask;
    }

    public String getCompositeMandatoryFlag() {
        return compositeMandatoryFlag;
    }

    public void setCompositeMandatoryFlag(String compositeMandatoryFlag) {
        this.compositeMandatoryFlag = compositeMandatoryFlag;
    }

    public String getLength() {
        return length;
    }

    public void setLength(String length) {
        this.length = length;
    }

    public String getFieldOrder() {
        return fieldOrder;
    }

    public void setFieldOrder(String fieldOrder) {
        this.fieldOrder = fieldOrder;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getTrackingFieldFlag() {
        return trackingFieldFlag;
    }

    public void setTrackingFieldFlag(String trackingFieldFlag) {
        this.trackingFieldFlag = trackingFieldFlag;
    }

    public String getTrackingFieldOrder() {
        return trackingFieldOrder;
    }

    public void setTrackingFieldOrder(String trackingFieldOrder) {
        this.trackingFieldOrder = trackingFieldOrder;
    }

    public String getLeftOfDecimalLength() {
        return leftOfDecimalLength;
    }

    public void setLeftOfDecimalLength(String leftOfDecimalLength) {
        this.leftOfDecimalLength = leftOfDecimalLength;
    }

    public String getRightOfDecimalLength() {
        return rightOfDecimalLength;
    }

    public void setRightOfDecimalLength(String rightOfDecimalLength) {
        this.rightOfDecimalLength = rightOfDecimalLength;
    }

    public Boolean getBaseMandatory() {
        return isBaseMandatory;
    }

    public void setBaseMandatory(Boolean baseMandatory) {
        isBaseMandatory = baseMandatory;
    }

    public List getFieldTypeSelectOptions() {
        return fieldTypeSelectOptions;
    }

    public void setFieldTypeSelectOptions(List fieldTypeSelectOptions) {
        this.fieldTypeSelectOptions = fieldTypeSelectOptions;
    }

    public String getBaseFormatDetailId() {
        return baseFormatDetailId;
    }

    public void setBaseFormatDetailId(String baseFormatDetailId) {
        this.baseFormatDetailId = baseFormatDetailId;
    }

    public String getUserFormatDetailId() {
        return userFormatDetailId;
    }

    public void setUserFormatDetailId(String userFormatDetailId) {
        this.userFormatDetailId = userFormatDetailId;
    }

    public String getPaddingStrategyId() {
        return paddingStrategyId;
    }

    public void setPaddingStrategyId(String paddingStrategyId) {
        this.paddingStrategyId = paddingStrategyId;
    }

    public String getBaseRecordIdentifierId() {
        return baseRecordIdentifierId;
    }

    public void setBaseRecordIdentifierId(String baseRecordIdentifierId) {
        this.baseRecordIdentifierId = baseRecordIdentifierId;
    }

    public String getUserRecordIdentifierId() {
        return userRecordIdentifierId;
    }

    public void setUserRecordIdentifierId(String userRecordIdentifierId) {
        this.userRecordIdentifierId = userRecordIdentifierId;
    }

    public String getFieldDescription() {
        return fieldDescription;
    }

    public void setFieldDescription(String fieldDescription) {
        this.fieldDescription = fieldDescription;
    }
}
