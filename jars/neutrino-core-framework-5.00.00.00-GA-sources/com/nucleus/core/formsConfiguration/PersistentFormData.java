package com.nucleus.core.formsConfiguration;

import java.util.Map;

import javax.persistence.Cacheable;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.annotations.Synonym;
import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.CloneOptions;

@Entity
@DynamicInsert
@DynamicUpdate
@Synonym(grant="ALL")
public class PersistentFormData extends BaseEntity {

    private static final long   serialVersionUID = 1726704386595828922L;

    @Transient
    private Map<String, Object> dataMap;

    @Lob
    private String              dataJsonString;

    @ManyToOne
    private ModelMetaData       modelMetaData;

    private String              formUri;

    /**
     * @return the fieldValuePariring
     */
    public Map<String, Object> getFieldValuePariring() {
        return dataMap;
    }

    /**
     * @param fieldValuePariring the fieldValuePariring to set
     */
    public void setFieldValuePariring(Map<String, Object> fieldValuePariring) {
        this.dataMap = fieldValuePariring;
    }

    /**
     * @return the fieldValueData
     */
    public String getFieldValueData() {
        return dataJsonString;
    }

    /**
     * @param fieldValueData the fieldValueData to set
     */
    public void setFieldValueData(String fieldValueData) {
        this.dataJsonString = fieldValueData;
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

    @Override
    protected void populate(BaseEntity baseEntity, CloneOptions cloneOptions) {
        PersistentFormData persistentFormData = (PersistentFormData) baseEntity;
        super.populate(persistentFormData, cloneOptions);

        persistentFormData.setFieldValueData(dataJsonString);
        persistentFormData.setModelMetaData(modelMetaData);
        persistentFormData.setFormUri(formUri);
    }
    protected void populateFrom(BaseEntity baseEntity, CloneOptions cloneOptions) {
        PersistentFormData persistentFormData = (PersistentFormData) baseEntity;
        super.populateFrom(persistentFormData, cloneOptions);
        
        this.setFieldValueData(persistentFormData.getFieldValueData());
        this.setModelMetaData(persistentFormData.getModelMetaData());
        this.setFormUri(persistentFormData.getFormUri());
        
        
    }

}
