/**
 * Copyright Nucleus Software India Pvt Ltd. All rights reserved.
 */
package com.nucleus.web.zipcode.master;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.inject.Inject;
import javax.inject.Named;

import com.nucleus.activeInactiveReason.ReasonsActiveInactiveMapping;
import com.nucleus.address.*;
import com.nucleus.autocomplete.AutocompleteService;
import com.nucleus.core.validation.util.NeutrinoValidator;
import com.nucleus.dao.query.NamedQueryExecutor;
import com.nucleus.persistence.EntityDao;
import org.apache.commons.collections4.CollectionUtils;
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

import com.nucleus.core.villagemaster.entity.VillageMaster;
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
 * @author Nucleus Software India Pvt Ltd Controller for country master form
 *         operations
 */
@Transactional
@Controller
@RequestMapping(value = "/ZipCode")
public class ZipCodeController extends BaseController {

    public static final String         COUNTRY_NAME = "countryName";
    public static final String         CODE         = "countryISOCode";

    @Inject
    @Named("makerCheckerService")
    private MakerCheckerService        makerCheckerService;

    @Inject
    @Named("entityDao")
    protected EntityDao entityDao;

    @Inject
    @Named("baseMasterService")
    private BaseMasterService          baseMasterService;

    @Inject
    @Named("addressService")
    private AddressTagService          addressService;

    @Inject
    @Named("stringEncryptor")
    private StandardPBEStringEncryptor encryptor;

    @Inject
    @Named("autocompleteService")
    private AutocompleteService autocompleteService;
    
    
    @Inject
    @Named("zipCodeValidator")
    private Validator   zipCodeValidator;

    @Inject
    @Named("jsMessageResourceService")
    public JsMessageResourceService jsMessageResourceService;
        	

    // For Server Side Validations
    @InitBinder("zipCode")
    protected void initBinder(WebDataBinder binder) {
        binder.setValidator(zipCodeValidator);
    }

    /*Method Added to send current Entity Uri for working of comments,activity,history,notes*/
    @ModelAttribute("currentEntityClassName")
    public String getEntityClassName() throws UnsupportedEncodingException {
        return UriUtils.encodeQueryParam("enc_" + encryptor.encrypt(ZipCode.class.getName()), "UTF-8");
    }

    private static final String masterId = "ZipCode";

    @PreAuthorize("hasAuthority('MAKER_ZIPCODE')")
    @RequestMapping(value = "/create")
    public String createZipCode(ModelMap map) {
        ReasonsActiveInactiveMapping reasActInactMap = getActInactReasMapForCreate(map);
        ZipCode zipCode= new ZipCode();
        zipCode.setReasonActInactMap(reasActInactMap);
        map.put("reasonsActiveInactiveMapping",zipCode.getReasonActInactMap());
    	String regexForZipCodeName=jsMessageResourceService.getAppendedPropertyForKeys("allowed.specChars.zipCode.name","core.web.validation.config.customValidatorForZipcodeName");
    	map.put("regexForZipCodeName",regexForZipCodeName);
    	prepareData(map, zipCode);
        return "zipCode";
    }
    
    private void prepareData(ModelMap map, ZipCode zipCode) {
    	map.put("zipCode", zipCode);
        map.put("masterID", masterId);
        List<Country> countries = baseMasterService.getLastApprovedEntities(Country.class);
        map.put("countryList", countries);
        List<VillageMaster> villages = null;

        List<State> stateList = null;
        List<City> cityList = null;
        List<Integer> approvalStatus = new ArrayList<Integer>();
        approvalStatus.add(ApprovalStatus.UNAPPROVED);
        approvalStatus.add(ApprovalStatus.DELETED_APPROVED_IN_HISTORY);
        approvalStatus.add(ApprovalStatus.UNAPPROVED_HISTORY);

         if(ValidatorUtils.notNull(zipCode)){
            if(ValidatorUtils.notNull(zipCode.getCountry()) 
                    && ValidatorUtils.notNull(zipCode.getCountry().getId())){
                stateList = addressService.findAllApprovedStatesInCountry(zipCode.getCountry().getId());
            }
            
            if(ValidatorUtils.notNull(zipCode.getState())
                    && ValidatorUtils.notNull(zipCode.getState().getId())){
                cityList = addressService.findAllApprovedCitiesInState(zipCode.getState().getId());
                villages = findVillageByState(zipCode.getState().getId());
            }
            if(zipCode.getVillage() != null) {
                for (VillageMaster village : zipCode.getVillage()) {
                    if(!approvalStatus.contains(village.getMasterLifeCycleData().getApprovalStatus())) {
                        villages.add(village);
                    }
                }
            }
        }
        map.put("stateList", stateList);
        map.put("cityList", cityList);
        map.put("villageList", villages);
    }

