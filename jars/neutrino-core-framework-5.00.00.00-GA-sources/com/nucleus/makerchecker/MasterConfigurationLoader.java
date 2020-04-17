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
package com.nucleus.makerchecker;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.SystemUtils;
import org.springframework.core.io.Resource;

import com.nucleus.core.exceptions.SystemException;
import com.nucleus.core.initialization.NeutrinoResourceLoader;
import com.nucleus.core.xml.util.XmlUtils;

/**
 * @author Nucleus Software Exports Limited
 */
public class MasterConfigurationLoader {

    private NeutrinoResourceLoader               resourceLoader;

    private final Map<String, GridConfiguration> cacheMap = new LinkedHashMap<String, GridConfiguration>();

    public GridConfiguration getConfiguration(String entityName) {
        if (!cacheMap.containsKey(entityName)) {
            Resource resource = resourceLoader.getResource("masters-config" + SystemUtils.FILE_SEPARATOR + entityName
                    + ".xml");
            if (!resource.exists()) {
                cacheMap.put(entityName, null);
            } else {
                try {
                    GridConfiguration config = XmlUtils.readFromXml(IOUtils.toString(resource.getInputStream()),
                            GridConfiguration.class);
                    cacheMap.put(entityName, config);
                } catch (Exception e) {
                    throw new SystemException("Application is unable to read " + entityName + ".xml", e);
                }
            }
        }
        return cacheMap.get(entityName);
    }

    public void setResourceLoader(NeutrinoResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

}
