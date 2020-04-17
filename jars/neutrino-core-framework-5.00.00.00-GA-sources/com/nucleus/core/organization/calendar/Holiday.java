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

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.apache.commons.lang3.ObjectUtils;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.annotations.Synonym;
import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.CloneOptions;
import com.nucleus.master.audit.annotation.EmbedInAuditAsValue;

/**
 * @author Nucleus Software Exports Limited
 * To represent a holiday.
 */
@Entity
@DynamicUpdate
@DynamicInsert
@Synonym(grant="ALL")
@Table(indexes={@Index(name="branch_calendar_fk_index",columnList="branch_calendar_fk")})
public class Holiday extends BaseEntity {

    private static final long serialVersionUID = 568825341991167759L;
    @Column
    @Temporal(TemporalType.DATE)
    //@Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    @EmbedInAuditAsValue(displayKey="label.user.profile.date")
    private Date	          holidayDate;
    
    @EmbedInAuditAsValue(displayKey="label.generic.description")
    private String            description;

    public Holiday(Holiday holiday) {
        this.setDescription(holiday.getDescription());
        this.setHolidayDate(holiday.getHolidayDate());
    }

    public Holiday() {
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return the holidayDate
     */
    public Date getHolidayDate() {
        return holidayDate;
    }

    /**
     * @param holidayDate the holidayDate to set
     */
    public void setHolidayDate(Date holidayDate) {
        this.holidayDate = holidayDate;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 11;
        result = prime * result + ((description == null) ? 0 : description.hashCode());
        result = prime * result + ((holidayDate == null) ? 0 : holidayDate.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        Holiday other = (Holiday) obj;
        if (!ObjectUtils.equals(description, other.description)) {
            return false;
        }
        if (!ObjectUtils.equals(holidayDate, other.holidayDate)) {
            return false;
        }
        return true;
    }

    protected void populate(BaseEntity baseEntity, CloneOptions cloneOptions) {
        Holiday holiday = (Holiday) baseEntity;
        super.populate(holiday, cloneOptions);
        holiday.setDescription(description);
        holiday.setHolidayDate(holidayDate);
    }

}
