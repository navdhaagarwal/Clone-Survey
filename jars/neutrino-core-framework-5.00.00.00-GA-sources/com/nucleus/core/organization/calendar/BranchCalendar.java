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

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;

import com.nucleus.core.annotations.Synonym;
import com.nucleus.core.misc.util.DateUtils;
import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.CloneOptions;
import com.nucleus.finnone.pro.base.exception.SystemException;
import com.nucleus.master.audit.annotation.EmbedInAuditAsValue;
import com.nucleus.master.audit.annotation.EmbedInAuditAsValueObject;

/**
 * @author Nucleus Software Exports Limited
 * BranchCalendar holds holiday list and other configuration data for a branch which is used during
 * due date calculation of tasks.
 */
@Entity
@DynamicUpdate
@DynamicInsert
@Cacheable
@Synonym(grant="ALL")
public class BranchCalendar extends BaseEntity {

    private static final long serialVersionUID = -489622386566545795L;

    @OneToMany(cascade = CascadeType.ALL)
    @Fetch(FetchMode.SUBSELECT)
    @JoinColumn(name = "branch_calendar_fk")
    @EmbedInAuditAsValueObject(identifierColumn="holidayDate",displayKey="label.branchCalendar.holidays")
    private List<Holiday>     holidayList;

    @OneToOne(cascade = CascadeType.ALL)
    @EmbedInAuditAsValueObject(displayKey="label.branchCalendar.sunday")
    private DailySchedule     sundaySchedule;

    @OneToOne(cascade = CascadeType.ALL)
    @EmbedInAuditAsValueObject(displayKey="label.user.management.monday")
    private DailySchedule     mondaySchedule;

    @OneToOne(cascade = CascadeType.ALL)
    @EmbedInAuditAsValueObject(displayKey="label.user.management.tuesday")
    private DailySchedule     tuesdaySchedule;

    @OneToOne(cascade = CascadeType.ALL)
    @EmbedInAuditAsValueObject(displayKey="label.user.management.wednesday")
    private DailySchedule     wednesdaySchedule;

    @OneToOne(cascade = CascadeType.ALL)
    @EmbedInAuditAsValueObject(displayKey="label.user.management.thursday")
    private DailySchedule     thursdaySchedule;

    @OneToOne(cascade = CascadeType.ALL)
    @EmbedInAuditAsValueObject(displayKey="label.user.management.friday")
    private DailySchedule     fridaySchedule;

    @OneToOne(cascade = CascadeType.ALL)
    @EmbedInAuditAsValueObject(displayKey="label.user.management.saturday")
    private DailySchedule     saturdaySchedule;

    @OneToOne(cascade = CascadeType.ALL)
    @EmbedInAuditAsValueObject(displayKey="label.branchCalendar.evenSaturday")
    private DailySchedule     evenSaturdaySchedule;

    @EmbedInAuditAsValue(displayKey="label.branchCalendar.moveAheadOnMonthEnd")
    private boolean           moveAheadOnMonthEnd;

    public BranchCalendar(BranchCalendar branchCalendar) {
        if (branchCalendar != null) {
            if (branchCalendar.getSundaySchedule() != null) {
                this.setSundaySchedule(new DailySchedule(branchCalendar.getSundaySchedule()));
            }
            if (branchCalendar.getMondaySchedule() != null) {
                this.setMondaySchedule(new DailySchedule(branchCalendar.getMondaySchedule()));
            }
            if (branchCalendar.getTuesdaySchedule() != null) {
                this.setTuesdaySchedule(new DailySchedule(branchCalendar.getTuesdaySchedule()));
            }
            if (branchCalendar.getWednesdaySchedule() != null) {
                this.setWednesdaySchedule(new DailySchedule(branchCalendar.getWednesdaySchedule()));
            }
            if (branchCalendar.getThursdaySchedule() != null) {
                this.setThursdaySchedule(new DailySchedule(branchCalendar.getThursdaySchedule()));
            }
            if (branchCalendar.getFridaySchedule() != null) {
                this.setFridaySchedule(new DailySchedule(branchCalendar.getFridaySchedule()));
            }
            if (branchCalendar.getSaturdaySchedule() != null) {
                this.setSaturdaySchedule(new DailySchedule(branchCalendar.getSaturdaySchedule()));
            }
            if (branchCalendar.getEvenSaturdaySchedule() != null) {
                this.setEvenSaturdaySchedule(new DailySchedule(branchCalendar.getEvenSaturdaySchedule()));
            }
            if (branchCalendar.getHolidayList() != null) {
                List<Holiday> holidayListWithoutId = new ArrayList<Holiday>();
                for (Holiday holiday : branchCalendar.getHolidayList()) {
                    holidayListWithoutId.add(new Holiday(holiday));
                }
                this.setHolidayList(holidayListWithoutId);
            }
            this.setMoveAheadOnMonthEnd(branchCalendar.getMoveAheadOnMonthEnd());
        }

    }

