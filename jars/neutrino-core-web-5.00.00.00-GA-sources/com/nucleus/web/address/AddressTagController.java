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
package com.nucleus.web.address;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nucleus.address.*;
import com.nucleus.autocomplete.AutocompleteVO;
import com.nucleus.html.util.HtmlUtils;
import com.nucleus.web.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.conn.routing.HttpRoutePlanner;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeader;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.RequestContextUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.nucleus.core.datastore.service.DatastorageService;
import com.nucleus.core.exceptions.SystemException;
import com.nucleus.core.genericparameter.service.GenericParameterService;
import com.nucleus.core.initialization.NeutrinoResourceLoader;
import com.nucleus.core.initialization.ProductInformationLoader;
import com.nucleus.config.persisted.service.ConfigurationService;
import com.nucleus.core.loanproduct.ProductType;
import com.nucleus.core.villagemaster.entity.VillageMaster;
import com.nucleus.dao.query.NamedQueryExecutor;
import com.nucleus.fieldConfig.FieldConfig;
import com.nucleus.finnone.pro.general.util.ValidatorUtils;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.persistence.EntityDao;
import com.nucleus.query.constants.QueryHint;
import com.nucleus.rules.model.SourceProduct;
import com.nucleus.web.common.controller.BaseController;

import flexjson.JSONSerializer;

/**
 * @author Nucleus Software Exports Limited This field is being used for
 *         controlling Address CRUD and task allocation work-flow related
 *         operations.
 */
@Transactional
@Controller
@RequestMapping(value = "/AddressTag")
public class    AddressTagController extends BaseController {

    public static final String POSTAL_CODE = "postalCode";
    public static final String FALSE = "false";
    public static final String COUNTRY_REGION_LIST = "countryRegionList";
    public static final String STATE_LIST = "stateList";
    public static final String CITY_LIST = "cityList";
    public static final String DISTRICT_LIST = "districtList";
    public static final String ICICI = "icici";
    public static final String ZIP_CODE_LIST = "zipCodeList";
    public static final String ADDRESS = "address";
    public static final String REGION_LIST = "regionList";
    public static final String COUNTRY_CODE = "countryCode";
    public static final String COUNTRY_ISD_CODE = "countryISDCode";
    public static final String TEHSIL_ID_LIST = "tehsilIdList";
    public static final String VILLAGE_ID_LIST = "villageIdList";
    public static final String DISTRICT_NAME = "districtName";
    public static final String MASKED_DISTRICT_NAME = "maskedDistrictName";
    public static final String TALUKA = "taluka";
    public static final String TRUE = "true";
    public static final String NULL = "null";
    public static final String CLASS = "*.class";
    public static final String ZIP_CODE = "zipCode";
    public static final String XPATH_TARGET_CNTRY = "/address-configs/address-config/target-country[@name=" + "'";
    public static final String XPATH_FIELD_CONFIG = "']/field-config";
    public static final String XPATH_TARGET_CNTRY_COMMA = "/address-configs/address-config/target-country[contains(@name," + "'";
    public static final String XPATH_TARGET_ADDRESS_TYPE = "')]/address-type[contains(@name," + "'";
    public static final String XPATH_TARGET_FILTER_CODE = "')]/filter-code[contains(@name," + "'";
    public static final String XPATH_FIELD_CONFIG_COMMA = "')]/field-config";
    public static final String MAX_LENGTH = "maxLength";
    public static final String STREET_LIST = "streetMasterList";

    private static Map<String,String> localeIsoCodeByCountryNameMap;
    private static Map<String,List<FieldConfig>> evaluatedXpathFieldConfigsMap;
    private static String nullZipCodeJson;
    
    @Inject
    @Named("couchDataStoreDocumentService")
    private DatastorageService       docService2;


    @Inject
    @Named("addressInitializer")
    private AddressInitializer addressInitializer;
    
    @Inject
    @Named("entityDao")
    private EntityDao       entityDao;

    private static XPathFactory      factory = XPathFactory.newInstance();
    private static Document                 document;

    @Inject
    @Named("frameworkConfigResourceLoader")
    protected NeutrinoResourceLoader frameworkConfigResourceLoader;
	
	@Inject
    @Named("configurationService")
    public ConfigurationService configurationService;

    @Value("${cas.client}")
    private String                   profileName;

    // Properties for proxy settings
    @Value(value = "#{'${addressTagController.ipaddress}'}")
    private String                   ipaddress;

    @Value(value = "#{'${addressTagController.port}'}")
    private String                   port;

    @Value(value = "#{'${addressTagController.username}'}")
    private String                   username;

    @Value(value = "#{'${addressTagController.password}'}")
    private String                   password;

    @Value(value = "#{'${addressTagController.isProxyEnabled}'}")
    private String                   isProxyEnabled;

    @Value("${show.ownership.fields.in.address.for.officetype}")
    private String                   showFieldsForOfficeTypeFlag;

    @Value("${show.ownership.fields.in.address.for.alternatetype}")
    private String                   showFieldsForAlternateTypeFlag;

    private static final String         DEFAULT                                   = "default";
    private static final String         CHOSEN_CLASS                                   = "chosen_a";
    private static final String AREA_LIST="areaList";
	
	private static final String        CONFIGURATION_QUERY             = "Configuration.getPropertyValueFromPropertyKey";
	
    @PostConstruct
    public void initializeDocument() throws ParserConfigurationException, IOException, SAXException  {
        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        InputStream is = frameworkConfigResourceLoader.getResource("country-specific-fields-mapping.xml").getInputStream();
        document = builder.parse(is);
        evaluatedXpathFieldConfigsMap = new ConcurrentHashMap<>();
        localeIsoCodeByCountryNameMap = new ConcurrentHashMap<>();
        String[] countries = Locale.getISOCountries();
        String language = "en";
        for (String eachCountry : countries) {
            Locale locale = new Locale(language, eachCountry);
            localeIsoCodeByCountryNameMap.put((locale.getDisplayCountry()).toLowerCase(), locale.getISO3Country());
        }
    }

    @Inject
    @Named("addressService")
    private AddressTagService addressService;

    @Inject
    @Named("genericParameterService")
    private GenericParameterService genericParameterService;


    @Inject
    @Named("addressServiceCore")
    private AddressService addressServiceCore;


    /**
     * @param
     * @return String
     * @throws IOException
     * @description to create address Tag form
     */
    @RequestMapping(value = "/createAddress")
    public String createAddressTag(ModelMap map, HttpServletRequest request) {
        map.put(POSTAL_CODE, new PostalCode());
        Properties sysProps = System.getProperties();
        sysProps.getProperty("user.country");
        map.put("countryName", "JP");
        return "addressTagContainer";
    }

