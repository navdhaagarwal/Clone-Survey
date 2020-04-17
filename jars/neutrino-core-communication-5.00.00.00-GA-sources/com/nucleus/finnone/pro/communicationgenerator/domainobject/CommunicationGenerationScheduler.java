package com.nucleus.finnone.pro.communicationgenerator.domainobject;

import static com.nucleus.finnone.pro.general.util.ValidatorUtils.hasElements;

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

@Entity
@Cacheable
@DynamicInsert
@DynamicUpdate
@Table(name = "COM_COMMN_GENERATION_SCHEDULER")
@Named("communicationGenerationScheduler")
@Synonym(grant="ALL")
public class CommunicationGenerationScheduler extends
        CommunicationSchedulerBase {

    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;
    
  
    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "COM_COMMN_GENERATION_SCH_ID")
    private List<CommunicationGenerationSchedulerMapping> communicationGenerationSchedulerMappings;

    public List<CommunicationGenerationSchedulerMapping> getCommunicationGenerationSchedulerMappings() {
        return communicationGenerationSchedulerMappings;
    }

    public void setCommunicationGenerationSchedulerMappings(
            List<CommunicationGenerationSchedulerMapping> communicationGenerationSchedulerMappings) {
        this.communicationGenerationSchedulerMappings = communicationGenerationSchedulerMappings;
    }

    @Override
    protected void populate(BaseEntity baseEntity, CloneOptions cloneOptions) {
        CommunicationGenerationScheduler communicationGenerationScheduler = (CommunicationGenerationScheduler) baseEntity;
        super.populate(communicationGenerationScheduler, cloneOptions);
        communicationGenerationScheduler.setSchedulerName(getSchedulerName());
        communicationGenerationScheduler.setCronExpression(getCronExpression());
        communicationGenerationScheduler
                .setCronBuilderSelector(getCronBuilderSelector());
        communicationGenerationScheduler
                .setMaintainExecutionLog(getMaintainExecutionLog());
        communicationGenerationScheduler.setSourceProduct(getSourceProduct());
        communicationGenerationScheduler.setRunOnHoliday(getRunOnHoliday());
        communicationGenerationScheduler.setEndDate(getEndDate());

        if (hasElements(communicationGenerationSchedulerMappings)) {
            List<CommunicationGenerationSchedulerMapping> cloneCommGenReqMap = new ArrayList<CommunicationGenerationSchedulerMapping>();
            for (CommunicationGenerationSchedulerMapping communicationGenerationSchedulerMapping : communicationGenerationSchedulerMappings) {
                cloneCommGenReqMap
                        .add((CommunicationGenerationSchedulerMapping) communicationGenerationSchedulerMapping
                                .cloneYourself(cloneOptions));
            }
            communicationGenerationScheduler
                    .setCommunicationGenerationSchedulerMappings(cloneCommGenReqMap);
        }

    }

    @Override
    protected void populateFrom(BaseEntity baseEntity, CloneOptions cloneOptions) {
        CommunicationGenerationScheduler communicationGenerationScheduler = (CommunicationGenerationScheduler) baseEntity;
        super.populateFrom(communicationGenerationScheduler, cloneOptions);
        this.setSchedulerName(communicationGenerationScheduler
                .getSchedulerName());
        this.setCronExpression(communicationGenerationScheduler
                .getCronExpression());
        this.setCronBuilderSelector(communicationGenerationScheduler
                .getCronBuilderSelector());
        this.setMaintainExecutionLog(communicationGenerationScheduler
                .getMaintainExecutionLog());
        this.setSourceProduct(communicationGenerationScheduler
                .getSourceProduct());
        this.setRunOnHoliday(communicationGenerationScheduler.getRunOnHoliday());
        this.setEndDate(communicationGenerationScheduler.getEndDate());
        if (hasElements(communicationGenerationScheduler
                .getCommunicationGenerationSchedulerMappings())) {
            this.getCommunicationGenerationSchedulerMappings().clear();
            for (CommunicationGenerationSchedulerMapping communicationGenerationSchedulerMapping : communicationGenerationScheduler
                    .getCommunicationGenerationSchedulerMappings()) {
                this.getCommunicationGenerationSchedulerMappings()
                        .add((CommunicationGenerationSchedulerMapping) communicationGenerationSchedulerMapping
                                .cloneYourself(cloneOptions));
            }
        }
    }
    
    @Override
    public String getDisplayName() {
        return getSchedulerName();
    }
}