    public BranchCalendar() {
    }

    /**
     * @return the holidayList
     */
    public List<Holiday> getHolidayList() {
        return holidayList;
    }

    /**
     * @param holidayList the holidayList to set
     */
    public void setHolidayList(List<Holiday> holidayList) {
        this.holidayList = holidayList;
    }

    public DailySchedule getSchedule(int day) {
        if (day == DateTimeConstants.SUNDAY) {
            return getSundaySchedule();
        } else if (day == DateTimeConstants.MONDAY) {
            return getMondaySchedule();
        } else if (day == DateTimeConstants.TUESDAY) {
            return getTuesdaySchedule();
        } else if (day == DateTimeConstants.WEDNESDAY) {
            return getWednesdaySchedule();
        } else if (day == DateTimeConstants.THURSDAY) {
            return getThursdaySchedule();
        } else if (day == DateTimeConstants.FRIDAY) {
            return getFridaySchedule();
        } else if (day == DateTimeConstants.SATURDAY) {
            return getSaturdaySchedule();
        }

        return null;
    }

    /**
     * @return the sundaySchedule
     */
    public DailySchedule getSundaySchedule() {
    	isValidSchedule(sundaySchedule);
        return sundaySchedule;
    }

	/**
     * @param sundaySchedule the sundaySchedule to set
     */
    public void setSundaySchedule(DailySchedule sundaySchedule) {
        this.sundaySchedule = sundaySchedule;
    }

    /**
     * @return the mondaySchedule
     */
    public DailySchedule getMondaySchedule() {
    	isValidSchedule(mondaySchedule);
        return mondaySchedule;
    }

    /**
     * @param mondaySchedule the mondaySchedule to set
     */
    public void setMondaySchedule(DailySchedule mondaySchedule) {
        this.mondaySchedule = mondaySchedule;
    }

    /**
     * @return the tuesdaySchedule
     */
    public DailySchedule getTuesdaySchedule() {
    	isValidSchedule(tuesdaySchedule);
        return tuesdaySchedule;
    }

    /**
     * @param tuesdaySchedule the tuesdaySchedule to set
     */
    public void setTuesdaySchedule(DailySchedule tuesdaySchedule) {
        this.tuesdaySchedule = tuesdaySchedule;
    }

    /**
     * @return the wednesdaySchedule
     */
    public DailySchedule getWednesdaySchedule() {
    	isValidSchedule(wednesdaySchedule);
        return wednesdaySchedule;
    }

    /**
     * @param wednesdaySchedule the wednesdaySchedule to set
     */
    public void setWednesdaySchedule(DailySchedule wednesdaySchedule) {
        this.wednesdaySchedule = wednesdaySchedule;
    }

    /**
     * @return the thursdaySchedule
     */
    public DailySchedule getThursdaySchedule() {
    	isValidSchedule(thursdaySchedule);
        return thursdaySchedule;
    }

    /**
     * @param thursdaySchedule the thursdaySchedule to set
     */
    public void setThursdaySchedule(DailySchedule thursdaySchedule) {
        this.thursdaySchedule = thursdaySchedule;
    }