    @RequestMapping(value = "/paintRestFields", method = RequestMethod.POST)
    public String paintRestFields(@RequestParam(value = "countryId", required = false) Long countryId,
                                  @RequestParam(value = "addressType", required = false) Long addressType,
                                  @RequestParam(value = "addressTypePresent", required = false) boolean addressTypePresent,
                                  @RequestParam(value = "filterCode", required = false) String filterCode,
                                  @RequestParam(value = "relativePath", required = false) String relativePath, HttpServletRequest request,
                                  @RequestParam(value = "addressId", required = false) Long addressId,
                                  @RequestParam(value = "source", required = false) String source,
                                  @RequestParam(value = "htmlAddressId", required = false) String htmlAddressId,
                                  @RequestParam(value = "enableMapValue", required = false) String enableMapValue,
                                  @RequestParam(value = "tabCount", required = false) Integer tabCount,
                                  @RequestParam(value = "mandatoryAttr", required = false) String mandatoryAttr,
                                  @RequestParam(value = "addressWithMap", required = false) String addressWithMap,
                                  @RequestParam(value = "addressLine4", required = false) String addressLine4,
                                  @RequestParam(value = "remarks", required = false) String remarks,
                                  @RequestParam(value = "genericYesNo", required = false) Long genericYesNoId,
                                  @RequestParam(value = "additionalField1", required = false) String additionalField1,
                                  @RequestParam(value = "additionalField2", required = false) String additionalField2,
                                  @RequestParam(value = "additionalField3", required = false) String additionalField3,
                                  @RequestParam(value = "additionalField4", required = false) String additionalField4,
                                  @RequestParam(value = "additionalField5", required = false) String additionalField5,
                                  @RequestParam(value = "decorateSelectTag", required = false, defaultValue= "true") String decorateSelectTag,
                                  @RequestParam(value = "disableISDCode", required = false, defaultValue= FALSE) String disableISDCode,
                                  @RequestParam(value = "residenceEnabled", required = false) String residenceEnabled,
                                  @RequestParam(value = "modificationAllowed", required = false) String modificationAllowed,
                                  @RequestParam(value = "viewModeMask", required = false) String viewModeMask,
                                  @RequestParam(value="columnLayout" , required = false) String columnLayout,ModelMap map)
            throws XPathExpressionException {
        String message = "";
        String countryISDCode = "";
        String countryISOCode = "";
        String addressTypeCode="";
		
		String pincodeValidation = FALSE;
        String moduleCode = "";
        if (ProductInformationLoader.productInfoExists())        {
            moduleCode = ProductInformationLoader.getProductCode();
            pincodeValidation = configurationService.getPropertyValueByPropertyKey("custom.pincode.validation."+moduleCode.toLowerCase(),CONFIGURATION_QUERY);
            if(pincodeValidation == null){
                pincodeValidation = FALSE;
            }
			pincodeValidation = pincodeValidation.toLowerCase();
        }
        map.put("enableCustomPincode",pincodeValidation);
		
        String chosenClass=CHOSEN_CLASS;
        Country addressCountry = null;
        boolean stateFound = false;
        boolean cityfound = false;
        boolean zipCodefound = false;
        if(addressType!=null){
            AddressType addressType1=genericParameterService.findById(addressType,AddressType.class);
            addressTypeCode=addressType1.getCode();
        }
        if(decorateSelectTag!=null && decorateSelectTag.equals(FALSE)){
            chosenClass="";
        }
        if(disableISDCode!=null){
            map.put("disableISDCode", disableISDCode);
        }
        map.put("chosenClass", chosenClass);
        map.put("residenceEnabled", residenceEnabled);
        if (countryId != null) {
            addressCountry = baseMasterService.getMasterEntityById(Country.class, countryId);

        }
        map.put("htmlAddressId", htmlAddressId);
        if (addressCountry != null) {
            countryISOCode = addressCountry.getCountryISOCode();
            countryISDCode = countryISDCode + addressCountry.getCountryISDCode();

        }
        if (addressId != null) {
            Address address = addressService.findAddressById(addressId);
            // auto populate all regions based on country selected

            if (address.getCountry() != null && address.getCountry().getId() != null) {
                List<IntraCountryRegion> regionList = addressService.findIntraCountryRegionByCountryId(address.getCountry()
                        .getId());
                map.put(COUNTRY_REGION_LIST, regionList);
            }

            if (address.getRegion() != null && address.getRegion().getId() != null) {
                List<State> stateList = addressService.findAllStateInIntraCountryRegion(address.getRegion().getId());
                map.put(STATE_LIST, stateList);
                stateFound = true;

            }
            if (!stateFound && address.getCountry() != null && address.getCountry().getId() != null) {
                List<State> stateList = addressService.findAllStateInCountry(address.getCountry().getId());
                map.put(STATE_LIST, stateList);
            }
            if (address.getState() != null && address.getState().getId() != null) {
                List<City> cityList = addressService.findAllCityInState(address.getState().getId());
                map.put(CITY_LIST, cityList);
                List<District> districtList = addressService.findAllDistrictInState(address.getState().getId());
                map.put(DISTRICT_LIST, districtList);
                cityfound = true;

            }

            if (!cityfound && address.getCountry() != null && address.getCountry().getId() != null) {
                List<City> cityList = addressService.findAllCityInCountry(address.getCountry().getId());
                map.put(CITY_LIST, cityList);
            }


            if (address.getCity() != null && address.getCity().getId() != null) {
                List<Area> areaList = addressService.findAllAreaInCity(address.getCity().getId());
                map.put(AREA_LIST, areaList);
                if (profileName.equalsIgnoreCase(ICICI)) {
                    List<ZipCode> zipcodesList = new ArrayList<>();
                    map.put(ZIP_CODE_LIST, zipcodesList);
                } else {
                    List<ZipCode> zipcodesList = addressService.findAllZipCodeInCity(address.getCity().getId());
                    map.put(ZIP_CODE_LIST, zipcodesList);
                }
                zipCodefound = true;

                List<Street> streetList = addressService.findAllStreetInCity(address.getCity().getId());
                map.put(STREET_LIST,streetList);

            }

            if (!zipCodefound) {
                if (address.getState() != null && address.getState().getId() != null) {

                    if (profileName.equalsIgnoreCase(ICICI)) {
                        List<ZipCode> zipcodesList = new ArrayList<>();
                        map.put(ZIP_CODE_LIST, zipcodesList);
                    } else {
                        List<ZipCode> zipcodesList = addressService.findAllZipCodeInState(address.getState().getId());
                        if (ValidatorUtils.hasElements(zipcodesList)) {
                            map.put(ZIP_CODE_LIST, zipcodesList);
                        }
                    }

                } else if (address.getCountry() != null && address.getCountry().getId() != null) {

                    if (profileName.equalsIgnoreCase(ICICI)) {
                        List<ZipCode> zipcodesList = new ArrayList<>();
                        map.put(ZIP_CODE_LIST, zipcodesList);
                    } else {
                        List<ZipCode> zipcodesList = addressService.findAllZipCodeInCountry(address.getCountry().getId());
                        if (ValidatorUtils.hasElements(zipcodesList)) {
                            map.put(ZIP_CODE_LIST, zipcodesList);
                        }

                    }
                }
            }

            if (address.getCountry() != null) {
            	addressInitializer.initialize(address, AddressInitializer.AddressLazyAttributes.ZIPCODE,
            			AddressInitializer.AddressLazyAttributes.AREA, AddressInitializer.AddressLazyAttributes.DISTRICT,
            			AddressInitializer.AddressLazyAttributes.ACCOMODATION_TYPE, AddressInitializer.AddressLazyAttributes.RESIDENCE_TYPE);
            	countryISOCode = updateAddressAttrOfModelMap(countryId, map, address,addressTypeCode,addressTypePresent);
            }

        } else {
            Address newAddress = new Address();
            map.put(ADDRESS, newAddress);
        }

        // Find Region list belonging to the selected country
        if (addressCountry != null) {
            List<IntraCountryRegion> intraCountryRegions = addressService.findIntraCountryRegionByCountryId(addressCountry
                    .getId());
            map.put(REGION_LIST, intraCountryRegions);
            List<State> states = addressService.findAllStateInCountry(countryId);
            map.put(STATE_LIST, states);
        }
        
        paintFieldsData(countryId,addressTypeCode,filterCode,RequestContextUtils.getLocale(request),null,map,countryISOCode);
        
        
        map.put("completePath", relativePath);
        map.put("filterCode",filterCode);
        map.put("itemLabel", POSTAL_CODE);
        map.put(COUNTRY_ISD_CODE, countryISDCode);
        map.put("enableMapValue", enableMapValue);
        map.put("tabCount", tabCount);
        map.put("addressId", addressId);
        map.put("mandatoryAttr", mandatoryAttr);
        map.put("viewModeMask", viewModeMask);
        map.put("modificationAllowed", modificationAllowed);
        if (addressWithMap == null || StringUtils.isBlank(addressWithMap) || addressWithMap.equalsIgnoreCase(TRUE)) {
            return "addressImplementation";
        } else {
            return "address/withoutMapAddressImplementation";
        }
    }
    @RequestMapping(value = "/pincodeBased/paintRestFields", method = RequestMethod.POST)
    @Transactional(readOnly = true)
    public String paintRestFieldsPincodeImplementation(@RequestParam(value = "countryId", required = false) Long countryId,
                                                       @RequestParam(value = "addressType", required = false) Long addressType,
                                                       @RequestParam(value = "addressTypePresent", required = false) boolean addressTypePresent,
                                                       @RequestParam(value = "filterCode", required = false) String filterCode,
                                                       @RequestParam(value = "relativePath", required = false) String relativePath, HttpServletRequest request,
                                                       @RequestParam(value = "addressId", required = false) Long addressId,
                                                       @RequestParam(value = "appId", required = false) Long appId,
                                                       @RequestParam(value = "htmlAddressId", required = false) String htmlAddressId,
                                                       @RequestParam(value = "enableMapValue", required = false) String enableMapValue,
                                                       @RequestParam(value = "tabCount", required = false) Integer tabCount,
                                                       @RequestParam(value = "source", required = false) String source,
                                                       @RequestParam(value = "additionalParameter", required = false) String additionalParameter,
                                                       @RequestParam(value = "selectedZipCode", required = false) Long selectedZipCodeID,
                                                       @RequestParam(value = "addressLine1", required = false) String addressLine1,
                                                       @RequestParam(value = "addressLine2", required = false) String addressLine2,
                                                       @RequestParam(value = "addressLine3", required = false) String addressLine3,
                                                       @RequestParam(value = "addressLine4", required = false) String addressLine4,
                                                       @RequestParam(value = "remarks", required = false) String remarks,
                                                       @RequestParam(value = "genericYesNo", required = false) Long genericYesNoId,
                                                       @RequestParam(value = "additionalField1", required = false) String additionalField1,
                                                       @RequestParam(value = "additionalField2", required = false) String additionalField2,
                                                       @RequestParam(value = "additionalField3", required = false) String additionalField3,
                                                       @RequestParam(value = "additionalField4", required = false) String additionalField4,
                                                       @RequestParam(value = "additionalField5", required = false) String additionalField5,
                                                       @RequestParam(value = "poBox", required = false) String poBox,
                                                       @RequestParam(value = "street", required = false) String street,
                                                       @RequestParam(value = "district", required = false) String selectedDistrictId,
                                                       @RequestParam(value = "village", required = false) String village,
                                                       @RequestParam(value = "implementation_Type", required = false) boolean implementationType,
                                                       @RequestParam(value = "mandatoryAttr", required = false) String mandatoryAttr,
                                                       @RequestParam(value = "decorateSelectTag", required = false, defaultValue= "true") String decorateSelectTag,
                                                       @RequestParam(value = "disableISDCode", required = false, defaultValue= FALSE) String disableISDCode,
                                                       @RequestParam(value = "isThirdParty", required = false) String isThirdParty,
                                                       @RequestParam(value = "addressWithMap", required = false) String addressWithMap,
                                                       @RequestParam(value = "residenceEnabled", required = false) String residenceEnabled,
                                                       @RequestParam(value = "modificationAllowed", required = false) String modificationAllowed,
                                                       @RequestParam(value = "viewModeMask", required = false) String viewModeMask, ModelMap map,
                                                       @RequestParam(value = "addressDataVo", required = false) String addressDataVo,
                                                       @RequestParam(value="columnLayout" , required = false) String columnLayout)
            throws XPathExpressionException {

        String countryISDCode = "";
        String countryISOCode = "";
        String addressTypeCode="";
        String chosenClass = CHOSEN_CLASS;
        Address addressObjFromVo = null;
        if(addressDataVo!=null && StringUtils.isNotEmpty(addressDataVo)){
            String addressJson = HtmlUtils.htmlUnescape(addressDataVo);
            ObjectMapper mapper = new ObjectMapper();
            try {
                AddressDataVo addressDataVoObj = mapper.readValue(addressJson, AddressDataVo.class);
                addressObjFromVo = addressService.getAddressFromAddressDataVo(addressDataVoObj);
            } catch (IOException e) {
                BaseLoggers.exceptionLogger.error("Exception creating addressObjFromVo from Json : ", addressJson);
            }
        }
		String pincodeValidation = FALSE;
        String moduleCode = "";
        if (ProductInformationLoader.productInfoExists())        {
            moduleCode = ProductInformationLoader.getProductCode();
            pincodeValidation = configurationService.getPropertyValueByPropertyKey("custom.pincode.validation."+moduleCode.toLowerCase(),CONFIGURATION_QUERY);
            if(pincodeValidation == null){
                pincodeValidation = FALSE;
            }
			pincodeValidation = pincodeValidation.toLowerCase();
        }
        map.put("enableCustomPincode",pincodeValidation);

        ZipCode zipcodeSelected = null;

        AddressGeneric genericYesNo=null;
        if(genericYesNoId!=null) {
            genericYesNo = genericParameterService.findById(genericYesNoId, AddressGeneric.class);
        }
        if(addressType!=null){
            AddressType addressType1=genericParameterService.findById(addressType,AddressType.class);
            addressTypeCode=addressType1.getCode();
        }
        if (decorateSelectTag != null && decorateSelectTag.equals(FALSE)) {
            chosenClass = "";
        }
        if (disableISDCode != null) {
            map.put("disableISDCode", disableISDCode);
        }
        map.put("chosenClass", chosenClass);
        Map<String, Object> addressCountry = new HashMap<>();
        map.put("residenceEnabled", residenceEnabled);
        map.put(TEHSIL_ID_LIST, "{0:0}");
        map.put(VILLAGE_ID_LIST, "{0:0}");
        map.put("showFieldsForOfficeTypeFlag", showFieldsForOfficeTypeFlag);
        map.put("showFieldsForAlternateTypeFlag", showFieldsForAlternateTypeFlag);
        String selectedDistrictIdMasked=null;
        if (countryId != null) {
            addressCountry = addressService.getCountryDetailsByCountryId(countryId);

            if (implementationType) {
                if (selectedZipCodeID != null) {
					/*
					 * Code added to populate all the fields based on the
					 * selected country
					 */
                    zipcodeSelected = baseMasterService.getMasterEntityById(ZipCode.class, selectedZipCodeID);
                    map.put("selectedZipcodeValue", zipcodeSelected.getZipCode());
                    updateModelMapAttrBasedOnZipCodeSelected(map, zipcodeSelected);
                    if(zipcodeSelected!=null && zipcodeSelected.getCity()!=null && zipcodeSelected.getCity().getDistrict()!=null && zipcodeSelected.getCity().getDistrict().getId()!=null){
                        selectedDistrictId = String.valueOf(zipcodeSelected.getCity().getDistrict().getId());
                        selectedDistrictIdMasked=(String)zipcodeSelected.getCity().getDistrict().getTransientMaskingMap().get("districtName");
                    }
                } else {
                    List<IntraCountryRegion> regionList = addressService.findIntraCountryRegionByCountryId(countryId);
                    map.put(COUNTRY_REGION_LIST, regionList);
                }
                map.put("maskedSelectedZipcodeId", "XXXXXX");
                map.put("selectedZipcodeId", selectedZipCodeID);

            }

        }
        map.put("htmlAddressId", htmlAddressId);
        if (!addressCountry.isEmpty()) {
            countryISOCode = addressCountry.get("countryISOCode").toString();
            countryISDCode = countryISDCode + addressCountry.get(COUNTRY_ISD_CODE).toString();

        }
        if (addressId != null && !"true".equalsIgnoreCase(isThirdParty)) {
            countryISOCode = updateModelMapAttrBasedOnAddressId(countryId, addressId, source, map, countryISOCode,addressTypeCode,addressTypePresent);
        } else if(addressObjFromVo != null){
            countryISOCode = updateModelMapAttrBasedOnAddressVoObject(countryId, addressObjFromVo, source, map, countryISOCode,addressTypeCode,addressTypePresent);
        } else {
            prepareNewAddress(addressLine1, addressLine2, addressLine3,addressLine4,remarks,additionalField1,additionalField2,additionalField3,additionalField4,additionalField5,genericYesNo, poBox, street, map, zipcodeSelected);
        }

        // Find Region list belonging to the selected country / Find State list
        // for the selected Country
        if (!addressCountry.isEmpty()) {
            updateRegionListStateListAttrOfModelMap(countryId, map);
        }
        
        map.put("selectedDistrictId", selectedDistrictId);
        map.put("selectedDistrictIdMasked", selectedDistrictIdMasked);
        paintFieldsData(countryId,addressTypeCode,filterCode, RequestContextUtils.getLocale(request), appId, map, countryISOCode);
        map.put("completePath", relativePath);

        map.put("itemLabel", POSTAL_CODE);
        map.put(COUNTRY_ISD_CODE, countryISDCode);
        map.put("enableMapValue", enableMapValue);
        map.put("tabCount", tabCount);
        map.put("mandatoryAttr", mandatoryAttr);
        map.put("addressId", addressId);
        map.put("filterCode",filterCode);
        map.put("implementation_Type", implementationType);
        map.put("source", source);
        map.put("modificationAllowed", modificationAllowed);
        map.put("viewModeMask", viewModeMask);
        map.put("columnLayout",columnLayout);
        return viewForPaintRestFieldsPincodeImpl(addressWithMap);

    }

