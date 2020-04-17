/**
 * Copyright Nucleus Software India Pvt Ltd. All rights reserved.
 */
package com.nucleus.web.currency.master;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

import com.nucleus.activeInactiveReason.MasterActiveInactiveReasons;
import com.nucleus.activeInactiveReason.ReasonsActiveInactiveMapping;
import com.nucleus.persistence.HibernateUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.Hibernate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.nucleus.currency.ConversionRate;
import com.nucleus.currency.Currency;
import com.nucleus.entity.ApprovalStatus;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.makerchecker.MakerCheckerService;
import com.nucleus.master.BaseMasterService;
import com.nucleus.money.MoneyService;
import com.nucleus.persistence.EntityDao;
import com.nucleus.user.User;
import com.nucleus.user.UserInfo;
import com.nucleus.web.common.controller.BaseController;
import com.nucleus.web.locale.LocaleUtils;

/**
 * @author Nucleus Software India Pvt Ltd This field is being used for
 *         controlling currency CRUD and task allocation work-flow related
 *         operations.
 */
@Transactional
@Controller
@RequestMapping(value = "/Currency")
public class CurrencyController extends BaseController{
    
    @Inject
    @Named("makerCheckerService")
    private MakerCheckerService makerCheckerService;

    @Inject
    @Named("baseMasterService")
    private BaseMasterService   baseMasterService;
    
    @Inject
    @Named("entityDao")
    protected EntityDao                  entityDao;
    
    @Inject
    @Named("moneyService")
    private MoneyService moneyService;


    private static final String                             masterId   = "Currency";
    
    @InitBinder("currency")
    protected void initBinder(WebDataBinder binder) {
        binder.setValidator(new CurrencyValidator());
    }
    
    @PreAuthorize("hasAuthority('MAKER_CURRENCY')")
    @RequestMapping(value = "/create")
    public String createCurrency(ModelMap map) {
        return initializeCurrency(map);
    }
    
