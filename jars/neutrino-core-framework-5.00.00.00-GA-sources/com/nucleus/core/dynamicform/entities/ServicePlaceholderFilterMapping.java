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
@Table(name="SERV_PLACEHOLDER_FILTER_MST",indexes={@Index(name="RAIM_PERF_45_4123",columnList="REASON_ACT_INACT_MAP")})
@Synonym(grant="ALL")
public class ServicePlaceholderFilterMapping extends BaseMasterEntity{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@ManyToOne
	@JoinColumn(name="SERVICE_IDENTIFIER_ID")
	private ServiceIdentifier serviceIdentifier;
	
	
	@OneToMany(cascade = {CascadeType.ALL})
	@JoinColumn(name="SERVICE_MAPPING_ID")
	private List<PlaceholderFilterMapping> placeholderFilterMappings;

	@OneToOne(cascade = CascadeType.ALL)
	private ReasonsActiveInactiveMapping reasonActInactMap;

	
	public ServiceIdentifier getServiceIdentifier() {
		return serviceIdentifier;
	}


	public void setServiceIdentifier(ServiceIdentifier serviceIdentifier) {
		this.serviceIdentifier = serviceIdentifier;
	}


	public List<PlaceholderFilterMapping> getPlaceholderFilterMappings() {
		return placeholderFilterMappings;
	}


	public ReasonsActiveInactiveMapping getReasonActInactMap() {
		return reasonActInactMap;
	}

	public void setReasonActInactMap(ReasonsActiveInactiveMapping reasonActInactMap) {
		this.reasonActInactMap = reasonActInactMap;
	}

	public void setPlaceholderFilterMappings(List<PlaceholderFilterMapping> placeholderFilterMappings) {
		this.placeholderFilterMappings = placeholderFilterMappings;


	}

	@Override
    protected void populate(BaseEntity baseEntity, CloneOptions cloneOptions) {
		ServicePlaceholderFilterMapping servicePlaceholderFilterMapping = (ServicePlaceholderFilterMapping) baseEntity;
        super.populate(servicePlaceholderFilterMapping, cloneOptions);
        servicePlaceholderFilterMapping.setServiceIdentifier(serviceIdentifier);
        servicePlaceholderFilterMapping.setPlaceholderFilterMappings(placeholderFilterMappings != null
				&& !placeholderFilterMappings.isEmpty()
						? new ArrayList<PlaceholderFilterMapping>(placeholderFilterMappings) : null);

		if (reasonActInactMap != null) {
			servicePlaceholderFilterMapping.setReasonActInactMap((ReasonsActiveInactiveMapping) this.reasonActInactMap.cloneYourself(cloneOptions));
		}
        
    }

    @Override
	protected void populateFrom(BaseEntity baseEntity, CloneOptions cloneOptions) {
		ServicePlaceholderFilterMapping servicePlaceholderFilterMapping = (ServicePlaceholderFilterMapping) baseEntity;
		super.populateFrom(servicePlaceholderFilterMapping, cloneOptions);
		this.setServiceIdentifier(servicePlaceholderFilterMapping.getServiceIdentifier());
		this.setPlaceholderFilterMappings(servicePlaceholderFilterMapping.getPlaceholderFilterMappings() != null
				&& !servicePlaceholderFilterMapping.getPlaceholderFilterMappings().isEmpty()
						? servicePlaceholderFilterMapping.getPlaceholderFilterMappings() : null);
		if (servicePlaceholderFilterMapping.getReasonActInactMap() != null) {
			this.setReasonActInactMap((ReasonsActiveInactiveMapping) servicePlaceholderFilterMapping.getReasonActInactMap().cloneYourself(cloneOptions));
		}
	}

	@Override
	public String getDisplayName() {
		return getServiceIdentifier().getName();
	}
}