    /**
     * @return the fridaySchedule
     */
    public DailySchedule getFridaySchedule() {
    	isValidSchedule(fridaySchedule);
        return fridaySchedule;
    }

    /**
     * @param fridaySchedule the fridaySchedule to set
     */
    public void setFridaySchedule(DailySchedule fridaySchedule) {
        this.fridaySchedule = fridaySchedule;
    }

    /**
     * @return the saturdaySchedule
     */
    public DailySchedule getSaturdaySchedule() {
    	isValidSchedule(saturdaySchedule);
        return saturdaySchedule;
    }

    /**
     * @param saturdaySchedule the saturdaySchedule to set
     */
    public void setSaturdaySchedule(DailySchedule saturdaySchedule) {
        this.saturdaySchedule = saturdaySchedule;
    }

    /**
     * @return the moveAheadOnMonthEnd
     */
    public boolean getMoveAheadOnMonthEnd() {
        return moveAheadOnMonthEnd;
    }

    /**
     * @param moveAheadOnMonthEnd the moveAheadOnMonthEnd to set
     */
    public void setMoveAheadOnMonthEnd(boolean moveAheadOnMonthEnd) {
        this.moveAheadOnMonthEnd = moveAheadOnMonthEnd;
    }

    /**
     * @return the evenSaturdaySchedule
     */
    public DailySchedule getEvenSaturdaySchedule() {
    	isValidSchedule(evenSaturdaySchedule);
        return evenSaturdaySchedule;
    }

    /**
     * @param evenSaturdaySchedule the evenSaturdaySchedule to set
     */
    public void setEvenSaturdaySchedule(DailySchedule evenSaturdaySchedule) {
        this.evenSaturdaySchedule = evenSaturdaySchedule;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 11;
        result = prime * result + ((evenSaturdaySchedule == null) ? 0 : evenSaturdaySchedule.hashCode());
        result = prime * result + ((fridaySchedule == null) ? 0 : fridaySchedule.hashCode());
        result = prime * result + ((holidayList == null) ? 0 : holidayList.hashCode());
        result = prime * result + ((mondaySchedule == null) ? 0 : mondaySchedule.hashCode());
        result = prime * result + (moveAheadOnMonthEnd ? 1231 : 1237);
        result = prime * result + ((saturdaySchedule == null) ? 0 : saturdaySchedule.hashCode());
        result = prime * result + ((sundaySchedule == null) ? 0 : sundaySchedule.hashCode());
        result = prime * result + ((thursdaySchedule == null) ? 0 : thursdaySchedule.hashCode());
        result = prime * result + ((tuesdaySchedule == null) ? 0 : tuesdaySchedule.hashCode());
        result = prime * result + ((wednesdaySchedule == null) ? 0 : wednesdaySchedule.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj)
            return true;
        BranchCalendar other = (BranchCalendar) obj;
        if (!ObjectUtils.equals(mondaySchedule, other.mondaySchedule)) {
            return false;
        }
        if (!ObjectUtils.equals(tuesdaySchedule, other.tuesdaySchedule)) {
            return false;
        }
        if (!ObjectUtils.equals(wednesdaySchedule, other.wednesdaySchedule)) {
            return false;
        }
        if (!ObjectUtils.equals(thursdaySchedule, other.thursdaySchedule)) {
            return false;
        }
        if (!ObjectUtils.equals(fridaySchedule, other.fridaySchedule)) {
            return false;
        }
        if (!ObjectUtils.equals(saturdaySchedule, other.saturdaySchedule)) {
            return false;
        }
        if (!ObjectUtils.equals(sundaySchedule, other.sundaySchedule)) {
            return false;
        }
        if (!ObjectUtils.equals(evenSaturdaySchedule, other.evenSaturdaySchedule)) {
            return false;
        }
        if ((holidayList == null && other.holidayList != null) || (holidayList != null && other.holidayList == null)) {
            return false;
        }
        if (holidayList != null && other.holidayList != null) {
            if (!CollectionUtils.isEqualCollection(holidayList, other.holidayList)) {
                return false;
            }
        }
        if (moveAheadOnMonthEnd != other.moveAheadOnMonthEnd) {
            return false;
        }
        return true;
    }

