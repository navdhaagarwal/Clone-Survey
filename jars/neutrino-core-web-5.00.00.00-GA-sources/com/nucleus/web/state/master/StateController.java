/**
 * Copyright Nucleus Software India Pvt Ltd. All rights reserved.
 */
package com.nucleus.web.state.master;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;
import javax.inject.Named;

import com.nucleus.activeInactiveReason.ReasonsActiveInactiveMapping;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.collections4.PredicateUtils;
import org.apache.commons.lang3.StringUtils;
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
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.UriUtils;

import com.nucleus.address.Country;
import com.nucleus.address.IntraCountryRegion;
import com.nucleus.address.MasterGeographicService;
import com.nucleus.address.State;
import com.nucleus.address.VehicleStateRegistraionMapping;
import com.nucleus.core.web.util.ComboBoxAdapterUtil;
import com.nucleus.entity.ApprovalStatus;
import com.nucleus.finnone.pro.general.util.ValidatorUtils;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.makerchecker.MakerCheckerService;
import com.nucleus.master.BaseMasterService;
import com.nucleus.persistence.HibernateUtils;
import com.nucleus.user.User;
import com.nucleus.user.UserInfo;
import com.nucleus.web.common.controller.BaseController;

import com.nucleus.core.initialization.ProductInformationLoader;
import com.nucleus.core.state.service.VehicleStateService;
import com.nucleus.config.persisted.service.ConfigurationService;
import com.nucleus.jsMessageResource.service.JsMessageResourceService;

import flexjson.JSONSerializer;

/**
 * @author Nucleus Software India Pvt Ltd Controller for country master form
 *         operations
 */
@Transactional
@Controller
@RequestMapping(value = "/State")
public class StateController extends BaseController {

    @Inject
    @Named("stringEncryptor")
    private StandardPBEStringEncryptor encryptor;
    @Inject
    @Named("makerCheckerService")
    private MakerCheckerService makerCheckerService;

    @Inject
    @Named("baseMasterService")
    private BaseMasterService   baseMasterService;

    @Inject
    @Named("masterGeographicService")
    private MasterGeographicService   masterGeographicService;


    @Inject
    @Named("stateValidator")
    private Validator   stateValidator;

    @Inject
    @Named("configurationService")
    public ConfigurationService configurationService;

    @Inject
    @Named("jsMessageResourceService")
    public JsMessageResourceService jsMessageResourceService;
    
    @Inject
    @Named("vehicleStateService")
    private VehicleStateService vehicleStateService;

    private static final String                      masterId = "State";
    public static final String FALSE = "false";
    private static final String CONFIGURATION_QUERY = "Configuration.getPropertyValueFromPropertyKey";


    public String isPincodeValidation(){
        String pincodeValidation = FALSE;
        String moduleCode = "";
        if (ProductInformationLoader.productInfoExists()){
            moduleCode = ProductInformationLoader.getProductCode();
            pincodeValidation = configurationService.getPropertyValueByPropertyKey("custom.pincode.validation."+moduleCode.toLowerCase(),CONFIGURATION_QUERY);
            if(pincodeValidation == null){
                pincodeValidation = FALSE;
            }
            pincodeValidation = pincodeValidation.toLowerCase();
        }
        return pincodeValidation;
    }

    @InitBinder("state")
    protected void initBinder(WebDataBinder binder) {
        binder.setValidator(stateValidator);
    }

    /*Method Added to send current Entity Uri for working of comments,activity,history,notes*/
    @ModelAttribute("currentEntityClassName")
    public String getEntityClassName() throws UnsupportedEncodingException {
        String encrypt="enc_"+encryptor.encrypt(State.class.getName());
        String returnUri=UriUtils.encodeQueryParam(encrypt,"UTF-8");
        return returnUri;
    }

