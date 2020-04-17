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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.inject.Named;

import org.apache.commons.lang3.time.DateUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.LocalTime;

import com.nucleus.core.exceptions.InvalidDataException;
import com.nucleus.core.organization.calendar.BranchCalendar;
import com.nucleus.core.organization.calendar.DailySchedule;
import com.nucleus.core.organization.calendar.Holiday;
import com.nucleus.core.organization.entity.OrganizationBranch;
import com.nucleus.core.organization.entity.OrganizationType;
import com.nucleus.core.validation.util.NeutrinoValidator;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.service.BaseServiceImpl;

import net.bull.javamelody.MonitoredWithSpring;

/**
 * @author Nucleus Software Exports Limited
 */

@Named(value = "businessHoursService")
public class BusinessHoursServiceImpl extends BaseServiceImpl implements BusinessHoursService {

    @Override
    @MonitoredWithSpring(name = "BHSI_FETCH_DUE_DATE")
    public DateTime getCalculatedDueDate(BranchCalendar branchCalendar, DateTime issueReportedAt, Long durationInMillis) {
        NeutrinoValidator.notNull(issueReportedAt, "Issue reported time can not be null");
        NeutrinoValidator.notNull(durationInMillis, "Response time can not be null");
        DateTime issueReprtedAtClone = new DateTime(issueReportedAt.toDate());
        if (branchCalendar == null) {
            issueReprtedAtClone = issueReprtedAtClone.withMillis(issueReprtedAtClone.getMillis() + durationInMillis);
            // issueReprtedAtClone.setTimeInMillis(issueReprtedAtClone.getTimeInMillis() + durationInMillis);
            return issueReprtedAtClone;
        }

        DateTime actualDate = new DateTime(issueReprtedAtClone.getYear(), issueReprtedAtClone.getMonthOfYear(),
                issueReprtedAtClone.dayOfMonth().getMaximumValue(), issueReprtedAtClone.getHourOfDay(),
                issueReprtedAtClone.getMinuteOfHour(), issueReprtedAtClone.getSecondOfMinute(), 0);
        /*actualDate.set(Calendar.DATE, issueReprtedAtClone.getActualMaximum(Calendar.DATE));
        actualDate.set(Calendar.MONTH, issueReprtedAtClone.get(Calendar.MONTH));
        actualDate.set(Calendar.YEAR, issueReprtedAtClone.get(Calendar.YEAR));*/
        DateTime expectedDate;

        if (branchCalendar != null && !branchCalendar.getMoveAheadOnMonthEnd()) {
            DateTime expectedMonthEnd = geExpectedMonthEndDate(branchCalendar, actualDate);
            if (issueReprtedAtClone.compareTo(expectedMonthEnd) <= 0) {
                expectedDate = calculateResponseTime(branchCalendar, issueReprtedAtClone, durationInMillis, false);
            } else {
                int fromHour = 0;
                int fromMinute = 0;
                issueReprtedAtClone = issueReprtedAtClone.plusMonths(1);
                issueReprtedAtClone = issueReprtedAtClone.withDate(issueReprtedAtClone.getYear(),
                        issueReprtedAtClone.getMonthOfYear(), issueReprtedAtClone.dayOfMonth().getMinimumValue());
                DailySchedule dailySchedule = getDailySchedule(branchCalendar, issueReprtedAtClone);
                if (dailySchedule != null) {
                    fromHour = dailySchedule.getOpeningTime().getHourOfDay();
                    fromMinute = dailySchedule.getOpeningTime().getMinuteOfHour();
                }
                issueReprtedAtClone = issueReprtedAtClone.withHourOfDay(fromHour);
                issueReprtedAtClone = issueReprtedAtClone.withMinuteOfHour(fromMinute);
                expectedDate = calculateResponseTime(branchCalendar, issueReprtedAtClone, durationInMillis, false);
            }
        } else {
            expectedDate = calculateResponseTime(branchCalendar, issueReprtedAtClone, durationInMillis, true);
        }
        return expectedDate;
    }

