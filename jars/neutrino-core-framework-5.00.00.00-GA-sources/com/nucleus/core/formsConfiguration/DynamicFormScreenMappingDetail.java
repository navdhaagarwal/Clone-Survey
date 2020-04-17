package com.nucleus.core.formsConfiguration;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Transient;


import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.annotations.Synonym;
import com.nucleus.finnone.pro.general.util.ValidatorUtils;
import com.nucleus.master.BaseMasterEntity;

@Entity
@DynamicUpdate
@DynamicInsert
@Table(name = "DYNAMIC_FORM_SCREEN_MAP_DTL")
@NamedQuery(name="getDynamicFormsMappedToScreenId",query="select a from DynamicFormScreenMapping b,DynamicFormScreenMappingDetail a " +
        "                where b.id=a.dynamicFormMappingId and b.screenIdValue=:screenId " +
        "                and b.sourceProductId=:sourceProductId " +
        "                and a.productTypes is null " +
        "                and b.masterLifeCycleData.approvalStatus in (:approvalStatusList)"+
        "                AND b.activeFlag = true"+
        "                and a.formConfigurationMapping.masterLifeCycleData.approvalStatus in (:approvalStatusList)"+
        "                AND a.formConfigurationMapping.activeFlag = true"+
        "				 order by a.formSequence")
@Synonym(grant="SELECT")

public class DynamicFormScreenMappingDetail extends BaseMasterEntity {

    private static final long serialVersionUID = 1L;

    @Column(name = "form_configuration_mapping")
    private Long formConfigValue;

    @Column(name = "DYNAMIC_FORM_SCREEN_MAP_ID")
    private Long dynamicFormMappingId;

    @Column(name = "SCREEN_ID")
    private Long screenIdValue;

    private String productTypes;

    @Transient
    public Long [] productTypeList;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "form_configuration_mapping", updatable = false, insertable = false)
    private FormConfigurationMapping formConfigurationMapping;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SCREEN_ID", referencedColumnName = "ID", updatable = false, insertable = false)
    private ScreenId screenId;

    @Column(name = "EDIT_MODE")
    private Boolean editModeEnabled = Boolean.FALSE;

    @Column(name = "FORM_SEQUENCE", nullable = false)
    private Integer formSequence = 0;


    public Long getDynamicFormMappingId() {
		return dynamicFormMappingId;
	}

	public void setDynamicFormMappingId(Long dynamicFormMappingId) {
		this.dynamicFormMappingId = dynamicFormMappingId;
	}

	public FormConfigurationMapping getFormConfigurationMapping() {
        return formConfigurationMapping;
    }

    public void setFormConfigurationMapping(
            FormConfigurationMapping formConfigurationMapping) {
        this.formConfigurationMapping = formConfigurationMapping;
    }

    public Boolean getEditModeEnabled() {

    	if(ValidatorUtils.isNull(editModeEnabled)){
    		return false;
    	}
        return editModeEnabled;
    }

    public void setEditModeEnabled(Boolean editModeEnabled) {
        this.editModeEnabled = editModeEnabled;
    }

    public ScreenId getScreenId() {
        return screenId;
    }

    public void setScreenId(ScreenId screenId) {
        this.screenId = screenId;
    }

    public Long getFormConfigValue() {
        return formConfigValue;
    }

    public void setFormConfigValue(Long formConfigValue) {
        this.formConfigValue = formConfigValue;
    }

    public Long getScreenIdValue() {
        return screenIdValue;
    }

    public void setScreenIdValue(Long screenIdValue) {
        this.screenIdValue = screenIdValue;
    }

	public Integer getFormSequence() {
		return formSequence;
	}

	public void setFormSequence(Integer formSequence) {
		this.formSequence = formSequence;
	}


	public String getProductTypes() {
        return this.productTypes;
    }

    public void setProductTypes(String productTypes) {
        this.productTypes = productTypes;
    }

    public Long[] getProductTypeList() {
        return productTypeList;
    }

    public void setProductTypeList(Long[] productTypeList) {
        this.productTypeList = productTypeList;
    }

}
