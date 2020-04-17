package com.nucleus.core.dynamicform.entities;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.annotations.Synonym;
import com.nucleus.core.formsConfiguration.ScreenId;
import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.CloneOptions;
import com.nucleus.master.BaseMasterEntity;
import com.nucleus.ws.core.entities.ServiceIdentifier;

@Entity
@DynamicUpdate
@DynamicInsert
@Synonym(grant="ALL")
public class ServicePlaceholderMapping extends BaseMasterEntity{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="SERVICE_ID")
	private ServiceIdentifier serviceIdentifier;
	

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="SCREEN_ID")
	private ScreenId screenId;


	public ServiceIdentifier getServiceIdentifier() {
		return serviceIdentifier;
	}


	public void setServiceIdentifier(ServiceIdentifier serviceIdentifier) {
		this.serviceIdentifier = serviceIdentifier;
	}


	public ScreenId getScreenId() {
		return screenId;
	}


	public void setScreenId(ScreenId screenId) {
		this.screenId = screenId;
	}
	
	@Override
    protected void populate(BaseEntity baseEntity, CloneOptions cloneOptions) {
		ServicePlaceholderMapping servicePlaceholderMapping = (ServicePlaceholderMapping) baseEntity;
        super.populate(servicePlaceholderMapping, cloneOptions);
        servicePlaceholderMapping.setServiceIdentifier(serviceIdentifier);
        servicePlaceholderMapping.setScreenId(screenId);
    }

    @Override
    protected void populateFrom(BaseEntity baseEntity, CloneOptions cloneOptions) {
    	ServicePlaceholderMapping servicePlaceholderMapping = (ServicePlaceholderMapping) baseEntity;
        super.populateFrom(servicePlaceholderMapping, cloneOptions);
        this.setServiceIdentifier(servicePlaceholderMapping.getServiceIdentifier());
        this.setScreenId(servicePlaceholderMapping.getScreenId());
    }
	
	

}
