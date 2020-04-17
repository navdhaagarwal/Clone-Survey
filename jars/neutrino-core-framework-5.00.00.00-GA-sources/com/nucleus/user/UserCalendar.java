package com.nucleus.user;

import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToOne;

import com.nucleus.master.audit.annotation.EmbedInAuditAsValueObject;
import org.apache.commons.lang3.ObjectUtils;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;

import com.nucleus.core.annotations.Synonym;
import com.nucleus.core.misc.util.DateUtils;
import com.nucleus.core.organization.calendar.DailySchedule;
import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.CloneOptions;
import com.nucleus.finnone.pro.base.exception.SystemException;

/**
 * @author Nucleus Software Exports Limited UserCalendar holds holiday list and
 *         other configuration data for a branch which is used during due date
 *         calculation of tasks.
 */
@Entity
@DynamicUpdate
@DynamicInsert
@Cacheable		
@Synonym(grant = "ALL")
public class UserCalendar extends BaseEntity {

	private static final long serialVersionUID = -489622386566545795L;

	@OneToOne(cascade = CascadeType.ALL)
	@EmbedInAuditAsValueObject
	private DailySchedule sundaySchedule;

	@OneToOne(cascade = CascadeType.ALL)
	@EmbedInAuditAsValueObject
	private DailySchedule mondaySchedule;

	@OneToOne(cascade = CascadeType.ALL)
	@EmbedInAuditAsValueObject
	private DailySchedule tuesdaySchedule;

	@OneToOne(cascade = CascadeType.ALL)
	@EmbedInAuditAsValueObject
	private DailySchedule wednesdaySchedule;

	@OneToOne(cascade = CascadeType.ALL)
	@EmbedInAuditAsValueObject
	private DailySchedule thursdaySchedule;

	@OneToOne(cascade = CascadeType.ALL)
	@EmbedInAuditAsValueObject
	private DailySchedule fridaySchedule;

	@OneToOne(cascade = CascadeType.ALL)
	@EmbedInAuditAsValueObject
	private DailySchedule saturdaySchedule;

	@OneToOne(cascade = CascadeType.ALL)
	@EmbedInAuditAsValueObject
	private DailySchedule evenSaturdaySchedule;

	public UserCalendar(UserCalendar userCalendar) {
		if (userCalendar != null) {
			if (userCalendar.getSundaySchedule() != null) {
				this.setSundaySchedule(new DailySchedule(userCalendar.getSundaySchedule()));
			}
			if (userCalendar.getMondaySchedule() != null) {
				this.setMondaySchedule(new DailySchedule(userCalendar.getMondaySchedule()));
			}
			if (userCalendar.getTuesdaySchedule() != null) {
				this.setTuesdaySchedule(new DailySchedule(userCalendar.getTuesdaySchedule()));
			}
			if (userCalendar.getWednesdaySchedule() != null) {
				this.setWednesdaySchedule(new DailySchedule(userCalendar.getWednesdaySchedule()));
			}
			if (userCalendar.getThursdaySchedule() != null) {
				this.setThursdaySchedule(new DailySchedule(userCalendar.getThursdaySchedule()));
			}
			if (userCalendar.getFridaySchedule() != null) {
				this.setFridaySchedule(new DailySchedule(userCalendar.getFridaySchedule()));
			}
			if (userCalendar.getSaturdaySchedule() != null) {
				this.setSaturdaySchedule(new DailySchedule(userCalendar.getSaturdaySchedule()));
			}
			if (userCalendar.getEvenSaturdaySchedule() != null) {
				this.setEvenSaturdaySchedule(new DailySchedule(userCalendar.getEvenSaturdaySchedule()));
			}
		}

	}

