package com.nucleus.core.formsConfiguration;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.nucleus.core.formsConfiguration.fieldcomponent.LOVFieldVO;
import org.apache.commons.lang3.StringUtils;

import com.nucleus.core.formsConfiguration.fieldcomponent.EmailInfoVO;
import com.nucleus.core.formsConfiguration.fieldcomponent.PhoneNumberVO;

public class FormContainerVO implements Serializable {

    private static final long          serialVersionUID = -4681782913039549552L;

    /**
     * Container Type - Table, Panel, Field, Group
     */
    private int                        type;

    /**
     * List of form container
     */
    private List<FormContainerVO>      formContainerVOList;

    /**
     * Field Key
     */
    private String                     fieldKey;

    /**
     * Key to find the component in page
     */
    private String                     componentDisplayKey;

    /**
     * Field Label
     */
    private String                     fieldLabel;

    /**
     * Field Value - Default Value
     */
    private List<String>               defaultValue;

    /**
     * Type of field - say dropdown, textbox etc
     */
    private String                     fieldType;

    /**
     * Entity Name - usage dropdown, radio etc
     */
    private String                     entityName;

    /**
     * Binder name - usage drop down , radio
     */
    private String                     binderName;

    /**
     * Is field Mandatory
     */
    private boolean                    mandatoryField;

    /**
     * Is field Expandable
     */
    private Boolean                    expandableField = true;

    /**
     * Field Data Type - Reference, String etc - for validation
     */
    private Integer                    fieldDataType;

    /**
     * Tool Tip Message
     */
    private String                     toolTipMessage;

    /**
     * Eror MEssage - Added by user to be displayed while validating
     */
    private String                     fieldValidationErrorMessage;

    /**
     * panel name
     */
    private String                     panelName;

    /**
     * panel header
     */
    private String                     panelHeader;
    /**
     * description of any type of field or panel
     */
    private String                     description;
    /**
     * value to hole item value for drop down and radio buttons
     */
    private String                     itemValue;
    /**
     * value to hole item label for drop down and radio buttons
     */
    private String                     itemLabel;
    /**
     * value used to decide panel layout i.e one column or two column layout
     */
    private int                        panelColumnLayout;

    /**
     * This list will hold the dynamic options
     */
    private List<FieldCustomOptionsVO> fieldCustomOptionsList;

    /**
     * added for adding dynamic questions
     */
    private String                     customeLongMessage;

    /**
     * added for storing model name for tables
     */
    private String                     modelName;

    /**
     * Added to store the model id for edit mode
     */
    private Long                       modelId;

    /**
     * Field for Maximum Length allowed
     */
    private Integer                    maxFieldLength;

    /**
     * Field for Minimum Length allowed
     */
    private Integer                    minFieldLength;

    /**
     * Field for Maximum value allowed
     */
    private String                     maxFieldValue;

    /**
     * Field for Minimum Value allowed
     */
    private String                     minFieldValue;

    private List<String>               fieldValue;

    private List<String>               autoCompleteColumnsHolder;

    private PhoneNumberVO              phoneNumberVO;

    private Boolean                    mobile;

    private EmailInfoVO                emailInfoVO;

    private LOVFieldVO                 lovFieldVO;

    private Integer                    defaultMonth;

    private Integer                    defaultYear;

    private boolean                    includeSelect;
    
    /**
     * to be used only in case of cascade drop down
     */
    private String 					   currentChildEntityName;
    
    private String                     previousChildEntityName;
    
    private String sourceFormName;
    
    /**
     * to be used only in case of cascade drop down
     */
    private String                     parentFieldKey;

    private String                     urlCascadeSelect;
    
    private Long 				      parentId;

	/**
     * fields for hyperlink based elements
    */
    private String href;
    
    /**
     * fields for recording logic on screen for a button
     */
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
    
    private String specialTable;

    private String[] specialColumns;
	
	private String associatedFieldKey;
	
	private String previousKey;

	private String[] partyRoles;

	private String lovKey;

    public String getLovKey() {
        return lovKey;
    }

    public void setLovKey(String lovKey) {
        this.lovKey = lovKey;
    }

   public String getAssociatedFieldKey() {
        return associatedFieldKey;
    }

    public void setAssociatedFieldKey(String associatedFieldKey) {
        this.associatedFieldKey = associatedFieldKey;
    }

    private Boolean allowPanelSave;

    public Boolean getAllowPanelSave() {
        return allowPanelSave;
    }

   public void setAllowPanelSave(Boolean allowPanelSave) {
        this.allowPanelSave = allowPanelSave;
    }

    public String[] getSpecialColumns() {
        return specialColumns;
    }

