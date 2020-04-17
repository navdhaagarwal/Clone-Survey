/**
 * Copyright Nucleus Software India Pvt Ltd. All rights reserved.
 */

package com.nucleus.web.organizationbranch.master;

import static com.nucleus.finnone.pro.general.util.ValidatorUtils.notNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.collections4.CollectionUtils;
import org.hibernate.Hibernate;
import org.joda.time.DateTime;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.validation.Validator;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.nucleus.address.State;
import com.nucleus.address.Address;
import com.nucleus.address.AddressInitializer;
import com.nucleus.address.AddressTagService;
import com.nucleus.address.City;
import com.nucleus.config.persisted.service.ConfigurationService;
import com.nucleus.config.persisted.vo.ConfigurationVO;
import com.nucleus.contact.PhoneNumber;
import com.nucleus.core.genericparameter.service.GenericParameterService;
import com.nucleus.core.organization.calendar.BranchCalendar;
import com.nucleus.core.organization.calendar.Holiday;
import com.nucleus.core.organization.entity.OrganizationBranch;
import com.nucleus.core.organization.entity.OrganizationType;
import com.nucleus.core.organization.entity.ParentBranchMapping;
import com.nucleus.core.organization.entity.SystemName;
import com.nucleus.core.organization.service.OrganizationService;
import com.nucleus.core.villagemaster.entity.VillageMaster;
import com.nucleus.entity.ApprovalStatus;
import com.nucleus.entity.EntityId;
import com.nucleus.finnone.pro.general.util.ValidatorUtils;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.makerchecker.MakerCheckerService;
import com.nucleus.master.BaseMasterService;
import com.nucleus.persistence.HibernateUtils;
import com.nucleus.user.User;
import com.nucleus.user.UserInfo;
import com.nucleus.web.city.vo.CityVO;
import com.nucleus.web.common.controller.BaseController;
import com.nucleus.jsMessageResource.service.JsMessageResourceService;

import flexjson.JSONSerializer;
import flexjson.transformer.AbstractTransformer;
import flexjson.transformer.Transformer;

/**
 * @author Nucleus Software India Pvt Ltd This field is being used for
 *         controlling country CRUD and task allocation work-flow related
 *         operations.
 */
@Transactional
@Controller
@RequestMapping(value = "/OrganizationBranch")
public class OrganizationBranchController extends BaseController {

	private static final String ERROR_CODE_INITIALS = "label.dailySchedule";
	
    @Inject
    @Named("addressInitializer")
    private AddressInitializer addressInitializer;

	
    @Inject
    @Named("makerCheckerService")
    public MakerCheckerService     makerCheckerService;

    @Inject
    @Named("baseMasterService")
    public BaseMasterService       baseMasterService;

    @Inject
    @Named("genericParameterService")
    public GenericParameterService genericParameterService;

    @Inject
    public OrganizationService     organizationService;

    @Inject
    @Named("configurationService")
    private ConfigurationService configurationService;
    @Inject
    @Named("addressService")
    private AddressTagService addressService;
    
    @Inject
    @Named("organizationBranchValidator")
    private Validator   organizationBranchValidator;
    
       @Inject
    @Named("jsMessageResourceService")
    public JsMessageResourceService jsMessageResourceService;

    /*
     * @InitBinder protected void initBinder(WebDataBinder binder) {
     * binder.setValidator(new OrganizationBranchValidator()); }
     */

    public final Transformer       date_transformer = new AbstractTransformer() {
                                                        @Override
                                                        public void transform(Object object) {
                                                            getContext().write(
                                                                    "\"" + getFormattedDate((DateTime) object) + "\"");
                                                        }
                                                    };

    public static final String     masterId         = "OrganizationBranch";
    public static final String     CONFIG_SHOW_GSTIN  = "config.showGSTIN";
    public static final String	   ENTITY_URI		= "com.nucleus.entity.SystemEntity:1";
    public static final String	   CONFIG_MAP		= "showGSTIN";

    /*
     * Method Added to send current Entity Uri for working of
     * comments,activity,history,notes
     */
    @ModelAttribute("currentEntityClassName")
    public String getEntityClassName() {
        return OrganizationBranch.class.getName();
    }

