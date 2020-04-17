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
import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.CloneOptions;
import com.nucleus.master.BaseMasterEntity;
import com.nucleus.rules.model.SourceProduct;

@Entity
@DynamicUpdate
@DynamicInsert
@Table(name = "COM_COMMN_GEN_SCH_MAPPING")
@Named("communicationGenerationSchedulerMapping")
@Cacheable
@Synonym(grant="ALL")
public class CommunicationGenerationSchedulerMapping extends BaseMasterEntity{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@ManyToOne
	private CommunicationName communication;
	
	@Column(name="COM_COMMN_GENERATION_SCH_ID")
	private Long communicationGenerationSchedulerId;

	@ManyToOne
    @JoinColumn(name="SOURCE_PRODUCT_ID")
	private SourceProduct sourceProduct;
	
	public CommunicationName getCommunication() {
		return communication;
	}

	public void setCommunication(CommunicationName communication) {
		this.communication = communication;
	}

	public Long getCommunicationGenerationSchedulerId() {
		return communicationGenerationSchedulerId;
	}

	public void setCommunicationGenerationSchedulerId(
			Long communicationGenerationSchedulerId) {
		this.communicationGenerationSchedulerId = communicationGenerationSchedulerId;
	}		

	public SourceProduct getSourceProduct() {
        return sourceProduct;
    }

    public void setSourceProduct(SourceProduct sourceProduct) {
        this.sourceProduct = sourceProduct;
    }

    @Override
	protected void populate(BaseEntity baseEntity, CloneOptions cloneOptions) {
		CommunicationGenerationSchedulerMapping communicationGenerationSchedulerMapping = (CommunicationGenerationSchedulerMapping) baseEntity;
        super.populate(communicationGenerationSchedulerMapping, cloneOptions);
        communicationGenerationSchedulerMapping.setCommunication(communication);
        communicationGenerationSchedulerMapping.setCommunicationGenerationSchedulerId(communicationGenerationSchedulerId);
        communicationGenerationSchedulerMapping.setSourceProduct(sourceProduct);
	}
    
    
    @Override
    protected void populateFrom(BaseEntity baseEntity, CloneOptions cloneOptions) {
    	CommunicationGenerationSchedulerMapping communicationGenerationSchedulerMapping = (CommunicationGenerationSchedulerMapping) baseEntity;
        super.populateFrom(communicationGenerationSchedulerMapping, cloneOptions);
        this.setCommunication(communicationGenerationSchedulerMapping.getCommunication());
        this.setCommunicationGenerationSchedulerId(communicationGenerationSchedulerMapping.getCommunicationGenerationSchedulerId());
        this.setSourceProduct(communicationGenerationSchedulerMapping.getSourceProduct());                      
    }
	
}
