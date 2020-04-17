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
package com.nucleus.web.systemSetup;

import static com.nucleus.web.security.AesUtil.PASS_PHRASE;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.commons.CommonsMultipartFile;
import org.springframework.web.servlet.support.RequestContextUtils;

import com.nucleus.core.FrameworkServiceLocator;
import com.nucleus.core.exceptions.SystemException;
import com.nucleus.core.genericparameter.service.GenericParameterService;
import com.nucleus.core.initialization.ProductInformationLoader;
import com.nucleus.finnone.pro.base.utility.CoreUtility;
import com.nucleus.finnone.pro.cache.common.NeutrinoCachePopulator;
import com.nucleus.license.cache.LicenseClientCacheService;
import com.nucleus.license.content.model.LicenseDetail;
import com.nucleus.license.content.model.LicenseFeature;
import com.nucleus.license.core.entities.AppliedLicenses;
import com.nucleus.license.model.LicenseText;
import com.nucleus.license.pojo.LicenseInfoVO;
import com.nucleus.license.pojo.LicenseMobilityModuleInfo;
import com.nucleus.license.service.LicenseClientService;
import com.nucleus.license.utils.LicenseSetupUtil;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.persistence.EntityDao;
import com.nucleus.rules.model.SourceProduct;
import com.nucleus.security.oauth.apim.APIDetails;
import com.nucleus.security.oauth.apim.APIManagementService;
import com.nucleus.systemSetup.entity.ApplicationFeatures;
import com.nucleus.systemSetup.entity.CompanyLicenseInfo;
import com.nucleus.systemSetup.entity.CountryConfig;
import com.nucleus.systemSetup.entity.ProductConfig;
import com.nucleus.systemSetup.entity.SeedConfiguration;
import com.nucleus.systemSetup.service.SystemSetupService;
import com.nucleus.systemSetup.service.SystemSetupServiceImpl;
import com.nucleus.user.UserInfo;
import com.nucleus.user.UserService;
import com.nucleus.user.UserSessionManagerService;
import com.nucleus.web.common.controller.BaseController;
import com.nucleus.web.security.SystemSetupUtil;
import com.nucleus.web.trustedsource.TrustedSourceRegistrationService;

/**
 * Controller for seed configuration.
 * 
 * @author Nucleus Software Exports Limited
 */
@Transactional
@Controller
@RequestMapping(value = "/systemSetup")
public class SystemSetupController extends BaseController {
 
    @Inject
    @Named("systemSetupService")
    private SystemSetupService         systemSetupService;
    @Inject
	@Named("userService")
	private UserService userService;
    @Inject
    @Named("userSessionManagerService")
    private UserSessionManagerService  userSessionManagerService;
    
    @Inject
    @Named("licenseClientCacheService")
    public  LicenseClientCacheService licenseClientCacheService;

    @Inject
    @Named("systemSetupUtil")
    private SystemSetupUtil            systemSetupUtil;

    @Inject
    @Named("coreFrameworkServiceLocator")
    private FrameworkServiceLocator    frameworkServiceLocator;

    @Inject
    @Named("entityDao")
    private EntityDao                  entityDao;

    @Inject
    @Named("trustedSourceRegistrationService")
    TrustedSourceRegistrationService trustedSourceRegistrationService;

    @Inject
    @Named("seedConfigurationValidator")
    private SeedConfigurationValidator seedConfigurationValidator;
    @Inject
    @Named("apiManagementService")
    APIManagementService apiManagementService;
    
    @Inject
    @Named("genericParameterService")
    private GenericParameterService genericParameterService;
    
    @Autowired
    private LicenseSetupUtil           licenseSetupUtil;
    @Autowired
    LicenseClientService               licenseClientService;

    private static final String               MSG_SUCCESS = "success";
    
    private static final String               REDIRECT_SYSTEM_CONFIG_PAGE = "/systemSetup/seedConfigMainPage";
    
    private static final String 			   ERROR_COUNTRY_CONFIG = " Error in fetching country configurations in system setup in saveAndRunSeedConfig";
    
    private static final String 			   ERROR_PRODUCT_CONFIG = " Error in fetching product configurations in system setup in saveAndRunSeedConfig";

    private static final String 			   ERROR_SYSTEM_PRECOMPLETION = " Error in System Setup Pre Completion Service from Product Processor";
    
    private static final String 			   ERROR_CURRENT_LOGGEDIN_USER = " Error in invalidating LoggedIn User";
    
    private static final String 			   ERROR_TASK_CANCELLATION = " Task has been cancelled for one of the following Resource Names ";
    
    private static final String 			   ERROR_TASK_INTERRUPTION = " Task has been interrupted for one of the following Resource Names ";
    
    private static final String 			   ERROR_TASK_EXECUTION = " Error in Task execution for one of the following Resource Names "; 
    
