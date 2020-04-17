/**
 * Copyright Nucleus Software India Pvt Ltd. All rights reserved.
 */
package com.nucleus.web.city.master;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import com.nucleus.activeInactiveReason.ReasonsActiveInactiveMapping;
import com.nucleus.address.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.WordUtils;
import org.hibernate.Hibernate;
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

import com.nucleus.core.genericparameter.service.GenericParameterServiceImpl;
import com.nucleus.entity.ApprovalStatus;
import com.nucleus.finnone.pro.general.util.ValidatorUtils;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.makerchecker.MakerCheckerService;
import com.nucleus.master.BaseMasterService;
import com.nucleus.persistence.HibernateUtils;
import com.nucleus.user.User;
import com.nucleus.user.UserInfo;
import com.nucleus.web.common.controller.BaseController;
import com.nucleus.jsMessageResource.service.JsMessageResourceService;

/**
 * @author Nucleus Software India Pvt Ltd This file is being used for
 *         controlling city CRUD and task allocation work-flow related
 *         operations.
 */
@Transactional
@Controller
@RequestMapping(value = "/City")
public class CityController extends BaseController {

    @Inject
    @Named("makerCheckerService")
    private MakerCheckerService         makerCheckerService;

    @Inject
    @Named("baseMasterService")
    private BaseMasterService           baseMasterService;

    @Inject
    @Named("genericParameterService")
    private GenericParameterServiceImpl genericParameterServiceImpl;

    private static final String                              masterId = "City";

    @Inject
    @Named("cityValidator")
    private CityValidator               cityValidator;
    
    @Inject
    @Named("addressService")
    private AddressTagService          addressService;

	@Inject
	@Named("stringEncryptor")
	private StandardPBEStringEncryptor encryptor;

	@Inject
    @Named("jsMessageResourceService")
    public JsMessageResourceService jsMessageResourceService;
	
    @InitBinder("city")
    protected void initBinder(WebDataBinder binder) {

        binder.setValidator(cityValidator);
    }

    /*Method Added to send current Entity Uri for working of comments,activity,history,notes*/
    @ModelAttribute("currentEntityClassName")
    public String getEntityClassName() throws UnsupportedEncodingException {
        return UriUtils.encodeQueryParam("enc_" + encryptor.encrypt(City.class.getName()), "UTF-8");
    }

    /**
     * @param city
     *            object containing city
     *            name,cityCode,country,state,cityMICRCode etc.
     * @return String
     * @throws IOException
     * @description to save city object from view
     */
    @PreAuthorize("hasAuthority('MAKER_CITY')")
    @RequestMapping(value = "/save", method = RequestMethod.POST)
    public String saveCity(@Validated City city, BindingResult result, ModelMap map,
            @RequestParam("createAnotherMaster") boolean createAnotherMaster) {

        if(null!=city.getId()){
            City dubplicateCity = entityDao.find(City.class,city.getId());
            if(null != dubplicateCity.getEntityLifeCycleData()){
                city.setEntityLifeCycleData(dubplicateCity.getEntityLifeCycleData());
            }
            if(null != dubplicateCity.getMasterLifeCycleData()){
                city.setMasterLifeCycleData(dubplicateCity.getMasterLifeCycleData());
            }
        }

    	if (checkDuplicates(city,map,result)) {
    		String regexForCityName=jsMessageResourceService.getAppendedPropertyForKeys("allowed.specChars.city.name","core.web.validation.config.customValidatorForCityName");
            map.put("regexForCityName",regexForCityName);
            if(city.getId() != null) {
                City c = baseMasterService.getMasterEntityById(City.class, city.getId());
                if (!(ApprovalStatus.UNAPPROVED_ADDED == c.getApprovalStatus() || ApprovalStatus.CLONED == c.getApprovalStatus())) {
                    map.put("codeViewMode", true);
                }
            }
    		return "city";
    	}
        boolean eventResult = executeMasterEvent(city,"contextObjectCity",map);
        if(!eventResult){
            // getActInactReasMapForEdit(map,city);
            String masterName = city.getClass().getSimpleName();
            String uniqueParameter = "cityCode";
            String uniqueValue = city.getCityCode();
            getActInactReasMapForEditApproved(map,city,masterName,uniqueParameter,uniqueValue);
            map.put("edit" , true);
            ReasonsActiveInactiveMapping reasonsActiveInactiveMapping = getActInactReasonsForEditGeneric(map,city.getReasonActInactMap());
            city.setReasonActInactMap(reasonsActiveInactiveMapping);
            map.put("viewable" , false);
            prepareDataForCity(map,city);
            map.put("activeFlag",city.isActiveFlag());
            return "city";
        }
    	prepareDataForSaveCity(city);
        // we need to get below logged in user from session
        User user = getUserDetails().getUserReference();
        if (user != null) {
            ReasonsActiveInactiveMapping reasonsActiveInactiveMapping = city.getReasonActInactMap();
            if(reasonsActiveInactiveMapping != null){
                saveActInactReasonForMaster(reasonsActiveInactiveMapping,city);
            }
            city.setReasonActInactMap(reasonsActiveInactiveMapping);
            makerCheckerService.masterEntityChangedByUser(city, user);
        }
        if (createAnotherMaster) {
            ReasonsActiveInactiveMapping reasActInactMap = getActInactReasMapForCreate(map);
            City cityForCreateAnother= new  City();
            cityForCreateAnother.setReasonActInactMap(reasActInactMap);
        	prepareDataForCity(map,cityForCreateAnother);
        	String regexForCityName=jsMessageResourceService.getAppendedPropertyForKeys("allowed.specChars.city.name","core.web.validation.config.customValidatorForCityName");
            map.put("regexForCityName",regexForCityName);
            return "city";
        }
        map.put("masterID", masterId);
        return "redirect:/app/grid/City/City/loadColumnConfig";

    }
    