	private void paintFieldsData(Long countryId, String addressTypeCode, String filterCode, Locale loc, Long appId,
			ModelMap map, String countryISOCode) throws XPathExpressionException {
        
        String xpath = "";
        if(!StringUtils.isEmpty(filterCode) && !StringUtils.isEmpty(addressTypeCode) && !StringUtils.isEmpty(countryISOCode)) {
            xpath = XPATH_TARGET_CNTRY_COMMA + countryISOCode + XPATH_TARGET_ADDRESS_TYPE + addressTypeCode + XPATH_TARGET_FILTER_CODE + filterCode + XPATH_FIELD_CONFIG_COMMA;
        }else if(!StringUtils.isEmpty(addressTypeCode) && !StringUtils.isEmpty(countryISOCode)){
            xpath = XPATH_TARGET_CNTRY_COMMA + countryISOCode + XPATH_TARGET_ADDRESS_TYPE + addressTypeCode + XPATH_FIELD_CONFIG_COMMA;
        }else if(!StringUtils.isEmpty(filterCode) && !StringUtils.isEmpty(countryISOCode)){
            xpath = XPATH_TARGET_CNTRY_COMMA + countryISOCode + XPATH_TARGET_FILTER_CODE + filterCode + XPATH_FIELD_CONFIG_COMMA;
        } else if(!StringUtils.isEmpty(countryISOCode)){
            xpath = XPATH_TARGET_CNTRY_COMMA + countryISOCode + XPATH_FIELD_CONFIG_COMMA;
        } else{
            xpath =  XPATH_TARGET_CNTRY + countryISOCode + XPATH_FIELD_CONFIG;
        }

        evaluateXpathAndPrepareFields(xpath,loc,map,appId,countryId);
        map.put("countryIsoCode", countryISOCode);
        
    }

