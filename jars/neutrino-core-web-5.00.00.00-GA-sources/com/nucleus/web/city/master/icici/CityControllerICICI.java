/**
 * Copyright Nucleus Software India Pvt Ltd. All rights reserved.
 */
package com.nucleus.web.city.master.icici;

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

import com.nucleus.address.City;
import com.nucleus.address.CityType;
import com.nucleus.address.Country;
import com.nucleus.address.LocationType;
import com.nucleus.address.State;
import com.nucleus.core.genericparameter.service.GenericParameterServiceImpl;
import com.nucleus.entity.ApprovalStatus;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.makerchecker.MakerCheckerService;
import com.nucleus.master.BaseMasterService;
import com.nucleus.user.User;
import com.nucleus.user.UserInfo;
import com.nucleus.web.city.master.CityValidator;
import com.nucleus.web.common.controller.BaseController;

/**
 * @author Nucleus Software India Pvt Ltd This file is being used for
 *         controlling city CRUD and task allocation work-flow related
 *         operations.
 */
@Transactional
@Controller
@RequestMapping(value = "/City/ICICI")
public class CityControllerICICI extends BaseController {

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
    @Named("stringEncryptor")
    private StandardPBEStringEncryptor  encryptor;

    @InitBinder
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

        BaseLoggers.flowLogger.debug(city.getLogInfo());
        /*
         * Map whoes Key Is Table Column Name with whom to validate and Value is
         * The One to be validated.This Map Is Send in the Validator Method
         */
        Map<String, Object> validateMap = new HashMap<String, Object>();
        validateMap.put("cityCode", city.getCityCode());

        List<CityType> cityCategorization = genericParameterServiceImpl.retrieveTypes(CityType.class);
        List<LocationType> locationType = genericParameterServiceImpl.retrieveTypes(LocationType.class);

