package com.nucleus.web.security;


import javax.inject.Named;

import org.springframework.beans.factory.annotation.Value;

@Named
public class NeutrinoCommonsMultipartFileConfig {

	private final String defaultRestrictedFileExtension = ".exe,.html,.htm,.mht,.dhtml,.phtml,.jhtml,.mhtml,.rhtml,.shtml,.shtm,.zhtml,.cfm,.cfml,.asp";

	@Value(value = "#{'${security.restricted.file.extensions:.php,.php3}'}")
	private String restrictedFileExtension;

	public void setRestrictedFileExtension(String restrictedFileExtension) {
		this.restrictedFileExtension = restrictedFileExtension;
	}

	public String getRestrictedFileExtension() {

		if (restrictedFileExtension == null) {
			restrictedFileExtension = ".php,.php3";
		}

		return defaultRestrictedFileExtension +","+ restrictedFileExtension;
	}

}