    /**
     * @param organization
     *            branch object containing branch name, branch code, parent
     *            organization branch, country, city and district.
     * @return String
     * @throws IOException
     * @description to save organization branch object from view
     */
    @PreAuthorize("hasAuthority('MAKER_ORGANIZATIONBRANCH')")
    @RequestMapping(value = "/save", method = RequestMethod.POST)
    public String saveOrganizationBranch(
            @Validated @ModelAttribute("organizationBranch") OrganizationBranch organizationBranch, BindingResult result,
            ModelMap map, @RequestParam("createAnotherMaster") boolean createAnotherMaster) {
        BaseLoggers.flowLogger.debug("Saving Organization Branch Details-->" + organizationBranch.getLogInfo());
        if (checkForDuplicateRecordsAndValidateForm(organizationBranch, result, map)) {
        	 String regexForOrgBranchName=jsMessageResourceService.getAppendedPropertyForKeys("allowed.specChars.orgBranch.name","core.web.validation.config.customValidatorForOrgBranchName");
             map.put("regexForOrgBranchName",regexForOrgBranchName);
            if(organizationBranch.getId() != null) {
                OrganizationBranch ob = baseMasterService.getMasterEntityById(OrganizationBranch.class, organizationBranch.getId());
                if (!(ApprovalStatus.UNAPPROVED_ADDED == ob.getApprovalStatus() || ApprovalStatus.CLONED == ob.getApprovalStatus())) {
                    map.put("codeViewMode", true);
                }
            }
            return "organizationBranch";
        }
        
        if(organizationBranch.getBranchRiskCategory()==null || organizationBranch.getBranchRiskCategory().getId()==null){
            organizationBranch.setBranchRiskCategory(null);
        }

    	if(organizationBranch.getContactInfo().getPhoneNumber() == null || organizationBranch.getContactInfo().getPhoneNumber().getId()==null ) {
    		organizationBranch.getContactInfo().setPhoneNumber(null);
    	}
        
        if (organizationBranch.getContactInfo().getAddress().getCountry() == null
                || organizationBranch.getContactInfo().getAddress().getCountry().getId() == null) {
            organizationBranch.getContactInfo().getAddress().setCountry(null);
        }

        if (organizationBranch.getContactInfo().getAddress().getCity() == null
                || organizationBranch.getContactInfo().getAddress().getCity().getId() == null) {
            organizationBranch.getContactInfo().getAddress().setCity(null);
        }

        if (organizationBranch.getContactInfo().getAddress().getState() == null
                || organizationBranch.getContactInfo().getAddress().getState().getId() == null) {
            organizationBranch.getContactInfo().getAddress().setState(null);
        }
        if (organizationBranch.getContactInfo().getAddress().getArea() == null
                || organizationBranch.getContactInfo().getAddress().getArea().getId() == null) {
            organizationBranch.getContactInfo().getAddress().setArea(null);
        }
        if (organizationBranch.getContactInfo().getAddress().getDistrict() == null
                || organizationBranch.getContactInfo().getAddress().getDistrict().getId() == null) {
            organizationBranch.getContactInfo().getAddress().setDistrict(null);
        }

        if (organizationBranch.getContactInfo().getAddress().getRegion() == null
                || organizationBranch.getContactInfo().getAddress().getRegion().getId() == null) {
            organizationBranch.getContactInfo().getAddress().setRegion(null);
        }
        if (organizationBranch.getContactInfo().getAddress().getZipcode() == null
                || organizationBranch.getContactInfo().getAddress().getZipcode().getId() == null) {
            organizationBranch.getContactInfo().getAddress().setZipcode(null);
        }
        if (organizationBranch.getContactInfo() != null
                && organizationBranch.getContactInfo().getAddress() != null) {
            addressService.handleVillageAndTehsilMaster(organizationBranch.getContactInfo().getAddress());
        }

        // Code for branch calendar
        OrganizationBranch orgBranch = removeNullEntries(organizationBranch);
        // prepareForCloning(orgBranch);

        // Set the served cities of the branch
        if (organizationBranch.getServedCityIds() != null && organizationBranch.getServedCityIds().length > 0) {
            List<City> cities = new ArrayList<City>();
            for (long cityId : organizationBranch.getServedCityIds()) {
                cities.add(baseMasterService.getMasterEntityById(City.class, cityId));

            }
            orgBranch.setServedCities(cities);
        }

        if (organizationBranch.getServedVillageIds() != null && organizationBranch.getServedVillageIds().length > 0) {
            List<VillageMaster> villageMasters = new ArrayList<>();
            for (long villageId : organizationBranch.getServedVillageIds()) {
                villageMasters.add(baseMasterService.getMasterEntityById(VillageMaster.class, villageId));

            }
            orgBranch.setServedVillages(villageMasters);
        }


        // we need to get below logged in user from session
        User user = getUserDetails().getUserReference();
        if (user != null) {
            makerCheckerService.masterEntityChangedByUser(orgBranch, user);
        }

        if (createAnotherMaster) {
            return createOrganizationBranch(map);
        }
        return "redirect:/app/grid/OrganizationBranch/OrganizationBranch/loadColumnConfig";

    }

    /**
     * @param record
     *            id for edit
     * @return void
     * @throws
     * @description to create organization branch
     */

    @PreAuthorize("hasAuthority('MAKER_ORGANIZATIONBRANCH')")
    @RequestMapping(value = "/create")
    public String createOrganizationBranch(ModelMap map) {
        OrganizationBranch organizationBranch = new OrganizationBranch();
        List<OrganizationType> organizationType = genericParameterService.retrieveTypes(OrganizationType.class);
        map.put("organizationType", organizationType);
        String rootOrg = organizationService.getRootOrganization().getName();
        map.put("company", rootOrg);
        map.put("masterID", masterId);
        List<ParentBranchMapping> branchMappings = new ArrayList<ParentBranchMapping>();
        ParentBranchMapping branchMappingCas = new ParentBranchMapping();
        branchMappingCas.setModuleName(genericParameterService.findByCode(SystemName.SOURCE_PRODUCT_TYPE_CAS,
                SystemName.class));
        ParentBranchMapping branchMappingLms = new ParentBranchMapping();
        branchMappingLms.setModuleName(genericParameterService.findByCode(SystemName.SOURCE_PRODUCT_TYPE_LMS,
                SystemName.class));
        branchMappings.add(0, branchMappingCas);
        branchMappings.add(1, branchMappingLms);
        organizationBranch.setParentBranchMapping(branchMappings);
        map.put("organizationBranch", organizationBranch);
        ConfigurationVO config = this.configurationService.getConfigurationPropertyFor(
      	      EntityId.fromUri(ENTITY_URI), 
      	      CONFIG_SHOW_GSTIN);
      	    if (config != null && "true".equalsIgnoreCase(config.getPropertyValue()))
      	    {
      	        map.put(CONFIG_MAP, "TRUE");
      	    }else
      	        map.put(CONFIG_MAP, "FALSE");
        String regexForOrgBranchName=jsMessageResourceService.getAppendedPropertyForKeys("allowed.specChars.orgBranch.name","core.web.validation.config.customValidatorForOrgBranchName");
        map.put("regexForOrgBranchName",regexForOrgBranchName);
        map.put("servedCityList","");
        map.put("servedVillageList","");
        return "organizationBranch";
    }

