package com.nucleus.finnone.pro.communicationgenerator.service;

import java.util.Properties;

import javax.inject.Inject;
import javax.inject.Named;

import com.nucleus.cfi.mail.service.BounceMailboxPropertiesProvider;
import com.nucleus.mail.service.AbstractMailboxManagerService;

@Named("bounceMailProcessorService")
public class BounceMailProcessorService extends AbstractMailboxManagerService {
	
	@Inject
	@Named("bounceMailboxPropertiesProvider")
	private BounceMailboxPropertiesProvider bounceMailboxPropertiesProvider;

	@Override
	public Properties getDefaultMailBoxProperties() {
		return bounceMailboxPropertiesProvider.getDefaultProperties();
	}
	
}
