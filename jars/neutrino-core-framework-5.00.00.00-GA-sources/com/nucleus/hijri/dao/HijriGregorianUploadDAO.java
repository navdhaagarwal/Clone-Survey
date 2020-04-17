package com.nucleus.hijri.dao;

import javax.inject.Named;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import com.nucleus.hijri.domainobject.GregorianHijriCalendarMapping;
import com.nucleus.persistence.BaseDaoImpl;

@Named("hijriGregorianUploadDAO")
public class HijriGregorianUploadDAO extends BaseDaoImpl<GregorianHijriCalendarMapping>  implements IHijriGregorianUploadDAO{
	
	@Override
	public void updateHijriDateInCalendar(GregorianHijriCalendarMapping data){
		Query namedQuery=getEntityManager().createNamedQuery("updateHijriDate");
		namedQuery.setParameter("hijriDate", data.getHijriDate());
		namedQuery.setParameter("gregorianDate",data.getGregorianDate());
		int updated =namedQuery.executeUpdate();
	}
	
	
	@Override
	public GregorianHijriCalendarMapping getCalendarBasedOnGregDate(GregorianHijriCalendarMapping data){
		
		Query query= getEntityManager().createNamedQuery("selectWholeHijriCalendar");
		query.setParameter("gregorianDate", data.getGregorianDate());
		GregorianHijriCalendarMapping hijriCalendarUploadResult =null;
		try{
			hijriCalendarUploadResult = (GregorianHijriCalendarMapping) query.getSingleResult();
		}catch(NoResultException nex){
			return null;
		}
		return hijriCalendarUploadResult;
	}


}
