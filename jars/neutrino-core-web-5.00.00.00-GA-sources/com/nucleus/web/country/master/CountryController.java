/**
 * Copyright Nucleus Software India Pvt Ltd. All rights reserved.
 */

package com.nucleus.web.country.master;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Collections;
import java.util.Comparator;
import javax.inject.Inject;
import javax.inject.Named;


import com.nucleus.activeInactiveReason.MasterActiveInactiveReasons;
import com.nucleus.activeInactiveReason.ReasonsActiveInactiveMapping;



import com.google.common.collect.Lists;
import com.nucleus.autocomplete.AutocompleteVO;
import com.nucleus.dao.query.NamedQueryExecutor;
import com.nucleus.persistence.EntityDao;
import com.nucleus.persistence.HibernateUtils;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.springframework.beans.factory.annotation.Value;
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

import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.nucleus.address.Country;
import com.nucleus.entity.ApprovalStatus;
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
 *         controlling country CRUD and task allocation work-flow related
 *         operations.
 */
@Transactional
@Controller
@RequestMapping(value = "/Country")
public class CountryController extends BaseController {

    @Inject
    @Named("stringEncryptor")
    private StandardPBEStringEncryptor encryptor;
    @Inject
    @Named("makerCheckerService")
    private MakerCheckerService makerCheckerService;

    @Inject
    @Named("masterXMLDocumentBuilder")
    private CommonFileIOMasterGridLoad commonFileIOMasterGridLoad;

    @Inject
    @Named("baseMasterService")
    private BaseMasterService baseMasterService;

    @Inject
    @Named("userService")
    private UserService userService;


    @Inject
    @Named("countryValidator")
    private Validator countryValidator;

    @Value("${cas.country.flag}")
    public String countryFlag;

    @Inject
    @Named("entityDao")
    protected EntityDao entityDao;


    private static final String masterId = "Country";
    private static final String sortCountryListByName = "countryname";

    @InitBinder("country")
    protected void initBinder(WebDataBinder binder) {
        binder.setValidator(countryValidator);
    }

    /*Method Added to send current Entity Uri for working of comments,activity,history,notes*/
    @ModelAttribute("currentEntityClassName")
    public String getEntityClassName() throws UnsupportedEncodingException {
        String encrypt = "enc_" + encryptor.encrypt(Country.class.getName());
        String returnUri = UriUtils.encodeQueryParam(encrypt, "UTF-8");
        return returnUri;
    }

