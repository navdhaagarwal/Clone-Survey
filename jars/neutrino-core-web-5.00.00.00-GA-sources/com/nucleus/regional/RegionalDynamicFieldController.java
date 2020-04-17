package com.nucleus.regional;

import static com.nucleus.finnone.pro.general.util.ValidatorUtils.isNull;
import static com.nucleus.finnone.pro.general.util.ValidatorUtils.notNull;
import static org.apache.commons.collections.MapUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestParam;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.nucleus.core.initialization.NeutrinoResourceLoader;
import com.nucleus.logging.BaseLoggers;


@Transactional
@Named("regionalDynamicFieldController")
public class RegionalDynamicFieldController {
	
	private static Map<String, Object> regionalDataCacheMap=new HashMap<String, Object>();
	
	private static final String TARGET_SCREEN_XPATH= "/regional-configs/regional-config/target-screen";
	private static XPathFactory      factory = XPathFactory.newInstance();
    private Document                 document;
    
    @Inject
    @Named("frameworkConfigResourceLoader")
    protected NeutrinoResourceLoader frameworkConfigResourceLoader;
        
    @PostConstruct
    public void initializeDocument() throws Exception {
    	
        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        InputStream inputStream = frameworkConfigResourceLoader.getResource("regional-fields-mapping.xml").getInputStream();
        if(notNull(inputStream)){
	        document = builder.parse(inputStream);     
	        initializeRegionalDataMap();
        }else{
        	BaseLoggers.exceptionLogger.debug("Failed to initialize regionalDataCacheMap");
        }
    }
    
    
    private void initializeRegionalDataMap() throws XPathExpressionException {
    	   NodeList targetScreenNodeList = (NodeList) factory.newXPath().evaluate(TARGET_SCREEN_XPATH,document, XPathConstants.NODESET);	        
	        for (int j = 0 ; j < targetScreenNodeList.getLength() ; j++) {
	        	Node childNode = targetScreenNodeList.item(j);
			if (checkNullOrEmpty(childNode, RegionalFieldConstants.NAME)) {
				String targetScreenName = childNode.getAttributes().getNamedItem(RegionalFieldConstants.NAME).getNodeValue();
				if (notNull(targetScreenName)) {
					createRegionalDataMap(targetScreenName);
				}
			}
           }
	}
    
    public Map<String,Object> paintRegionalFields(@RequestParam(value = "screenSectionIdValue", required = true) String screenSectionIdValue)
                throws XPathExpressionException {    	
    	 	return getRegionalFieldConfigMapFromCache(screenSectionIdValue);
    	}
    
