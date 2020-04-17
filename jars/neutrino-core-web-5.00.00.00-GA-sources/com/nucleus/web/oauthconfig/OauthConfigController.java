package com.nucleus.web.oauthconfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.validator.routines.UrlValidator;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.util.UriUtils;

import com.nucleus.dao.query.NamedQueryExecutor;
import com.nucleus.entity.ApprovalStatus;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.makerchecker.MakerCheckerService;
import com.nucleus.oauth.config.OauthConfig;
import com.nucleus.user.User;
import com.nucleus.user.UserInfo;
import com.nucleus.user.UserService;
import com.nucleus.web.common.controller.BaseController;

@Controller
@RequestMapping(value = "/OauthConfig")
public class OauthConfigController extends BaseController {
	
	@Inject
    @Named("makerCheckerService")
    private MakerCheckerService makerCheckerService;

    @Inject
    @Named("userService")
    private UserService userService;
    
    @Inject
    @Named("stringEncryptor")
    private StandardPBEStringEncryptor encryptor;
    
    private static final String MASTER_ID = "OauthConfig";
    private static final String MASTER_ID_KEY = "masterID";
    private static final String OAUTH_CONFIG_KEY = "oauthConfig";
    private static final String EDIT_KEY = "edit";
    private static final String VIEWABLE_KEY = "viewable";
    
    /*Method Added to send current Entity Uri for working of comments,activity,history,notes*/
    @ModelAttribute("currentEntityClassName")
    public String getEntityClassName() {
        String encrypt = "enc_" + encryptor.encrypt(OauthConfig.class.getName());
        return UriUtils.encodeQueryParam(encrypt, "UTF-8");
    }
    
    @PreAuthorize("hasAuthority('MAKER_OAUTHCONFIG')")
    @RequestMapping(value = "/create")
    public String createOauthCconfig(ModelMap map) {
        OauthConfig oauthConfig = new OauthConfig();
        map.put(OAUTH_CONFIG_KEY, oauthConfig);
        map.put(MASTER_ID_KEY, MASTER_ID);
        return OAUTH_CONFIG_KEY;
    }
    
    /**
     * @param record id for edit.
     * 
     * @return void
     * @throws
     * @description to edit oauthConfig.
     */
    @PreAuthorize("hasAuthority('MAKER_OAUTHCONFIG')")
    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/edit/{id}")
    public String editOauthConfig(@PathVariable("id") Long id, ModelMap map) {
        UserInfo currentUser = getUserDetails();
        OauthConfig oauthConfig = baseMasterService.getMasterEntityWithActionsById(OauthConfig.class, id, currentUser.getUserEntityId().getUri());
        if (oauthConfig.getApprovalStatus() == ApprovalStatus.UNAPPROVED_MODIFIED) {
            map.put("editLink", false);
        }
        map.put(OAUTH_CONFIG_KEY, oauthConfig);
        map.put(EDIT_KEY, true);
        map.put(MASTER_ID_KEY, MASTER_ID);
        ArrayList<String> actions = (ArrayList<String>) oauthConfig.getViewProperties().get("actions");
        if (actions != null) {
            for (String act : actions) {
                map.put("act" + act, false);
            }
        }
        return OAUTH_CONFIG_KEY;
    }
    
    /**
     * @param record id for view.
     * 
     * @return void
     * @throws
     * @description to view OauthConfig.
     */
    @SuppressWarnings("unchecked")
    @PreAuthorize("hasAuthority('VIEW_OAUTHCONFIG') or hasAuthority('MAKER_OAUTHCONFIG') or hasAuthority('CHECKER_OAUTHCONFIG')")
    @RequestMapping(value = "/view/{id}", method = RequestMethod.GET)
    public String viewOauthConfig(@PathVariable("id") Long id, ModelMap map) {
    	UserInfo currentUser = getUserDetails();
        OauthConfig oauthConfig = baseMasterService.getMasterEntityWithActionsById(OauthConfig.class, id, currentUser.getUserEntityId().getUri());
        map.put(OAUTH_CONFIG_KEY, oauthConfig);
        map.put(MASTER_ID_KEY, MASTER_ID);
        map.put(VIEWABLE_KEY, true);
        if (oauthConfig.getViewProperties() != null) {
            ArrayList<String> actions = (ArrayList<String>) oauthConfig.getViewProperties().get("actions");
            if (actions != null) {
                for (String act : actions) {
                    String actionString = "act" + act;
                    map.put(actionString.replaceAll(" ", ""), false);
                }
            }
        }
        return OAUTH_CONFIG_KEY;
    }
    
