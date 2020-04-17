package com.nucleus.hijri.dao;

import java.util.Date;



public interface IHijriGregorianMappingDAO {
	
	String getHijriDateFromGregorian(Date gregorianDate);
	Date getGregorianDateFromHijri(String hijriDate);
}
