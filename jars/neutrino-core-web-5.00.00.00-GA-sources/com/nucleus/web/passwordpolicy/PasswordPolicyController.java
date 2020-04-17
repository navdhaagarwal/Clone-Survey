package com.nucleus.web.passwordpolicy;

import com.nucleus.config.persisted.enity.Configuration;
import com.nucleus.config.persisted.enity.ConfigurationGroup;
import com.nucleus.config.persisted.service.ConfigurationService;
import com.nucleus.config.persisted.service.ConfigurationServiceImpl;
import com.nucleus.core.cache.FWCacheHelper;
import com.nucleus.core.misc.util.DateUtils;
import com.nucleus.entity.EntityId;
import com.nucleus.entity.SystemEntity;
import com.nucleus.finnone.pro.base.Message;
import com.nucleus.finnone.pro.cache.constants.FWCacheConstants;
import com.nucleus.passwordpolicy.PasswordCreationConfigurationFactory;
import com.nucleus.passwordpolicy.PasswordCreationPolicy;
import com.nucleus.passwordpolicy.PasswordPolicyDictWords;
import com.nucleus.passwordpolicy.service.PasswordValidationService;
import com.nucleus.passwordpolicy.vo.PasswordPolicyVO;
import com.nucleus.persistence.EntityDao;
import com.nucleus.web.common.controller.BaseController;
import oracle.jdbc.proxy.annotation.Pre;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping(value = "/PasswordPolicy")
public class PasswordPolicyController extends BaseController {

    @Inject
    @Named("passwordValidationService")
    private PasswordValidationService passwordValidationService;

    @Inject
    @Named("entityDao")
    private EntityDao entityDao;

    @Inject
    @Named("configurationService")
    private ConfigurationService configurationService;
    
    @Inject
    @Named("fwCacheHelper")
    private FWCacheHelper fwCacheHelper;

    private static final String masterId = "PasswordCreationPolicy";

    private static final String BOOLEAN_TRUE = "TRUE";
    private static final String BOOLEAN_FALSE = "FALSE";

    @PreAuthorize("hasAuthority('EDIT_PASSWORDCREATIONPOLICY')")
    @RequestMapping(value = "/validate", method = RequestMethod.POST)
    @ResponseBody
    public String validate(@RequestParam("type")String type, @RequestParam("name") String name, @RequestParam("configValue") String configValue){
        String error="";
        if(type.equalsIgnoreCase(PasswordPolicyVO.PASSWORD_POLICY)) {

            if (name.equalsIgnoreCase(PasswordCreationConfigurationFactory.MAXIMUM_LENGTH.toString())) {
                PasswordCreationPolicy pcp = passwordValidationService.getPasswordPolicyByName(PasswordCreationConfigurationFactory.MINIMUM_LENGTH.toString());
                if (Integer.valueOf(pcp.getConfigValue()) > Integer.valueOf(configValue)) {
                    Message message = new Message();
                    message.setI18nCode("msg.8000014");

                    error = passwordValidationService.getMessageDescription(message, passwordValidationService.getLocale());

                }
            }
            if (name.equalsIgnoreCase(PasswordCreationConfigurationFactory.MINIMUM_LENGTH.toString())) {
                PasswordCreationPolicy pcp = passwordValidationService.getPasswordPolicyByName(PasswordCreationConfigurationFactory.MAXIMUM_LENGTH.toString());
                if (Integer.valueOf(pcp.getConfigValue()) < Integer.valueOf(configValue)) {
                    Message message = new Message();
                    message.setI18nCode("msg.8000015");

                    error = passwordValidationService.getMessageDescription(message, passwordValidationService.getLocale());
                }
            }
        }
        return error;
    }