    private DateTime geExpectedMonthEndDate(BranchCalendar branchCalendar, DateTime actualDate) {
        int toHour = 0;
        int toMinute = 0;
        DateTime expectedMonthEnd = new DateTime(actualDate.toDate());
        DateTime monthEndDate = null;

        while (monthEndDate == null) {
            DailySchedule dailySchedule = getDailySchedule(branchCalendar, expectedMonthEnd);
            if (dailySchedule != null) {
                toHour = dailySchedule.getClosingTime().getHourOfDay();
                toMinute = dailySchedule.getClosingTime().getMinuteOfHour();
            }
            if (dailySchedule == null || isHoliday(branchCalendar, expectedMonthEnd)) {
                expectedMonthEnd = expectedMonthEnd.plusDays(-1);
                continue;
            } else {
                expectedMonthEnd = expectedMonthEnd.withHourOfDay(toHour);
                expectedMonthEnd = expectedMonthEnd.withMinuteOfHour(toMinute);
                monthEndDate = new DateTime(expectedMonthEnd.toDate());
            }
        }
        return monthEndDate;

    }

    private DateTime calculateResponseTime(BranchCalendar branchCalendar, DateTime issueReportedAt, Long durationInMillis,
            Boolean moveAheadOnMonthEnd) {

        int fromHour = 0;
        int fromMinute = 0;
        int toHour = 0;
        int toMinute = 0;

        DateTime dueDate = null;
        while (durationInMillis > 0 && dueDate == null) {

            DateTime issueReportedDate = new DateTime(issueReportedAt.toDate());

            int hourOfDay = issueReportedAt.getHourOfDay();

            // Gets the daily schedule of the branch so that the from and to hours can be adjusted accordingly.
            DailySchedule dailySchedule = null;
            if (branchCalendar != null) {
                dailySchedule = getDailySchedule(branchCalendar, issueReportedAt);
            }
            if (dailySchedule != null && dailySchedule.getOpeningTime() != null && dailySchedule.getClosingTime() != null) {
                fromHour = dailySchedule.getOpeningTime().getHourOfDay();
                fromMinute = dailySchedule.getOpeningTime().getMinuteOfHour();
                toHour = dailySchedule.getClosingTime().getHourOfDay();
                toMinute = dailySchedule.getClosingTime().getMinuteOfHour();
            }

            // checks the hourOfDay if it is less than fromHour than adjust it as per the branch opening time.
            // This will help in calculating the remaining time of the task
            if (hourOfDay < fromHour) {
                issueReportedAt = issueReportedAt.withHourOfDay(fromHour);
                issueReportedAt = issueReportedAt.withMinuteOfHour(fromMinute);
            }

            // This checks will help in the situation where a task has been allocated after the closing time of the branch.
            // This will move the task to next day.
            if (hourOfDay >= toHour && toHour != 0) {
                issueReportedAt = issueReportedAt.plusDays(1);
                issueReportedAt = issueReportedAt.withHourOfDay(fromHour);
                issueReportedAt = issueReportedAt.withMinuteOfHour(fromMinute);
                issueReportedDate = new DateTime(issueReportedAt.toDate());
                continue;
            }

            // Checks if the reporting day will fall on any of the working day or not.
            if ((dailySchedule != null) && !isHoliday(branchCalendar, issueReportedAt)) {
                int hour = issueReportedAt.getHourOfDay();
                int minute = issueReportedAt.getMinuteOfHour();

                // Calculates at what time the task has been reported.
                long dateMilliseconds = ((hour * 60) + minute) * 60 * 1000L;

                // Calculates what is the closing time of the branch.
                long dayPartEndMilleseconds = ((toHour * 60) + toMinute) * 60 * 1000L;

                // Calculates the remaining time of the day to complete the task
                long millisecondsInThisDayPart = dayPartEndMilleseconds - dateMilliseconds;

                // Checks if the response time lies within the day of reporting.
                // If yes, calculates the due date
                // If no, calculates the remaining time of the task to respond.
                if (durationInMillis <= millisecondsInThisDayPart) {
                    dueDate = new DateTime(issueReportedAt.getMillis() + durationInMillis);
                } else {
                    durationInMillis = durationInMillis - millisecondsInThisDayPart;
                    issueReportedAt = issueReportedAt.plusDays(1);
                    issueReportedAt = issueReportedAt.withHourOfDay(fromHour);
                    issueReportedAt = issueReportedAt.withMinuteOfHour(fromMinute);

                    if ((issueReportedDate.getMonthOfYear() != issueReportedAt.getMonthOfYear()) && !moveAheadOnMonthEnd) {
                        dueDate = calculatePrecedingDueDate(branchCalendar, issueReportedDate);
                        return dueDate;
                    }
                    continue;
                }
            } else {
                issueReportedAt = issueReportedAt.plusDays(1);
                issueReportedAt = issueReportedAt.withHourOfDay(fromHour);
                issueReportedAt = issueReportedAt.withMinuteOfHour(fromMinute);

                if ((issueReportedDate.getMonthOfYear() != issueReportedAt.getMonthOfYear()) && !moveAheadOnMonthEnd) {
                    dueDate = calculatePrecedingDueDate(branchCalendar, issueReportedDate);
                    return dueDate;
                }
                continue;
            }
        }

        return dueDate;

    }

