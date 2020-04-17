package com.nucleus.web.hijri.controller;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;



import com.nucleus.hijri.service.IHijriGregorianMappingService;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.web.common.controller.BaseController;
import com.nucleus.web.hijri.contstants.HijriGregorionConstants;

@Controller
@RequestMapping(value = "/hijriGregorianMapping")
public class HijriGregorianMappingController extends BaseController{
	
	@Inject
	@Named("hijriGregorianMappingService")
	private IHijriGregorianMappingService hijriGregorianMappingService;
	
	@RequestMapping(value = "/getHijriDate")
	@ResponseBody
	public  Map<String,Object> getHijriDate(Date date) {
		
		BaseLoggers.exceptionLogger.error("In receiptEntryFromOther---> " );
		Map<String,Object> map = new HashMap<String, Object>();
		String hijriDate = hijriGregorianMappingService.getHijriDateFromGregorian(date);
		map.put("hijriDate", hijriDate);
		return map;
	}
	
	
	
	@RequestMapping(value = "/getGregorianDate")
	@ResponseBody
	public  Map<String,Object> getGregorianDate(String date) {
		
		BaseLoggers.exceptionLogger.error("In receiptEntryFromOther---> " );
		Map<String,Object> map = new HashMap<String, Object>();
		String gregorianDate = hijriGregorianMappingService.getGregorianDateFromHijri(date);
		map.put("gregorianDate", gregorianDate);
		return map;
	}
	
	
}