        /*
         * Code to check as if any existing(or new) record is being modified(or created) into another existing record
         */
        List<String> colNameList = checkValidationForDuplicates(city, City.class, validateMap);
        if (result.hasErrors() || (colNameList != null && colNameList.size() > 0)) {
            map.put("city", city);
            map.put("masterID", masterId);
           /* List<Country> countries = baseMasterService.getLastApprovedEntities(Country.class);
            List<State> states = baseMasterService.getLastApprovedEntities(State.class);
            map.put("countriesList", countries);
            map.put("statesList", states);*/
            map.put("cityCategorizationList", cityCategorization);
            map.put("locationTypeList", locationType);
            /*
             * if List Contains Any Duplicate Values Column Names, Then set them
             * in result
             */
            if (colNameList != null && colNameList.size() > 0) {
                for (String c : colNameList) {
                    result.rejectValue(c, "label." + c + ".validation.exists");
                }
            }
            return "cityICICI";
        }
        /*
         * To check if referenced entity id is null,set entity as null
         */
        if (city.getState() == null || city.getState().getId() == null) {
            city.setState(null);
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
        // we need to get below logged in user from session
        User user = getUserDetails().getUserReference();
        if (user != null) {
            makerCheckerService.masterEntityChangedByUser(city, user);
        }
        if (createAnotherMaster) {
            map.put("city", new City());
            map.put("masterID", masterId);
            map.put("cityCategorizationList", cityCategorization);
            map.put("locationTypeList", locationType);
            return "cityICICI";
        }
        map.put("masterID", masterId);
        return "redirect:/app/grid/City/City/loadColumnConfig";

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
        map.put("city", new City());
        /*List<Country> countries = baseMasterService.getLastApprovedEntities(Country.class);
        List<State> states = baseMasterService.getLastApprovedEntities(State.class);*/
        List<CityType> cityCategorization = genericParameterServiceImpl.retrieveTypes(CityType.class);
        List<LocationType> locationType = genericParameterServiceImpl.retrieveTypes(LocationType.class);
       /* map.put("countriesList", countries);
        map.put("statesList", states);*/
        map.put("cityCategorizationList", cityCategorization);
        map.put("locationTypeList", locationType);
        map.put("masterID", masterId);
        return "cityICICI";
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
        /*List<Country> countries = baseMasterService.getLastApprovedEntities(Country.class);
        List<State> states = baseMasterService.getLastApprovedEntities(State.class);*/
        List<CityType> cityCategorization = genericParameterServiceImpl.retrieveTypes(CityType.class);
        List<LocationType> locationType = genericParameterServiceImpl.retrieveTypes(LocationType.class);
       /* map.put("countriesList", countries);
        map.put("statesList", states);*/
        map.put("cityCategorizationList", cityCategorization);
        map.put("locationTypeList", locationType);
        map.put("city", city);
        map.put("edit", true);
        map.put("masterID", masterId);
        return "cityICICI";
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

        BaseLoggers.flowLogger.debug(city.getLogInfo());
        /*
         * Map whoes Key Is Table Column Name with whom to validate and Value is
         * The One to be validated.This Map Is Send in the Validator Method
         */
        Map<String, Object> validateMap = new HashMap<String, Object>();
        validateMap.put("cityCode", city.getCityCode());

        List<CityType> cityCategorization = genericParameterServiceImpl.retrieveTypes(CityType.class);
        List<LocationType> locationType = genericParameterServiceImpl.retrieveTypes(LocationType.class);

        /*
         * Code to check as if any existing(or new) record is being modified(or created) into another existing record
         */
        List<String> colNameList = checkValidationForDuplicates(city, City.class, validateMap);
        if (result.hasErrors() || (colNameList != null && colNameList.size() > 0 )) {
            map.put("city", city);
            map.put("masterID", masterId);
            /*List<Country> countries = baseMasterService.getLastApprovedEntities(Country.class);
            List<State> states = baseMasterService.getLastApprovedEntities(State.class);
            map.put("countriesList", countries);
            map.put("statesList", states);*/
            map.put("cityCategorizationList", cityCategorization);
            map.put("locationTypeList", locationType);
            /*
             * if List Contains Any Duplicate Values Column Names, Then set them
             * in result
             */
            if (colNameList != null && colNameList.size() > 0) {
                for (String c : colNameList) {
                    result.rejectValue(c, "label." + c + ".validation.exists");
                }
            }
            return "cityICICI";
        }
        if (city.getState() == null || city.getState().getId() == null) {
            city.setState(null);
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
        // we need to get below logged in user from session
        User user = getUserDetails().getUserReference();
        if (user != null) {
            makerCheckerService.saveAndSendForApproval(city, user);
        }
        if (createAnotherMaster) {
            map.put("city", new City());
            map.put("masterID", masterId);
            map.put("cityCategorizationList", cityCategorization);
            map.put("locationTypeList", locationType);
            return "cityICICI";
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
       /* List<Country> countries = baseMasterService.getLastApprovedEntities(Country.class);
        List<State> states = baseMasterService.getLastApprovedEntities(State.class);*/
        city = baseMasterService.getMasterEntityById(City.class, id);
        List<CityType> cityCategorization = genericParameterServiceImpl.retrieveTypes(CityType.class);
        List<LocationType> locationType = genericParameterServiceImpl.retrieveTypes(LocationType.class);
        /*map.put("countriesList", countries);
        map.put("statesList", states);
*/
        map.put("cityCategorizationList", cityCategorization);
        map.put("locationTypeList", locationType);

        map.put("city", city);
        map.put("masterID", masterId);
        map.put("viewable", true);

        if (city.getViewProperties() != null) {
            @SuppressWarnings("unchecked")
            ArrayList<String> actions = (ArrayList<String>) city.getViewProperties().get("actions");
            if (actions != null) {
                for (String act : actions) {
                    String actionString = "act" + act;
                    map.put(actionString.replaceAll(" ", ""), false);
                }
            }
        }
        return "cityICICI";
    }

}
