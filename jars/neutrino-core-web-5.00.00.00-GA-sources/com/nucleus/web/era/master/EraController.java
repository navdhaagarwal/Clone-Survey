/**
 * Copyright Nucleus Software India Pvt Ltd. All rights reserved.
 */

package com.nucleus.web.era.master;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.UriUtils;

import com.nucleus.entity.ApprovalStatus;
import com.nucleus.era.Era;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.makerchecker.MakerCheckerService;
import com.nucleus.master.BaseMasterService;
import com.nucleus.user.User;
import com.nucleus.user.UserInfo;
import com.nucleus.user.UserService;
import com.nucleus.web.common.controller.BaseController;
import com.nucleus.web.master.CommonFileIOMasterGridLoad;

/**
 * @author Nucleus Software India Pvt Ltd This field is being used for
 *         controlling era CRUD and task allocation work-flow related
 *         operations.
 */
@Transactional
@Controller
@RequestMapping(value = "/Era")
public class EraController extends BaseController {

    @Inject
    @Named("stringEncryptor")
    private StandardPBEStringEncryptor encryptor;
    @Inject
    @Named("makerCheckerService")
    private MakerCheckerService        makerCheckerService;

    @Inject
    @Named("masterXMLDocumentBuilder")
    private CommonFileIOMasterGridLoad commonFileIOMasterGridLoad;

    @Inject
    @Named("baseMasterService")
    private BaseMasterService          baseMasterService;

    @Inject
    @Named("userService")
    private UserService                userService;

    private static final String                             masterID   = "Era";

    @InitBinder
    protected void initBinder(WebDataBinder binder) {
        binder.setValidator(new EraValidator());
    }

    /*Method Added to send current Entity Uri for working of comments,activity,history,notes*/
    @ModelAttribute("currentEntityClassName")
    public String getEntityClassName() throws UnsupportedEncodingException {
        String encrypt = "enc_" + encryptor.encrypt(Era.class.getName());
        String returnUri = UriUtils.encodeQueryParam(encrypt, "UTF-8");
        return returnUri;
    }

    /**
     * @param era
     *            object containing era name,era symbol and start year of era
     * @return String
     * @throws IOException
     * @description to save era object from view
     */
    @PreAuthorize("hasAuthority('MAKER_ERA')")
    @RequestMapping(value = "/save", method = RequestMethod.POST)
    public String saveEra(@Validated Era era, BindingResult result, ModelMap map,
            @RequestParam("createAnotherMaster") boolean createAnotherMaster) {

        BaseLoggers.flowLogger.debug(era.getLogInfo());
        /*
         * Map whose Key Is Table Column Name with whom to validate and Value is
         * The One to be validated.This Map Is Send in the Validator Method
         */

        Map<String, Object> validateMap = new HashMap<String, Object>();
        validateMap.put("eraName", era.getEraName());

        /*
         * Code to check as if any existing(or new) record is being modified(or created) into another existing record 
         */
        List<String> colNameList = checkValidationForDuplicates(era, Era.class, validateMap);
        if (result.hasErrors() || (colNameList != null && colNameList.size() > 0)) {
            map.put("era", era);
            map.put("masterID", masterID);
            if (era != null && colNameList != null && colNameList.size() > 0) {
                result.rejectValue("eraName", "label.eraName.validation.exists");
            }
            return "era";
        }

        // we need to get below logged in user from session
        User user = getUserDetails().getUserReference();
        if (user != null) {
            makerCheckerService.masterEntityChangedByUser(era, user);
        }
        if (createAnotherMaster) {
            map.put("era", new Era());
            map.put("masterID", masterID);
            return "era";
        }
        map.put("masterID", masterID);
        return "redirect:/app/grid/Era/Era/loadColumnConfig";

    }

    /**
     * @param record
     *            id for edit
     * @return void
     * @throws
     * @description to create era
     */

    @PreAuthorize("hasAuthority('MAKER_ERA')")
    @RequestMapping(value = "/create")
    public String createEra(ModelMap map) {

        map.put("era", new Era());
        map.put("masterID", masterID);
        return "era";
    }

