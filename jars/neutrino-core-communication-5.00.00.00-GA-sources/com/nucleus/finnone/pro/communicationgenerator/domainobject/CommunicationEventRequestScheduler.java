package com.nucleus.finnone.pro.communicationgenerator.domainobject;

import java.util.ArrayList;


import java.util.List;

import javax.inject.Named;
import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.annotations.Synonym;
import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.CloneOptions;
import static com.nucleus.finnone.pro.general.util.ValidatorUtils.hasElements;


@Entity
@DynamicUpdate
@DynamicInsert
@Table(name="COM_COMMN_EVENT_REQ_SCHEDULER")
@Cacheable
@Named("communicationEventRequestScheduler")
@Synonym(grant="ALL")
public class CommunicationEventRequestScheduler extends CommunicationSchedulerBase{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
	
	
	@OneToMany(cascade=CascadeType.ALL)
	@JoinColumn(name = "COM_COMMN_EVENT_REQ_SCH_ID")
	private List<CommunicationEventRequestSchedulerMapping> communicationEventRequestSchedulerMappings;
	

	public List<CommunicationEventRequestSchedulerMapping> getCommunicationEventRequestSchedulerMappings() {
		return communicationEventRequestSchedulerMappings;
	}

	public void setCommunicationEventRequestSchedulerMappings(
			List<CommunicationEventRequestSchedulerMapping> communicationEventRequestSchedulerMappings) {
		this.communicationEventRequestSchedulerMappings = communicationEventRequestSchedulerMappings;
	}

	@Override
    protected void populate(BaseEntity baseEntity, CloneOptions cloneOptions) {
      CommunicationEventRequestScheduler communicationEventRequestScheduler = (CommunicationEventRequestScheduler) baseEntity;
      super.populate(communicationEventRequestScheduler, cloneOptions);
      communicationEventRequestScheduler.setSchedulerName(getSchedulerName());
      communicationEventRequestScheduler.setCronExpression(getCronExpression());
      communicationEventRequestScheduler.setCronBuilderSelector(getCronBuilderSelector());
      communicationEventRequestScheduler.setMaintainExecutionLog(getMaintainExecutionLog());
      communicationEventRequestScheduler.setSourceProduct(getSourceProduct());
      communicationEventRequestScheduler.setRunOnHoliday(getRunOnHoliday());
      communicationEventRequestScheduler.setEndDate(getEndDate());
      if (hasElements(communicationEventRequestSchedulerMappings)) {
          List<CommunicationEventRequestSchedulerMapping> cloneCommEventReqMap = new ArrayList<CommunicationEventRequestSchedulerMapping>();
          for (CommunicationEventRequestSchedulerMapping communicationEventRequestSchedulerMapping : communicationEventRequestSchedulerMappings) {
        	  cloneCommEventReqMap.add((CommunicationEventRequestSchedulerMapping) communicationEventRequestSchedulerMapping.cloneYourself(cloneOptions));
          }
          communicationEventRequestScheduler.setCommunicationEventRequestSchedulerMappings(cloneCommEventReqMap);
      }
      
      
    }

    @Override
    protected void populateFrom(BaseEntity baseEntity, CloneOptions cloneOptions) {
    	CommunicationEventRequestScheduler communicationEventRequestScheduler = (CommunicationEventRequestScheduler) baseEntity;
       super.populateFrom(communicationEventRequestScheduler, cloneOptions);
       this.setSchedulerName(communicationEventRequestScheduler.getSchedulerName());
       this.setCronExpression(communicationEventRequestScheduler.getCronExpression());
       this.setCronBuilderSelector(communicationEventRequestScheduler.getCronBuilderSelector());
       this.setMaintainExecutionLog(communicationEventRequestScheduler.getMaintainExecutionLog());
       this.setSourceProduct(communicationEventRequestScheduler.getSourceProduct());
       this.setRunOnHoliday(communicationEventRequestScheduler.getRunOnHoliday());
       this.setEndDate(communicationEventRequestScheduler.getEndDate());
       if (hasElements(communicationEventRequestScheduler.getCommunicationEventRequestSchedulerMappings())) {
           this.getCommunicationEventRequestSchedulerMappings().clear();
           for (CommunicationEventRequestSchedulerMapping communicationEventRequestSchedulerMapping : communicationEventRequestScheduler
                   .getCommunicationEventRequestSchedulerMappings()) {
               this.getCommunicationEventRequestSchedulerMappings().add(
                       (CommunicationEventRequestSchedulerMapping) communicationEventRequestSchedulerMapping.cloneYourself(cloneOptions));
           }
       }
    }
    
    @Override
    public String getDisplayName() {
        return getSchedulerName();
    }
	
}