    /**
     * @param country
     *            object containing country name,ISO code,ISD code,nationality
     *            and country group.
     * @return String
     * @throws IOException
     * @description to save country object from view
     */
    @PreAuthorize("hasAuthority('MAKER_ZIPCODE')")
    @RequestMapping(value = "/save", method = RequestMethod.POST)
    public String saveZipCode(@Validated ZipCode zipCode, BindingResult result, ModelMap map,
            @RequestParam("createAnotherMaster") boolean createAnotherMaster) {

        if(null!=zipCode.getId()){
            ZipCode dubplicateZipCode = entityDao.find(ZipCode.class,zipCode.getId());
            if(null != dubplicateZipCode.getEntityLifeCycleData()){
                zipCode.setEntityLifeCycleData(dubplicateZipCode.getEntityLifeCycleData());
            }
            if(null != dubplicateZipCode.getMasterLifeCycleData()){
                zipCode.setMasterLifeCycleData(dubplicateZipCode.getMasterLifeCycleData());
            }
        }

    	if (checkDuplicates(zipCode,map,result)) {
            String regexForZipCodeName=jsMessageResourceService.getAppendedPropertyForKeys("allowed.specChars.zipCode.name","core.web.validation.config.customValidatorForZipcodeName");
            map.put("regexForZipCodeName",regexForZipCodeName);
            if(zipCode.getId() != null) {
                ZipCode zc = baseMasterService.getMasterEntityById(ZipCode.class, zipCode.getId());
                if (!(ApprovalStatus.UNAPPROVED_ADDED == zc.getApprovalStatus() || ApprovalStatus.CLONED == zc.getApprovalStatus())) {
                    map.put("codeViewMode", true);
                }
            }
    		return "zipCode";
    	}

        boolean eventResult = executeMasterEvent(zipCode,"contextObjectZipCode",map);
        if(!eventResult){
            //getActInactReasMapForEdit(map,zipCodes);
            String masterName = zipCode.getClass().getSimpleName();
            String uniqueValue = zipCode.getZipCode();
            String uniqueParameter = "zipCode";
            getActInactReasMapForEditApproved(map,zipCode,masterName,uniqueParameter,uniqueValue);
            map.put("edit" , true);
            ReasonsActiveInactiveMapping reasonsActiveInactiveMapping = getActInactReasonsForEditGeneric(map,zipCode.getReasonActInactMap());
            zipCode.setReasonActInactMap(reasonsActiveInactiveMapping);
            prepareData(map,zipCode);
            String regexForZipCodeName=jsMessageResourceService.getAppendedPropertyForKeys("allowed.specChars.zipCode.name","core.web.validation.config.customValidatorForZipcodeName");
            map.put("regexForZipCodeName",regexForZipCodeName);
            map.put("viewable" , false);
            map.put("activeFlag",zipCode.isActiveFlag());
            return "zipCode";
        }

    	prepareDataForSave(zipCode);

        // we need to get below logged in user from session
        User user = getUserDetails().getUserReference();
        if (user != null) {
            ReasonsActiveInactiveMapping reasonsActiveInactiveMapping = zipCode.getReasonActInactMap();
            if(reasonsActiveInactiveMapping != null){
                saveActInactReasonForMaster(reasonsActiveInactiveMapping,zipCode);
            }
            zipCode.setReasonActInactMap(reasonsActiveInactiveMapping);
            makerCheckerService.masterEntityChangedByUser(zipCode, user);
        }
        if (createAnotherMaster) {
            ReasonsActiveInactiveMapping reasActInactMap = getActInactReasMapForCreate(map);
            ZipCode zipCodeForCreateAnother= new ZipCode();
            zipCodeForCreateAnother.setReasonActInactMap(reasActInactMap);
        	prepareData(map,zipCodeForCreateAnother);
            String regexForZipCodeName=jsMessageResourceService.getAppendedPropertyForKeys("allowed.specChars.zipCode.name","core.web.validation.config.customValidatorForZipcodeName");
            map.put("regexForZipCodeName",regexForZipCodeName);
            return "zipCode";
        }
        map.put("masterID", masterId);
        return "redirect:/app/grid/ZipCode/ZipCode/loadColumnConfig";

    }
    
