/**
 * Copyright Nucleus Software India Pvt Ltd. All rights reserved.
 */

package com.nucleus.web.region.master;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import com.nucleus.activeInactiveReason.MasterActiveInactiveReasons;
import com.nucleus.activeInactiveReason.ReasonsActiveInactiveMapping;
import com.nucleus.persistence.HibernateUtils;
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

import com.nucleus.address.IntraCountryRegion;
import com.nucleus.entity.ApprovalStatus;
import com.nucleus.makerchecker.MakerCheckerService;
import com.nucleus.master.BaseMasterService;
import com.nucleus.user.User;
import com.nucleus.user.UserInfo;
import com.nucleus.web.common.controller.BaseController;

/**
 * @author Nucleus Software India Pvt Ltd This field is being used for
 *         controlling region CRUD and task allocation work-flow related
 *         operations.
 */
@Transactional
@Controller
@RequestMapping(value = "/IntraCountryRegion")
public class IntraCountryRegionController extends BaseController {
	@Inject
	@Named("stringEncryptor")
	private StandardPBEStringEncryptor encryptor;
    @Inject
    @Named("makerCheckerService")
    private MakerCheckerService makerCheckerService;

    @Inject
    @Named("baseMasterService")
    private BaseMasterService   baseMasterService;

    @InitBinder("intraCountryRegion")
    protected void initBinder(WebDataBinder binder) {
        binder.setValidator(new IntraCountryRegionValidator());
    }

    /*Method Added to send current Entity Uri for working of comments,activity,history,notes*/
    @ModelAttribute("currentEntityClassName")
    public String getEntityClassName() throws UnsupportedEncodingException {
    	 return UriUtils.encodeQueryParam("enc_" + encryptor.encrypt(IntraCountryRegion.class.getName()), "UTF-8");
    }

    private static final String masterId = "IntraCountryRegion";