    @PreAuthorize("hasAuthority('MAKER_ORGANIZATIONBRANCH')")
    @RequestMapping(value = "/populateApprovedCitiesFromState")
    @ResponseBody
    public  Map<String, String> populateApprovedCitiesFromState(@RequestParam(value = "stateList",required = false) List<Long> stateList, ModelMap map) {
        List<City> cityList = new ArrayList<>();
        Map<String, String> consolidateMap = new HashMap<String, String>();
        List<CityVO> cityVOs = new ArrayList<>();
        if (null != stateList) {
            cityList = addressService.findAllApprovedCitiesInState(stateList);
            for (City c : cityList) {
                consolidateMap.put(c.getId().toString(), c.getCityName().concat(" - ").concat(c.getState().getStateName()));
                CityVO cityV = new CityVO();
                cityV.setId(c.getId());
                cityV.setCityName(c.getCityName());
                cityV.setCityStateName(c.getCityName().concat(" - ").concat(c.getState().getStateName()));
                cityVOs.add(cityV);
            }
            map.put("cityList", cityVOs);
        }
        map.put("viewable", false);
        return consolidateMap;
    }

    @PreAuthorize("hasAuthority('MAKER_ORGANIZATIONBRANCH')")
    @RequestMapping(value = "/populateApprovedVillagesFromState")
    @ResponseBody
    public  Map<String, String> populateApprovedVillagesFromState(@RequestParam(value = "stateList",required = false) List<Long> stateList, ModelMap map) {
        List<VillageMaster> villageList = new ArrayList<>();
        Map<String, String> villageMap = new HashMap<String, String>();

        if (null != stateList) {
            villageList = addressService.findAllApprovedVillagesInState(stateList);
            for (VillageMaster v : villageList) {
                villageMap.put(v.getId().toString(), v.getName());
            }
            map.put("villageList",villageList);
        }
        map.put("viewable", false);
        return villageMap;
    }
    /**
     * @param record
     *            id for edit
     * @return void
     * @throws
     * @description to edit organization branch
     */
    @PreAuthorize("hasAuthority('MAKER_ORGANIZATIONBRANCH')")
    @RequestMapping(value = "/edit/{id}")
    @Transactional(readOnly = true)
    public String editOrganizationBranch(@PathVariable("id") Long id, ModelMap map) {

        OrganizationBranch organizationBranch = baseMasterService.getMasterEntityById(OrganizationBranch.class, id);
        Hibernate.initialize(organizationBranch.getParentBranchMapping());
        if (CollectionUtils.isNotEmpty(organizationBranch.getParentBranchMapping())) {
            for (ParentBranchMapping pbm : organizationBranch.getParentBranchMapping()) {
                Hibernate.initialize(pbm.getParentBranch());
                Hibernate.initialize(pbm.getModuleName());
            }
        }
        if(organizationBranch!=null && organizationBranch.getBranchRiskCategory()!=null){
            Hibernate.initialize(organizationBranch.getBranchRiskCategory());
        }

        Hibernate.initialize(organizationBranch.getSignatureAuthority());
        Hibernate.initialize(organizationBranch.getPrimaryContactPerson());
        if (organizationBranch.getApprovalStatus() == ApprovalStatus.UNAPPROVED_MODIFIED) {
            OrganizationBranch prevOrganizationBranch = (OrganizationBranch) baseMasterService
                    .getLastApprovedEntityByUnapprovedEntityId(organizationBranch.getEntityId());
            map.put("prevOrganizationBranch", prevOrganizationBranch);
            map.put("editLink", false);
        }
        List<OrganizationType> organizationType = genericParameterService.retrieveTypes(OrganizationType.class);
        map.put("organizationType", organizationType);
        String rootOrg = organizationService.getRootOrganization().getName();
        map.put("company", rootOrg);
        List<OrganizationBranch> parentOrganizationBranch = getParentOrganizationBranchesForType(organizationBranch.getOrganizationType()
                .getId(), organizationBranch);
        map.put("parentOrganizationBranch", parentOrganizationBranch);
        map.put("masterID", masterId);
        map.put("edit", true);
        BranchCalendar branchCalendar = organizationService.getDerivedBranchCalendar(organizationBranch);
        if (notNull(organizationBranch) && notNull(branchCalendar)) {
            Hibernate.initialize(branchCalendar.getHolidayList());
            if (organizationBranch.getBranchCalendar() == null) {
                map.put("calendarViewOnly", true);
                organizationBranch.setBranchCalendar(branchCalendar);
            }
        }
        map.put("branchCalendar", branchCalendar);

        Map<String,String> servedCityMap = new HashMap<>();
        if (organizationBranch.getServedCities() != null && organizationBranch.getServedCities().size() > 0) {
            long[] ids = new long[organizationBranch.getServedCities().size()];
            int i = 0;
            for (City city : organizationBranch.getServedCities()) {
                ids[i] = city.getId();
                servedCityMap.put(Long.toString(city.getId()),city.getCityName().concat(" - ").concat(city.getState().getStateName()));
                i++;
            }
            organizationBranch.setServedCityIds(ids);
            JSONSerializer iSerializer = new JSONSerializer();
            String jsonString = iSerializer.exclude("*.class").deepSerialize(servedCityMap);
            map.put("servedCityList",jsonString);
        }
        Map<String,String> servedVillageMap = new HashMap<>();
        if (organizationBranch.getServedVillages() != null && !organizationBranch.getServedVillages().isEmpty()) {
            long[] ids = new long[organizationBranch.getServedVillages().size()];
            int i = 0;
            for (VillageMaster villageMaster : organizationBranch.getServedVillages()) {
                ids[i] = villageMaster.getId();
                servedVillageMap.put(Long.toString(villageMaster.getId()),villageMaster.getName());
                i++;
            }
            organizationBranch.setServedVillageIds(ids);
            JSONSerializer villageSerializer = new JSONSerializer();
            String villageJson = villageSerializer.exclude("*.class").deepSerialize(servedVillageMap);
            map.put("servedVillageList",villageJson);

        }


        map.put("renderWithoutEnclosingForm", false);
        map.put("organizationBranch", organizationBranch);

        if (organizationBranch.getContactInfo() != null) {
        	Address address = organizationBranch.getContactInfo().getAddress();
        	if(address != null){
        		addressInitializer.initialize(address, AddressInitializer.AddressLazyAttributes.ADDRESS_TYPE, AddressInitializer.AddressLazyAttributes.COUNTRY);
        		map.put("address", address);
        	}
        	PhoneNumber phoneNumber = organizationBranch.getContactInfo().getPhoneNumber();
        	if(phoneNumber != null) {
        		HibernateUtils.initializeAndUnproxy(phoneNumber);
        	}
        }
        
        ConfigurationVO config = this.configurationService.getConfigurationPropertyFor(
      	      EntityId.fromUri(ENTITY_URI), 
      	      CONFIG_SHOW_GSTIN);
      	    if (config != null && "true".equalsIgnoreCase(config.getPropertyValue()))
      	    {
      	        map.put(CONFIG_MAP, "TRUE");
      	    }else
      	        map.put(CONFIG_MAP, "FALSE");
        String regexForOrgBranchName=jsMessageResourceService.getAppendedPropertyForKeys("allowed.specChars.orgBranch.name","core.web.validation.config.customValidatorForOrgBranchName");
        map.put("regexForOrgBranchName",regexForOrgBranchName);
        
		if(!(ApprovalStatus.UNAPPROVED_ADDED == organizationBranch.getApprovalStatus() || ApprovalStatus.CLONED == organizationBranch.getApprovalStatus())) {
            map.put("codeViewMode", true);
        }
        return "organizationBranch";
    }