    private String viewForPaintRestFieldsPincodeImpl(String addressWithMap) {
        if (addressWithMap == null || StringUtils.isBlank(addressWithMap) || addressWithMap.equalsIgnoreCase("true")) {
            return "pincodeBasedAddressImplementation";
        } else {
            return "withoutMapPincodeBasedAddress";
        }
    }

   

    private void updateRegionListStateListAttrOfModelMap(Long countryId, ModelMap map) {
        List<IntraCountryRegion> intraCountryRegions = addressService.findIntraCountryRegionByCountryId(countryId);
        map.put(REGION_LIST, intraCountryRegions);
        List<State> states = addressService.findAllStateInCountry(countryId);
        map.put(STATE_LIST, states);
    }

    private String updateModelMapAttrBasedOnAddressId(Long countryId, Long addressId, String source, ModelMap map,
                                                      String countryISOCode,String addressTypeCode,boolean addressTypePresent) {
        Address address = null;
        if (source != null) {
            address = addressServiceCore.getAddressByAddressIdAndSource(addressId, source);
        }
        if (ValidatorUtils.isNull(address)) {
            address = this.addressService.findAddressById(addressId);
        }
        return updateModelMapAttrBasedOnAddressVoObject(countryId,address,source,map,countryISOCode,addressTypeCode,addressTypePresent);
    }

    private String updateModelMapAttrBasedOnAddressVoObject(Long countryId, Address address, String source, ModelMap map,
                                                      String countryISOCode,String addressTypeCode,boolean addressTypePresent) {
        if (ValidatorUtils.notNull(address)) {
            handleVillageAndTehsil(address);
            if (address.getCountry() != null) {
            	addressInitializer.initialize(address, AddressInitializer.AddressLazyAttributes.ALL);
                countryISOCode = updateAddressAttrOfModelMap(countryId, map, address,addressTypeCode,addressTypePresent);
            }
            List<State> stateList = new ArrayList<>();
            stateList.add(address.getState());
            map.put(STATE_LIST, stateList);
            List<District> districtList=new ArrayList<>();
            if(address.getState()!=null)
            {
                districtList = addressService.findAllDistrictInState(address.getState().getId());
            }
            map.put(DISTRICT_LIST, districtList);
            List<City> cityList = new ArrayList<>();
            cityList.add(address.getCity());
            map.put(CITY_LIST, cityList);

            // Village Handling in edit and view
            map.put("villageName", address.getVillage());
            if (address.getDistrict() != null) {
                map.put(DISTRICT_NAME, address.getDistrict().getDistrictName());
                map.put(MASKED_DISTRICT_NAME, address.getDistrict().getTransientMaskingMap().get(DISTRICT_NAME));
            } else {
                map.put(DISTRICT_NAME, "");
            }

            List<Area> areaList = new ArrayList<>();
            areaList.add(address.getArea());
            map.put(AREA_LIST, areaList);
            if(address.getCity()!=null) {
                List<Street> streetList = addressService.findAllStreetInCity(address.getCity().getId());
                map.put(STREET_LIST, streetList);
                if (address.getStreetMaster() != null)
                    map.put("selectedStreetId", address.getStreetMaster().getId());
            }
            // Find Tehsil list for district
            updateTehsilIdListAttrForDistrict(map, address);
            // Find Village for District and Tehsil
            updateMapVillageIDListAttribute(map, address);

        }
        return countryISOCode;
    }

    private String updateAddressAttrOfModelMap(Long countryId, ModelMap map, Address address,String addressTypeCode,boolean addressTypePresent) {
        String countryISOCode="";
        if (countryId != null && !StringUtils.isEmpty(addressTypeCode)) {
               if (!(countryId.equals(address.getCountry().getId()))) {

                    Address newAddress = new Address();
                    map.put(ADDRESS, newAddress);

            } else {
                countryISOCode = address.getCountry().getCountryISOCode();
                map.put(ADDRESS, address);
            }
        } {
            countryISOCode = address.getCountry().getCountryISOCode();
            map.put(ADDRESS, address);
           }

        return countryISOCode;
    }





    private void updateModelMapAttrBasedOnZipCodeSelected(ModelMap map, ZipCode zipcodeSelected) {
        if (zipcodeSelected != null) {
            Hibernate.initialize(zipcodeSelected.getState());
            Hibernate.initialize(zipcodeSelected.getCity());
            Hibernate.initialize(zipcodeSelected.getVillage());
            if (zipcodeSelected.getState() != null) {
                updateModelMapAttrBasedOnZipCodeSelectedAndState(map, zipcodeSelected);

            }
            if (zipcodeSelected.getCity() != null) {
                updateModelMapAttrBasedOnZipCodeSelectedAndCity(map, zipcodeSelected);
            }

            if (zipcodeSelected.getCity() != null && zipcodeSelected.getCity().getDistrict() != null) {
                updateModelMapAttrBasedOnZipCodeAndCityForDistrict(map, zipcodeSelected);
            }

            if (zipcodeSelected.getVillage() != null && !zipcodeSelected.getVillage().isEmpty()) {
                updateModelMapAttrBasedOnZipCodeSelectedAndVillage(map, zipcodeSelected);
            }
        }
    }