    @PreAuthorize("hasAuthority('MAKER_OAUTHCONFIG')")
    @RequestMapping(value = "/save", method = RequestMethod.POST)
    public String saveOauthConfig(@Validated OauthConfig oauthConfig, BindingResult bindingResult, ModelMap map,
			@RequestParam("createAnotherMaster") boolean createAnotherMaster, MultipartHttpServletRequest request) {
		BaseLoggers.flowLogger.debug(oauthConfig.getLogInfo());
		OauthConfig existingOauthConfig = null;
		if (oauthConfig.getId() != null) {
			existingOauthConfig = entityDao.find(OauthConfig.class, oauthConfig.getId());
			if (null != existingOauthConfig.getEntityLifeCycleData()) {
				oauthConfig.setEntityLifeCycleData(existingOauthConfig.getEntityLifeCycleData());
			}
			if (null != existingOauthConfig.getMasterLifeCycleData()) {
				oauthConfig.setMasterLifeCycleData(existingOauthConfig.getMasterLifeCycleData());
			}
		}
		NamedQueryExecutor<OauthConfig> executor = new NamedQueryExecutor<>("getActiveOauthConfigByClientId");
		executor.addParameter("clientId", oauthConfig.getClientId()).addParameter("approvalStatusList",
				ApprovalStatus.APPROVED_RECORD_STATUS_IN_APPROVED_APPROVED_MODIFIED);
		List<OauthConfig> existingConfigForSameClientId = entityDao.executeQuery(executor);
		map.put("isUniqueClientId", existingConfigForSameClientId.isEmpty());
		Map<String, Object> validateMap = new HashMap<>();
		validateMap.put("clientId", oauthConfig.getClientId());
		if (!existingConfigForSameClientId.isEmpty()) {
			bindingResult.rejectValue("clientId", "label.clientId.validation.already.exists");
		}
		// we need to get below logged in user from session
		User user = getUserDetails().getUserReference();
		if (user != null) {
			makerCheckerService.masterEntityChangedByUser(oauthConfig, user);
		}
		if (createAnotherMaster) {
			OauthConfig oauthConfigToCreateAnother = new OauthConfig();
			map.put(OAUTH_CONFIG_KEY, oauthConfigToCreateAnother);
			map.put(MASTER_ID_KEY, MASTER_ID);
			return OAUTH_CONFIG_KEY;
		}
		map.put(MASTER_ID_KEY, MASTER_ID);
		return "redirect:/app/grid/OauthConfig/OauthConfig/loadColumnConfig";
	}

    @PreAuthorize("hasAuthority('MAKER_OAUTHCONFIG')")
    @RequestMapping(value = "/saveAndSendForApproval", method = RequestMethod.POST)
    public String saveAndSendForApprovalOauthConfig(@Validated OauthConfig oauthConfig, BindingResult bindingResult, ModelMap map,
			@RequestParam("createAnotherMaster") boolean createAnotherMaster, MultipartHttpServletRequest request) {
		BaseLoggers.flowLogger.debug(oauthConfig.getLogInfo());
		OauthConfig existingOauthConfig = null;
		if (oauthConfig.getId() != null) {
			existingOauthConfig = entityDao.find(OauthConfig.class, oauthConfig.getId());
			if (null != existingOauthConfig.getEntityLifeCycleData()) {
				oauthConfig.setEntityLifeCycleData(existingOauthConfig.getEntityLifeCycleData());
			}
			if (null != existingOauthConfig.getMasterLifeCycleData()) {
				oauthConfig.setMasterLifeCycleData(existingOauthConfig.getMasterLifeCycleData());
			}
		}
		NamedQueryExecutor<OauthConfig> executor = new NamedQueryExecutor<>("getActiveOauthConfigByClientId");
		executor.addParameter("clientId", oauthConfig.getClientId()).addParameter("approvalStatusList",
				ApprovalStatus.APPROVED_RECORD_STATUS_IN_APPROVED_APPROVED_MODIFIED);
		List<OauthConfig> existingConfigForSameClientId = entityDao.executeQuery(executor);
		map.put("isUniqueClientId", existingConfigForSameClientId.isEmpty());
		Map<String, Object> validateMap = new HashMap<>();
		validateMap.put("clientId", oauthConfig.getClientId());
		if (!existingConfigForSameClientId.isEmpty()) {
			bindingResult.rejectValue("clientId", "label.clientId.validation.already.exists");
		}
		// we need to get below logged in user from session
		User user = getUserDetails().getUserReference();
		if (user != null) {
			makerCheckerService.saveAndSendForApproval(oauthConfig, user);
		}
		if (createAnotherMaster) {
			OauthConfig oauthConfigToCreateAnother = new OauthConfig();
			map.put(OAUTH_CONFIG_KEY, oauthConfigToCreateAnother);
			map.put(MASTER_ID_KEY, MASTER_ID);
			return OAUTH_CONFIG_KEY;
		}
		map.put(MASTER_ID_KEY, MASTER_ID);
		return "redirect:/app/grid/OauthConfig/OauthConfig/loadColumnConfig";
	}
    
}