    @PreAuthorize("hasAuthority('EDIT_PASSWORDCREATIONPOLICY')")
    @RequestMapping(value = "/save", method = RequestMethod.POST)
    public String savePasswordPolicy(
            @ModelAttribute("passwordPolicyVO") PasswordPolicyVO passwordPolicyVO,
            BindingResult result, ModelMap map) {

        if(passwordPolicyVO.getType().equals(PasswordPolicyVO.PASSWORD_POLICY)) {

            PasswordCreationPolicy passwordCreationPolicyOld = entityDao.find(PasswordCreationPolicy.class, passwordPolicyVO.getEntityId());

            passwordCreationPolicyOld.setDescription(passwordPolicyVO.getDescription());
            passwordCreationPolicyOld.setEnabled(passwordPolicyVO.getEnabled());
            passwordCreationPolicyOld.setErrorCode(passwordPolicyVO.getErrorCode());
            passwordCreationPolicyOld.setConfigValue(passwordPolicyVO.getConfigValue());
            passwordCreationPolicyOld.getEntityLifeCycleData().setLastUpdatedByUri(getUserDetails().getUserEntityId().getUri());
            passwordCreationPolicyOld.getEntityLifeCycleData().setLastUpdatedTimeStamp(DateUtils.getCurrentUTCTime());
            entityDao.update(passwordCreationPolicyOld);

        }
        if(passwordPolicyVO.getType().equals(PasswordPolicyVO.CONFIGURATION)) {
            Configuration configuration = entityDao.find(Configuration.class,passwordPolicyVO.getEntityId());
            configuration.setPropertyValue(passwordPolicyVO.getConfigValue().toString());
            entityDao.update(configuration);
            ConfigurationGroup configurationGroup = configurationService
                    .getConfigurationGroupFor(SystemEntity.getSystemEntityId(), false);
            Map<String,Object> dataMap = new HashMap<>();
            dataMap.put(ConfigurationServiceImpl.CONFIGURATION_GROUP_OBJECT, configurationGroup);
            dataMap.put(FWCacheConstants.IMPACTED_CACHE_MAP, fwCacheHelper.createAndGetImpactedCachesFromCacheNames(
					FWCacheConstants.CONFIGURATION_DISTINCT_MODIFIABLE_PROPERTYKEY,
					FWCacheConstants.CONFIGURATION_DISTINCT_PROPERTKEY, FWCacheConstants.CONFIGURATION_GROUP_ID,
					FWCacheConstants.CONFIGURATION_GROUP_CACHE_ASSOCIATED_ENTITY,
					FWCacheConstants.ENTITYURI_PROPKEY_CONFIG_MAP, FWCacheConstants.ENTITYURI_PROPKEY_CONFIGVO_MAP));
            configurationService.updateConfigurationCache(dataMap);
           
        }

        return "redirect:/app/PasswordCreationPolicyGrid/PasswordCreationPolicy/PasswordCreationPolicy/loadColumnConfig";

    }

    @PreAuthorize("hasAuthority('EDIT_PASSWORDCREATIONPOLICY')")
    @RequestMapping(value = "/edit/{type}/{id}")
    public String editPasswordPolicy(@PathVariable("id") Long id,@PathVariable("type")String type,  ModelMap map) {

        PasswordPolicyVO passwordPolicyVO = prepareForEditAndView(id,type,map);

        map.put("passwordPolicyVO", passwordPolicyVO);
        map.put("masterId", masterId);
        map.put("viewable",false);
        return "passwordCreationPolicy";
    }

    @PreAuthorize("hasAuthority('EDIT_PASSWORDCREATIONPOLICY') or hasAuthority('VIEW_PASSWORDCREATIONPOLICY') ")
    @RequestMapping(value = "/view/{type}/{id}")
    public String viewFormMapping(@PathVariable("id") Long id,@PathVariable("type")String type,ModelMap map) {

        PasswordPolicyVO passwordPolicyVO = prepareForEditAndView(id,type,map);
        map.put("passwordPolicyVO", passwordPolicyVO);
        map.put("viewable", true);
        map.put("masterId", masterId);
        return "passwordCreationPolicy";

    }

    private PasswordPolicyVO prepareForEditAndView(Long id, String type,ModelMap map){
        PasswordPolicyVO passwordPolicyVO = new PasswordPolicyVO();

        if(type.equals(PasswordPolicyVO.PASSWORD_POLICY)) {
            PasswordCreationPolicy passwordCreationPolicy = entityDao.find(PasswordCreationPolicy.class, id);
            passwordPolicyVO.setConfigValue(passwordCreationPolicy.getConfigValue());
            passwordPolicyVO.setErrorCode(passwordCreationPolicy.getErrorCode());
            passwordPolicyVO.setEnabled(passwordCreationPolicy.getEnabled());
            passwordPolicyVO.setDescription(passwordCreationPolicy.getDescription());
            passwordPolicyVO.setName(passwordCreationPolicy.getName());
            passwordPolicyVO.setEntityId(id);
            passwordPolicyVO.setType(type);

            if (passwordCreationPolicy.getName().equals(PasswordCreationConfigurationFactory.CONTAINS_DICTIONARY_WORDS)) {
                List dictWords = entityDao.findAll(PasswordPolicyDictWords.class);
                map.put("dictWords", dictWords);
            }
        }
        if(type.equals(PasswordPolicyVO.CONFIGURATION)){
            Configuration configuration = entityDao.find(Configuration.class,id);
            passwordPolicyVO.setEntityId(id);
            passwordPolicyVO.setType(type);
            passwordPolicyVO.setName(configuration.getPropertyKey());
            passwordPolicyVO.setConfigValue(configuration.getPropertyValue());
            passwordPolicyVO.setUserModifiable(configuration.isUserModifiable());
            passwordPolicyVO.setValueType(configuration.getValueType().toString());

        }
        List trueFalseValueList = new ArrayList();
        trueFalseValueList.add(BOOLEAN_TRUE);
        trueFalseValueList.add(BOOLEAN_FALSE);
        map.put("trueFalseValueList",trueFalseValueList);
        return passwordPolicyVO;
    }

}
