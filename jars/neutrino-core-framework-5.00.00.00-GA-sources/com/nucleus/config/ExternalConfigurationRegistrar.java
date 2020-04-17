/**
 * This file and a proportion of its content is copyright of Nucleus Software Exports Limited - © 2012. All rights reserved.
 * Any redistribution or reproduction of part or all of the contents in any form is prohibited other than the following:
 * - you cannot print or download to a local hard disk extract contents either part or full for personal/ commercial/
 * academic or any other use
 * - you may not copy the content to individual/ third parties for any type of use, either as compiled or source format
 * without the knowledge and consent of Nucleus SOftware
 * - You may not, except with our express written permission, distribute or commercially exploit the content. Nor may you
 * transmit it or store it in any other web site or other form of electronic retrieval system.
 */
package com.nucleus.config;

import javax.annotation.PostConstruct;

import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.reloading.InvariantReloadingStrategy;
import org.springframework.core.io.Resource;

import com.nucleus.core.exceptions.SystemException;
import com.nucleus.logging.BaseLoggers;

/**
 * Registry class to build configuration resources
 */
public class ExternalConfigurationRegistrar {

    private Resource[] configurationResources;

    @PostConstruct
    public void addConfigResource() {
        if (configurationResources == null) {
            throw new SystemException("Configuration resources cannot be null");
        }
        XMLConfiguration configuration = null;
        for (Resource resource : configurationResources) {
            if (!resource.exists()) {
                throw new SystemException(String.format("Configuration resource %s doesn't exist", resource));
            }
            try {
                configuration = new XMLConfiguration(resource.getURL());
                configuration.setReloadingStrategy(new InvariantReloadingStrategy());
                ExternalConfigurationRegistry.addConfiguration(configuration);
                BaseLoggers.flowLogger
                        .info("External configuration successfully added into External Configuration Registry from location: "
                                + resource);
            } catch (Exception e) {
                throw new SystemException("Exception while loading configuration resource from path: " + resource, e);
            }
        }
    }

    public void setConfigurationResources(Resource[] configurationResources) {
        this.configurationResources = configurationResources;
    }

}