    /**
     * @param Currency
     *            object containing CustomerCategoryCode and
     *            CustomerCategoryDescription.
     * @return String
     * @throws IOException
     * @description to save Currency object from view
     */
    @PreAuthorize("hasAuthority('MAKER_CURRENCY')")
    @RequestMapping(value = "/save", method = RequestMethod.POST)
    public String saveCurrency(@Validated Currency currency, BindingResult result, ModelMap map,
            @RequestParam("createAnotherMaster") boolean createAnotherMaster) {
        BaseLoggers.flowLogger.debug("Saving Currency Details-->"+currency.getLogInfo());
        /*
         * Map whose Key Is Table Column Name with whom to validate and Value is
         * The One to be validated.This Map Is Send in the Validator Method
         */

        Currency dubplicateCurrency =null;
        if(null!=currency.getId()){
            dubplicateCurrency = entityDao.find(Currency.class,currency.getId());
            if(null != dubplicateCurrency.getEntityLifeCycleData()){
                currency.setEntityLifeCycleData(dubplicateCurrency.getEntityLifeCycleData());
            }
            if(null != dubplicateCurrency.getMasterLifeCycleData()){
                currency.setMasterLifeCycleData(dubplicateCurrency.getMasterLifeCycleData());
            }
        }

        Map<String, Object> validateMap = new HashMap<String, Object>();
        Boolean isCurrencyISOCodeValid=validateCurrencyISOCode(currency.getIsoCode());		
        validateMap.put("isoCode", currency.getIsoCode());
        validateMap.put("currencyName", currency.getCurrencyName());

        /*
         * Code to check as if any existing(or new) record is being modified(or created) into another existing record
         */

        List<String> colNameList = checkValidationForDuplicates(currency, Currency.class, validateMap);

        boolean isValidLocale = LocaleUtils.isValidLocale(currency.getLocale().toString());

        List<Date> effectiveFromList = new ArrayList<>();
        List<ConversionRate> conversionRateList = new ArrayList<ConversionRate>();
        boolean duplicateConvRateflag = false;
        
        for (int i = 0; i < currency.getConversionRateList().size(); i++) {
        	ConversionRate conversionrate = currency.getConversionRateList().get(i);
        	Date effectiveFrom = conversionrate.getEffectiveFrom();

            if (!(effectiveFromList.contains(effectiveFrom))) {

            	effectiveFromList.add(effectiveFrom);
            	duplicateConvRateflag = false;

            } else {
                result.rejectValue("conversionRateList[" + i + "].effectiveFrom",
                        "label.DuplicateConversionRate");
                duplicateConvRateflag = true;
            }
            if (!conversionrate.isEmpty()) {
            	conversionRateList.add(conversionrate);
            }
        }        
        currency.setConversionRateList(conversionRateList);
        
        if (result.hasErrors() || (colNameList != null && colNameList.size() > 0)  || !isValidLocale || duplicateConvRateflag || !isCurrencyISOCodeValid) {
            if(currency.getId() != null) {
                Currency c = baseMasterService.getMasterEntityById(Currency.class, currency.getId());
                if (!(ApprovalStatus.UNAPPROVED_ADDED == c.getApprovalStatus() || ApprovalStatus.CLONED == c.getApprovalStatus())) {
                    map.put("codeViewMode", true);
                }
            }
            String masterName = currency.getClass().getSimpleName();
            String uniqueValue = null;
            String uniqueParameter = null;
            if (null != currency.getId()) {
                //Currency curr = baseMasterService.findById(Currency.class, currency.getId());
                uniqueValue = dubplicateCurrency.getCurrencyName();
                uniqueParameter = "currencyName";
                getActInactReasMapForEditApproved(map, currency, masterName, uniqueParameter, uniqueValue);
            }
            else {
                ReasonsActiveInactiveMapping reasActInactMap = getActInactReasMapForCreate(map);
                currency.setReasonActInactMap(reasActInactMap);
            }
            ReasonsActiveInactiveMapping reasonsActiveInactiveMapping = getActInactReasonsForEditGeneric(map,currency.getReasonActInactMap());
            currency.setReasonActInactMap(reasonsActiveInactiveMapping);
            map.put("edit" , true);
            map.put("viewable" , false);
            map.put("currency", currency);
            map.put("masterID", masterId);
            /**
             * Code to check whether Currency Code is valid ISO Code
             */
            if(!isCurrencyISOCodeValid){
            	result.rejectValue("isoCode", "label.currency.validation.invalidISOCode");
            }
            
            /*
             * Code to check whether the locale is valid java locale or not.
             */
            if (!isValidLocale) {
            	result.rejectValue("locale", "label.currency.validation.invalidLocale");
            }
            /*
             * Duplicate Customer Category Code Validation : If Code Exists then
             * Return With Message
             */
            if (colNameList != null && colNameList.size() > 0) {
                for (String c : colNameList) {
                    result.rejectValue(c, "label." + c + ".validation.exists");
                }                
            }
            return "currency";
        }

        boolean eventResult = executeMasterEvent(currency,"contextObjectCurrency",map);
        if(!eventResult){
            // getActInactReasMapForEdit(map,currency);
            String masterName = currency.getClass().getSimpleName();
            String uniqueParameter = "currencyName";
            String uniqueValue = currency.getCurrencyName();
            getActInactReasMapForEditApproved(map,currency,masterName,uniqueParameter,uniqueValue);
            map.put("edit" , true);
            ReasonsActiveInactiveMapping reasonsActiveInactiveMapping = getActInactReasonsForEditGeneric(map,currency.getReasonActInactMap());
            currency.setReasonActInactMap(reasonsActiveInactiveMapping);
            map.put("currency", currency);
            map.put("viewable" , false);
            map.put("masterID", masterId);
            map.put("activeFlag",currency.isActiveFlag());
            return "currency";
        }

        // we need to get below logged in user from session
        User user = getUserDetails().getUserReference();
        if (user != null) {
            ReasonsActiveInactiveMapping reasonsActiveInactiveMapping = currency.getReasonActInactMap();
            if(reasonsActiveInactiveMapping != null){
                saveActInactReasonForMaster(reasonsActiveInactiveMapping,currency);
            }
            currency.setReasonActInactMap(reasonsActiveInactiveMapping);
            Currency savedCurrency = (Currency)makerCheckerService.masterEntityChangedByUser(currency, user);
   
        }
        if (createAnotherMaster) {
            return initializeCurrency(map);
        }
        return "redirect:/app/grid/Currency/Currency/loadColumnConfig";

    
    }   
    