    public static BranchCalendar getDefaultBranchCalendar() {
        BranchCalendar calendar = new BranchCalendar();
        calendar.setSundaySchedule(null);
        calendar.setMondaySchedule(createDefaultDailySchedule());
        calendar.setTuesdaySchedule(createDefaultDailySchedule());
        calendar.setWednesdaySchedule(createDefaultDailySchedule());
        calendar.setThursdaySchedule(createDefaultDailySchedule());
        calendar.setFridaySchedule(createDefaultDailySchedule());
        calendar.setSaturdaySchedule(createDefaultDailySchedule());
        calendar.setEvenSaturdaySchedule(createDefaultDailySchedule());
        calendar.setHolidayList(new ArrayList<Holiday>());
        calendar.setMoveAheadOnMonthEnd(false);
        return calendar;
    }

    private static DailySchedule createDefaultDailySchedule() {
        DailySchedule dailySchedule = new DailySchedule();
        DateTime currentUTCTime = DateUtils.getCurrentUTCTime();
        DateTime openingTime = currentUTCTime.withHourOfDay(9);
        DateTime closingTime = currentUTCTime.withHourOfDay(17);

        dailySchedule.setOpeningTime(openingTime);
        dailySchedule.setClosingTime(closingTime);
        dailySchedule.setWorkingDay(true);

        return dailySchedule;
    }

    @Override
    protected void populate(BaseEntity baseEntity, CloneOptions cloneOptions) {
        BranchCalendar branchCalendar = (BranchCalendar) baseEntity;
        super.populate(branchCalendar, cloneOptions);
        List<Holiday> cloneHolidayList = new ArrayList<Holiday>();
        if (holidayList != null && holidayList.size() > 0) {

            for (Holiday holiday : holidayList) {
                cloneHolidayList.add((Holiday) holiday.cloneYourself(cloneOptions));
            }
        }
        if (sundaySchedule != null) {
            branchCalendar.setSundaySchedule((DailySchedule) sundaySchedule.cloneYourself(cloneOptions));
        }
        if (mondaySchedule != null) {
            branchCalendar.setMondaySchedule((DailySchedule) mondaySchedule.cloneYourself(cloneOptions));
        }
        if (tuesdaySchedule != null) {
            branchCalendar.setTuesdaySchedule((DailySchedule) tuesdaySchedule.cloneYourself(cloneOptions));
        }
        if (wednesdaySchedule != null) {
            branchCalendar.setWednesdaySchedule((DailySchedule) wednesdaySchedule.cloneYourself(cloneOptions));
        }
        if (thursdaySchedule != null) {
            branchCalendar.setThursdaySchedule((DailySchedule) thursdaySchedule.cloneYourself(cloneOptions));
        }
        if (fridaySchedule != null) {
            branchCalendar.setFridaySchedule((DailySchedule) fridaySchedule.cloneYourself(cloneOptions));
        }
        if (saturdaySchedule != null) {
            branchCalendar.setSaturdaySchedule((DailySchedule) saturdaySchedule.cloneYourself(cloneOptions));
        }
        if (evenSaturdaySchedule != null) {
            branchCalendar.setEvenSaturdaySchedule((DailySchedule) evenSaturdaySchedule.cloneYourself(cloneOptions));
        }
        branchCalendar.setMoveAheadOnMonthEnd(moveAheadOnMonthEnd);
        branchCalendar.setHolidayList(cloneHolidayList);

    }
    


    private void isValidSchedule(DailySchedule dailySchedule) {
		
    	if(dailySchedule!=null && dailySchedule.getOpeningTime()!=null && dailySchedule.getClosingTime()!=null
    		&& dailySchedule.getOpeningTime().getDayOfWeek()!=dailySchedule.getClosingTime().getDayOfWeek()){
    		throw new SystemException("Opening and Closing Time should fall in the same day");
    	}
		
	}

}