    /**
     * @param region
     *            object containing region name,region code.
     * @return String
     * @throws IOException
     * @description to save region object from view
     */
    @PreAuthorize("hasAuthority('MAKER_GEOREGION')")
    @RequestMapping(value = "/save", method = RequestMethod.POST)
    public String saveRegion(@Validated IntraCountryRegion region, BindingResult result, ModelMap map,
            @RequestParam("createAnotherMaster") boolean createAnotherMaster) {

      //  BaseLoggers.flowLogger.debug(region.getLogInfo());
        /*
         * Map whose Key Is Table Column Name with whom to validate and Value is
         * The One to be validated.This Map Is Send in the Validator Method
         */

        IntraCountryRegion dubplicateIntraCountryRegion = null;
        if(null!=region.getId()){
            dubplicateIntraCountryRegion = entityDao.find(IntraCountryRegion.class,region.getId());
            if(null != dubplicateIntraCountryRegion.getEntityLifeCycleData()){
                region.setEntityLifeCycleData(dubplicateIntraCountryRegion.getEntityLifeCycleData());
            }
            if(null != dubplicateIntraCountryRegion.getMasterLifeCycleData()){
                region.setMasterLifeCycleData(dubplicateIntraCountryRegion.getMasterLifeCycleData());
            }
        }
       
        Map<String, Object> validateMap = new HashMap<String, Object>();
        validateMap.put("intraRegionCode", region.getIntraRegionCode());

        /*
         * Code to check as if any existing(or new) record is being modified(or created) into another existing record 
         */
        List<String> colNameList = checkValidationForDuplicates(region, IntraCountryRegion.class, validateMap);
        if (result.hasErrors() || (colNameList != null && colNameList.size() > 0)) {
            String masterName = region.getClass().getSimpleName();
            String uniqueValue = null;
            String uniqueParameter = null;
            if (null != region.getId()) {
                //IntraCountryRegion reg = baseMasterService.findById(IntraCountryRegion.class, region.getId());
                uniqueValue = dubplicateIntraCountryRegion.getIntraRegionCode();
                uniqueParameter = "intraRegionCode";
                getActInactReasMapForEditApproved(map, region, masterName, uniqueParameter, uniqueValue);
            }
            else {
                ReasonsActiveInactiveMapping reasActInactMap = getActInactReasMapForCreate(map);
                region.setReasonActInactMap(reasActInactMap);
            }
            ReasonsActiveInactiveMapping reasonsActiveInactiveMapping = getActInactReasonsForEditGeneric(map,region.getReasonActInactMap());
            region.setReasonActInactMap(reasonsActiveInactiveMapping);
            map.put("edit" , true);
            map.put("viewable" , false);
            map.put("intraCountryRegion", region);
            map.put("masterID", masterId);
            /*
             * Duplicate Customer Category Code Validation : If Code Exists then
             * Return With Message
             */
            if (colNameList != null && colNameList.size() > 0) {
                result.rejectValue("intraRegionCode", "label.regionCode.validation.exists");
            }
               return "intraCountryRegion";
        }
        map.put("masterID", masterId);

        boolean eventResult = executeMasterEvent(region,"contextObjectIntraCountryRegion",map);
        if(!eventResult){
            // getActInactReasMapForEdit(map,region);
            String masterName = region.getClass().getSimpleName();
            String uniqueParameter = "intraRegionCode";
            String uniqueValue = region.getIntraRegionCode();
            getActInactReasMapForEditApproved(map,region,masterName,uniqueParameter,uniqueValue);
            map.put("edit" , true);
            ReasonsActiveInactiveMapping reasonsActiveInactiveMapping = getActInactReasonsForEditGeneric(map,region.getReasonActInactMap());
            region.setReasonActInactMap(reasonsActiveInactiveMapping);
            map.put("intraCountryRegion", region);
            map.put("viewable" , false);
            map.put("masterID", masterId);
            map.put("activeFlag",region.isActiveFlag());
            return "intraCountryRegion";
        }

        // we need to get below logged in user from session
        User user = getUserDetails().getUserReference();
        if (user != null) {
            ReasonsActiveInactiveMapping reasonsActiveInactiveMapping = region.getReasonActInactMap();
            if(reasonsActiveInactiveMapping != null){
                saveActInactReasonForMaster(reasonsActiveInactiveMapping,region);
            }
            region.setReasonActInactMap(reasonsActiveInactiveMapping);
            makerCheckerService.masterEntityChangedByUser(region, user);
        }
        if (createAnotherMaster) {
            ReasonsActiveInactiveMapping reasActInactMap = getActInactReasMapForCreate(map);
            IntraCountryRegion IntraCountryRegionAnother= new IntraCountryRegion();
            IntraCountryRegionAnother.setReasonActInactMap(reasActInactMap);
            map.put("intraCountryRegion", IntraCountryRegionAnother);
            map.put("masterID", masterId);
            return "intraCountryRegion";
        }
        map.put("masterID", masterId);
        return "redirect:/app/grid/IntraCountryRegion/IntraCountryRegion/loadColumnConfig";

    }

    /**
     * @param record
     *            id for edit
     * @return void
     * @throws
     * @description to create region
     */

    @PreAuthorize("hasAuthority('MAKER_GEOREGION')")
    @RequestMapping(value = "/create")
    public String createRegion(IntraCountryRegion region, ModelMap map) {
        ReasonsActiveInactiveMapping reasActInactMap = getActInactReasMapForCreate(map);
        IntraCountryRegion intraCountryRegion= new IntraCountryRegion();
        intraCountryRegion.setReasonActInactMap(reasActInactMap);
        map.put("reasonsActiveInactiveMapping",intraCountryRegion.getReasonActInactMap());
        map.put("geoRegion", intraCountryRegion);
        map.put("masterID", masterId);
        return "intraCountryRegion";
    }