    private DateTime calculatePrecedingDueDate(BranchCalendar branchCalendar, DateTime issueReportedDate) {
        int fromHour = 0;
        int fromMinute = 0;
        int toHour = 0;
        int toMinute = 0;
        DateTime dueDate = null;

        while (dueDate == null) {
            // Gets the daily schedule of the branch so that the from and to hours can be adjusted accordingly.
            DailySchedule dailySchedule = null;
            if (branchCalendar != null) {
                dailySchedule = getDailySchedule(branchCalendar, issueReportedDate);
            }
            if (dailySchedule != null) {
                fromHour = dailySchedule.getOpeningTime().getHourOfDay();
                fromMinute = dailySchedule.getOpeningTime().getMinuteOfHour();
                toHour = dailySchedule.getClosingTime().getHourOfDay();
                toMinute = dailySchedule.getClosingTime().getMinuteOfHour();

            }
            if (!isHoliday(branchCalendar, issueReportedDate) && dailySchedule != null) {
                dueDate = issueReportedDate;
                dueDate = dueDate.withHourOfDay(toHour);
                dueDate = dueDate.withMinuteOfHour(toMinute);
            } else {
                issueReportedDate = issueReportedDate.plusDays(-1);
                issueReportedDate = issueReportedDate.plusHours(fromHour);
                issueReportedDate = issueReportedDate.plusMinutes(fromMinute);
                continue;
            }
        }
        return dueDate;
    }

    @Override
    @MonitoredWithSpring(name = "BHSI_FETCH_REMAIN_RESPONSE_TIME")
    public long getRemainingResponseTime(BranchCalendar branchCalendar, DateTime issueReportedAt, DateTime dueDate) {
        NeutrinoValidator.notNull(branchCalendar, "Branch Calendar for which due date is to be calculated can not be null");
        NeutrinoValidator.notNull(issueReportedAt, "Issue reported time can not be null");
        NeutrinoValidator.notNull(dueDate, "Due Date can not be null");
        long diff = 0;
        long actualRemainingTime = 0;
        long addExtraHours = 0;
        DateTime issueReprtedAtClone = new DateTime(issueReportedAt.toDate());
        // Calculate total time difference from issue reported date and due date.
        diff = dueDate.getMillis() - issueReprtedAtClone.getMillis();
        actualRemainingTime = diff - getNonWorkingTime(branchCalendar, issueReprtedAtClone, dueDate, addExtraHours);
        BaseLoggers.flowLogger.debug("getRemainingResponseTime " + actualRemainingTime);
        return actualRemainingTime;
    }

