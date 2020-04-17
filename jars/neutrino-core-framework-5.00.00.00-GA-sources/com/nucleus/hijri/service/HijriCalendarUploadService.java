package com.nucleus.hijri.service;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nucleus.hijri.businessobject.IHijriCalendarUploadBusinessObject;
import com.nucleus.hijri.domainobject.GregorianHijriCalendarMapping;

@Service
@Named("hijriCalendarUploadService")
public class HijriCalendarUploadService implements IHijriCalendarUploadService{
	
	
	@Inject
	private IHijriCalendarUploadBusinessObject hijriCalendarUploadBusinessObject;
	
/*	@Override
	public GregorianHijriCalendarMapping uploadHijriCalendar( GregorianHijriCalendarMapping hijriCalendarUpload, Date processDate) {
		GeneralUtility.setBaseEntityDefaultMakerData(hijriCalendarUpload, requestServicingContext,processDate);		
		return hijriCalendarUploadBusinessObject.uploadHijriCalendar(hijriCalendarUpload);
	}
*/	
	
	@Override
	@Transactional
	public GregorianHijriCalendarMapping uploadHijriCalendar(GregorianHijriCalendarMapping hijriCalendarUpload) {
//		return uploadHijriCalendar(hijriCalendarUpload,requestServicingContext.getBusinessDate());
		return hijriCalendarUploadBusinessObject.uploadHijriCalendar(hijriCalendarUpload);
	}

}
