/**
 * Copyright Nucleus Software India Pvt Ltd. All rights reserved.
 */

package com.nucleus.web.organizationbranch.master.dcb;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.collections4.CollectionUtils;
import org.hibernate.Hibernate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.nucleus.address.Address;
import com.nucleus.address.AddressInitializer;
import com.nucleus.address.AddressTagService;
import com.nucleus.address.City;
import com.nucleus.core.organization.calendar.BranchCalendar;
import com.nucleus.core.organization.entity.OrganizationBranch;
import com.nucleus.core.organization.entity.OrganizationType;
import com.nucleus.core.organization.entity.ParentBranchMapping;
import com.nucleus.core.organization.entity.SystemName;
import com.nucleus.core.villagemaster.entity.VillageMaster;
import com.nucleus.entity.ApprovalStatus;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.user.User;
import com.nucleus.user.UserInfo;
import com.nucleus.web.organizationbranch.master.OrganizationBranchController;

/**
 * @author Nucleus Software India Pvt Ltd This field is being used for
 *         controlling country CRUD and task allocation work-flow related
 *         operations.
 */
@Transactional
@Controller("organizationBranchControllerDCB")
@RequestMapping(value = "/OrganizationBranch/DCB")
public class OrganizationBranchControllerDCB extends OrganizationBranchController {

	@Inject
    @Named("addressInitializer")
    private AddressInitializer addressInitializer;

	
    @Inject
    @Named("addressService")
    private AddressTagService addressService;

    /**
     * @param organization
     *            branch object containing branch name, branch code, parent
     *            organization branch, country, city and district.
     * @return String
     * @throws IOException
     * @description to save organization branch object from view
     */
    @RequestMapping(value = "/save", method = RequestMethod.POST)
    public String saveOrganizationBranch(
            @Validated @ModelAttribute("organizationBranch") OrganizationBranch organizationBranch, BindingResult result,
            ModelMap map, @RequestParam("createAnotherMaster") boolean createAnotherMaster) {
        BaseLoggers.flowLogger.debug("Saving Organization Branch Details-->" + organizationBranch.getLogInfo());
        if (checkForDuplicateRecordsAndValidateForm(organizationBranch, result, map)) {
            return "organizationBranchDCB";
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

        return "organizationBranchDCB";
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

        if (organizationBranch.getBranchCalendar() == null) {
            // Code inserted after adding branch calendar to organization branch.
            BranchCalendar branchCalendar = organizationService.getDerivedBranchCalendar(organizationBranch);
            branchCalendar = new BranchCalendar(branchCalendar);
            organizationBranch.setBranchCalendar(branchCalendar);
            Hibernate.initialize(branchCalendar.getHolidayList());
            map.put("calendarViewOnly", true);
            map.put("renderWithoutEnclosingForm", false);
            map.put("branchCalendar", branchCalendar);

        } else {
            map.put("renderWithoutEnclosingForm", false);
            Hibernate.initialize(organizationBranch.getBranchCalendar().getHolidayList());
            map.put("branchCalendar", organizationBranch.getBranchCalendar());
        }

        if (organizationBranch.getServedCities() != null && organizationBranch.getServedCities().size() > 0) {
            long[] ids = new long[organizationBranch.getServedCities().size()];
            int i = 0;
            for (City city : organizationBranch.getServedCities()) {
                ids[i] = city.getId();
                i++;
            }
            organizationBranch.setServedCityIds(ids);

        }

        if (organizationBranch.getServedVillageIds() != null && organizationBranch.getServedVillageIds().length > 0) {
            List<VillageMaster> villageMasters = new ArrayList<>();
            for (long villageId : organizationBranch.getServedVillageIds()) {
                villageMasters.add(baseMasterService.getMasterEntityById(VillageMaster.class, villageId));

            }
            organizationBranch.setServedVillages(villageMasters);
        }

        map.put("organizationBranch", organizationBranch);
        if (organizationBranch.getContactInfo() != null) {
        	Address address = organizationBranch.getContactInfo().getAddress();
        	if(address != null){
        		addressInitializer.initialize(address, AddressInitializer.AddressLazyAttributes.ADDRESS_TYPE, AddressInitializer.AddressLazyAttributes.COUNTRY);
        		map.put("address", address);
        	}
        }
        return "organizationBranchDCB";
    }

    /**
     * @description to save and send for approval * @return String
     * @throws IOException
     * @description to save and send for approval organization branch object
     *              from view
     */
    @RequestMapping(value = "/saveAndSendForApproval", method = RequestMethod.POST)
    public String saveAndSendForApproval(@Validated OrganizationBranch organizationBranch, BindingResult result,
            ModelMap map, @RequestParam("createAnotherMaster") boolean createAnotherMaster) {
        BaseLoggers.flowLogger.debug("Saving Organization Branch Details-->" + organizationBranch.getLogInfo());
        if (checkForDuplicateRecordsAndValidateForm(organizationBranch, result, map)) {
            return "organizationBranchDCB";
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

        if (organizationBranch.getServedCities() != null && organizationBranch.getServedCities().size() > 0) {
            long[] ids = new long[organizationBranch.getServedCities().size()];
            int i = 0;
            for (City city : organizationBranch.getServedCities()) {
                ids[i] = city.getId();
                i++;
            }
            organizationBranch.setServedCityIds(ids);

        }

		if (organizationBranch.getServedVillages() != null && !organizationBranch.getServedVillages().isEmpty()) {
            long[] ids = new long[organizationBranch.getServedVillages().size()];
            int i = 0;
            for (VillageMaster villageMaster : organizationBranch.getServedVillages()) {
                ids[i] = villageMaster.getId();
                i++;
            }
            organizationBranch.setServedVillageIds(ids);

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
        if (organizationBranch.getBranchCalendar() == null) {
            // Code inserted after adding branch calendar to organization branch.
            BranchCalendar branchCalendar = organizationService.getDerivedBranchCalendar(organizationBranch);
            if (organizationBranch != null && branchCalendar != null) {

                branchCalendar = new BranchCalendar(branchCalendar);
                organizationBranch.setBranchCalendar(branchCalendar);
                Hibernate.initialize(branchCalendar.getHolidayList());
                map.put("calendarViewOnly", true);
                map.put("renderWithoutEnclosingForm", false);
                map.put("branchCalendar", branchCalendar);

            }
        } else {
            map.put("renderWithoutEnclosingForm", false);
            Hibernate.initialize(organizationBranch.getBranchCalendar().getHolidayList());
            map.put("branchCalendar", organizationBranch.getBranchCalendar());
        }

        map.put("parentOrganizationBranch", parentOrganizationBranch);
        map.put("viewable", true);
        if (organizationBranch.getContactInfo() != null) {
        	Address address = organizationBranch.getContactInfo().getAddress();
        	if(address != null){
        		addressInitializer.initialize(address, AddressInitializer.AddressLazyAttributes.ADDRESS_TYPE, AddressInitializer.AddressLazyAttributes.COUNTRY);
        		map.put("address", address);
        	}
        }
        map.put("masterID", masterId);
        return "organizationBranchDCB";
    }

}