    public void setSpecialColumns(String[] specialColumns) {
        this.specialColumns = specialColumns;
    }

    public String getSpecialTable() {
        return specialTable;
    }

    public void setSpecialTable(String specialTable) {
        this.specialTable = specialTable;
    }
    
    private String productScheme;
    private String parent;
    private Boolean firstParent=Boolean.FALSE;
	private List<ParentChildForm> parentChildForms;

	private Boolean disable = Boolean.FALSE;

	private Boolean populateAssignmentResult;

	private String assignmentName;

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

	public String getHref() {
		return href;
	}

	public void setHref(String href) {
		this.href = href;
	}
    
	public Long getParentId() {
		return parentId;
	}

	public void setParentId(Long parentId) {
		this.parentId = parentId;
	}

	public String getSourceFormName() {
		return sourceFormName;
	}

	public void setSourceFormName(String sourceFormName) {
		this.sourceFormName = sourceFormName;
	}

	/**
     * @return the type
     */
    public int getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @param type the type to set
     */
    public void setType(int type) {
        this.type = type;
    }

    /**
     * @return the formContainerVOList
     */
    public List<FormContainerVO> getFormContainerVOList() {
        return formContainerVOList;
    }

    /**
     * @param formContainerVOList the formContainerVOList to set
     */
    public void setFormContainerVOList(List<FormContainerVO> formContainerVOList) {
        this.formContainerVOList = formContainerVOList;
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
     * @return the componentDisplayKey
     */
    public String getComponentDisplayKey() {
        return componentDisplayKey;
    }

    /**
     * @param componentDisplayKey the componentDisplayKey to set
     */
    public void setComponentDisplayKey(String componentDisplayKey) {
        this.componentDisplayKey = componentDisplayKey;
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
     * @return the defaultValue
     */
    public List<String> getDefaultValue() {
        return defaultValue;
    }

    /**
     * @param defaultValue the defaultValue to set
     */
    public void setDefaultValue(List<String> defaultValue) {
        this.defaultValue = defaultValue;
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
     * @return the expandableField
     */
    public Boolean getExpandableField() {
        return expandableField;
    }

    /**
     * @param expandableField the expandableField to set
     */
    public void setExpandableField(Boolean expandableField) {
        this.expandableField = expandableField;
    }

    /**
     * @return the fieldDataType
     */
    public Integer getFieldDataType() {
        return fieldDataType;
    }

    /**
     * @param fieldDataType the fieldDataType to set
     */
    public void setFieldDataType(Integer fieldDataType) {
        this.fieldDataType = fieldDataType;
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
     * @return the fieldValidationErrorMessage
     */
    public String getFieldValidationErrorMessage() {
        return fieldValidationErrorMessage;
    }

    /**
     * @param fieldValidationErrorMessage the fieldValidationErrorMessage to set
     */
    public void setFieldValidationErrorMessage(String fieldValidationErrorMessage) {
        this.fieldValidationErrorMessage = fieldValidationErrorMessage;
    }

    /**
     * @return the panelName
     */
    public String getPanelName() {
        return panelName;
    }

    /**
     * @param panelName the panelName to set
     */
    public void setPanelName(String panelName) {
        this.panelName = panelName;
    }

    /**
     * @return the panelHeader
     */
    public String getPanelHeader() {
        return panelHeader;
    }

    /**
     * @param panelHeader the panelHeader to set
     */
    public void setPanelHeader(String panelHeader) {
        this.panelHeader = panelHeader;
    }

    /**
     * @return
     */
    public String getItemValue() {
        return itemValue;
    }

    /**
     * @param itemValue
     */
    public void setItemValue(String itemValue) {
        this.itemValue = itemValue;
    }

    /**
     * @return
     */
    public String getItemLabel() {
        return itemLabel;
    }

    /**
     * @param itemLabel
     */
    public void setItemLabel(String itemLabel) {
        this.itemLabel = itemLabel;
    }

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    /**
     * @return the panelColumnLayout
     */
    public int getPanelColumnLayout() {
        return panelColumnLayout;
    }

    /**
     * @param panelColumnLayout the panelColumnLayout to set
     */
    public void setPanelColumnLayout(int panelColumnLayout) {
        this.panelColumnLayout = panelColumnLayout;
    }

    /**
     * @return the fieldCustomOptionsList
     */
    public List<FieldCustomOptionsVO> getFieldCustomOptionsList() {
        return fieldCustomOptionsList;
    }

    /**
     * @param fieldCustomOptionsList the fieldCustomOptionsList to set
     */
    public void setFieldCustomOptionsList(List<FieldCustomOptionsVO> fieldCustomOptionsList) {
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
     * @return the modelName
     */
    public String getModelName() {
        return modelName;
    }

    /**
     * @param modelName the modelName to set
     */
    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    /**
     * @return the modelId
     */
    public Long getModelId() {
        return modelId;
    }

    /**
     * @param modelId the modelId to set
     */
    public void setModelId(Long modelId) {
        this.modelId = modelId;
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
     * Gets the field value.
     *
     * @return the field value
     */
    public List<String> getFieldValue() {
        return fieldValue;
    }

    /**
     * Sets the field value.
     *
     * @param fieldValue the new field value
     */
    public void setFieldValue(List<String> fieldValue) {
        this.fieldValue = fieldValue;
    }

    /**
     * Gets the auto complete columns holder.
     *
     * @return the auto complete columns holder
     */
    public List<String> getAutoCompleteColumnsHolder() {
        return autoCompleteColumnsHolder;
    }

    /**
     * Sets the auto complete columns holder.
     *
     * @param autoCompleteColumnsHolder the new auto complete columns holder
     */
    public void setAutoCompleteColumnsHolder(String autoCompleteColumnsHolder) {
        if (StringUtils.isNotBlank(autoCompleteColumnsHolder)) {
            this.autoCompleteColumnsHolder = Arrays.asList(autoCompleteColumnsHolder.split(","));
        } else {
            this.autoCompleteColumnsHolder = null;
        }
    }

    /**
     * Gets the phone number vo.
     *
     * @return the phone number vo
     */
    public PhoneNumberVO getPhoneNumberVO() {
        return phoneNumberVO;
    }

    /**
     * Sets the phone number vo.
     *
     * @param phoneNumberVO the new phone number vo
     */
    public void setPhoneNumberVO(PhoneNumberVO phoneNumberVO) {
        this.phoneNumberVO = phoneNumberVO;
    }

    /**
     * Gets the mobile.
     *
     * @return the mobile
     */
    public Boolean getMobile() {
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
     * Gets the email info vo.
     *
     * @return the email info vo
     */
    public EmailInfoVO getEmailInfoVO() {
        return emailInfoVO;
    }

    /**
     * Sets the email info vo.
     *
     * @param emailInfoVO the new email info vo
     */
    public void setEmailInfoVO(EmailInfoVO emailInfoVO) {
        this.emailInfoVO = emailInfoVO;
    }

    public LOVFieldVO getLovFieldVO() {
        return lovFieldVO;
    }

    public void setLovFieldVO(LOVFieldVO lovFieldVO) {
        this.lovFieldVO = lovFieldVO;
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

	/**
     * @return the currentChildEntityName
     */
	public String getCurrentChildEntityName() {
		return currentChildEntityName;
	}

	/**
     * @param currentChildEntityName the currentChildEntityName to set
     */
	public void setCurrentChildEntityName(String currentChildEntityName) {
		this.currentChildEntityName = currentChildEntityName;
	}

	/**
     * @return the previousChildEntityName
     */
	public String getPreviousChildEntityName() {
		return previousChildEntityName;
	}

	/**
     * @param previousChildEntityName the previousChildEntityName to set
     */
	public void setPreviousChildEntityName(String previousChildEntityName) {
		this.previousChildEntityName = previousChildEntityName;
	}

	/**
     * @return the parentFieldKey
     */
	public String getParentFieldKey() {
		return parentFieldKey;
	}

	/**
     * @param parentFieldKey the parentFieldKey to set
     */
	public void setParentFieldKey(String parentFieldKey) {
		this.parentFieldKey = parentFieldKey;
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

	public String getProductScheme() {
		return productScheme;
	}

	public void setProductScheme(String productScheme) {
		this.productScheme = productScheme;
	}

	public String getParent() {
		return parent;
	}

	public void setParent(String parent) {
		this.parent = parent;
	}

	public List<ParentChildForm> getParentChildForms() {
		return parentChildForms;
	}

	public void setParentChildForms(List<ParentChildForm> parentChildForms) {
		this.parentChildForms = parentChildForms;
	}

    public Boolean getPopulateAssignmentResult() {
        return populateAssignmentResult;
    }

    public void setPopulateAssignmentResult(Boolean populateAssignmentResult) {
        this.populateAssignmentResult = populateAssignmentResult;
    }

    public String getAssignmentName() {
        return assignmentName;
    }

    public void setAssignmentName(String assignmentName) {
        this.assignmentName = assignmentName;
    }

    public String getPreviousKey() {
        return previousKey;
    }

    public void setPreviousKey(String previousKey) {
        this.previousKey = previousKey;
    }

    public String[] getPartyRoles() {
        return partyRoles;
    }

    public void setPartyRoles(String[] partyRoles) {
        this.partyRoles = partyRoles;
    }
}