    /**
     * @description to save and send for approval * @return String
     * @throws IOException
     * @description to save and send for approval organization branch object
     *              from view
     */
    @PreAuthorize("hasAuthority('MAKER_ORGANIZATIONBRANCH')")
    @RequestMapping(value = "/saveAndSendForApproval", method = RequestMethod.POST)
    public String saveAndSendForApproval(@Validated OrganizationBranch organizationBranch, BindingResult result,
            ModelMap map, @RequestParam("createAnotherMaster") boolean createAnotherMaster) {
        BaseLoggers.flowLogger.debug("Saving Organization Branch Details-->" + organizationBranch.getLogInfo());
        if (checkForDuplicateRecordsAndValidateForm(organizationBranch, result, map)) {
            String regexForOrgBranchName=jsMessageResourceService.getAppendedPropertyForKeys("allowed.specChars.orgBranch.name","core.web.validation.config.customValidatorForOrgBranchName");
            map.put("regexForOrgBranchName",regexForOrgBranchName);
            if(organizationBranch.getId() != null) {
                OrganizationBranch ob = baseMasterService.getMasterEntityById(OrganizationBranch.class, organizationBranch.getId());
                if (!(ApprovalStatus.UNAPPROVED_ADDED == ob.getApprovalStatus() || ApprovalStatus.CLONED == ob.getApprovalStatus())) {
                    map.put("codeViewMode", true);
                }
            }
            return "organizationBranch";
        }

        if(organizationBranch.getBranchRiskCategory()==null || organizationBranch.getBranchRiskCategory().getId()==null){
            organizationBranch.setBranchRiskCategory(null);
        }

        if(organizationBranch.getContactInfo().getPhoneNumber() == null || organizationBranch.getContactInfo().getPhoneNumber().getId()==null ) {
    		organizationBranch.getContactInfo().setPhoneNumber(null);
    	}
        
        if (organizationBranch.getContactInfo().getAddress().getCountry() == null
                || organizationBranch.getContactInfo().getAddress().getCountry().getId() == null) {
            organizationBranch.getContactInfo().getAddress().setCountry(null);
        }

        if (organizationBranch.getContactInfo().getAddress().getCity() == null
                || organizationBranch.getContactInfo().getAddress().getCity().getId() == null) {
            organizationBranch.getContactInfo().getAddress().setCity(null);
        }

        if (organizationBranch.getContactInfo().getAddress().getState() == null
                || organizationBranch.getContactInfo().getAddress().getState().getId() == null) {
            organizationBranch.getContactInfo().getAddress().setState(null);
        }
        if (organizationBranch.getContactInfo().getAddress().getArea() == null
                || organizationBranch.getContactInfo().getAddress().getArea().getId() == null) {
            organizationBranch.getContactInfo().getAddress().setArea(null);
        }
        if (organizationBranch.getContactInfo().getAddress().getDistrict() == null
                || organizationBranch.getContactInfo().getAddress().getDistrict().getId() == null) {
            organizationBranch.getContactInfo().getAddress().setDistrict(null);
        }

        if (organizationBranch.getContactInfo().getAddress().getRegion() == null
                || organizationBranch.getContactInfo().getAddress().getRegion().getId() == null) {
            organizationBranch.getContactInfo().getAddress().setRegion(null);
        }
        if (organizationBranch.getContactInfo().getAddress().getZipcode() == null
                || organizationBranch.getContactInfo().getAddress().getZipcode().getId() == null) {
            organizationBranch.getContactInfo().getAddress().setZipcode(null);
        }
        if (organizationBranch.getContactInfo() != null
                && organizationBranch.getContactInfo().getAddress() != null) {
            addressService.handleVillageAndTehsilMaster(organizationBranch.getContactInfo().getAddress());
        }

        // Code for branch calendar
        OrganizationBranch orgBranch = removeNullEntries(organizationBranch);
        // prepareForCloning(orgBranch);
        // we need to get below logged in user from session

        // Set the served cities of the branch
        if (organizationBranch.getServedCityIds() != null && organizationBranch.getServedCityIds().length > 0) {
            List<City> cities = new ArrayList<City>();
            for (long cityId : organizationBranch.getServedCityIds()) {
                cities.add(baseMasterService.getMasterEntityById(City.class, cityId));

            }
            orgBranch.setServedCities(cities);
        }

        if (organizationBranch.getServedVillageIds() != null && organizationBranch.getServedVillageIds().length > 0) {
            List<VillageMaster> villageMasters = new ArrayList<>();
            for (long villageId : organizationBranch.getServedVillageIds()) {
                villageMasters.add(baseMasterService.getMasterEntityById(VillageMaster.class, villageId));

            }
            orgBranch.setServedVillages(villageMasters);
        }

        User user = getUserDetails().getUserReference();
        if (user != null) {
            makerCheckerService.saveAndSendForApproval(orgBranch, user);
        }
        if (createAnotherMaster) {
            return createOrganizationBranch(map);
        }
        return "redirect:/app/grid/OrganizationBranch/OrganizationBranch/loadColumnConfig";

    }