    private void prepareDataForSave(ZipCode zipCode) {
        if (zipCode.getCity() != null && zipCode.getCity().getId() == null) {
            zipCode.setCity(null);
        }
        if (zipCode.getCountry() != null && zipCode.getCountry().getId() == null) {
            zipCode.setCountry(null);
        }
        if (zipCode.getState() != null && zipCode.getState().getId() == null) {
            zipCode.setState(null);
        }
        if(zipCode.getVillageIds() == null) {
            zipCode.setVillage(null);
        } else {
            List<VillageMaster> villageMastersObject = new ArrayList<VillageMaster>();

            for (Long villageMasterId : zipCode.getVillageIds()) {
                VillageMaster villageMasterObject = baseMasterService.findById(VillageMaster.class, villageMasterId);
                if (villageMasterObject != null) {
                    villageMastersObject.add(villageMasterObject);
                }
            }
            zipCode.setVillage(villageMastersObject);
        }
	}
    
    private boolean checkDuplicates(ZipCode zipCode, ModelMap map,
			BindingResult result) {
    	BaseLoggers.flowLogger.debug(zipCode.getLogInfo());
        /*
         * Map whose Key Is Table Column Name with whom to validate and Value is
         * The One to be validated.This Map Is Send in the Validator Method
         */
        Map<String, Object> validateMap = new HashMap<String, Object>();
        validateMap.put("zipCode", zipCode.getZipCode());
        String placeName=zipCode.getPlaceName();
        if(placeName.contains("'")){
        	placeName=placeName.replace("'", "''");
        }
        validateMap.put("placeName",placeName );

        /*
         * Code to check as if any existing(or new) record is being modified(or created) into another existing record
         */
        List<String> colNameList = checkValidationForDuplicates(zipCode, ZipCode.class, validateMap);
        boolean errorOccuredInThisZipCode = Boolean.FALSE;
        if (result.hasErrors() || CollectionUtils.isNotEmpty(colNameList)) {
            String masterName = zipCode.getClass().getSimpleName();
            String uniqueValue = null;
            String uniqueParameter = null;
            if (null != zipCode.getId()) {
                ZipCode zip = baseMasterService.findById(ZipCode.class, zipCode.getId());
                uniqueValue = zip.getZipCode();
                uniqueParameter = "zipCode";
                getActInactReasMapForEditApproved(map, zipCode, masterName, uniqueParameter, uniqueValue);
            }
            else {
                ReasonsActiveInactiveMapping reasActInactMap = getActInactReasMapForCreate(map);
                zipCode.setReasonActInactMap(reasActInactMap);
            }
            ReasonsActiveInactiveMapping reasonsActiveInactiveMapping = getActInactReasonsForEditGeneric(map,zipCode.getReasonActInactMap());
            zipCode.setReasonActInactMap(reasonsActiveInactiveMapping);
        	prepareDataForShowDuplicates(zipCode, map, result, colNameList);
        	errorOccuredInThisZipCode = Boolean.TRUE;
        }
		return errorOccuredInThisZipCode;
	}
    
