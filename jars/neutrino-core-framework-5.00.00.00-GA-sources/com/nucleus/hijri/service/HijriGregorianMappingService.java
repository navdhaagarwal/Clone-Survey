package com.nucleus.hijri.service;

import java.util.Date;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.stereotype.Service;

import com.nucleus.hijri.businessobject.IHijriGregorianMappingBusinessObject;


@Service("hijriGregorianMappingService")
public class HijriGregorianMappingService implements IHijriGregorianMappingService {
	
	@Inject
	@Named("hijriGregorianMappingBusinessObject")
	private IHijriGregorianMappingBusinessObject hijriGregorianMappingBusinessObject;
	
	@Override
	public String getHijriDateFromGregorian(Date gregorianDate) {
		return hijriGregorianMappingBusinessObject.getHijriDateFromGregorian(gregorianDate);
	}

	@Override
	public String getGregorianDateFromHijri(String hijriDate) {
		return hijriGregorianMappingBusinessObject.getGregorianDateFromHijri(hijriDate);
	}

}
