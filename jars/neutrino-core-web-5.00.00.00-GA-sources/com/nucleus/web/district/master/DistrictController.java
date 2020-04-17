/**
 * This file and a proportion of its content is copyright of Nucleus Software Exports Limited - © 2012. All rights reserved.
 * Any redistribution or reproduction of part or all of the contents in any form is prohibited other than the following:
 * - you cannot print or download to a local hard disk extract contents either part or full for personal/ commercial/
 * academic or any other use
 * - you may not copy the content to individual/ third parties for any type of use, either as compiled or source format
 * without the knowledge and consent of Nucleus SOftware
 * - You may not, except with our express written permission, distribute or commercially exploit the content. Nor may you
 * transmit it or store it in any other web site or other form of electronic retrieval system.
 */
package com.nucleus.web.district.master;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import com.nucleus.activeInactiveReason.ReasonsActiveInactiveMapping;
import org.apache.commons.collections4.CollectionUtils;
import org.hibernate.Hibernate;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Validator;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.UriUtils;

import com.nucleus.address.District;
import com.nucleus.address.State;
import com.nucleus.entity.ApprovalStatus;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.makerchecker.MakerCheckerService;
import com.nucleus.master.BaseMasterService;
import com.nucleus.persistence.HibernateUtils;
import com.nucleus.user.User;
import com.nucleus.user.UserInfo;
import com.nucleus.web.common.controller.BaseController;
import com.nucleus.jsMessageResource.service.JsMessageResourceService;

/**
 * @author Nucleus Software Exports Limited TODO -> taru.agarwal Add
 *         documentation to class
 */
@Transactional
@Controller
@RequestMapping(value = "/District")
public class DistrictController extends BaseController {

    @Inject
    @Named("makerCheckerService")
    private MakerCheckerService makerCheckerService;

    @Inject
    @Named("baseMasterService")
    private BaseMasterService   baseMasterService;

    private static final String                      masterId = "District";

    @Inject
	@Named("stringEncryptor")
	private StandardPBEStringEncryptor encryptor;
    
    @Inject
    @Named("jsMessageResourceService")
    public JsMessageResourceService jsMessageResourceService;

    @Inject
    @Named("districtValidator")
    private Validator   districtValidator;
    
    @InitBinder("district")
    protected void initBinder(WebDataBinder binder) {
        binder.setValidator(districtValidator);
    }

    /*Method Added to send current Entity Uri for working of comments,activity,history,notes*/
    @ModelAttribute("currentEntityClassName")
    public String getEntityClassName() throws UnsupportedEncodingException {
    	 return UriUtils.encodeQueryParam("enc_" + encryptor.encrypt(District.class.getName()), "UTF-8");
    }