	public UserCalendar() {
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
	 * @param sundaySchedule
	 *            the sundaySchedule to set
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
	 * @param mondaySchedule
	 *            the mondaySchedule to set
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
	 * @param tuesdaySchedule
	 *            the tuesdaySchedule to set
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
	 * @param wednesdaySchedule
	 *            the wednesdaySchedule to set
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
	 * @param thursdaySchedule
	 *            the thursdaySchedule to set
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
	 * @param fridaySchedule
	 *            the fridaySchedule to set
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
	 * @param saturdaySchedule
	 *            the saturdaySchedule to set
	 */
	public void setSaturdaySchedule(DailySchedule saturdaySchedule) {
		this.saturdaySchedule = saturdaySchedule;
	}

	/**
	 * @return the evenSaturdaySchedule
	 */
	public DailySchedule getEvenSaturdaySchedule() {
		isValidSchedule(evenSaturdaySchedule);
		return evenSaturdaySchedule;
	}

	/**
	 * @param evenSaturdaySchedule
	 *            the evenSaturdaySchedule to set
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
		result = prime * result + ((mondaySchedule == null) ? 0 : mondaySchedule.hashCode());
		result = prime * result + ((saturdaySchedule == null) ? 0 : saturdaySchedule.hashCode());
		result = prime * result + ((sundaySchedule == null) ? 0 : sundaySchedule.hashCode());
		result = prime * result + ((thursdaySchedule == null) ? 0 : thursdaySchedule.hashCode());
		result = prime * result + ((tuesdaySchedule == null) ? 0 : tuesdaySchedule.hashCode());
		result = prime * result + ((wednesdaySchedule == null) ? 0 : wednesdaySchedule.hashCode());
		return result;
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean equals(Object obj) {

		if (this == obj)
			return true;
		UserCalendar other = (UserCalendar) obj;
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
		return true;
	}

	public static UserCalendar getDefaultUserCalendar() {
		UserCalendar calendar = new UserCalendar();
		calendar.setSundaySchedule(null);
		calendar.setMondaySchedule(createDefaultDailySchedule());
		calendar.setTuesdaySchedule(createDefaultDailySchedule());
		calendar.setWednesdaySchedule(createDefaultDailySchedule());
		calendar.setThursdaySchedule(createDefaultDailySchedule());
		calendar.setFridaySchedule(createDefaultDailySchedule());
		calendar.setSaturdaySchedule(createDefaultDailySchedule());
		calendar.setEvenSaturdaySchedule(createDefaultDailySchedule());
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
		UserCalendar userCalendar = (UserCalendar) baseEntity;
		super.populate(userCalendar, cloneOptions);
		if (sundaySchedule != null) {
			userCalendar.setSundaySchedule((DailySchedule) sundaySchedule.cloneYourself(cloneOptions));
		}
		if (mondaySchedule != null) {
			userCalendar.setMondaySchedule((DailySchedule) mondaySchedule.cloneYourself(cloneOptions));
		}
		if (tuesdaySchedule != null) {
			userCalendar.setTuesdaySchedule((DailySchedule) tuesdaySchedule.cloneYourself(cloneOptions));
		}
		if (wednesdaySchedule != null) {
			userCalendar.setWednesdaySchedule((DailySchedule) wednesdaySchedule.cloneYourself(cloneOptions));
		}
		if (thursdaySchedule != null) {
			userCalendar.setThursdaySchedule((DailySchedule) thursdaySchedule.cloneYourself(cloneOptions));
		}
		if (fridaySchedule != null) {
			userCalendar.setFridaySchedule((DailySchedule) fridaySchedule.cloneYourself(cloneOptions));
		}
		if (saturdaySchedule != null) {
			userCalendar.setSaturdaySchedule((DailySchedule) saturdaySchedule.cloneYourself(cloneOptions));
		}
		if (evenSaturdaySchedule != null) {
			userCalendar.setEvenSaturdaySchedule((DailySchedule) evenSaturdaySchedule.cloneYourself(cloneOptions));
		}

	}

	@Override
	protected void populateFrom(BaseEntity copyEntity, CloneOptions cloneOptions) {
		UserCalendar userCalendar = (UserCalendar) copyEntity;
		super.populateFrom(userCalendar, cloneOptions);
		if (userCalendar.getSundaySchedule() != null) {
			this.setSundaySchedule((DailySchedule) userCalendar.getSundaySchedule().cloneYourself(cloneOptions));
		}
		if (userCalendar.getMondaySchedule() != null) {
			this.setMondaySchedule((DailySchedule) userCalendar.getMondaySchedule().cloneYourself(cloneOptions));
		}
		if (userCalendar.getTuesdaySchedule() != null) {
			this.setTuesdaySchedule((DailySchedule) userCalendar.getTuesdaySchedule().cloneYourself(cloneOptions));
		}
		if (userCalendar.getWednesdaySchedule() != null) {
			this.setWednesdaySchedule((DailySchedule) userCalendar.getWednesdaySchedule().cloneYourself(cloneOptions));
		}
		if (userCalendar.getThursdaySchedule() != null) {
			this.setThursdaySchedule((DailySchedule) userCalendar.getThursdaySchedule().cloneYourself(cloneOptions));
		}
		if (userCalendar.getFridaySchedule() != null) {
			this.setFridaySchedule((DailySchedule) userCalendar.getFridaySchedule().cloneYourself(cloneOptions));
		}
		if (userCalendar.getSaturdaySchedule() != null) {
			this.setSaturdaySchedule((DailySchedule) userCalendar.getSaturdaySchedule().cloneYourself(cloneOptions));
		}
		if (userCalendar.getEvenSaturdaySchedule() != null) {
			this.setEvenSaturdaySchedule((DailySchedule) userCalendar.getEvenSaturdaySchedule().cloneYourself(cloneOptions));
		}
	}
	
	
	private void isValidSchedule(DailySchedule dailySchedule) {

		if (dailySchedule != null && dailySchedule.getOpeningTime() != null && dailySchedule.getClosingTime() != null
				&& dailySchedule.getOpeningTime().getDayOfWeek() != dailySchedule.getClosingTime().getDayOfWeek()) {
			throw new SystemException("Opening and Closing Time should fall in the same day");
		}

	}

}