    /**
     * @param record
     *            id for edit
     * @return void
     * @throws
     * @description to edit region
     */
    @PreAuthorize("hasAuthority('MAKER_GEOREGION')")
    @RequestMapping(value = "/edit/{id}")
    public String editRegion(@PathVariable("id") Long id, IntraCountryRegion region, ModelMap map) {

        region = baseMasterService.getMasterEntityById(IntraCountryRegion.class, id);
        IntraCountryRegion prevRegion = null;
        String uniqueValue = null;
        if (region.getApprovalStatus() == ApprovalStatus.UNAPPROVED_MODIFIED) {
        	prevRegion = (IntraCountryRegion) baseMasterService.getLastApprovedEntityByUnapprovedEntityId(region
                    .getEntityId());
            map.put("prevRegion", prevRegion);
            map.put("editLink", false);
            uniqueValue = prevRegion.getIntraRegionCode();
        }
       else{
            uniqueValue = region.getIntraRegionCode();
        }
        String masterName = region.getClass().getSimpleName();
        String uniqueParameter = "intraRegionCode";

        getActInactReasMapForEditApproved(map,region,masterName,uniqueParameter,uniqueValue);
        ReasonsActiveInactiveMapping reasonsActiveInactiveMapping = getActInactReasonsForEditGeneric(map,region.getReasonActInactMap());
        region.setReasonActInactMap(reasonsActiveInactiveMapping);
        map.put("viewable" ,false);
        map.put("intraCountryRegion", region);
        map.put("masterID", masterId);
        map.put("edit", true);
        return "intraCountryRegion";
    }

    /**
     * @param record
     *            id for view
     * @return void
     * @throws
     * @description to view region
     */
    @SuppressWarnings("unchecked")
    @PreAuthorize("hasAuthority('VIEW_GEOREGION') or hasAuthority('MAKER_GEOREGION') or hasAuthority('CHECKER_GEOREGION')")
    @RequestMapping(value = "/view/{id}", method = RequestMethod.GET)
    public String viewRegion(@PathVariable("id") Long id, ModelMap map) {
        UserInfo currentUser = getUserDetails();
        IntraCountryRegion region = baseMasterService.getMasterEntityWithActionsById(IntraCountryRegion.class, id, currentUser
                .getUserEntityId().getUri());
        if (region.getApprovalStatus() == ApprovalStatus.UNAPPROVED_MODIFIED
                || region.getApprovalStatus() == ApprovalStatus.WORFLOW_IN_PROGRESS) {
            IntraCountryRegion prevRegion = (IntraCountryRegion) baseMasterService.getLastApprovedEntityByUnapprovedEntityId(region
                    .getEntityId());
            map.put("prevRegion", prevRegion);
           map.put("editLink", false);
        } else if (region.getApprovalStatus() == ApprovalStatus.APPROVED_MODIFIED) {
            IntraCountryRegion prevRegion = (IntraCountryRegion) baseMasterService.getLastUnApprovedEntityByApprovedEntityId(region
                    .getEntityId());
            map.put("prevRegion", prevRegion);
           map.put("viewLink", false);
        }
        ReasonsActiveInactiveMapping reasonsActiveInactiveMapping = getActInactReasonsForEditGeneric(map,region.getReasonActInactMap());
        region.setReasonActInactMap(reasonsActiveInactiveMapping);
        // getActInactReasMapForEdit(map,region);
        String masterName = region.getClass().getSimpleName();
        String uniqueParameter = "intraRegionCode";
        String uniqueValue = region.getIntraRegionCode();
        getActInactReasMapForEditApproved(map,region,masterName,uniqueParameter,uniqueValue);
        map.put("intraCountryRegion", region);
        map.put("masterID", masterId);
        map.put("viewable", true);
        if (region.getViewProperties() != null) {

            ArrayList<String> actions = (ArrayList<String>) region.getViewProperties().get("actions");
            if (actions != null) {
                for (String act : actions) {
                    String actionString = "act" + act;
                    map.put(actionString.replaceAll(" ", ""), false);
                }

            }

        }
        return "intraCountryRegion";
    }

