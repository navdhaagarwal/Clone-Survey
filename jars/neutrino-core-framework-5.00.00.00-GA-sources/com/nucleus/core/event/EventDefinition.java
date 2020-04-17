/**

 * Copyright Nucleus Software India Pvt Ltd. All rights reserved.
 */
package com.nucleus.core.event;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.*;

import com.nucleus.activeInactiveReason.ReasonsActiveInactiveMapping;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.annotations.Synonym;
import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.CloneOptions;
import com.nucleus.master.BaseMasterEntity;

/**
 * @author Nucleus Software India Pvt Ltd 
 */
@Entity
@DynamicUpdate
@DynamicInsert
@Cacheable
@Synonym(grant="ALL")
@Table(indexes={@Index(name="RAIM_PERF_45_4416",columnList="REASON_ACT_INACT_MAP")})
public class EventDefinition extends BaseMasterEntity {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The event code. */
    private String            code;

    /** The event description. */
    private String            description;

 

    private Boolean           orderingRequired;

    /** The event task list. */
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "fk_evnt_defntion")
    @Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
    private List<EventTask>   eventTaskList;

    @OneToOne(cascade = CascadeType.ALL)
    private ReasonsActiveInactiveMapping reasonActInactMap;

    public String getCode() {
        return code;
    }

    /**
     * Sets the code.
     *
     * @param code the new code
     */
    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    
    
    public Boolean getOrderingRequired() {
        return orderingRequired;
    }

    public void setOrderingRequired(Boolean orderingRequired) {
        this.orderingRequired = orderingRequired;
    }

    /**
     * Gets the event task list.
     *
     * @return the event task list
     */
    public List<EventTask> getEventTaskList() {
        return eventTaskList;
    }

    /**
     * Sets the event task list.
     *
     * @param eventTaskList the new event task list
     */
    public void setEventTaskList(List<EventTask> eventTaskList) {
        this.eventTaskList = eventTaskList;
    }

    public ReasonsActiveInactiveMapping getReasonActInactMap() {
        return reasonActInactMap;
    }

    public void setReasonActInactMap(ReasonsActiveInactiveMapping reasonActInactMap) {
        this.reasonActInactMap = reasonActInactMap;
    }

    @Override
    protected void populate(BaseEntity baseEntity, CloneOptions cloneOptions) {
        EventDefinition eventDefinition = (EventDefinition) baseEntity;
        super.populate(eventDefinition, cloneOptions);
        eventDefinition.setCode(code);
        eventDefinition.setDescription(description);
        eventDefinition.setOrderingRequired(orderingRequired);
        if (null != eventTaskList && eventTaskList.size() > 0) {
            List<EventTask> eventTasks = new ArrayList<EventTask>();
            for (EventTask eventTask : eventTaskList) {
                eventTasks.add((EventTask) eventTask.cloneYourself(cloneOptions));
            }
            eventDefinition.setEventTaskList(eventTasks);
        }
        if (reasonActInactMap != null) {
            eventDefinition.setReasonActInactMap((ReasonsActiveInactiveMapping) this.reasonActInactMap.cloneYourself(cloneOptions));
        }
    }

    @Override
    protected void populateFrom(BaseEntity baseEntity, CloneOptions cloneOptions) {
        EventDefinition eventDefinition = (EventDefinition) baseEntity;
        super.populateFrom(eventDefinition, cloneOptions);
        this.setCode(eventDefinition.getCode());
        this.setDescription(eventDefinition.getDescription());
        this.setOrderingRequired(eventDefinition.getOrderingRequired());
        if (eventDefinition.getEventTaskList() != null && eventDefinition.getEventTaskList().size() > 0) {
            getEventTaskList().removeAll(eventTaskList);
            for (EventTask eventTask : eventDefinition.getEventTaskList()) {
                getEventTaskList().add((EventTask) eventTask.cloneYourself(cloneOptions));
            }
        }
        if (eventDefinition.getReasonActInactMap() != null) {
            this.setReasonActInactMap((ReasonsActiveInactiveMapping) eventDefinition.getReasonActInactMap().cloneYourself(cloneOptions));
        }
    }

    @Override
    public String getDisplayName() {
        return getCode();
    }
}
