package com.nucleus.core.locale;

import java.io.Serializable;

public class LanguageInfoVO implements Serializable {

    private static final long serialVersionUID = 4593151888750146372L;

    private String localeCode;

    private String localeLabel;

    private String countryFlagImageUrl;

    private String langCode;

    private String readingDirection;

    public LanguageInfoVO(String localeCode, String localeLabel,
            String countryFlagImageUrl, String langCode, String readingDirection) {
        super();
        this.localeCode = localeCode;
        this.localeLabel = localeLabel;
        this.countryFlagImageUrl = countryFlagImageUrl;
        this.langCode = langCode;
        this.readingDirection = readingDirection;
    }

    public LanguageInfoVO() {
        super();
      }

    public String getReadingDirection() {
        return readingDirection;
    }

    public void setReadingDirection(String readingDirection) {
        this.readingDirection = readingDirection;
    }

    
    public String getLocaleCode() {
        return localeCode;
    }

    public void setLocaleCode(String localeCode) {
        this.localeCode = localeCode;
    }

    public String getLocaleLabel() {
        return localeLabel;
    }

    public void setLocaleLabel(String localeLabel) {
        this.localeLabel = localeLabel;
    }

    public String getCountryFlagImageUrl() {
        return countryFlagImageUrl;
    }

    public void setCountryFlagImageUrl(String countryFlagImageUrl) {
        this.countryFlagImageUrl = countryFlagImageUrl;
    }

    public String getLangCode() {
        return langCode;
    }

    public void setLangCode(String langCode) {
        this.langCode = langCode;
    }

}