    /**
     * @param record
     *            id for edit
     * @return void
     * @throws
     * @description to edit era
     */
    @PreAuthorize("hasAuthority('MAKER_ERA')")
    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/edit/{id}")
    public String editEra(@PathVariable("id") Long id, ModelMap map) {
        UserInfo currentUser = getUserDetails();
        Era era = baseMasterService.getMasterEntityWithActionsById(Era.class, id, currentUser.getUserEntityId().getUri());
        if (era.getApprovalStatus() == ApprovalStatus.UNAPPROVED_MODIFIED) {
            Era prevEra = (Era) baseMasterService.getLastApprovedEntityByUnapprovedEntityId(era.getEntityId());
            map.put("prevEra", prevEra);
            map.put("editLink", false);
        }
        map.put("era", era);
        map.put("edit", true);
        map.put("masterID", masterID);
        ArrayList<String> actions = (ArrayList<String>) era.getViewProperties().get("actions");
        if (actions != null) {
            for (String act : actions) {
                map.put("act" + act, false);
            }
        }

        return "era";
    }

    /**
     * @description to save and send for approval * @return String
     * @throws IOException
     * @description to save and send for approval era object from view
     */
    @PreAuthorize("hasAuthority('MAKER_ERA')")
    @RequestMapping(value = "/saveAndSendForApproval", method = RequestMethod.POST)
    public String saveAndSendForApproval(@Validated Era era, BindingResult result, ModelMap map,
            @RequestParam("createAnotherMaster") boolean createAnotherMaster) {

        BaseLoggers.flowLogger.debug(era.getLogInfo());
        /*
         * Map whose Key Is Table Column Name with whom to validate and Value is
         * The One to be validated.This Map Is Send in the Validator Method
         */

        Map<String, Object> validateMap = new HashMap<String, Object>();
        validateMap.put("eraName", era.getEraName());

        /*
         * Code to check as if any existing(or new) record is being modified(or created) into another existing record
         */
        List<String> colNameList = checkValidationForDuplicates(era, Era.class, validateMap);
        if (result.hasErrors() || (colNameList != null && colNameList.size() > 0)) {
            map.put("era", era);
            map.put("masterID", masterID);
            if (colNameList != null && colNameList.size() > 0) {
                result.rejectValue("eraName", "label.eraName.validation.exists");
            }
            return "era";
        }

        // we need to get below logged in user from session
        User user = getUserDetails().getUserReference();
        if (user != null) {
            makerCheckerService.saveAndSendForApproval(era, user);
        }
        if (createAnotherMaster) {
            map.put("era", new Era());
            map.put("masterID", masterID);
            return "era";
        }
        map.put("masterID", masterID);
        return "redirect:/app/grid/Era/Era/loadColumnConfig";

    }

    /**
     * @param record
     *            id for view
     * @return void
     * @throws
     * @description to view era
     */
    @SuppressWarnings("unchecked")
    @PreAuthorize("hasAuthority('VIEW_ERA') or hasAuthority('MAKER_ERA') or hasAuthority('CHECKER_ERA')")
    @RequestMapping(value = "/view/{id}", method = RequestMethod.GET)
    public String viewEra(@PathVariable("id") Long id, ModelMap map) {
        UserInfo currentUser = getUserDetails();
        Era era = baseMasterService.getMasterEntityWithActionsById(Era.class, id, currentUser.getUserEntityId().getUri());
        if (era.getApprovalStatus() == ApprovalStatus.UNAPPROVED_MODIFIED
                || era.getApprovalStatus() == ApprovalStatus.WORFLOW_IN_PROGRESS) {
            Era prevEra = (Era) baseMasterService.getLastApprovedEntityByUnapprovedEntityId(era.getEntityId());
            map.put("prevEra", prevEra);
            map.put("editLink", false);
        } else if (era.getApprovalStatus() == ApprovalStatus.APPROVED_MODIFIED) {
            Era prevEra = (Era) baseMasterService.getLastUnApprovedEntityByApprovedEntityId(era.getEntityId());
            map.put("prevEra", prevEra);
            map.put("viewLink", false);
        }
        map.put("era", era);
        map.put("masterID", masterID);
        map.put("viewable", true);
        if (era.getViewProperties() != null) {
            ArrayList<String> actions = (ArrayList<String>) era.getViewProperties().get("actions");
            if (actions != null) {
                for (String act : actions) {
                    String actionString = "act" + act;
                    map.put(actionString.replaceAll(" ", ""), false);
                }
            }
        }

        return "era";
    }

}
