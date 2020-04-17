package com.nucleus.core.formsConfiguration;

import static com.nucleus.finnone.pro.general.util.ValidatorUtils.hasElements;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.*;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.annotations.Synonym;
import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.CloneOptions;
import com.nucleus.finnone.pro.general.util.ValidatorUtils;


@Entity
@DynamicUpdate
@DynamicInsert
@Cacheable
@Synonym(grant="ALL")
public class FieldDefinition extends BaseEntity {

    private static final long        serialVersionUID = 1L;

    private String                   fieldKey;

    private String                   itemLabel;

    private String                   itemValue;

    private String                   fieldType;

    private boolean                  mandatoryField;

    private Boolean                   expandableField;

    private String                   binderName;

    private String                   fieldLabel;

    private int                      fieldSequence;

    private int                      fieldDataType;

    private String                   autoCompleteColumnsHolder;

    // It will be true for 'Mobile' and false for 'LandLine' and null for other
    private Boolean                  mobile;

    private String                   emailTypeCode;

    private Integer                  defaultMonth;

    private Integer                  defaultYear;

    private String					 description;

    /**
     * Field for parent field key in cascade select
     */
    private String                   parentFieldKey;

    private String                   urlCascadeSelect;

    /**
     * Field for current child entity name in cascade select
     */
    private String                   activeChildEntityName;

    /**
     * Field Value - Default Value
     */
    @ElementCollection
    @JoinTable(name = "form_field_values")
    @Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
    @OrderColumn(name="order_column")
    private List<String>             fieldValue;

    /**
     * Tool Tip Message
     */
    private String                   toolTipMessage;

    /**
     * added for adding dyanmic questions
     */
    private String                   customeLongMessage;
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @JoinColumn(name = "fk_field_custom_option")
    @OrderBy("customeItemLabel ASC")
    @Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
    private List<FieldCustomOptions> fieldCustomOptionsList;

    /**
     * Entity Name - usage dropdown, radio etc
     */
    private String                   entityName;

    /**
     * Field for Maximum Length allowed
     */
    private Integer                  maxFieldLength;

    /**
     * Field for Minimum Length allowed
     */
    private Integer                  minFieldLength;

    /**
     * Field for Maximum value allowed
     */
    private String                   maxFieldValue;

    /**
     * Field for Minimum Value allowed
     */
    private String                   minFieldValue;

    /**
     * Field to include select in drop down
     */

    private boolean                  includeSelect;

    /**
     *  Field added for button and hyperlink type elements
     */
    @Column(length=4000)
    private String href;

    /**
     * fields for recording logic on screen for a button
     */
    @Column(length=4000)
    private String functionLogic;

    /**
     * authority for hyperlink and button
     */
    private String authority;

    /**
     * Fields added for dependant autocomplete values
     *
     */
    private String parentColumn;

    private String errorMessageCode;

    private String parentFieldId;

    private boolean mainFormDependant;

    @ManyToOne(fetch = FetchType.LAZY)
    private SpecialTable specialTable;
    private String parent;

    private Boolean firstParent;


    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JoinColumn(name = "fk_field_definition")
    @Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
    private List<CustomCascadeOptions> customCascadeOptions;

    private String assignmentMasterCode;
    private Boolean populateAssignmentResult;


    private String associatedFieldKey;
    private Boolean disable = Boolean.FALSE;

    private String lovKey;

    public String getLovKey() {
        return lovKey;
    }

    public void setLovKey(String lovKey) {
        this.lovKey = lovKey;
    }

    public Boolean getDisable() {
        return disable;
    }

    public void setDisable(Boolean disable) {
        if (disable == null) {
            this.disable = Boolean.FALSE;
        }
        this.disable = disable;
    }
    public Boolean getFirstParent() {
        return firstParent;
    }
    public void setFirstParent(Boolean firstParent) {
        if(firstParent==null){
            this.firstParent = Boolean.FALSE;
        }
        this.firstParent = firstParent;
    }

    public String getParent() {
        return parent;
    }

    public void setParent(String parent) {
        this.parent = parent;
    }



    public List<CustomCascadeOptions> getCustomCascadeOptions() {
        return customCascadeOptions;
    }

    public void setCustomCascadeOptions(List<CustomCascadeOptions> customCascadeOptions) {
        this.customCascadeOptions = customCascadeOptions;
    }

    public String getAssociatedFieldKey() {
        return associatedFieldKey;
    }

    public void setAssociatedFieldKey(String associatedFieldKey) {
        this.associatedFieldKey = associatedFieldKey;
    }

    public SpecialTable getSpecialTable() {
        return specialTable;
    }