    private void prepareDataForSaveCity(City city) {
    	/*
         * To check if referenced entity id is null,set entity as null
         */
        if (city.getState() == null || city.getState().getId() == null) {
            city.setState(null);
        }
        if (city.getDistrict() == null || city.getDistrict().getId() == null) {
            city.setDistrict(null);
        }
        if (city.getCountry() == null || city.getCountry().getId() == null) {
            city.setCountry(null);
        }
        if (city.getLocationType() == null || city.getLocationType().getId() == null) {
            city.setLocationType(null);
        }
        if (city.getCityCategorization() == null || city.getCityCategorization().getId() == null) {
            city.setCityCategorization(null);
        }
        if(city.getCityRiskCategory()==null || city.getCityRiskCategory().getId()==null){
            city.setCityRiskCategory(null);
        }
    }
    
    private boolean checkDuplicates(City city, ModelMap map,
			BindingResult result) {
    	BaseLoggers.flowLogger.debug(city.getLogInfo());
    	/*
         * Map whoes Key Is Table Column Name with whom to validate and Value is
         * The One to be validated.This Map Is Send in the Validator Method
        */
        Map<String, Object> validateMap = new HashMap<>();
        validateMap.put("cityCode", city.getCityCode());
       // validateMap.put("cityName", WordUtils.capitalizeFully(city.getCityName()));
        city.setCityName(WordUtils.capitalizeFully(city.getCityName()));

        /*
         * Code to check as if any existing(or new) record is being modified(or created) into another existing record
        */
        List<String> colNameList = checkValidationForDuplicates(city, City.class, validateMap);
        boolean errorOccuredInThisCity = Boolean.FALSE;
        if(CollectionUtils.isEmpty(colNameList)){
            colNameList = checkForDuplicateCityName(city);
        }
        if (result.hasErrors() || CollectionUtils.isNotEmpty(colNameList)) {
            String masterName = city.getClass().getSimpleName();
            String uniqueValue = null;
            String uniqueParameter = null;
            if (null != city.getId()) {
                City cityForcode = baseMasterService.findById(City.class, city.getId());
                uniqueValue = cityForcode.getCityCode();
                uniqueParameter = "cityCode";
                getActInactReasMapForEditApproved(map, city, masterName, uniqueParameter, uniqueValue);
            }
            else {
                ReasonsActiveInactiveMapping reasActInactMap = getActInactReasMapForCreate(map);
                city.setReasonActInactMap(reasActInactMap);
            }
            ReasonsActiveInactiveMapping reasonsActiveInactiveMapping = getActInactReasonsForEditGeneric(map,city.getReasonActInactMap());
            city.setReasonActInactMap(reasonsActiveInactiveMapping);
        	prepareDataForShowDuplicates(city, map, result, colNameList);
        	errorOccuredInThisCity = Boolean.TRUE;
        }
		return errorOccuredInThisCity;
    
	}
    
