package com.nucleus.address;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static com.nucleus.finnone.pro.general.util.ValidatorUtils.notNull;
import static org.springframework.util.StringUtils.isEmpty;
import com.nucleus.address.AddressConstants;
import com.nucleus.core.initialization.NeutrinoResourceLoader;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.nucleus.fieldConfig.FieldConfig;

@Named(value = "addressTagUtil")
  public class AddressTagUtil {
    
    @Inject
    @Named("addressService")
    private AddressTagService addressService;  
    
    @Inject
    @Named("frameworkConfigResourceLoader")
    protected NeutrinoResourceLoader frameworkConfigResourceLoader;
    
    private static XPathFactory      factory = XPathFactory.newInstance();
    private Document                 document;
    private static final String         DEFAULT                                   = "default";
    
    @PostConstruct
    public void initializeDocument() throws Exception {
      DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
      InputStream is = frameworkConfigResourceLoader.getResource("country-specific-fields-mapping.xml").getInputStream();
      document = builder.parse(is);
    }
    
    public Map<String,String> validateAddressBeforeSave(Address address) throws XPathExpressionException{
      Long countryId = address.getCountry().getId();
      String message = "";
      StringBuilder validationFailedFields = new StringBuilder();
      Map<String, Object> addressCountry=new HashMap<String, Object>();
      Map<String,String> map = new HashMap<String,String>();
      Boolean isMandatoryFieldEmpty=Boolean.FALSE;
      addressCountry= addressService.getCountryDetailsByCountryId(countryId);
      
      String countryCode=!addressCountry.isEmpty()?addressCountry.get("countryISOCode").toString():DEFAULT;
      
      String xpath = "/address-configs/address-config/target-country[@name=" + "'" + countryCode + "']/field-config";
      List<FieldConfig> fields = new ArrayList<FieldConfig>();
      NodeList result = (NodeList) factory.newXPath().evaluate(xpath, document, XPathConstants.NODESET);
      
      if (notNull(result)){
        setDefaultCountryConfiguration(result, countryCode);
        setFieldsXMLBasedConfiguration(fields, result);
        for (FieldConfig field : fields) {
          isMandatoryFieldEmpty=validateAddressLine1(field, address, validationFailedFields,isMandatoryFieldEmpty);
          isMandatoryFieldEmpty=validateAddressLine2(field, address, validationFailedFields,isMandatoryFieldEmpty);
          isMandatoryFieldEmpty=validateAddressLine3(field, address, validationFailedFields,isMandatoryFieldEmpty);
          isMandatoryFieldEmpty=validateAddressLine4(field, address, validationFailedFields,isMandatoryFieldEmpty);
          isMandatoryFieldEmpty=validateZipCode(field, address, validationFailedFields,isMandatoryFieldEmpty);
          isMandatoryFieldEmpty= validatePostalCode(field, address, validationFailedFields,isMandatoryFieldEmpty);
          isMandatoryFieldEmpty= validatePinCode(field, address, validationFailedFields,isMandatoryFieldEmpty);
          isMandatoryFieldEmpty=validateRegion(field, address, validationFailedFields,isMandatoryFieldEmpty);
          isMandatoryFieldEmpty=validateState(field, address, validationFailedFields,isMandatoryFieldEmpty);
          isMandatoryFieldEmpty=validateCity(field, address, validationFailedFields,isMandatoryFieldEmpty);
          isMandatoryFieldEmpty=validateStreet(field, address, validationFailedFields,isMandatoryFieldEmpty);
          isMandatoryFieldEmpty=validatePOBox(field, address, validationFailedFields,isMandatoryFieldEmpty);
          isMandatoryFieldEmpty=validateDistrict(field, address, validationFailedFields,isMandatoryFieldEmpty);
          isMandatoryFieldEmpty= validateArea(field, address, validationFailedFields,isMandatoryFieldEmpty);
          isMandatoryFieldEmpty=validateSTaluka(field, address, validationFailedFields,isMandatoryFieldEmpty);
          isMandatoryFieldEmpty=validateVillage(field, address, validationFailedFields,isMandatoryFieldEmpty);
          isMandatoryFieldEmpty= validateAccomodationType(field, address, validationFailedFields,isMandatoryFieldEmpty);
          isMandatoryFieldEmpty= validateResidenceType(field, address, validationFailedFields,isMandatoryFieldEmpty);
        }
        if(isMandatoryFieldEmpty){
          message=AddressConstants.VALIDATION_FAIL;
          map.put("message",message);
          map.put("MandatoryFieldsFailed", validationFailedFields.toString());
        }else{
          message=AddressConstants.VALIDATION_SUCCESS;
          map.put("message",message);
        }
      } 
      return map;
    }
    
    private void setDefaultCountryConfiguration (NodeList result,String countryCode) throws XPathExpressionException{
      if(result.getLength()==0){
        String xpathForDefault = "/address-configs/address-config/target-country[@name=" + "'" + countryCode + "']/field-config";
        result = (NodeList) factory.newXPath().evaluate(xpathForDefault, document, XPathConstants.NODESET);
      }
    }
    
    private void setFieldsXMLBasedConfiguration(List<FieldConfig> fields, NodeList result){
      for (int i = 0 ; i < result.getLength() ; i++) {
        FieldConfig field = new FieldConfig();
        Node childNode = result.item(i);
        field.setFieldName(childNode.getAttributes().getNamedItem("name").getNodeValue());
        field.setMandatory(childNode.getAttributes().getNamedItem("mandatory").getNodeValue());
        fields.add(field);
      }
    }
    
    
    private Boolean validateAddressLine1(FieldConfig field,Address address,StringBuilder validationFailedFields,Boolean isMandatoryFieldEmpty){
      isMandatoryFieldEmpty = isMandatoryFieldEmpty?isMandatoryFieldEmpty:Boolean.FALSE;
      if (isSameFieldAndMandatory(field,AddressConstants.ADDRESS_1,address.getAddressLine1())) {
        validationFailedFields.append(AddressConstants.ADDRESS_1+"/");
        isMandatoryFieldEmpty=Boolean.TRUE;
      }
      return isMandatoryFieldEmpty;
    }
    
    private Boolean validateAddressLine2(FieldConfig field,Address address,StringBuilder validationFailedFields,Boolean isMandatoryFieldEmpty){
      isMandatoryFieldEmpty = isMandatoryFieldEmpty?isMandatoryFieldEmpty:Boolean.FALSE;
      if (isSameFieldAndMandatory(field, AddressConstants.ADDRESS_2,address.getAddressLine2())) {
        validationFailedFields.append(AddressConstants.ADDRESS_2+"/");
        isMandatoryFieldEmpty=Boolean.TRUE;;
      }
      return isMandatoryFieldEmpty;
    }
    
    private Boolean validateAddressLine3(FieldConfig field,Address address,StringBuilder validationFailedFields,Boolean isMandatoryFieldEmpty){
      isMandatoryFieldEmpty = isMandatoryFieldEmpty?isMandatoryFieldEmpty:Boolean.FALSE;
      if (isSameFieldAndMandatory(field,AddressConstants.ADDRESS_3,address.getAddressLine3())) {
        validationFailedFields.append(AddressConstants.ADDRESS_3+"/");
        isMandatoryFieldEmpty=Boolean.TRUE;;
      }
      return isMandatoryFieldEmpty;
    }
    
    
    private Boolean validateAddressLine4(FieldConfig field,Address address,StringBuilder validationFailedFields,Boolean isMandatoryFieldEmpty){
      isMandatoryFieldEmpty = isMandatoryFieldEmpty?isMandatoryFieldEmpty:Boolean.FALSE;
      if (isSameFieldAndMandatory(field,AddressConstants.ADDRESS_4,address.getAddressLine4())) {
        validationFailedFields.append(AddressConstants.ADDRESS_4+"/");
        isMandatoryFieldEmpty=Boolean.TRUE;;
      }
      return isMandatoryFieldEmpty;
    }
    
    private Boolean validateZipCode(FieldConfig field,Address address,StringBuilder validationFailedFields,Boolean isMandatoryFieldEmpty){
      isMandatoryFieldEmpty = isMandatoryFieldEmpty?isMandatoryFieldEmpty:Boolean.FALSE;
      if (isSameFieldAndMandatory(field,AddressConstants.ZIP_CODE,address.getZipcode())) {
        validationFailedFields.append(AddressConstants.ZIP_CODE+"/");
        isMandatoryFieldEmpty=Boolean.TRUE;;
      }
      return isMandatoryFieldEmpty;
    }
    
    private Boolean validatePostalCode(FieldConfig field,Address address,StringBuilder validationFailedFields,Boolean isMandatoryFieldEmpty){
      isMandatoryFieldEmpty = isMandatoryFieldEmpty?isMandatoryFieldEmpty:Boolean.FALSE;
      if (isSameFieldAndMandatory(field,AddressConstants.POSTAL_CODE,address.getZipcode())) {
        validationFailedFields.append(AddressConstants.POSTAL_CODE+"/");
        isMandatoryFieldEmpty=Boolean.TRUE;;
      }
      return isMandatoryFieldEmpty;
    }
    
    private Boolean validatePinCode(FieldConfig field,Address address,StringBuilder validationFailedFields,Boolean isMandatoryFieldEmpty){
      isMandatoryFieldEmpty = isMandatoryFieldEmpty?isMandatoryFieldEmpty:Boolean.FALSE;
      if (isSameFieldAndMandatory(field,AddressConstants.PIN_CODE,address.getZipcode())) {
        validationFailedFields.append(AddressConstants.PIN_CODE+"/");
        isMandatoryFieldEmpty = Boolean.TRUE;;
      }
      return isMandatoryFieldEmpty;
    }
    
    private Boolean validateRegion(FieldConfig field,Address address,StringBuilder validationFailedFields,Boolean isMandatoryFieldEmpty){
      isMandatoryFieldEmpty = isMandatoryFieldEmpty?isMandatoryFieldEmpty:Boolean.FALSE;
      if (isSameFieldAndMandatory(field,AddressConstants.REGION,address.getRegion())) {
        validationFailedFields.append(AddressConstants.REGION+"/");
        isMandatoryFieldEmpty = Boolean.TRUE;;
      }
      return isMandatoryFieldEmpty;
    }
    
    private Boolean validateState(FieldConfig field,Address address,StringBuilder validationFailedFields,Boolean isMandatoryFieldEmpty){
      isMandatoryFieldEmpty = isMandatoryFieldEmpty?isMandatoryFieldEmpty:Boolean.FALSE;
      if (isSameFieldAndMandatory(field,AddressConstants.STATE,address.getState())) {
        validationFailedFields.append(AddressConstants.STATE+"/");
        isMandatoryFieldEmpty = Boolean.TRUE;;
      }
      return isMandatoryFieldEmpty;
    }
    
    private Boolean validateCity(FieldConfig field,Address address,StringBuilder validationFailedFields,Boolean isMandatoryFieldEmpty){
      isMandatoryFieldEmpty = isMandatoryFieldEmpty?isMandatoryFieldEmpty:Boolean.FALSE;
      if (isSameFieldAndMandatory(field,AddressConstants.CITY,address.getCity())) {
        validationFailedFields.append(AddressConstants.CITY+"/");
        isMandatoryFieldEmpty = Boolean.TRUE;;
      }
      return isMandatoryFieldEmpty;
    }
    
    
    private Boolean validateStreet(FieldConfig field,Address address,StringBuilder validationFailedFields,Boolean isMandatoryFieldEmpty){
      isMandatoryFieldEmpty = isMandatoryFieldEmpty?isMandatoryFieldEmpty:Boolean.FALSE;
      if (isSameFieldAndMandatory(field,AddressConstants.STREET,address.getStreet())) {
        validationFailedFields.append(AddressConstants.STREET+"/");
        isMandatoryFieldEmpty = Boolean.TRUE;;
      }
      return isMandatoryFieldEmpty;
    }
    
    
    private Boolean validatePOBox(FieldConfig field,Address address,StringBuilder validationFailedFields,Boolean isMandatoryFieldEmpty){
      isMandatoryFieldEmpty = isMandatoryFieldEmpty?isMandatoryFieldEmpty:Boolean.FALSE;
      if (isSameFieldAndMandatory(field,AddressConstants.PO_BOX,address.getPoBox()) ) {
        validationFailedFields.append(AddressConstants.PO_BOX+"/");
        isMandatoryFieldEmpty = Boolean.TRUE;;
      }
      return isMandatoryFieldEmpty;
    }
    
    
    
    private Boolean validateDistrict(FieldConfig field,Address address,StringBuilder validationFailedFields,Boolean isMandatoryFieldEmpty){
      isMandatoryFieldEmpty = isMandatoryFieldEmpty?isMandatoryFieldEmpty:Boolean.FALSE;
      if (isSameFieldAndMandatory(field,AddressConstants.DISTRICT,address.getDistrict())) {
        validationFailedFields.append(AddressConstants.DISTRICT+"/");
        isMandatoryFieldEmpty = Boolean.TRUE;;
      }
      return isMandatoryFieldEmpty;
    }
    
    private Boolean validateArea(FieldConfig field,Address address,StringBuilder validationFailedFields,Boolean isMandatoryFieldEmpty){
      isMandatoryFieldEmpty = isMandatoryFieldEmpty?isMandatoryFieldEmpty:Boolean.FALSE;
      if (isSameFieldAndMandatory(field,AddressConstants.AREA,address.getArea())) {
        validationFailedFields.append(AddressConstants.AREA+"/");
        isMandatoryFieldEmpty = Boolean.TRUE;;
      }
      return isMandatoryFieldEmpty;
    }
    
    private Boolean validateSTaluka(FieldConfig field,Address address,StringBuilder validationFailedFields,Boolean isMandatoryFieldEmpty){
      isMandatoryFieldEmpty = isMandatoryFieldEmpty?isMandatoryFieldEmpty:Boolean.FALSE;
      if (isSameFieldAndMandatory(field,AddressConstants.TALUKA,address.getTaluka())) {
        validationFailedFields.append(AddressConstants.TALUKA+"/");
        isMandatoryFieldEmpty = Boolean.TRUE;;
      }
      return isMandatoryFieldEmpty;
    }
    
    private Boolean validateVillage(FieldConfig field,Address address,StringBuilder validationFailedFields,Boolean isMandatoryFieldEmpty){
      isMandatoryFieldEmpty = isMandatoryFieldEmpty?isMandatoryFieldEmpty:Boolean.FALSE;
      if (isSameFieldAndMandatory(field,AddressConstants.VILLAGE,address.getVillage())) {
        validationFailedFields.append(AddressConstants.VILLAGE+"/");
        isMandatoryFieldEmpty = Boolean.TRUE;
      }
      return isMandatoryFieldEmpty;
    }
    
    private Boolean validateAccomodationType(FieldConfig field,Address address,StringBuilder validationFailedFields,Boolean isMandatoryFieldEmpty){
      isMandatoryFieldEmpty = isMandatoryFieldEmpty?isMandatoryFieldEmpty:Boolean.FALSE;
      if (isSameFieldAndMandatory(field,AddressConstants.ACCOMMODATION_TYPE,address.getAccomodationType())) {
        validationFailedFields.append(AddressConstants.ACCOMMODATION_TYPE+"/");
        isMandatoryFieldEmpty = Boolean.TRUE;;
      }
      return isMandatoryFieldEmpty;
    }
    
    private Boolean validateResidenceType(FieldConfig field,Address address,StringBuilder validationFailedFields,Boolean isMandatoryFieldEmpty){
      isMandatoryFieldEmpty = isMandatoryFieldEmpty?isMandatoryFieldEmpty:Boolean.FALSE;
      if (isSameFieldAndMandatory(field,AddressConstants.RESIDENCE_TYPE,address.getResidenceType())) {
        validationFailedFields.append(AddressConstants.RESIDENCE_TYPE+"/");
        isMandatoryFieldEmpty = Boolean.TRUE;;
      }
      return isMandatoryFieldEmpty;
    }
    
    private Boolean isSameFieldAndMandatory(FieldConfig field,String targetFieldName,Object fieldName){
      return (field.getFieldName().equals(targetFieldName)  
          && field.getMandatory().equals("true")
          && isEmpty(fieldName));
    }
}
  