    /**
     * @param record
     *            id for view
     * @return void
     * @throws
     * @description to view organization branch
     */

    // Transaction made read-only as we don't want to commit changes on managed object for
    // setting the branch calendar.
    @SuppressWarnings("unchecked")
    @PreAuthorize("hasAuthority('MAKER_ORGANIZATIONBRANCH') or hasAuthority('CHECKER_ORGANIZATIONBRANCH')")
    @RequestMapping(value = "/view/{id}", method = RequestMethod.GET)
    @Transactional(readOnly = true)
    public String viewOrganizationBranch(@PathVariable("id") Long id, ModelMap map) {

        UserInfo currentUser = getUserDetails();
        OrganizationBranch organizationBranch = baseMasterService.getMasterEntityWithActionsById(OrganizationBranch.class,
                id, currentUser.getUserEntityId().getUri());

        Hibernate.initialize(organizationBranch.getParentBranchMapping());
        if (CollectionUtils.isNotEmpty(organizationBranch.getParentBranchMapping())) {
            for (ParentBranchMapping pbm : organizationBranch.getParentBranchMapping()) {
                Hibernate.initialize(pbm.getParentBranch());
                Hibernate.initialize(pbm.getModuleName());
            }
        }
        if(organizationBranch!=null && organizationBranch.getBranchRiskCategory()!=null){
            Hibernate.initialize(organizationBranch.getBranchRiskCategory());
        }
        Hibernate.initialize(organizationBranch.getSignatureAuthority());
        Hibernate.initialize(organizationBranch.getPrimaryContactPerson());
        if (organizationBranch.getApprovalStatus() == ApprovalStatus.UNAPPROVED_MODIFIED) {
            OrganizationBranch prevOrganizationBranch = (OrganizationBranch) baseMasterService
                    .getLastApprovedEntityByUnapprovedEntityId(organizationBranch.getEntityId());
            map.put("prevOrganizationBranch", prevOrganizationBranch);
          map.put("editLink", false);
        } else if (organizationBranch.getApprovalStatus() == ApprovalStatus.APPROVED_MODIFIED
                || organizationBranch.getApprovalStatus() == ApprovalStatus.WORFLOW_IN_PROGRESS) {
            OrganizationBranch prevOrganizationBranch = (OrganizationBranch) baseMasterService
                    .getLastUnApprovedEntityByApprovedEntityId(organizationBranch.getEntityId());
            map.put("prevOrganizationBranch", prevOrganizationBranch);
            map.put("viewLink", false);
        }
        /* List<Country> countryList = baseMasterService.getLastApprovedEntities(Country.class);
         map.put("countryList", countryList);*/
        List<OrganizationType> organizationType = genericParameterService.retrieveTypes(OrganizationType.class);
        map.put("organizationType", organizationType);
        String rootOrg = organizationService.getRootOrganization().getName();

        Map<String,String> servedCityMap = new HashMap<>();
        if (organizationBranch.getServedCities() != null && organizationBranch.getServedCities().size() > 0) {
            long[] ids = new long[organizationBranch.getServedCities().size()];
            int i = 0;
            for (City city : organizationBranch.getServedCities()) {
                ids[i] = city.getId();
                servedCityMap.put(Long.toString(city.getId()),city.getCityName().concat(" - ").concat(city.getState().getStateName()));
                i++;
            }
            organizationBranch.setServedCityIds(ids);
            JSONSerializer iSerializer = new JSONSerializer();
            String jsonString = iSerializer.exclude("*.class").deepSerialize(servedCityMap);
            map.put("servedCityList",jsonString);

        }
        Map<String,String> servedVillageMap = new HashMap<>();
        if (organizationBranch.getServedVillages() != null && !organizationBranch.getServedVillages().isEmpty()) {
            long[] ids = new long[organizationBranch.getServedVillages().size()];
            int i = 0;
            for (VillageMaster villageMaster : organizationBranch.getServedVillages()) {
                ids[i] = villageMaster.getId();
                servedVillageMap.put(Long.toString(villageMaster.getId()),villageMaster.getName());
                i++;
            }
            organizationBranch.setServedVillageIds(ids);
            JSONSerializer villageSerializer = new JSONSerializer();
            String villageJson = villageSerializer.exclude("*.class").deepSerialize(servedVillageMap);
            map.put("servedVillageList",villageJson);
        }
        map.put("company", rootOrg);
        map.put("organizationBranch", organizationBranch);

        List<OrganizationBranch> parentOrganizationBranch = getParentOrganizationBranchesForType(organizationBranch.getOrganizationType()
                .getId(), organizationBranch);

        if (organizationBranch.getViewProperties() != null) {
            ArrayList<String> actions = (ArrayList<String>) organizationBranch.getViewProperties().get("actions");
            if (actions != null) {
                for (String act : actions) {
                    String actionString = "act" + act;
                    map.put(actionString.replaceAll(" ", ""), false);
                }

            }

        }
    
        BranchCalendar branchCalendar = organizationService.getDerivedBranchCalendar(organizationBranch);
        if (organizationBranch != null && branchCalendar != null) {
            Hibernate.initialize(branchCalendar.getHolidayList());
            if (organizationBranch.getBranchCalendar() == null) {
                map.put("calendarViewOnly", true);
                organizationBranch.setBranchCalendar(branchCalendar);
            }
        }
        map.put("branchCalendar", branchCalendar);
        map.put("renderWithoutEnclosingForm", false);
        map.put("parentOrganizationBranch", parentOrganizationBranch);
        map.put("viewable", true);
        map.put("codeViewMode", true);

        if (organizationBranch.getContactInfo() != null) {
        	Address address = organizationBranch.getContactInfo().getAddress();
        	if(address != null){
        		addressInitializer.initialize(address, AddressInitializer.AddressLazyAttributes.ADDRESS_TYPE, AddressInitializer.AddressLazyAttributes.COUNTRY);
        		map.put("address", address);
        	}
        	PhoneNumber phoneNumber = organizationBranch.getContactInfo().getPhoneNumber();
        	if(phoneNumber != null) {
        		HibernateUtils.initializeAndUnproxy(phoneNumber);
        	}
        }
        
        ConfigurationVO config = this.configurationService.getConfigurationPropertyFor(
      	      EntityId.fromUri(ENTITY_URI), 
      	      CONFIG_SHOW_GSTIN);
      	    if (config != null && "true".equalsIgnoreCase(config.getPropertyValue()))
      	    {
      	        map.put(CONFIG_MAP, "TRUE");
      	    }else
      	        map.put(CONFIG_MAP, "FALSE");
      	    
        map.put("masterID", masterId);
        return "organizationBranch";
    }