    /**
     * @param country
     *            object containing country name,ISO code,ISD code,nationality
     *            and country group.
     * @return String
     * @throws IOException
     * @description to save country object from view
     */
    @PreAuthorize("hasAuthority('MAKER_STATE')")
    @RequestMapping(value = "/save", method = RequestMethod.POST)
    public String saveState(@Validated State state, BindingResult result, ModelMap map,
                            @RequestParam("createAnotherMaster") boolean createAnotherMaster) {

        BaseLoggers.flowLogger.debug(state.getLogInfo());
        /*
         * Map whose Key Is Table Column Name with whom to validate and Value is
         * The One to be validated.This Map Is Send in the Validator Method
         */

        State dubplicateState = null;
        if(null!=state.getId()){
            dubplicateState = entityDao.find(State.class,state.getId());
            if(null != dubplicateState.getEntityLifeCycleData()){
                state.setEntityLifeCycleData(dubplicateState.getEntityLifeCycleData());
            }
            if(null != dubplicateState.getMasterLifeCycleData()){
                state.setMasterLifeCycleData(dubplicateState.getMasterLifeCycleData());
            }
        }

        Map<String, Object> validateMap = new HashMap<String, Object>();
        validateMap.put("stateCode", state.getStateCode());
        validateMap.put("stateName", state.getStateName());

        /*
         * Code to check as if any existing(or new) record is being modified(or created) into another existing record
         */
        List<String> colNameList = checkValidationForDuplicates(state, State.class, validateMap);
        List<String> stateRTOCodes = getStateRTOCodes(state);
        Map<String, List<String>> duplicateStateRTOCode = vehicleStateService.checkForDuplicateStateRTOCode(state.getId(), stateRTOCodes);
        
        if (result.hasErrors() || (colNameList != null && colNameList.size() > 0) || MapUtils.isNotEmpty(duplicateStateRTOCode)) {
            if(state.getId() != null) {
                State s = baseMasterService.getMasterEntityById(State.class, state.getId());
                if (!(ApprovalStatus.UNAPPROVED_ADDED == s.getApprovalStatus() || ApprovalStatus.CLONED == s.getApprovalStatus())) {
                    map.put("codeViewMode", true);
                }
            }
            String masterName = state.getClass().getSimpleName();
            String uniqueValue = null;
            String uniqueParameter = null;
            if (null != state.getId()) {
                //State stateForCode = baseMasterService.findById(State.class, state.getId());
                uniqueValue = dubplicateState.getStateCode();
                uniqueParameter = "stateCode";
                getActInactReasMapForEditApproved(map, state, masterName, uniqueParameter, uniqueValue);
            }
            else {
                ReasonsActiveInactiveMapping reasActInactMap = getActInactReasMapForCreate(map);
                state.setReasonActInactMap(reasActInactMap);
            }
            map.put("viewable" , false);
            map.put("edit" , true);
            ReasonsActiveInactiveMapping reasonsActiveInactiveMapping = getActInactReasonsForEditGeneric(map,state.getReasonActInactMap());
            state.setReasonActInactMap(reasonsActiveInactiveMapping);
            prepareDataforDuplicateStateRTOCode(map, duplicateStateRTOCode);
            map.put("state", state);
            map.put("masterID", masterId);
            /*
             * if List "colNameList" Contains Any Duplicate Values Column Names,
             * Then set them in result
             */
            if (colNameList != null && colNameList.size() > 0) {
                for (String c : colNameList) {
                    result.rejectValue(c, "label." + c + ".validation.exists");
                }
            }
            List<IntraCountryRegion> intraRegionList = null;
            if(ValidatorUtils.notNull(state)
                    && ValidatorUtils.notNull(state.getCountry())
                    && ValidatorUtils.notNull(state.getCountry().getId())){
                intraRegionList = masterGeographicService.getAllIntraCountryRegionsOfCountryById(state.getCountry().getId());
            }
            map.put("countryList", baseMasterService.getLastApprovedEntities(Country.class));
            map.put("intraRegionList", intraRegionList);
            String regexForStateCode=jsMessageResourceService.getAppendedPropertyForKeys("allowed.specChars.state.code","core.web.validation.config.customValidatorForStateCode");
            map.put("regexForStateCode",regexForStateCode);
            map.put("enableCustomPincode",isPincodeValidation());
            String isIndiaRegionFlag=configurationService.getPropertyValueByPropertyKey("config.isIndiaRegion", "Configuration.getPropertyValueFromPropertyKey");
            if(StringUtils.isNotEmpty(isIndiaRegionFlag)) {
                isIndiaRegionFlag = isIndiaRegionFlag.toLowerCase();
            }
            map.put("isIndiaRegionFlag", isIndiaRegionFlag);
            return "state";
        }
        map.put("duplicateRTOExist", false);

        boolean eventResult = executeMasterEvent(state,"contextObjectState",map);
        if(!eventResult){
            List<IntraCountryRegion> intraRegionList = null;
            if(ValidatorUtils.notNull(state)
                    && ValidatorUtils.notNull(state.getCountry())
                    && ValidatorUtils.notNull(state.getCountry().getId())){
                intraRegionList = masterGeographicService.getAllIntraCountryRegionsOfCountryById(state.getCountry().getId());
            }
            map.put("countryList", baseMasterService.getLastApprovedEntities(Country.class));
            map.put("intraRegionList", intraRegionList);
            String regexForStateCode=jsMessageResourceService.getAppendedPropertyForKeys("allowed.specChars.state.code","core.web.validation.config.customValidatorForStateCode");
            map.put("regexForStateCode",regexForStateCode);
            map.put("enableCustomPincode",isPincodeValidation());
            //getActInactReasMapForEdit(map,state);
            String masterName = state.getClass().getSimpleName();
            String uniqueValue = state.getStateCode();
            String uniqueParameter = "stateCode";
            getActInactReasMapForEditApproved(map,state,masterName,uniqueParameter,uniqueValue);
            map.put("edit" , true);
            ReasonsActiveInactiveMapping reasonsActiveInactiveMapping = getActInactReasonsForEditGeneric(map,state.getReasonActInactMap());
            state.setReasonActInactMap(reasonsActiveInactiveMapping);
            String isIndiaRegionFlag=configurationService.getPropertyValueByPropertyKey("config.isIndiaRegion", "Configuration.getPropertyValueFromPropertyKey");
            if(StringUtils.isNotEmpty(isIndiaRegionFlag)) {
                isIndiaRegionFlag = isIndiaRegionFlag.toLowerCase();
            }
            map.put("isIndiaRegionFlag", isIndiaRegionFlag);
            map.put("state", state);
            map.put("viewable" , false);
            map.put("masterID", masterId);

            return "state";
        }

        if (state.getCountry().getId() == null) {
            state.setCountry(null);
        }
        if (state.getRegion().getId() == null) {
            state.setRegion(null);
        }
        populateStateRTOCodes(state);
        // we need to get below logged in user from session
        User user = getUserDetails().getUserReference();
        if (user != null) {
            ReasonsActiveInactiveMapping reasonsActiveInactiveMapping = state.getReasonActInactMap();
            if(reasonsActiveInactiveMapping != null){
                saveActInactReasonForMaster(reasonsActiveInactiveMapping,state);
            }
            state.setReasonActInactMap(reasonsActiveInactiveMapping);
            makerCheckerService.masterEntityChangedByUser(state, user);
        }
        if (createAnotherMaster) {
            ReasonsActiveInactiveMapping reasActInactMap = getActInactReasMapForCreate(map);
            State stateForCreateAnother= new State();
            stateForCreateAnother.setReasonActInactMap(reasActInactMap);
            map.put("state", stateForCreateAnother);
            map.put("countryList", baseMasterService.getLastApprovedEntities(Country.class));
            map.put("masterID", masterId);
            String regexForStateCode=jsMessageResourceService.getAppendedPropertyForKeys("allowed.specChars.state.code","core.web.validation.config.customValidatorForStateCode");
            map.put("regexForStateCode",regexForStateCode);
            map.put("enableCustomPincode",isPincodeValidation());
            String isIndiaRegionFlag=configurationService.getPropertyValueByPropertyKey("config.isIndiaRegion", "Configuration.getPropertyValueFromPropertyKey");
            if(StringUtils.isNotEmpty(isIndiaRegionFlag)) {
                isIndiaRegionFlag = isIndiaRegionFlag.toLowerCase();
            }
            map.put("isIndiaRegionFlag", isIndiaRegionFlag);
            return "state";
        }
        map.put("masterID", masterId);
        return "redirect:/app/grid/State/State/loadColumnConfig";

    }

