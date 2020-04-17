package com.nucleus.web.passwordpolicy;

import com.nucleus.config.persisted.enity.Configuration;
import com.nucleus.config.persisted.service.ConfigurationService;

import com.nucleus.makerchecker.ColumnConfiguration;

import com.nucleus.passwordpolicy.PasswordCreationPolicy;
import com.nucleus.passwordpolicy.service.PasswordValidationService;
import com.nucleus.passwordpolicy.vo.PasswordPolicyVO;
import com.nucleus.persistence.EntityDao;

import com.nucleus.web.common.controller.BaseController;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;


import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

import java.util.*;

@Controller
@RequestMapping(value = "/PasswordCreationPolicyGrid")
public class PasswordPolicyGridController extends BaseController{

    @Inject
    @Named("entityDao")
    private EntityDao entityDao;

    @Inject
    @Named("passwordValidationService")
    private PasswordValidationService passwordValidationService;

    @Inject
    @Named("configurationService")
    private ConfigurationService configurationService;

    private static List<ColumnConfiguration> columnConfigurationList = new ArrayList<>();

    private static final String                masterId      = "PasswordCreationPolicy";

    @PreAuthorize("hasAuthority('EDIT_PASSWORDCREATIONPOLICY') or hasAuthority('VIEW_PASSWORDCREATIONPOLICY') ")
    @RequestMapping(value = "/PasswordCreationPolicy/PasswordCreationPolicy/loadColumnConfig")
    public String displayGrid(ModelMap map, HttpServletRequest request) {
        List<PasswordPolicyVO> passwordPolicyVOList = new ArrayList<>();

        List<PasswordCreationPolicy> passwordCreationPolicyList = entityDao.findAll(PasswordCreationPolicy.class);
        for (PasswordCreationPolicy passwordCreationPolicy : passwordCreationPolicyList) {
            PasswordPolicyVO passwordPolicyVO = new PasswordPolicyVO();
            passwordPolicyVO.setConfigValue(passwordCreationPolicy.getConfigValue());
            passwordPolicyVO.setEnabled(passwordCreationPolicy.getEnabled());
            passwordPolicyVO.setName(passwordCreationPolicy.getName());
            passwordPolicyVO.setEntityId(passwordCreationPolicy.getId());
            passwordPolicyVO.setType(PasswordPolicyVO.PASSWORD_POLICY);
            passwordPolicyVO.setDescription(passwordCreationPolicy.getDescription());
            passwordPolicyVOList.add(passwordPolicyVO);
        }

        Configuration c1 = passwordValidationService.getConfigurationFromPropertyKey("config.user.allowed.failedLoginAttempts");
        if(c1!=null) {
            PasswordPolicyVO passwordPolicyVO = new PasswordPolicyVO();
            passwordPolicyVO.setEntityId(c1.getId());
            passwordPolicyVO.setName(c1.getPropertyKey());
            passwordPolicyVO.setConfigValue(c1.getPropertyValue());
            passwordPolicyVO.setType(PasswordPolicyVO.CONFIGURATION);
            passwordPolicyVO.setEnabled(true);
            passwordPolicyVO.setDescription("Maximum failed login allowed");
            passwordPolicyVOList.add(passwordPolicyVO);
        }

        Configuration c2 = passwordValidationService.getConfigurationFromPropertyKey("config.user.allowed.passwordHistoryCount");
        if (c2 != null){
            PasswordPolicyVO passwordPolicyVO1 = new PasswordPolicyVO();
            passwordPolicyVO1.setEntityId(c2.getId());
            passwordPolicyVO1.setName(c2.getPropertyKey());
            passwordPolicyVO1.setConfigValue(c2.getPropertyValue());
            passwordPolicyVO1.setType(PasswordPolicyVO.CONFIGURATION);
            passwordPolicyVO1.setEnabled(true);
            passwordPolicyVO1.setDescription("Password history count");
            passwordPolicyVOList.add(passwordPolicyVO1);
        }

        Configuration c3 = passwordValidationService.getConfigurationFromPropertyKey("config.password.prefix");
        if(c3!=null) {
            PasswordPolicyVO passwordPolicyVO2 = new PasswordPolicyVO();
            passwordPolicyVO2.setEntityId(c3.getId());
            passwordPolicyVO2.setName(c3.getPropertyKey());
            passwordPolicyVO2.setConfigValue(c3.getPropertyValue());
            passwordPolicyVO2.setType(PasswordPolicyVO.CONFIGURATION);
            passwordPolicyVO2.setEnabled(true);
            passwordPolicyVO2.setDescription("Prefix for default password");
            passwordPolicyVOList.add(passwordPolicyVO2);

        }
        Configuration c4 = passwordValidationService.getConfigurationFromPropertyKey("config.autoInactivateUser.days");
        if(c4!=null) {
            PasswordPolicyVO passwordPolicyVO2 = new PasswordPolicyVO();
            passwordPolicyVO2.setEntityId(c4.getId());
            passwordPolicyVO2.setName(c4.getPropertyKey());
            passwordPolicyVO2.setConfigValue(c4.getPropertyValue());
            passwordPolicyVO2.setType(PasswordPolicyVO.CONFIGURATION);
            passwordPolicyVO2.setEnabled(true);
            passwordPolicyVO2.setDescription("Number of days after which inactive user is blocked");
            passwordPolicyVOList.add(passwordPolicyVO2);

        }
        Configuration c5 = passwordValidationService.getConfigurationFromPropertyKey("config.initialDormancy.days");
        if(c5!=null) {
            PasswordPolicyVO passwordPolicyVO2 = new PasswordPolicyVO();
            passwordPolicyVO2.setEntityId(c5.getId());
            passwordPolicyVO2.setName(c5.getPropertyKey());
            passwordPolicyVO2.setConfigValue(c5.getPropertyValue());
            passwordPolicyVO2.setType(PasswordPolicyVO.CONFIGURATION);
            passwordPolicyVO2.setEnabled(true);
            passwordPolicyVO2.setDescription("Number of days after which first time users are blocked");
            passwordPolicyVOList.add(passwordPolicyVO2);

        }

        Configuration c6 = passwordValidationService.getConfigurationFromPropertyKey("config.globalExpiry.days");
        if(c6!=null) {
            PasswordPolicyVO passwordPolicyVO2 = new PasswordPolicyVO();
            passwordPolicyVO2.setEntityId(c6.getId());
            passwordPolicyVO2.setName(c6.getPropertyKey());
            passwordPolicyVO2.setConfigValue(c6.getPropertyValue());
            passwordPolicyVO2.setType(PasswordPolicyVO.CONFIGURATION);
            passwordPolicyVO2.setEnabled(true);
            passwordPolicyVO2.setDescription("User expiry days");
            passwordPolicyVOList.add(passwordPolicyVO2);

        }

        Configuration c7 = passwordValidationService.getConfigurationFromPropertyKey("config.applyGlobalExpiry");
        if(c7!=null) {
            PasswordPolicyVO passwordPolicyVO2 = new PasswordPolicyVO();
            passwordPolicyVO2.setEntityId(c7.getId());
            passwordPolicyVO2.setName(c7.getPropertyKey());
            passwordPolicyVO2.setConfigValue(c7.getPropertyValue());
            passwordPolicyVO2.setType(PasswordPolicyVO.CONFIGURATION);
            passwordPolicyVO2.setEnabled(true);
            passwordPolicyVO2.setDescription("Enable global config for User expiry days");
            passwordPolicyVOList.add(passwordPolicyVO2);

        }
        map.put("passwordPolicyVOList",passwordPolicyVOList);
        map.put("masterId", masterId);

        return "passwordCreationPolicyGrid";
    }





}
