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

import com.nucleus.address.GeoRegion;
import com.nucleus.entity.ApprovalStatus;
import com.nucleus.logging.BaseLoggers;
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
@RequestMapping(value = "/GeoRegion")
public class RegionController extends BaseController {
	@Inject
	@Named("stringEncryptor")
	private StandardPBEStringEncryptor encryptor;
    @Inject
    @Named("makerCheckerService")
    private MakerCheckerService makerCheckerService;

    @Inject
    @Named("baseMasterService")
    private BaseMasterService   baseMasterService;

    @InitBinder("geoRegion")
    protected void initBinder(WebDataBinder binder) {
        binder.setValidator(new RegionValidator());
    }

    /*Method Added to send current Entity Uri for working of comments,activity,history,notes*/
    @ModelAttribute("currentEntityClassName")
    public String getEntityClassName() throws UnsupportedEncodingException {
    	 return UriUtils.encodeQueryParam("enc_" + encryptor.encrypt(GeoRegion.class.getName()), "UTF-8");
    }

    private static final String masterId = "GeoRegion";

    /**
     * @param region
     *            object containing region name,region code.
     * @return String
     * @throws IOException
     * @description to save region object from view
     */
    @PreAuthorize("hasAuthority('MAKER_GEOREGION')")
    @RequestMapping(value = "/save", method = RequestMethod.POST)
    public String saveRegion(@Validated GeoRegion region, BindingResult result, ModelMap map,
            @RequestParam("createAnotherMaster") boolean createAnotherMaster) {

        BaseLoggers.flowLogger.debug(region.getLogInfo());
        /*
         * Map whose Key Is Table Column Name with whom to validate and Value is
         * The One to be validated.This Map Is Send in the Validator Method
         */

        GeoRegion dubplicateGeoRegion = null;
        if(null!=region.getId()){
            dubplicateGeoRegion = entityDao.find(GeoRegion.class,region.getId());
            if(null != dubplicateGeoRegion.getEntityLifeCycleData()){
                region.setEntityLifeCycleData(dubplicateGeoRegion.getEntityLifeCycleData());
            }
            if(null != dubplicateGeoRegion.getMasterLifeCycleData()){
                region.setMasterLifeCycleData(dubplicateGeoRegion.getMasterLifeCycleData());
            }
        }
       
        Map<String, Object> validateMap = new HashMap<String, Object>();
        validateMap.put("regionCode", region.getRegionCode());

        /*
         * Code to check as if any existing(or new) record is being modified(or created) into another existing record 
         */
        List<String> colNameList = checkValidationForDuplicates(region, GeoRegion.class, validateMap);
        if (result.hasErrors() || (colNameList != null && colNameList.size() > 0)) {
            if(region.getId() != null) {
                GeoRegion gr = baseMasterService.getMasterEntityById(GeoRegion.class, region.getId());
                if (!(ApprovalStatus.UNAPPROVED_ADDED == gr.getApprovalStatus() || ApprovalStatus.CLONED == gr.getApprovalStatus())) {
                    map.put("codeViewMode", true);
                }
            }
            String masterName = region.getClass().getSimpleName();
            String uniqueValue = null;
            String uniqueParameter = null;
            if (null != region.getId()) {
                //GeoRegion geoReg = baseMasterService.findById(GeoRegion.class, region.getId());
                uniqueValue = dubplicateGeoRegion.getRegionCode();
                uniqueParameter = "regionCode";
                getActInactReasMapForEditApproved(map, region, masterName, uniqueParameter, uniqueValue);
            }
            else {
                ReasonsActiveInactiveMapping reasActInactMap = getActInactReasMapForCreate(map);
                region.setReasonActInactMap(reasActInactMap);
            }
            ReasonsActiveInactiveMapping reasonsActiveInactiveMapping = getActInactReasonsForEditGeneric(map,region.getReasonActInactMap());
            region.setReasonActInactMap(reasonsActiveInactiveMapping);
            map.put("edit" , true);
            map.put("geoRegion", region);
            map.put("masterID", masterId);
            /*
             * Duplicate Customer Category Code Validation : If Code Exists then
             * Return With Message
             */
            if (colNameList != null && colNameList.size() > 0) {
                result.rejectValue("regionCode", "label.regionCode.validation.exists");
            }
            return "region";
        }

        boolean eventResult = executeMasterEvent(region,"contextObjectGeoRegion",map);
        if(!eventResult){
            String masterName = region.getClass().getSimpleName();
            String uniqueParameter = "regionCode";
            String uniqueValue = region.getRegionCode();
            getActInactReasMapForEditApproved(map,region,masterName,uniqueParameter,uniqueValue);
            map.put("edit" , true);
            ReasonsActiveInactiveMapping reasonsActiveInactiveMapping = getActInactReasonsForEditGeneric(map,region.getReasonActInactMap());
            region.setReasonActInactMap(reasonsActiveInactiveMapping);
            map.put("viewable" , false);
            map.put("geoRegion", region);
            map.put("masterID", masterId);
            map.put("activeFlag",region.isActiveFlag());
            return "region";
        }

        map.put("masterID", masterId);
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
            GeoRegion geoRegionForCreateAnother= new GeoRegion();
            geoRegionForCreateAnother.setReasonActInactMap(reasActInactMap);
            map.put("geoRegion",geoRegionForCreateAnother);
            map.put("masterID", masterId);
            return "region";
        }
        map.put("masterID", masterId);
        return "redirect:/app/grid/GeoRegion/GeoRegion/loadColumnConfig";

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
    public String createRegion(GeoRegion region, ModelMap map) {
        ReasonsActiveInactiveMapping reasActInactMap = getActInactReasMapForCreate(map);
        GeoRegion geoRegion= new GeoRegion();
        geoRegion.setReasonActInactMap(reasActInactMap);
        map.put("reasonsActiveInactiveMapping",geoRegion.getReasonActInactMap());
        map.put("geoRegion", new GeoRegion());
        map.put("masterID", masterId);
        return "region";
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
    public String editRegion(@PathVariable("id") Long id, GeoRegion region, ModelMap map) {

        region = baseMasterService.getMasterEntityById(GeoRegion.class, id);
        if (region.getApprovalStatus() == ApprovalStatus.UNAPPROVED_MODIFIED) {
            GeoRegion prevRegion = (GeoRegion) baseMasterService.getLastApprovedEntityByUnapprovedEntityId(region
                    .getEntityId());
            map.put("prevRegion", prevRegion);
            map.put("editLink", false);
        }
        if(!(ApprovalStatus.UNAPPROVED_ADDED == region.getApprovalStatus() || ApprovalStatus.CLONED == region.getApprovalStatus())) {
            map.put("codeViewMode", true);
        }
        ReasonsActiveInactiveMapping reasonsActiveInactiveMapping = getActInactReasonsForEditGeneric(map,region.getReasonActInactMap());
        region.setReasonActInactMap(reasonsActiveInactiveMapping);
        // getActInactReasMapForEdit(map,region);
        String masterName = region.getClass().getSimpleName();
        String uniqueParameter = "regionCode";
        String uniqueValue = region.getRegionCode();
        getActInactReasMapForEditApproved(map,region,masterName,uniqueParameter,uniqueValue);
        map.put("geoRegion", region);
        map.put("masterID", masterId);
        map.put("edit", true);

        map.put("viewable" ,false);
        return "region";
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
        GeoRegion region = baseMasterService.getMasterEntityWithActionsById(GeoRegion.class, id, currentUser
                .getUserEntityId().getUri());
        if (region.getApprovalStatus() == ApprovalStatus.UNAPPROVED_MODIFIED
                || region.getApprovalStatus() == ApprovalStatus.WORFLOW_IN_PROGRESS) {
            GeoRegion prevRegion = (GeoRegion) baseMasterService.getLastApprovedEntityByUnapprovedEntityId(region
                    .getEntityId());
            map.put("prevRegion", prevRegion);
           map.put("editLink", false);
        } else if (region.getApprovalStatus() == ApprovalStatus.APPROVED_MODIFIED) {
            GeoRegion prevRegion = (GeoRegion) baseMasterService.getLastUnApprovedEntityByApprovedEntityId(region
                    .getEntityId());
            map.put("prevRegion", prevRegion);
            map.put("viewLink", false);
        }
        ReasonsActiveInactiveMapping reasonsActiveInactiveMapping = getActInactReasonsForEditGeneric(map,region.getReasonActInactMap());
        region.setReasonActInactMap(reasonsActiveInactiveMapping);
        // getActInactReasMapForEdit(map,region);
        String masterName = region.getClass().getSimpleName();
        String uniqueParameter = "regionCode";
        String uniqueValue = region.getRegionCode();
        getActInactReasMapForEditApproved(map,region,masterName,uniqueParameter,uniqueValue);
        map.put("geoRegion", region);
        map.put("masterID", masterId);
        map.put("viewable", true);
        map.put("codeViewMode", true);
        if (region.getViewProperties() != null) {

            ArrayList<String> actions = (ArrayList<String>) region.getViewProperties().get("actions");
            if (actions != null) {
                for (String act : actions) {
                    String actionString = "act" + act;
                    map.put(actionString.replaceAll(" ", ""), false);
                }

            }

        }

        return "region";
    }

    /**
     * @description to save and send for approval * @return String
     * @throws IOException
     * @description to save and send for approval region object from view
     */
    @PreAuthorize("hasAuthority('MAKER_GEOREGION')")
    @RequestMapping(value = "/saveAndSendForApproval", method = RequestMethod.POST)
    public String saveAndSendForApproval(@Validated GeoRegion region, BindingResult result, ModelMap map,
            @RequestParam("createAnotherMaster") boolean createAnotherMaster) {

        BaseLoggers.flowLogger.debug(region.getLogInfo());
        /*
         * Map whose Key Is Table Column Name with whom to validate and Value is
         * The One to be validated.This Map Is Send in the Validator Method
         */
        GeoRegion dubplicateGeoRegion = null;
        if(null!=region.getId()){
            dubplicateGeoRegion = entityDao.find(GeoRegion.class,region.getId());
            if(null != dubplicateGeoRegion.getEntityLifeCycleData()){
                region.setEntityLifeCycleData(dubplicateGeoRegion.getEntityLifeCycleData());
            }
            if(null != dubplicateGeoRegion.getMasterLifeCycleData()){
                region.setMasterLifeCycleData(dubplicateGeoRegion.getMasterLifeCycleData());
            }
        }

        Map<String, Object> validateMap = new HashMap<String, Object>();
        validateMap.put("regionCode", region.getRegionCode());
        
        /*
         * Code to check as if any existing(or new) record is being modified(or created) into another existing record 
         */
        List<String> colNameList = checkValidationForDuplicates(region, GeoRegion.class, validateMap);
        if (result.hasErrors() || (colNameList != null && colNameList.size() > 0)) {
            if(region.getId() != null) {
                GeoRegion gr = baseMasterService.getMasterEntityById(GeoRegion.class, region.getId());
                if (!(ApprovalStatus.UNAPPROVED_ADDED == gr.getApprovalStatus() || ApprovalStatus.CLONED == gr.getApprovalStatus())) {
                    map.put("codeViewMode", true);
                }
            }
            String masterName = region.getClass().getSimpleName();
            String uniqueValue = null;
            String uniqueParameter = null;
            if (null != region.getId()) {
                //GeoRegion geoReg = baseMasterService.findById(GeoRegion.class, region.getId());
                uniqueValue = dubplicateGeoRegion.getRegionCode();
                uniqueParameter = "regionCode";
                getActInactReasMapForEditApproved(map, region, masterName, uniqueParameter, uniqueValue);
            }
            else {
                ReasonsActiveInactiveMapping reasActInactMap = getActInactReasMapForCreate(map);
                region.setReasonActInactMap(reasActInactMap);
            }
            ReasonsActiveInactiveMapping reasonsActiveInactiveMapping = getActInactReasonsForEditGeneric(map,region.getReasonActInactMap());
            region.setReasonActInactMap(reasonsActiveInactiveMapping);
            map.put("viewable" , false);
            map.put("edit" , true);
            map.put("geoRegion", region);
            map.put("masterID", masterId);
            /*
             * Duplicate Customer Category Code Validation : If Code Exists then
             * Return With Message
             */
            if (colNameList != null && colNameList.size() > 0) {
                result.rejectValue("regionCode", "label.regionCode.validation.exists");
            }
            return "region";
        }

        boolean eventResult = executeMasterEvent(region,"contextObjectGeoRegion",map);
        if(!eventResult){
            String masterName = region.getClass().getSimpleName();
            String uniqueParameter = "regionCode";
            String uniqueValue = region.getRegionCode();
            getActInactReasMapForEditApproved(map,region,masterName,uniqueParameter,uniqueValue);
            map.put("edit" , true);
            ReasonsActiveInactiveMapping reasonsActiveInactiveMapping = getActInactReasonsForEditGeneric(map,region.getReasonActInactMap());
            region.setReasonActInactMap(reasonsActiveInactiveMapping);
            map.put("viewable" , false);
            map.put("geoRegion", region);
            map.put("masterID", masterId);
            map.put("activeFlag",region.isActiveFlag());
            return "region";
        }

        map.put("masterID", masterId);
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
            GeoRegion geoRegionForCreateAnother= new GeoRegion();
            geoRegionForCreateAnother.setReasonActInactMap(reasActInactMap);
            map.put("geoRegion",geoRegionForCreateAnother);
            map.put("masterID", masterId);
            return "region";
        }
        map.put("masterID", masterId);
        return "redirect:/app/grid/GeoRegion/GeoRegion/loadColumnConfig";

    }

}
