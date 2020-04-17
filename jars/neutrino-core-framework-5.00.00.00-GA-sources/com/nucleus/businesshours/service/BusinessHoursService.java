/**
 * This file and a proportion of its content is copyright of Nucleus Software Exports Limited - © 2012. All rights reserved.
 * Any redistribution or reproduction of part or all of the contents in any form is prohibited other than the following:
 * - you cannot print or download to a local hard disk extract contents either part or full for personal/ commercial/
 * academic or any other use
 * - you may not copy the content to individual/ third parties for any type of use, either as compiled or source format
 * without the knowledge and consent of Nucleus Software
 * - You may not, except with our express written permission, distribute or commercially exploit the content. Nor may you
 * transmit it or store it in any other web site or other form of electronic retrieval system.
 */
package com.nucleus.businesshours.service;

import org.joda.time.DateTime;

import com.nucleus.businesshours.service.BusinessHoursServiceImpl.SchedularClass;
import com.nucleus.core.organization.calendar.BranchCalendar;
import com.nucleus.core.organization.entity.OrganizationBranch;
import com.nucleus.core.organization.entity.OrganizationType;
import org.joda.time.LocalTime;

/**
 * @author Nucleus Software Exports Limited
 */
public interface BusinessHoursService {

    /**
     * Gets the due date based on response time, branch calendar and the date on which task has been reported. 
     *
     * @param branchCalendar the branch calendar consists the daily schedule and holiday list of the branch.
     * @param issueReportedAt the date on which the task has been reported.
     * @param durationInMillis the response time within which the task has to be completed
     * @return the calculated due date.
     */
    public DateTime getCalculatedDueDate(BranchCalendar branchCalendar, DateTime issueReportedAt, Long durationInMillis);

    /**
     * Creates a new Organization Branch. 
     *
     * @param branch the branch to persist.
     */
    public void createOrganizationBranch(OrganizationBranch branch);

    /**
     * Creates a new Organization Type. 
     *
     * @param orgType the organization type to persist.
     */
    public void createOrganizationType(OrganizationType orgType);

    /**
     * Gets the remaining response time based on due date, branch calendar and the date on which task has been reported. 
     *
     * @param branchCalendar the branch calendar consists the daily schedule and holiday list of the branch.
     * @param issueReportedAt the date on which the task has been reported.
     * @param dueDate the boundary date within which the task has to be completed
     * @return the remaining response time.
     */
    public long getRemainingResponseTime(BranchCalendar branchCalendar, DateTime issueReportedAt, DateTime dueDate);

    /**
     * Gets the start and stop schedule based on due date, branch calendar and the date on which task has been reported. 
     *
     * @param branchCalendar the branch calendar consists the daily schedule and holiday list of the branch.
     * @param issueReportedAt the date on which the task has been reported.
     * @param expectedDate the boundary date within which the task has to be completed
     * @param lastScheduleGenDate TODO
     * @return ScheduleClass which consists the schedule.
     */
    public SchedularClass getStartOrStopSchedule(BranchCalendar branchCalendar, DateTime issueReportedAt,
            DateTime expectedDate, DateTime lastScheduleGenDate);

    /**
     * Gets the next working day based on branch calendar and the date from which next working day has been calculated. 
     *
     * @param branchCalendar the branch calendar consists the daily schedule and holiday list of the branch.
     * @param workingDay the date from which next working day has been calculated.
     * @return calendar of the next working day.
     */
    public DateTime getNextWorkingDay(BranchCalendar branchCalendar, DateTime workingDay);

    public DateTime getNextWorkingDayforReassign(BranchCalendar var1, DateTime var2, LocalTime var3);

}