    /**
     * @param country object containing country name,ISO code,ISD code,nationality
     *                and country group.
     * @return String
     * @throws IOException
     * @description to save country object from view
     */
    @PreAuthorize("hasAuthority('MAKER_COUNTRY')")
    @RequestMapping(value = "/save", method = RequestMethod.POST)
    public String saveCountry(@Validated Country country, BindingResult result, ModelMap map,
                              @RequestParam("createAnotherMaster") boolean createAnotherMaster) {

        BaseLoggers.flowLogger.debug(country.getLogInfo());
        /*
         * Map whose Key Is Table Column Name with whom to validate and Value is
         * The One to be validated.This Map Is Send in the Validator Method
         */
        Country dubplicateCountry = null;
        if(null!=country.getId()){
            dubplicateCountry = entityDao.find(Country.class,country.getId());
            if(null != dubplicateCountry.getEntityLifeCycleData()){
                country.setEntityLifeCycleData(dubplicateCountry.getEntityLifeCycleData());
            }
            if(null != dubplicateCountry.getMasterLifeCycleData()){
                country.setMasterLifeCycleData(dubplicateCountry.getMasterLifeCycleData());
            }
        }

        Boolean countryNamee = false;
        String isoCode3 = "";
        String[] countries = Locale.getISOCountries();
        String language = "en";
        for (String eachCountry : countries) {
            Locale locale = new Locale(language, eachCountry);
            if (locale.getDisplayCountry().equalsIgnoreCase(country.getCountryName())) {
                isoCode3 = locale.getISO3Country();
                break;
            }
        }
        country.setCountryISOCode(isoCode3);
        NamedQueryExecutor<String> executor = new NamedQueryExecutor<>("country.findCountryName");
        executor.addParameter("countryName", country.getCountryName())
                .addParameter("approvalStatusList", ApprovalStatus.HISTORY_RECORD_STATUS_LIST);
        List<String> countryName = entityDao.executeQuery(executor);
        if (!countryName.isEmpty()) {
            countryNamee = true;
        }
        map.put("countryNamee", countryNamee);

        Map<String, Object> validateMap = new HashMap<String, Object>();
        validateMap.put("countryName", country.getCountryName());
        validateMap.put("nationality", country.getNationality());

        /*
         * Code to check as if any existing(or new) record is being modified(or created) into another existing record
         */
        List<String> colNameList = checkValidationForDuplicates(country, Country.class, validateMap);
        if (result.hasErrors() || (colNameList != null && !colNameList.isEmpty())) {
            // getActInactReasMapForEdit(map,tehsil);
            String masterName = country.getClass().getSimpleName();
            String uniqueValue = null;
            String uniqueParameter = null;
            if (null != country.getId()) {
                //Country coun = baseMasterService.findById(Country.class, country.getId());
                uniqueValue = dubplicateCountry.getCountryName();
                uniqueParameter = "countryName";
                getActInactReasMapForEditApproved(map, country, masterName, uniqueParameter, uniqueValue);
            }
         else {
                ReasonsActiveInactiveMapping reasActInactMap = getActInactReasMapForCreate(map);
                country.setReasonActInactMap(reasActInactMap);
        }

        map.put("edit", true);
        ReasonsActiveInactiveMapping reasonsActiveInactiveMapping = getActInactReasonsForEditGeneric(map,country.getReasonActInactMap());
        country.setReasonActInactMap(reasonsActiveInactiveMapping);
        map.put("viewable", false);
        map.put("country", country);
        map.put("masterID", masterId);
        if (colNameList != null && !colNameList.isEmpty() && colNameList.contains("countryName")) {
            result.rejectValue("countryName", "label.countryName.validation.exists");
        }
        if (colNameList != null && !colNameList.isEmpty() && colNameList.contains("nationality")) {
            result.rejectValue("nationality", "label.nationality.validation.exists");
        }

        return "country";
    }

        boolean eventResult = executeMasterEvent(country,"contextObjectCountry",map);
        if(!eventResult){
            // getActInactReasMapForEdit(map,country);
            String masterName = country.getClass().getSimpleName();
            String uniqueParameter = "countryName";
            String uniqueValue = country.getCountryName();
            getActInactReasMapForEditApproved(map,country,masterName,uniqueParameter,uniqueValue);
            map.put("viewable" , false);
            map.put("edit" , true);
            ReasonsActiveInactiveMapping reasonsActiveInactiveMapping = getActInactReasonsForEditGeneric(map,country.getReasonActInactMap());
            country.setReasonActInactMap(reasonsActiveInactiveMapping);
            map.put("country", country);
            map.put("masterID", masterId);
            map.put("viewable" , false);
            map.put("activeFlag",country.isActiveFlag());
            return "country";
        }
        /*
         * To check if referenced entity id is null,set entity as null
         */
        if (country.getCountryGroup() == null || country.getCountryGroup().getId() == null) {
            country.setCountryGroup(null);
        }
        if (country.getRegion() == null || country.getRegion().getId() == null) {
            country.setRegion(null);
        }

        // we need to get below logged in user from session
        User user = getUserDetails().getUserReference();
        if (user != null) {
            ReasonsActiveInactiveMapping reasonsActiveInactiveMapping = country.getReasonActInactMap();
            if(reasonsActiveInactiveMapping != null){
                saveActInactReasonForMaster(reasonsActiveInactiveMapping,country);
            }
            country.setReasonActInactMap(reasonsActiveInactiveMapping);
            makerCheckerService.masterEntityChangedByUser(country, user);
        }

        if (createAnotherMaster) {
            ReasonsActiveInactiveMapping reasActInactMap = getActInactReasMapForCreate(map);
            Country countryForCreateAnother = new Country();
            countryForCreateAnother.setReasonActInactMap(reasActInactMap);
            map.put("country", countryForCreateAnother);
            map.put("masterID", masterId);
            return "country";
        }
        map.put("masterID", masterId);
        return "redirect:/app/grid/Country/Country/loadColumnConfig";

    }

