package com.nucleus.core.dynamicform.entities;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.*;

import com.nucleus.activeInactiveReason.ReasonsActiveInactiveMapping;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.annotations.Synonym;
import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.CloneOptions;
import com.nucleus.master.BaseMasterEntity;
import com.nucleus.ws.core.entities.ServiceIdentifier;

@Entity
@DynamicUpdate
@DynamicInsert
@Cacheable
@Table(name="SERVICE_FIELD_FILTER_MST", indexes={@Index(name="RAIM_PERF_45_4031",columnList="REASON_ACT_INACT_MAP")})
@Synonym(grant="ALL")
public class ServiceFieldFilterMapping extends BaseMasterEntity{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@ManyToOne
	@JoinColumn(name="SERVICE_IDENTIFIER_ID")
	private ServiceIdentifier serviceIdentifier;
	
	
	@OneToMany(cascade = {CascadeType.ALL})
	@JoinColumn(name="SERVICE_MAPPING_ID")
	private List<FieldFilterMapping> fieldFilterMappings;

	@OneToOne(cascade = CascadeType.ALL)
	private ReasonsActiveInactiveMapping reasonActInactMap;

	
	public ServiceIdentifier getServiceIdentifier() {
		return serviceIdentifier;
	}


	public void setServiceIdentifier(ServiceIdentifier serviceIdentifier) {
		this.serviceIdentifier = serviceIdentifier;
	}


	public List<FieldFilterMapping> getFieldFilterMappings() {
		return fieldFilterMappings;
	}


	public void setFieldFilterMappings(List<FieldFilterMapping> fieldFilterMappings) {
		this.fieldFilterMappings = fieldFilterMappings;
	}

	public ReasonsActiveInactiveMapping getReasonActInactMap() {
		return reasonActInactMap;
	}

	public void setReasonActInactMap(ReasonsActiveInactiveMapping reasonActInactMap) {
		this.reasonActInactMap = reasonActInactMap;
	}

	@Override
    protected void populate(BaseEntity baseEntity, CloneOptions cloneOptions) {
    	ServiceFieldFilterMapping serviceFieldFilterMapping=(ServiceFieldFilterMapping)baseEntity;
        super.populate(serviceFieldFilterMapping, cloneOptions);
        serviceFieldFilterMapping.setServiceIdentifier(serviceIdentifier);
        serviceFieldFilterMapping.setFieldFilterMappings(fieldFilterMappings != null && !fieldFilterMappings.isEmpty() ? new ArrayList<FieldFilterMapping>(fieldFilterMappings) : null);

		if (reasonActInactMap != null) {
			serviceFieldFilterMapping.setReasonActInactMap((ReasonsActiveInactiveMapping) this.reasonActInactMap.cloneYourself(cloneOptions));
		}
    }

    @Override
    protected void populateFrom(BaseEntity baseEntity, CloneOptions cloneOptions) {
    	ServiceFieldFilterMapping serviceFieldFilterMapping=(ServiceFieldFilterMapping)baseEntity;
        super.populate(serviceFieldFilterMapping, cloneOptions);
        this.setServiceIdentifier(serviceFieldFilterMapping.getServiceIdentifier());
        this.setFieldFilterMappings(serviceFieldFilterMapping.getFieldFilterMappings() != null && !serviceFieldFilterMapping.getFieldFilterMappings().isEmpty() ? serviceFieldFilterMapping
                .getFieldFilterMappings() : null);
		if (serviceFieldFilterMapping.getReasonActInactMap() != null) {
			this.setReasonActInactMap((ReasonsActiveInactiveMapping) serviceFieldFilterMapping.getReasonActInactMap().cloneYourself(cloneOptions));
		}
    }

	@Override
	public String getDisplayName() {
		return getServiceIdentifier().getName();
	}
	
}
