/**
 * This file and a proportion of its content is copyright of Nucleus Software Exports Limited - © 2012. All rights reserved.
 * Any redistribution or reproduction of part or all of the contents in any form is prohibited other than the following:
 * - you cannot print or download to a local hard disk extract contents either part or full for personal/ commercial/
 * academic or any other use
 * - you may not copy the content to individual/ third parties for any type of use, either as compiled or source format
 * without the knowledge and consent of Nucleus Software
 * - You may not, except with our express written permission, distribute or commercially exploit the content. Nor may you
 * transmit it or store it in any other web site or other form of electronic retrieval system.
 */
package com.nucleus.core.organizationtemplate.service;

import java.util.List;
import java.util.Set;

import com.nucleus.core.organization.entity.OrganizationBranch;
import com.nucleus.core.organizationtemplate.entity.OrganizationTemplateMapping;
import com.nucleus.user.User;

// TODO: Auto-generated Javadoc
/**
 * The Interface OrganizationTemplateService.
 * @author Nucleus Software Exports Limited
 */
public interface OrganizationTemplateService {

    /**
     * Retrieve all the keys for from a property file.
     *
     * @param fileLocation the file location
     * @return the all keys from property file
     */
    public Set<Object> getAllKeysFromPropertyFile(String fileLocation);

    /**
     * Retrieve value for a key for from a property file.
     *
     * @param fileLocation the file location
     * @param key the key
     * @return the valuefor key
     */
    public String getValueforKey(String fileLocation, String key);

    /**
     * retrieve all overridden keys for a branch.
     *
     * @param organizationBranchID the organization branch id
     * @return the overridedkeys with value
     */
    public List<OrganizationTemplateMapping> getOverridedkeysWithValue(Long organizationBranchID);

    /**
     * save mail template object.
     *
     * @param mailTemplate the mail template
     * @param user the user
     */
    public void saveOrUpdateMailTemplate(OrganizationTemplateMapping mailTemplate, User user);

    /**
     * Retrieve mail template for a branch.
     *
     * @param branch the branch
     * @param templateKey the template key
     * @return the branch template
     */
    public String getBranchTemplate(OrganizationBranch branch, String templateKey, String sourceName);

}