    private List<String> checkForDuplicateCityName(City city) {
        /*
         * if cityName + state combo already exists
         */
        List<String> colNameList = new ArrayList<>();
        Long cityId = city.getId();
        boolean isDupli = false;
        Map<String, Object> propertyNameValueMap = new HashMap<>();
        propertyNameValueMap.put("cityName", city.getCityName());
        propertyNameValueMap.put("state", city.getState());
        if(cityId != null && cityId != 0 )
           isDupli = baseMasterService.hasEntity(City.class, propertyNameValueMap, cityId);
        else {
            List<Integer> statusList = new ArrayList<Integer>();
            statusList.add(ApprovalStatus.UNAPPROVED_ADDED);
            statusList.add(ApprovalStatus.APPROVED_MODIFIED);
            statusList.add(ApprovalStatus.UNAPPROVED_MODIFIED);
            statusList.add(ApprovalStatus.APPROVED_DELETED);
            statusList.add(ApprovalStatus.WORFLOW_IN_PROGRESS);
            statusList.add(ApprovalStatus.APPROVED_DELETED_IN_PROGRESS);
            statusList.add(ApprovalStatus.APPROVED);
            List<Map<String, Object>> result = baseMasterService.getAllApprovedAndActiveSelectedListEntities(City.class, statusList,
                    propertyNameValueMap, new String[] { "cityName", "cityCode" });
            if (CollectionUtils.isNotEmpty(result)) {
                isDupli = true;
            }
        }
        if(isDupli)
            colNameList.add("cityName");
        return colNameList; 
    }

    private void prepareDataForShowDuplicates(City city, ModelMap map,
			BindingResult result, List<String> colNameList) {
    	 prepareDataForCity(map,city);
    	/*
         * if List Contains Any Duplicate Values Column Names, Then set them
         * in result
         */
        if (CollectionUtils.isNotEmpty(colNameList)) {
            for (String c : colNameList) {
                result.rejectValue(c, "label." + c + ".validation.exists");
            }
        }
	}
    
    private void prepareDataForCity(ModelMap map, City city) {
        List<CityType> cityCategorization = genericParameterServiceImpl.retrieveTypes(CityType.class);
        List<LocationType> locationType = genericParameterServiceImpl.retrieveTypes(LocationType.class);
        List<Country>  countries = baseMasterService.getLastApprovedEntities(Country.class);
        map.put("city", city);
        map.put("cityCategorizationList", cityCategorization);
        map.put("locationTypeList", locationType);
        map.put("countryList", countries);
		List<State> stateList = null;
		if (ValidatorUtils.notNull(city)
				&& ValidatorUtils.notNull(city.getCountry())
				&& ValidatorUtils.notNull(city.getCountry().getId())) {
			stateList = addressService.findAllApprovedStatesInCountry(city.getCountry().getId());
		}
        List<District> districtList = null;
        if (ValidatorUtils.notNull(stateList)
                && ValidatorUtils.notNull(city.getState())
                && ValidatorUtils.notNull(city.getState().getId())) {
            districtList = addressService.findAllApprovedDistrictsInState(city.getState().getId());
        }
		map.put("stateList", stateList);
        map.put("districtList", districtList);
        map.put("masterID", masterId);
	}

    /**
     * @param record
     *            id for edit
     * @return void
     * @throws
     * @description to create city
     */

    @PreAuthorize("hasAuthority('MAKER_CITY')")
    @RequestMapping(value = "/create")
    public String createCity(ModelMap map) {
        ReasonsActiveInactiveMapping reasActInactMap = getActInactReasMapForCreate(map);
        City city= new City();
        city.setReasonActInactMap(reasActInactMap);
        map.put("reasonsActiveInactiveMapping",city.getReasonActInactMap());
        String regexForCityName=jsMessageResourceService.getAppendedPropertyForKeys("allowed.specChars.city.name","core.web.validation.config.customValidatorForCityName");
        map.put("regexForCityName",regexForCityName);
    	prepareDataForCity(map,city);
        return "city";
    }