    public void setSpecialTable(SpecialTable specialTable) {
        this.specialTable = specialTable;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    private ProductSchemeMetaData productSchemeMetaData;

    @Override
    protected void populate(BaseEntity baseEntity, CloneOptions cloneOptions) {
        FieldDefinition fieldDefinition=(FieldDefinition)baseEntity;
        super.populate(fieldDefinition, cloneOptions);
        fieldDefinition.setFieldKey(fieldKey);
        fieldDefinition.setItemLabel(itemLabel);
        fieldDefinition.setItemValue(itemValue);
        fieldDefinition.setFieldType(fieldType);
        fieldDefinition.setMandatoryField(mandatoryField);
        fieldDefinition.setExpandableField(expandableField);
        fieldDefinition.setBinderName(binderName);
        fieldDefinition.setFieldLabel(fieldLabel);
        fieldDefinition.setFieldSequence(fieldSequence);
        fieldDefinition.setFieldDataType(fieldDataType);
        fieldDefinition.setAutoCompleteColumnsHolder(autoCompleteColumnsHolder);
        fieldDefinition.setMobile(mobile);
        fieldDefinition.setEmailTypeCode(emailTypeCode);
        fieldDefinition.setDefaultMonth(defaultMonth);
        fieldDefinition.setDefaultYear(defaultYear);
        fieldDefinition.setDescription(description);
        fieldDefinition.setSpecialTable(specialTable);
        fieldDefinition.setProductSchemeMetaData(productSchemeMetaData);
        if(hasElements(fieldValue))
        {
            List<String> fieldValueList=new ArrayList<String>();
            for(String fieldVal:fieldValue)
            {
                fieldValueList.add(fieldVal);
            }
            fieldDefinition.setValue(fieldValueList);
        }

        fieldDefinition.setToolTipMessage(toolTipMessage);
        fieldDefinition.setCustomeLongMessage(customeLongMessage);

        if (hasElements(fieldCustomOptionsList)) {
            List<FieldCustomOptions> clonedfieldCustomOptionsList = new ArrayList<FieldCustomOptions>();
            for (FieldCustomOptions fieldCustomOption : fieldCustomOptionsList) {
                clonedfieldCustomOptionsList.add((FieldCustomOptions) fieldCustomOption.cloneYourself(cloneOptions));
            }
            fieldDefinition.setFieldCustomOptionsList(clonedfieldCustomOptionsList);
        }
        fieldDefinition.setEntityName(entityName);
        fieldDefinition.setMaxFieldLength(maxFieldLength);
        fieldDefinition.setMinFieldLength(minFieldLength);
        fieldDefinition.setMaxFieldValue(maxFieldValue);
        fieldDefinition.setMinFieldValue(minFieldValue);
        fieldDefinition.setIncludeSelect(includeSelect);
        fieldDefinition.setParentFieldKey(parentFieldKey);
        fieldDefinition.setUrlCascadeSelect(urlCascadeSelect);
        fieldDefinition.setActiveChildEntityName(activeChildEntityName);
        fieldDefinition.setHref(href);
        fieldDefinition.setFunctionLogic(functionLogic);
        fieldDefinition.setAuthority(authority);
        fieldDefinition.setParentColumn(parentColumn);
        fieldDefinition.setErrorMessageCode(errorMessageCode);
        fieldDefinition.setParentFieldId(parentFieldId);
        fieldDefinition.setMainFormDependant(mainFormDependant);
        fieldDefinition.setParent(parent);
        if (hasElements(customCascadeOptions)) {
            List<CustomCascadeOptions> customCascadeOptions = new ArrayList<>();
            for (CustomCascadeOptions customCascadeOption : customCascadeOptions) {
                customCascadeOptions.add((CustomCascadeOptions) customCascadeOption.cloneYourself(cloneOptions));
            }
            fieldDefinition.setCustomCascadeOptions(customCascadeOptions);
        }
        fieldDefinition.setAssociatedFieldKey(associatedFieldKey);
        fieldDefinition.setPopulateAssignmentResult(populateAssignmentResult);
        fieldDefinition.setAssignmentMasterCode(assignmentMasterCode);
        fieldDefinition.setLovKey(lovKey);
    }

    @Override
    protected void populateFrom(BaseEntity baseEntity, CloneOptions cloneOptions) {


        FieldDefinition fieldDefinition=(FieldDefinition)baseEntity;
        super.populateFrom(fieldDefinition, cloneOptions);
        this.setFieldKey(fieldDefinition.getFieldKey());
        this.setItemLabel(fieldDefinition.getItemLabel());
        this.setItemValue(fieldDefinition.getItemValue());
        this.setFieldType(fieldDefinition.getFieldType());
        this.setMandatoryField(fieldDefinition.isMandatoryField());
        this.setExpandableField(fieldDefinition.getExpandableField());
        this.setBinderName(fieldDefinition.getBinderName());
        this.setFieldLabel(fieldDefinition.getFieldLabel());
        this.setFieldSequence(fieldDefinition.getFieldSequence());
        this.setFieldDataType(fieldDefinition.getFieldDataType());
        this.setAutoCompleteColumnsHolder(fieldDefinition.getAutoCompleteColumnsHolder());
        this.setMobile(fieldDefinition.getMobile());
        this.setEmailTypeCode(fieldDefinition.getEmailTypeCode());
        this.setDefaultMonth(fieldDefinition.getDefaultMonth());
        this.setDefaultYear(fieldDefinition.getDefaultYear());
        this.setDescription(fieldDefinition.getDescription());
        this.setSpecialTable(fieldDefinition.getSpecialTable());
        this.setAssociatedFieldKey(fieldDefinition.getAssociatedFieldKey());
        this.setProductSchemeMetaData(fieldDefinition.getProductSchemeMetaData());
        if(hasElements(fieldDefinition.getValue()))
        {
            List<String> fieldValList=new ArrayList<String>();
            for(String fieldVal:fieldDefinition.getValue())
            {
                fieldValList.add(fieldVal);
            }
            this.setValue(fieldValList);
        }
        this.setToolTipMessage(fieldDefinition.getToolTipMessage());
        this.setCustomeLongMessage(fieldDefinition.getCustomeLongMessage());
        if(this.getFieldCustomOptionsList()==null)
            this.setFieldCustomOptionsList(new ArrayList<FieldCustomOptions>());
        if (hasElements(fieldDefinition.getFieldCustomOptionsList())) {

            this.getFieldCustomOptionsList().clear();
            for (FieldCustomOptions fieldCustomOption : fieldDefinition.getFieldCustomOptionsList()) {
                this.getFieldCustomOptionsList().add((FieldCustomOptions) fieldCustomOption.cloneYourself(cloneOptions));
            }
        }
        this.setEntityName(fieldDefinition.getEntityName());
        this.setMaxFieldLength(fieldDefinition.getMaxFieldLength());
        this.setMinFieldLength(fieldDefinition.getMinFieldLength());
        this.setMaxFieldValue(fieldDefinition.getMaxFieldValue());
        this.setMinFieldValue(fieldDefinition.getMaxFieldValue());
        this.setIncludeSelect(fieldDefinition.isIncludeSelect());
        this.setParentFieldKey(fieldDefinition.getParentFieldKey());
        this.setUrlCascadeSelect(fieldDefinition.getUrlCascadeSelect());
        this.setActiveChildEntityName(fieldDefinition.getActiveChildEntityName());
        this.setHref(fieldDefinition.getHref());
        this.setFunctionLogic(fieldDefinition.getFunctionLogic());
        this.setAuthority(fieldDefinition.getAuthority());
        this.setErrorMessageCode(fieldDefinition.getErrorMessageCode());
        this.setParentColumn(fieldDefinition.getParentColumn());
        this.setParentFieldId(fieldDefinition.getParentFieldId());
        this.setMainFormDependant(fieldDefinition.isMainFormDependant());
        this.setParent(fieldDefinition.getParent());
        if (hasElements(fieldDefinition.getCustomCascadeOptions())) {
            this.getCustomCascadeOptions().clear();
            for (CustomCascadeOptions CustomCascadeOption : fieldDefinition.getCustomCascadeOptions()) {
                this.getCustomCascadeOptions().add((CustomCascadeOptions) CustomCascadeOption.cloneYourself(cloneOptions));
            }
        }
        this.setAssignmentMasterCode(fieldDefinition.getAssignmentMasterCode());
        this.setPopulateAssignmentResult(fieldDefinition.getPopulateAssignmentResult());
        this.setLovKey(fieldDefinition.getLovKey());
    }



    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }

