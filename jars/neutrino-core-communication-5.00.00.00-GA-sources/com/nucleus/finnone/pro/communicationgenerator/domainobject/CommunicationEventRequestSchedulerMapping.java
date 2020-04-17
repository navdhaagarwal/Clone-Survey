package com.nucleus.finnone.pro.communicationgenerator.domainobject;

import javax.inject.Named;
import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.annotations.Synonym;
import com.nucleus.core.event.EventCode;
import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.CloneOptions;
import com.nucleus.master.BaseMasterEntity;
import com.nucleus.rules.model.SourceProduct;

@Entity
@DynamicUpdate
@DynamicInsert
@Table(name = "COM_COMMN_EVENT_SCH_MAPPING")
@Named("communicationEventRequestSchedulerMapping")
@Cacheable
@Synonym(grant="ALL")
public class CommunicationEventRequestSchedulerMapping extends BaseMasterEntity{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@ManyToOne
	private EventCode eventCode;
	
	@Column(name="COM_COMMN_EVENT_REQ_SCH_ID")
	private Long communicationEventRequestSchedulerId;
	
	@ManyToOne
    @JoinColumn(name="SOURCE_PRODUCT_ID")
	private SourceProduct sourceProduct;

	public EventCode getEventCode() {
		return eventCode;
	}

	public void setEventCode(EventCode eventCode) {
		this.eventCode = eventCode;
	}

	public Long getCommunicationEventRequestSchedulerId() {
		return communicationEventRequestSchedulerId;
	}

	public void setCommunicationEventRequestSchedulerId(
			Long communicationEventRequestSchedulerId) {
		this.communicationEventRequestSchedulerId = communicationEventRequestSchedulerId;
	}
		
	public SourceProduct getSourceProduct() {
        return sourceProduct;
    }

    public void setSourceProduct(SourceProduct sourceProduct) {
        this.sourceProduct = sourceProduct;
    }

    @Override
	protected void populate(BaseEntity baseEntity, CloneOptions cloneOptions) {
		CommunicationEventRequestSchedulerMapping communicationEventRequestSchedulerMapping = (CommunicationEventRequestSchedulerMapping) baseEntity;
        super.populate(communicationEventRequestSchedulerMapping, cloneOptions);
        communicationEventRequestSchedulerMapping.setEventCode(eventCode);
        communicationEventRequestSchedulerMapping.setCommunicationEventRequestSchedulerId(communicationEventRequestSchedulerId);
        communicationEventRequestSchedulerMapping.setSourceProduct(sourceProduct);
    
	}
    
    
    @Override
    protected void populateFrom(BaseEntity baseEntity, CloneOptions cloneOptions) {
    	CommunicationEventRequestSchedulerMapping communicationEventRequestSchedulerMapping = (CommunicationEventRequestSchedulerMapping) baseEntity;
        super.populateFrom(communicationEventRequestSchedulerMapping, cloneOptions);
        this.setEventCode(communicationEventRequestSchedulerMapping.getEventCode());
        this.setCommunicationEventRequestSchedulerId(communicationEventRequestSchedulerMapping.getCommunicationEventRequestSchedulerId());
        this.setSourceProduct(communicationEventRequestSchedulerMapping.getSourceProduct());
                      
    }
}