    private static final String 			   ERROR_COUNTRY_CONFIG_MESSAGE = "label.error.system.setup.country.config";
    
    private static final String 			   ERROR_PRODUCT_CONFIG_MESSAGE = "label.error.system.setup.product.config";

    private static final String 			   ERROR_SYSTEM_PRECOMPLETION_MESSAGE = "label.error.system.setup.precompletion.service";
    
    private static final String 			   ERROR_CURRENT_LOGGEDIN_USER_MESSAGE = "label.error.system.setup.invalid.loggedin.user";
    
    private static final String 			   ERROR_TASK_CANCELLATION_MESSAGE = "label.error.system.setup.task.cancellation";
    
    private static final String 			   ERROR_TASK_INTERRUPTION_MESSAGE = "label.error.system.setup.task.interruption";
    
    private static final String 			   ERROR_TASK_EXECUTION_MESSAGE = "label.error.system.setup.task.execution";
    
    private static final String 			   ERROR_MESSAGE = "errorMessage";
    
    private static final String           INVALID_CREDENTIALS           = "login.controller.invalid.credentials";


    @Value("${core.web.config.webClientToEncryptpwd}")
    private String                                 webClientToEncryptpwd;

    /**
     * Handles and retrieves the login JSP page
     * 
     * @return the name of the JSP page
     */
    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public String getLoginPage(@RequestParam(value = "error", required = false) boolean error,ModelMap map, HttpServletRequest request) {
        BaseLoggers.flowLogger.debug("Received request to show configuration login page");
        request.getSession().setAttribute(PASS_PHRASE, RandomStringUtils.randomNumeric(8));
        if (systemSetupUtil.isSystemSetup()) {
            BaseLoggers.flowLogger.debug("Detected that system is already setup. Redirecting to main module.");
            return "redirect:" + systemSetupUtil.getLoginFormUrl();
        }else{
        	if(systemSetupUtil.isSystemSetUpInProgress()){
    			BaseLoggers.flowLogger.debug("Detected that system setup is in progress.");
    	        return "redirect:" + systemSetupUtil.getSystemSetUpInProgressUrl();
    		}
        }
        if (error) {
        	BaseLoggers.flowLogger
            .error("Login called with error parameter set to true.Extracting SPRING_SECURITY_LAST_EXCEPTION from session.");
    AuthenticationException exception = (AuthenticationException) request.getSession().getAttribute(
            "SPRING_SECURITY_LAST_EXCEPTION");
    BaseLoggers.flowLogger.error(
            "Exception(extracted from SPRING_SECURITY_LAST_EXCEPTION) occured while Logging In", exception);
    
    
    if (exception != null&&exception.getMessage().contains("UnauthorizedToApplyLicense"))
    {
    	
    	map.put("error", messageSource.getMessage("label.license.status.unauthorized.apply.license", null, request.getLocale()));
		
    }else{
             map.put("error", messageSource.getMessage(INVALID_CREDENTIALS, null, request.getLocale()));
        } 
        }
        map.put("userName", "config");
        map.put("isWebClientToEncryptpwd", "Y".equalsIgnoreCase(webClientToEncryptpwd));
        return "systemSetupLoginpage";

    }

    
    @RequestMapping(value = "/setUpInProgress", method = RequestMethod.GET)
    public String getSystemSetUpInProgressPage(ModelMap map, HttpServletRequest request) {
        if (systemSetupUtil.isSystemSetup() && !systemSetupUtil.isSystemSetUpInProgress()) {
            BaseLoggers.flowLogger.debug("Detected that system is already setup. Redirecting to main module..");
            return "redirect:" + systemSetupUtil.getLoginFormUrl();
        }
        return "systemSetupInProgressPage";

    }

    @RequestMapping(value = "/logout", method = RequestMethod.GET)
    public String getLogoutPage(ModelMap model) {
    	if (systemSetupUtil.isSystemSetup()) {
            BaseLoggers.flowLogger.debug("Detected that system is already setup. Redirecting to main module..");
            return "redirect:" + systemSetupUtil.getLoginFormUrl();
        }
        BaseLoggers.flowLogger.debug("Received request to Logout");
        userSessionManagerService.invalidateCurrentLoggedinUserSession();
        return "redirect:" + systemSetupUtil.getLoginFormUrlForSetup();
    }
    @PreAuthorize("hasAuthority('APPLY_LICENSE')")
    @RequestMapping(value = "/startSetup")
    public String setupSystem(ModelMap map) {
    	if (systemSetupUtil.isSystemSetup()) {
            BaseLoggers.flowLogger.debug("Detected that system is already setup. Redirecting to main module..");
            return "redirect:" + systemSetupUtil.getLoginFormUrl();
        }
        SeedConfiguration seedConfiguration = new SeedConfiguration();
        List<CountryConfig> countryConfigs = new ArrayList<CountryConfig>();
        List<ProductConfig> productConfigs = new ArrayList<ProductConfig>();

        countryConfigs.addAll(systemSetupService.getAllSeedConfigEntities(CountryConfig.class));
        productConfigs.addAll(systemSetupService.getAllSeedConfigEntities(ProductConfig.class));
        List<ApplicationFeatures> appFeaturesList = systemSetupService.getallApplicationFeatures();
        map.put("seedConfiguration", seedConfiguration);
        map.put("countryConfigs", countryConfigs);
        map.put("productConfigs", productConfigs);
        map.put("applicationFeaturesList", appFeaturesList);
        return "seedConfigMainPage";
    }