    private List<String> getStateRTOCodes(State state) {
        Hibernate.initialize(state.getVehicleStateRegistraionMappings());
        List<VehicleStateRegistraionMapping> vehicleStateRegistraionMappings = state.getVehicleStateRegistraionMappings();
        List<String> stateRTOCodes = new ArrayList<String>();
        if(CollectionUtils.isNotEmpty(vehicleStateRegistraionMappings)) {
            for(VehicleStateRegistraionMapping vehicleStateRegistraionMapping : vehicleStateRegistraionMappings) {
                if(vehicleStateRegistraionMapping.getStateRTOCode() != null) {
                    stateRTOCodes.add(vehicleStateRegistraionMapping.getStateRTOCode());
                }
            }
        }
        return stateRTOCodes;
    }

    private void prepareDataforDuplicateStateRTOCode(ModelMap map, Map<String, List<String>> duplicateStateRTOCode) {
        if(MapUtils.isNotEmpty(duplicateStateRTOCode)) {
            StringBuilder prepareRtoString = new StringBuilder();
            prepareRtoString.append("Sate RTO codes ");
            StringBuilder prepareStateString = new StringBuilder();
            int count =0;
            for (Entry<String, List<String>> entry : duplicateStateRTOCode.entrySet()) {
                if(count >0) {
                    prepareRtoString.append(" , ");
                    prepareStateString.append(" , ");
                }
                String listInString = entry.getValue() !=null?String.join(",",entry.getValue()):null;
                prepareRtoString.append(listInString);
                prepareStateString.append(entry.getKey());
                count++;
            }
            if(count >1) {
                prepareRtoString.append(" Already exists in state respectively ").append(prepareStateString.toString());

            }else {
                prepareRtoString.append(" Already exists in state ").append(prepareStateString.toString());
            }
            map.put("duplicateRTO", prepareRtoString.toString());
            map.put("duplicateRTOExist", true);
        }

    }

