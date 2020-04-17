package com.nucleus.hijri.businessobject;

import java.util.Date;

import javax.inject.Inject;
import javax.inject.Named;

import com.nucleus.hijri.dao.IHijriGregorianMappingDAO;
import com.nucleus.hijri.util.DateUtility;

@Named("hijriGregorianMappingBusinessObject")
public class HijriGregorianMappingBusinessObject implements IHijriGregorianMappingBusinessObject{
	
	@Inject
	@Named("hijriGregorianMappingDAO")
	private IHijriGregorianMappingDAO hijriGregorianMappingDAO;
	
	@Override
	public String getHijriDateFromGregorian(Date gregorianDate) {		
	//	String hijriDateAsString = null;
		String hijriDate = hijriGregorianMappingDAO.getHijriDateFromGregorian(gregorianDate);
		
		if(hijriDate!=null&&hijriDate.contains("'"))
			{
			hijriDate = hijriDate.replace("'","");		
			}
		
		return hijriDate;
	}

	@Override
	public String getGregorianDateFromHijri(String hijriDate) {
		String gregorianDateAsString = null;
		//hijriDate=hijriDate+"'";
		Date gregorianDate = hijriGregorianMappingDAO.getGregorianDateFromHijri(hijriDate);
		if(gregorianDate !=null){
			gregorianDateAsString = DateUtility.formatDateAsString(gregorianDate, "dd/MM/yyyy");
		}
		return gregorianDateAsString;
	}

}
