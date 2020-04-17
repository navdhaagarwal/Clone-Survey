package com.nucleus.ws.core.entities;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.annotations.Synonym;
import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.CloneOptions;
import com.nucleus.master.BaseMasterEntity;
import com.nucleus.rules.model.SourceProduct;

@Entity
@DynamicUpdate
@DynamicInsert
@Cacheable
@Synonym(grant="ALL")
public class ServiceIdentifier extends BaseMasterEntity{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String name;
	
	
	private String code;
	
	
	private String description;
	
	
	private String serviceUri;
	
	@ManyToOne
	@JoinColumn(name="SOURCE_PRODUCT_ID")
	private SourceProduct sourceProduct;
	
	
	
	@OneToMany(cascade = {CascadeType.ALL})
	@JoinColumn(name="SERVICE_ID")
	private List<ServiceFieldName> serviceFields;

	@ManyToOne
    private ServiceFieldType serviceFieldType;
	
	public SourceProduct getSourceProduct() {
		return sourceProduct;
	}


	public void setSourceProduct(SourceProduct sourceProduct) {
		this.sourceProduct = sourceProduct;
	}


	public List<ServiceFieldName> getServiceFields() {
		return serviceFields;
	}


	public void setServiceFields(List<ServiceFieldName> serviceFields) {
		this.serviceFields = serviceFields;
	}


	public String getName() {
		return name;
	}


	public void setName(String name) {
		this.name = name;
	}


	public String getCode() {
		return code;
	}


	public void setCode(String code) {
		this.code = code;
	}


	public String getDescription() {
		return description;
	}


	public void setDescription(String description) {
		this.description = description;
	}


	public String getServiceUri() {
		return serviceUri;
	}


	public void setServiceUri(String serviceUri) {
		this.serviceUri = serviceUri;
	}
	
	public ServiceFieldType getServiceFieldType() {
		return serviceFieldType;
	}


	public void setServiceFieldType(ServiceFieldType serviceFieldType) {
		this.serviceFieldType = serviceFieldType;
	}


	@Override
    protected void populate(BaseEntity baseEntity, CloneOptions cloneOptions) {
		ServiceIdentifier serviceIdentifier = (ServiceIdentifier) baseEntity;
        super.populate(serviceIdentifier, cloneOptions);
        serviceIdentifier.setName(name);
        serviceIdentifier.setCode(code);
        serviceIdentifier.setDescription(description);
        serviceIdentifier.setServiceUri(serviceUri);
        serviceIdentifier.setSourceProduct(sourceProduct);
        serviceIdentifier.setServiceFields(serviceFields != null && !serviceFields.isEmpty() ? new ArrayList<ServiceFieldName>(serviceFields) : null);
        
    }

    @Override
    protected void populateFrom(BaseEntity baseEntity, CloneOptions cloneOptions) {
    	ServiceIdentifier serviceIdentifier = (ServiceIdentifier) baseEntity;
        super.populateFrom(serviceIdentifier, cloneOptions);
        this.setName(serviceIdentifier.getName());
        this.setCode(serviceIdentifier.getCode());
        this.setDescription(serviceIdentifier.getDescription());
        this.setServiceUri(serviceIdentifier.getServiceUri());
        this.setSourceProduct(serviceIdentifier.getSourceProduct());
        this.setServiceFields(serviceIdentifier.getServiceFields() != null && !serviceIdentifier.getServiceFields().isEmpty() ? serviceIdentifier
                .getServiceFields() : null);
    }	

}
