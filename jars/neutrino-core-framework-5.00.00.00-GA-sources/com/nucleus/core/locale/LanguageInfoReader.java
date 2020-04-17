package com.nucleus.core.locale;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.inject.Named;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import com.nucleus.logging.BaseLoggers;

@Named("languageInfoReader")
public class LanguageInfoReader {

    private List<LanguageInfoVO> availableLanguagesInfo;
    private Map<String, LanguageInfoVO> localeLanguageInfoMap;

    public List<LanguageInfoVO> getAvailableLanguageInfo() {
    	return availableLanguagesInfo;
    }
    
    public Map<String, LanguageInfoVO> getAvailableLocaleLanguageInfoMap() {
        return localeLanguageInfoMap;
    }
    	
    private void initializeAvailableLanguageInfo() {
		String localesPropertyFile = "/core-web-config/supported-locales.properties";
		Resource resource = new ClassPathResource(localesPropertyFile);
		availableLanguagesInfo = new ArrayList<>();
		try {
			Properties ccProperties = PropertiesLoaderUtils.loadProperties(resource);
			Enumeration<Object> enuKeys = ccProperties.keys();
			while (enuKeys.hasMoreElements()) {
				String key = (String) enuKeys.nextElement();
				String value = ccProperties.getProperty(key);
				String[] valArray = value.split("~");
				LanguageInfoVO languageInfoVO = new LanguageInfoVO(key,
						valArray[0], valArray[1], valArray[2], valArray[3]);
				availableLanguagesInfo.add(languageInfoVO);
			}
		} catch (Exception e) {
			BaseLoggers.exceptionLogger
			.error("Exception occured while accessing file at location /core-web-config/supported-locales.properties", e);
		}
	}
	
	private void initilizeLocaleLanguageInfoMap() {
		localeLanguageInfoMap = new HashMap<>();
        List<LanguageInfoVO> availableLanguageInfoList = getAvailableLanguageInfo();
        Iterator<LanguageInfoVO> itr = availableLanguageInfoList.iterator();
        while (itr.hasNext()) {
            LanguageInfoVO livo = itr.next();
            localeLanguageInfoMap.put(livo.getLocaleCode(), livo);
        }
	}
	
    @PostConstruct
    public void initializeLanguageInfo() {
    	BaseLoggers.flowLogger.debug("Start initialization of availableLanguagesInfo.");
		initializeAvailableLanguageInfo();
		initilizeLocaleLanguageInfoMap();
		BaseLoggers.flowLogger.debug("Initialization completed for availableLanguagesInfo and localeLanguageInfoMap.");
    }

}