    /**
     * @param record
     *            id for edit
     * @return void
     * @throws
     * @description to create country
     */

    @PreAuthorize("hasAuthority('MAKER_COUNTRY')")
    @RequestMapping(value = "/create")
    public String createCountry(ModelMap map) {
        Country countryObj = getCountryMarkedDefault();
        if(null != countryObj){
            map.put("defaultMarkedCountry" , countryObj);
        }
        ReasonsActiveInactiveMapping reasActInactMap = getActInactReasMapForCreate(map);
        Country country= new Country();
        country.setReasonActInactMap(reasActInactMap);
        map.put("reasonsActiveInactiveMapping",country.getReasonActInactMap());
        map.put("country", country);
        map.put("masterID", masterId);
        return "country";
    }

    /**
     * @param record
     *            id for edit
     * @return void
     * @throws
     * @description to edit country
     */
    @PreAuthorize("hasAuthority('MAKER_COUNTRY')")
    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/edit/{id}")
    public String editCountry(@PathVariable("id") Long id, ModelMap map) {
        UserInfo currentUser = getUserDetails();
        Country country = baseMasterService.getMasterEntityWithActionsById(Country.class, id, currentUser.getUserEntityId()
                .getUri());
        String flagBasedISOCode = null;
        if (countryFlag.equals("2")) {
            String[] countries = Locale.getISOCountries();
            String language = "en";
            for (String eachCountry : countries) {
                Locale locale = new Locale(language, eachCountry);
                if (locale.getDisplayCountry().equalsIgnoreCase(country.getCountryName())) {
                    flagBasedISOCode=eachCountry;
                    break;

                }
            }
        }
        else{
            flagBasedISOCode = country.getCountryISOCode();
        }


        if (country.getApprovalStatus() == ApprovalStatus.UNAPPROVED_MODIFIED) {
            Country prevCountry = (Country) baseMasterService.getLastApprovedEntityByUnapprovedEntityId(country
                    .getEntityId());
            map.put("prevCountry", prevCountry);
            map.put("editLink", false);
        }
        // check added as per CAS-22713
        if (country.getApprovalStatus() == ApprovalStatus.APPROVED
                || country.getApprovalStatus() == ApprovalStatus.UNAPPROVED_MODIFIED
                || country.getApprovalStatus() == ApprovalStatus.APPROVED_MODIFIED) {
            map.put("disableIsoCode", true);
        }
        ReasonsActiveInactiveMapping reasonsActiveInactiveMapping = getActInactReasonsForEditGeneric(map,country.getReasonActInactMap());
        country.setReasonActInactMap(reasonsActiveInactiveMapping);
        String masterName = country.getClass().getSimpleName();
        String uniqueParameter = "countryName";
        String uniqueValue = country.getCountryName();
        getActInactReasMapForEditApproved(map,country,masterName,uniqueParameter,uniqueValue);
        map.put("viewable" ,false);
        Country countryObj = getCountryMarkedDefault();
        if(null != countryObj){
            map.put("defaultMarkedCountry" , countryObj);
        }
        map.put("country", country);
        map.put("flagBasedISOCode", flagBasedISOCode);
        map.put("edit", true);
        map.put("masterID", masterId);
        ArrayList<String> actions = (ArrayList<String>) country.getViewProperties().get("actions");
        if (actions != null) {
            for (String act : actions) {
                map.put("act" + act, false);
            }
        }

        return "country";
    }