    @Override
    public SchedularClass getStartOrStopSchedule(BranchCalendar branchCalendar, DateTime issueReportedAt, DateTime dueDate,
            DateTime lastScheduleGenDate) {
        NeutrinoValidator.notNull(branchCalendar, "Branch Calendar for which due date is to be calculated can not be null");
        NeutrinoValidator.notNull(issueReportedAt, "Issue reported time can not be null");
        NeutrinoValidator.notNull(dueDate, "Due Date can not be null");
        SchedularClass schedularClass = new SchedularClass();
        DateTime issueReprtedAtClone = new DateTime(issueReportedAt.toDate());
        List<Long> startStopList = new ArrayList<Long>();
        if (lastScheduleGenDate == null || lastScheduleGenDate.compareTo(dueDate) < 0) {
            lastScheduleGenDate = dueDate;
        }
        return getSchedule(branchCalendar, issueReprtedAtClone, lastScheduleGenDate, schedularClass, startStopList);
    }

    @Override
    public DateTime getNextWorkingDay(BranchCalendar branchCalendar, DateTime workingDay) {
        NeutrinoValidator.notNull(branchCalendar, "Branch Calendar for which due date is to be calculated can not be null");
        NeutrinoValidator.notNull(workingDay, "Working Day can not be null");
        DateTime workingDayClone = new DateTime(workingDay.toDate());
        DateTime nextWorkingDay = null;
        while (nextWorkingDay == null) {
            DailySchedule dailySchedule = getDailySchedule(branchCalendar, workingDayClone);
            if (dailySchedule == null || isHoliday(branchCalendar, workingDayClone)) {
                workingDayClone = workingDayClone.plusDays(1);
                continue;
            } else {
                workingDayClone = workingDayClone.withHourOfDay(dailySchedule.getOpeningTime().getHourOfDay());
                workingDayClone = workingDayClone.withMinuteOfHour(dailySchedule.getOpeningTime().getMinuteOfHour());
                workingDayClone = workingDayClone.withSecondOfMinute(dailySchedule.getOpeningTime().getSecondOfMinute());
                nextWorkingDay = new DateTime(workingDayClone.toDate());
            }
        }
        return nextWorkingDay;
    }

