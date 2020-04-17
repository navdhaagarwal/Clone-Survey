package com.nucleus.core.dynamicform.entities;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.annotations.Synonym;
import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.CloneOptions;
import com.nucleus.master.BaseMasterEntity;
import com.nucleus.ws.core.entities.ServiceFieldName;

@Entity
@DynamicUpdate
@DynamicInsert
@Cacheable
@Synonym(grant="SELECT")
public class FieldFilterMapping extends BaseMasterEntity{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@ManyToOne
	@JoinColumn(name="DYNAMIC_FORM_FILTER_ID")
	private DynamicFormFilter dynamicFormFilter;
	
	@ManyToOne
	@JoinColumn(name="SERVICE_FIELD_NAME_ID")
	private ServiceFieldName serviceFieldName;

	
	@Column(name="SERVICE_MAPPING_ID")
	private Long serviceMappingId;

	public Long getServiceMappingId() {
		return serviceMappingId;
	}


	public void setServiceMappingId(Long serviceMappingId) {
		this.serviceMappingId = serviceMappingId;
	}


	public DynamicFormFilter getDynamicFormFilter() {
		return dynamicFormFilter;
	}


	public void setDynamicFormFilter(DynamicFormFilter dynamicFormFilter) {
		this.dynamicFormFilter = dynamicFormFilter;
	}


	public ServiceFieldName getServiceFieldName() {
		return serviceFieldName;
	}


	public void setServiceFieldName(ServiceFieldName serviceFieldName) {
		this.serviceFieldName = serviceFieldName;
	}
	
	
    @Override
    protected void populate(BaseEntity baseEntity, CloneOptions cloneOptions) {
    	FieldFilterMapping fieldFilterMapping=(FieldFilterMapping)baseEntity;
        super.populate(fieldFilterMapping, cloneOptions);
        fieldFilterMapping.setDynamicFormFilter(dynamicFormFilter);
        fieldFilterMapping.setServiceFieldName(serviceFieldName);
        fieldFilterMapping.setServiceMappingId(serviceMappingId);
     
    }

    @Override
    protected void populateFrom(BaseEntity baseEntity, CloneOptions cloneOptions) {
    	FieldFilterMapping fieldFilterMapping=(FieldFilterMapping)baseEntity;
        super.populate(fieldFilterMapping, cloneOptions);
        this.setDynamicFormFilter(fieldFilterMapping.getDynamicFormFilter());
        this.setServiceFieldName(fieldFilterMapping.getServiceFieldName());
        this.setServiceMappingId(fieldFilterMapping.getServiceMappingId());
    	
    }
	
	
	
}