    /*This method checks whether attribute is present and
     * whether its value is null or empty */
    private Boolean checkNullOrEmpty(Node childNode,String nodeName){
    	if(childNode.getAttributes().getNamedItem(nodeName)!=null && 
    			isNotBlank(childNode.getAttributes().getNamedItem(nodeName).getNodeValue())){    		
    		return true;
    	}
    	return false;
    }
    
    
    private Map<String,Object> createMapFromXpath(NodeList xpath, Map<String, Object> regionalDataMap,String fieldType){
    	
    	if(isNull(xpath)){
    		return regionalDataMap;
    	}
    	for (int i = 0 ; i < xpath.getLength() ; i++) {
                RegionalFieldConfig regionalFieldConfig = new RegionalFieldConfig();
                Node childNode = xpath.item(i);
                
                
                /*This check represents name is mandatory field in regional-fields-mapping.xml*/
                
                if(checkNullOrEmpty(childNode,RegionalFieldConstants.NAME)){
               	 regionalFieldConfig.setFieldName(childNode.getAttributes().getNamedItem(RegionalFieldConstants.NAME).getNodeValue());
               	 
               	 if(checkNullOrEmpty(childNode,RegionalFieldConstants.MANDATORY)){
               		 regionalFieldConfig.setMandatory(childNode.getAttributes().getNamedItem(RegionalFieldConstants.MANDATORY).getNodeValue());
               	 }
               	 
               	 if(checkNullOrEmpty(childNode,RegionalFieldConstants.VIEW_MODE)){
               		 regionalFieldConfig.setViewMode(childNode.getAttributes().getNamedItem(RegionalFieldConstants.VIEW_MODE).getNodeValue());          
               	 }
               	 
               	 if(checkNullOrEmpty(childNode,RegionalFieldConstants.DISABLED)){
               		 regionalFieldConfig.setDisabled(childNode.getAttributes().getNamedItem(RegionalFieldConstants.DISABLED).getNodeValue());          
               	 }
               	 
               	 if(checkNullOrEmpty(childNode,RegionalFieldConstants.REGIONAL_VISIBILITY)){
             		 regionalFieldConfig.setRegionalVisibility(childNode.getAttributes().getNamedItem(RegionalFieldConstants.REGIONAL_VISIBILITY).getNodeValue());
             	 }
               	 if(checkNullOrEmpty(childNode,RegionalFieldConstants.LABEL)){
               		 regionalFieldConfig.setFieldLabel(childNode.getAttributes().getNamedItem(RegionalFieldConstants.LABEL).getNodeValue());  
               	 }
               	 
               	 if(checkNullOrEmpty(childNode,RegionalFieldConstants.PLACE_HOLDER_KEY)){
               		 regionalFieldConfig.setFieldPlaceHolderKey(childNode.getAttributes().getNamedItem(RegionalFieldConstants.PLACE_HOLDER_KEY).getNodeValue());
               	 }
               	 
               	 if(checkNullOrEmpty(childNode,RegionalFieldConstants.TOOL_TIP_KEY)){
               		 regionalFieldConfig.setFieldToolTipKey(childNode.getAttributes().getNamedItem(RegionalFieldConstants.TOOL_TIP_KEY).getNodeValue());
               	 }
               	 
               	 if(fieldType != null && fieldType.equals(RegionalFieldConstants.REGIONAL)){
	              
               		 if(checkNullOrEmpty(childNode,RegionalFieldConstants.SOURCE_ENTITY_NAME)){
	               		 regionalFieldConfig.setSourceEntityName(childNode.getAttributes().getNamedItem(RegionalFieldConstants.SOURCE_ENTITY_NAME).getNodeValue());  
	               	 }
	               	 if(checkNullOrEmpty(childNode,RegionalFieldConstants.DIV_ID)){
	               		 regionalFieldConfig.setDivId(childNode.getAttributes().getNamedItem(RegionalFieldConstants.DIV_ID).getNodeValue());
	               	 }
	                
	                 if(checkNullOrEmpty(childNode,RegionalFieldConstants.REGIONAL_GENERIC_PARAMETER_TYPE)){
                         regionalFieldConfig.setRegionalGenericParameterType(childNode.getAttributes().getNamedItem(RegionalFieldConstants.REGIONAL_GENERIC_PARAMETER_TYPE).getNodeValue());
                     }
                     if(checkNullOrEmpty(childNode,RegionalFieldConstants.IS_GENERIC)){
                         regionalFieldConfig.setIsGeneric(childNode.getAttributes().getNamedItem(RegionalFieldConstants.IS_GENERIC).getNodeValue());
                     }
                     
                     if(checkNullOrEmpty(childNode,RegionalFieldConstants.REGIONAL_ITEM_VALUE)){
                         regionalFieldConfig.setRegionalItemValue(childNode.getAttributes().getNamedItem(RegionalFieldConstants.REGIONAL_ITEM_VALUE).getNodeValue());
                     }
                     if(checkNullOrEmpty(childNode,RegionalFieldConstants.REGIONAL_ITEM_LABEL)){
                         regionalFieldConfig.setRegionalItemLabel(childNode.getAttributes().getNamedItem(RegionalFieldConstants.REGIONAL_ITEM_LABEL).getNodeValue());
                     }
                     if(checkNullOrEmpty(childNode,RegionalFieldConstants.REGIONAL_LIST_VALUE)){
                         regionalFieldConfig.setRegionalListValue(childNode.getAttributes().getNamedItem(RegionalFieldConstants.REGIONAL_LIST_VALUE).getNodeValue());
                     }
                     if(checkNullOrEmpty(childNode,RegionalFieldConstants.REGIONAL_ITEM_CODE)){
                         regionalFieldConfig.setRegionalItemCode(childNode.getAttributes().getNamedItem(RegionalFieldConstants.REGIONAL_ITEM_CODE).getNodeValue());
                     }
                     if(checkNullOrEmpty(childNode,RegionalFieldConstants.REGIONAL_PARENT_ID)){
                         regionalFieldConfig.setRegionalParentId(childNode.getAttributes().getNamedItem(RegionalFieldConstants.REGIONAL_PARENT_ID).getNodeValue());
                     }	    
                     
               	 }                  	                	 
               	 regionalDataMap.put(childNode.getAttributes().getNamedItem(RegionalFieldConstants.NAME).getNodeValue(), regionalFieldConfig);
                }
            }
     
    	return regionalDataMap;
    }
    
    
    @SuppressWarnings("unchecked")
    private Map<String, Object> getRegionalFieldConfigMapFromCache(String key){
                  
 	   return  (Map<String, Object>) regionalDataCacheMap.get(key);
    }
    
    private void createRegionalDataMap(String screenSectionIdValue) throws XPathExpressionException {
     
        Map<String, Object> regionalDataMapCache=getRegionalFieldConfigMapFromCache(screenSectionIdValue);
        
		if (isEmpty(regionalDataMapCache)) {
			prepareRegionalDataMapCache(screenSectionIdValue);
		}
     }
    
    private void prepareRegionalDataMapCache(String screenSectionIdValue) throws XPathExpressionException {
		Map<String, Object> regionalDataMap = new HashMap<String, Object>();

		String regionalXPath = TARGET_SCREEN_XPATH+"[@name="
				+ "'"
				+ screenSectionIdValue
				+ "']/field-type[@name='regional']/field-config";
		String coreXPath = TARGET_SCREEN_XPATH+"[@name="
				+ "'"
				+ screenSectionIdValue
				+ "']/field-type[@name='core']/field-config";
		NodeList resultRegionalXPath = (NodeList) factory.newXPath()
				.evaluate(regionalXPath, document, XPathConstants.NODESET);
		NodeList resultCoreXPath = (NodeList) factory.newXPath().evaluate(
				coreXPath, document, XPathConstants.NODESET);

		regionalDataMap = createMapFromXpath(resultRegionalXPath,
				regionalDataMap, RegionalFieldConstants.REGIONAL);
		regionalDataMap = createMapFromXpath(resultCoreXPath,
				regionalDataMap, RegionalFieldConstants.CORE);			
		if (!isEmpty(regionalDataMap)) {
			putRegionalFieldConfigMapIntoCache(screenSectionIdValue,
					regionalDataMap);
		}	
	}


	private void putRegionalFieldConfigMapIntoCache(String key,Map<String, Object> regionalFieldConfigMap){        
 	   regionalDataCacheMap.put(key, regionalFieldConfigMap);    
    }
}