    @RequestMapping(value = "/saveAndRunSeedConfig", method = RequestMethod.POST)
    public String saveAndRunSeedConfig(SeedConfiguration seedConfiguration, ModelMap map,
            @RequestParam("attachedLicenseKeyFile") CommonsMultipartFile attachedLicenseKeyFile,
            @RequestParam("attachedLicenseTextFile") CommonsMultipartFile attachedLicenseTextFile, HttpServletRequest request)
            throws InterruptedException, ExecutionException, JAXBException {

        List<Future<String>>               futures     = new ArrayList<>();


    	if (systemSetupUtil.isSystemSetup()) {
            BaseLoggers.flowLogger.debug("Detected that system is already setup. Redirecting to main module..");
            return "redirect:" + systemSetupUtil.getLoginFormUrl();
        }

        CompanyLicenseInfo companyLicenseInfo = seedConfiguration.getCompanyLicenseInfo();
        String validationResult = null;

        if (companyLicenseInfo != null) {
            validationResult = seedConfigurationValidator.validateLicenseKeyText(companyLicenseInfo.getLicenseKey(),
                    companyLicenseInfo.getLicenseText(), attachedLicenseKeyFile, attachedLicenseTextFile, request);
        }

        String tempResultArray[] = validationResult.split(",");

        boolean isValidationSuccessful = false;

        if (tempResultArray[1].equals(MSG_SUCCESS))
            isValidationSuccessful = true;

        if (isValidationSuccessful) {
        	
        	systemSetupUtil.updateSystemSetupInProgressFlagValue(true);
            UserInfo userInfo = getUserDetails();

            List<CountryConfig> countryConfigs = new ArrayList<CountryConfig>();
            List<ProductConfig> productConfigs = new ArrayList<ProductConfig>();

            try {

                boolean isKeyFileEmpty, isTextFileEmpty, isKeyEmpty, isTextEmpty;

                isKeyFileEmpty = (attachedLicenseKeyFile.isEmpty());
                isTextFileEmpty = (attachedLicenseTextFile.isEmpty());
                isKeyEmpty = (seedConfiguration.getCompanyLicenseInfo() == null
                        || seedConfiguration.getCompanyLicenseInfo().getLicenseKey() == null || seedConfiguration
                        .getCompanyLicenseInfo().getLicenseKey().isEmpty());
                isTextEmpty = (seedConfiguration.getCompanyLicenseInfo() == null
                        || seedConfiguration.getCompanyLicenseInfo().getLicenseText() == null || seedConfiguration
                        .getCompanyLicenseInfo().getLicenseText().isEmpty());

                if ((!isKeyFileEmpty || !isKeyEmpty) && (!isTextFileEmpty || !isTextEmpty)) {
                    AppliedLicenses appliedLicense = new AppliedLicenses();

                    if (!isKeyEmpty) {
                        appliedLicense.setLicensePublicKey(seedConfiguration.getCompanyLicenseInfo().getLicenseKey());
                    } else {
                        InputStream keyFileInputStream = attachedLicenseKeyFile.getInputStream();
                        StringWriter keyWriter = new StringWriter();
                        IOUtils.copy(keyFileInputStream, keyWriter, "UTF-8");
                        String uploadedLicensekey = keyWriter.toString();
                        appliedLicense.setLicensePublicKey(uploadedLicensekey);
                    }

                    if (!isTextEmpty) {
                        appliedLicense.setLicenseText(seedConfiguration.getCompanyLicenseInfo().getLicenseText());
                    } else {
                        InputStream textFileInputStream = attachedLicenseTextFile.getInputStream();
                        StringWriter textWriter = new StringWriter();
                        IOUtils.copy(textFileInputStream, textWriter, "UTF-8");
                        String uploadedLicenseString = textWriter.toString();
                        appliedLicense.setLicenseText(uploadedLicenseString);
                    }
                    String userUri= CoreUtility.getUserUri();
                    licenseClientService.persist(appliedLicense,userUri);
                    BaseLoggers.flowLogger.info("Core-System-Setup : Applied license Saved.");
                    LicenseText licenseTextObj = new LicenseText(appliedLicense.getLicenseText(),
                            appliedLicense.getLicensePublicKey());
                    JAXBContext jaxbContext = JAXBContext.newInstance(LicenseFeature.class);
                    Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
                    StringReader reader = new StringReader(licenseTextObj.getLicenseProperties().getProperty("licf-custom"));
                    LicenseFeature licFeature = (LicenseFeature) unmarshaller.unmarshal(reader);
                 
                    createInternalTrustedSourceFromLicense(licFeature.getLicenseDetail());
                    Map<String ,LicenseDetail> productCodeLicenseDetailMap=new HashMap<>();
                    productCodeLicenseDetailMap.put(ProductInformationLoader.getProductCode(), licFeature.getLicenseDetail());
                    licenseClientCacheService.update(NeutrinoCachePopulator.Action.INSERT, productCodeLicenseDetailMap);
                  
                    BaseLoggers.flowLogger.info("Core-System-Setup : LicenseInformation loaded.");             
                    BaseLoggers.flowLogger.info("Core-System-Setup : Core framework System Setup Done.");
                }

            } catch (Exception e) {
                BaseLoggers.exceptionLogger.error(e.getClass() + " occured while saving license key/text file");
                throw new SystemException(e);
            }
            Locale loc = RequestContextUtils.getLocale(request);
			try {
				if (seedConfiguration.getCountryConfigs() != null) {
					for (Long countryConfigId : seedConfiguration
							.getCountryConfigs()) {
						countryConfigs.add(systemSetupService
								.findAbstractConfigById(CountryConfig.class,
										countryConfigId));
					}
				}
			} catch (SystemException e) {
				String message = messageSource.getMessage(
						ERROR_COUNTRY_CONFIG_MESSAGE, null, loc);
				BaseLoggers.exceptionLogger.error(e + ERROR_COUNTRY_CONFIG);
				updateSystemConfigurations(map,seedConfiguration);
				map.put(ERROR_MESSAGE, message);
				return REDIRECT_SYSTEM_CONFIG_PAGE;

			}
			try {
				if (seedConfiguration.getProductConfigs() != null
						&& seedConfiguration.getProductConfigs().length > 0) {
					for (Long productConfigId : seedConfiguration
							.getProductConfigs()) {
						productConfigs.add(systemSetupService
								.findAbstractConfigById(ProductConfig.class,
										productConfigId));
					}
				}
			} catch (SystemException e) {
				String message = messageSource.getMessage(
						ERROR_PRODUCT_CONFIG_MESSAGE, null, loc);
				BaseLoggers.exceptionLogger.error(e + ERROR_PRODUCT_CONFIG);
				updateSystemConfigurations(map,seedConfiguration);
				map.put(ERROR_MESSAGE, message);
				return REDIRECT_SYSTEM_CONFIG_PAGE;
			}
			if (seedConfiguration.getUserProfile() != null) {
				if(seedConfiguration.getUserProfile().getAssociatedUser() != null){
				seedConfiguration.getUserProfile().getAssociatedUser().setLoginEnabled(true);
				}
				companyLicenseInfo.setUserProfile(seedConfiguration
						.getUserProfile());
			}

			companyLicenseInfo.setCountryConfigs(countryConfigs);
			companyLicenseInfo.setProductConfigs(productConfigs);

			if (userInfo != null && userInfo.getUserEntityId() != null) {
				systemSetupService.saveConfiguration(companyLicenseInfo,
						userInfo.getUserEntityId());
			}

			List<String> resourceNames = new ArrayList<>();

			if (countryConfigs != null && !countryConfigs.isEmpty()) {
				for (CountryConfig countryConfig : countryConfigs) {
					resourceNames.add(countryConfig.getFileName());
				}
			}
			if(!SourceProduct.SOURCE_PRODUCT_NEUTRINO_FW.equalsIgnoreCase(ProductInformationLoader.getProductCode())){
				if(((SystemSetupServiceImpl) systemSetupService).isConsolidatedSeedOperation()) {
                    resourceNames.add("post-startUp/GENERIC_PRODUCT.xls");
                } else {
                    resourceNames.add("db-init/generic_product.xls");
                }
			}
			
			if (productConfigs != null && !productConfigs.isEmpty()) {
				for (ProductConfig productConfig : productConfigs) {
					resourceNames.add(productConfig.getFileName());
				}
			}
			
			
			
			if (resourceNames != null && !resourceNames.isEmpty()) {
				futures = systemSetupService
						.executeSeedOperation(resourceNames);
				int totalFutures = futures.size();
				int counter = 0;
				int totalPercentage = 0;
				while (CollectionUtils.isNotEmpty(futures)) {
					Iterator<Future<String>> it = futures.listIterator();
					while (it.hasNext()) {
						Future<String> futureTask = it.next();
						Boolean futureValue = futureTask.isDone();
						if (futureValue) {
							try {
								futureTask.get();
							} catch (CancellationException e) {
								String message = messageSource
										.getMessage(
												ERROR_TASK_CANCELLATION_MESSAGE,
												null, loc);
								
								BaseLoggers.exceptionLogger
										.error(e
												+ ERROR_TASK_CANCELLATION);
								message = message +fetchAllResourceNames(resourceNames);
								updateSystemConfigurations(map,seedConfiguration);
								map.put(ERROR_MESSAGE, message);
								return REDIRECT_SYSTEM_CONFIG_PAGE;
							} catch (InterruptedException e) {
								String message = messageSource
										.getMessage(
												ERROR_TASK_INTERRUPTION_MESSAGE,
												null, loc);
								BaseLoggers.exceptionLogger
										.error(e
												+ ERROR_TASK_INTERRUPTION);
								message = message +fetchAllResourceNames(resourceNames);
								updateSystemConfigurations(map,seedConfiguration);
								map.put(ERROR_MESSAGE, message);
								return REDIRECT_SYSTEM_CONFIG_PAGE;
							} catch (ExecutionException e) {
								String message = messageSource
										.getMessage(
												ERROR_TASK_EXECUTION_MESSAGE,
												null, loc);
								BaseLoggers.exceptionLogger
										.error(e
												+ ERROR_TASK_EXECUTION);
								message = message +fetchAllResourceNames(resourceNames);
								updateSystemConfigurations(map,seedConfiguration);
								map.put(ERROR_MESSAGE, message);
								return REDIRECT_SYSTEM_CONFIG_PAGE;
							}
							it.remove();
							counter++;
							totalPercentage = getPercentageCompletionOfSeed(
									totalFutures, counter);
							BaseLoggers.flowLogger.debug("System setup is "
									+ totalPercentage + " % COMPLETED ");
						}
					}
				}
			}
			try {
				frameworkServiceLocator.getSystemSetupPreCompletionService()
						.execute();
			} catch (SystemException e) {
				String message = messageSource.getMessage(
						ERROR_SYSTEM_PRECOMPLETION_MESSAGE, null, loc);
				BaseLoggers.exceptionLogger.error(e
						+ ERROR_SYSTEM_PRECOMPLETION);
				updateSystemConfigurations(map,seedConfiguration);
				map.put(ERROR_MESSAGE, message);
				return REDIRECT_SYSTEM_CONFIG_PAGE;
			}catch(org.springframework.beans.factory.NoSuchBeanDefinitionException beanDefinitionException){
				BaseLoggers.exceptionLogger.error(
						"No beandefinition of type SystemSetupPreCompletionService found - system will continue", beanDefinitionException);
			}
			try {
				userSessionManagerService
						.invalidateCurrentLoggedinUserSession();
			} catch (SystemException e) {
				String message = messageSource.getMessage(
						ERROR_CURRENT_LOGGEDIN_USER_MESSAGE, null, loc);
				BaseLoggers.exceptionLogger.error(e
						+ ERROR_CURRENT_LOGGEDIN_USER);
				updateSystemConfigurations(map,seedConfiguration);
				map.put(ERROR_MESSAGE, message);
				return REDIRECT_SYSTEM_CONFIG_PAGE;
			}
		    systemSetupUtil.updateSystemSetupFlagValue(true);
		    licenseSetupUtil.updateSystemSetupFlagValue(true);
		    licenseSetupUtil.checkLicenseExpiryMessage(request);
		    systemSetupUtil.updateSystemSetupInProgressFlagValue(false);
			 return "systemSetUpLogout";
        } else {
        	updateSystemConfigurations(map,seedConfiguration);
            map.put(ERROR_MESSAGE, tempResultArray[0]);
            return REDIRECT_SYSTEM_CONFIG_PAGE;
        }

    }

