/**
 * This file and a proportion of its content is copyright of Nucleus Software Exports Limited - � 2012. All rights reserved.
 * Any redistribution or reproduction of part or all of the contents in any form is prohibited other than the following:
 * - you cannot print or download to a local hard disk extract contents either part or full for personal/ commercial/
 * academic or any other use
 * - you may not copy the content to individual/ third parties for any type of use, either as compiled or source format
 * without the knowledge and consent of Nucleus Software
 * - You may not, except with our express written permission, distribute or commercially exploit the content. Nor may you
 * transmit it or store it in any other web site or other form of electronic retrieval system.
 */
package com.nucleus.core.organization.calendar;

import javax.persistence.Column;
import javax.persistence.Entity;

import org.apache.commons.lang3.ObjectUtils;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

import com.nucleus.core.annotations.Synonym;
import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.CloneOptions;
import com.nucleus.master.audit.annotation.EmbedInAuditAsValue;
import com.nucleus.master.audit.annotation.EmbedInAuditAsValueObject;

/**
 * @author Nucleus Software Exports Limited
 *  To represent working hours for a day.
 */
@Entity
@DynamicUpdate
@DynamicInsert
@Synonym(grant="ALL")
public class DailySchedule extends BaseEntity {

    private static final long serialVersionUID = -4610302021842053099L;
    @Column
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    @EmbedInAuditAsValue(displayKey="label.branchCalendar.openingTime")
    private DateTime          openingTime;
    @Column
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    @EmbedInAuditAsValue(displayKey="label.branchCalendar.closingTime")
    private DateTime          closingTime;
    @Column
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    @EmbedInAuditAsValue(displayKey="label.branchCalendar.lunchFrom")
    private DateTime          lunchFrom;
    @Column
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    @EmbedInAuditAsValue(displayKey="label.branchCalendar.lunchTo")
    private DateTime          lunchTo;

    @EmbedInAuditAsValue(displayKey="label.branchCalendar.workingDay")
    private boolean           workingDay;

    public DailySchedule(DailySchedule dailySchedule) {
        this.setOpeningTime(dailySchedule.getOpeningTime());
        this.setClosingTime(dailySchedule.getClosingTime());
        this.setLunchFrom(dailySchedule.getLunchFrom());
        this.setLunchTo(dailySchedule.getLunchTo());
        this.setWorkingDay(dailySchedule.isWorkingDay());
    }

    public DailySchedule() {
    }

    /**
     * @return the openingTime
     */
    public DateTime getOpeningTime() {
        return openingTime;
    }

    /**
     * @param openingTime the openingTime to set
     */
    public void setOpeningTime(DateTime openingTime) {
        this.openingTime = openingTime;
    }

    /**
     * @return the closingTime
     */
    public DateTime getClosingTime() {
        return closingTime;
    }

    /**
     * @param closingTime the closingTime to set
     */
    public void setClosingTime(DateTime closingTime) {
        this.closingTime = closingTime;
    }

    /**
     * @return the lunchFrom
     */
    public DateTime getLunchFrom() {
        return lunchFrom;
    }

    /**
     * @param lunchFrom the lunchFrom to set
     */
    public void setLunchFrom(DateTime lunchFrom) {
        this.lunchFrom = lunchFrom;
    }

    /**
     * @return the lunchTo
     */
    public DateTime getLunchTo() {
        return lunchTo;
    }

    /**
     * @param lunchTo the lunchTo to set
     */
    public void setLunchTo(DateTime lunchTo) {
        this.lunchTo = lunchTo;
    }

    /**
     * @return the workingDay
     */
    public boolean isWorkingDay() {
        return workingDay;
    }

    /**
     * @param workingDay the workingDay to set
     */
    public void setWorkingDay(boolean workingDay) {
        this.workingDay = workingDay;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 11;
        result = prime * result + ((closingTime == null) ? 0 : closingTime.hashCode());
        result = prime * result + ((lunchFrom == null) ? 0 : lunchFrom.hashCode());
        result = prime * result + ((lunchTo == null) ? 0 : lunchTo.hashCode());
        result = prime * result + ((openingTime == null) ? 0 : openingTime.hashCode());
        result = prime * result + (workingDay ? 1231 : 1237);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        DailySchedule other = (DailySchedule) obj;
        if (!ObjectUtils.equals(closingTime, other.closingTime)) {
            return false;
        }
        if (!ObjectUtils.equals(openingTime, other.openingTime)) {
            return false;
        }
        if (!ObjectUtils.equals(lunchFrom, other.lunchFrom)) {
            return false;
        }
        if (!ObjectUtils.equals(lunchTo, other.lunchTo)) {
            return false;
        }
        if (workingDay != other.workingDay) {
            return false;
        }
        return true;
    }

    protected void populate(BaseEntity baseEntity, CloneOptions cloneOptions) {
        DailySchedule dailySchedule = (DailySchedule) baseEntity;
        super.populate(dailySchedule, cloneOptions);
        dailySchedule.setClosingTime(closingTime);
        dailySchedule.setLunchFrom(lunchFrom);
        dailySchedule.setLunchTo(lunchTo);
        dailySchedule.setOpeningTime(openingTime);
        dailySchedule.setWorkingDay(workingDay);
    }

    @Override
    protected void populateFrom(BaseEntity copyEntity, CloneOptions cloneOptions) {
    	 DailySchedule dailySchedule = (DailySchedule) copyEntity;
    	super.populateFrom(dailySchedule, cloneOptions);
    	 this.setClosingTime(dailySchedule.getClosingTime());
    	 this.setLunchFrom(dailySchedule.getLunchFrom());
    	 this.setLunchTo(dailySchedule.getLunchTo());
    	 this.setOpeningTime(dailySchedule.getOpeningTime());
    	 this.setWorkingDay(dailySchedule.isWorkingDay());
    }
}