    /**
     * @param record
     *            id for edit
     * @return void
     * @throws
     * @description to create state
     */
    @PreAuthorize("hasAuthority('MAKER_STATE')")
    @RequestMapping(value = "/create")
    public String createState(ModelMap map) {
        ReasonsActiveInactiveMapping reasActInactMap = getActInactReasMapForCreate(map);
        State state= new State();
        state.setReasonActInactMap(reasActInactMap);
        map.put("reasonsActiveInactiveMapping",state.getReasonActInactMap());
        map.put("state",state);
        map.put("countryList", baseMasterService.getLastApprovedEntities(Country.class));
        map.put("masterID", masterId);
        String regexForStateCode=jsMessageResourceService.getAppendedPropertyForKeys("allowed.specChars.state.code","core.web.validation.config.customValidatorForStateCode");
        map.put("regexForStateCode",regexForStateCode);
        map.put("enableCustomPincode",isPincodeValidation());
        String isIndiaRegionFlag=configurationService.getPropertyValueByPropertyKey("config.isIndiaRegion", "Configuration.getPropertyValueFromPropertyKey");
        if(StringUtils.isNotEmpty(isIndiaRegionFlag)) {
            isIndiaRegionFlag = isIndiaRegionFlag.toLowerCase();
        }
        map.put("isIndiaRegionFlag", isIndiaRegionFlag);
        return "state";
    }

