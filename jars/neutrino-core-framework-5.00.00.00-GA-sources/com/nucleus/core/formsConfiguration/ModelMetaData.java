package com.nucleus.core.formsConfiguration;

import static com.nucleus.finnone.pro.general.util.ValidatorUtils.hasElements;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;

import com.nucleus.master.BaseMasterEntity;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.annotations.Synonym;
import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.CloneOptions;

@Entity
@DynamicUpdate
@DynamicInsert
@Cacheable
@Synonym(grant="SELECT")
public class ModelMetaData extends BaseMasterEntity {

    private static final long serialVersionUID = 7906790284024550888L;

    private String            name;

    private String            description;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "model_meta_data_fk")
    @Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
    List<FieldMetaData>       fields;

    private String            modeluuid;

    private String            modelVersion;
    
    
    
    @Override
    protected void populate(BaseEntity baseEntity, CloneOptions cloneOptions) {
        ModelMetaData modelMetaData = (ModelMetaData) baseEntity;
        super.populate(modelMetaData, cloneOptions);

        modelMetaData.setDescription(description);
        modelMetaData.setName(name);

        if (fields != null && fields.size() > 0) {
            List<FieldMetaData> cloneFields = new ArrayList<FieldMetaData>();
            for (FieldMetaData field : fields) {
                cloneFields.add((FieldMetaData) field.cloneYourself(cloneOptions));
            }
            modelMetaData.setFields(cloneFields);
        }
    }

    
    @Override
    protected void populateFrom(BaseEntity baseEntity, CloneOptions cloneOptions) {
    	
    	ModelMetaData modelMetaData=(ModelMetaData)baseEntity;
        super.populateFrom(modelMetaData, cloneOptions);
        this.setName(modelMetaData.getName());
        this.setDescription(modelMetaData.getDescription());
        if(this.getFields()==null)
        {
        	this.setFields(new ArrayList<FieldMetaData>());
        }
        if (hasElements(modelMetaData.getFields())) {
        	
            this.getFields().clear();
            for (FieldMetaData fieldMetaData : modelMetaData.getFields()) {
            	this.getFields().add((FieldMetaData) fieldMetaData.cloneYourself(cloneOptions));
            }
        }   
        this.setModeluuid(modelMetaData.getModeluuid());
        this.setModelVersion(modelMetaData.getModelVersion());
        
        
    	
    }
    
    
    /**
     * @return the modeluuid
     */
    public String getModeluuid() {
        return modeluuid;
    }

    /**
     * @param modeluuid the modeluuid to set
     */
    public void setModeluuid(String modeluuid) {
        this.modeluuid = modeluuid;
    }

    /**
     * @return the modelVersion
     */
    public String getModelVersion() {
        return modelVersion;
    }

    /**
     * @param modelVersion the modelVersion to set
     */
    public void setModelVersion(String modelVersion) {
        this.modelVersion = modelVersion;
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
     * @return the Description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param modelDescription the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return the fieldMetaDataList
     */
    public List<FieldMetaData> getFields() {
        return fields;
    }

    /**
     * @param fieldMetaDataList the fieldMetaDataList to set
     */
    public void setFields(List<FieldMetaData> fields) {
        this.fields = fields;
    }



}
