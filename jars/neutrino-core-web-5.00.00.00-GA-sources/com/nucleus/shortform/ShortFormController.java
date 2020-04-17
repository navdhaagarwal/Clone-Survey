package com.nucleus.shortform;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.nucleus.entity.ApprovalStatus;
import com.nucleus.makerchecker.MakerCheckerService;
import com.nucleus.master.BaseMasterService;
import com.nucleus.shortFormMaster.ShortForm;
import com.nucleus.user.User;
import com.nucleus.user.UserInfo;
import com.nucleus.web.common.controller.BaseController;

@Controller
@RequestMapping("/ShortForm")
public class ShortFormController extends BaseController {
    @Inject
    @Named("makerCheckerService")
    private MakerCheckerService makerCheckerService;

    @Inject
    @Named("baseMasterService")
    private BaseMasterService   baseMasterService;

    private static final String  masterId = "ShortForm";

    @PreAuthorize("hasAuthority('MAKER_SHORTFORM')")
    @RequestMapping(value = "/create")
    public String createShortForm(ModelMap map) {

        map.put("shortForm", new ShortForm());
        map.put("masterID", masterId);
        return "shortForm";
    }

    @PreAuthorize("hasAuthority('MAKER_SHORTFORM')")
    @RequestMapping(value = "/save", method = RequestMethod.POST)
    public String saveShortForm(@Validated ShortForm shortForm, BindingResult result, ModelMap map,
            @RequestParam("createAnotherMaster") boolean createAnotherMaster) {

        Map<String, Object> validateMap = new HashMap<String, Object>();
        validateMap.put("code", shortForm.getCode());

        List<String> colNameList = checkValidationForDuplicates(shortForm, ShortForm.class, validateMap);
        if (result.hasErrors() || (colNameList != null && colNameList.size() > 0)) {
            map.put("shortForm", shortForm);
            map.put("masterID", masterId);
            if (shortForm != null && colNameList != null && colNameList.size() > 0) {
                result.rejectValue("code", "label.code.validation.exists");
            }
            return "shortForm";
        }

        if (shortForm.getCode() == null) {
            shortForm.setCode(null);
        }

        User user = getUserDetails().getUserReference();
        if (user != null) {
            makerCheckerService.masterEntityChangedByUser(shortForm, user);
        }
        if (createAnotherMaster) {
            map.put("shortForm", new ShortForm());
            map.put("masterID", masterId);
            return "shortForm";
        }
        map.put("masterID", masterId);
        return "redirect:/app/grid/ShortForm/ShortForm/loadColumnConfig";

    }

    @PreAuthorize("hasAuthority('MAKER_SHORTFORM')")
    @RequestMapping(value = "/saveAndSendForApproval", method = RequestMethod.POST)
    public String saveAndSendForApproval(@Validated ShortForm shortForm, BindingResult result, ModelMap map,
            @RequestParam("createAnotherMaster") boolean createAnotherMaster) {

        Map<String, Object> validateMap = new HashMap<String, Object>();
        validateMap.put("code", shortForm.getCode());

        List<String> colNameList = checkValidationForDuplicates(shortForm, ShortForm.class, validateMap);
        if (result.hasErrors() || (colNameList != null && colNameList.size() > 0)) {
            map.put("shortForm", shortForm);
            map.put("masterID", masterId);
            if (shortForm != null && colNameList != null && colNameList.size() > 0) {
                result.rejectValue("code", "label.code.validation.exists");
            }
            return "shortForm";
        }

        if (shortForm.getCode() == null) {
            shortForm.setCode(null);
        }

        User user = getUserDetails().getUserReference();
        if (user != null) {
            makerCheckerService.saveAndSendForApproval(shortForm, user);
        }
        if (createAnotherMaster) {
            map.put("shortForm", new ShortForm());
            map.put("masterID", masterId);
            return "shortForm";
        }
        map.put("masterID", masterId);
        return "redirect:/app/grid/ShortForm/ShortForm/loadColumnConfig";

    }

    @PreAuthorize("hasAuthority('VIEW_SHORTFORM') or hasAuthority('MAKER_SHORTFORM') or hasAuthority('CHECKER_SHORTFORM')")
    @RequestMapping(value = "/view/{id}", method = RequestMethod.GET)
    public String viewShortForm(@PathVariable("id") Long id, ModelMap map) {
        UserInfo currentUser = getUserDetails();
        ShortForm shortForm = baseMasterService.getMasterEntityWithActionsById(ShortForm.class, id, currentUser
                .getUserEntityId().getUri());
        if (shortForm.getApprovalStatus() == ApprovalStatus.UNAPPROVED_MODIFIED
                || shortForm.getApprovalStatus() == ApprovalStatus.WORFLOW_IN_PROGRESS) {
            ShortForm prevShortForm = (ShortForm) baseMasterService.getLastApprovedEntityByUnapprovedEntityId(shortForm
                    .getEntityId());
            map.put("prevShortForm", prevShortForm);
           map.put("editLink", false);
        } else if (shortForm.getApprovalStatus() == ApprovalStatus.APPROVED_MODIFIED) {
            ShortForm prevShortForm = (ShortForm) baseMasterService.getLastUnApprovedEntityByApprovedEntityId(shortForm
                    .getEntityId());
            map.put("prevShortForm", prevShortForm);
            map.put("viewLink", false);
        }
        map.put("shortForm", shortForm);
        map.put("masterID", masterId);
        map.put("viewable", true);
        if (shortForm.getViewProperties() != null) {
            @SuppressWarnings("unchecked")
            ArrayList<String> actions = (ArrayList<String>) shortForm.getViewProperties().get("actions");
            if (actions != null) {
                for (String act : actions) {
                    String actionString = "act" + act;
                    map.put(actionString.replaceAll(" ", ""), false);
                }
            }
        }

        return "shortForm";

    }

    @PreAuthorize("hasAuthority('MAKER_SHORTFORM')")
    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/edit/{id}")
    public String editCountry(@PathVariable("id") Long id, ModelMap map) {
        UserInfo currentUser = getUserDetails();
        ShortForm shortForm = baseMasterService.getMasterEntityWithActionsById(ShortForm.class, id, currentUser
                .getUserEntityId().getUri());
        if (shortForm.getApprovalStatus() == ApprovalStatus.UNAPPROVED_MODIFIED) {
            ShortForm prevShortForm = (ShortForm) baseMasterService.getLastApprovedEntityByUnapprovedEntityId(shortForm
                    .getEntityId());
            map.put("prevShortForm", prevShortForm);
            map.put("editLink", false);
        }
        map.put("shortForm", shortForm);
        map.put("edit", true);
        map.put("masterID", masterId);
        ArrayList<String> actions = (ArrayList<String>) shortForm.getViewProperties().get("actions");
        if (actions != null) {
            for (String act : actions) {
                map.put("act" + act, false);
            }
        }

        return "shortForm";
    }

}