    /**
     * @param record
     *            id for edit
     * @return void
     * @throws
     * @description to edit city
     */
    @PreAuthorize("hasAuthority('MAKER_CITY')")
    @RequestMapping(value = "/edit/{id}")
    public String editCity(@PathVariable("id") Long id, ModelMap map) {
        City city = baseMasterService.getMasterEntityById(City.class, id);
        if (city.getApprovalStatus() == ApprovalStatus.UNAPPROVED_MODIFIED) {
            City prevCity = (City) baseMasterService.getLastApprovedEntityByUnapprovedEntityId(city.getEntityId());
            map.put("prevCity", prevCity);
          map.put("viewLink", false);
        }
        if(!(ApprovalStatus.UNAPPROVED_ADDED == city.getApprovalStatus() || ApprovalStatus.CLONED == city.getApprovalStatus())) {
            map.put("codeViewMode", true);
        }
        ReasonsActiveInactiveMapping reasonsActiveInactiveMapping = getActInactReasonsForEditGeneric(map,city.getReasonActInactMap());
        city.setReasonActInactMap(reasonsActiveInactiveMapping);
        // getActInactReasMapForEdit(map,city);
        String masterName = city.getClass().getSimpleName();
        String uniqueParameter = "cityCode";
        String uniqueValue = city.getCityCode();
        getActInactReasMapForEditApproved(map,city,masterName,uniqueParameter,uniqueValue);
        if(city!=null && city.getCityRiskCategory()!=null){
            Hibernate.initialize(city.getCityRiskCategory());
        }
        prepareDataForCity(map,city);
        map.put("edit", true);
        String regexForCityName=jsMessageResourceService.getAppendedPropertyForKeys("allowed.specChars.city.name","core.web.validation.config.customValidatorForCityName");
        map.put("regexForCityName",regexForCityName);
        map.put("viewable" ,false);
        return "city";
    }

    /**
     * @description to save and send for approval * @return String
     * @throws IOException
     * @description to save and send for approval City object from view
     */
    @PreAuthorize("hasAuthority('MAKER_CITY')")
    @RequestMapping(value = "/saveAndSendForApproval", method = RequestMethod.POST)
    public String saveAndSendForApproval(@Validated City city, BindingResult result, ModelMap map,
            @RequestParam("createAnotherMaster") boolean createAnotherMaster) {

        if(null!=city.getId()){
            City dubplicateCity = entityDao.find(City.class,city.getId());
            if(null != dubplicateCity.getEntityLifeCycleData()){
                city.setEntityLifeCycleData(dubplicateCity.getEntityLifeCycleData());
            }
            if(null != dubplicateCity.getMasterLifeCycleData()){
                city.setMasterLifeCycleData(dubplicateCity.getMasterLifeCycleData());
            }
        }

    	if (checkDuplicates(city,map,result)) {
    		String regexForCityName=jsMessageResourceService.getAppendedPropertyForKeys("allowed.specChars.city.name","core.web.validation.config.customValidatorForCityName");
            map.put("regexForCityName",regexForCityName);
            if(city.getId() != null) {
                City c = baseMasterService.getMasterEntityById(City.class, city.getId());
                if (!(ApprovalStatus.UNAPPROVED_ADDED == c.getApprovalStatus() || ApprovalStatus.CLONED == c.getApprovalStatus())) {
                    map.put("codeViewMode", true);
                }
            }
    		return "city";
    	}
        boolean eventResult = executeMasterEvent(city,"contextObjectCity",map);
        if(!eventResult){
            // getActInactReasMapForEdit(map,city);
            String masterName = city.getClass().getSimpleName();
            String uniqueParameter = "cityCode";
            String uniqueValue = city.getCityCode();
            getActInactReasMapForEditApproved(map,city,masterName,uniqueParameter,uniqueValue);
            map.put("edit" , true);
            ReasonsActiveInactiveMapping reasonsActiveInactiveMapping = getActInactReasonsForEditGeneric(map,city.getReasonActInactMap());
            city.setReasonActInactMap(reasonsActiveInactiveMapping);
            map.put("viewable" , false);
            prepareDataForCity(map,city);
            map.put("activeFlag",city.isActiveFlag());
            return "city";
        }

    	prepareDataForSaveCity(city);
        // we need to get below logged in user from session
        User user = getUserDetails().getUserReference();
        if (user != null) {
            ReasonsActiveInactiveMapping reasonsActiveInactiveMapping = city.getReasonActInactMap();
            if(reasonsActiveInactiveMapping != null){
                saveActInactReasonForMaster(reasonsActiveInactiveMapping,city);
            }
            city.setReasonActInactMap(reasonsActiveInactiveMapping);
            makerCheckerService.saveAndSendForApproval(city, user);
        }
        if (createAnotherMaster) {
            ReasonsActiveInactiveMapping reasActInactMap = getActInactReasMapForCreate(map);
            City cityForCreateAnother= new  City();
            cityForCreateAnother.setReasonActInactMap(reasActInactMap);
        	prepareDataForCity(map,cityForCreateAnother);
        	String regexForCityName=jsMessageResourceService.getAppendedPropertyForKeys("allowed.specChars.city.name","core.web.validation.config.customValidatorForCityName");
            map.put("regexForCityName",regexForCityName);
            return "city";
        }
        map.put("masterID", masterId);
        return "redirect:/app/grid/City/City/loadColumnConfig";

    }

