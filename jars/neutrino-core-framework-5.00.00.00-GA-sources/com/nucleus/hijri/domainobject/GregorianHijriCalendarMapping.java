package com.nucleus.hijri.domainobject;


import java.util.Date;

import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.QueryHint;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.annotations.Synonym;
import com.nucleus.entity.BaseEntity;
import com.nucleus.hijri.service.IHijriCalendarUploadService;


@Entity
@DynamicUpdate
@DynamicInsert
@Table(name="GREGOR_HIJRI_CALENDAR_MAPPING")
@NamedQueries({
	
	@NamedQuery(
			name="getHijriDateFromGregorian",
			query = "select hijriDate from GregorianHijriCalendarMapping  "+					
					"where gregorianDate = :gregorianDate",
					hints={@QueryHint(name="org.hibernate.cacheable",value="true"),@QueryHint(name="org.hibernate.cacheRegion",value="getHijriDateFromGregorian") }
		),
	@NamedQuery(
			name="getGregorianDateFromHijri",
			query = "select gregorianDate from GregorianHijriCalendarMapping "+					
					"where hijriDate = :hijriDate",
					hints={@QueryHint(name="org.hibernate.cacheable",value="true"),@QueryHint(name="org.hibernate.cacheRegion",value="getHijriDateFromGregorian") }
		),
		@NamedQuery(
				name="updateHijriDate",query="update GregorianHijriCalendarMapping hijriCalendarUpload set hijriCalendarUpload.hijriDate =:hijriDate where hijriCalendarUpload.gregorianDate =:gregorianDate"
		),
		@NamedQuery(
				name="selectWholeHijriCalendar",				
				query = "select a from GregorianHijriCalendarMapping a where a.gregorianDate=:gregorianDate"
		)
	
	
})
@Synonym(grant="ALL")
public class GregorianHijriCalendarMapping  extends BaseEntity{
	
	private static final long serialVersionUID = 1L;	

	@Column(name="Hijri_Date")
	private String hijriDate;
	
	@Column(name="Gregorian_Date", columnDefinition = "Date")
	private Date gregorianDate;

	public String getHijriDate() {
		return hijriDate;
	}

	private String remarks;
	
	public String getRemarks() {
		return remarks;
	}

	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}

	public void setHijriDate(String  hijriDate) {
		this.hijriDate = hijriDate;
	}

	public Date getGregorianDate() {
		return gregorianDate;
	}

	public void setGregorianDate(Date gregorianDate) {
		this.gregorianDate = gregorianDate;
	}
	
	
}
