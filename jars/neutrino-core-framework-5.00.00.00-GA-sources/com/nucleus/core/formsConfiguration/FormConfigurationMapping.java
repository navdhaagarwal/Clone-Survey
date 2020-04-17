package com.nucleus.core.formsConfiguration;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.CascadeType;

import com.nucleus.cas.parentChildDeletionHandling.DeletionPreValidator;
import com.nucleus.entity.CloneOptions;
import com.nucleus.master.BaseMasterEntity;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.annotations.Synonym;
import com.nucleus.entity.BaseEntity;
import com.nucleus.rules.model.SourceProduct;

@Entity
@DynamicUpdate
@DynamicInsert
@Cacheable
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Synonym(grant="SELECT")
@DeletionPreValidator
public class FormConfigurationMapping extends BaseMasterEntity {

    private static final long serialVersionUID = 3245392976852015489L;

    private String            invocationPoint;
    private static String ENTITY_DISPLAY_NAME="Dynamic Form";

    @Override
	public String getEntityDisplayName() {
		return ENTITY_DISPLAY_NAME;
	}

	@OneToOne(cascade = CascadeType.ALL)
    private UIMetaData        uiMetaData;

    @OneToOne(cascade = CascadeType.ALL)
    private ModelMetaData     modelMetaData;
    
    @ManyToOne
    @JoinColumn(name = "SOURCE_PRODUCT_ID", referencedColumnName = "ID", updatable = false, insertable = false)
    private SourceProduct sourceProduct;
    
    public SourceProduct getSourceProduct() {
        return sourceProduct;
    }

    public void setSourceProduct(SourceProduct sourceProduct) {
        this.sourceProduct = sourceProduct;
    }

    @Column(name = "SOURCE_PRODUCT_ID")
    private Long sourceProductId;

    public Long getSourceProductId() {
        return sourceProductId;
    }

    public void setSourceProductId(Long sourceProductId) {
        this.sourceProductId = sourceProductId;
    }

    /**
     * @return the invocationPoint
     */
    public String getInvocationPoint() {
        return invocationPoint;
    }

    /**
     * @param invocationPoint the invocationPoint to set
     */
    public void setInvocationPoint(String invocationPoint) {
        this.invocationPoint = invocationPoint;
    }

    /**
     * @return the uiMetaData
     */
    public UIMetaData getUiMetaData() {
        return uiMetaData;
    }

    /**
     * @param uiMetaData the uiMetaData to set
     */
    public void setUiMetaData(UIMetaData uiMetaData) {
        this.uiMetaData = uiMetaData;
    }

    /**
     * @return the modelMetaData
     */
    public ModelMetaData getModelMetaData() {
        return modelMetaData;
    }

    /**
     * @param modelMetaData the modelMetaData to set
     */
    public void setModelMetaData(ModelMetaData modelMetaData) {
        this.modelMetaData = modelMetaData;
    }

    @Override
    protected void populate(BaseEntity baseEntity, CloneOptions cloneOptions) {
        FormConfigurationMapping formConfigurationMapping = (FormConfigurationMapping) baseEntity;
        super.populate(formConfigurationMapping, cloneOptions);
        formConfigurationMapping.setInvocationPoint(invocationPoint);
        formConfigurationMapping.setUiMetaData(uiMetaData);
        formConfigurationMapping.setModelMetaData(modelMetaData);
        formConfigurationMapping.setSourceProduct(sourceProduct);
        formConfigurationMapping.setSourceProductId(sourceProductId);
    }

    @Override
    protected void populateFrom(BaseEntity baseEntity, CloneOptions cloneOptions) {
        FormConfigurationMapping formConfigurationMapping = (FormConfigurationMapping) baseEntity;
        super.populateFrom(formConfigurationMapping, cloneOptions);
        this.setInvocationPoint(formConfigurationMapping.getInvocationPoint());
        this.setUiMetaData(formConfigurationMapping.getUiMetaData());
        this.setModelMetaData(formConfigurationMapping.getModelMetaData());
        this.setSourceProduct(formConfigurationMapping.getSourceProduct());
        this.setSourceProductId(formConfigurationMapping.getSourceProductId());
    }

    @Override
    public String getDisplayName() {
        return getUiMetaData().getFormName();
    }

}