    private SchedularClass getSchedule(BranchCalendar branchCalendar, DateTime issueReportedAt, DateTime expectedDate,
            SchedularClass schedularClass, List<Long> startStopList) {
        // AuthenticationManagerDelegator a = new AuthenticationManagerDelegator();
        int fromHour = 0;
        int fromMinute = 0;
        int toHour = 0;
        int toMinute = 0;
        int fromSeconds = 0;
        int toSeconds = 0;

        boolean isSchedulerRunningStatusNotFilled = true;

        while (issueReportedAt.compareTo(expectedDate) <= 0) {

            int hourOfDay = issueReportedAt.getHourOfDay();
            DailySchedule dailySchedule = getDailySchedule(branchCalendar, issueReportedAt);

            if (dailySchedule != null) {
                fromHour = dailySchedule.getOpeningTime().getHourOfDay();
                fromMinute = dailySchedule.getOpeningTime().getMinuteOfHour();
                fromSeconds = dailySchedule.getOpeningTime().getSecondOfMinute();
                toHour = dailySchedule.getClosingTime().getHourOfDay();
                toMinute = dailySchedule.getClosingTime().getMinuteOfHour();
                toSeconds = dailySchedule.getClosingTime().getSecondOfMinute();
            }

            if (dailySchedule == null || isHoliday(branchCalendar, issueReportedAt) || hourOfDay > toHour) {
                if (isSchedulerRunningStatusNotFilled) {
                    schedularClass.setIsRunning(false);
                    isSchedulerRunningStatusNotFilled = false;
                }
                issueReportedAt = issueReportedAt.plusDays(1);
                DailySchedule tempDailySchedule = getDailySchedule(branchCalendar, issueReportedAt);
                if (tempDailySchedule != null) {
                    issueReportedAt = issueReportedAt.withHourOfDay(tempDailySchedule.getOpeningTime().getHourOfDay());
                    issueReportedAt = issueReportedAt.withMinuteOfHour(tempDailySchedule.getOpeningTime().getMinuteOfHour());
                    issueReportedAt = issueReportedAt.withSecondOfMinute(tempDailySchedule.getOpeningTime()
                            .getSecondOfMinute());
                }
                continue;
            }

            if (hourOfDay < fromHour) {
                if (isSchedulerRunningStatusNotFilled) {
                    schedularClass.setIsRunning(false);
                    isSchedulerRunningStatusNotFilled = false;
                }
                issueReportedAt = issueReportedAt.withHourOfDay(fromHour);
                issueReportedAt = issueReportedAt.withMinuteOfHour(fromMinute);
                issueReportedAt = issueReportedAt.withSecondOfMinute(fromSeconds);
                continue;
            } else {
                DateTime tempCalendar1 = new DateTime(issueReportedAt.toDate());
                tempCalendar1 = tempCalendar1.withHourOfDay(fromHour);
                tempCalendar1 = tempCalendar1.withMinuteOfHour(fromMinute);
                tempCalendar1 = tempCalendar1.withSecondOfMinute(fromSeconds);
                if (issueReportedAt.compareTo(tempCalendar1) <= 0) {
                    long startTime = tempCalendar1.getMillis();
                    startStopList.add(startTime);
                }
                tempCalendar1 = tempCalendar1.withHourOfDay(toHour);
                tempCalendar1 = tempCalendar1.withMinuteOfHour(toMinute);
                tempCalendar1 = tempCalendar1.withSecondOfMinute(toSeconds);
                long stopTime = tempCalendar1.getMillis();
                startStopList.add(stopTime);
                if (isSchedulerRunningStatusNotFilled) {
                    schedularClass.setIsRunning(true);
                    isSchedulerRunningStatusNotFilled = false;
                }
                issueReportedAt = issueReportedAt.plusDays(1);
                DailySchedule tempDailySchedule1 = getDailySchedule(branchCalendar, issueReportedAt);
                if (tempDailySchedule1 != null) {
                    issueReportedAt = issueReportedAt.withHourOfDay(tempDailySchedule1.getOpeningTime().getHourOfDay());
                    issueReportedAt = issueReportedAt
                            .withMinuteOfHour(tempDailySchedule1.getOpeningTime().getMinuteOfHour());
                    issueReportedAt = issueReportedAt.withSecondOfMinute(tempDailySchedule1.getOpeningTime()
                            .getSecondOfMinute());
                }
                continue;
            }
        }
        schedularClass.setScheduleList(startStopList);
        return schedularClass;

    }