	private void createInternalTrustedSourceFromLicense(LicenseDetail licenseDetail) {
		
		//deactiveAlreadyCreateTrustedSource();
		List<LicenseMobilityModuleInfo> mobilityModuleInfoList = licenseDetail.getLicenseMobilityModuleInfoList();
		if (mobilityModuleInfoList == null || mobilityModuleInfoList.isEmpty())
			return;
		List<APIDetails> apiDetailsList = apiManagementService.getAPIDetailsFromLicense(licenseDetail.getLicenseApiDetailsVOList());
		trustedSourceRegistrationService.registerInternalTrustedSourceFromLicense(mobilityModuleInfoList,
				licenseDetail.getEmailId(), apiDetailsList,getUserDetails().getUserReference());

	}


	private void deactiveAlreadyCreateTrustedSource() {
	trustedSourceRegistrationService.deActivateInternalTrustedSource();
	
	
	}


	private int getPercentageCompletionOfSeed(int totalTasks, int tasksCompleted) {


        return  (tasksCompleted * 100) / totalTasks;

    }

    @RequestMapping(value = "/validateLicenseTextAndKey", method = RequestMethod.POST)
    public @ResponseBody
    String validateLicenseTextAndKey(
            @RequestParam(value = "companyLicenseInfo.licenseKey", required = false) String licensePublicKey,
            @RequestParam(value = "companyLicenseInfo.licenseText", required = false) String licenseText,
            @RequestParam(value = "attachedLicenseKeyFile", required = false) CommonsMultipartFile attachedLicenseKeyFile,
            @RequestParam(value = "attachedLicenseTextFile", required = false) CommonsMultipartFile attachedLicenseTextFile,
            HttpServletRequest request) throws ParseException, JAXBException, IOException {
    	if (systemSetupUtil.isSystemSetup()) {
            BaseLoggers.flowLogger.debug("Detected that system is already setup. Redirecting to main module..");
            return "redirect:" + systemSetupUtil.getLoginFormUrl();
        }


        String validationResult = null;

        validationResult = seedConfigurationValidator.validateLicenseKeyText(licensePublicKey, licenseText,
                attachedLicenseKeyFile, attachedLicenseTextFile, request);

        return validationResult;

    }

