package com.nucleus.core.formsConfiguration;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UIMetaDataVo implements Serializable {

    private static final long     serialVersionUID = 197670120275652785L;

    @ApiModelProperty(notes="This field is Form Name",required=false,dataType="String",hidden=false)
    private String                formName;

    @ApiModelProperty(notes="This field is Model Name",required=false,dataType="String",hidden=false)
    private String                modelName;

    @ApiModelProperty(notes="This field is Form Header",required=false,dataType="String",hidden=false)
    private String                formHeader;

    @ApiModelProperty(notes="This field is Allow Save Option",required=false,dataType="Boolean",hidden=false)
    private Boolean               allowSaveOption;

    @ApiModelProperty(notes="This field is Allow Border",required=false,dataType="Boolean",hidden=false)
    private Boolean               allowBorder;

    @ApiModelProperty(notes="This field is Formuuid",required=false,dataType="String",hidden=false)
    private String                formuuid;

    @ApiModelProperty(notes="This field is Form Version",required=false,dataType="String",hidden=false)
    private String                formVersion;

    @ApiModelProperty(notes="This field is Ui Components",required=false,dataType="List of FormComponentVO",hidden=false)
    private List<FormComponentVO> uiComponents;
    @ApiModelProperty(notes="This field is Form Uri",required=false,dataType="String",hidden=false)
    private String                formUri;

    @ApiModelProperty(notes="This field is Model Uri",required=false,dataType="String",hidden=false)
    private String                modelUri;

    @ApiModelProperty(notes="This field is Form Title",required=false,dataType="String",hidden=false)
    private String                formTitle;

    /**
     * used only in offline template
     */
    @ApiModelProperty(notes="This field is Package Name",required=false,dataType="String",hidden=false)
    private String                packageName;

    /**
     * used only in offline template
     */
    @ApiModelProperty(notes="This field is Entity Uri",required=false,dataType="String",hidden=false)
    private String                entityUri;

    /**
     * used only in offline template
     */
    @ApiModelProperty(notes="This field is Entity Id",required=false,dataType="String",hidden=false)
    private String                entityId;

    private Boolean formViewMode=Boolean.FALSE;
    
    @ApiModelProperty(notes="This field is Place Holder ID",required=false,dataType="Long",hidden=false)
    private Long                placeHolderID;
    
    private Map<String,List<String>> defaultValuesMap = new HashMap<String, List<String>>();
    
    @ApiModelProperty(notes="This field is Validation JS",required=false,dataType="String",hidden=false)
    private String validationJS;
    
    /**
     * @return the entityId
     */
    public String getEntityId() {
        return entityId;
    }

    /**
     * @param entityId the entityId to set
     */
    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    /**
     * @return the packageName
     */
    public String getPackageName() {
        return packageName;
    }

    /**
     * @param packageName the packageName to set
     */
    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    /**
     * @return the entityUri
     */
    public String getEntityUri() {
        return entityUri;
    }

    /**
     * @param entityUri the entityUri to set
     */
    public void setEntityUri(String entityUri) {
        this.entityUri = entityUri;
    }

    /**
     * @return the formTitle
     */
    public String getFormTitle() {
        return formTitle;
    }

    /**
     * @param formTitle the formTitle to set
     */
    public void setFormTitle(String formTitle) {
        this.formTitle = formTitle;
    }

    /**
     * @return the formuuid
     */
    public String getFormuuid() {
        return formuuid;
    }

    /**
     * @param formuuid the formuuid to set
     */
    public void setFormuuid(String formuuid) {
        this.formuuid = formuuid;
    }

    /**
     * @return the formVersion
     */
    public String getFormVersion() {
        return formVersion;
    }

    /**
     * @param formVersion the formVersion to set
     */
    public void setFormVersion(String formVersion) {
        this.formVersion = formVersion;
    }

    /**
     * @return the uiComponents
     */
    public List<FormComponentVO> getUiComponents() {
        return uiComponents;
    }

    /**
     * @param uiComponents the uiComponents to set
     */
    public void setUiComponents(List<FormComponentVO> uiComponents) {
        this.uiComponents = uiComponents;
    }

    public void addToUIComponents(FormComponentVO component) {
        if (uiComponents == null) {
            uiComponents = new ArrayList<FormComponentVO>();
        }

        uiComponents.add(component);
    }

    /**
     * @return the formName
     */
    public String getFormName() {
        return formName;
    }

    /**
     * @param formName the formName to set
     */
    public void setFormName(String formName) {
        this.formName = formName;
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
     * @return the formHeader
     */
    public String getFormHeader() {
        return formHeader;
    }

    /**
     * @param formHeader the formHeader to set
     */
    public void setFormHeader(String formHeader) {
        this.formHeader = formHeader;
    }

    public Boolean getAllowSaveOption() {
        return allowSaveOption;
    }

    public void setAllowSaveOption(Boolean allowSaveOption) {
        this.allowSaveOption = allowSaveOption;
    }

    /**
     * @return the formUri
     */
    public String getFormUri() {
        return formUri;
    }

    /**
     * @param formUri the formUri to set
     */
    public void setFormUri(String formUri) {
        this.formUri = formUri;
    }

    /**
     * @return the modelUri
     */
    public String getModelUri() {
        return modelUri;
    }

    /**
     * @param modelUri the modelUri to set
     */
    public void setModelUri(String modelUri) {
        this.modelUri = modelUri;
    }

    /**
     * @return the allowBorder
     */
    public Boolean getAllowBorder() {
        return allowBorder;
    }

    /**
     * @param allowBorder the allowBorder to set
     */
    public void setAllowBorder(Boolean allowBorder) {
        this.allowBorder = allowBorder;
    }

	public Boolean getFormViewMode() {
		return formViewMode;
	}

	public void setFormViewMode(Boolean formViewMode) {
		this.formViewMode = formViewMode;
	}

	public Map<String, List<String>> getDefaultValuesMap() {
		return defaultValuesMap;
	}

	public void setDefaultValuesMap(Map<String, List<String>> defaultValuesMap) {
		this.defaultValuesMap = defaultValuesMap;
	}

	public Long getPlaceHolderID() {
		return placeHolderID;
	}

	public void setPlaceHolderID(Long placeHolderID) {
		this.placeHolderID = placeHolderID;
	}

	public String getValidationJS() {
		return validationJS;
	}

	public void setValidationJS(String validationJS) {
		this.validationJS = validationJS;
	}

	
}
