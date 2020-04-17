package com.nucleus.core.formsConfiguration;

import static com.nucleus.finnone.pro.general.util.ValidatorUtils.hasElements;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderBy;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;

import com.nucleus.master.BaseMasterEntity;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.annotations.Synonym;
import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.CloneOptions;
import com.nucleus.finnone.pro.general.util.ValidatorUtils;
import com.nucleus.rules.model.SourceProduct;

@Entity
@DynamicUpdate
@DynamicInsert
@Cacheable
@Synonym(grant="ALL")
@Table(indexes = {@Index(name="formuuid_index",columnList="formuuid"),@Index(name="DYNAMIC_COLL_DEDUPE_CONFIG_IDX",columnList="DYNAMIC_COLL_DEDUPE_CONFIG")})
public class UIMetaData extends BaseMasterEntity {

    private static final long     serialVersionUID = 1300037238127091606L;

    private String                formName;

    private String                formHeader;

    private String                formTitle;

    private String                formDescription;

    private String                modelName;

    private Boolean               allowSaveOption;

    private Boolean               allowBorder;

    private Boolean               createNewVersion;

    private String                formuuid;

    private String                formVersion;

    @OrderBy("panelSequence")
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "ui_panel_def_fk")
    @Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
    private List<PanelDefinition> panelDefinitionList;
    
    @ManyToOne
    @JoinColumn(name = "SOURCE_PRODUCT_ID", referencedColumnName = "ID", updatable = false, insertable = false)
    private SourceProduct sourceProduct;

    @Column(name = "SOURCE_PRODUCT_ID")
    private Long sourceProductId;

    private String                modelUri;
    
    @Lob
    @Column(name="VALIDATION_RULES_IN_JSON")
    private String formValidationsRulesInJSON;
    
    @Lob
    @Column(name="VALIDATION_RULES_IN_JS")
    private String formValidationRulesInJS;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "ui_dynamic_form_mapper_fk")
    private List<DynamicFormMapper> dynamicFormMapperList;
    
    @OneToOne(cascade = CascadeType.ALL)    
    @JoinColumn(name="DYNAMIC_COLL_DEDUPE_CONFIG")
    private DynamicCollDedupeConfig dynamicCollDedupeConfig;

    
    @Override
    protected void populate(BaseEntity baseEntity, CloneOptions cloneOptions) {
    	UIMetaData uIMetaData=(UIMetaData)baseEntity;
        super.populate(uIMetaData, cloneOptions);
        uIMetaData.setFormName(formName);
        uIMetaData.setFormHeader(formHeader);
        uIMetaData.setFormTitle(formTitle);
        uIMetaData.setFormDescription(formDescription);
        uIMetaData.setModelName(modelName);
        uIMetaData.setAllowSaveOption(allowSaveOption);
        uIMetaData.setAllowBorder(allowBorder);
        uIMetaData.setCreateNewVersion(createNewVersion);
        uIMetaData.setFormuuid(formuuid);
        uIMetaData.setFormVersion(formVersion);
        uIMetaData.setSourceProduct(sourceProduct);
        uIMetaData.setSourceProductId(sourceProductId);
        uIMetaData.setModelUri(modelUri);
        uIMetaData.setFormValidationRulesInJS(formValidationRulesInJS);
        uIMetaData.setFormValidationsRulesInJSON(formValidationsRulesInJSON);
        uIMetaData.setDynamicCollDedupeConfig(dynamicCollDedupeConfig);

        if (hasElements(panelDefinitionList)) {
        	List<PanelDefinition> clonedPanelDefinitionList = new ArrayList<PanelDefinition>();
            for (PanelDefinition fieldCustomOption : panelDefinitionList) {
            	clonedPanelDefinitionList.add((PanelDefinition) fieldCustomOption.cloneYourself(cloneOptions));
            }
            uIMetaData.setPanelDefinitionList(clonedPanelDefinitionList);
        }

        if(hasElements(dynamicFormMapperList)) {
            List<DynamicFormMapper> cloneDynamicFormMapperList = new ArrayList<>();
            for (DynamicFormMapper dynamicFormMapper : dynamicFormMapperList) {
                cloneDynamicFormMapperList.add((DynamicFormMapper)dynamicFormMapper.cloneYourself(cloneOptions));
            }
            uIMetaData.setDynamicFormMapperList(cloneDynamicFormMapperList);
        }
    }

    @Override
    protected void populateFrom(BaseEntity baseEntity, CloneOptions cloneOptions) {
    	
    	UIMetaData uIMetaData=(UIMetaData)baseEntity;
        super.populateFrom(uIMetaData, cloneOptions);
        this.setFormName(uIMetaData.getFormName());
        this.setFormHeader(uIMetaData.getFormHeader());
        this.setFormTitle(uIMetaData.getFormTitle());
        this.setFormDescription(uIMetaData.getFormDescription());
        this.setModelName(uIMetaData.getModelName());
        this.setAllowSaveOption(uIMetaData.getAllowSaveOption());
        this.setAllowBorder(uIMetaData.getAllowBorder());
        this.setCreateNewVersion(uIMetaData.getCreateNewVersion());
        this.setFormuuid(uIMetaData.getFormuuid());
        this.setFormVersion(uIMetaData.getFormVersion());
        this.setSourceProduct(uIMetaData.getSourceProduct());
        this.setSourceProductId(uIMetaData.getSourceProductId());
        this.setModelUri(uIMetaData.getModelUri());
        this.setFormValidationRulesInJS(uIMetaData.getFormValidationRulesInJS());
        this.setFormValidationsRulesInJSON(uIMetaData.getFormValidationsRulesInJSON());
        this.setDynamicCollDedupeConfig(uIMetaData.getDynamicCollDedupeConfig());
        if(this.getPanelDefinitionList()==null)
        {
        	this.setPanelDefinitionList(new ArrayList<PanelDefinition>());
        }
        if (hasElements(uIMetaData.getPanelDefinitionList())) {
        	
            this.getPanelDefinitionList().clear();
            for (PanelDefinition panelDefinition : uIMetaData.getPanelDefinitionList()) {
            	this.getPanelDefinitionList().add((PanelDefinition) panelDefinition.cloneYourself(cloneOptions));
            }
        }
        if(hasElements(uIMetaData.getDynamicFormMapperList())) {
            for (DynamicFormMapper dynamicFormMapper : uIMetaData.getDynamicFormMapperList()) {
                this.getDynamicFormMapperList().add(
                        dynamicFormMapper != null ? (DynamicFormMapper) dynamicFormMapper.cloneYourself(cloneOptions) : null);
            }
        }
    }
    
        
    
    
    
    public SourceProduct getSourceProduct() {
        return sourceProduct;
    }

    public void setSourceProduct(SourceProduct sourceProduct) {
        this.sourceProduct = sourceProduct;
    }
    
    public Long getSourceProductId() {
        return sourceProductId;
    }

    public void setSourceProductId(Long sourceProductId) {
        this.sourceProductId = sourceProductId;
    }



    /**
     * @return the createNewVersion
     */
    public Boolean getCreateNewVersion() {
        return createNewVersion;
    }

    /**
     * @param createNewVersion the createNewVersion to set
     */
    public void setCreateNewVersion(Boolean createNewVersion) {
        this.createNewVersion = createNewVersion;
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
     * @return the panelDefinitionList
     */
    public List<PanelDefinition> getPanelDefinitionList() {
        return panelDefinitionList;
    }

    /**
     * @param panelDefinitionList the panelDefinitionList to set
     */
    public void setPanelDefinitionList(List<PanelDefinition> panelDefinitionList) {
        this.panelDefinitionList = panelDefinitionList;
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

    /**
     * @return the formDescription
     */
    public String getFormDescription() {
        return formDescription;
    }

    /**
     * @param formDescription the formDescription to set
     */
    public void setFormDescription(String formDescription) {
        this.formDescription = formDescription;
    }

    public Boolean getAllowSaveOption() {
        return allowSaveOption;
    }

    public void setAllowSaveOption(Boolean allowSaveOption) {
        this.allowSaveOption = allowSaveOption;
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

	public String getFormValidationsRulesInJSON() {
		return formValidationsRulesInJSON;
	}

	public void setFormValidationsRulesInJSON(String formValidationsRulesInJSON) {
		this.formValidationsRulesInJSON = formValidationsRulesInJSON;
	}

	public String getFormValidationRulesInJS() {
		return formValidationRulesInJS;
	}

	public void setFormValidationRulesInJS(String formValidationRulesInJS) {
		this.formValidationRulesInJS = formValidationRulesInJS;
	}

    public List<DynamicFormMapper> getDynamicFormMapperList() {
        return dynamicFormMapperList;
    }

    public void setDynamicFormMapperList(List<DynamicFormMapper> dynamicFormMapperList) {
        this.dynamicFormMapperList = dynamicFormMapperList;
    }
    
    @PreUpdate
    @PrePersist
    public void updatePanelSequence(){
    	if(ValidatorUtils.hasElements(this.panelDefinitionList)){
    		int panelSequence=1;
    		for(PanelDefinition panelDefinition:this.panelDefinitionList){
    			panelDefinition.setPanelSequence(panelSequence++);
    		}
    	}
    }

    public DynamicCollDedupeConfig getDynamicCollDedupeConfig() {
        return dynamicCollDedupeConfig;
    }

    public void setDynamicCollDedupeConfig(DynamicCollDedupeConfig dynamicCollDedupeConfig) {
        this.dynamicCollDedupeConfig = dynamicCollDedupeConfig;
    }

 
    
    
}