    private void updateModelMapAttrBasedOnZipCodeSelectedAndVillage(ModelMap map, ZipCode zipcodeSelected) {
        if(zipcodeSelected.getVillage().get(0).getName() != null ) {
            map.put("villageName", zipcodeSelected.getVillage().get(0).getName());
            if (zipcodeSelected.getVillage().get(0).getDistrict() != null) {
                map.put(DISTRICT_NAME, zipcodeSelected.getVillage().get(0).getDistrict().getDistrictName());
                map.put(MASKED_DISTRICT_NAME, zipcodeSelected.getVillage().get(0).getDistrict().getTransientMaskingMap().get(DISTRICT_NAME));
            } else {
                map.put(DISTRICT_NAME, "");
            }
            if (zipcodeSelected.getVillage().get(0).getTehsil() != null) {
                map.put(TALUKA, zipcodeSelected.getVillage().get(0).getTehsil().getName());
            } else {
                map.put(TALUKA, "");
            }
        }
    }

    private void updateModelMapAttrBasedOnZipCodeSelectedAndCity(ModelMap map, ZipCode zipcodeSelected) {
        Long cityId = zipcodeSelected.getCity().getId();
        map.put("selectedCityId", zipcodeSelected.getCity().getId());
        map.put("selectedCityIdMasked", zipcodeSelected.getCity().getTransientMaskingMap().get("cityName"));
        List<Street> streetList = addressService.findAllStreetInCity(cityId);
        map.put(STREET_LIST,streetList);
        List<City> cityList = new ArrayList<>();
        cityList.add(zipcodeSelected.getCity());
        map.put(CITY_LIST, cityList);
    }

    private void updateModelMapAttrBasedOnZipCodeAndCityForDistrict(ModelMap map, ZipCode zipcodeSelected) {
        map.put("selectedDistrictId",zipcodeSelected.getCity().getDistrict().getId());
        map.put("selectedDistrictIdMasked",zipcodeSelected.getCity().getDistrict().getTransientMaskingMap().get("districtName"));
        List<District> districtList = new ArrayList<>();
        ((ArrayList<District>) districtList).add(zipcodeSelected.getCity().getDistrict());
        map.put(DISTRICT_LIST,districtList );
    }

    private void updateModelMapAttrBasedOnZipCodeSelectedAndState(ModelMap map, ZipCode zipcodeSelected) {
        map.put("selectedStateId", zipcodeSelected.getState().getId());
        map.put("selectedStateIdMasked", zipcodeSelected.getState().getTransientMaskingMap().get("stateName"));
        List<State> stateList = new ArrayList<>();
        stateList.add(zipcodeSelected.getState());
        map.put(STATE_LIST, stateList);

        Hibernate.initialize(zipcodeSelected.getState().getRegion());
        if (zipcodeSelected.getState().getRegion() != null) {
            map.put("selectedRegionId", zipcodeSelected.getState().getRegion().getId());
            map.put("selectedRegionIdMasked", zipcodeSelected.getState().getRegion().getTransientMaskingMap().get("intraRegionName"));
            List<IntraCountryRegion> regionList = new ArrayList<>();
            regionList.add(zipcodeSelected.getState().getRegion());
            map.put(COUNTRY_REGION_LIST, regionList);
        }

        List<District> districtList = addressService.findAllDistrictInState(zipcodeSelected.getState()
                .getId());
        map.put(DISTRICT_LIST, districtList);

        List<Area> areaList = addressService.findAllAreaInZipCode(zipcodeSelected.getId());
        map.put(AREA_LIST, areaList);
    }

    private void updateTehsilIdListAttrForDistrict(ModelMap map, Address address) {
        if(address.getDistrict() != null && address.getDistrict().getId() != null){
            map.put(TEHSIL_ID_LIST,getSerializedString(addressService.getTehsilIdsByDistrictId(address.getDistrict().getId())));
            //Find Village for Districa and tehsil
        }
    }

    private void prepareNewAddress(String addressLine1, String addressLine2, String addressLine3, String addressLine4,String remarks,
                                   String additionalField1,String additionalField2,String additionalField3,
                                   String additionalField4,String additionalField5,AddressGeneric genericYesNo,
                                   String poBox, String street, ModelMap map, ZipCode zipcodeSelected) {
        Address newAddress = new Address();
        newAddress.setAddressLine1(addressLine1);
        newAddress.setAddressLine2(addressLine2);
        newAddress.setAddressLine3(addressLine3);
        newAddress.setAddressLine4(addressLine4);
        newAddress.setAdditionalInfo(remarks);
        newAddress.setAdditionalField1(additionalField1);
        newAddress.setAdditionalField2(additionalField2);
        newAddress.setAdditionalField3(additionalField3);
        newAddress.setAdditionalField4(additionalField4);
        newAddress.setAdditionalField5(additionalField5);
        newAddress.setGenericYesNo(genericYesNo);
        newAddress.setZipcode(zipcodeSelected);
    if(zipcodeSelected != null && zipcodeSelected.getVillage() != null && !zipcodeSelected.getVillage().isEmpty()) {
            if(zipcodeSelected.getVillage().size() == 1) {
                if (StringUtils.isNotEmpty(zipcodeSelected.getVillage().get(0).getName())) {
                    newAddress.setVillageMaster(zipcodeSelected.getVillage().get(0));
                    newAddress.setVillage(zipcodeSelected.getVillage().get(0).getName());
                    if (zipcodeSelected.getVillage().get(0).getTehsil() != null && StringUtils.isNotEmpty(zipcodeSelected.getVillage().get(0).getTehsil().getName())) {
                        newAddress.setTaluka(zipcodeSelected.getVillage().get(0).getTehsil().getName());
                        newAddress.setTehsil(zipcodeSelected.getVillage().get(0).getTehsil());
                    }
            }
        }
    }

        newAddress.setPoBox(poBox);
        newAddress.setStreet(street);
        map.put(ADDRESS, newAddress);
    }

    private void updateMapVillageIDListAttribute(ModelMap map, Address address) {
        if (address.getDistrict() != null && address.getDistrict().getId() != null) {
            if (address.getTehsil() != null && address.getTehsil().getId() != null) {
                map.put(VILLAGE_ID_LIST, getSerializedString(addressService.getVillageIdsByDistrictIdAndTehsilId(
                        address.getDistrict().getId(), address.getTehsil().getId())));
            } else {
                map.put(VILLAGE_ID_LIST,
                        getSerializedString(addressService.getVillageIdsByDistrictId(address.getDistrict().getId())));
            }
        }
    }