    /**
     * @return the fieldLabel
     */
    public String getFieldLabel() {
        return fieldLabel;
    }

    /**
     * @param fieldLabel the fieldLabel to set
     */
    public void setFieldLabel(String fieldLabel) {
        this.fieldLabel = fieldLabel;
    }

    /**
     * @return the fieldKey
     */
    public String getFieldKey() {
        return fieldKey;
    }

    /**
     * @param fieldKey the fieldKey to set
     */
    public void setFieldKey(String fieldKey) {
        this.fieldKey = fieldKey;
    }

    /**
     * @return the itemLabel
     */
    public String getItemLabel() {
        return itemLabel;
    }

    /**
     * @param itemLabel the itemLabel to set
     */
    public void setItemLabel(String itemLabel) {
        this.itemLabel = itemLabel;
    }

    /**
     * @return the fieldType
     */
    public String getFieldType() {
        return fieldType;
    }

    /**
     * @param fieldType the fieldType to set
     */
    public void setFieldType(String fieldType) {
        this.fieldType = fieldType;
    }

    /**
     * @return the mandatoryField
     */
    public boolean isMandatoryField() {
        return mandatoryField;
    }

    /**
     * @param mandatoryField the mandatoryField to set
     */
    public void setMandatoryField(boolean mandatoryField) {
        this.mandatoryField = mandatoryField;
    }

