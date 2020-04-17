package com.nucleus.core.formsConfiguration;

import javax.persistence.*;

import com.nucleus.core.annotations.Synonym;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.cas.eligibility.service.ProductProcessor;
import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.CloneOptions;

@Entity
@DynamicUpdate
@DynamicInsert
@Table(name = "DYNAMIC_FORM_MAPPER")
@Cacheable
@Synonym(grant="SELECT")
public class DynamicFormMapper extends BaseEntity {

    @Column(length = 1000)
    private String formFields;

    @ManyToOne
    private ProductProcessor productProcessor;

    public String getFormFields() {
        return formFields;
    }

    public void setFormFields(String formFields) {
        this.formFields = formFields;
    }

    public ProductProcessor getProductProcessor() {
        return productProcessor;
    }

    public void setProductProcessor(ProductProcessor productProcessor) {
        this.productProcessor = productProcessor;
    }
    @Override
    protected void populate(BaseEntity baseEntity, CloneOptions cloneOptions) {
        DynamicFormMapper dynamicFormMapper = (DynamicFormMapper) baseEntity;
        super.populate(dynamicFormMapper, cloneOptions);
        dynamicFormMapper.setProductProcessor(this.productProcessor);
        dynamicFormMapper.setFormFields(this.formFields);
    }

    @Override
    protected void populateFrom(BaseEntity baseEntity, CloneOptions cloneOptions) {
        DynamicFormMapper dynamicFormMapper = (DynamicFormMapper) baseEntity;
        super.populateFrom(dynamicFormMapper, cloneOptions);
        this.setProductProcessor(dynamicFormMapper.getProductProcessor());
        this.setFormFields(dynamicFormMapper.getFormFields());
    }

    @Override
    public String toString() {
        return "DynamicFormMapper{" +
                "formFields=" + formFields +
                ", productProcessor=" + productProcessor +
                '}';
    }
}