    /**
     * @param District
     *            object containing District name, District Code,District
     *            Abbreviation etc.
     * @return String
     * @throws IOException
     * @description to save District object from view
     */
    @PreAuthorize("hasAuthority('MAKER_DISTRICT')")
    @RequestMapping(value = "/save", method = RequestMethod.POST)
    public String saveDistrict(@Validated District district, BindingResult result, ModelMap map,
            @RequestParam("createAnotherMaster") boolean createAnotherMaster) {

        if(null!=district.getId()){
            District dubplicateDistrict = entityDao.find(District.class,district.getId());
            if(null != dubplicateDistrict.getEntityLifeCycleData()){
                district.setEntityLifeCycleData(dubplicateDistrict.getEntityLifeCycleData());
            }
            if(null != dubplicateDistrict.getMasterLifeCycleData()){
                district.setMasterLifeCycleData(dubplicateDistrict.getMasterLifeCycleData());
            }
        }

    	if (checkDuplicates(district,map,result)) {
    		 String regexForDistrictName=jsMessageResourceService.getAppendedPropertyForKeys("allowed.specChars.district.name","core.web.validation.config.customValidatorForDistrictName");
            map.put("regexForDistrictName",regexForDistrictName);
            if(district.getId() != null) {
                District d = baseMasterService.getMasterEntityById(District.class, district.getId());
                if (!(ApprovalStatus.UNAPPROVED_ADDED == d.getApprovalStatus() || ApprovalStatus.CLONED == d.getApprovalStatus())) {
                    map.put("codeViewMode", true);
                }
            }
    		return "district";
    	}

        boolean eventResult = executeMasterEvent(district,"contextObjectDistrict",map);
        if(!eventResult){
            // getActInactReasMapForEdit(map,contextDistrict);
            String masterName = district.getClass().getSimpleName();
            String uniqueParameter = "districtCode";
            String uniqueValue = district.getDistrictCode();
            getActInactReasMapForEditApproved(map,district,masterName,uniqueParameter,uniqueValue);
            map.put("edit" , true);
            ReasonsActiveInactiveMapping reasonsActiveInactiveMapping = getActInactReasonsForEditGeneric(map,district.getReasonActInactMap());
            district.setReasonActInactMap(reasonsActiveInactiveMapping);
            prepareData(map,district);
            map.put("viewable" , false);
            map.put("activeFlag",district.isActiveFlag());
            String regexForDistrictName=jsMessageResourceService.getAppendedPropertyForKeys("allowed.specChars.district.name","core.web.validation.config.customValidatorForDistrictName");
            map.put("regexForDistrictName",regexForDistrictName);
            return "district";
        }

        if (district.getState() == null || district.getState().getId() == null) {
            district.setState(null);
        }
        // we need to get below logged in user from session
        User user = getUserDetails().getUserReference();
        if (user != null) {
            ReasonsActiveInactiveMapping reasonsActiveInactiveMapping = district.getReasonActInactMap();
            if(reasonsActiveInactiveMapping != null){
                saveActInactReasonForMaster(reasonsActiveInactiveMapping,district);
            }
            district.setReasonActInactMap(reasonsActiveInactiveMapping);
            makerCheckerService.masterEntityChangedByUser(district, user);
        }
        if (createAnotherMaster) {
            ReasonsActiveInactiveMapping reasActInactMap = getActInactReasMapForCreate(map);
            District districtForCreateAnother= new District();
            districtForCreateAnother.setReasonActInactMap(reasActInactMap);
        	prepareData(map, districtForCreateAnother);
            String regexForDistrictName=jsMessageResourceService.getAppendedPropertyForKeys("allowed.specChars.district.name","core.web.validation.config.customValidatorForDistrictName");
            map.put("regexForDistrictName",regexForDistrictName);
            return "district";
        }
        map.put("masterID", masterId);
        return "redirect:/app/grid/District/District/loadColumnConfig";

    }
    
    private boolean checkDuplicates(District district, ModelMap map,
			BindingResult result) {
    	BaseLoggers.flowLogger.debug(district.getLogInfo());
        /*
         * Map whoes Key Is Table Column Name with whom to validate and Value is
         * The One to be validated.This Map Is Send in the Validator Method
         */
        Map<String, Object> validateMap = new HashMap<String, Object>();
        validateMap.put("districtCode", district.getDistrictCode());
        validateMap.put("districtName", district.getDistrictName());

        /*
         * Code to check as if any existing(or new) record is being modified(or created) into another existing record
         */
        List<String> colNameList = checkValidationForDuplicates(district, District.class, validateMap);
        boolean errorOccuredInThisDistrict = Boolean.FALSE;
        if (result.hasErrors() || CollectionUtils.isNotEmpty(colNameList)) {
            String masterName = district.getClass().getSimpleName();
            String uniqueValue = null;
            String uniqueParameter = null;
            if (null != district.getId()) {
                District dis = baseMasterService.findById(District.class, district.getId());
                uniqueValue = dis.getDistrictCode();
                uniqueParameter = "districtCode";
                getActInactReasMapForEditApproved(map, district, masterName, uniqueParameter, uniqueValue);
            }
            else {
                ReasonsActiveInactiveMapping reasActInactMap = getActInactReasMapForCreate(map);
                district.setReasonActInactMap(reasActInactMap);
            }
            ReasonsActiveInactiveMapping reasonsActiveInactiveMapping = getActInactReasonsForEditGeneric(map,district.getReasonActInactMap());
            district.setReasonActInactMap(reasonsActiveInactiveMapping);;
            map.put("edit" , true);
            map.put("viewable" ,false);
        	prepareDataForShowDuplicates(district, map, result, colNameList);
        	errorOccuredInThisDistrict = Boolean.TRUE;
        }
		return errorOccuredInThisDistrict;
	}
    
    private void prepareDataForShowDuplicates(District district, ModelMap map,
			BindingResult result, List<String> colNameList) {
    	prepareData(map, district);
        if (CollectionUtils.isNotEmpty(colNameList)) {
            for (String c : colNameList) {
                result.rejectValue(c, "label." + c + ".validation.exists");
            }
        }
    }