    /**
     * @return the binderName
     */
    public String getBinderName() {
        return binderName;
    }

    /**
     * @param binderName the binderName to set
     */
    public void setBinderName(String binderName) {
        this.binderName = binderName;
    }

    /**
     * @return the fieldValue
     */
    public String getItemValue() {
        return itemValue;
    }

    /**
     * @param itemValue the itemValue to set
     */
    public void setItemValue(String itemValue) {
        this.itemValue = itemValue;
    }

    /**
     * @return the fieldSequence
     */
    public int getFieldSequence() {
        return fieldSequence;
    }

    /**
     * @param fieldSequence the fieldSequence to set
     */
    public void setFieldSequence(int fieldSequence) {
        this.fieldSequence = fieldSequence;
    }

    /**
     * @return the toolTipMessage
     */
    public String getToolTipMessage() {
        return toolTipMessage;
    }

    /**
     * @param toolTipMessage the toolTipMessage to set
     */
    public void setToolTipMessage(String toolTipMessage) {
        this.toolTipMessage = toolTipMessage;
    }

    /**
     * @return the fieldCustomOptionsList
     */
    public List<FieldCustomOptions> getFieldCustomOptionsList() {
        return fieldCustomOptionsList;
    }

    /**
     * @param fieldCustomOptionsList the fieldCustomOptionsList to set
     */
    public void setFieldCustomOptionsList(List<FieldCustomOptions> fieldCustomOptionsList) {
        this.fieldCustomOptionsList = fieldCustomOptionsList;
    }

    /**
     * @return the customeLongMessage
     */
    public String getCustomeLongMessage() {
        return customeLongMessage;
    }

    /**
     * @param customeLongMessage the customeLongMessage to set
     */
    public void setCustomeLongMessage(String customeLongMessage) {
        this.customeLongMessage = customeLongMessage;
    }

    /**
     * @return the entityName
     */
    public String getEntityName() {
        return entityName;
    }

    /**
     * @param entityName the entityName to set
     */
    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    /**
     * @return the fieldDataType
     */
    public int getFieldDataType() {
        return fieldDataType;
    }

    /**
     * @param fieldDataType the fieldDataType to set
     */
    public void setFieldDataType(int fieldDataType) {
        this.fieldDataType = fieldDataType;
    }

    public List<String> getValue() {
        return fieldValue;
    }

    public void setValue(List<String> value) {
        this.fieldValue = value;
    }

    /**
     * @return the maxFieldLength
     */
    public Integer getMaxFieldLength() {
        return maxFieldLength;
    }

    /**
     * @param maxFieldLength the maxFieldLength to set
     */
    public void setMaxFieldLength(Integer maxFieldLength) {
        this.maxFieldLength = maxFieldLength;
    }

    /**
     * @return the minFieldLength
     */
    public Integer getMinFieldLength() {
        return minFieldLength;
    }

    /**
     * @param minFieldLength the minFieldLength to set
     */
    public void setMinFieldLength(Integer minFieldLength) {
        this.minFieldLength = minFieldLength;
    }

    /**
     * @return the maxFieldValue
     */
    public String getMaxFieldValue() {
        return maxFieldValue;
    }

    /**
     * @param maxFieldValue the maxFieldValue to set
     */
    public void setMaxFieldValue(String maxFieldValue) {
        this.maxFieldValue = maxFieldValue;
    }

    /**
     * @return the minFieldValue
     */
    public String getMinFieldValue() {
        return minFieldValue;
    }

    /**
     * @param minFieldValue the minFieldValue to set
     */
    public void setMinFieldValue(String minFieldValue) {
        this.minFieldValue = minFieldValue;
    }

    /**
     * Gets the auto complete columns holder.
     *
     * @return the auto complete columns holder
     */
    public String getAutoCompleteColumnsHolder() {
        return autoCompleteColumnsHolder;
    }