    private long getNonWorkingTime(BranchCalendar branchCalendar, DateTime issueReportedAt, DateTime expectedDate,
            long nonWorkingTimeInMillis) {
        int fromHour = 0;
        int fromMinute = 0;
        int toHour = 0;
        int toMinute = 0;
        while (issueReportedAt.compareTo(expectedDate) <= 0) {
            DateTime tempCalendar = new DateTime(issueReportedAt.toDate());

            int hourOfDay = issueReportedAt.getHourOfDay();
            DailySchedule dailySchedule = null;
            if (branchCalendar != null) {
                dailySchedule = getDailySchedule(branchCalendar, issueReportedAt);
            }

            if (dailySchedule != null) {
                fromHour = dailySchedule.getOpeningTime().getHourOfDay();
                fromMinute = dailySchedule.getOpeningTime().getMinuteOfHour();
                toHour = dailySchedule.getClosingTime().getHourOfDay();
                toMinute = dailySchedule.getClosingTime().getMinuteOfHour();
            }
            // Handles the scenarios where issue reported time is before the opening time of the branch.
            if (hourOfDay < fromHour) {
                tempCalendar = tempCalendar.withHourOfDay(fromHour);
                tempCalendar = tempCalendar.withMinuteOfHour(fromMinute);
                nonWorkingTimeInMillis += tempCalendar.getMillis() - issueReportedAt.getMillis();
                issueReportedAt = tempCalendar;
                continue;
            }

            // Handles the scenarios where issue reported time is after the closing time of the branch.
            if (hourOfDay >= toHour) {
                tempCalendar = tempCalendar.plusDays(1);
                DailySchedule tempDailySchedule = null;
                if (branchCalendar != null) {
                    tempDailySchedule = getDailySchedule(branchCalendar, tempCalendar);
                }
                if (tempDailySchedule != null) {
                    tempCalendar = tempCalendar.withHourOfDay(tempDailySchedule.getOpeningTime().getHourOfDay());
                    tempCalendar = tempCalendar.withMinuteOfHour(tempDailySchedule.getOpeningTime().getMinuteOfHour());
                    nonWorkingTimeInMillis += tempCalendar.getMillis() - issueReportedAt.getMillis();
                } else {
                    nonWorkingTimeInMillis += tempCalendar.getMillis() - issueReportedAt.getMillis();
                }
                issueReportedAt = tempCalendar;
                continue;
            }

            // Handles the scenarios where issue reported date is holiday.
            if (dailySchedule == null || isHoliday(branchCalendar, issueReportedAt)) {
                tempCalendar = tempCalendar.plusDays(1);
                DailySchedule tempDailySchedule = null;
                if (branchCalendar != null) {
                    tempDailySchedule = getDailySchedule(branchCalendar, tempCalendar);
                }
                if (tempDailySchedule != null) {
                    tempCalendar = tempCalendar.withHourOfDay(tempDailySchedule.getOpeningTime().getHourOfDay());
                    tempCalendar = tempCalendar.withMinuteOfHour(tempDailySchedule.getOpeningTime().getMinuteOfHour());
                    nonWorkingTimeInMillis += tempCalendar.getMillis() - issueReportedAt.getMillis();
                } else {
                    nonWorkingTimeInMillis += tempCalendar.getMillis() - issueReportedAt.getMillis();
                }
                issueReportedAt = tempCalendar;
                continue;
            } else {
                tempCalendar = tempCalendar.withHourOfDay(toHour);
                tempCalendar = tempCalendar.withMinuteOfHour(toMinute);

                DateTime tempCalendar1 = new DateTime(issueReportedAt.toDate());
                tempCalendar1 = tempCalendar1.plusDays(1);
                DailySchedule tempDailySchedule1 = null;
                if (branchCalendar != null) {
                    tempDailySchedule1 = getDailySchedule(branchCalendar, tempCalendar1);
                }
                if (tempCalendar.compareTo(expectedDate) < 0) {
                    if (tempDailySchedule1 != null) {
                        tempCalendar1 = tempCalendar1.withHourOfDay(tempDailySchedule1.getOpeningTime().getHourOfDay());
                        tempCalendar1 = tempCalendar1
                                .withMinuteOfHour(tempDailySchedule1.getOpeningTime().getMinuteOfHour());
                        nonWorkingTimeInMillis += tempCalendar1.getMillis() - tempCalendar.getMillis();
                    } else {
                        nonWorkingTimeInMillis += tempCalendar1.getMillis() - tempCalendar.getMillis();
                    }
                }

                issueReportedAt = tempCalendar1;
                continue;
            }
        }

        return nonWorkingTimeInMillis;
    }

    private boolean isHoliday(BranchCalendar branchCalendar, DateTime responseTime) {
        List<Date> HOLIDAYS = new ArrayList<Date>();
        List<Holiday> holidayList = new ArrayList<Holiday>();
        if (branchCalendar != null) {
            holidayList = branchCalendar.getHolidayList();
        }
        if (holidayList == null || holidayList.size() == 0) {
            return false;
        }
        for (Holiday holiday : holidayList) {
            HOLIDAYS.add(holiday.getHolidayDate());
        }
        for (Date holiday : HOLIDAYS) {
        	Calendar holidayCal = Calendar.getInstance();
        	holidayCal.setTime(holiday);
            if (DateUtils.isSameDay(responseTime.toCalendar(getUserLocale()), holidayCal)) {
                return true;
            }
        }
        return false;
    }