    private void setFieldConfigData(NodeList result, Locale loc, List<FieldConfig> fields,
                                    ModelMap map, String productTypeName, Long countryId) {
        for (int i = 0 ; i < result.getLength() ; i++) {
            FieldConfig field = new FieldConfig();
            Node childNode = result.item(i);
            String labelKey = childNode.getAttributes().getNamedItem("label").getNodeValue();
            String label = messageSource.getMessage(labelKey, null, loc);
            field.setFieldName(childNode.getAttributes().getNamedItem("name").getNodeValue());
            field.setMandatory(childNode.getAttributes().getNamedItem("mandatory").getNodeValue());
            if(childNode.getAttributes().getNamedItem("binderName")!=null) {
                field.setBinderName(childNode.getAttributes().getNamedItem("binderName").getNodeValue());
            }
            if(childNode.getAttributes().getNamedItem("itemValue")!=null) {
                field.setItemValue(childNode.getAttributes().getNamedItem("itemValue").getNodeValue());
            }
            if(childNode.getAttributes().getNamedItem("itemLabel")!=null) {
                field.setItemLabel(childNode.getAttributes().getNamedItem("itemLabel").getNodeValue());
            }
            field.setFieldLabel(label);
            if (childNode.getAttributes().getNamedItem(MAX_LENGTH) != null) {
                field.setMaxLength(childNode.getAttributes().getNamedItem(MAX_LENGTH).getNodeValue());
            }
            fields.add(field);
            if (field.getFieldName().equals("region") && field.getMandatory().equals(FALSE)) {
                map.put("loadStateFromCountry", true);
                map.put("countryId", countryId);
            }

            if (productTypeName != null && productTypeName.equals(ProductType.MICRO_HOUSING_LOAN) && field.getMandatory().equals(FALSE)) {
                if (field.getFieldName().equals("district")) {
                    field.setMandatory("true");
                }
                if (field.getFieldName().equals(TALUKA)) {
                    field.setMandatory("true");
                }

            }
        }
    }
    private void handleVillageAndTehsil(Address address) {
        if(address.getVillageMaster() != null)
            address.setVillage(address.getVillageMaster().getName());
        if(address.getTehsil() != null)
            address.setTaluka(address.getTehsil().getName());
    }

    @RequestMapping(value = "/viewInMapMode", method = RequestMethod.POST)
    public String viewInMapMode(HttpServletRequest request, ModelMap map) {
        map.put(POSTAL_CODE, new PostalCode());
        return "mapView";
    }

    @RequestMapping(value = "/response/{location}", method = RequestMethod.POST)
    public @ResponseBody
    String getResponseForLocation(@PathVariable("location") String location, @RequestParam(" CouchMapId") String CouchMapId) {

        List<String> latlongList = addressService.findLatLongByZipCode(location);
        String url = "http://www.openrouteservice.org/php/OpenLSLUS_Geocode.php";
        String locationLatLong = "";
        if (ValidatorUtils.hasElements(latlongList)) {
            locationLatLong = latlongList.get(0);
        }

        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
        httpClientBuilder.setConnectionManager(new PoolingHttpClientConnectionManager());

        if (isProxyEnabled.equalsIgnoreCase("true")) {
            if (username.equalsIgnoreCase(NULL)) {
                HttpHost httpHost = new HttpHost(ipaddress, Integer.valueOf(port));
                HttpRoutePlanner routePlanner = new DefaultProxyRoutePlanner(httpHost);
                httpClientBuilder.setRoutePlanner(routePlanner);
            } else {
                AuthScope authScope = new AuthScope(ipaddress, Integer.valueOf(port));
                Credentials credentials = new UsernamePasswordCredentials(username, password);
                CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
                credentialsProvider.setCredentials(authScope, credentials);
                httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
            }
        }

        // Create a method instance.
        URI uri = null;
        try {
            uri = new URIBuilder(url).setParameter("FreeFormAdress", locationLatLong).setParameter("MaxResponse", "1")
                    .build();
        } catch (URISyntaxException e1) {
            BaseLoggers.exceptionLogger.error("Exception creating uri : ", e1);
        }
        HttpPost httpPost = new HttpPost(uri);
        httpPost.addHeader(new BasicHeader("X-Requested-With", "XMLHttpRequest"));
        httpPost.addHeader(new BasicHeader("Content-Type", "application/x-www-form-urlencoded"));
        String latLong = "";

        CloseableHttpClient client = null;
        try {
            // Execute the method.
            client = httpClientBuilder.build();
            CloseableHttpResponse httpResponse = client.execute(httpPost);

            if (httpResponse.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                return "";
            }

            // Read the response body.
            HttpEntity httpEntity = null;
            try {
                httpEntity = httpResponse.getEntity();
            } finally {
                httpResponse.close();
            }
            InputStream content = httpEntity.getContent();
            String responseBody = IOUtils.toString(content);

            latLong = getUpdatedLatLong(CouchMapId, responseBody);

        } catch (Exception e) {
            throw new SystemException("Error while fetching data from Open Layers server to populate map", e);
        } finally {
            // Release the connection.
            httpPost.completed();
            try {
                if(null!= client){
                    client.close();
                }
            } catch (IOException e) {
                BaseLoggers.exceptionLogger.warn("Exception while closing http client : " + e.getMessage());
            }
        }

        return latLong;

    }

    private String getUpdatedLatLong(String couchMapId, String responseBody) {
        StringTokenizer token = new StringTokenizer(responseBody, System.getProperty("line.separator"));
        String latLong="";
        while (token.hasMoreTokens()) {
            latLong = token.nextToken();
            if (latLong.contains("<gml:pos")) {
                latLong = latLong.replace("<gml:pos srsName=\"EPSG:4326\">", "");
                latLong = latLong.replace("</gml:pos>", "");
                latLong	= latLong.trim();
                if (couchMapId == null || couchMapId.equals("")) {
                    token = new StringTokenizer(latLong, " ");
                    AddressTracker addressTracker = new AddressTracker(Double.valueOf(token.nextToken()),
                            Double.valueOf(token.nextToken()));
                    docService2.saveObject(addressTracker);
                    String addressTrackerId = addressTracker.getId();
                    latLong = latLong.concat(" "+addressTrackerId);
                } else {
                    token = new StringTokenizer(latLong, " ");
                    docService2.updateAddressLocation(couchMapId, new double[] { Double.valueOf(token.nextToken()),
                            Double.valueOf(token.nextToken()) }, AddressTracker.class);
                    latLong = latLong.concat(" " + couchMapId);
                }
                break;
            } else {
                latLong = "";
            }

        }
        return latLong;
    }

    @RequestMapping(value = "/pincodeBased/getZipCodeId", method = RequestMethod.POST)
    public @ResponseBody
    String getZipCodeId(@RequestParam(value = "selectedZipCode", required = false) String selectedZipCodeID) {
    	Long zipCodeId = null;
        SourceProduct sourceProduct=null;

    	if(nullZipCodeJson==null) {
    		sourceProduct=genericParameterService.findByCode(ProductInformationLoader.getProductCode(), SourceProduct.class);
           	JSONSerializer iSerializer = new JSONSerializer();
            Map<String,Object> map = new HashMap<>();
            map.put("zipCodeId", null);
            map.put("source", sourceProduct);
            nullZipCodeJson = iSerializer.deepSerialize(map);
    	}
    	
        if(selectedZipCodeID!=null){
        	zipCodeId = addressService.findZipCodeIdByZipCode(selectedZipCodeID);
        	JSONSerializer iSerializer = new JSONSerializer();
            Map<String,Object> map = new HashMap<>();
            map.put("zipCodeId", zipCodeId);
            map.put("source", sourceProduct);
            return iSerializer.deepSerialize(map);
        } else {
        	return nullZipCodeJson;
        }
        
    }

    /**
     * @param
     * @return String
     * @throws IOException
     * @description to populate state on address form
     */
    @RequestMapping(value = "/showMap", method = RequestMethod.POST)
    public String showMap(@RequestParam String lattitude, @RequestParam String longitude, ModelMap map) {
        map.put("lattitude", lattitude);
        map.put("longitude", longitude);
        return "addressTagMapViewer";
    }

    /**
     * @param
     * @return String
     * @throws IOException
     * @description to populate state on address form
     */
    @RequestMapping(value = "/populateArea")
    public @ResponseBody
    String populateArea(@RequestParam String postalCode, @RequestParam String countryCode, HttpServletRequest request,
                        ModelMap map) {
        JSONSerializer iSerializer = new JSONSerializer();
        map.put(POSTAL_CODE, new PostalCode());
        List<PostalCode> areaList = addressService.findAreaByPostalCode(countryCode, postalCode);
        return iSerializer.exclude(CLASS).exclude("data.entityLifeCycleData", "entityLifeCycleData")
                .deepSerialize(areaList);
    }