    @PreAuthorize("hasAuthority('MAKER_ORGANIZATIONBRANCH')")
    @RequestMapping(value = "/checkcode/{id}")
    public @ResponseBody
    String checkCode(ModelMap map, @PathVariable Long id, @RequestParam(value = "organizationBranchId", required = false) Long organizationBranchId) {
        if (id == 0) {
            return null;
        }
        JSONSerializer iSerializer = new JSONSerializer();

        OrganizationBranch organizationBranch = null;
        if (null != organizationBranchId) {
            organizationBranch = baseMasterService.getMasterEntityById(OrganizationBranch.class, organizationBranchId);
        }
        List<OrganizationBranch> parentOrganizationBranch = getParentOrganizationBranchesForType(id, organizationBranch);
        return iSerializer.exclude("*.class")
                .include("name", "id", "parentOrganizationBranch.name", "parentOrganizationBranch.id").exclude("*")
                .transform(date_transformer, DateTime.class).serialize(parentOrganizationBranch);
    }

    // Transaction made read-only as we don't want to commit changes on managed object for
    // setting the branch calendar.    
    @PreAuthorize("hasAuthority('MAKER_ORGANIZATIONBRANCH')")
    @RequestMapping(value = "/getCalendarFromParent/{id}")
    @Transactional(readOnly = true)
    public String getParentBranchCalendar(@PathVariable("id") Long id, ModelMap map) {

        OrganizationBranch organizationBranch = baseMasterService.getMasterEntityById(OrganizationBranch.class, id);
        BranchCalendar derivedBranchCalendar = organizationService.getDerivedBranchCalendar(organizationBranch);

        // Only make ids null in case it comes from supplied branch(not parent).
        if (derivedBranchCalendar.getId() != null) {
            derivedBranchCalendar = new BranchCalendar(derivedBranchCalendar);
        }
        OrganizationBranch emptyBranch = new OrganizationBranch();
        emptyBranch.setBranchCalendar(derivedBranchCalendar);

        map.put("organizationBranch", emptyBranch);
        map.put("branchCalendar", derivedBranchCalendar);
        map.put("calendarViewOnly", true);
        map.put("renderWithoutEnclosingForm", true);
        map.put("viewable", true);
        map.put("codeViewMode", true);

        return "branchCalendar";

    }

    @RequestMapping(value = "/getParentBranchCalendarByParentBranchId/{parentId}/{branchId}")
    @Transactional(readOnly = true)
    public String getParentBranchCalendarByParentBranchId(@PathVariable("parentId") Long parentId,@PathVariable("branchId") Long branchId, ModelMap map) {
    	 
    	 OrganizationBranch orgBranch;
    	 Long zero = 0L;
    	 
    	 OrganizationBranch organizationBranch = baseMasterService.getMasterEntityById(OrganizationBranch.class, parentId);
         BranchCalendar derivedBranchCalendar = organizationService.getDerivedBranchCalendar(organizationBranch);

         // Only make ids null in case it comes from supplied branch(not parent).
         if (derivedBranchCalendar.getId() != null) {
        	 derivedBranchCalendar = new BranchCalendar(derivedBranchCalendar);
         }
         
         if(branchId != null && branchId.equals(zero)){
    		 orgBranch = new OrganizationBranch();    		 
    	 }else{
        	 orgBranch = baseMasterService.getMasterEntityById(OrganizationBranch.class, branchId);
    	 }
         
         orgBranch.setBranchCalendar(derivedBranchCalendar);

         map.put("organizationBranch", orgBranch);
         map.put("branchCalendar", derivedBranchCalendar);
         map.put("calendarViewOnly", true);
         map.put("renderWithoutEnclosingForm", true);
         map.put("viewable", true);
         map.put("codeViewMode", true);

         return "branchCalendar";

    }
    