    /**
     * @description to save and send for approval * @return String
     * @throws IOException
     * @description to save and send for approval country object from view
     */
    @PreAuthorize("hasAuthority('MAKER_COUNTRY')")
    @RequestMapping(value = "/saveAndSendForApproval", method = RequestMethod.POST)
    public String saveAndSendForApproval(@Validated Country country, BindingResult result, ModelMap map,
                                         @RequestParam("createAnotherMaster") boolean createAnotherMaster) {

        BaseLoggers.flowLogger.debug(country.getLogInfo());
        /*
         * Map whose Key Is Table Column Name with whom to validate and Value is
         * The One to be validated.This Map Is Send in the Validator Method
         */
        Country dubplicateCountry = null;
        if(null!=country.getId()){
            dubplicateCountry = entityDao.find(Country.class,country.getId());
            if(null != dubplicateCountry.getEntityLifeCycleData()){
                country.setEntityLifeCycleData(dubplicateCountry.getEntityLifeCycleData());
            }
            if(null != dubplicateCountry.getMasterLifeCycleData()){
                country.setMasterLifeCycleData(dubplicateCountry.getMasterLifeCycleData());
            }
        }

        Boolean countryNamee = false;
        String isoCode3 = "";
        String[] countries = Locale.getISOCountries();
        String language = "en";
        for (String eachCountry : countries) {
            Locale locale = new Locale(language, eachCountry);
            if (locale.getDisplayCountry().equalsIgnoreCase(country.getCountryName())) {
                isoCode3 = locale.getISO3Country();
                break;
            }
        }
        country.setCountryISOCode(isoCode3);
        NamedQueryExecutor<String> executor = new NamedQueryExecutor<>("country.findCountryName");
        executor.addParameter("countryName", country.getCountryName())
                .addParameter("approvalStatusList", ApprovalStatus.HISTORY_RECORD_STATUS_LIST);;
        List <String> countryName = entityDao.executeQuery(executor);
        if(!countryName.isEmpty()){
            countryNamee = true;
        }
        map.put("countryNamee", countryNamee);

        Map<String, Object> validateMap = new HashMap<String, Object>();
        validateMap.put("countryName", country.getCountryName());
        validateMap.put("nationality", country.getNationality());

        /*
         * Code to check as if any existing(or new) record is being modified(or created) into another existing record
         */
        List<String> colNameList = checkValidationForDuplicates(country, Country.class, validateMap);
        if (result.hasErrors() || (colNameList != null && !colNameList.isEmpty())) {
            String masterName = country.getClass().getSimpleName();
            String uniqueValue = null;
            String uniqueParameter = null;
            if (null != country.getId()) {
                //Country coun = baseMasterService.findById(Country.class, country.getId());
                uniqueValue = dubplicateCountry.getCountryName();
                uniqueParameter = "countryName";
                getActInactReasMapForEditApproved(map, country, masterName, uniqueParameter, uniqueValue);
            }
            else {
                ReasonsActiveInactiveMapping reasActInactMap = getActInactReasMapForCreate(map);
                country.setReasonActInactMap(reasActInactMap);
            }
            map.put("viewable" , false);
            map.put("edit" , true);
            ReasonsActiveInactiveMapping reasonsActiveInactiveMapping = getActInactReasonsForEditGeneric(map,country.getReasonActInactMap());
            country.setReasonActInactMap(reasonsActiveInactiveMapping);
            map.put("country", country);
            map.put("masterID", masterId);
            if (colNameList != null && !colNameList.isEmpty() && colNameList.contains("countryName")) {
                result.rejectValue("countryName", "label.countryName.validation.exists");
            }
            if (colNameList != null && !colNameList.isEmpty() && colNameList.contains("nationality")) {
                result.rejectValue("nationality", "label.nationality.validation.exists");
            }

            return "country";
        }

        boolean eventResult = executeMasterEvent(country,"contextObjectCountry",map);
        if(!eventResult){
            // getActInactReasMapForEdit(map,country);
            String masterName = country.getClass().getSimpleName();
            String uniqueParameter = "countryName";
            String uniqueValue = country.getCountryName();
            getActInactReasMapForEditApproved(map,country,masterName,uniqueParameter,uniqueValue);
            map.put("edit" , true);
            ReasonsActiveInactiveMapping reasonsActiveInactiveMapping = getActInactReasonsForEditGeneric(map,country.getReasonActInactMap());
            country.setReasonActInactMap(reasonsActiveInactiveMapping);
            map.put("viewable" , false);
            map.put("country", country);
            map.put("masterID", masterId);
            map.put("activeFlag",country.isActiveFlag());
            return "country";
        }

        /*
         * To check if referenced entity id is null,set entity as null
         */
        if (country.getCountryGroup() == null || country.getCountryGroup().getId() == null) {
            country.setCountryGroup(null);
        }
        if (country.getRegion() == null || country.getRegion().getId() == null) {
            country.setRegion(null);
        }

        // we need to get below logged in user from session
        User user = getUserDetails().getUserReference();
        if (user != null) {
            ReasonsActiveInactiveMapping reasonsActiveInactiveMapping = country.getReasonActInactMap();
            if(reasonsActiveInactiveMapping != null){
                saveActInactReasonForMaster(reasonsActiveInactiveMapping,country);
            }
            country.setReasonActInactMap(reasonsActiveInactiveMapping);
            makerCheckerService.saveAndSendForApproval(country, user);
        }

        if (createAnotherMaster) {
            ReasonsActiveInactiveMapping reasActInactMap = getActInactReasMapForCreate(map);
            Country countryForCreateAnother = new Country();
            countryForCreateAnother.setReasonActInactMap(reasActInactMap);
            map.put("country", countryForCreateAnother);
            map.put("masterID", masterId);
            return "country";
        }
        map.put("masterID", masterId);
        return "redirect:/app/grid/Country/Country/loadColumnConfig";

    }

