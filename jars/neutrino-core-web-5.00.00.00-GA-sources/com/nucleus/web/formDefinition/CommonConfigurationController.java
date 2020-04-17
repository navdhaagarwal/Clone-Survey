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
package com.nucleus.web.formDefinition;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import net.bull.javamelody.MonitoredWithSpring;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.nucleus.core.configuration.entity.CommonConfiguration;
import com.nucleus.core.formDefinition.CommonConfigurationService;
import com.nucleus.web.common.controller.BaseController;

import flexjson.JSONSerializer;

/**
 * @author Nucleus Software Exports Limited
 */
@Controller
@RequestMapping(value = "/CommonConfiguration")
public class CommonConfigurationController extends BaseController {
    @Inject
    @Named("commonConfigurationService")
    private CommonConfigurationService commonConfigurationService;

    @PreAuthorize("hasAuthority('MAKER_MATRIXRULE') or hasAuthority('CHECKER_MATRIXRULE') or hasAuthority('VIEW_MATRIXRULE')")
    @RequestMapping(value = "/showData")
    @MonitoredWithSpring(name = "CCC_CREATE_DEVIATION")
    public @ResponseBody
    Map<String, Object> createDeviation(ModelMap map) {
        List<CommonConfiguration> commonConfigurationsList = commonConfigurationService.getConfigurationData();
        Map<String, Object> commonConfigurationsMap = new HashMap<String, Object>();
        JSONSerializer serializer = new JSONSerializer();
        commonConfigurationsMap.put("configurations", serializer.exclude("*.class").include("name", "uri").exclude("*")
                .deepSerialize(commonConfigurationsList));

        return commonConfigurationsMap;
    }
}