    @RequestMapping(value = "/returnSystemSetUpError", method = RequestMethod.GET)
    public @ResponseBody
    String returnSystemSetUpError(@RequestParam(value = "message") String message, ModelMap map) {
    	if (systemSetupUtil.isSystemSetup()) {
            BaseLoggers.flowLogger.debug("Detected that system is already setup. Redirecting to main module..");
            return "redirect:" + systemSetupUtil.getLoginFormUrl();
        }
        return message;
    }
    @PreAuthorize("hasAuthority('APPLY_LICENSE')")
    @RequestMapping(value = "/license/licenseSetup", method = RequestMethod.GET)
    public String getlicenseSetupString(ModelMap map) {
    	if (systemSetupUtil.isSystemSetup()) {
            BaseLoggers.flowLogger.debug("Detected that system is already setup. Redirecting to main module..");
            return "redirect:" + systemSetupUtil.getLoginFormUrl();
        }
        map.put("seedConfiguration", new SeedConfiguration());
        if (licenseSetupUtil.isSystemSetup())
            return "redirect:/app/auth/login";
        else
            return "/systemSetup/licenseSetup";
    }
    
	
	@PreAuthorize("hasAuthority('APPLY_LICENSE')")
    @RequestMapping(value = "/license/saveLicenseTextAndKey", method = RequestMethod.POST)
    public @ResponseBody String putLicenseTextAndKey(SeedConfiguration seedConfiguration,
            @RequestParam(value = "companyLicenseInfo.licenseKey", required = false) String licensePublicKey,
            @RequestParam(value = "companyLicenseInfo.licenseText", required = false) String licenseText,
            @RequestParam(value = "attachedLicenseKeyFile", required = false) CommonsMultipartFile attachedLicenseKeyFile,
            @RequestParam(value = "attachedLicenseTextFile", required = false) CommonsMultipartFile attachedLicenseTextFile,
            HttpServletRequest request, ModelMap map) throws ParseException, JAXBException, IOException {
    	
        String validationResult = null;
        CompanyLicenseInfo companyLicenseInfo = seedConfiguration.getCompanyLicenseInfo();
        if (companyLicenseInfo != null) {
            validationResult = seedConfigurationValidator.validateLicenseKeyText(companyLicenseInfo.getLicenseKey(),
                    companyLicenseInfo.getLicenseText(), attachedLicenseKeyFile, attachedLicenseTextFile, request);
        }

        String tempResultArray[] = validationResult.split(",");

        boolean isValidationSuccessful = false;

        if (tempResultArray[1].equals(MSG_SUCCESS))
            isValidationSuccessful = true;
        if (isValidationSuccessful) {
            try {
                boolean isKeyFileEmpty, isTextFileEmpty, isKeyEmpty, isTextEmpty;
                isKeyFileEmpty = (attachedLicenseKeyFile.isEmpty());
                isTextFileEmpty = (attachedLicenseTextFile.isEmpty());
                isKeyEmpty = (seedConfiguration.getCompanyLicenseInfo() == null
                        || seedConfiguration.getCompanyLicenseInfo().getLicenseKey() == null || seedConfiguration
                        .getCompanyLicenseInfo().getLicenseKey().isEmpty());
                isTextEmpty = (seedConfiguration.getCompanyLicenseInfo() == null
                        || seedConfiguration.getCompanyLicenseInfo().getLicenseText() == null || seedConfiguration
                        .getCompanyLicenseInfo().getLicenseText().isEmpty());
                if ((!isKeyFileEmpty || !isKeyEmpty) && (!isTextFileEmpty || !isTextEmpty)) {
                    AppliedLicenses appliedLicense = new AppliedLicenses();
                    if (!isKeyEmpty) {
                        appliedLicense.setLicensePublicKey(seedConfiguration.getCompanyLicenseInfo().getLicenseKey());
                        licensePublicKey = seedConfiguration.getCompanyLicenseInfo().getLicenseKey();
                    } else {
                        InputStream keyFileInputStream = attachedLicenseKeyFile.getInputStream();
                        StringWriter keyWriter = new StringWriter();
                        IOUtils.copy(keyFileInputStream, keyWriter, "UTF-8");
                        String uploadedLicensekey = keyWriter.toString();
                        licensePublicKey = uploadedLicensekey;
                        appliedLicense.setLicensePublicKey(uploadedLicensekey);
                    }
                    if (!isTextEmpty) {
                        appliedLicense.setLicenseText(seedConfiguration.getCompanyLicenseInfo().getLicenseText());
                        licenseText = seedConfiguration.getCompanyLicenseInfo().getLicenseText();
                    } else {
                        InputStream textFileInputStream = attachedLicenseTextFile.getInputStream();
                        StringWriter textWriter = new StringWriter();
                        IOUtils.copy(textFileInputStream, textWriter, "UTF-8");
                        String uploadedLicenseString = textWriter.toString();
                        licenseText = uploadedLicenseString;
                        appliedLicense.setLicenseText(uploadedLicenseString);
                    }
                    String userUri= CoreUtility.getUserUri();
                    licenseClientService.disableActiveLicenses(ProductInformationLoader.getProductCode(),userUri);
                    licenseClientService.persist(appliedLicense,userUri);
                    BaseLoggers.flowLogger.debug("Core-System-Setup : Applied license Saved.");
                    LicenseText licenseTextObj = new LicenseText(licenseText, licensePublicKey);
                
                    JAXBContext jaxbContext = JAXBContext.newInstance(LicenseFeature.class);
                    Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
                    StringReader reader = new StringReader(licenseTextObj.getLicenseProperties().getProperty("licf-custom"));
                    LicenseFeature licFeature = (LicenseFeature) unmarshaller.unmarshal(reader);
                   
                    Map<String ,LicenseDetail> productCodeLicenseDetailMap=new HashMap<>();
                    productCodeLicenseDetailMap.put(ProductInformationLoader.getProductCode(), licFeature.getLicenseDetail());
                    licenseClientCacheService.update(NeutrinoCachePopulator.Action.INSERT, productCodeLicenseDetailMap);
                    createInternalTrustedSourceFromLicense(licFeature.getLicenseDetail());
                 
                    BaseLoggers.flowLogger.debug("Core-System-Setup : LicenseInformation loaded.");
                    systemSetupUtil.updateSystemSetupFlagValue(true);
                    licenseSetupUtil.updateSystemSetupFlagValue(true);
                    licenseSetupUtil.checkLicenseExpiryMessage(request);
                    BaseLoggers.flowLogger.debug("Core-System-Setup : Core framework System Setup Done.");
                }
            } catch (Exception e) {
                BaseLoggers.exceptionLogger.error(e.getClass() + " occured while saving license key/text file");
                throw new SystemException(e);
            }
        } else {
            map.put("errorMessage", tempResultArray[0]);
        }
        return validationResult;
    }

   

