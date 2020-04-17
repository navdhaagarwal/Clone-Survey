/**
 * This file and a proportion of its content is copyright of Nucleus Software Exports Limited - � 2012. All rights reserved.
 * Any redistribution or reproduction of part or all of the contents in any form is prohibited other than the following:
 * - you cannot print or download to a local hard disk extract contents either part or full for personal/ commercial/
 * academic or any other use
 * - you may not copy the content to individual/ third parties for any type of use, either as compiled or source format
 * without the knowledge and consent of Nucleus SOftware
 * - You may not, except with our express written permission, distribute or commercially exploit the content. Nor may you
 * transmit it or store it in any other web site or other form of electronic retrieval system.
 */
package com.nucleus.web.technical;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import com.nucleus.address.*;
import com.nucleus.core.dynamicform.service.FormService;
import com.nucleus.core.formsConfiguration.SpecialTable;
import com.nucleus.core.villagemaster.entity.VillageMaster;
import org.springframework.beans.factory.annotation.Autowired;

import com.nucleus.adhoc.AdhocTaskSubType;
import com.nucleus.adhoc.AdhocTaskType;
import com.nucleus.config.persisted.vo.MyFavorites;
import com.nucleus.core.formsConfiguration.FormConfigInvocMapping;
import com.nucleus.core.formsConfiguration.ModelMetaData;
import com.nucleus.core.genericparameter.service.GenericParameterService;
import com.nucleus.core.misc.util.DateUtils;
import com.nucleus.core.organization.entity.OrganizationBranch;
import com.nucleus.core.organization.entity.RootOrganization;
import com.nucleus.core.organization.service.OrganizationService;
import com.nucleus.core.role.entity.Role;
import com.nucleus.core.team.entity.Team;
import com.nucleus.core.team.service.TeamService;
import com.nucleus.core.workflowconfig.entity.ProcessingStageType;
import com.nucleus.currency.Currency;
import com.nucleus.customer.CustomerCategory;
import com.nucleus.customer.CustomerConstitution;
import com.nucleus.customer.ExistingBankRelationshipType;
import com.nucleus.customer.RegistrationType;
import com.nucleus.customer.qualification.QualificationClassification;
import com.nucleus.customer.qualification.QualificationSpecialization;
import com.nucleus.customer.qualification.QualificationType;
import com.nucleus.demographics.RelationshipType;
import com.nucleus.document.core.entity.CersaiDocumentType;
import com.nucleus.document.core.entity.DocumentClassificationType;
import com.nucleus.document.core.entity.DocumentParameterType;
import com.nucleus.document.core.entity.DocumentType;
import com.nucleus.document.service.DocumentService;
import com.nucleus.internetchannel.AccomodationType;
import com.nucleus.internetchannel.ResidenceType;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.notificationMaster.NotificationMaster;
import com.nucleus.person.entity.GenderType;
import com.nucleus.core.genericparameter.entity.GenericParameter;
import com.nucleus.core.genericparameter.entity.TimeUnit;
import com.nucleus.user.DeviationLevel;
import com.nucleus.user.User;
import com.nucleus.user.UserInfo;
import com.nucleus.user.UserService;
import com.nucleus.web.binder.AbstractWebDataBinder;
import com.nucleus.web.binder.MasterMapDataBinder;
import com.nucleus.web.binder.ProductTypeDataBinder;
import com.nucleus.web.binder.WebDataBinderRegistry;
import com.nucleus.core.locale.LanguageInfoReader;
import com.nucleus.web.master.GenericParameterBinder;

/**
 * @author Nucleus Software Exports Limited
 */
public class CoreWebDataRegistrar {

    @Autowired
    private WebDataBinderRegistry registry;
    @Inject
    private LanguageInfoReader languageInfoReader;
    @Autowired
    private GenericParameterService genericParameterService;

    private static final String DynamicDtypeClass = "com.nucleus.core.genericparameter.entity.DynamicGenericParameter"; 