    /**
     * @param record
     *            id for view
     * @return void
     * @throws
     * @description to view country
     */
    @SuppressWarnings("unchecked")
    @PreAuthorize("hasAuthority('VIEW_COUNTRY') or hasAuthority('MAKER_COUNTRY') or hasAuthority('CHECKER_COUNTRY')")
    @RequestMapping(value = "/view/{id}", method = RequestMethod.GET)
    public String viewCountry(@PathVariable("id") Long id, ModelMap map) {
        UserInfo currentUser = getUserDetails();
        Country country = baseMasterService.getMasterEntityWithActionsById(Country.class, id, currentUser.getUserEntityId()
                .getUri());
        String flagBasedISOCode = null;
        if (countryFlag.equals("2")) {

            String[] countries = Locale.getISOCountries();
            String language = "en";
            for (String eachCountry : countries) {
                Locale locale = new Locale(language, eachCountry);
                if (locale.getDisplayCountry().equalsIgnoreCase(country.getCountryName())) {
                    flagBasedISOCode=eachCountry;
                    break;

                }
            }
        }
        else{
            flagBasedISOCode = country.getCountryISOCode();
        }
        if (country.getApprovalStatus() == ApprovalStatus.UNAPPROVED_MODIFIED
                || country.getApprovalStatus() == ApprovalStatus.WORFLOW_IN_PROGRESS) {
            Country prevCountry = (Country) baseMasterService.getLastApprovedEntityByUnapprovedEntityId(country
                    .getEntityId());
            map.put("prevCountry", prevCountry);
            map.put("editLink", false);
        } else if (country.getApprovalStatus() == ApprovalStatus.APPROVED_MODIFIED) {
            Country prevCountry = (Country) baseMasterService.getLastUnApprovedEntityByApprovedEntityId(country
                    .getEntityId());
            map.put("prevCountry", prevCountry);
            map.put("viewLink", false);
        }
        Country countryObj = getCountryMarkedDefault();
        if(null != countryObj){
            map.put("defaultMarkedCountry" , countryObj);
        }
        ReasonsActiveInactiveMapping reasonsActiveInactiveMapping = getActInactReasonsForEditGeneric(map,country.getReasonActInactMap());
        country.setReasonActInactMap(reasonsActiveInactiveMapping);
        // getActInactReasMapForEdit(map,tehsil);
        String masterName = country.getClass().getSimpleName();
        String uniqueParameter = "countryName";
        String uniqueValue = country.getCountryName();
        getActInactReasMapForEditApproved(map,country,masterName,uniqueParameter,uniqueValue);
        map.put("country", country);
        map.put("flagBasedISOCode", flagBasedISOCode);
        map.put("masterID", masterId);
        map.put("viewable", true);
        if (country.getViewProperties() != null) {
            ArrayList<String> actions = (ArrayList<String>) country.getViewProperties().get("actions");
            if (actions != null) {
                for (String act : actions) {
                    String actionString = "act" + act;
                    map.put(actionString.replaceAll(" ", ""), false);
                }
            }
        }

        return "country";
    }

