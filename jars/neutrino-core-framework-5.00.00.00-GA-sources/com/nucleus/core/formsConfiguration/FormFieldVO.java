package com.nucleus.core.formsConfiguration;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;

import com.nucleus.core.formsConfiguration.fieldcomponent.EmailInfoVO;
import com.nucleus.core.formsConfiguration.fieldcomponent.LOVFieldVO;
import com.nucleus.core.formsConfiguration.fieldcomponent.PhoneNumberVO;
import com.nucleus.finnone.pro.general.util.ValidatorUtils;

public class FormFieldVO extends FormComponentVO {

    private static final long          serialVersionUID = 4629251662622504882L;
    private static final Integer MAX_TEXTBOX_LENGTH = 50;
    private static final Integer MAX_TEXTAREA_LENGTH = 255;

    @ApiModelProperty(notes="This field is Id",required=false,dataType="String",hidden=false)
    private String                     id;

    @ApiModelProperty(notes="This field is Item Label",required=false,dataType="String",hidden=false)
    private String                     itemLabel;

    @ApiModelProperty(notes="This field is Item Value",required=false,dataType="String",hidden=false)
    private String                     itemValue;

    @ApiModelProperty(notes="This field is Value",required=false,dataType="List of String",hidden=false)
    private List<String>               value;

    @ApiModelProperty(notes="This field is Field Type",required=false,dataType="String",hidden=false)
    private String                     fieldType;

    @ApiModelProperty(notes="This field is Binder Name",required=false,dataType="String",hidden=false)
    private String                     binderName;

    @ApiModelProperty(notes="This field is Mandatory Field",required=false,dataType="boolean",hidden=false)
    private boolean                    mandatoryField;

    @ApiModelProperty(notes="This field is Expandable Field",required=false,dataType="Boolean",hidden=false)
    private Boolean                    expandableField;

    @ApiModelProperty(notes="This field is Field Sequence",required=false,dataType="int",hidden=false)
    private int                        fieldSequence;

    @ApiModelProperty(notes="This field is Field Data Type",required=false,dataType="int",hidden=false)
    private int                        fieldDataType;

    @ApiModelProperty(notes="This field is Field Label",required=false,dataType="String",hidden=false)
    private String                     fieldLabel;
    /**
     * Tool Tip Message
     */
    @ApiModelProperty(notes="This field is Tool Tip Message",required=false,dataType="String",hidden=false)
    private String                     toolTipMessage;

    /**
     * added for adding dyanmic questions
     */
    @ApiModelProperty(notes="This field is Custome Long Message",required=false,dataType="String",hidden=false)
    private String                     customeLongMessage;

    @ApiModelProperty(notes="This field is Field Custom Options VO List",required=false,dataType="List of FieldCustomOptionsVO",hidden=false)
    private List<FieldCustomOptionsVO> fieldCustomOptionsVOList;

    /**
     * Entity Name - usage dropdown, radio etc
     */
    @ApiModelProperty(notes="This field is Entity Name",required=false,dataType="String",hidden=false)
    private String                     entityName;

    /**
     * Field for Maximum Length allowed
     */
    @ApiModelProperty(notes="This field is Max Field Length",required=false,dataType="Integer",hidden=false)
    private Integer                    maxFieldLength;

    /**
     * Field for Minimum Length allowed
     */
    @ApiModelProperty(notes="This field is Min Field Length",required=false,dataType="Integer",hidden=false)
    private Integer                    minFieldLength;

    /**
     * Field for Maximum value allowed
     */
    @ApiModelProperty(notes="This field is Max Field Value",required=false,dataType="String",hidden=false)
    private String                     maxFieldValue;

    /**
     * Field for Minimum Value allowed
     */
    @ApiModelProperty(notes="This field is Min Field Value",required=false,dataType="String",hidden=false)
    private String                     minFieldValue;

    @ApiModelProperty(notes="This field is Searchable Columns",required=false,dataType="String",hidden=false)
    private String                     searchableColumns;

    @ApiModelProperty(notes="This field is Phone Number VO",required=false,dataType="PhoneNumberVO",hidden=false)
    private PhoneNumberVO              phoneNumberVO;

    @ApiModelProperty(notes="This field is Mobile",required=false,dataType="Boolean",hidden=false)
    private Boolean                    mobile;

    @ApiModelProperty(notes="This field is Email Info VO",required=false,dataType="EmailInfoVO",hidden=false)
    private EmailInfoVO                emailInfoVO;

    @ApiModelProperty(notes="This field is Lov Field VO",required=false,dataType="LOVFieldVO",hidden=false)
    private LOVFieldVO                 lovFieldVO;

    @ApiModelProperty(notes="This field is Def Date",required=false,dataType="String",hidden=false)
    private String                     defDate;

    /**
     * Field to include select in drop down
     */
	@ApiModelProperty(notes="This field is Url Cascade Select",required=false,dataType="String",hidden=false)
	private String urlCascadeSelect;

    @ApiModelProperty(notes="This field is Include Select",required=false,dataType="boolean",hidden=false)
    private boolean                    includeSelect;
    
    /**
     * whether to hide field or not
     */
   // private boolean					   hideField;

    /**
     * to be used only in case of cascade drop down
     */
    @ApiModelProperty(notes="This field is Current Child Entity Name",required=false,dataType="String",hidden=false)
    private String 					   currentChildEntityName;
    