    @PreAuthorize("hasAuthority('APPLY_LICENSE')")
	@RequestMapping(value = "/license/updateLicense", method = RequestMethod.GET)
    public String updateLicensePage(ModelMap map) {
    	map.put("seedConfiguration", new SeedConfiguration());
        return "/systemSetup/licenseSetup";
    }
    
   /* @RequestMapping(value = "/license/getDetails", method = RequestMethod.GET)
    public String getActiveLicenseDetails(ModelMap map, HttpServletRequest request) throws JAXBException {
    	String productCode=ProductInformationLoader.getProductCode();
        LicenseInfoVO licenseInfoVO = licenseClientService.getLicenseInfo(request,productCode);
        licenseInfoVO.setNumOfNamedUserConsumed( userService.getUsersCountByProductName(productCode,""));
        map.put("licenseInfo", licenseInfoVO);
        return "licenseDetailPage";
    }*/
    @ResponseBody
    @RequestMapping(value = "/license/hideAlert", method = RequestMethod.GET)
    public void hideLicenseAlert(ModelMap map, HttpServletRequest request) throws JAXBException {
    
        licenseClientService.hideLicenseAlert(request);
        
        
    }
    
    @ResponseBody
    @RequestMapping(value = "/license/hideNamedUserAlert", method = RequestMethod.GET)
    public void hideNamedUserAlert(ModelMap map, HttpServletRequest request) throws JAXBException {
    
    	if (request.getSession().getAttribute(LicenseSetupUtil.LICENSE_ALERT_ON_THRESHOLD_NAMED_USER) != null) {

			request.getSession().removeAttribute(LicenseSetupUtil.LICENSE_ALERT_ON_THRESHOLD_NAMED_USER);
		}
		
		if (request.getSession().getAttribute(LicenseSetupUtil.LICENSE_ALERT_AFTER_MAX_NAMED_USER) != null) {

			request.getSession().removeAttribute(LicenseSetupUtil.LICENSE_ALERT_AFTER_MAX_NAMED_USER);
		}
        
        
    }
    
    
    private String fetchAllResourceNames(List<String> resourceNames) {
    	String consolidatedResourceNames = "";
    	for (String resourceName:resourceNames) {
    		consolidatedResourceNames = consolidatedResourceNames +","+resourceName;
    	}
    	
    	return consolidatedResourceNames;
    }
    private void updateSystemConfigurations(ModelMap map,SeedConfiguration seedConfiguration) {
    	List<CountryConfig> countryConfigs = new ArrayList<>();
        List<ProductConfig> productConfigs = new ArrayList<>();

        countryConfigs.addAll(systemSetupService.getAllSeedConfigEntities(CountryConfig.class));
        productConfigs.addAll(systemSetupService.getAllSeedConfigEntities(ProductConfig.class));
        List<ApplicationFeatures> appFeaturesList = systemSetupService.getallApplicationFeatures();

        map.put("countryConfigs", countryConfigs);
        map.put("productConfigs", productConfigs);
        map.put("applicationFeaturesList", appFeaturesList);

        map.put("seedConfiguration", seedConfiguration);
    }

