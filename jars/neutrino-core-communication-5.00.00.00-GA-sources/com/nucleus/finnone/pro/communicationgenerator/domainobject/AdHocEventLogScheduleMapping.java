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
@Table(name = "COM_COMMN_ADHOC_SCH_MAPPING")
@Cacheable
@Named("adHocEventLogScheduleMapping")
@Synonym(grant="SELECT")
public class AdHocEventLogScheduleMapping extends BaseMasterEntity{
    
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    @ManyToOne
    private EventCode eventCode;
    
    @Column(name="COM_COMMN_ADHOC_SCH_ID")
    private Long adHocEventLogScheduleId;
    
    @ManyToOne
    @JoinColumn(name="SOURCE_PRODUCT_ID")
    private SourceProduct sourceProduct;
    
    public EventCode getEventCode() {
        return eventCode;
    }

    public void setEventCode(EventCode eventCode) {
        this.eventCode = eventCode;
    }

    public Long getAdHocEventLogScheduleId() {
        return adHocEventLogScheduleId;
    }

    public void setAdHocEventLogScheduleId(Long adHocEventLogScheduleId) {
        this.adHocEventLogScheduleId = adHocEventLogScheduleId;
    }
    
    public SourceProduct getSourceProduct() {
        return sourceProduct;
    }

    public void setSourceProduct(SourceProduct sourceProduct) {
        this.sourceProduct = sourceProduct;
    }

    @Override
    protected void populate(BaseEntity baseEntity, CloneOptions cloneOptions) {
        AdHocEventLogScheduleMapping adHocEventLogScheduleMapping = (AdHocEventLogScheduleMapping) baseEntity;
        super.populate(adHocEventLogScheduleMapping, cloneOptions);
        adHocEventLogScheduleMapping.setEventCode(eventCode);
        adHocEventLogScheduleMapping.setAdHocEventLogScheduleId(adHocEventLogScheduleId);
        adHocEventLogScheduleMapping.setSourceProduct(sourceProduct);
    
    }
    
    
    @Override
    protected void populateFrom(BaseEntity baseEntity, CloneOptions cloneOptions) {
        AdHocEventLogScheduleMapping adHocEventLogScheduleMapping = (AdHocEventLogScheduleMapping) baseEntity;
        super.populateFrom(adHocEventLogScheduleMapping, cloneOptions);
        this.setEventCode(adHocEventLogScheduleMapping.getEventCode());
        this.setAdHocEventLogScheduleId(adHocEventLogScheduleMapping.getAdHocEventLogScheduleId());
        this.setSourceProduct(adHocEventLogScheduleMapping.getSourceProduct());
                      
    }

}
