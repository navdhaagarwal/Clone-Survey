package com.nucleus.finnone.pro.communicationgenerator.domainobject;

import static com.nucleus.finnone.pro.general.util.ValidatorUtils.hasElements;


import java.util.ArrayList;
import java.util.List;

import javax.inject.Named;
import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.annotations.Synonym;
import com.nucleus.core.event.EventCodeType;
import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.CloneOptions;
import static com.nucleus.finnone.pro.general.util.ValidatorUtils.isNull;

@Entity
@DynamicUpdate
@DynamicInsert
@Table(name = "COM_COMMN_ADHOC_EVENT_SCH")
@Cacheable
@Named("adHocEventLogSchedule")
@Synonym(grant="SELECT")
public class AdHocEventLogSchedule extends CommunicationSchedulerBase {

    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;
    
    
    @ManyToOne
    @JoinColumn(name="EVENT_CODE_TYPE_ID")
    private EventCodeType eventCodeType;
    
    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "COM_COMMN_ADHOC_SCH_ID")
    private List<AdHocEventLogScheduleMapping> adHocEventLogScheduleMappings;
    
    private Boolean generateMergedFile;
    
    public EventCodeType getEventCodeType() {
        return eventCodeType;
    }

    public void setEventCodeType(EventCodeType eventCodeType) {
        this.eventCodeType = eventCodeType;
    }

    public List<AdHocEventLogScheduleMapping> getAdHocEventLogScheduleMappings() {
        return adHocEventLogScheduleMappings;
    }

    public void setAdHocEventLogScheduleMappings(
            List<AdHocEventLogScheduleMapping> adHocEventLogScheduleMappings) {
        this.adHocEventLogScheduleMappings = adHocEventLogScheduleMappings;
    }    
    
    public Boolean getGenerateMergedFile() {
    	if (isNull(this.generateMergedFile)){
    		generateMergedFile=false;
    	}
		return generateMergedFile;
	}

	public void setGenerateMergedFile(Boolean generateMergedFile) {
		this.generateMergedFile = generateMergedFile;
	}
	
	@Override
    protected void populate(BaseEntity baseEntity, CloneOptions cloneOptions) {
        AdHocEventLogSchedule adHocEventLogSchedule = (AdHocEventLogSchedule) baseEntity;
        super.populate(adHocEventLogSchedule, cloneOptions);
        adHocEventLogSchedule.setSchedulerName(getSchedulerName());
        adHocEventLogSchedule.setCronExpression(getCronExpression());
        adHocEventLogSchedule.setCronBuilderSelector(getCronBuilderSelector());
        adHocEventLogSchedule
                .setMaintainExecutionLog(getMaintainExecutionLog());
        adHocEventLogSchedule.setSourceProduct(getSourceProduct());
        adHocEventLogSchedule.setRunOnHoliday(getRunOnHoliday());
        adHocEventLogSchedule.setEndDate(getEndDate());
        adHocEventLogSchedule.setEventCodeType(getEventCodeType());
        adHocEventLogSchedule.setGenerateMergedFile(getGenerateMergedFile());
        if (hasElements(adHocEventLogScheduleMappings)) {
            List<AdHocEventLogScheduleMapping> cloneAdHocEventLogMap = new ArrayList<AdHocEventLogScheduleMapping>();
            for (AdHocEventLogScheduleMapping adHocEventLogScheduleMapping : adHocEventLogScheduleMappings) {
                cloneAdHocEventLogMap
                        .add((AdHocEventLogScheduleMapping) adHocEventLogScheduleMapping
                                .cloneYourself(cloneOptions));
            }
            adHocEventLogSchedule
                    .setAdHocEventLogScheduleMappings(cloneAdHocEventLogMap);
        }

    }

    @Override
    protected void populateFrom(BaseEntity baseEntity, CloneOptions cloneOptions) {
        AdHocEventLogSchedule adHocEventLogSchedule = (AdHocEventLogSchedule) baseEntity;
        super.populateFrom(adHocEventLogSchedule, cloneOptions);
        this.setSchedulerName(adHocEventLogSchedule.getSchedulerName());
        this.setCronExpression(adHocEventLogSchedule.getCronExpression());
        this.setCronBuilderSelector(adHocEventLogSchedule
                .getCronBuilderSelector());
        this.setMaintainExecutionLog(adHocEventLogSchedule
                .getMaintainExecutionLog());
        this.setSourceProduct(adHocEventLogSchedule.getSourceProduct());
        this.setRunOnHoliday(adHocEventLogSchedule.getRunOnHoliday());
        this.setEndDate(adHocEventLogSchedule.getEndDate());
        this.setEventCodeType(adHocEventLogSchedule.getEventCodeType());
        this.setGenerateMergedFile(adHocEventLogSchedule.getGenerateMergedFile());
        if (hasElements(adHocEventLogSchedule
                .getAdHocEventLogScheduleMappings())) {
            this.getAdHocEventLogScheduleMappings().clear();
            for (AdHocEventLogScheduleMapping adHocEventLogScheduleMapping : adHocEventLogSchedule
                    .getAdHocEventLogScheduleMappings()) {
                this.getAdHocEventLogScheduleMappings()
                        .add((AdHocEventLogScheduleMapping) adHocEventLogScheduleMapping
                                .cloneYourself(cloneOptions));
            }
        }
    }
    
    @Override
    public String getDisplayName() {
        return getSchedulerName();
    }

}