    /**
     * @param Currency
     *            object containing isoCode and
     *            CurrencyName.
     * @return String
     * @throws IOException
     * @description to save Currency object from view
     */
    @PreAuthorize("hasAuthority('MAKER_CURRENCY')")
    @RequestMapping(value = "/saveAndSendForApproval", method = RequestMethod.POST)
    public String saveAndSendForApproval(@Validated Currency currency, BindingResult result, ModelMap map,
            @RequestParam("createAnotherMaster") boolean createAnotherMaster) {
        BaseLoggers.flowLogger.debug("Saving Currency Details-->"+currency.getLogInfo());
        /*
         * Map whose Key Is Table Column Name with whom to validate and Value is
         * The One to be validated.This Map Is Send in the Validator Method
         */

        Currency dubplicateCurrency =null;
        if(null!=currency.getId()){
            dubplicateCurrency = entityDao.find(Currency.class,currency.getId());
            if(null != dubplicateCurrency.getEntityLifeCycleData()){
                currency.setEntityLifeCycleData(dubplicateCurrency.getEntityLifeCycleData());
            }
            if(null != dubplicateCurrency.getMasterLifeCycleData()){
                currency.setMasterLifeCycleData(dubplicateCurrency.getMasterLifeCycleData());
            }
        }

        Map<String, Object> validateMap = new HashMap<String, Object>();
        validateMap.put("isoCode", currency.getIsoCode());
        validateMap.put("currencyName", currency.getCurrencyName());

        
        /*
         * Code to check as if any existing(or new) record is being modified(or created) into another existing record
         */

        List<String> colNameList = checkValidationForDuplicates(currency, Currency.class, validateMap);

        boolean isValidLocale = LocaleUtils.isValidLocale(currency.getLocale().toString());

        List<Date> effectiveFromList = new ArrayList<>();
        List<ConversionRate> conversionRateList = new ArrayList<ConversionRate>();
        boolean duplicateConvRateflag = false;
        
        for (int i = 0; i < currency.getConversionRateList().size(); i++) {
        	ConversionRate conversionrate = currency.getConversionRateList().get(i);
        	Date effectiveFrom = conversionrate.getEffectiveFrom();

            if (!(effectiveFromList.contains(effectiveFrom))) {

            	effectiveFromList.add(effectiveFrom);
            	duplicateConvRateflag = false;

            } else {
                result.rejectValue("conversionRateList[" + i + "].effectiveFrom",
                        "label.DuplicateConversionRate");
                duplicateConvRateflag = true;
            }
            if (!conversionrate.isEmpty()) {
            	conversionRateList.add(conversionrate);
            }
        }        
        currency.setConversionRateList(conversionRateList);
        
        if (result.hasErrors() || (colNameList != null && colNameList.size() > 0)  || !isValidLocale || duplicateConvRateflag) {
            if(currency.getId() != null) {
                Currency c = baseMasterService.getMasterEntityById(Currency.class, currency.getId());
                if (!(ApprovalStatus.UNAPPROVED_ADDED == c.getApprovalStatus() || ApprovalStatus.CLONED == c.getApprovalStatus())) {
                    map.put("codeViewMode", true);
                }
            }
            String masterName = currency.getClass().getSimpleName();
            String uniqueValue = null;
            String uniqueParameter = null;
            if (null != currency.getId()) {
                //Currency curr = baseMasterService.findById(Currency.class, currency.getId());
                uniqueValue = dubplicateCurrency.getCurrencyName();
                uniqueParameter = "currencyName";
                getActInactReasMapForEditApproved(map, currency, masterName, uniqueParameter, uniqueValue);
            }
            else {
                ReasonsActiveInactiveMapping reasActInactMap = getActInactReasMapForCreate(map);
                currency.setReasonActInactMap(reasActInactMap);
            }
            ReasonsActiveInactiveMapping reasonsActiveInactiveMapping = getActInactReasonsForEditGeneric(map,currency.getReasonActInactMap());
            currency.setReasonActInactMap(reasonsActiveInactiveMapping);
            map.put("edit" , true);
            map.put("viewable" , false);
            map.put("currency", currency);
            map.put("masterID", masterId);
            /*
             * Code to check whether the locale is valid java locale or not.
             */
            if (!isValidLocale) {
            	result.rejectValue("locale", "label.currency.validation.invalidLocale");
            }
            /*
             * Duplicate Currency Code Validation : If Code Exists then
             * Return With Message
             */
            if (colNameList != null && colNameList.size() > 0) {
                for (String c : colNameList) {
                    result.rejectValue(c, "label." + c + ".validation.exists");
                }                
            }
            return "currency";
        }

        boolean eventResult = executeMasterEvent(currency,"contextObjectCurrency",map);
        if(!eventResult){
            // getActInactReasMapForEdit(map,currency);
            String masterName = currency.getClass().getSimpleName();
            String uniqueParameter = "currencyName";
            String uniqueValue = currency.getCurrencyName();
            getActInactReasMapForEditApproved(map,currency,masterName,uniqueParameter,uniqueValue);
            map.put("edit" , true);
            ReasonsActiveInactiveMapping reasonsActiveInactiveMapping = getActInactReasonsForEditGeneric(map,currency.getReasonActInactMap());
            currency.setReasonActInactMap(reasonsActiveInactiveMapping);
            map.put("currency", currency);
            map.put("viewable" , false);
            map.put("masterID", masterId);
            map.put("activeFlag",currency.isActiveFlag());
            return "currency";
        }

        // we need to get below logged in user from session
        User user = getUserDetails().getUserReference();
        if (user != null) {
            ReasonsActiveInactiveMapping reasonsActiveInactiveMapping = currency.getReasonActInactMap();
            if(reasonsActiveInactiveMapping != null){
                saveActInactReasonForMaster(reasonsActiveInactiveMapping,currency);
            }
            currency.setReasonActInactMap(reasonsActiveInactiveMapping);
            makerCheckerService.saveAndSendForApproval(currency, user);
        }
        if (createAnotherMaster) {
            return initializeCurrency(map);
            
        }
        return "redirect:/app/grid/Currency/Currency/loadColumnConfig";

    }
    
