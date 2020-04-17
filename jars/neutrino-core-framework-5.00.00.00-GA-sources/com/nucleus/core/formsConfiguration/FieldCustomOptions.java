package com.nucleus.core.formsConfiguration;

import javax.persistence.Cacheable;
import javax.persistence.Entity;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.annotations.Synonym;
import com.nucleus.core.dynamicform.entities.DynamicFormFilter;
import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.CloneOptions;

@Entity
@DynamicUpdate
@DynamicInsert
@Cacheable
@Synonym(grant="ALL")
public class FieldCustomOptions extends BaseEntity {

    private static final long serialVersionUID = -2537406546374037450L;

    private String            customeItemValue;

    private String            customeItemLabel;

    

    @Override
    protected void populate(BaseEntity baseEntity, CloneOptions cloneOptions) {
    	FieldCustomOptions fieldCustomOptions=(FieldCustomOptions)baseEntity;
        super.populate(fieldCustomOptions, cloneOptions);
        fieldCustomOptions.setCustomeItemValue(customeItemValue);
        fieldCustomOptions.setCustomeItemLabel(customeItemLabel);
    }

    @Override
    protected void populateFrom(BaseEntity baseEntity, CloneOptions cloneOptions) {
    	 FieldCustomOptions fieldCustomOptions=(FieldCustomOptions)baseEntity;
    	 super.populateFrom(fieldCustomOptions, cloneOptions);
    	 this.setCustomeItemLabel(fieldCustomOptions.getCustomeItemLabel());
    	 this.setCustomeItemValue(fieldCustomOptions.getCustomeItemValue());
    }
    
    
    
    
    /**
     * @return the customeItemValue
     */
    public String getCustomeItemValue() {
        return customeItemValue;
    }

    /**
     * @param customeItemValue the customeItemValue to set
     */
    public void setCustomeItemValue(String customeItemValue) {
        this.customeItemValue = customeItemValue;
    }

    /**
     * @return the customeItemLabel
     */
    public String getCustomeItemLabel() {
        return customeItemLabel;
    }

    /**
     * @param customeItemLabel the customeItemLabel to set
     */
    public void setCustomeItemLabel(String customeItemLabel) {
        this.customeItemLabel = customeItemLabel;
    }

}