    /**
     * @param record
     *            id for view
     * @return void
     * @throws
     * @description to view city
     */
    @PreAuthorize("hasAuthority('VIEW_CITY') or hasAuthority('MAKER_CITY') or hasAuthority('CHECKER_CITY')")
    @RequestMapping(value = "/view/{id}", method = RequestMethod.GET)
    public String viewCity(@PathVariable("id") Long id, ModelMap map) {
        UserInfo currentUser = getUserDetails();
        City city = baseMasterService.getMasterEntityWithActionsById(City.class, id, currentUser.getUserEntityId().getUri());
        
        HibernateUtils.initializeAndUnproxy(city.getCountry());
        HibernateUtils.initializeAndUnproxy(city.getState());
        if(city!=null && city.getCityRiskCategory()!=null){
            Hibernate.initialize(city.getCityRiskCategory());
        }

        List<Country> countryList = new ArrayList<>();
        countryList.add(city.getCountry());
        List<State> stateList = new ArrayList<>();
        stateList.add(city.getState());
        List<District> districtList = new ArrayList<>();
        districtList.add(city.getDistrict());

        if (city.getApprovalStatus() == ApprovalStatus.UNAPPROVED_MODIFIED
                || city.getApprovalStatus() == ApprovalStatus.WORFLOW_IN_PROGRESS) {
            City prevCity = (City) baseMasterService.getLastApprovedEntityByUnapprovedEntityId(city.getEntityId());
            map.put("prevCity", prevCity);
            map.put("editLink", false);
        } else if (city.getApprovalStatus() == ApprovalStatus.APPROVED_MODIFIED) {
            City prevCity = (City) baseMasterService.getLastUnApprovedEntityByApprovedEntityId(city.getEntityId());
            map.put("prevCity", prevCity);
            map.put("viewLink", false);
        }
        List<CityType> cityCategorization = genericParameterServiceImpl.retrieveTypes(CityType.class);
        List<LocationType> locationType = genericParameterServiceImpl.retrieveTypes(LocationType.class);
        ReasonsActiveInactiveMapping reasonsActiveInactiveMapping = getActInactReasonsForEditGeneric(map,city.getReasonActInactMap());
        city.setReasonActInactMap(reasonsActiveInactiveMapping);
        // getActInactReasMapForEdit(map,city);
        String masterName = city.getClass().getSimpleName();
        String uniqueParameter = "cityCode";
        String uniqueValue = city.getCityCode();
        getActInactReasMapForEditApproved(map,city,masterName,uniqueParameter,uniqueValue);
        map.put("cityCategorizationList", cityCategorization);
        map.put("locationTypeList", locationType);
        map.put("countryList",countryList);
        map.put("stateList",stateList);
        map.put("districtList",districtList);
        map.put("city", city);
        map.put("masterID", masterId);
        map.put("viewable", true);
        map.put("codeViewMode", true);

        if (city.getViewProperties() != null) {
            @SuppressWarnings("unchecked")
            List<String> actions = (ArrayList<String>) city.getViewProperties().get("actions");
            if (actions != null) {
                for (String act : actions) {
                    String actionString = "act" + act;
                    map.put(actionString.replaceAll(" ", ""), false);
                }
            }
        }

        return "city";
    }


}