    /**
     * Sets the auto complete columns holder.
     *
     * @param autoCompleteColumnsHolder the new auto complete columns holder
     */
    public void setAutoCompleteColumnsHolder(String autoCompleteColumnsHolder) {
        this.autoCompleteColumnsHolder = autoCompleteColumnsHolder;
    }

    /**
     * Gets the mobile.
     *
     * @return the mobile
     */
    public Boolean getMobile() {
        if(ValidatorUtils.isNull(mobile)){
            return false;
        }
        return mobile;
    }

    /**
     * Sets the mobile.
     *
     * @param mobile the new mobile
     */
    public void setMobile(Boolean mobile) {
        this.mobile = mobile;
    }

    /**
     * Gets the email type code.
     *
     * @return the email type code
     */
    public String getEmailTypeCode() {
        return emailTypeCode;
    }

    /**
     * Sets the email type code.
     *
     * @param emailTypeCode the new email type code
     */
    public void setEmailTypeCode(String emailTypeCode) {
        this.emailTypeCode = emailTypeCode;
    }

    /**
     * @return the defaultMonth
     */
    public Integer getDefaultMonth() {
        return defaultMonth;
    }

    /**
     * @param defaultMonth the defaultMonth to set
     */
    public void setDefaultMonth(Integer defaultMonth) {
        this.defaultMonth = defaultMonth;
    }

    /**
     * @return the defaultYear
     */
    public Integer getDefaultYear() {
        return defaultYear;
    }

    /**
     * @param defaultYear the defaultYear to set
     */
    public void setDefaultYear(Integer defaultYear) {
        this.defaultYear = defaultYear;
    }

    /**
     * @return the includeSelect
     */
    public boolean isIncludeSelect() {
        return includeSelect;
    }

    /**
     * @param includeSelect the includeSelect to set
     */
    public void setIncludeSelect(boolean includeSelect) {
        this.includeSelect = includeSelect;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getParentFieldKey() {
        return parentFieldKey;
    }

    public void setParentFieldKey(String parentFieldKey) {
        this.parentFieldKey = parentFieldKey;
    }

    public String getActiveChildEntityName() {
        return activeChildEntityName;
    }

    public void setActiveChildEntityName(String activeChildEntityName) {
        this.activeChildEntityName = activeChildEntityName;
    }

    /**
     * @return the urlCascadeSelect
     */
    public String getUrlCascadeSelect() {
        return urlCascadeSelect;
    }

    /**
     * @param urlCascadeSelect the url for cascade select to set
     */
    public void setUrlCascadeSelect(String urlCascadeSelect) {
        this.urlCascadeSelect = urlCascadeSelect;
    }

    public String getFunctionLogic() {
        return functionLogic;
    }

    public void setFunctionLogic(String functionLogic) {
        this.functionLogic = functionLogic;
    }

    public String getAuthority() {
        return authority;
    }

    public void setAuthority(String authority) {
        this.authority = authority;
    }

    public String getParentColumn() {
        return parentColumn;
    }

    public void setParentColumn(String parentColumn) {
        this.parentColumn = parentColumn;
    }

    public String getErrorMessageCode() {
        return errorMessageCode;
    }

    public void setErrorMessageCode(String errorMessageCode) {
        this.errorMessageCode = errorMessageCode;
    }

    public String getParentFieldId() {
        return parentFieldId;
    }

    public void setParentFieldId(String parentFieldId) {
        this.parentFieldId = parentFieldId;
    }

    public boolean isMainFormDependant() {
        return mainFormDependant;
    }

    public void setMainFormDependant(boolean mainFormDependant) {
        this.mainFormDependant = mainFormDependant;
    }

    public ProductSchemeMetaData getProductSchemeMetaData() {
        return productSchemeMetaData;
    }

    public void setProductSchemeMetaData(ProductSchemeMetaData productSchemeMetaData) {
        this.productSchemeMetaData = productSchemeMetaData;
    }

    public String getAssignmentMasterCode() {
        return assignmentMasterCode;
    }

    public void setAssignmentMasterCode(String assignmentMasterCode) {
        this.assignmentMasterCode = assignmentMasterCode;
    }

    public Boolean getPopulateAssignmentResult() {
        return populateAssignmentResult;
    }

    public void setPopulateAssignmentResult(Boolean populateAssignmentResult) {
        this.populateAssignmentResult = populateAssignmentResult;
    }

    public Boolean getExpandableField() {
        return expandableField;
    }

    public void setExpandableField(Boolean expandableField) {
        this.expandableField = expandableField;
    }
}