    private DailySchedule getDailySchedule(BranchCalendar branchCalendar, DateTime issueReportedAt) {
        int dayOfWeek = issueReportedAt.getDayOfWeek();
        DailySchedule dailySchedule = null;
        switch (dayOfWeek) {
            case DateTimeConstants.SUNDAY:
                dailySchedule = branchCalendar.getSundaySchedule();
                break;

            case DateTimeConstants.MONDAY:
                dailySchedule = branchCalendar.getMondaySchedule();
                break;

            case DateTimeConstants.TUESDAY:
                dailySchedule = branchCalendar.getTuesdaySchedule();
                break;

            case DateTimeConstants.WEDNESDAY:
                dailySchedule = branchCalendar.getWednesdaySchedule();
                break;

            case DateTimeConstants.THURSDAY:
                dailySchedule = branchCalendar.getThursdaySchedule();
                break;

            case DateTimeConstants.FRIDAY:
                dailySchedule = branchCalendar.getFridaySchedule();
                break;

            case DateTimeConstants.SATURDAY:
                // Chk Here
                if ((issueReportedAt.toCalendar(getUserLocale()).get(Calendar.WEEK_OF_MONTH)) % 2 == 0) {
                    dailySchedule = branchCalendar.getEvenSaturdaySchedule();
                } else {
                    dailySchedule = branchCalendar.getSaturdaySchedule();
                }
                break;
        }
        if (dailySchedule != null && !dailySchedule.isWorkingDay()) {
            dailySchedule = null;
        }
        return dailySchedule;
    }

    @Override
    public void createOrganizationBranch(OrganizationBranch branch) {
        if (branch == null) {
            throw new InvalidDataException("Organization Branch cannot be null");
        }
        if (branch.getId() == null) {
            entityDao.persist(branch);
        } else {
            entityDao.update(branch);
        }

    }

    @Override
    public void createOrganizationType(OrganizationType orgType) {
        if (orgType == null) {
            throw new InvalidDataException("Organization Type cannot be null");
        }
        entityDao.persist(orgType);
    }

    public static class SchedularClass {
        private boolean    isRunning;

        private List<Long> scheduleList;

        public boolean getIsRunning() {
            return isRunning;
        }

        public void setIsRunning(boolean isRunning) {
            this.isRunning = isRunning;
        }

        public List<Long> getScheduleList() {
            return scheduleList;
        }

        public void setScheduleList(List<Long> scheduleList) {
            this.scheduleList = scheduleList;
        }
    }

    @Override
    public DateTime getNextWorkingDayforReassign(BranchCalendar branchCalendar, DateTime workingDay, LocalTime reassignmentTime) {
        NeutrinoValidator.notNull(branchCalendar, "Branch Calendar for which due date is to be calculated can not be null");
        NeutrinoValidator.notNull(workingDay, "Working Day can not be null");
        DateTime workingDayClone = new DateTime(workingDay.toDate());
        DateTime nextWorkingDay = null;

        while(true) {
            while(nextWorkingDay == null) {
                DailySchedule dailySchedule = this.getDailySchedule(branchCalendar, workingDayClone);
                if(dailySchedule != null && !this.isHoliday(branchCalendar, workingDayClone)) {
                    if(reassignmentTime!=null) {
                        workingDayClone = workingDayClone.withHourOfDay(reassignmentTime.getHourOfDay());
                        workingDayClone = workingDayClone.withMinuteOfHour(reassignmentTime.getMinuteOfHour());
                        workingDayClone = workingDayClone.withSecondOfMinute(reassignmentTime.getSecondOfMinute());
                    }
                    else{
                        workingDayClone = workingDayClone.withHourOfDay(dailySchedule.getOpeningTime().getHourOfDay());
                        workingDayClone = workingDayClone.withMinuteOfHour(dailySchedule.getOpeningTime().getMinuteOfHour());
                        workingDayClone = workingDayClone.withSecondOfMinute(dailySchedule.getOpeningTime().getSecondOfMinute());
                    }
                    nextWorkingDay = new DateTime(workingDayClone.toDate());
                } else {
                    workingDayClone = workingDayClone.plusDays(1);
                }
            }

            return nextWorkingDay;
        }
    }

}
