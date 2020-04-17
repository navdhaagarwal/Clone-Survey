package com.nucleus.hijri.dao;

import com.nucleus.hijri.domainobject.GregorianHijriCalendarMapping;





public interface IHijriGregorianUploadDAO {
	
	public void updateHijriDateInCalendar(GregorianHijriCalendarMapping data);
	public GregorianHijriCalendarMapping getCalendarBasedOnGregDate(GregorianHijriCalendarMapping data);
}