    /**
     * @param record
     *            id for edit
     * @return void
     * @throws
     * @description to edit state
     */
    @PreAuthorize("hasAuthority('MAKER_STATE')")
    @RequestMapping(value = "/edit/{id}")
    public String editState(@PathVariable("id") Long id, ModelMap map) {
        State state = baseMasterService.getMasterEntityById(State.class, id);
        if (state.getApprovalStatus() == ApprovalStatus.UNAPPROVED_MODIFIED) {
            State prevState = (State) baseMasterService.getLastApprovedEntityByUnapprovedEntityId(state.getEntityId());
            Hibernate.initialize(prevState.getCountry());
            Hibernate.initialize(prevState.getRegion());
            map.put("prevState", prevState);
            map.put("viewLink", false);
            
        }

        state = baseMasterService.getMasterEntityById(State.class, id);
        List<IntraCountryRegion> intraRegionList = null;
        Hibernate.initialize(state.getCountry());
        Hibernate.initialize(state.getRegion());

        if(ValidatorUtils.notNull(state)
                && ValidatorUtils.notNull(state.getCountry())
                && ValidatorUtils.notNull(state.getCountry().getId())){
            intraRegionList = masterGeographicService.getAllIntraCountryRegionsOfCountryById(state.getCountry().getId());
        }
        if(!(ApprovalStatus.UNAPPROVED_ADDED == state.getApprovalStatus() || ApprovalStatus.CLONED == state.getApprovalStatus())) {
            map.put("codeViewMode", true);
        }

/*

        List<Integer> approvalStatusList = new ArrayList<Integer>();
        approvalStatusList.add(ApprovalStatus.APPROVED);
        approvalStatusList.add(ApprovalStatus.APPROVED_MODIFIED);
        StringBuilder sb = new StringBuilder();
        String uniqueValue = state.getStateCode();
        String uniqueParameter = "stateCode";
        sb.append("select distinct c from "+masterId+" c where c."+uniqueParameter+" = :uniqueValue and c.masterLifeCycleData.approvalStatus IN :approvalStatus");
        JPAQueryExecutor<BaseMasterEntity> jpaQueryExecutor = new JPAQueryExecutor<BaseMasterEntity>(sb.toString());
        jpaQueryExecutor.addParameter("uniqueValue", uniqueValue).addParameter("approvalStatus", approvalStatusList);
        BaseMasterEntity entity  = entityDao.executeQueryForSingleValue(jpaQueryExecutor);
*/


        if(CollectionUtils.isNotEmpty(state.getVehicleStateRegistraionMappings())) {
             map.put("lastStateRTOIndex", (state.getVehicleStateRegistraionMappings().size()-1));
        }
        ReasonsActiveInactiveMapping reasonsActiveInactiveMapping = getActInactReasonsForEditGeneric(map,state.getReasonActInactMap());
        state.setReasonActInactMap(reasonsActiveInactiveMapping);
        //getActInactReasMapForEdit(map,state);
        String masterName = state.getClass().getSimpleName();
        String uniqueValue = state.getStateCode();
        String uniqueParameter = "stateCode";
        getActInactReasMapForEditApproved(map,state,masterName,uniqueParameter,uniqueValue);
        map.put("viewable" ,false);
        map.put("masterID", masterId);
        map.put("state", state);
        map.put("edit", true);
        map.put("countryList", baseMasterService.getLastApprovedEntities(Country.class));
        map.put("intraRegionList", intraRegionList);
        String regexForStateCode=jsMessageResourceService.getAppendedPropertyForKeys("allowed.specChars.state.code","core.web.validation.config.customValidatorForStateCode");
        map.put("regexForStateCode",regexForStateCode);
        map.put("enableCustomPincode",isPincodeValidation());
        String isIndiaRegionFlag=configurationService.getPropertyValueByPropertyKey("config.isIndiaRegion", "Configuration.getPropertyValueFromPropertyKey");
        if(StringUtils.isNotEmpty(isIndiaRegionFlag)) {
            isIndiaRegionFlag = isIndiaRegionFlag.toLowerCase();
        }
        map.put("isIndiaRegionFlag", isIndiaRegionFlag);
        return "state";
    }