    private void prepareDataForShowDuplicates(ZipCode zipCode, ModelMap map,
			BindingResult result, List<String> colNameList) {
    	prepareData(map, zipCode);
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
     * @description to edit country
     */
    @PreAuthorize("hasAuthority('MAKER_ZIPCODE')")
    @RequestMapping(value = "/edit/{id}")
    public String editZipCode(@PathVariable("id") Long id, ModelMap map) {
        ZipCode zipCodes = new ZipCode();
        zipCodes.setId(id);
        ZipCode zipCode = baseMasterService.getMasterEntityById(ZipCode.class, id);
        prepareVillageIds(zipCode);
        if (zipCode.getApprovalStatus() == ApprovalStatus.UNAPPROVED_MODIFIED) {
            ZipCode prevZipCode = (ZipCode) baseMasterService.getLastApprovedEntityByUnapprovedEntityId(zipCode
                    .getEntityId());
            if(prevZipCode!=null){
                HibernateUtils.initializeAndUnproxy(prevZipCode);
                HibernateUtils.initializeAndUnproxy(prevZipCode.getCountry());
                HibernateUtils.initializeAndUnproxy(prevZipCode.getState());
                HibernateUtils.initializeAndUnproxy(prevZipCode.getCity());

                if (prevZipCode.getVillage() != null){
                    for(VillageMaster village : prevZipCode.getVillage())
                        if (village.getId() == null) {
                            village = null;
                        }
                }

                HibernateUtils.initializeAndUnproxy(prevZipCode.getVillage());
            }
            map.put("prevZipCode", prevZipCode);
            map.put("editLink", false);
        }
        
        zipCodes = baseMasterService.getMasterEntityById(ZipCode.class, id);
        HibernateUtils.initializeAndUnproxy(zipCode.getCountry());
        HibernateUtils.initializeAndUnproxy(zipCode.getState());
        HibernateUtils.initializeAndUnproxy(zipCode.getCity());
        HibernateUtils.initializeAndUnproxy(zipCode.getVillage());

        if(!(ApprovalStatus.UNAPPROVED_ADDED == zipCode.getApprovalStatus() || ApprovalStatus.CLONED == zipCode.getApprovalStatus())) {
            map.put("codeViewMode", true);
        }
        ReasonsActiveInactiveMapping reasonsActiveInactiveMapping = getActInactReasonsForEditGeneric(map,zipCode.getReasonActInactMap());
        zipCode.setReasonActInactMap(reasonsActiveInactiveMapping);
        //getActInactReasMapForEdit(map,zipCodes);
        String masterName = zipCodes.getClass().getSimpleName();
        String uniqueValue = zipCodes.getZipCode();
        String uniqueParameter = "zipCode";
        getActInactReasMapForEditApproved(map,zipCodes,masterName,uniqueParameter,uniqueValue);
        prepareData(map, zipCodes);
        map.put("edit", true);
        String regexForZipCodeName=jsMessageResourceService.getAppendedPropertyForKeys("allowed.specChars.zipCode.name","core.web.validation.config.customValidatorForZipcodeName");
        map.put("regexForZipCodeName",regexForZipCodeName);
        map.put("viewable" ,false);
        return "zipCode";
    }

    /**
     * @description to save and send for approval * @return String
     * @throws IOException
     * @description to save and send for approval ZipCode object from view
     */
    @PreAuthorize("hasAuthority('MAKER_ZIPCODE')")
    @RequestMapping(value = "/saveAndSendForApproval", method = RequestMethod.POST)
    public String saveAndSendForApproval(@Validated ZipCode zipCode, ModelMap map, BindingResult result,
            @RequestParam("createAnotherMaster") boolean createAnotherMaster) {

        if(null!=zipCode.getId()){
            ZipCode dubplicateZipCode = entityDao.find(ZipCode.class,zipCode.getId());
            if(null != dubplicateZipCode.getEntityLifeCycleData()){
                zipCode.setEntityLifeCycleData(dubplicateZipCode.getEntityLifeCycleData());
            }
            if(null != dubplicateZipCode.getMasterLifeCycleData()){
                zipCode.setMasterLifeCycleData(dubplicateZipCode.getMasterLifeCycleData());
            }
        }

    	if (checkDuplicates(zipCode,map,result)) {
            String regexForZipCodeName=jsMessageResourceService.getAppendedPropertyForKeys("allowed.specChars.zipCode.name","core.web.validation.config.customValidatorForZipcodeName");
            map.put("regexForZipCodeName",regexForZipCodeName);
            if(zipCode.getId() != null) {
                ZipCode zc = baseMasterService.getMasterEntityById(ZipCode.class, zipCode.getId());
                if (!(ApprovalStatus.UNAPPROVED_ADDED == zc.getApprovalStatus() || ApprovalStatus.CLONED == zc.getApprovalStatus())) {
                    map.put("codeViewMode", true);
                }
            }
    		return "zipCode";
    	}

        boolean eventResult = executeMasterEvent(zipCode,"contextObjectZipCode",map);
        if(!eventResult){
            //getActInactReasMapForEdit(map,zipCodes);
            String masterName = zipCode.getClass().getSimpleName();
            String uniqueValue = zipCode.getZipCode();
            String uniqueParameter = "zipCode";
            getActInactReasMapForEditApproved(map,zipCode,masterName,uniqueParameter,uniqueValue);
            map.put("edit" , true);
            ReasonsActiveInactiveMapping reasonsActiveInactiveMapping = getActInactReasonsForEditGeneric(map,zipCode.getReasonActInactMap());
            zipCode.setReasonActInactMap(reasonsActiveInactiveMapping);
            prepareData(map,zipCode);
            String regexForZipCodeName=jsMessageResourceService.getAppendedPropertyForKeys("allowed.specChars.zipCode.name","core.web.validation.config.customValidatorForZipcodeName");
            map.put("regexForZipCodeName",regexForZipCodeName);
            map.put("viewable" , false);
            map.put("activeFlag",zipCode.isActiveFlag());
            return "zipCode";
        }

    	prepareDataForSave(zipCode);
    	
        // we need to get below logged in user from session
        User user = getUserDetails().getUserReference();
        if (user != null) {
            ReasonsActiveInactiveMapping reasonsActiveInactiveMapping = zipCode.getReasonActInactMap();
            if(reasonsActiveInactiveMapping != null){
                saveActInactReasonForMaster(reasonsActiveInactiveMapping,zipCode);
            }
            zipCode.setReasonActInactMap(reasonsActiveInactiveMapping);
            makerCheckerService.saveAndSendForApproval(zipCode, user);
        }
        if (createAnotherMaster) {
            ReasonsActiveInactiveMapping reasActInactMap = getActInactReasMapForCreate(map);
            ZipCode zipCodeForCreateAnother= new ZipCode();
            zipCodeForCreateAnother.setReasonActInactMap(reasActInactMap);
            prepareData(map,zipCodeForCreateAnother);
            String regexForZipCodeName=jsMessageResourceService.getAppendedPropertyForKeys("allowed.specChars.zipCode.name","core.web.validation.config.customValidatorForZipcodeName");
            map.put("regexForZipCodeName",regexForZipCodeName);
            return "zipCode";
        }
        map.put("masterID", masterId);
        return "redirect:/app/grid/ZipCode/ZipCode/loadColumnConfig";

    }

    @PreAuthorize("hasAuthority('VIEW_ZIPCODE') or hasAuthority('MAKER_ZIPCODE') or hasAuthority('CHECKER_ZIPCODE')")
    @RequestMapping(value = "/view/{id}", method = RequestMethod.GET)
    public String viewZipCode(@PathVariable("id") Long id, ModelMap map) {
        UserInfo currentUser = getUserDetails();
        ZipCode zipCode = baseMasterService.getMasterEntityWithActionsById(ZipCode.class, id, currentUser.getUserEntityId()
                .getUri());

        HibernateUtils.initializeAndUnproxy(zipCode);
        HibernateUtils.initializeAndUnproxy(zipCode.getCountry());
        HibernateUtils.initializeAndUnproxy(zipCode.getState());
        HibernateUtils.initializeAndUnproxy(zipCode.getCity());
        HibernateUtils.initializeAndUnproxy(zipCode.getVillage());

        prepareVillageIds(zipCode);


        List<Country> countryList = new ArrayList<Country>();
        countryList.add(zipCode.getCountry());
        List<State> stateList = new ArrayList<State>();
        stateList.add(zipCode.getState());
        List<City> cityList = new ArrayList<City>();
        cityList.add(zipCode.getCity());
        ZipCode prevZipCode = null;
        if (zipCode.getApprovalStatus() == ApprovalStatus.UNAPPROVED_MODIFIED
                || zipCode.getApprovalStatus() == ApprovalStatus.WORFLOW_IN_PROGRESS) {
            prevZipCode = (ZipCode) baseMasterService.getLastApprovedEntityByUnapprovedEntityId(zipCode
                    .getEntityId());
            map.put("prevZipCode", prevZipCode);
          map.put("editLink", false);
        } else if (zipCode.getApprovalStatus() == ApprovalStatus.APPROVED_MODIFIED) {
            prevZipCode = (ZipCode) baseMasterService.getLastUnApprovedEntityByApprovedEntityId(zipCode
                    .getEntityId());
            map.put("prevZipCode", prevZipCode);
            map.put("viewLink", false);
        }
        if(prevZipCode!=null){
            HibernateUtils.initializeAndUnproxy(prevZipCode);
            HibernateUtils.initializeAndUnproxy(prevZipCode.getCountry());
            HibernateUtils.initializeAndUnproxy(prevZipCode.getState());
            HibernateUtils.initializeAndUnproxy(prevZipCode.getCity());
            HibernateUtils.initializeAndUnproxy(prevZipCode.getVillage());
        }
        ReasonsActiveInactiveMapping reasonsActiveInactiveMapping = getActInactReasonsForEditGeneric(map,zipCode.getReasonActInactMap());
        zipCode.setReasonActInactMap(reasonsActiveInactiveMapping);
        String masterName = zipCode.getClass().getSimpleName();
        String uniqueValue = zipCode.getZipCode();
        String uniqueParameter = "zipCode";
        getActInactReasMapForEditApproved(map,zipCode,masterName,uniqueParameter,uniqueValue);
        map.put("masterID", masterId);
        map.put("zipCode", zipCode);
        map.put("countryList",countryList);
        map.put("stateList",stateList);
        map.put("cityList",cityList);
        map.put("villageList", zipCode.getVillage());
        map.put("viewable", true);
        map.put("codeViewMode", true);

        if (zipCode.getViewProperties() != null) {
            @SuppressWarnings("unchecked")
            List<String> actions = (ArrayList<String>) zipCode.getViewProperties().get("actions");
            if (actions != null) {
                for (String act : actions) {
                    String actionString = "act" + act;
                    map.put(actionString.replaceAll(" ", ""), false);
                }
            }
        }

        return "zipCode";
    }

    @PreAuthorize("hasAuthority('MAKER_ZIPCODE')")
    @SuppressWarnings({ "null", "rawtypes" })
    @RequestMapping(value = "/populateStateFromCountry/{id}")
    public @ResponseBody
    Map fetchListOfValues(ModelMap map, @PathVariable Long id) {

        Map consolidateMap = new TreeMap<String, String>();
        List<State> stateList = addressService.findAllStateInCountry(id);
        for (State s : stateList) {
            consolidateMap.put(s.getId().toString(), s.getStateName());
        }

        return consolidateMap;
    }

    @PreAuthorize("hasAuthority('MAKER_ZIPCODE')")
    @SuppressWarnings({ "null", "rawtypes" })
    @RequestMapping(value = "/populateCityFromState/{id}")
    public @ResponseBody
    Map fetchListOfcity(ModelMap map, @PathVariable Long id) {
        Map consolidateMap = new HashMap<String, String>();
        List<City> cityList = addressService.findAllCityInState(id);
        for (City c : cityList) {
            consolidateMap.put(c.getId().toString(), c.getCityName());
        }
        // Code to sort the consolidate map_STARTS
        Map sortConsolidateMap =  sortByComparator(consolidateMap);
       
        return sortConsolidateMap;
    }
    
    private static Map<String, String> sortByComparator(Map unsortMap) {

		// Convert Map to List
		List<Map.Entry<String, String>> list = new LinkedList<Map.Entry<String, String>>(unsortMap.entrySet());

		// Sort list with comparator, to compare the Map values
		Collections.sort(list, new Comparator<Map.Entry<String, String>>() {
			
			public int compare(Map.Entry<String, String> o1,Map.Entry<String, String> o2) {
				return (o1.getValue()).compareTo(o2.getValue());
			}
		});

		// Convert sorted map back to a Map
		Map<String, String> sortedMap = new LinkedHashMap<String, String>();
		for (Iterator<Map.Entry<String, String>> it = list.iterator(); it.hasNext();) {
			Map.Entry<String, String> entry = it.next();
			sortedMap.put(entry.getKey(), entry.getValue());
		}
		return sortedMap;
	}

    //Code to sort the consolidate map_ENDS
    
    @PreAuthorize("hasAuthority('MAKER_ZIPCODE')")
    @RequestMapping(value = "/populateCountryFromCity")
    public @ResponseBody
    Long getCountryIdByCity(@RequestParam(value = "cityId") Long cityId, ModelMap map) {
    	Long countryId = null;
		if (cityId != null) {
			countryId = addressService.findCountryIdByCityId(cityId);
		}
		return countryId;
    }
    
    @PreAuthorize("hasAuthority('MAKER_ZIPCODE')")
	@RequestMapping(value = "/populateStateFromCity")
	public @ResponseBody
	Long getStateIdByCityId(@RequestParam(value = "cityId") Long cityId, ModelMap map) {
		Long stateId = null;
		if (cityId != null) {
			stateId = addressService.findStateIdByCityId(cityId);
		}
		return stateId;
	}
    @PreAuthorize("hasAuthority('MAKER_ZIPCODE')")
	@SuppressWarnings({ "null", "rawtypes" })
    @RequestMapping(value = "/populateApprovedStatesFromCountry/{id}")
    public @ResponseBody
    Map populateApprovedStateFromCountry(ModelMap map, @PathVariable Long id) {

        Map consolidateMap = new TreeMap<String, String>();
        List<State> stateList = addressService.findAllApprovedStatesInCountry(id);
        for (State s : stateList) {
            consolidateMap.put(s.getId().toString(), s.getStateName());
        }

        return consolidateMap;
    }

    @PreAuthorize("hasAuthority('MAKER_ZIPCODE')")
    @SuppressWarnings({ "null", "rawtypes" })
    @RequestMapping(value = "/populateApprovedDistrictsFromState/{id}")
    public @ResponseBody
    Map populateApprovedDistrictsFromState(ModelMap map, @PathVariable Long id) {

        Map consolidateMap = new TreeMap<String, String>();
        List<District> districtList = addressService.findAllApprovedDistrictsInState(id);
        for (District s : districtList) {
            consolidateMap.put(s.getId().toString(), s.getDistrictName());
        }

        return consolidateMap;
    }

    @PreAuthorize("hasAuthority('MAKER_ZIPCODE')")
    @SuppressWarnings({ "null", "rawtypes" })
    @RequestMapping(value = "/populateApprovedCitiesFromState/{id}")
    public @ResponseBody
    Map populateApprovedCityFromState(ModelMap map, @PathVariable Long id) {
        Map consolidateMap = new HashMap<String, String>();
        List<City> cityList = addressService.findAllApprovedCitiesInState(id);
        for (City c : cityList) {
            consolidateMap.put(c.getId().toString(), c.getCityName());
        }
        // Code to sort the consolidate map_STARTS
        Map sortConsolidateMap =  sortByComparator(consolidateMap);
       
        return sortConsolidateMap;
    }

    @RequestMapping(value = "/populateVillageFromState/{id}")
    public String populateVillageFromState(ModelMap map,  @PathVariable("id") Long stateId) {
        List<VillageMaster> villageMasterList = findVillageByState(stateId);
        map.put("villageList", villageMasterList);
        return "villageIds";
    }

    private List<VillageMaster> findVillageByState(Long stateId){
        NeutrinoValidator.notNull(stateId);
        NamedQueryExecutor<VillageMaster> villageList1 = new NamedQueryExecutor<VillageMaster>("address.findVillageByStateId")
                .addParameter("stateId", stateId);
        List<VillageMaster> villageList = entityDao.executeQuery(villageList1);

        return villageList;

    }

    private void prepareVillageIds (ZipCode zipCode){
        List<VillageMaster> villages = zipCode.getVillage();

        VillageMaster retrievedVillage = null;
        List<Long> villageIds = new ArrayList<Long>();
        for (VillageMaster village : villages) {
            retrievedVillage = baseMasterService.findById(VillageMaster.class,village.getId());
            villageIds.add(retrievedVillage.getId());
        }
        Long villageIdsArray[] = new Long[villageIds.size()];
        for (int i = 0 ; i < villageIds.size() ; i++) {
            villageIdsArray[i] = villageIds.get(i);
        }

        zipCode.setVillageIds(villageIdsArray);
    }
	
	@RequestMapping(value = "/validatePincodeValue", method = RequestMethod.GET)
	@ResponseBody
    public String validatePincodeValue(@RequestParam("stateId") String stateId, @RequestParam("zipcodeValue") String zipcodeValue) {
				
		int validationFlag = 0;
		if(stateId.equals("") || zipcodeValue.equals("")){
			return "";
		}
		State stateObj = addressService.getStateAttributes(Long.parseLong(stateId));
		if(stateObj!=null){
			validationFlag = addressService.validateCustomPincodeValue(zipcodeValue,stateObj);
		}
		if(validationFlag == -1){
			return "Invalid Pincode";
		}else{
			return "Valid Pincode";
		}
				
	}


}