    /**
     * @param record
     *            id for edit
     * @return void
     * @throws
     * @description to edit Currency
     */
    @PreAuthorize("hasAuthority('MAKER_CURRENCY')")
    @RequestMapping(value = "/edit/{id}")
    public String editCurrency(@PathVariable("id") Long id, ModelMap map) {

        Currency currency = baseMasterService.getMasterEntityById(Currency.class, id);
        if (currency.getApprovalStatus() == ApprovalStatus.UNAPPROVED_MODIFIED) {
            Currency prevCurrency = (Currency) baseMasterService
                    .getLastApprovedEntityByUnapprovedEntityId(currency.getEntityId());
            map.put("prevCurrency", prevCurrency);
            map.put("editLink", false);
        }
        if(currency.getConversionRateList() != null && currency.getConversionRateList().size() > 0){
            Hibernate.initialize(currency.getConversionRateList());
        }
        if(!(ApprovalStatus.UNAPPROVED_ADDED == currency.getApprovalStatus() || ApprovalStatus.CLONED == currency.getApprovalStatus())) {
            map.put("codeViewMode", true);
        }
        ReasonsActiveInactiveMapping reasonsActiveInactiveMapping = getActInactReasonsForEditGeneric(map,currency.getReasonActInactMap());
        currency.setReasonActInactMap(reasonsActiveInactiveMapping);
        // getActInactReasMapForEdit(map,currency);
        String masterName = currency.getClass().getSimpleName();
        String uniqueParameter = "currencyName";
        String uniqueValue = currency.getCurrencyName();
        getActInactReasMapForEditApproved(map,currency,masterName,uniqueParameter,uniqueValue);
        map.put("currency", currency);
        map.put("edit", true);
        map.put("masterID", masterId);

        map.put("viewable" ,false);
        return "currency";
    }
    