    @RequestMapping(value = "/populateOther")
    public @ResponseBody
    String populateCity(@RequestParam String areaName, @RequestParam String postalCode, @RequestParam String countryCode,
                        HttpServletRequest request, ModelMap map){
        JSONSerializer iSerializer = new JSONSerializer();
        map.put(POSTAL_CODE, new PostalCode());
        List<PostalCode> completeList = addressService.findOthers(countryCode, areaName, postalCode);
        return iSerializer.exclude(CLASS).exclude("data.entityLifeCycleData", "entityLifeCycleData")
                .deepSerialize(completeList);
    }

    @RequestMapping(value = "/populateState")
    public @ResponseBody
    String populateState(@RequestParam("selectedRegion") Long regionId, ModelMap map) {
        JSONSerializer iSerializer = new JSONSerializer();
        List<State> stateList = addressService.findAllStateInIntraCountryRegion(regionId);
        return iSerializer.exclude(CLASS).include("id", "stateName").exclude("*").deepSerialize(stateList);
    }

    @RequestMapping(value = "/populateCity")
    public @ResponseBody
    String populateCity(@RequestParam("selectedState") Long stateId, ModelMap map) {
        JSONSerializer iSerializer = new JSONSerializer();
        List<City> cityList = addressService.findAllCityInState(stateId);
        return iSerializer.exclude(CLASS).include("id", "cityName").exclude("*").deepSerialize(cityList);
    }

    @RequestMapping(value = "/populateDistrict")
    public @ResponseBody
    String populateDistrict(@RequestParam("selectedState") Long stateId, ModelMap map) {
        JSONSerializer iSerializer = new JSONSerializer();
        List<District> districtList = addressService.findAllDistrictInState(stateId);
        return iSerializer.exclude(CLASS).include("id", DISTRICT_NAME).exclude("*").deepSerialize(districtList);
    }

    @RequestMapping(value = "/populateZipCodeFromCity")
    public @ResponseBody
    String populateZipCodeFromCity(@RequestParam("selectedCity") Long cityId, ModelMap map) {
        JSONSerializer iSerializer = new JSONSerializer();
        List<ZipCode> zipCodeList = addressService.findAllZipCodeInCity(cityId);
        return iSerializer.exclude(CLASS).include("id", ZIP_CODE).exclude("*").deepSerialize(zipCodeList);
    }

    @RequestMapping(value = "/populateZipCodeFromState")
    public @ResponseBody
    String populateZipCodeFromState(@RequestParam("selectedState") Long stateId, ModelMap map) {
        JSONSerializer iSerializer = new JSONSerializer();
        List<ZipCode> zipCodeList = addressService.findAllZipCodeInState(stateId);
        return iSerializer.exclude(CLASS).include("id", ZIP_CODE).exclude("*").deepSerialize(zipCodeList);
    }

    @RequestMapping(value = "/populateZipCodeFromCountry")
    public @ResponseBody
    String populateZipCodeFromCountry(@RequestParam("selectedCountry") Long countryId, ModelMap map) {
        JSONSerializer iSerializer = new JSONSerializer();
        List<ZipCode> zipCodeList = addressService.findAllZipCodeInCountry(countryId);
        return iSerializer.exclude(CLASS).include("id", ZIP_CODE).exclude("*").deepSerialize(zipCodeList);
    }

    @RequestMapping(value = "/populateAreaFromZipCode")
    public @ResponseBody
    String populateAreaFromZipCode(@RequestParam("selectedZipCode") Long zipCodeId, ModelMap map) {
        JSONSerializer iSerializer = new JSONSerializer();
        List<Area> areaList = addressService.findAllAreaInZipCode(zipCodeId);
        return iSerializer.exclude(CLASS).include("id", "areaName").exclude("*").deepSerialize(areaList);
    }

    @RequestMapping(value = "/populateStateFromCountry")
    public @ResponseBody
    String populateStateFromCountry(@RequestParam("selectedCountry") Long countryId, ModelMap map) {
        JSONSerializer iSerializer = new JSONSerializer();
        List<State> stateList = addressService.findAllStateInCountry(countryId);
        return iSerializer.exclude(CLASS).include("id", "stateName").exclude("*").deepSerialize(stateList);
    }
    
    @RequestMapping(value = "/populateIntraCountryRegionFromCountry")
    public @ResponseBody
    String populateIntraCountryRegionFromCountry(@RequestParam("selectedCountry") Long countryId, ModelMap map) {
        JSONSerializer iSerializer = new JSONSerializer();
        List<IntraCountryRegion> regionList = addressService.findIntraCountryRegionByCountryId(countryId);
        return iSerializer.exclude(CLASS).include("id", "intraRegionName").exclude("*").deepSerialize(regionList);
    }

    @RequestMapping(value = "/populateCityFromCountry")
    public @ResponseBody
    String populateCityFromCountry(@RequestParam("selectedCountry") Long countryId, ModelMap map) {
        JSONSerializer iSerializer = new JSONSerializer();
        List<City> cityList = addressService.findAllCityInCountry(countryId);
        return iSerializer.exclude(CLASS).include("id", "cityName").exclude("*").deepSerialize(cityList);
    }

    @RequestMapping(value = "/populateAreaFromCity")
    public @ResponseBody
    String populateAreaFromCity(@RequestParam("selectedCity") Long cityId, ModelMap map) {
        JSONSerializer iSerializer = new JSONSerializer();
        List<Area> areaList = addressService.findAllAreaInCity(cityId);
        return iSerializer.exclude(CLASS).include("id", "areaName").exclude("*").deepSerialize(areaList);
    }

    @RequestMapping(value = "/populateStreetFromCity")
    public @ResponseBody
    String populateStreetFromCity(@RequestParam("selectedCity") Long cityId, ModelMap map) {
        JSONSerializer iSerializer = new JSONSerializer();
        List<Street> streetList = addressService.findAllStreetInCity(cityId);
        return iSerializer.exclude(CLASS).include("id", "streetName").exclude("*").deepSerialize(streetList);
    }


    @RequestMapping(value = "/validateAddressPincodeOnSave")
    public @ResponseBody
    boolean validateAddressPincodeOnSave(@RequestParam(value = "requestedZipCode") String requestedZipCode, ModelMap map) {
        if (StringUtils.isNotEmpty(requestedZipCode)) {
            List<ZipCode> zipCodeList = addressService.findZipcodeId(requestedZipCode);
            return !CollectionUtils.isEmpty(zipCodeList);

        }
        return true;
    }

    private ProductType getProductTypeForApplicationId(Long appId) {
        ProductType productType = null;
        if (appId != null) {
            NamedQueryExecutor<ProductType> executor = new NamedQueryExecutor<ProductType>("LoanApplication.getProductType")
                    .addParameter("appId", appId);
            productType = entityDao.executeQueryForSingleValue(executor);
        }
        if (productType == null) {
            NamedQueryExecutor<ProductType> executor = new NamedQueryExecutor<ProductType>(
                    "LoanApplication.getProductTypeForLead").addParameter("appId", appId).addQueryHint(
                    QueryHint.QUERY_HINT_READONLY, Boolean.TRUE);
            productType = entityDao.executeQueryForSingleValue(executor);
        }
        return productType;
    }

    @RequestMapping(value = "/populateRegionFromState")
    public @ResponseBody
    Map<String, Object> populateRegionFromState(@RequestParam("selectedState") Long stateId) {
        Map<String, Object> map = new HashMap<>();
        NamedQueryExecutor<IntraCountryRegion> executor = new NamedQueryExecutor<IntraCountryRegion>(
                "address.getRegionFromState").addParameter("stateId", stateId);
        List<IntraCountryRegion> regionList = entityDao.executeQuery(executor);
        if (ValidatorUtils.hasElements(regionList)) {
            map.put("regionName", regionList.get(0).getIntraRegionName());
            map.put("id", regionList.get(0).getId());
        }
        return map;
    }