    /**
     * @param record
     *            id for edit
     * @return void
     * @throws
     * @description to create District
     */
    @PreAuthorize("hasAuthority('MAKER_DISTRICT')")
    @RequestMapping(value = "/create")
    public String createDistrict(@ModelAttribute("district") District district, ModelMap map, BindingResult result) {
        ReasonsActiveInactiveMapping reasActInactMap = getActInactReasMapForCreate(map);
        District districtNew = new District();
        districtNew.setReasonActInactMap(reasActInactMap);
        map.put("reasonsActiveInactiveMapping",districtNew.getReasonActInactMap());
        String regexForDistrictName=jsMessageResourceService.getAppendedPropertyForKeys("allowed.specChars.district.name","core.web.validation.config.customValidatorForDistrictName");
        map.put("regexForDistrictName",regexForDistrictName);
    	prepareData(map,districtNew);
        return "district";
    }
    
    private void prepareData(ModelMap map, District district) {
        map.put("masterID", masterId);
        map.put("district", district);
        List<State> states= baseMasterService.getLastApprovedEntities(State.class);
        map.put("stateList", states);
	}

    /**
     * @param record
     *            id for edit
     * @return void
     * @throws
     * @description to edit District
     */
    @PreAuthorize("hasAuthority('MAKER_DISTRICT')")
    @RequestMapping(value = "/edit/{id}")
    public String editDistrict(@PathVariable("id") Long id, @ModelAttribute("district") District district, ModelMap map) {
        District contextDistrict = baseMasterService.getMasterEntityById(District.class, id);
        district = contextDistrict;
        if (contextDistrict.getApprovalStatus() == ApprovalStatus.UNAPPROVED_MODIFIED) {
            District prevDistrict = (District) baseMasterService.getLastApprovedEntityByUnapprovedEntityId(contextDistrict
                    .getEntityId());
            map.put("prevDistrict", prevDistrict);
            map.put("editLink", false);
        }
        if(!(ApprovalStatus.UNAPPROVED_ADDED == contextDistrict.getApprovalStatus() || ApprovalStatus.CLONED == contextDistrict.getApprovalStatus())) {
            map.put("codeViewMode", true);
        }
        Hibernate.initialize(district.getState());
        map.put("edit", true);
        String regexForDistrictName=jsMessageResourceService.getAppendedPropertyForKeys("allowed.specChars.district.name","core.web.validation.config.customValidatorForDistrictName");
        map.put("regexForDistrictName",regexForDistrictName);
        ReasonsActiveInactiveMapping reasonsActiveInactiveMapping = getActInactReasonsForEditGeneric(map,contextDistrict.getReasonActInactMap());
        contextDistrict.setReasonActInactMap(reasonsActiveInactiveMapping);
        // getActInactReasMapForEdit(map,contextDistrict);
        String masterName = contextDistrict.getClass().getSimpleName();
        String uniqueParameter = "districtCode";
        String uniqueValue = contextDistrict.getDistrictCode();
        getActInactReasMapForEditApproved(map,contextDistrict,masterName,uniqueParameter,uniqueValue);
        prepareData(map, contextDistrict);
        map.put("viewable" ,false);
        return "district";
    }