    /**
     * @param record
     *            id for view
     * @return void
     * @throws
     * @description to view Currency
     */
    @SuppressWarnings("unchecked")
    @PreAuthorize("hasAuthority('VIEW_CURRENCY') or hasAuthority('MAKER_CURRENCY') or hasAuthority('CHECKER_CURRENCY')")
    @RequestMapping(value = "/view/{id}", method = RequestMethod.GET)
    public String viewCurrency(@PathVariable("id") Long id, ModelMap map) {
      

        UserInfo currentUser = getUserDetails();
        Currency currency = baseMasterService.getMasterEntityWithActionsById(Currency.class, id,
                currentUser.getUserEntityId().getUri());
        if (currency.getApprovalStatus() == ApprovalStatus.UNAPPROVED_MODIFIED
                || currency.getApprovalStatus() == ApprovalStatus.WORFLOW_IN_PROGRESS) {
            Currency prevCurrency = (Currency) baseMasterService
                    .getLastApprovedEntityByUnapprovedEntityId(currency.getEntityId());
            map.put("prevCurrency", prevCurrency);
            map.put("editLink", false);
        } else if (currency.getApprovalStatus() == ApprovalStatus.APPROVED_MODIFIED) {
            Currency prevCurrency = (Currency) baseMasterService
                    .getLastUnApprovedEntityByApprovedEntityId(currency.getEntityId());
            map.put("prevCurrency", prevCurrency);
            map.put("viewLink", false);
        }
        if(currency.getConversionRateList() != null && currency.getConversionRateList().size() > 0){
            Hibernate.initialize(currency.getConversionRateList());
        }
        ReasonsActiveInactiveMapping reasonsActiveInactiveMapping = getActInactReasonsForEditGeneric(map,currency.getReasonActInactMap());
        currency.setReasonActInactMap(reasonsActiveInactiveMapping);
        // getActInactReasMapForEdit(map,currency);
        String masterName = currency.getClass().getSimpleName();
        String uniqueParameter = "currencyName";
        String uniqueValue = currency.getCurrencyName();
        getActInactReasMapForEditApproved(map,currency,masterName,uniqueParameter,uniqueValue);
        map.put("currency", currency);
        map.put("viewable", true);
        map.put("codeViewMode", true);
        map.put("masterID", masterId);
        if (currency.getViewProperties() != null) {
            ArrayList<String> actions = (ArrayList<String>) currency.getViewProperties().get("actions");
            if (actions != null) {
                for (String act : actions) {
                    String actionString = "act" + act;
                    map.put(actionString.replaceAll(" ", ""), false);
                }
            }
        }

        return "currency";
    }
    
    @PreAuthorize("hasAuthority('MAKER_CURRENCY')")
    @RequestMapping(value = "/addAddtionalRow")
    public String addAdditionalRow(@RequestParam int endSize, ModelMap map,
            HttpServletRequest request) {
        ConversionRate conversionRate = new ConversionRate();
        List<ConversionRate> conversionRateList = new ArrayList<ConversionRate>();
        conversionRateList.add(conversionRate);
        Currency currency = new Currency();
        currency.setConversionRateList(conversionRateList);
        map.put("endSize", endSize);
        return "currency/conversionRateAdditionalRow";
    }
    
    @PreAuthorize("hasAuthority('MAKER_CURRENCY')")
    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    public @ResponseBody
    String deleteCoversionRate(ModelMap map, @RequestParam(value = "rateID") long rateID) {

        if (moneyService.deleteConversionRateFromCurrency(rateID)) {
            return "true";
        } else {
            return "false";
        }

    }
    
    
    @ResponseBody
    @RequestMapping(value = "/validateCurrencyISOCode/{currencyIsoCode}", method = RequestMethod.GET)
    public Map<String, Object> validateCurrencyISOCode(@PathVariable String currencyIsoCode, ModelMap map) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		Boolean isCurrencyISOCodeValid=validateCurrencyISOCode(currencyIsoCode);
		if(isCurrencyISOCodeValid){
			resultMap.put("valid", true);
		}else{
			resultMap.put("valid", false);
		}
		return resultMap;
    }
    
    private Boolean validateCurrencyISOCode(String currencyIsoCode) {
    	Boolean isCurrencyISOCodeValid=Boolean.FALSE;
    	if(StringUtils.isNotBlank(currencyIsoCode)){
			try{
				java.util.Currency.getInstance(currencyIsoCode.toUpperCase());
				isCurrencyISOCodeValid=true;				
			}catch(Exception e){
				BaseLoggers.exceptionLogger.error("Exception: " + e.getMessage(),
						e);
				isCurrencyISOCodeValid=false;
			}
		}
    	return isCurrencyISOCodeValid;
	}

	private String initializeCurrency(ModelMap map){
        ConversionRate conversionRate = new ConversionRate();
        List<ConversionRate> conversionRateList = new ArrayList<ConversionRate>();
        conversionRateList.add(conversionRate);
        Currency currency = new Currency();
        ReasonsActiveInactiveMapping reasActInactMap = getActInactReasMapForCreate(map);
        currency.setReasonActInactMap(reasActInactMap);
        map.put("reasonsActiveInactiveMapping",currency.getReasonActInactMap());
        currency.setConversionRateList(conversionRateList);
        map.put("currency", currency);
        map.put("masterID", masterId);
        return "currency";
    }

}
