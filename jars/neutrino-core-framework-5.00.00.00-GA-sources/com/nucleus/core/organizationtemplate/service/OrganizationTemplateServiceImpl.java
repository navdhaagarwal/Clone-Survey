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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import com.nucleus.core.misc.util.DateUtils;
import com.nucleus.core.organization.entity.OrganizationBranch;
import com.nucleus.core.organization.entity.ParentBranchMapping;
import com.nucleus.core.organizationtemplate.entity.OrganizationTemplateMapping;
import com.nucleus.dao.query.NamedQueryExecutor;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.persistence.EntityDao;
import com.nucleus.service.BaseServiceImpl;
import com.nucleus.user.User;

// TODO: Auto-generated Javadoc
/**
 * The Class OrganizationTemplateServiceImpl.
 *  @author Nucleus Software Exports Limited
 */
@Named("organizationTemplateService")
public class OrganizationTemplateServiceImpl extends BaseServiceImpl implements OrganizationTemplateService {

    /** The pro. */
    private Properties          pro        = new Properties();

    /** The entity dao. */
    @Inject
    @Named("entityDao")
    private EntityDao           entityDao;

    private static final String source_CAS = com.nucleus.core.organization.entity.SystemName.SOURCE_PRODUCT_TYPE_CAS;
    private static final String source_LMS = com.nucleus.core.organization.entity.SystemName.SOURCE_PRODUCT_TYPE_LMS;

    /**
     * Retrieve all the keys for from a property file
     * @param fileLocation the file location
     * @return the all keys from property file
     */
    @Override
    public Set<Object> getAllKeysFromPropertyFile(String fileLocation) {
        loadFile(fileLocation, pro);
        Set<Object> key = pro.keySet();
        return key;
    }

    /**
     * Retrieve value for a key for from a property file.
     *
     * @param fileLocation the file location
     * @param key the key
     * @return the valuefor key
     */
    @Override
    public String getValueforKey(String fileLocation, String key) {
        // loadFile(fileLocation, pro);
        return pro.getProperty(key);
    }

    /**
     * retrieve all overridden keys for a branch.
     *
     * @param organizationBranchID the organization branch id
     * @return the overridedkeys with value
     */
    @Override
    public List<OrganizationTemplateMapping> getOverridedkeysWithValue(Long organizationBranchID) {
        NamedQueryExecutor<OrganizationTemplateMapping> executor = new NamedQueryExecutor<OrganizationTemplateMapping>(
                "MailTemplate.getByOrganizationBranchID").addParameter("organizationBranchID", organizationBranchID);
        return entityDao.executeQuery(executor);
    }

    /**
     * Load file.
     *
     * @param fileLocation the file location
     * @param pro the pro
     */
    private void loadFile(String fileLocation, Properties pro) {
        try {
            InputStream inputStream = new PathMatchingResourcePatternResolver().getResource("classpath:" + fileLocation)
                    .getInputStream();
            pro.load(inputStream);
        } catch (IOException e) {
            BaseLoggers.exceptionLogger.error("IOException while loading file from:" + fileLocation, e.getMessage());
        }
    }

    /**
     * Save or Update mail template along
     * with uri of the user and timestamp
     * @param mailTemplate
     * @param user
     */
    @Override
    public void saveOrUpdateMailTemplate(OrganizationTemplateMapping mailTemplate, User user) {
        if (user != null && mailTemplate != null) {
            mailTemplate.getEntityLifeCycleData().setCreatedByUri(user.getUri());
            mailTemplate.getEntityLifeCycleData().setCreationTimeStamp(DateUtils.getCurrentUTCTime());
            if (mailTemplate.getId() == null) {
                entityDao.persist(mailTemplate);
            } else {
                entityDao.update(mailTemplate);
            }
        }
    }

    /**
     * For any Branch if there exists
     * any template key then get that message.
     * If Any Of the parent of that branch
     * have any Template key then get the message of
     * the parent.Else get the message from
     * Property file.
     *
     * @param branch the branch
     * @param templateKey the template key
     * @return the branch template
     */
    @Override
    public String getBranchTemplate(OrganizationBranch branch, String templateKey, String sysName) {
        List branchList = new ArrayList();
        // Get list Of Org Branch ID's
        populateBranchHierarchy(branch, branchList, sysName);

        // get MailTemplate of these Branch Id's
        NamedQueryExecutor<OrganizationTemplateMapping> executor = new NamedQueryExecutor<OrganizationTemplateMapping>(
                "MailTemplate.getBranchSpecificDetails").addParameter("organizationBranchIDs", branchList).addParameter(
                "templateKey", templateKey);
        List<OrganizationTemplateMapping> list = entityDao.executeQuery(executor);

        if (list != null && list.size() > 0) {
            // Map contains ID's as key and MailTemplate as value
            Map<Long, OrganizationTemplateMapping> map = new HashMap<Long, OrganizationTemplateMapping>();
            for (OrganizationTemplateMapping mailDetails : list) {
                map.put(mailDetails.getOrganizationBranch().getId(), mailDetails);
            }

            // If ID's match with map key retuirn that template message else from property file
            for (int i = 0 ; i < branchList.size() ; i++) {
                if (map.containsKey(branchList.get(i))) {
                    return map.get(branchList.get(i)).getTemplateMessage();
                }
            }
        }
        return null;
    }

    /**
     * Get the List Of Organisation Branch and
     * its parents.
     *
     * @param branch the branch
     * @param branchList the branch list
     */
    private void populateBranchHierarchy(OrganizationBranch branch, List branchList, String sourceProduct) {
        branchList.add(branch.getId());
        if (branch.getParentBranchMapping() != null) {
            for (ParentBranchMapping mapping : branch.getParentBranchMapping()) {
                if (mapping.getModuleName().getCode().equalsIgnoreCase(sourceProduct)) {
                    populateBranchHierarchy(mapping.getParentBranch(), branchList, sourceProduct);
                }
            }
        }
    }

}