    /**
     * @description to save and send for approval * @return String
     * @throws IOException
     * @description to save and send for approval State object from view
     */
    @PreAuthorize("hasAuthority('MAKER_STATE')")
    @RequestMapping(value = "/saveAndSendForApproval", method = RequestMethod.POST)
    public String saveAndSendForApproval(@Validated State state, BindingResult result, ModelMap map,
                                         @RequestParam("createAnotherMaster") boolean createAnotherMaster) {

        BaseLoggers.flowLogger.debug(state.getLogInfo());
        /*
         * Map whose Key Is Table Column Name with whom to validate and Value is
         * The One to be validated.This Map Is Send in the Validator Method
         */

        State dubplicateState = null;
        if(null!=state.getId()){
            dubplicateState = entityDao.find(State.class,state.getId());
            if(null != dubplicateState.getEntityLifeCycleData()){
                state.setEntityLifeCycleData(dubplicateState.getEntityLifeCycleData());
            }
            if(null != dubplicateState.getMasterLifeCycleData()){
                state.setMasterLifeCycleData(dubplicateState.getMasterLifeCycleData());
            }
        }

        Map<String, Object> validateMap = new HashMap<String, Object>();
        validateMap.put("stateCode", state.getStateCode());
        validateMap.put("stateName", state.getStateName());

        /*
         * Code to check as if any existing(or new) record is being modified(or created) into another existing record
         */
        List<String> colNameList = checkValidationForDuplicates(state, State.class, validateMap);
        List<String> stateRTOCodes = getStateRTOCodes(state);
        Map<String, List<String>> duplicateStateRTOCode = vehicleStateService.checkForDuplicateStateRTOCode(state.getId(), stateRTOCodes);

        if (result.hasErrors() || (colNameList.size() > 0 && colNameList != null) || MapUtils.isNotEmpty(duplicateStateRTOCode)) {
            if(state.getId() != null) {
                State s = baseMasterService.getMasterEntityById(State.class, state.getId());
                if (!(ApprovalStatus.UNAPPROVED_ADDED == s.getApprovalStatus() || ApprovalStatus.CLONED == s.getApprovalStatus())) {
                    map.put("codeViewMode", true);
                }
            }
            String masterName = state.getClass().getSimpleName();
            String uniqueValue = null;
            String uniqueParameter = null;
            if (null != state.getId()) {
                //State stateForCode = baseMasterService.findById(State.class, state.getId());
                uniqueValue = dubplicateState.getStateCode();
                uniqueParameter = "stateCode";
                getActInactReasMapForEditApproved(map, state, masterName, uniqueParameter, uniqueValue);
            }
            else {
                ReasonsActiveInactiveMapping reasActInactMap = getActInactReasMapForCreate(map);
                state.setReasonActInactMap(reasActInactMap);
            }
            map.put("viewable" , false);
            map.put("edit" , true);
            ReasonsActiveInactiveMapping reasonsActiveInactiveMapping = getActInactReasonsForEditGeneric(map,state.getReasonActInactMap());
            state.setReasonActInactMap(reasonsActiveInactiveMapping);
            map.put("state", state);
            map.put("masterID", masterId);
            /*
             * if List "colNameList" Contains Any Duplicate Values Column Names,
             * Then set them in result
             */
            if (colNameList.size() > 0 && colNameList != null) {
                for (String c : colNameList) {
                    result.rejectValue(c, "label." + c + ".validation.exists");
                }
            }
            
            prepareDataforDuplicateStateRTOCode(map, duplicateStateRTOCode);

            List<IntraCountryRegion> intraRegionList = null;
            if(ValidatorUtils.notNull(state)
                    && ValidatorUtils.notNull(state.getCountry())
                    && ValidatorUtils.notNull(state.getCountry().getId())){
                intraRegionList = masterGeographicService.getAllIntraCountryRegionsOfCountryById(state.getCountry().getId());
            }
            map.put("countryList", baseMasterService.getLastApprovedEntities(Country.class));
            map.put("intraRegionList", intraRegionList);
            String regexForStateCode=jsMessageResourceService.getAppendedPropertyForKeys("allowed.specChars.state.code","core.web.validation.config.customValidatorForStateCode");
            map.put("regexForStateCode",regexForStateCode);
            map.put("enableCustomPincode",isPincodeValidation());
            String isIndiaRegionFlag=configurationService.getPropertyValueByPropertyKey("config.isIndiaRegion", "Configuration.getPropertyValueFromPropertyKey");
            if(StringUtils.isNotEmpty(isIndiaRegionFlag)) {
                isIndiaRegionFlag = isIndiaRegionFlag.toLowerCase();
            }
            map.put("isIndiaRegionFlag", isIndiaRegionFlag);
            return "state";
        }
        map.put("duplicateRTOExist", false);

        boolean eventResult = executeMasterEvent(state,"contextObjectState",map);
        if(!eventResult){
            List<IntraCountryRegion> intraRegionList = null;
            if(ValidatorUtils.notNull(state)
                    && ValidatorUtils.notNull(state.getCountry())
                    && ValidatorUtils.notNull(state.getCountry().getId())){
                intraRegionList = masterGeographicService.getAllIntraCountryRegionsOfCountryById(state.getCountry().getId());
            }
            map.put("countryList", baseMasterService.getLastApprovedEntities(Country.class));
            map.put("intraRegionList", intraRegionList);
            String regexForStateCode=jsMessageResourceService.getAppendedPropertyForKeys("allowed.specChars.state.code","core.web.validation.config.customValidatorForStateCode");
            map.put("regexForStateCode",regexForStateCode);
            map.put("enableCustomPincode",isPincodeValidation());
            //getActInactReasMapForEdit(map,state);
            String masterName = state.getClass().getSimpleName();
            String uniqueValue = state.getStateCode();
            String uniqueParameter = "stateCode";
            getActInactReasMapForEditApproved(map,state,masterName,uniqueParameter,uniqueValue);
            ReasonsActiveInactiveMapping reasonsActiveInactiveMapping = getActInactReasonsForEditGeneric(map,state.getReasonActInactMap());
            state.setReasonActInactMap(reasonsActiveInactiveMapping);

            map.put("state", state);
            map.put("viewable" , false);
            map.put("masterID", masterId);
            String isIndiaRegionFlag=configurationService.getPropertyValueByPropertyKey("config.isIndiaRegion", "Configuration.getPropertyValueFromPropertyKey");
            if(StringUtils.isNotEmpty(isIndiaRegionFlag)) {
                isIndiaRegionFlag = isIndiaRegionFlag.toLowerCase();
            }
            map.put("isIndiaRegionFlag", isIndiaRegionFlag);
            return "state";
        }

        if (state.getCountry().getId() == null) {
            state.setCountry(null);
        }
        if (state.getRegion().getId() == null) {
            state.setRegion(null);
        }
        populateStateRTOCodes(state);
        // we need to get below logged in user from session
        User user = getUserDetails().getUserReference();
        if (user != null) {
            ReasonsActiveInactiveMapping reasonsActiveInactiveMapping = state.getReasonActInactMap();
            if(reasonsActiveInactiveMapping != null){
                saveActInactReasonForMaster(reasonsActiveInactiveMapping,state);
            }
            state.setReasonActInactMap(reasonsActiveInactiveMapping);
            makerCheckerService.saveAndSendForApproval(state, user);
        }
        if (createAnotherMaster) {
            ReasonsActiveInactiveMapping reasActInactMap = getActInactReasMapForCreate(map);
            State stateForCreateAnother= new State();
            stateForCreateAnother.setReasonActInactMap(reasActInactMap);
            map.put("state", stateForCreateAnother);
            map.put("countryList", baseMasterService.getLastApprovedEntities(Country.class));
            map.put("masterID", masterId);
            String regexForStateCode=jsMessageResourceService.getAppendedPropertyForKeys("allowed.specChars.state.code","core.web.validation.config.customValidatorForStateCode");
            map.put("regexForStateCode",regexForStateCode);
            map.put("enableCustomPincode",isPincodeValidation());
            String isIndiaRegionFlag=configurationService.getPropertyValueByPropertyKey("config.isIndiaRegion", "Configuration.getPropertyValueFromPropertyKey");
            if(StringUtils.isNotEmpty(isIndiaRegionFlag)) {
                isIndiaRegionFlag = isIndiaRegionFlag.toLowerCase();
            }
            map.put("isIndiaRegionFlag", isIndiaRegionFlag);
            return "state";
        }
        map.put("masterID", masterId);
        return "redirect:/app/grid/State/State/loadColumnConfig";

    }