    @PreAuthorize("hasAuthority('MAKER_ORGANIZATIONBRANCH')")
    @RequestMapping(value = "/getMaxEmailsFromParent/{id}")
    @Transactional(readOnly = true)
    public @ResponseBody
    String getMaxEmailsFromParent(@PathVariable("id") Long id, ModelMap map) {

        OrganizationBranch organizationBranch = baseMasterService.getMasterEntityById(OrganizationBranch.class, id);

        if ((organizationService.getMaximumEmailsForBranch(organizationBranch) != null)
                && (organizationService.getEmailFilteringEnabledStatus(organizationBranch) != false)) {
            return String.valueOf(organizationService.getMaximumEmailsForBranch(organizationBranch)) + "|"
                    + organizationService.getEmailFilteringEnabledStatus(organizationBranch);
        } else
             return String.valueOf(organizationService.getMaximumEmailsForBranch(organizationBranch)) + "|"
                    + organizationService.getEmailFilteringEnabledStatus(organizationBranch);

    }
    
    public OrganizationBranch removeNullEntries(OrganizationBranch organizationBranch) {
        // Removing null entries from holidayList.
        if (organizationBranch.getHasParentBranchCalender() == null
                || organizationBranch.getHasParentBranchCalender() == true) {
            organizationBranch.setBranchCalendar(null);
        }
        if (organizationBranch != null && organizationBranch.getBranchCalendar() != null
                && organizationBranch.getBranchCalendar().getHolidayList() != null) {
            for (Iterator<Holiday> iterator = organizationBranch.getBranchCalendar().getHolidayList().iterator() ; iterator
                    .hasNext() ;) {
                Holiday holiday = iterator.next();
                Date date= holiday.getHolidayDate();
                if (date == null) {
                    iterator.remove();
                }
            }
        }

        if (organizationBranch != null && organizationBranch.getBranchCalendar() != null) {
            BranchCalendar branchCalendar = organizationBranch.getBranchCalendar();
            if (branchCalendar.getMondaySchedule() != null && !branchCalendar.getMondaySchedule().isWorkingDay()) {
                branchCalendar.setMondaySchedule(null);
            }
            if (branchCalendar.getTuesdaySchedule() != null && !branchCalendar.getTuesdaySchedule().isWorkingDay()) {
                branchCalendar.setTuesdaySchedule(null);
            }
            if (branchCalendar.getWednesdaySchedule() != null && !branchCalendar.getWednesdaySchedule().isWorkingDay()) {
                branchCalendar.setWednesdaySchedule(null);
            }
            if (branchCalendar.getThursdaySchedule() != null && !branchCalendar.getThursdaySchedule().isWorkingDay()) {
                branchCalendar.setThursdaySchedule(null);
            }
            if (branchCalendar.getFridaySchedule() != null && !branchCalendar.getFridaySchedule().isWorkingDay()) {
                branchCalendar.setFridaySchedule(null);
            }
            if (branchCalendar.getSaturdaySchedule() != null && !branchCalendar.getSaturdaySchedule().isWorkingDay()) {
                branchCalendar.setSaturdaySchedule(null);
            }
            if (branchCalendar.getSundaySchedule() != null && !branchCalendar.getSundaySchedule().isWorkingDay()) {
                branchCalendar.setSundaySchedule(null);
            }
            branchCalendar.setHolidayList(organizationBranch.getBranchCalendar().getHolidayList());
        }
        return organizationBranch;

    }

    public void prepareForCloning(OrganizationBranch organizationBranch) {
        if (organizationBranch != null && organizationBranch.getApprovalStatus() == ApprovalStatus.APPROVED) {
            BranchCalendar branchCalendar = new BranchCalendar(organizationBranch.getBranchCalendar());
            organizationBranch.setBranchCalendar(branchCalendar);
        }
    }

    /*
     * Method to validate form and check for duplicate records 
     */

