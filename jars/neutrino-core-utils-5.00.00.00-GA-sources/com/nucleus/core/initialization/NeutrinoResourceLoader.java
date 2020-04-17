/**
 * This file and a proportion of its content is copyright of Nucleus Software Exports Limited - � 2012. All rights reserved.
 * Any redistribution or reproduction of part or all of the contents in any form is prohibited other than the following:
 * - you cannot print or download to a local hard disk extract contents either part or full for personal/ commercial/
 * academic or any other use
 * - you may not copy the content to individual/ third parties for any type of use, either as compiled or source format
 * without the knowledge and consent of Nucleus SOftware
 * - You may not, except with our express written permission, distribute or commercially exploit the content. Nor may you
 * transmit it or store it in any other web site or other form of electronic retrieval system.
 */
package com.nucleus.core.initialization;

import org.apache.commons.lang3.SystemUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * One of the most important classes in the framework which facilitates fallback mechanism for resource loading. This allows easy customizability for clients who are using Neutrino products
 * @author Nucleus Software Exports Limited
 */
public class NeutrinoResourceLoader {

    private String                              rootConfigFolderName;
    private String                              productSuiteConfigFolderName;
    private String                              moduleName;
    private PathMatchingResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();

    public NeutrinoResourceLoader(String rootConfigFolderName, String productSuiteConfigFolderName) {
        this.rootConfigFolderName = rootConfigFolderName;
        this.productSuiteConfigFolderName = productSuiteConfigFolderName;
    }

    public String resolvePath(String resourceName) {
        Resource finalResource = null;
        if (ProductInformationLoader.productInfoExists()) {
            String productName;
            productName = ProductInformationLoader.getProductName();
            String productResourcePath = "classpath:" + rootConfigFolderName + SystemUtils.FILE_SEPARATOR
                    + productName.toLowerCase() + "-config" + SystemUtils.FILE_SEPARATOR + moduleName
                    + SystemUtils.FILE_SEPARATOR + resourceName;
            finalResource = resourcePatternResolver.getResource(productResourcePath);
            if (finalResource.exists()) {
                return productResourcePath;
            }
        }

        String rootResourcePath = "classpath:" + rootConfigFolderName + SystemUtils.FILE_SEPARATOR
                + productSuiteConfigFolderName + SystemUtils.FILE_SEPARATOR + moduleName + SystemUtils.FILE_SEPARATOR
                + resourceName;
        finalResource = resourcePatternResolver.getResource(rootResourcePath);
        if (finalResource.exists()) {
            return rootResourcePath;
        }
        return "classpath:" + moduleName + SystemUtils.FILE_SEPARATOR + resourceName;
    }

    public Resource getResource(String resourceName) {
        return resourcePatternResolver.getResource(resolvePath(resourceName));
    }

    public List<Resource> getIncludedResources(String nestedPattern) {
        try {
            return Arrays.asList(resourcePatternResolver.getResources(resolvePath("")+nestedPattern));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public PathMatchingResourcePatternResolver getResourcePatternResolver() {
        return resourcePatternResolver;
    }

    public void setResourcePatternResolver(PathMatchingResourcePatternResolver resourcePatternResolver) {
        this.resourcePatternResolver = resourcePatternResolver;
    }

}