    /*@RequestMapping(value = "/isoCode/{countryName}", method = RequestMethod.GET)
    public @ResponseBody String getIsoCode(@PathVariable String countryName){

        Locale[] locales = Locale.getAvailableLocales();
        for (Locale locale : locales) {
            if (locale.getDisplayCountry().equalsIgnoreCase(countryName)) {
                return locale.getISO3Country();

            }
        }
        return "";
    }*/

    @PreAuthorize("hasAuthority('MAKER_COUNTRY')")
    @RequestMapping(value = "/getOtherDetailsByCountryName/{countryName}", method = RequestMethod.GET)
    public
    @ResponseBody
    List<String> getIsoCode(@PathVariable String countryName) {
        Locale currentLocale = null;
        List<String> otherDetails = new ArrayList<>();
        String isoCode3 = "";
        String isoCode = "";
        int isdCode = 0;

        String[] countries = Locale.getISOCountries();
        String language = "en";
        String is02="";
        for (String eachCountry : countries) {
            Locale locale = new Locale(language, eachCountry);
            if (locale.getDisplayCountry().equalsIgnoreCase(countryName)) {
                isoCode3 = locale.getISO3Country();
                is02=eachCountry;
                currentLocale = locale;
                break;

            }
        }
        if (countryFlag.equals("2")) {
            isoCode = is02;
        }
        else{
            isoCode = isoCode3;
        }
        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
        if ((phoneUtil != null) && (currentLocale != null)) {
            isdCode = phoneUtil.getCountryCodeForRegion(currentLocale.getCountry());
        }

        otherDetails.add(isoCode);
        otherDetails.add("+" + isdCode);
        return otherDetails;
    }



    @RequestMapping(value = "/getCountryName")
    @ResponseBody
    public AutocompleteVO getCountryName(ModelMap map, @RequestParam String value,
                                         @RequestParam String itemVal, @RequestParam String searchCol, @RequestParam String className,
                                         @RequestParam Boolean loadApprovedEntityFlag, @RequestParam String i_label, @RequestParam String idCurr,
                                         @RequestParam String content_id, @RequestParam int page, @RequestParam(required = false) String itemsList,
                                         @RequestParam(required = false) Boolean strictSearchOnitemsList) {
        AutocompleteVO autocompleteVO = new AutocompleteVO();
        String[] searchColumnList = searchCol.split(" ");
        if (strictSearchOnitemsList == null) {
            strictSearchOnitemsList = false;
        }
        if (loadApprovedEntityFlag == null) {
            loadApprovedEntityFlag = false;
        }
        List<Map<String,String>> list = searchCountryNames(itemVal, searchColumnList, value, loadApprovedEntityFlag, itemsList, strictSearchOnitemsList, page);
        if (!list.isEmpty()) {
            Map listMap = (Map) list.get(list.size() - 1);
            int sizeList1 = ((Integer) listMap.get("size")).intValue();
            list.remove(list.size() - 1);
            //map.put("size", Integer.valueOf(sizeList1));
            //map.put("page", Integer.valueOf(page));
            autocompleteVO.setS(Integer.valueOf(sizeList1));
            autocompleteVO.setP(Integer.valueOf(page));
        }
        String[] sclHeading=new String[searchColumnList.length];

        for(int i=0;i<searchColumnList.length;i++)
        {
            searchColumnList[i]=searchColumnList[i].replace(".", "");
            sclHeading[i]=messageSource.getMessage("label.autoComplete."+searchColumnList[i],null, Locale.getDefault());
        }
        if (i_label != null && i_label.contains(".")) {
            i_label = i_label.replace(".", "");
        }

        //map.put("data", list);

        autocompleteVO.setD(Lists.newArrayList(list));

        if (idCurr != null && idCurr.trim().length() > 0) {
            idCurr = idCurr.replaceAll("[^\\w\\s\\-_]", "");
        }

        // map.put("idCurr", idCurr);
        //map.put("i_label", i_label);
        //map.put("content_id", content_id);
        autocompleteVO.setIc(idCurr);
        autocompleteVO.setIl(i_label);
        autocompleteVO.setCi(content_id);
        autocompleteVO.setIv(itemVal);
        autocompleteVO.setScl(searchColumnList);
        autocompleteVO.setColh(sclHeading);
        return  autocompleteVO;
        //return "autocomplete";
    }