    @PostConstruct
    public void registerBinders() {

        /**
         * Master data binder registrations
         */
    	List<String>  genericParameterTypes= genericParameterService.findAllGenericParameterTypes();
    	for (String genericParameterType:genericParameterTypes){
    		try {
    			if(DynamicDtypeClass.equalsIgnoreCase(genericParameterType)){
    				Class genericParameterEntityClass = Class.forName(genericParameterType);
    				List<String> dynamicgenericTypes = genericParameterService.findAllDynamicGenericParameter();
    				for(String dynamicgenericType:dynamicgenericTypes){
    					Map<String,Object> map =  new HashMap<>();
    					map.put(RegistrarConstants.DYNAMIC_PARAMETER_FIELD, dynamicgenericType);
    					registry.registerBinder(dynamicgenericType,new MasterMapDataBinder(genericParameterEntityClass, 
    							 map,
    							 RegistrarConstants.AVAILABLE_COLUMN_NAMES_NAME_CODE_DESCRIPTION));
    				}
    			}
				Class genericParameterEntityClass = Class.forName(genericParameterType);
				 registry.registerBinder(genericParameterEntityClass.getSimpleName(),new MasterMapDataBinder(
						 genericParameterEntityClass,
                        RegistrarConstants.AVAILABLE_COLUMN_NAMES_NAME_CODE_DESCRIPTION));			
				 } catch (ClassNotFoundException e) {
					 	BaseLoggers.exceptionLogger.debug("Error Accure while binder creation for Generic Parameter",e);;
			}
    	}
    	
    	registry.registerBinder("GenericParameter", new MasterMapDataBinder(GenericParameter.class,
                RegistrarConstants.NAME));
        registry.registerBinder("roleList", new MasterMapDataBinder(Role.class,
                RegistrarConstants.NAME));
        registry.registerBinder("countryGroupList", new MasterMapDataBinder(
                CountryGroup.class, RegistrarConstants.COUNTRY_GROUP));
        registry.registerBinder("countryList", new MasterMapDataBinder(
                Country.class,
                RegistrarConstants.AVAILABLE_COLUMN_NAMES_FOR_COUNTRY));
        /*
         * registry.registerBinder("countryList", new
         * MasterDataBinder(Country.class));
         */
        registry.registerBinder("regionList", new MasterMapDataBinder(
                GeoRegion.class, RegistrarConstants.REGION_NAME));
        registry.registerBinder("intraRegionList", new MasterMapDataBinder(
                IntraCountryRegion.class, RegistrarConstants.INTRA_REGION_NAME));
        registry.registerBinder("areaList", new MasterMapDataBinder(Area.class,
                RegistrarConstants.AREA_NAME));
        // gistry.registerBinder("industryList", new
        // MasterDataBinder(Industry.class));
        registry.registerBinder("districtList", new MasterMapDataBinder(
                District.class, RegistrarConstants.DISTRICT_NAME));
        registry.registerBinder("cityList", new MasterMapDataBinder(City.class,
                RegistrarConstants.CITY_NAME));
        registry.registerBinder("villageList", new MasterMapDataBinder(VillageMaster.class,
                RegistrarConstants.NAME));


        registry.registerBinder("stateList", new MasterMapDataBinder(
                State.class, RegistrarConstants.STATE_NAME));
        
        registry.registerBinder("zipCodeList", new MasterMapDataBinder(
                ZipCode.class, RegistrarConstants.ZIP_CODE));
        registry.registerBinder(
                "customerConstitutionList",
                new MasterMapDataBinder(
                        CustomerConstitution.class,
                        RegistrarConstants.AVAILABLE_COLUMN_NAMES_FOR_CONSTITUTION));
        registry.registerBinder("customerCategoryList",
                new MasterMapDataBinder(CustomerCategory.class,
                        RegistrarConstants.CUSTOMER_CATEGORY_DESC));
        
        

        registry.registerBinder("qualificationClassification",
                new MasterMapDataBinder(QualificationClassification.class,
                        RegistrarConstants.NAME));

        /**
         * Added for loanInfo tab
         */
        registry.registerBinder("organizationBranchList",
                new MasterMapDataBinder(OrganizationBranch.class,
                        RegistrarConstants.NAME));
        registry.registerBinder("rootOrganizationBranch",
                new MasterMapDataBinder(RootOrganization.class,
                        RegistrarConstants.NAME));
        

        
        
		registry.registerBinder("usersList",  new AbstractWebDataBinder<List<?>>() {
            @Override
            public List<?> getData() {
            	return ((UserService) getWebApplicationContext().getBean("userService")).getAllUserProfileNameAndIdForBinderList();
            }
        });
        registry.registerBinder("allTeams", new MasterMapDataBinder(Team.class,
                RegistrarConstants.NAME));
        registry.registerBinder("allUsers", new MasterMapDataBinder(User.class,
                RegistrarConstants.USER_NAME));

        registry.registerBinder("selectFavourites", new MasterMapDataBinder(
                MyFavorites.class,
                RegistrarConstants.AVAILABLE_COLUMN_NAMES_FOR_FAV));
        registry.registerBinder("areaCategory", new MasterMapDataBinder(
                AreaType.class, RegistrarConstants.NAME));


        registry.registerBinder("modelMetaDataList", new MasterMapDataBinder(
                ModelMetaData.class, RegistrarConstants.NAME));

        registry.registerBinder("formMappingPoints", new MasterMapDataBinder(
                FormConfigInvocMapping.class,
                RegistrarConstants.AVAILABLE_COLUMN_NAMES_FOR_FORM_CONFIG));
        registry.registerBinder("documentTypeList", new MasterMapDataBinder(
                DocumentType.class,
                RegistrarConstants.AVAILABLE_COLUMN_NAMES_NAME_CODE));
        registry.registerBinder("cersaiDocumentTypeList",
                new MasterMapDataBinder(CersaiDocumentType.class,
                        RegistrarConstants.AVAILABLE_COLUMN_NAMES_NAME_CODE));
        registry.registerBinder("type", new MasterMapDataBinder(
                DocumentParameterType.class, RegistrarConstants.NAME));
        
        registry.registerBinder("documentClassificationType",
                new MasterMapDataBinder(DocumentClassificationType.class,
                        RegistrarConstants.NAME));

        registry.registerBinder(
                "registrationTypeList",
                new MasterMapDataBinder(
                        RegistrationType.class,
                        RegistrarConstants.AVAILABLE_COLUMN_NAMES_NAME_CODE_DESCRIPTION));
        registry.registerBinder("adhocTaskType", new MasterMapDataBinder(
                AdhocTaskType.class, RegistrarConstants.NAME));
        registry.registerBinder("adhocTaskSubType", new MasterMapDataBinder(
                AdhocTaskSubType.class, RegistrarConstants.NAME));
        registry.registerBinder("currency", new MasterMapDataBinder(
                Currency.class, RegistrarConstants.CURRENCY_NAME));
        registry.registerBinder(
                "productType",
                new ProductTypeDataBinder(
                        RegistrarConstants.AVAILABLE_COLUMN_NAMES_SHORT_NAME_DESCRIPTION));
        // Required for dynamic binder assignment
        registry.registerBinder(
                "genderType",
                new MasterMapDataBinder(
                        GenderType.class,
                        RegistrarConstants.AVAILABLE_COLUMN_NAMES_NAME_CODE_DESCRIPTION));
        registry.registerBinder("accomodationType", new MasterMapDataBinder(
                AccomodationType.class,
                RegistrarConstants.AVAILABLE_COLUMN_NAMES_FOR_ASSET_CATEGORY));

        /*
         * registry.registerBinder("relationType", new
         * MasterMapDataBinder(RelationshipType.class,
         * RegistrarConstants.AVAILABLE_COLUMN_NAMES_NAME_CODE));
         */
        /* Relationship type including parent code */
        registry.registerBinder(
                "relationTypeIncludingParentCode",
                new MasterMapDataBinder(
                        RelationshipType.class,
                        RegistrarConstants.AVAILABLE_COLUMN_NAMES_NAME_CODE_PARENT_CODE));

        
        
        /*
         * registry.registerBinder("productType", new
         * ListDataBinderUtil(ProductType.class, RegistrarConstants.NAME));
         */
        registry.registerBinder("qualificationSpecializationList",
                new MasterMapDataBinder(QualificationSpecialization.class,
                        RegistrarConstants.SPECIALIZATION_NAME));
        

        

        registry.registerBinder("processingStageTypeList",
                new MasterMapDataBinder(ProcessingStageType.class,
                        RegistrarConstants.NAME));
        
        registry.registerBinder("productTypeList", new ProductTypeDataBinder(
                RegistrarConstants.SHORT_NAME));
        registry.registerBinder("notificationMasterList",
                new MasterMapDataBinder(NotificationMaster.class,
                        RegistrarConstants.NOTIFICATION_NAME));
        
        registry.registerBinder("residenceType", new MasterMapDataBinder(
                ResidenceType.class, RegistrarConstants.CODE));

        registry.registerBinder(
                "existingRelationshipType",
                new MasterMapDataBinder(
                        ExistingBankRelationshipType.class,
                        RegistrarConstants.AVAILABLE_COLUMN_NAMES_NAME_CODE_DESCRIPTION));

        // Added by Taru






        // Added by Taru ends here

        /**
         * Anonymous bindings
         */
        // to be verified
        registry.registerBinder("organizationBranchListOfBranchType",
                new AbstractWebDataBinder<List<?>>() {
                    @Override
                    public List<?> getData() {
                        return ((OrganizationService) getWebApplicationContext()
                                .getBean("organizationService"))
                                .getOrgBranchesOfBranchType();
                    }
                });

        registry.registerBinder("organizationBranchListOfBranchTypeRO",
                new AbstractWebDataBinder<List<?>>() {
                    @Override
                    public List<?> getData() {
                        return ((OrganizationService) getWebApplicationContext()
                                .getBean("organizationService"))
                                .getOrgBranchesOfBranchTypeRO();
                    }
                });

        registry.registerBinder("documentList",
                new AbstractWebDataBinder<List<?>>() {
                    @Override
                    public List<?> getData() {
                        return ((DocumentService) getWebApplicationContext()
                                .getBean("documentService"))
                                .getApprovedgroupDocumentsForBinder();
                    }
                });

        registry.registerBinder("documentMappingCodes",new AbstractWebDataBinder<List<?>>() {
            @Override
            public List<?> getData() {
                return ((DocumentService) getWebApplicationContext()
                        .getBean("documentService"))
                        .getAllApprovedMappingCodes();
            }
        });

        /*
         * registry.registerBinder("documentList", new
         * AbstractWebDataBinder<List<?>>() {
         * 
         * @Override public List<?> getData() { return ((DocumentService)
         * getWebApplicationContext
         * ().getBean("documentService")).getApprovedgroupDocuments(); } });
         */

        registry.registerBinder("currentUserDateFormat",
                new AbstractWebDataBinder<String>() {
                    @Override
                    public String getData() {
                        return ((UserService) getWebApplicationContext()
                                .getBean("userService"))
                                .getUserPreferredDateFormat();
                    }
                });

        registry.registerBinder("currentUserLocale",
                new AbstractWebDataBinder<Locale>() {
                    @Override
                    public Locale getData() {
                        return ((UserService) getWebApplicationContext()
                                .getBean("userService")).getUserLocale();
                    }
                });

        registry.registerBinder("ruleInvocationPointList",
                new AbstractWebDataBinder<List<?>>() {
                    @Override
                    public List<?> getData() {
                        List<String> rulesPointList = new ArrayList<String>();
                        rulesPointList.add("");
                        return rulesPointList;

                    }
                });

        registry.registerBinder("passwordExpiringDaysList",
                new AbstractWebDataBinder<List<String>>() {
                    @Override
                    public List<String> getData() {
                        return ((UserService) getWebApplicationContext()
                                .getBean("userService"))
                                .getPasswordExpireInDays();
                    }
                });

        registry.registerBinder("getAllUserExceptLoggedIn",
                new AbstractWebDataBinder<List<?>>() {
                    @Override
                    public List<?> getData() {
                        return ((UserService) getWebApplicationContext()
                                .getBean("userService"))
                                .getAllUserExceptCurrent();
                    }
                });

        registry.registerBinder("getAllUserInBranch",
                new AbstractWebDataBinder<List<?>>() {
                    @Override
                    public List<?> getData() {
                        return ((UserService) getWebApplicationContext()
                                .getBean("userService"))
                                .getAllUsersInCurrentBranchExceptCurrent();
                    }
                });

        registry.registerBinder("getAllTeamsInBranch",
                new AbstractWebDataBinder<List<?>>() {
                    @Override
                    public List<?> getData() {
                        return ((TeamService) getWebApplicationContext()
                                .getBean("teamService"))
                                .getAllTeamsOfLoggedInBranch();
                    }
                });

        registry.registerBinder("getAllTeamLeads",
                new AbstractWebDataBinder<List<?>>() {
                    @Override
                    public List<?> getData() {
                        return ((TeamService) getWebApplicationContext()
                                .getBean("teamService")).getAllTeamLeads();
                    }
                });

        registry.registerBinder("qualificationType",
                new AbstractWebDataBinder<List<?>>() {
                    @Override
                    public List<?> getData() {

                        GenericParameterService genericParameterService = (GenericParameterService) getWebApplicationContext()
                                .getBean("genericParameterService");
                        List<QualificationType> qualificationList = genericParameterService
                                .retrieveTypes(QualificationType.class);

                        // for getting sorted list in order of qualification
                        // attained by a person
                        final List<String> definedOrder = Arrays.asList("HSC",
                                "SSC", "GRADUATION", "POST-GRADUATION");

                        Comparator<QualificationType> comparator = new Comparator<QualificationType>() {
                            // Comparator Function
                            @Override
                            public int compare(final QualificationType o1,
                                    final QualificationType o2) {
                                return Integer
                                        .valueOf(
                                                definedOrder.indexOf(o1
                                                        .getCode()))
                                        .compareTo(
                                                Integer.valueOf(definedOrder
                                                        .indexOf(o2.getCode())));
                            }
                        };

                        Collections.sort(qualificationList, comparator);
                        return qualificationList;
                    }
                });


        
        registry.registerBinder("allSupportedLanguageInfo",
                new AbstractWebDataBinder<List<?>>() {
                    @Override
                    public List<?> getData() {

                        return languageInfoReader.getAvailableLanguageInfo();
                    }
                });

        registry.registerBinder("monthList",
                new AbstractWebDataBinder<List<Integer>>() {
                    @Override
                    public List<Integer> getData() {
                        List<Integer> monthList = new ArrayList<Integer>();
                        for (int i = 1; i <= 12; i++) {
                            monthList.add(i);
                        }
                        return monthList;
                    }
                });

        registry.registerBinder("yearList",
                new AbstractWebDataBinder<List<Integer>>() {
                    @Override
                    public List<Integer> getData() {
                        int currentYear = DateUtils.getCurrentUTCTime()
                                .getYear();
                        List<Integer> yearList = new ArrayList<Integer>();
                        for (int i = currentYear; i <= currentYear + 30; i++) {
                            yearList.add(i);
                        }
                        return yearList;
                    }
                });

        

/*        registry.registerBinder("occupationTypeIcici",
                new AbstractWebDataBinder<List<?>>() {
                    @Override
                    public List<?> getData() {

                        GenericParameterService genericParameterService = (GenericParameterService) getWebApplicationContext()
                                .getBean("genericParameterService");
                        List<OccupationType> occupationTypeList = genericParameterService
                                .findChildrenByParentCode("ICICI",
                                        OccupationType.class);
                        return occupationTypeList;
                    }
                });*/

        registry.registerBinder("timeUnitList",
                new GenericParameterBinder(TimeUnit.class));
        registry.registerBinder("deviationLevelList",
                new GenericParameterBinder(DeviationLevel.class));

        registry.registerBinder("specialTable", new AbstractWebDataBinder<List<?>>() {
            @Override
            public List<?> getData() {
                FormService formService = (FormService) getWebApplicationContext().getBean("formConfigService");
                List<SpecialTable> specialTables = formService.getSpecialTableMetaData();
                return specialTables;
            }

        });
      
    }
    
}