    @RequestMapping(value = "/populateZipCodesForAutoComplete")
    @ResponseBody
    public AutocompleteVO populateZipCodesForAutoComplete(ModelMap map, @RequestParam String value, @RequestParam String itemVal,
                                                          @RequestParam String searchCol, @RequestParam String className, @RequestParam Boolean loadApprovedEntityFlag,
                                                          @RequestParam String i_label, @RequestParam String idCurr, @RequestParam String content_id,
                                                          @RequestParam int page, @RequestParam(required = false) String itemsList,
                                                  @RequestParam(required = false) Boolean strictSearchOnitemsList, HttpServletRequest req,@RequestParam String countryId,
                                                  @RequestParam String stateId,@RequestParam String cityId){

        String[] searchColumnList = searchCol.split(" ");
        AutocompleteVO autocompleteVO = new AutocompleteVO();
        if (strictSearchOnitemsList == null) {
            strictSearchOnitemsList = false;
        }
        if (loadApprovedEntityFlag == null) {
            loadApprovedEntityFlag = false;
        }

        List<Map<String, ?>> list = addressService.searchZipCodesForAutoComplete(className, itemVal, searchColumnList, value, loadApprovedEntityFlag,
                itemsList, strictSearchOnitemsList, page, countryId, stateId, cityId);
        int sizeList;
        if (ValidatorUtils.hasElements(list)) {
            Map<String, ?> listMap = list.get(list.size() - 1);
            sizeList = ((Long) listMap.get("size")).intValue();
            list.remove(list.size() - 1);
            //map.put("size", sizeList);
            //map.put("page", page);
            autocompleteVO.setS(sizeList);
            autocompleteVO.setP(page);
        }

        if (i_label != null && i_label.contains(".")) {
            i_label = i_label.replace(".", "");
        }

        //map.put("data", list);
        autocompleteVO.setD(list);
        if (idCurr != null && idCurr.trim().length() > 0) {
            idCurr = idCurr.replaceAll("[^\\w\\s\\-_]", "");
        }
        String[] sclHeading = new String[searchColumnList.length];
        for (int i = 0; i < searchColumnList.length; i++) {
            searchColumnList[i] = searchColumnList[i].replace(".", "");
            sclHeading[i] = messageSource.getMessage("label.autoComplete." + searchColumnList[i], null, Locale.getDefault());
        }
        //map.put("idCurr", idCurr);
        //map.put("i_label", i_label);
        //map.put("content_id", content_id);

        autocompleteVO.setIc(idCurr);
        autocompleteVO.setIl(i_label);
        autocompleteVO.setCi(content_id);
        autocompleteVO.setIv(itemVal);
        autocompleteVO.setScl(searchColumnList);
        autocompleteVO.setColh(sclHeading);
        return autocompleteVO;
        //return "autocomplete";

    }





    @RequestMapping(value = "/fetchTehsilForDistrict", method = RequestMethod.GET)
    public @ResponseBody String fetchTehsilForDistrict(@RequestParam(value = "districtId", required = true) Long districtId) {
        List<Long> tehsilIds = addressService.getTehsilIdsByDistrictId(districtId);
        return getSerializedString(tehsilIds);
    }

    @RequestMapping(value = "/fetchTehsilForVillageId", method = RequestMethod.GET)
    public @ResponseBody String fetchTehsilForVillageId(@RequestParam(value = "villageId", required = true) Long villageId) {
        VillageMaster village = entityDao.find(VillageMaster.class,villageId);
        Map<String,String> returnData = new HashMap<>();
        if(village != null && village.getTehsil() != null && village.getTehsil().getId() != null){
            returnData.put("id",village.getTehsil().getId().toString());
            returnData.put("name",village.getTehsil().getName());
        }else{
            returnData.put("id","");
            returnData.put("name","");
        }

        JSONSerializer iSerializer = new JSONSerializer();
        return iSerializer.deepSerialize(returnData);
    }


    @RequestMapping(value = "/fetchVillageForDistrictAndTehsil", method = RequestMethod.GET)
    public @ResponseBody String fetchVillageForDistrict(@RequestParam(value = "districtId", required = true) Long districtId,@RequestParam(value = "tehsilId", required = false) Long tehsilId) {
        List<Long> villageIds;
        if(tehsilId != null) {
            villageIds = addressService.getVillageIdsByDistrictIdAndTehsilId(districtId,tehsilId);
        }else{
            villageIds = addressService.getVillageIdsByDistrictId(districtId);
        }
        return getSerializedString(villageIds);
    }

    private String getSerializedString(List<Long> tehsilIds) {
        if(CollectionUtils.isNotEmpty(tehsilIds)){
            Map<String, Long> map2 = new HashMap<>();
            for(Long id:tehsilIds) {
                map2.put("id".concat(String.valueOf(id)),id);
            }
            JSONSerializer iSerializer = new JSONSerializer();
            return iSerializer.deepSerialize(map2);
        }else {
            return "{\"id0\":0}";
        }
    }


    @RequestMapping(value = "/getPincode")
    @ResponseBody
    public String getPincode(ModelMap map, @RequestParam(value = "village", required = false) String villageName) {
        Map<String,Object> villageMap = new HashMap<>();
        villageMap.put("name", villageName);
        VillageMaster village = baseMasterService.findMasterByCode(VillageMaster.class, villageMap);
        if(village !=null) {
            Long villageId = village.getId();
            List<ZipCode> zipCode = entityDao.executeQuery(new NamedQueryExecutor<ZipCode>("address.findZipCodeByVillageId").addParameter(
                    "villageId", villageId).addParameter("approvalStatus", 0));
            if(zipCode!= null && zipCode.get(0)!= null && zipCode.get(0).getZipCode()!=null){
                Map<Long,String> zipMap=new HashMap<>();
                zipMap.put(zipCode.get(0).getId(),zipCode.get(0).getZipCode());

                return zipMap.toString();

            }
        }
        return null;

    }

    @RequestMapping(value = "/populateSelectedCountryInCaseofNull", method = RequestMethod.POST)
    @ResponseBody
    public String populateSelectedCountryInCaseofNull (@RequestParam(value = "selectedCountryId", required = false) String countryId){
        String countryISOCode="";
        if (countryId == null) {
        	return countryISOCode;
        }

        String addressCountry = addressService.getCountryNameByCountryId(countryId).toLowerCase();
        if (addressCountry==null) {
        	return countryISOCode;       
        }
        
        if(localeIsoCodeByCountryNameMap.get(addressCountry)!=null) {
        	return localeIsoCodeByCountryNameMap.get(addressCountry);	
    	}
        
        return countryISOCode;
    }
    
	private void evaluateXpathAndPrepareFields(String xpath, Locale loc, ModelMap map, Long appId,
			Long countryId) throws XPathExpressionException {

		List<FieldConfig> fields = evaluatedXpathFieldConfigsMap.get(xpath);
		if (fields != null) {
			map.put("message", "success");
			map.put("fields", fields);
			return;
		}

		String message = "";

		NodeList result = (NodeList) factory.newXPath().evaluate(xpath, document, XPathConstants.NODESET);
		fields = new ArrayList<>();
		if (result != null) {
			if (result.getLength() == 0) {
				String xpathForDefault = XPATH_TARGET_CNTRY + DEFAULT + XPATH_FIELD_CONFIG;
				NodeList resultForDefault = (NodeList) factory.newXPath().evaluate(xpathForDefault, document,
						XPathConstants.NODESET);
				result = resultForDefault;
			}

			String productTypeName = null;
			if (appId != null) {
				ProductType productType = getProductTypeForApplicationId(appId);
				productTypeName = productType != null ? productType.getShortName() : "";
			}

			setFieldConfigData(result, loc, fields, map, productTypeName, countryId);
			evaluatedXpathFieldConfigsMap.put(xpath, fields);
			message = "success";
		} else {
			message = "failure";
		}

		map.put("message", message);
		map.put("fields", fields);
	}



}