    @RequestMapping(value = "/license/getDetailsJson/{productCode}", method = RequestMethod.GET)
    public @ResponseBody LicenseInfoVO getActiveLicenseDetailsJson(HttpServletRequest request, @PathVariable("productCode") String productCode) throws JAXBException {
        
        LicenseInfoVO licenseInfoVO = licenseClientService.getLicenseInfo(request,productCode);
        licenseInfoVO.setNumOfNamedUserConsumed( userService.getUsersCountByProductName(productCode,"null"));
        if(ProductInformationLoader.getProductCode().equals(productCode))
        {
        	licenseInfoVO.setCurrentModuleFlag(true);
        }
        List<LicenseMobilityModuleInfo> mobilityModuleInfoAssociatedWithCurrentUser=licenseInfoVO.getMobilityModuleInfoList();
        List<LicenseMobilityModuleInfo> mobilityModuleInfoNotAssociatedWithCurrentUser=new ArrayList<>();
        List<Long> userRoleIDs = new ArrayList(userService.getCurrentUser().getUserRoleIds());
		
		List<String> productAssociatedWithUser = userService.getProductListFromRoleIds(userRoleIDs);
		
        if(mobilityModuleInfoAssociatedWithCurrentUser!=null )
        {
        	for(LicenseMobilityModuleInfo mobilityInfoVO:licenseInfoVO.getMobilityModuleInfoList())
        	{
        		
        		 if(!mobilityInfoVO.isAnnonymousModule())
        		{
        			 if(!productAssociatedWithUser.contains(mobilityInfoVO.getMobilityModuleCode()))
     				{
     			     mobilityModuleInfoNotAssociatedWithCurrentUser.add(mobilityInfoVO);
     				}
        			mobilityInfoVO.setNamedUserConsumed(userService.getUsersCountByProductName(mobilityInfoVO.getMobilityModuleCode(),"null"));
        		}
        	}
        	if(!mobilityModuleInfoNotAssociatedWithCurrentUser.isEmpty())
            {
            mobilityModuleInfoAssociatedWithCurrentUser.removeAll(mobilityModuleInfoNotAssociatedWithCurrentUser);
            licenseInfoVO.setMobilityModuleInfoList(mobilityModuleInfoAssociatedWithCurrentUser);
            }
        }
        
        SourceProduct sourceProduct = genericParameterService.findByCode(licenseInfoVO.getModuleCode(), SourceProduct.class);
        licenseInfoVO.setModuleCode(sourceProduct.getName());
        return licenseInfoVO;
    }
    
   @RequestMapping(value = "/license/getLicensedProductCode", method = RequestMethod.GET)
    public @ResponseBody Map<String,String> getLicensedProductCodeList(HttpServletRequest request) throws JAXBException {
	   
	   Map<String,String> licenseModuleData = new HashMap<>();
	   for(String moduleCode : licenseClientCacheService.getLicensedModuleList()) {
		   SourceProduct sourceProduct = genericParameterService.findByCode(moduleCode, SourceProduct.class);
		   licenseModuleData.put(moduleCode, sourceProduct.getName());
	   }
	   
	   return licenseModuleData;
      
    }
    
}
