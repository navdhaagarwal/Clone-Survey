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
package com.nucleus.rules.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import com.nucleus.rules.usage.RuleUsageDataExtractor;
import com.nucleus.rules.usage.RuleUsageDataService;

/**
 * This Service gives all the Modules in the Application
 * where A particular Rule is used
 * @author Nucleus Software Exports Limited
 */

@Named(value = "ruleUsageService")
public class RuleUsageServiceImpl extends RuleServiceImpl implements RuleUsageService {

    @Inject
    @Named(value = "ruleUsageDataService")
    private RuleUsageDataService ruleUsageDataService;

    /**
     * Method to give Map containing list
     * of all the places where a particular rule is used
     */
    @Override
    public Map<String, List> getRulesUsages(Long id) {
        Map<String, List> map = new HashMap<String, List>();

        for (RuleUsageDataExtractor ruleUsageDataExtractor : ruleUsageDataService.getRuleUsageDataExtractorList()) {
            map.put(ruleUsageDataExtractor.getKey(), ruleUsageDataExtractor.getData(id));
        }

        return map;
    }
}