    private void populateStateRTOCodes(State state) {
        List<VehicleStateRegistraionMapping> vehicleStateRegistraionMappings = state.getVehicleStateRegistraionMappings();
        if (CollectionUtils.isNotEmpty(vehicleStateRegistraionMappings)) {
            Iterator<VehicleStateRegistraionMapping> iterator = vehicleStateRegistraionMappings.iterator();
            while (iterator.hasNext()) {
                VehicleStateRegistraionMapping vehRegMap = iterator.next();
                if (StringUtils.isBlank(vehRegMap.getStateRTOCode())) {
                    iterator.remove();
                }
            }

        } else {
            state.setVehicleStateRegistraionMappings(null);
        }
    }

    @PreAuthorize("hasAuthority('VIEW_STATE') or hasAuthority('MAKER_STATE') or hasAuthority('CHECKER_STATE')")
    @RequestMapping(value = "/view/{id}", method = RequestMethod.GET)
    public String viewState(@PathVariable("id") Long id, ModelMap map) {
        UserInfo currentUser = getUserDetails();
        State state = baseMasterService.getMasterEntityWithActionsById(State.class, id, currentUser.getUserEntityId()
                .getUri());

        HibernateUtils.initializeAndUnproxy(state.getCountry());
        HibernateUtils.initializeAndUnproxy(state.getRegion());

        List<Country> countryList = new ArrayList<Country>();
        countryList.add(state.getCountry());
        List<IntraCountryRegion> intraRegionList = new ArrayList<IntraCountryRegion>();
        intraRegionList.add(state.getRegion());

        if (state.getApprovalStatus() == ApprovalStatus.UNAPPROVED_MODIFIED
                || state.getApprovalStatus() == ApprovalStatus.WORFLOW_IN_PROGRESS) {
            State prevState = (State) baseMasterService.getLastApprovedEntityByUnapprovedEntityId(state.getEntityId());
            Hibernate.initialize(prevState.getCountry());
            Hibernate.initialize(prevState.getRegion());
            map.put("prevState", prevState);
            map.put("editLink", false);
        } else if (state.getApprovalStatus() == ApprovalStatus.APPROVED_MODIFIED) {
            State prevState = (State) baseMasterService.getLastUnApprovedEntityByApprovedEntityId(state.getEntityId());
            map.put("prevState", prevState);
            map.put("viewLink", false);
        }
        if(CollectionUtils.isNotEmpty(state.getVehicleStateRegistraionMappings())) {
            map.put("lastStateRTOIndex", (state.getVehicleStateRegistraionMappings().size()-1));
        }
        ReasonsActiveInactiveMapping reasonsActiveInactiveMapping = getActInactReasonsForEditGeneric(map,state.getReasonActInactMap());
        state.setReasonActInactMap(reasonsActiveInactiveMapping);

        //getActInactReasMapForEdit(map,state);
        String masterName = state.getClass().getSimpleName();
        String uniqueValue = state.getStateCode();
        String uniqueParameter = "stateCode";
        getActInactReasMapForEditApproved(map,state,masterName,uniqueParameter,uniqueValue);
        map.put("state", state);
        map.put("countryList",countryList);
        map.put("intraRegionList",intraRegionList);
        map.put("masterID", masterId);
        map.put("viewable", true);
        map.put("enableCustomPincode",isPincodeValidation());
        map.put("codeViewMode", true);
        String isIndiaRegionFlag=configurationService.getPropertyValueByPropertyKey("config.isIndiaRegion", "Configuration.getPropertyValueFromPropertyKey");
        if(StringUtils.isNotEmpty(isIndiaRegionFlag)) {
            isIndiaRegionFlag = isIndiaRegionFlag.toLowerCase();
        }
        map.put("isIndiaRegionFlag", isIndiaRegionFlag);

        if (state.getViewProperties() != null) {
            @SuppressWarnings("unchecked")
            ArrayList<String> actions = (ArrayList<String>) state.getViewProperties().get("actions");
            if (actions != null) {
                for (String act : actions) {
                    String actionString = "act" + act;
                    map.put(actionString.replaceAll(" ", ""), false);
                }

            }

        }

        return "state";
    }

    //Added to filter intra-country regions on basis of country
    @PreAuthorize("hasAuthority('MAKER_STATE')")
    @SuppressWarnings("rawtypes")
    @RequestMapping(value = "/getRegionsForCountry/{countryId}")
    public @ResponseBody
    String fetchListOfValuesForScheme(ModelMap map, @PathVariable("countryId") Long countryId) {
        JSONSerializer iSerializer = new JSONSerializer();
        List<Map<String, ?>> par = null;

        Map consolidateMap = null;
        List<IntraCountryRegion> regionList = masterGeographicService.getAllIntraCountryRegionsOfCountryById(countryId);
        par = new ArrayList<Map<String, ?>>();
        for (IntraCountryRegion region : regionList) {
            Map<String, String> valueMap = new HashMap<String, String>();
            valueMap.put("id", String.valueOf(region.getId()));
            valueMap.put("intraRegionName", region.getIntraRegionName());
            par.add(valueMap);
            consolidateMap = ComboBoxAdapterUtil.listOfMapsToSingleMap(par, "id", "intraRegionName");
        }

        return iSerializer.serialize(consolidateMap);
    }

}
