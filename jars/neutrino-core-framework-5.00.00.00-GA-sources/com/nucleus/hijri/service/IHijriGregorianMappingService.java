package com.nucleus.hijri.service;

import java.util.Date;



public interface IHijriGregorianMappingService {
	
	String getHijriDateFromGregorian(Date gregorianDate);
	String getGregorianDateFromHijri(String hijriDate);
	
}
