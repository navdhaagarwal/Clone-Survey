package com.nucleus.web.security;

import javax.inject.Named;

import org.springframework.beans.factory.annotation.Value;

@Named
public class NeutrinoBodyManipulationFilterConfig {

	@Value(value = "#{'${security.bodyManipulationFilterEnabled:false}'}")
	private Boolean bodyManipulationFilterEnabled;

	public Boolean getBodyManipulationFilterEnabled() {
		return bodyManipulationFilterEnabled;
	}

	public void setBodyManipulationFilterEnabled(Boolean bodyManipulationFilterEnabled) {
		this.bodyManipulationFilterEnabled = bodyManipulationFilterEnabled;
	}
}