    /**
     * @description to save and send for approval * @return String
     * @throws IOException
     * @description to save and send for approval District object from view
     */
    @PreAuthorize("hasAuthority('MAKER_DISTRICT')")
    @RequestMapping(value = "/saveAndSendForApproval", method = RequestMethod.POST)
    public String saveAndSendForApproval(@Validated District district, BindingResult result, ModelMap map,
            @RequestParam("createAnotherMaster") boolean createAnotherMaster) {

        if(null!=district.getId()){
            District dubplicateDistrict = entityDao.find(District.class,district.getId());
            if(null != dubplicateDistrict.getEntityLifeCycleData()){
                district.setEntityLifeCycleData(dubplicateDistrict.getEntityLifeCycleData());
            }
            if(null != dubplicateDistrict.getMasterLifeCycleData()){
                district.setMasterLifeCycleData(dubplicateDistrict.getMasterLifeCycleData());
            }
        }

    	if (checkDuplicates(district,map,result)) {
            String regexForDistrictName=jsMessageResourceService.getAppendedPropertyForKeys("allowed.specChars.district.name","core.web.validation.config.customValidatorForDistrictName");
            map.put("regexForDistrictName",regexForDistrictName);
            if(district.getId() != null) {
                District d = baseMasterService.getMasterEntityById(District.class, district.getId());
                if (!(ApprovalStatus.UNAPPROVED_ADDED == d.getApprovalStatus() || ApprovalStatus.CLONED == d.getApprovalStatus())) {
                    map.put("codeViewMode", true);
                }
            }
    		return "district";
    	}

        boolean eventResult = executeMasterEvent(district,"contextObjectDistrict",map);
        if(!eventResult){
            // getActInactReasMapForEdit(map,contextDistrict);
            String masterName = district.getClass().getSimpleName();
            String uniqueParameter = "districtCode";
            String uniqueValue = district.getDistrictCode();
            getActInactReasMapForEditApproved(map,district,masterName,uniqueParameter,uniqueValue);
            map.put("edit" , true);
            ReasonsActiveInactiveMapping reasonsActiveInactiveMapping = getActInactReasonsForEditGeneric(map,district.getReasonActInactMap());
            district.setReasonActInactMap(reasonsActiveInactiveMapping);
            prepareData(map,district);
            map.put("viewable" , false);
            map.put("activeFlag",district.isActiveFlag());
            String regexForDistrictName=jsMessageResourceService.getAppendedPropertyForKeys("allowed.specChars.district.name","core.web.validation.config.customValidatorForDistrictName");
            map.put("regexForDistrictName",regexForDistrictName);
            return "district";
        }

        if (district.getState() == null || district.getState().getId() == null) {
            district.setState(null);
        }
        // we need to get below logged in user from session
        User user = getUserDetails().getUserReference();
        if (user != null) {
            ReasonsActiveInactiveMapping reasonsActiveInactiveMapping = district.getReasonActInactMap();
            if(reasonsActiveInactiveMapping != null){
                saveActInactReasonForMaster(reasonsActiveInactiveMapping,district);
            }
            district.setReasonActInactMap(reasonsActiveInactiveMapping);
            makerCheckerService.saveAndSendForApproval(district, user);
        }
        if (createAnotherMaster) {
            ReasonsActiveInactiveMapping reasActInactMap = getActInactReasMapForCreate(map);
            District districtForCreateAnother= new District();
            districtForCreateAnother.setReasonActInactMap(reasActInactMap);
        	prepareData(map, districtForCreateAnother);
            String regexForDistrictName=jsMessageResourceService.getAppendedPropertyForKeys("allowed.specChars.district.name","core.web.validation.config.customValidatorForDistrictName");
            map.put("regexForDistrictName",regexForDistrictName);
            return "district";
        }

        map.put("masterID", masterId);
        return "redirect:/app/grid/District/District/loadColumnConfig";

    }

    /**
     * @param record
     *            id for view
     * @return void
     * @throws
     * @description to view district
     */

    @SuppressWarnings("unchecked")
    @PreAuthorize("hasAuthority('VIEW_DISTRICT') or hasAuthority('MAKER_DISTRICT') or hasAuthority('CHECKER_DISTRICT')")
    @RequestMapping(value = "/view/{id}", method = RequestMethod.GET)
    public String viewDistrict(@PathVariable("id") Long id, ModelMap map) {
        UserInfo currentUser = getUserDetails();
        District district = baseMasterService.getMasterEntityWithActionsById(District.class, id, currentUser
                .getUserEntityId().getUri());
        HibernateUtils.initializeAndUnproxy(district.getState());
        List<State> stateList = new ArrayList<State>();
        stateList.add(district.getState());
        if (district.getApprovalStatus() == ApprovalStatus.UNAPPROVED_MODIFIED
                || district.getApprovalStatus() == ApprovalStatus.WORFLOW_IN_PROGRESS) {
            District prevDistrict = (District) baseMasterService.getLastApprovedEntityByUnapprovedEntityId(district
                    .getEntityId());
            map.put("prevDistrict", prevDistrict);
           map.put("editLink", false);
        } else if (district.getApprovalStatus() == ApprovalStatus.APPROVED_MODIFIED) {
            District prevDistrict = (District) baseMasterService.getLastUnApprovedEntityByApprovedEntityId(district
                    .getEntityId());
            map.put("prevDistrict", prevDistrict);
           map.put("viewLink", false);
        }

        Hibernate.initialize(district.getState());
        ReasonsActiveInactiveMapping reasonsActiveInactiveMapping = getActInactReasonsForEditGeneric(map,district.getReasonActInactMap());
        district.setReasonActInactMap(reasonsActiveInactiveMapping);
        // getActInactReasMapForEdit(map,district);
        String masterName = district.getClass().getSimpleName();
        String uniqueParameter = "districtCode";
        String uniqueValue = district.getDistrictCode();
        getActInactReasMapForEditApproved(map,district,masterName,uniqueParameter,uniqueValue);
        map.put("district", district);
        map.put("masterID", masterId);
        map.put("stateList",stateList);
        map.put("viewable", true);
        map.put("codeViewMode", true);
        if (district.getViewProperties() != null) {
            List<String> actions = (ArrayList<String>) district.getViewProperties().get("actions");
            if (actions != null) {
                for (String act : actions) {
                    String actionString = "act" + act;
                    map.put(actionString.replaceAll(" ", ""), false);
                }

            }

        }

        return "district";
    }


}
