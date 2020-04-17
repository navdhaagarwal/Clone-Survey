package com.nucleus.license.event;

import com.nucleus.core.event.NeutrinoEvent;

public class LicenseNamedUserEvent extends NeutrinoEvent{

	public LicenseNamedUserEvent(Object source, String name,
			LicenseNamedUserEventWorker neutrinoEventWorker) {
		
		super(source, name, neutrinoEventWorker);
	}

}
