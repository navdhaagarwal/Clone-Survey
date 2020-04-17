package com.nucleus.web.address;

import com.nucleus.core.initialization.*;
import com.nucleus.fieldConfig.*;
import com.nucleus.logging.*;
import com.nucleus.web.*;
import org.apache.commons.lang3.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.context.*;
import org.w3c.dom.*;
import org.xml.sax.*;

import javax.annotation.*;
import javax.inject.*;
import javax.xml.parsers.*;
import javax.xml.xpath.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;


@Named("addressAdditionalFieldValidator")
public class AddressAdditionalFieldValidatorImpl implements AddressAdditionalFieldValidator {

    private static final String XPATH_TARGET_CNTRY = "/address-configs/address-config/target-country[@name=" + "'";
    private static final String XPATH_FIELD_CONFIG = "']/field-config";
    private static final String XPATH_TARGET_CNTRY_COMMA = "/address-configs/address-config/target-country[contains(@name," + "'";
    private static final String XPATH_TARGET_ADDRESS_TYPE = "')]/address-type[contains(@name," + "'";
    private static final String XPATH_TARGET_FILTER_CODE = "')]/filter-code[contains(@name," + "'";
    private static final String XPATH_FIELD_CONFIG_COMMA = "')]/field-config";
    private static final String         DEFAULT                                   = "default";

    private Map<String,List<FieldConfig>> evaluatedXpathFieldConfigsMap;

    private static XPathFactory      factory = XPathFactory.newInstance();
    private Document                 document;

    @Autowired
    protected MessageSource messageSource;

    @Inject
    @Named("frameworkConfigResourceLoader")
    protected NeutrinoResourceLoader frameworkConfigResourceLoader;

    @PostConstruct
    public void initializeDocument() throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        InputStream is = frameworkConfigResourceLoader.getResource("country-specific-fields-mapping.xml").getInputStream();
        document = builder.parse(is);
        evaluatedXpathFieldConfigsMap = new ConcurrentHashMap<>();

    }

    private void paintFieldsData(String countryISOCode,String addressTypeCode, String filterCode, Map map) throws XPathExpressionException {

        String xpath = "";
        if (!StringUtils.isEmpty(filterCode) && !StringUtils.isEmpty(addressTypeCode) && !StringUtils.isEmpty(countryISOCode)) {
            xpath = XPATH_TARGET_CNTRY_COMMA + countryISOCode + XPATH_TARGET_ADDRESS_TYPE + addressTypeCode + XPATH_TARGET_FILTER_CODE + filterCode + XPATH_FIELD_CONFIG_COMMA;
        } else if (!StringUtils.isEmpty(addressTypeCode) && !StringUtils.isEmpty(countryISOCode)) {
            xpath = XPATH_TARGET_CNTRY_COMMA + countryISOCode + XPATH_TARGET_ADDRESS_TYPE + addressTypeCode + XPATH_FIELD_CONFIG_COMMA;
        } else if (!StringUtils.isEmpty(filterCode) && !StringUtils.isEmpty(countryISOCode)) {
            xpath = XPATH_TARGET_CNTRY_COMMA + countryISOCode + XPATH_TARGET_FILTER_CODE + filterCode + XPATH_FIELD_CONFIG_COMMA;
        } else if (!StringUtils.isEmpty(countryISOCode)) {
            xpath = XPATH_TARGET_CNTRY_COMMA + countryISOCode + XPATH_FIELD_CONFIG_COMMA;
        } else {
            xpath = XPATH_TARGET_CNTRY + countryISOCode + XPATH_FIELD_CONFIG;
        }

        getFieldsDataFromXML(xpath,map);
    }

    private void getFieldsDataFromXML(String xpath, Map map) throws XPathExpressionException {

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

            setFieldConfigData(result, fields, map);
            evaluatedXpathFieldConfigsMap.put(xpath, fields);
            message = "success";
        } else {
            message = "failure";
        }

        map.put("message", message);
        map.put("fields", fields);
    }


    private void setFieldConfigData(NodeList result, List<FieldConfig> fields,
                                    Map map) {
        for (int i = 0 ; i < result.getLength() ; i++) {
            FieldConfig field = new FieldConfig();
            Node childNode = result.item(i);
            String labelKey = childNode.getAttributes().getNamedItem("label").getNodeValue();
            String label = messageSource.getMessage(labelKey, null,Locale.getDefault());
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
            if (childNode.getAttributes().getNamedItem("maxLength") != null) {
                field.setMaxLength(childNode.getAttributes().getNamedItem("maxLength").getNodeValue());
            }
            fields.add(field);

        }
    }

    public boolean validateAdditionalDropDownField1(String countryISOCode,String addressTypeCode, String filterCode,  String additionalFieldCode) {
        Map map = new HashMap();
        try {
            paintFieldsData(countryISOCode, addressTypeCode ,filterCode,map );
        }catch (Exception e){

            BaseLoggers.flowLogger.error("Error in finding drop down field for given country", e);
            return false;
        }
        List<FieldConfig> fields= (List)map.get("fields");
        boolean isValid = false;
        for(FieldConfig fieldConfig : fields){

            if(fieldConfig.getFieldName().equals("additionalDropdownField1")){

                String binderName = fieldConfig.getBinderName();
                List<Map<String,Object>>  binderData = (List<Map<String,Object>> )WebDataBinderElClass.getWebDataBinderData(binderName);

                if(!binderData.isEmpty()){

                    for(Map<String,Object> myMap:binderData){

                        if(myMap.get(fieldConfig.getItemLabel())!=null && myMap.get(fieldConfig.getItemLabel()).equals(additionalFieldCode)){
                            isValid = true;
                            break;
                        }
                    }
                }
            }
        }
        return isValid;

    }

    public boolean validateAdditionalDropDownField2(String countryISOCode,String addressTypeCode, String filterCode,  String additionalFieldCode) {
        Map map = new HashMap();
        try {
            paintFieldsData(countryISOCode,addressTypeCode,filterCode,map);
        }catch (Exception e){

            BaseLoggers.flowLogger.error("Error in finding drop down field for given country", e);
            return false;
        }
        List<FieldConfig > fields= (List)map.get("fields");
        boolean isValid = false;
        for(FieldConfig fieldConfig : fields){

            if(fieldConfig.getFieldName().equals("additionalDropdownField2")){

                String binderName = fieldConfig.getBinderName();
                List<Map<String,Object>>  binderData = (List<Map<String,Object>> )WebDataBinderElClass.getWebDataBinderData(binderName);

                if(!binderData.isEmpty()){

                    for(Map<String,Object> myMap:binderData){

                        if(myMap.get(fieldConfig.getItemLabel())!=null && myMap.get(fieldConfig.getItemLabel()).equals(additionalFieldCode)){
                            isValid = true;
                            break;
                        }
                    }
                }
            }
        }
        return isValid;
    }

}
