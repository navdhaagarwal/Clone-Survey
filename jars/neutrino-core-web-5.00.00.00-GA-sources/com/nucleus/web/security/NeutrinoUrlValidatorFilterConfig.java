package com.nucleus.web.security;

import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;

/**
 *  Config for enabling/ disabling NeutrinoUrlValidatorFilter by setting security.urlValidatorFilterEnabled to true/false.
 * @author gajendra.jatav
 *
 */
@Named("neutrinoUrlValidatorFilterConfig")
public class NeutrinoUrlValidatorFilterConfig {

	@Value(value = "#{'${security.urlValidatorFilterEnabled}'}")
	private Boolean urlValidatorFilterEnabled;
	
	private Boolean paramEncryptionEnabled=false;

	public Boolean getUrlValidatorFilterEnabled() {
		return urlValidatorFilterEnabled;
	}

	public void setUrlValidatorFilterEnabled(Boolean urlValidatorFilterEnabled) {
		this.urlValidatorFilterEnabled = urlValidatorFilterEnabled;
	}

	public Boolean getParamEncryptionEnabled() {
		if(!getUrlValidatorFilterEnabled())
		{
			return false;
		}
		if(paramEncryptionEnabled==null)
		{
			return false;
		}
		return paramEncryptionEnabled;
	}

	@Value(value = "#{'${security.paramEncryptionEnabled}'}")
	public void setParamEncryptionEnabled(String paramEncryptionEnabled) {
		if (StringUtils.isEmpty(paramEncryptionEnabled)
				|| "${security.paramEncryptionEnabled}".equalsIgnoreCase(paramEncryptionEnabled)) {
			this.paramEncryptionEnabled = false;
			return;
		}
		this.paramEncryptionEnabled = Boolean.parseBoolean(paramEncryptionEnabled);
	}
	
}
