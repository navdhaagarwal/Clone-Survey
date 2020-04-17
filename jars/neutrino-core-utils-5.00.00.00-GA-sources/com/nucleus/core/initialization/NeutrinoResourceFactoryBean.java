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
package com.nucleus.core.initialization;

import org.springframework.beans.factory.FactoryBean;

/**
 * @author Nucleus Software Exports Limited
 */
public class NeutrinoResourceFactoryBean implements FactoryBean<String> {

    private String                 resourceName;
    private NeutrinoResourceLoader resourceLoader;

    @Override
    public String getObject() throws Exception {
        return resourceLoader.resolvePath(resourceName);
    }

    @Override
    public Class<?> getObjectType() {
        return String.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(String propertyFileName) {
        this.resourceName = propertyFileName;
    }

    public NeutrinoResourceLoader getResourceLoader() {
        return resourceLoader;
    }

    public void setResourceLoader(NeutrinoResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

}