/**
 * This file and a proportion of its content is copyright of Nucleus Software Exports Limited - 2012. All rights reserved.
 * Any redistribution or reproduction of part or all of the contents in any form is prohibited other than the following:
 * - you cannot print or download to a local hard disk extract contents either part or full for personal/ commercial/
 * academic or any other use
 * - you may not copy the content to individual/ third parties for any type of use, either as compiled or source format
 * without the knowledge and consent of Nucleus SOftware
 * - You may not, except with our express written permission, distribute or commercially exploit the content. Nor may you
 * transmit it or store it in any other web site or other form of electronic retrieval system.
 */
package com.nucleus.web.organizationtemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;

import com.nucleus.core.organization.entity.OrganizationBranch;
import com.nucleus.core.organizationtemplate.entity.OrganizationTemplateMapping;
import com.nucleus.core.organizationtemplate.service.OrganizationTemplateService;
import com.nucleus.persistence.EntityDao;
import com.nucleus.user.OrgBranchInfo;
import com.nucleus.user.User;
import com.nucleus.web.common.controller.BaseController;

/**
 * @author Nucleus Software Exports Limited This field is being used for
 *         controlling Mail Template related operations.
 */
@Controller
@RequestMapping(value = "/mailTemplate")
@Transactional
@SessionAttributes("overriddenList")
public class OrganizationTemplateController extends BaseController {

    @Inject
    @Named("organizationTemplateService")
    private OrganizationTemplateService templateService;

    @Inject
    @Named("entityDao")
    private EntityDao                   entityDao;

    private static final String                FILE_LOCATION = "resource-bundles/finnone_mail_message.properties";

    /**
     * method to open first template view page
     * @param map
     * @return
     */
    @PreAuthorize("hasAuthority('MAKER_ORGANIZATION_TEMPLATE_MAPPING') or hasAuthority('CHECKER_ORGANIZATION_TEMPLATE_MAPPING') or hasAuthority('VIEW_ORGANIZATION_TEMPLATE_MAPPING')")
    @RequestMapping(value = "/view")
    public String viewMailTemplate(ModelMap map) {
        createHomePage(map);
        return "mailTemplate";
    }

    /**
     *  method to trigger modal window during new add operation
     * @param map
     * @return
     */
    @PreAuthorize("hasAuthority('MAKER_ORGANIZATION_TEMPLATE_MAPPING')")
    @RequestMapping(value = "/addorEditKey", method = RequestMethod.POST)
    public String addnewKey(@ModelAttribute("overriddenList") List<OrganizationTemplateMapping> overriddenList,
            @RequestParam("index") Integer index, ModelMap map) {
        if (index == -1) {
            Set<Object> keySet = templateService.getAllKeysFromPropertyFile(FILE_LOCATION);
            if (keySet != null) {
                map.put("mailTemplateKeys", getNonEditedKeysForBranch(overriddenList, new ArrayList<Object>(keySet)));
            }
            OrganizationTemplateMapping newMailTemplate = new OrganizationTemplateMapping();
            OrgBranchInfo orgBranchInfo = getUserDetails().getLoggedInBranch();
            OrganizationBranch orgBranch = entityDao.find(OrganizationBranch.class, orgBranchInfo.getId());
            newMailTemplate.setOrganizationBranch(orgBranch);
            map.put("newMailTemplate", newMailTemplate);
        } else if (overriddenList != null && index != null && (overriddenList.size() > index)) {
            OrganizationTemplateMapping currentMailTemplate = overriddenList.get(index);
            map.put("newMailTemplate", currentMailTemplate);
            map.put("keyDisable", true);
        }
        map.put("index", index);
        return "newMailTemplate";
    }

    /**
     * Saving new or edited Label key and value
     * @param mailTemplate
     * @return
     */
    @PreAuthorize("hasAuthority('MAKER_ORGANIZATION_TEMPLATE_MAPPING')")
    @RequestMapping(value = "/saveKey", method = RequestMethod.POST)
    public String saveKey(@ModelAttribute("newMailTemplate") OrganizationTemplateMapping mailTemplate, ModelMap map) {
        User user = getUserDetails().getUserReference();
        templateService.saveOrUpdateMailTemplate(mailTemplate, user);
        createHomePage(map);
        return "mailTemplate";
    }

    private List<String> getNonEditedKeysForBranch(List<OrganizationTemplateMapping> obejctInTable,
            List<Object> keysInPropertyFile) {
        List<String> keysInTable = new ArrayList<String>();
        List<String> nonEditedKeysForBranch = new ArrayList<String>();
        for (OrganizationTemplateMapping mailTemplate : obejctInTable) {
            keysInTable.add(mailTemplate.getTemplateKey());
        }
        for (Object object : keysInPropertyFile) {
            if (!keysInTable.contains(object)) {
                nonEditedKeysForBranch.add(object.toString());
            }
        }
        return nonEditedKeysForBranch;
    }

    private void createHomePage(ModelMap map) {
        OrgBranchInfo orgBranchInfo = getUserDetails().getLoggedInBranch();
        OrganizationBranch loggedInBranch = entityDao.find(OrganizationBranch.class, orgBranchInfo.getId());
        OrganizationTemplateMapping mailTemplate = new OrganizationTemplateMapping();
        if (loggedInBranch != null) {
            mailTemplate.setOrganizationBranch(loggedInBranch);
            // getting all overridden keys for that branch
            List<OrganizationTemplateMapping> overriddenList = templateService.getOverridedkeysWithValue(loggedInBranch
                    .getId());
            map.put("overriddenList", overriddenList);
        }
        map.put("mailTemplate", mailTemplate);
    }

}
