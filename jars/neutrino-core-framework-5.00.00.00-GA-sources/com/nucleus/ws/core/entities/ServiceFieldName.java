package com.nucleus.ws.core.entities;

import javax.persistence.Column;
import javax.persistence.Entity;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.annotations.Synonym;
import com.nucleus.core.dynamicform.entities.ServicePlaceholderFilterMapping;
import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.CloneOptions;
import com.nucleus.master.BaseMasterEntity;

@Entity
@DynamicUpdate
@DynamicInsert
@Synonym(grant="ALL")
public class ServiceFieldName extends BaseMasterEntity{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
	private String fieldName;
	
	
	private String description;

	@Column(name="SERVICE_ID")
	private Long serviceId;
	
	

	public Long getServiceId() {
		return serviceId;
	}


	public void setServiceId(Long serviceId) {
		this.serviceId = serviceId;
	}


	public String getFieldName() {
		return fieldName;
	}


	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}


	public String getDescription() {
		return description;
	}


	public void setDescription(String description) {
		this.description = description;
	}
	
	@Override
    protected void populate(BaseEntity baseEntity, CloneOptions cloneOptions) {
		ServiceFieldName serviceFieldName = (ServiceFieldName) baseEntity;
        super.populate(serviceFieldName, cloneOptions);
        serviceFieldName.setServiceId(serviceId);
        serviceFieldName.setFieldName(fieldName);
        serviceFieldName.setDescription(description);
    }

    @Override
    protected void populateFrom(BaseEntity baseEntity, CloneOptions cloneOptions) {
    	ServiceFieldName serviceFieldName = (ServiceFieldName) baseEntity;
        super.populateFrom(serviceFieldName, cloneOptions);
        this.setServiceId(serviceFieldName.getServiceId());
        this.setFieldName(serviceFieldName.getFieldName());
        this.setDescription(serviceFieldName.getDescription());
    }
	

	
}