    @ApiModelProperty(notes="This field is Parent Field Key",required=false,dataType="String",hidden=false)
    private String                     parentFieldKey;
    
    /**
     * fields for hyperlink based elements
    */
    @ApiModelProperty(notes="This field is Href",required=false,dataType="String",hidden=false)
    private String href;
   
    /**
     * fields for recording logic on screen for a button
     */
    @ApiModelProperty(notes="This field is Function Logic",required=false,dataType="String",hidden=false)
    private String functionLogic;
    
    /**
     * authority for hyperlink and button
    */
    @ApiModelProperty(notes="This field is Authority",required=false,dataType="String",hidden=false)
    private String authority;

    /**
     * Fields added for dependant autocomplete values
     * 
     */
    @ApiModelProperty(notes="This field is Parent Column",required=false,dataType="String",hidden=false)
    private String parentColumn;
    
    @ApiModelProperty(notes="This field is Error Message Code",required=false,dataType="String",hidden=false)
    private String errorMessageCode;
    
    @ApiModelProperty(notes="This field is Parent Field Id",required=false,dataType="String",hidden=false)
    private String parentFieldId;
    
    @ApiModelProperty(notes="This field is Main Form Dependant",required=false,dataType="boolean",hidden=false)
    private boolean mainFormDependant;

    @ApiModelProperty(notes="This field is Lov Key",required=false,dataType="String",hidden=false)
    private String lovKey;
    
    @ApiModelProperty(notes="This field is Item",required=false,dataType="Object",hidden=false)
    private Object item;
    
    @ApiModelProperty(notes="This field is Product Scheme Meta Data Vo",required=false,dataType="ProductSchemeMetaDataVo",hidden=false)
    private ProductSchemeMetaDataVo productSchemeMetaDataVo;
    
    @ApiModelProperty(notes="This field is Parent",required=false,dataType="String",hidden=false)
    private String parent;
    
    private Boolean disable = Boolean.FALSE;

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

    public String getParent() {
		return parent;
	}

	public void setParent(String parent) {
		this.parent = parent;
	}

	public String getHref() {
		return href;
	}

	public void setHref(String href) {
		this.href = href;
	}

	public FormFieldVO() {
        setType(UI_COMPONENT_TYPE_FIELD);
    }

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return the itemLabel
     */
    public String getItemLabel() {
        return itemLabel;
    }

    /**
     * @param fieldLabel the fieldLabel to set
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
     * @param Type the fieldType to set
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
     * @return the fieldValue
     */
    public String getItemValue() {
        return itemValue;
    }

    /**
     * @param fieldValue the fieldValue to set
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
     * @return the fieldCustomOptionsVOList
     */
    public List<FieldCustomOptionsVO> getFieldCustomOptionsVOList() {
        return fieldCustomOptionsVOList;
    }

    /**
     * @param fieldCustomOptionsVOList the fieldCustomOptionsVOList to set
     */
    public void setFieldCustomOptionsVOList(List<FieldCustomOptionsVO> fieldCustomOptionsVOList) {
        this.fieldCustomOptionsVOList = fieldCustomOptionsVOList;
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

    public List<String> getValue() {
        return value;
    }

    public void setValue(List<String> value) {
        this.value = value;
    }

    /**
     * @return the maxFieldLength
     */
    public Integer getMaxFieldLength() {
    	if(FormComponentType.TEXT_AREA.equals(fieldType)
    			&& (ValidatorUtils.isNull(maxFieldLength))){
    		return MAX_TEXTAREA_LENGTH;
    		
    	}else if(FormComponentType.TEXT_BOX.equals(fieldType)
    			&& (ValidatorUtils.isNull(maxFieldLength) || maxFieldLength > MAX_TEXTBOX_LENGTH)){
    		return MAX_TEXTBOX_LENGTH;
    	}
    	
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
     * Gets the searchable columns.
     *
     * @return the searchable columns
     */
    public String getSearchableColumns() {
        return searchableColumns;
    }

    /**
     * Sets the searchable columns.
     *
     * @param searchableColumns the new searchable columns
     */
    public void setSearchableColumns(String searchableColumns) {
        this.searchableColumns = searchableColumns;
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
     @ApiModelProperty(notes="This field is Mobile",required=false,dataType="Boolean",hidden=false)
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
     * @return the defDate
     */
    public String getDefDate() {
        return defDate;
    }

    /**
     * @param defDate the defDate to set
     */
    public void setDefDate(String defDate) {
        this.defDate = defDate;
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
     * @return the hideField
     */
	/*public boolean isHideField() {
		return hideField;
	}

	*//**
     * @param hideField the hideField to set
     *//*
	public void setHideField(boolean hideField) {
		this.hideField = hideField;
	}*/

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

	public String getUrlCascadeSelect() {
		return urlCascadeSelect;
	}

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

	public ProductSchemeMetaDataVo getProductSchemeMetaDataVo() {
		return productSchemeMetaDataVo;
	}

	public void setProductSchemeMetaDataVo(ProductSchemeMetaDataVo productSchemeMetaDataVo) {
		this.productSchemeMetaDataVo = productSchemeMetaDataVo;
	}

	public Object getItem() {
		return item;
	}
	
	public void setItem(Object item) {
		this.item = (Object)item;
	}	
}
