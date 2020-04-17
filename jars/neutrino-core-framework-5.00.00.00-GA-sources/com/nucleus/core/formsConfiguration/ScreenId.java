package com.nucleus.core.formsConfiguration;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.annotations.Synonym;
import com.nucleus.core.dynamicform.entities.PlaceholderFilterMapping;
import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.CloneOptions;
import com.nucleus.rules.model.SourceProduct;

@Entity
@DynamicUpdate
@DynamicInsert
@Cacheable
@NamedQueries({
    @NamedQuery(name = "getScreenFromScreenId", query = "Select a from ScreenId a where a.id=:screenId"),
    @NamedQuery(name="getPlaceHolderIdsMappedToSourceProduct",
    query="Select a from ScreenId a where a.sourceProductId=:sourceProductId and a.id not in "+
"(select b.screenIdValue from DynamicFormScreenMapping b where  b.masterLifeCycleData.approvalStatus in (:approvalStatusList) AND b.activeFlag=true)")
})
@Synonym(grant="ALL")
public class ScreenId extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @Column(name="SCREEN_CODE")
    private String screenCode;

    private String screenName;

    private Boolean singleDynamicFormEnabled = Boolean.FALSE;

    private String entityClass;

    @ManyToOne
    @JoinColumn(name = "SOURCE_PRODUCT_ID", referencedColumnName = "ID", updatable = false, insertable = false)
    private SourceProduct sourceProduct;
    
    @Column(name = "SOURCE_PRODUCT_ID")
    private Long sourceProductId;

    private String name;

    private String description;

    public String getScreenCode() {
		return screenCode;
	}

	public void setScreenCode(String screenCode) {
		this.screenCode = screenCode;
	}

	public String getEntityClass() {
		return entityClass;
	}

	public void setEntityClass(String entityClass) {
		this.entityClass = entityClass;
	}

	public String getScreenIdValue() {
        return screenCode;
    }

    public void setScreenIdValue(String screenIdValue) {
        this.screenCode = screenIdValue;
    }

    public String getScreenName() {
        return screenName;
    }

    public void setScreenName(String screenName) {
        this.screenName = screenName;
    }

    public Boolean getSingleDynamicFormEnabled() {
        return singleDynamicFormEnabled;
    }

    public void setSingleDynamicFormEnabled(Boolean singleDynamicFormEnabled) {
        this.singleDynamicFormEnabled = singleDynamicFormEnabled;
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

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }

    public void setDescription(String description) { this.description = description; }

    @Override
    protected void populate(BaseEntity baseEntity, CloneOptions cloneOptions) {
    	ScreenId screenId = (ScreenId) baseEntity;
        super.populate(screenId, cloneOptions);
        screenId.setScreenIdValue(screenCode);
        screenId.setScreenName(screenName);
        screenId.setSingleDynamicFormEnabled(singleDynamicFormEnabled);
        screenId.setSourceProduct(sourceProduct);
        screenId.setSourceProductId(sourceProductId);
    }

    @Override
    protected void populateFrom(BaseEntity baseEntity, CloneOptions cloneOptions) {
    	ScreenId screenId = (ScreenId) baseEntity;
        super.populateFrom(screenId, cloneOptions);
        this.setScreenIdValue(screenId.getScreenIdValue());
        this.setScreenName(screenId.getScreenName());
        this.setSingleDynamicFormEnabled(screenId.getSingleDynamicFormEnabled());
        this.setSourceProduct(screenId.getSourceProduct());
        this.setSourceProductId(screenId.getSourceProductId());
    }

}
