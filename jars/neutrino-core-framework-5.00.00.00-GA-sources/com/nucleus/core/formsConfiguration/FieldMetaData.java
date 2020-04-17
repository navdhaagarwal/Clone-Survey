package com.nucleus.core.formsConfiguration;

import static com.nucleus.finnone.pro.general.util.ValidatorUtils.hasElements;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Cacheable;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.annotations.Synonym;
import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.CloneOptions;

@Entity
@DynamicUpdate
@DynamicInsert
@Cacheable
@Synonym(grant="ALL")
@Table(indexes={@Index(name="model_meta_data_fk",columnList="model_meta_data_fk")})
public class FieldMetaData extends BaseEntity {

    private static final long serialVersionUID = 2479245228746369399L;

    private String            name;

    private int               dataType;

    private boolean           multiValued;

    private String            fieldKey;

    
    @Override
    protected void populate(BaseEntity baseEntity, CloneOptions cloneOptions) {
        FieldMetaData fieldMetaData = (FieldMetaData) baseEntity;
        super.populate(fieldMetaData, cloneOptions);

        fieldMetaData.setDataType(dataType);
        fieldMetaData.setFieldKey(fieldKey);
        fieldMetaData.setMultiValued(multiValued);
        fieldMetaData.setName(name);
    }
   
    @Override
    protected void populateFrom(BaseEntity baseEntity, CloneOptions cloneOptions) {
    	
    	FieldMetaData fieldMetaData=(FieldMetaData)baseEntity;
        super.populateFrom(fieldMetaData, cloneOptions);
        this.setName(fieldMetaData.getName());
        this.setDataType(fieldMetaData.getDataType());
        this.setFieldKey(fieldMetaData.getFieldKey());
        this.setMultiValued(fieldMetaData.isMultiValued());
    	
    }
    
        
    
    
    
    
    
    
    
    
    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the dataType
     */
    public int getDataType() {
        return dataType;
    }

    /**
     * @param dataType the dataType to set
     */
    public void setDataType(int dataType) {
        this.dataType = dataType;
    }

    /**
     * @return the multiValued
     */
    public boolean isMultiValued() {
        return multiValued;
    }

    /**
     * @param multiValued the multiValued to set
     */
    public void setMultiValued(boolean multiValued) {
        this.multiValued = multiValued;
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

   
}
