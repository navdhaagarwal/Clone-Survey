package com.nucleus.core.dynamicform.entities;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.annotations.Synonym;
import com.nucleus.core.formsConfiguration.ScreenId;
import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.CloneOptions;
import com.nucleus.master.BaseMasterEntity;

@Entity
@DynamicUpdate
@DynamicInsert
@Cacheable
@Synonym(grant="SELECT")
public class PlaceholderFilterMapping extends BaseMasterEntity{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@ManyToOne
	@JoinColumn(name="DYNAMIC_FORM_FILTER_ID")
	private DynamicFormFilter dynamicFormFilter;
	
	@ManyToOne
	@JoinColumn(name="SERVICE_PLACEHOLDER_ID")
	private ScreenId screenId;

	
	@Column(name="SERVICE_MAPPING_ID")
	private Long serviceMappingId;

	public DynamicFormFilter getDynamicFormFilter() {
		return dynamicFormFilter;
	}


	public void setDynamicFormFilter(DynamicFormFilter dynamicFormFilter) {
		this.dynamicFormFilter = dynamicFormFilter;
	}


	public ScreenId getScreenId() {
		return screenId;
	}


	public void setScreenId(ScreenId screenId) {
		this.screenId = screenId;
	}


	public Long getServiceMappingId() {
		return serviceMappingId;
	}


	public void setServiceMappingId(Long serviceMappingId) {
		this.serviceMappingId = serviceMappingId;
	}

	@Override
    protected void populate(BaseEntity baseEntity, CloneOptions cloneOptions) {
		PlaceholderFilterMapping placeholderFilterMapping = (PlaceholderFilterMapping) baseEntity;
        super.populate(placeholderFilterMapping, cloneOptions);
        placeholderFilterMapping.setDynamicFormFilter(dynamicFormFilter);
        placeholderFilterMapping.setScreenId(screenId);
        placeholderFilterMapping.setServiceMappingId(serviceMappingId);
    }

    @Override
    protected void populateFrom(BaseEntity baseEntity, CloneOptions cloneOptions) {
    	PlaceholderFilterMapping placeholderFilterMapping = (PlaceholderFilterMapping) baseEntity;
        super.populateFrom(placeholderFilterMapping, cloneOptions);
        this.setDynamicFormFilter(placeholderFilterMapping.getDynamicFormFilter());
        this.setScreenId(placeholderFilterMapping.getScreenId());
        this.setServiceMappingId(placeholderFilterMapping.getServiceMappingId());
    }
	
}
