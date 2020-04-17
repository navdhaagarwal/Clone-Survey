/**
 * This file and a proportion of its content is copyright of Nucleus Software Exports Limited - � 2012. All rights reserved.
 */
package com.nucleus.core.messageSource;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.support.AbstractMessageSource;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;

import com.nucleus.logging.BaseLoggers;

/**
 * 
 * @author Nucleus Software Exports Limited
 * This class is responsible for loading all the resource value while server start up into a cached map.
 */
public class DatabaseDrivenMessageSource extends AbstractMessageSource implements ResourceLoaderAware {

    private ResourceLoader                                resourceLoader;

    /**
     * value will be saved in this format <locale,<label key, label value>>
     */
    private static final Map<String, Map<String, String>> properties         = new HashMap<String, Map<String, String>>();

    public static final String                            english_locale     = "en";
    public static final String                            american_locale    = "en_US";
    public static final String                            japanes_locale     = "ja";
    public static final String                            japan_locale       = "ja_JP";
    public static final String                            pacificTime_locale = "pt";
    public static final String                            default_locale     = "default_message";

    private MessageResourceService                        messageResourceService;

    /**
     * constructor
     */
    public DatabaseDrivenMessageSource() {
    }

    /**
     * setter injection for triggering resource loading
     * @param messageResourceService
     */
    public DatabaseDrivenMessageSource(MessageResourceService messageResourceService) {
        this.messageResourceService = messageResourceService;
        /*   reload();*/
    }

    @Override
    protected MessageFormat resolveCode(String code, Locale locale) {
        String msg = getText(code, locale);
        MessageFormat result = createMessageFormat(msg, locale);
        return result;
    }

    public MessageResourceService getMessageResourceService() {
        return messageResourceService;
    }

    @Required
    public void setMessageResourceService(MessageResourceService messageResourceService) {
        this.messageResourceService = messageResourceService;
        /*    reload();*/
    }

    @Override
    protected String resolveCodeWithoutArguments(String code, Locale locale) {
        return getText(code, locale);
    }

    // first we will search in locale specific map if not found then will go for default-message map and even if not found
    // there then we will return label key as it is.
	private String getText(String code, Locale locale) {
		if(StringUtils.isBlank(code)) {
			return code;
		}
		String textForCurrentLanguage = null;
		Map<String, String> localeSpecificlabels = properties.get(locale.toString());

		if (localeSpecificlabels != null) {
			if (null != code) {
				code = code.trim();
				textForCurrentLanguage = localeSpecificlabels.get(code);
			}

		} else {
			updateNewLocaleIntoCache(locale);
			getText(code, locale);
		}

		if (textForCurrentLanguage == null || textForCurrentLanguage.isEmpty()) {
			Map<String, String> defaultLocalelabels = properties.get(default_locale);
			textForCurrentLanguage = defaultLocalelabels.get(code);
		}

		if (textForCurrentLanguage == null) {
			// fall back to property file
			try {
				BaseLoggers.webLogger.warn(
						"Label not present in both locale map and default locale map :: So searching for Label in properties files :: FallBack Mechanism");
				textForCurrentLanguage = getParentMessageSource().getMessage(code, null, locale);
			} catch (Exception e) {
				BaseLoggers.webLogger.warn(
						"Label not present in locale map,default locale map and properties files :: So searching for Label in DB:: FallBack Mechanism");
				MessageResource messageResource = messageResourceService.getMessageResourceByCode(code);
				
				//Setting messageResource default value if not found in DB.
				if(messageResource == null) {
					messageResource = getDefaultMessageResource(code);
				}
				
					messageResourceService.updateMessageResourceIntoCache(messageResource);
					for (MessageResourceValue messageResourceValue : messageResource.getMessageResourceValues()) {
						if (locale.toString().equals(messageResourceValue.getLocaleKey())) {
							textForCurrentLanguage = messageResourceValue.getLocaleValue();
						}
					}
					if (textForCurrentLanguage == null) {
						textForCurrentLanguage = messageResource.getDefaultValue();
					}
					if (textForCurrentLanguage == null) {
						BaseLoggers.webLogger.warn("Label not present in DB");
						BaseLoggers.webLogger
								.warn("Cannot find message for code: " + code + " :: for Locale: " + locale);
					}
				
			}
		}

		return textForCurrentLanguage != null ? StringEscapeUtils.unescapeJava(textForCurrentLanguage)
				: code;
	}

	private MessageResource getDefaultMessageResource(String code) {
		MessageResource messageResource;
		messageResource = new MessageResource();
		messageResource.setMessageKey(code);
		List<MessageResourceValue> messageResourceValues = new ArrayList<>();
		MessageResourceValue messageResourceValue = new MessageResourceValue();
		messageResourceValue.setLocaleKey(default_locale);
		messageResourceValue.setLocaleValue(code);
		messageResourceValues.add(messageResourceValue);
		messageResource.setMessageResourceValues(messageResourceValues);
		return messageResource;
	}

    public void reload() {
        if (properties.size() == 0 || properties.get(default_locale)==null) {
            properties.putAll(loadTexts(default_locale));
        }
    }

    // loading default_message intially, load other locale on call
    protected Map<String, Map<String, String>> loadTexts(String localekey) {
        Map<String, Map<String, String>> m = new HashMap<String, Map<String, String>>();
        Map<String, String> localeSpecificlabels = new HashMap<String, String>();
        List<Map<String, String>> result = messageResourceService.loadAllMessageResourceByLocale(localekey);
        if (result != null) {
            for (Map<String, String> map : result) {
                localeSpecificlabels.put(map.get("MESSAGEKEY"), map.get("MESSAGEVALUE"));
            }
        }
        m.put(localekey, localeSpecificlabels);
        return m;
    }

    public void updateMap(MessageResource messageResource) {
        if (messageResource != null) {
            for (MessageResourceValue singleResourceValue : messageResource.getMessageResourceValues()) {
                Map<String, String> localSpecific = properties.get(singleResourceValue.getLocaleKey());
                if (localSpecific != null) {
                    localSpecific.put(messageResource.getMessageKey(), singleResourceValue.getLocaleValue());
                }
            }
        }
    }

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = (resourceLoader != null ? resourceLoader : new DefaultResourceLoader());
    }

    public void updateNewLocaleIntoCache(Locale locale) {
        properties.putAll(loadTexts(locale.toString()));
    }
}
