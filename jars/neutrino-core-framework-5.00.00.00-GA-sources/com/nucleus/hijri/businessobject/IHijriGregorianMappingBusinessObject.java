package com.nucleus.hijri.businessobject;

import java.util.Date;



public interface IHijriGregorianMappingBusinessObject {
	
	String getHijriDateFromGregorian(Date gregorianDate);
	String getGregorianDateFromHijri(String hijriDate);
}