    /**
     * @description to save and send for approval * @return String
     * @throws IOException
     * @description to save and send for approval region object from view
     */
    @PreAuthorize("hasAuthority('MAKER_GEOREGION')")
    @RequestMapping(value = "/saveAndSendForApproval", method = RequestMethod.POST)
    public String saveAndSendForApproval(@Validated IntraCountryRegion region, BindingResult result, ModelMap map,
            @RequestParam("createAnotherMaster") boolean createAnotherMaster) {

       // BaseLoggers.flowLogger.debug(region.getLogInfo());
        /*
         * Map whose Key Is Table Column Name with whom to validate and Value is
         * The One to be validated.This Map Is Send in the Validator Method
         */
        IntraCountryRegion dubplicateIntraCountryRegion = null;
        if(null!=region.getId()){
            dubplicateIntraCountryRegion = entityDao.find(IntraCountryRegion.class,region.getId());
            if(null != dubplicateIntraCountryRegion.getEntityLifeCycleData()){
                region.setEntityLifeCycleData(dubplicateIntraCountryRegion.getEntityLifeCycleData());
            }
            if(null != dubplicateIntraCountryRegion.getMasterLifeCycleData()){
                region.setMasterLifeCycleData(dubplicateIntraCountryRegion.getMasterLifeCycleData());
            }
        }

        Map<String, Object> validateMap = new HashMap<String, Object>();
        validateMap.put("intraRegionCode", region.getIntraRegionCode());
        
        /*
         * Code to check as if any existing(or new) record is being modified(or created) into another existing record 
         */
        List<String> colNameList = checkValidationForDuplicates(region, IntraCountryRegion.class, validateMap);
        if (result.hasErrors() || (colNameList != null && colNameList.size() > 0)) {
            String masterName = region.getClass().getSimpleName();
            String uniqueValue = null;
            String uniqueParameter = null;
            if (null != region.getId()) {
                //IntraCountryRegion reg = baseMasterService.findById(IntraCountryRegion.class, region.getId());
                uniqueValue = dubplicateIntraCountryRegion.getIntraRegionCode();
                uniqueParameter = "intraRegionCode";
                getActInactReasMapForEditApproved(map, region, masterName, uniqueParameter, uniqueValue);
            }
            else {
                ReasonsActiveInactiveMapping reasActInactMap = getActInactReasMapForCreate(map);
                region.setReasonActInactMap(reasActInactMap);
            }
            ReasonsActiveInactiveMapping reasonsActiveInactiveMapping = getActInactReasonsForEditGeneric(map,region.getReasonActInactMap());
            region.setReasonActInactMap(reasonsActiveInactiveMapping);
            map.put("edit" , true);
            map.put("viewable" , false);
            map.put("intraCountryRegion", region);
            map.put("masterID", masterId);
            /*
             * Duplicate Customer Category Code Validation : If Code Exists then
             * Return With Message
             */
            if (colNameList != null && colNameList.size() > 0) {
                result.rejectValue("intraRegionCode", "label.regionCode.validation.exists");
            }

            return "intraCountryRegion";
        }
        map.put("masterID", masterId);

        boolean eventResult = executeMasterEvent(region,"contextObjectIntraCountryRegion",map);
        if(!eventResult){
            // getActInactReasMapForEdit(map,region);
            String masterName = region.getClass().getSimpleName();
            String uniqueParameter = "intraRegionCode";
            String uniqueValue = region.getIntraRegionCode();
            getActInactReasMapForEditApproved(map,region,masterName,uniqueParameter,uniqueValue);
            map.put("edit" , true);
            ReasonsActiveInactiveMapping reasonsActiveInactiveMapping = getActInactReasonsForEditGeneric(map,region.getReasonActInactMap());
            region.setReasonActInactMap(reasonsActiveInactiveMapping);
            map.put("intraCountryRegion", region);
            map.put("viewable" , false);
            map.put("masterID", masterId);
            map.put("activeFlag",region.isActiveFlag());
            return "intraCountryRegion";
        }

        // we need to get below logged in user from session
        User user = getUserDetails().getUserReference();
        if (user != null) {
            ReasonsActiveInactiveMapping reasonsActiveInactiveMapping = region.getReasonActInactMap();
            if(reasonsActiveInactiveMapping != null){
                saveActInactReasonForMaster(reasonsActiveInactiveMapping,region);
            }
            region.setReasonActInactMap(reasonsActiveInactiveMapping);
            makerCheckerService.saveAndSendForApproval(region, user);
        }
        if (createAnotherMaster) {
            ReasonsActiveInactiveMapping reasActInactMap = getActInactReasMapForCreate(map);
            IntraCountryRegion IntraCountryRegionAnother= new IntraCountryRegion();
            IntraCountryRegionAnother.setReasonActInactMap(reasActInactMap);
            map.put("intraCountryRegion", IntraCountryRegionAnother);
            map.put("masterID", masterId);

            return "intraCountryRegion";
        }
        map.put("masterID", masterId);
        return "redirect:/app/grid/IntraCountryRegion/IntraCountryRegion/loadColumnConfig";

    }


}
