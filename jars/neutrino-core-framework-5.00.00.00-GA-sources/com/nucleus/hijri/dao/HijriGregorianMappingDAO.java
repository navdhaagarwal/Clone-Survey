package com.nucleus.hijri.dao;

import java.util.Date;

import javax.inject.Named;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import com.nucleus.entity.BaseEntity;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.persistence.BaseDaoImpl;


@Named("hijriGregorianMappingDAO")
public class HijriGregorianMappingDAO extends BaseDaoImpl<BaseEntity>  implements IHijriGregorianMappingDAO{

	@Override
	public String getHijriDateFromGregorian(Date gregorianDate) {
		String hijriDate=null;
		try{
		Query query= getEntityManager().createNamedQuery("getHijriDateFromGregorian");
		query.setHint("org.hibernate.fetchSize", 1);		
		query.setParameter("gregorianDate", gregorianDate);
		hijriDate= (String) query.getSingleResult();
		
		}catch(NoResultException ex){
			BaseLoggers.flowLogger.debug(ex.getMessage());
		}
		
		return hijriDate;
		
	}

	@Override
	public Date getGregorianDateFromHijri(String hijriDate) {
		Date gregorianDate=null;

		try{
		Query query= getEntityManager().createNamedQuery("getGregorianDateFromHijri");
		
		query.setHint("org.hibernate.fetchSize", 1);		
		query.setParameter("hijriDate", hijriDate);
		gregorianDate= (Date) query.getSingleResult();
		}catch(NoResultException ex){
			BaseLoggers.flowLogger.debug(ex.getMessage());
		}
		return gregorianDate;
	}

}