    private void sortCountryNames(List<Map<String, String>> filterList, final String countryName) {

        Comparator<Map<String, String>> mapComparator = new Comparator<Map<String, String>>() {
            @SuppressWarnings({ "unchecked", "rawtypes" })
            public int compare(Map<String, String> m1, Map<String, String> m2) {

                String lhs = m1.get(countryName);
                String rhs = m2.get(countryName);
                if (lhs instanceof Comparable && rhs instanceof Comparable) {
                    return ((Comparable) lhs).compareTo(rhs);
                }
                return 0;
            }
        };

        Collections.sort(filterList, mapComparator);
    }

    public List<Map<String,String>> searchCountryNames(String itemVal,
                                                       String[] searchColumnList, String value,
                                                       Boolean loadApprovedEntityFlag, String itemsList,
                                                       Boolean strictSearchOnitemsList, int page) {

        String[] isocodes = Locale.getISOCountries();
        String language = "en";
        List<Map<String,String>> newList = new ArrayList<Map<String,String>>();

        Map<String,String> newMap = new HashMap<>();
        for (String eachCountry : isocodes) {
            newMap = new HashMap<>();
            Locale locale = new Locale(language, eachCountry);

            newMap.put(sortCountryListByName,locale.getDisplayCountry());
            newMap.put("id",locale.getDisplayCountry());
            newList.add(newMap);
        }

        List<Map<String,String>> filterList= new ArrayList<>();
        if(!newList.isEmpty()) {
            if(value.startsWith("%%")){
                filterList.addAll(newList);
            }else{
                for (Map<String,String> product : newList){
                    String countryname = (String)product.get(sortCountryListByName);
                    if((countryname!=null && countryname.toLowerCase().contains(value.toLowerCase())))
                    {
                        filterList.add(product);
                    }
                }
            }
        }


        List<Map<String,String>> newList1 = new ArrayList<>();
        if(!filterList.isEmpty()){
            sortCountryNames(filterList,sortCountryListByName);
            int startIndex = page*3;
            int endIndex = (page*3)+2;
            if(endIndex < filterList.size()){
                newList1.addAll(filterList.subList(startIndex, (endIndex + 1)));
            }
            else {
                endIndex = filterList.size()-1;
                newList1.addAll(filterList.subList(startIndex, endIndex));
                newList1.add(filterList.get(endIndex));
            }
        }
        HashMap sizeMap = new HashMap();
        sizeMap.put("size", filterList.size());
        newList1.add(sizeMap);
        return newList1;

    }

    public Country getCountryMarkedDefault() {
        List<Integer> statusList = new ArrayList<Integer>();
        statusList.addAll(ApprovalStatus.APPROVED_RECORD_STATUS_LIST_EXCLUDING_APPROVED_DELETED);
        NamedQueryExecutor<Country> countryExecutor = new NamedQueryExecutor<Country>(
                "country.defaultMarkedCountry").addParameter(
                "approvalStatus", statusList);
        List<Country> defaultCountryList = entityDao.executeQuery(countryExecutor);
        if (null != defaultCountryList) {
            if(!defaultCountryList.isEmpty()){
                return defaultCountryList.get(0);
            }
        }
        return null;
    }


}