    public boolean checkForDuplicateRecordsAndValidateForm(OrganizationBranch organizationBranch, BindingResult result,
            ModelMap map) {
        boolean flag = false;
        organizationBranchValidator.validate(organizationBranch, result);

        if (result.hasErrors()) {
            for (ObjectError err : result.getAllErrors()) {
                BaseLoggers.exceptionLogger.error("===============================================" + err.toString());
            }
            flag = true;
            List<ObjectError> dailyScheduleObjectErrorList = getDailyScheduleObjectErrorList(result);
			map.put("dailyScheduleObjectErrorList", dailyScheduleObjectErrorList);
        } else {
            Map<String, Object> validateMap = new HashMap<String, Object>();
            validateMap.put("branchCode", organizationBranch.getBranchCode());
            validateMap.put("name", organizationBranch.getName());
            List<String> colNameList = checkValidationForDuplicates(organizationBranch, OrganizationBranch.class,
                    validateMap);
            boolean headOfficeAlreadyCreated=false;
            headOfficeAlreadyCreated=organizationService.checkForDuplicateHeadOffice(organizationBranch);
            if(headOfficeAlreadyCreated){
            	 result.rejectValue("organizationType", "label." + "organizationType" + ".validation.exists");
            }
            
            if (CollectionUtils.isNotEmpty(colNameList)) {
                for (String c : colNameList) {
                    result.rejectValue(c, "label." + c + ".validation.exists");
                    BaseLoggers.exceptionLogger.error("label." + c + ".validation.exists");
                }
                flag = true;
            } else if(headOfficeAlreadyCreated){
            	 flag = true;
            }
            else{
            	 return flag;
            }
        }
        List<OrganizationType> organizationType = genericParameterService.retrieveTypes(OrganizationType.class);
        String rootOrg = organizationService.getRootOrganization().getName();
        map.put("company", rootOrg);
        map.put("organizationType", organizationType);
        map.put("organizationBranch", organizationBranch);
        map.put("masterID", masterId);
        if (organizationBranch != null && organizationBranch.getOrganizationType() != null
                && organizationBranch.getOrganizationType().getId() != null &&organizationBranch.getId()!=null) {
            OrganizationBranch persistedOrgBranch = baseMasterService.getMasterEntityById(OrganizationBranch.class, organizationBranch.getId());
            List<OrganizationBranch> parentOrganizationBranch = getParentOrganizationBranchesForType(organizationBranch.getOrganizationType()
                    .getId(), persistedOrgBranch);
            map.put("parentOrganizationBranch", parentOrganizationBranch);
        }
        if (organizationBranch != null && organizationBranch.getContactInfo() != null) {
        	Address address = organizationBranch.getContactInfo().getAddress();
        	if(address != null){
        		addressInitializer.initialize(address, AddressInitializer.AddressLazyAttributes.ADDRESS_TYPE, AddressInitializer.AddressLazyAttributes.COUNTRY);
        		map.put("address", address);
        	}
        	PhoneNumber phoneNumber = organizationBranch.getContactInfo().getPhoneNumber();
        	if(phoneNumber != null) {
        		HibernateUtils.initializeAndUnproxy(phoneNumber);
        	}
        }
        // Code inserted after adding branch calendar to organization branch.
        BranchCalendar branchCalendar = organizationService.getDerivedBranchCalendar(organizationBranch);
        if (organizationBranch != null && branchCalendar != null) {
            Hibernate.initialize(branchCalendar.getHolidayList());
            if (branchCalendar.getId() == null) {
                map.put("calendarViewOnly", true);
                organizationBranch.setBranchCalendar(branchCalendar);
            }
            map.put("branchCalendar", branchCalendar);
        }
        map.put("edit", true);

        return flag;
    }
    @PreAuthorize("hasAuthority('MAKER_ORGANIZATIONBRANCH')")
    @ResponseBody
    @RequestMapping(value = "/checkDuplicateBranchCodeAndName", method = RequestMethod.POST)
    public   List<String> checkDuplicateBranchCodeAndName(@ModelAttribute OrganizationBranch organizationBranch, BindingResult result,
            ModelMap map) {
    	 Map<String, Object> validateMap = new HashMap<>();
         validateMap.put("branchCode", organizationBranch.getBranchCode());
         validateMap.put("name", organizationBranch.getName());
         List<String> colNameList= checkValidationForDuplicates( organizationBranch,  OrganizationBranch.class,
    			validateMap) ;
         if(organizationService.checkForDuplicateHeadOffice(organizationBranch)){
 	    	colNameList.add("organizationType");
 }
	return colNameList;
    
       
    }
    @PreAuthorize("hasAuthority('MAKER_ORGANIZATIONBRANCH')")
    @ResponseBody
    @RequestMapping(value = "/checkDuplicateBranchCode/{branchCode}", method = RequestMethod.GET)
    public String checkDuplicateBranchCode(@PathVariable String branchCode, ModelMap map) {
        if (checkIfBranchCodeExists(branchCode))
            return "Unavailable";

        return "Available";
    }
    
    private boolean checkIfBranchCodeExists(String branchCode) {
        List<Integer> statusList = new ArrayList<Integer>();
        statusList.add(ApprovalStatus.UNAPPROVED_ADDED) ;
        statusList.add(ApprovalStatus.APPROVED_MODIFIED) ;
        statusList.add(ApprovalStatus.UNAPPROVED_MODIFIED) ;
        statusList.add(ApprovalStatus.WORFLOW_IN_PROGRESS) ;
        statusList.add(ApprovalStatus.APPROVED_DELETED_IN_PROGRESS) ;
        statusList.add(ApprovalStatus.APPROVED_DELETED) ;
        statusList.add(ApprovalStatus.APPROVED) ;
        return organizationService.checkIfBranchCodeExists(branchCode,statusList);
        
    }
    
    protected List<OrganizationBranch> getParentOrganizationBranchesForType(Long organizationTypeId, OrganizationBranch organizationBranch) {
        List<OrganizationBranch> parentOrganizationBranch = organizationService
                .getParentBranchesForType(genericParameterService.findById(
                        organizationTypeId, OrganizationType.class));
        if (ValidatorUtils.notNull(organizationBranch) && CollectionUtils.isNotEmpty(parentOrganizationBranch)) {
            //To get Original Organization Branch
            OrganizationBranch originalOrganizationBranch = getOriginalOrganizationBranch(organizationBranch);
            //Remove Parent Organization Branch if there parent OrgBranch becoming parent of itself
            if (ValidatorUtils.notNull(originalOrganizationBranch)) {
                parentOrganizationBranch.remove(originalOrganizationBranch);
            }
            return parentOrganizationBranch;
        } else {
            return parentOrganizationBranch;
        }
    }

    private OrganizationBranch getOriginalOrganizationBranch(OrganizationBranch organizationBranch) {
        OrganizationBranch originalOrgBranch = null;
        Integer approvalStatusBeforeUpdate = organizationBranch.getApprovalStatus();
        if (ValidatorUtils.notNull(approvalStatusBeforeUpdate) && (approvalStatusBeforeUpdate == ApprovalStatus.APPROVED)) {
            originalOrgBranch = organizationBranch;
        } else if (ValidatorUtils.notNull(approvalStatusBeforeUpdate)
                && (approvalStatusBeforeUpdate == ApprovalStatus.UNAPPROVED_ADDED || approvalStatusBeforeUpdate == ApprovalStatus.UNAPPROVED_MODIFIED)) {
            originalOrgBranch = (OrganizationBranch) baseMasterService.getLastApprovedEntityByUnapprovedEntityId(organizationBranch.getEntityId());
        }
        return originalOrgBranch;
    }
    @RequestMapping(value = "/getBranchDiv", method = RequestMethod.GET)
    public String getBranchDiv() {
     
        return "branchChangeHeader";
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
    
    private List<ObjectError> getDailyScheduleObjectErrorList(BindingResult result) {
		List<ObjectError> dailyScheduleObjectErrorList = new ArrayList<ObjectError>();
		
		List<ObjectError> listOfError = result.getAllErrors();
		for(ObjectError  objectError : listOfError){
			if(objectError.getCode().contains(ERROR_CODE_INITIALS)) {
				dailyScheduleObjectErrorList.add(objectError);
			}
		}
		
		return dailyScheduleObjectErrorList;
	}
}
