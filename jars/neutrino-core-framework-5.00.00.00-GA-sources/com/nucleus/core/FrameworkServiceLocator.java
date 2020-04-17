package com.nucleus.core;

import org.springframework.beans.factory.config.ServiceLocatorFactoryBean;

import com.nucleus.systemSetup.service.SystemSetupPreCompletionService;

/**
 * The Interface FrameworkServiceLocator for locating services based upon the type or name of the service.
 * @see {@link ServiceLocatorFactoryBean}
 */
public interface FrameworkServiceLocator {

    /**
     * Gets the system setup pre completion service.
     *
     * @return the system setup pre completion service
     */
    SystemSetupPreCompletionService getSystemSetupPreCompletionService();

}
