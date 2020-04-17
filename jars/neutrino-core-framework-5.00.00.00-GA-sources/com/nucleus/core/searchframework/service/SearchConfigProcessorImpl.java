/**
 * This file and a proportion of its content is copyright of Nucleus Software Exports Limited - � 2012. All rights reserved.
 * Any redistribution or reproduction of part or all of the contents in any form is prohibited other than the following:
 * - you cannot print or download to a local hard disk extract contents either part or full for personal/ commercial/
 * academic or any other use
 * - you may not copy the content to individual/ third parties for any type of use, either as compiled or source format
 * without the knowledge and consent of Nucleus Software
 * - You may not, except with our express written permission, distribute or commercially exploit the content. Nor may you
 * transmit it or store it in any other web site or other form of electronic retrieval system.
 */
package com.nucleus.core.searchframework.service;

import static com.nucleus.finnone.pro.general.util.ValidatorUtils.noNullElements;
import static com.nucleus.finnone.pro.general.util.ValidatorUtils.notNull;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.nucleus.core.searchframework.entity.ConstantSearchAttribute;
import com.nucleus.core.searchframework.entity.ObjectGraphSearchAttribute;
import com.nucleus.core.searchframework.entity.SearchAttributeBean;
import com.nucleus.core.searchframework.entity.SearchAttributeExpression;
import com.nucleus.core.searchframework.entity.SearchCriteriaClause;
import com.nucleus.core.searchframework.entity.SearchRequest;
import com.nucleus.core.xml.util.XmlUtils;
import com.nucleus.finnone.pro.base.exception.BusinessException;
import com.nucleus.finnone.pro.base.exception.ExceptionBuilder;
import com.nucleus.finnone.pro.general.constants.ExceptionSeverityEnum;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.regional.RegionalEnabled;
import com.nucleus.regional.metadata.service.IRegionalMetaDataService;

/**
 * Class To Read SearchConfig.xml
 * Method processSearchCriteria() read the XML file and make a list of SearchAttributeBean
 * UI will be painted by reading this List of SearchAttributeBean.This list contain the information
 * of textbox to be painted with there default values if any and data types
 * 
 * Method getSearchRequestData() accept the list of SearchAttributeBean coming from UI
 * and again read the xml file along with this List (For values) and by matching the unique ID's
 * of each SearchAttribute, it make the Expression for only those SearchAttribute which have some values in it
 * @author Nucleus Software Exports Limited
 */
@Named(value = "searchConfigProcessor")
public class SearchConfigProcessorImpl implements SearchConfigProcessor {

    private static final String SEARCH_CONFIG          = "searchConfig";
    private static final String SEARCH_CRITERIA_CLAUSE = "searchCriteriaClause";
    private static final String SEARCH_ATTRIBUTE       = "searchAttribute";
    private static final String SELECT_FIELD           = "selectField";
    private static final String FIELD                  = "field";
    private static final String SEARCH_CONFIGURATIONS  = "searchConfigurations";
    private static final String GROUP_BY_CLAUSE        = "groupByClause";

    private static final String OR_GROUP               = "or-Group";
    private static final String AND_GROUP              = "and-Group";

    private static final String S_VALUE                = "svalue";
    private static final String I_VALUE                = "ivalue";
    private static final String D_VALUE                = "dvalue";
    private static final String B_VALUE                = "bvalue";
    private static final String R_VALUE                = "rvalue";
    private static final String L_VALUE                = "lvalue";
    private static final String N_VALUE                = "nvalue";
    private static final String REGIONAL_FIELD			="regionalField";
    private static final String OGNL                   = "ognl";
    private static final String SOURCE_ENTITY_NAME	   ="sourceEntityName";
    private static final String DISPLAY                = "display";
    private static final String VALUE                  = "value";
  /*  private static final String ENABLE                 = "enable";
    private static final String DISABLE                = "disable";*/
    private static final String LOOKUP                 = "lookup";
    private static final String ENTITY_ID              = "id";
    private static final String SEARCH_ATTRIBUTE_ID    = "id";
   /* private static final String NONE                   = "none";
    private static final String BLOCK                  = "block";*/
    private static final String VISIBLE                = "visible";
    private static final String HIDDEN                 = "hidden";

    private static final String AND                    = "AND";
    private static final String OR                     = "OR";

    private static final String EQUAL                  = "equal";
    private static final String CONTAIN                = "contain";
    private static final String BETWEEN                = "between";
    private static final String GREATER_THEN           = "greaterThen";
    private static final String LESS_THEN              = "lessThen";
    private static final String GREATER_THEN_EQUAL_TO  = "greaterThenEqualTo";
    private static final String LESS_THEN_EQUAL_TO     = "lessThenEqualTo";
    private static final String IN                     = "in";
    private static final String ENTITY_CLASS           = "entityClass";
    private static final String SEARCH_TYPE            = "searchType";
    private static final String ISNULL  			   = "isNull";
    
    private static final String EXCEPTION_OCCURED="Exception occured while instantiating SourceEntity: ";
    private static final String INVALID_REGIONAL_SOURCE_ENTITY="Source entity name does not implements Regional Enabled";
    
    @Inject
    @Named("regionalMetaDataService")
    private IRegionalMetaDataService regionalMetaDataService;

    // private int count = 0;

    /**
     * Method To read SearchConfig.xml and add SearchAttributes 
     * in a list of SearchAttributeBean.Read XML Node by Node 
     * with tag names and make a list of SearchAttributeBean
     * @throws IOException
     */
    public List<SearchAttributeBean> prepareSearchAttributesForSearchConfiguration(String searchConfigId) {
        try {
            /*
             * Load XML
             */
            int uid = 0;
            InputStream inputStream = new PathMatchingResourcePatternResolver().getResource("classpath:SearchConfig.xml")
                    .getInputStream();
            List<SearchAttributeBean> searchAttributeBeanList = new ArrayList<SearchAttributeBean>();
            if (inputStream != null) {
                String input = IOUtils.toString(inputStream);
                /*
                 * Load Document From XML
                 */
                Document doc = XmlUtils.readFromXml(input);
                doc.getDocumentElement().normalize();

                NodeList listOfSearchConfigurations = doc.getElementsByTagName(SEARCH_CONFIGURATIONS);
                Node rootNode = listOfSearchConfigurations.item(0);

                /*
                 * Get list of SearchRequest tag from xml
                 */
                NodeList listOfSearchConfig = ((Element) rootNode).getElementsByTagName(SEARCH_CONFIG);

                for (int j = 0 ; j < listOfSearchConfig.getLength() ; j++) {
                    Node firstNode = listOfSearchConfig.item(j);

                    /*
                     * Load Entity Id of SearchRequest
                     */
                    String xmlConfigId = ((Element) firstNode).getAttributes().getNamedItem(ENTITY_ID).getNodeValue();

                    /*
                     * Check Entity Id From Coming From UI matches Config File ID
                     */
                    if (xmlConfigId.equalsIgnoreCase(searchConfigId)) {
                        /*
                         * Get Of SearchCriteriaClause tag from xml
                         */
                        NodeList whereClause = ((Element) firstNode).getElementsByTagName(SEARCH_CRITERIA_CLAUSE);
                        Node whereClauseNode = whereClause.item(0);
                        /*
                         * Get Of SearchAttribute tag from xml
                         */
                        NodeList searchAttribute = ((Element) whereClauseNode).getElementsByTagName(SEARCH_ATTRIBUTE);
                        /*
                         * Iterate on SearchAttribute and make list of SearchAttributeBean,
                         * based on its attributes,ognl values,operator,type
                         */
                        for (int i = 0 ; i <= searchAttribute.getLength() - 1 ; i++) {
                            SearchAttributeBean searchAttributeBean = new SearchAttributeBean();
                            Node searchAttributeNode = searchAttribute.item(i);
                            NamedNodeMap searchAttributeNodeMap = ((Element) searchAttributeNode).getAttributes();
                            String field= validateRegionalFieldsInSearchNodeMap(searchAttributeNodeMap);
                            if(isBlank(field)){
                            	continue;
                            }
                            String displayName ="";
                            if(searchAttributeNodeMap.getNamedItem("displayName")!=null)
                            {
                                displayName = searchAttributeNodeMap.getNamedItem("displayName").getNodeValue();
                            }
                           
                            String binderName = searchAttributeNodeMap.getNamedItem(LOOKUP).getNodeValue();
                            String value = "";
                            if (searchAttributeNode.getFirstChild().getNextSibling().getAttributes().getNamedItem(VALUE) != null) {
                                value = searchAttributeNode.getFirstChild().getNextSibling().getAttributes()
                                        .getNamedItem(VALUE).getNodeValue();
                            }
                            String display = VISIBLE;
                            if (!"".equals(searchAttributeNodeMap.getNamedItem(DISPLAY).getNodeValue())) {
                                display = searchAttributeNodeMap.getNamedItem(DISPLAY).getNodeValue();
                            }
                            // String displayable = searchAttributeNodeMap.getNamedItem(DISPLAY).getNodeValue();
                            String searchAttrId = searchAttributeNodeMap.getNamedItem(SEARCH_ATTRIBUTE_ID).getNodeValue();
                            // String display = VISIBLE;
                            /*if (displayable.equals(HIDDEN)) {
                                display = HIDDEN;
                            }*/
                            String type = StringUtils.substringBefore(searchAttributeNode.getFirstChild().getNextSibling()
                                    .getNodeName(), "-");
                            String operator = getOperator(StringUtils.substringAfter(searchAttributeNode.getFirstChild()
                                    .getNextSibling().getNodeName(), "-"));
                            /*
                             * Handle Case Of between differently from other
                             * operators and make a list of beans
                             */
                            if (operator.equals("><")) {
                                for (int k = 0 ; k < 2 ; k++) {
                                    SearchAttributeBean attributeBean = new SearchAttributeBean();
                                    attributeBean.setField(field);
                                    attributeBean.setType(type);
                                    if (k == 0) {
                                        attributeBean.setId("From_" + searchAttrId);
                                        attributeBean.setOperator(">=");
                                    } else {
                                        attributeBean.setOperator("<=");
                                        attributeBean.setId("To_" + searchAttrId);
                                    }
                                    attributeBean.setDisplay(display);
                                    attributeBean.setValue(value);
                                    attributeBean.setBinderName(binderName);
                                    attributeBean.setDisplayName(displayName);
                                    searchAttributeBeanList.add(attributeBean);
                                }

                            } else {
                                searchAttributeBean.setField(field);
                                searchAttributeBean.setId(searchAttrId);
                                searchAttributeBean.setType(type);
                                searchAttributeBean.setOperator(operator);
                                searchAttributeBean.setDisplay(display);
                                searchAttributeBean.setDisplayName(displayName);
                                searchAttributeBean.setValue(value);
                                searchAttributeBean.setBinderName(binderName);
                                searchAttributeBeanList.add(searchAttributeBean);
                            }

                        }
                    }
                }
            }
            return searchAttributeBeanList;
        } catch (IOException io) {
            throw new SearchException("Error occured while loading config file SearchConfig.xml", io);
        }
    }

    private String validateRegionalFieldsInSearchNodeMap(NamedNodeMap searchAttributeNodeMap) {
    	 String field= null;
         if(notNull(searchAttributeNodeMap.getNamedItem(REGIONAL_FIELD))
         	&& notNull(searchAttributeNodeMap.getNamedItem(SOURCE_ENTITY_NAME))	){  
        	 field=prepareFieldValueBasedOnRegionalFieldAndSourceEntity(searchAttributeNodeMap);         	                        	
         }
        else{                           
          field = searchAttributeNodeMap.getNamedItem(OGNL).getNodeValue();
         }         
         return field;		
	}

	private String prepareFieldValueBasedOnRegionalFieldAndSourceEntity(
			NamedNodeMap searchAttributeNodeMap) {
		String regionalField=searchAttributeNodeMap.getNamedItem(REGIONAL_FIELD).getNodeValue();
        String sourceEntityName=searchAttributeNodeMap.getNamedItem(SOURCE_ENTITY_NAME).getNodeValue();  
        String ognlValue=searchAttributeNodeMap.getNamedItem(OGNL).getNodeValue();
        String field=null;
        if(noNullElements(regionalField,sourceEntityName,ognlValue)){        	
        	try {
    			Object classObject=Class.forName(sourceEntityName).newInstance();
    			if(classObject instanceof RegionalEnabled){
    				field= getFieldValueBasedOnRegionalFieldAndSourceEntity(sourceEntityName,ognlValue,regionalField);  			
    			}else{
    				throw ExceptionBuilder.getInstance(BusinessException.class,INVALID_REGIONAL_SOURCE_ENTITY,INVALID_REGIONAL_SOURCE_ENTITY).setSeverity(ExceptionSeverityEnum.SEVERITY_HIGH.getEnumValue()).build();
    			}
    		} catch (InstantiationException e) {
    			BaseLoggers.exceptionLogger.debug(EXCEPTION_OCCURED+ e);
    			throw ExceptionBuilder.getInstance(BusinessException.class,e.getMessage(),EXCEPTION_OCCURED).setSeverity(ExceptionSeverityEnum.SEVERITY_HIGH.getEnumValue()).setOriginalException(e).build();
    		} catch (IllegalAccessException e) {
    			BaseLoggers.exceptionLogger.debug(EXCEPTION_OCCURED+ e);
    			throw ExceptionBuilder.getInstance(BusinessException.class,e.getMessage(),EXCEPTION_OCCURED).setSeverity(ExceptionSeverityEnum.SEVERITY_HIGH.getEnumValue()).setOriginalException(e).build();
    		} catch (ClassNotFoundException e) {
    			BaseLoggers.exceptionLogger.debug(EXCEPTION_OCCURED+ e);
    			throw ExceptionBuilder.getInstance(BusinessException.class,e.getMessage(),EXCEPTION_OCCURED).setSeverity(ExceptionSeverityEnum.SEVERITY_HIGH.getEnumValue()).setOriginalException(e).build();
    		}
    	}  	      	               
        return field;
	}

	private String getFieldValueBasedOnRegionalFieldAndSourceEntity(
			String sourceEntityName,String ognlValue,String regionalField) {		 
			String regionalPath=regionalMetaDataService.getRegionalPathBasedOnSourceEntityAndLogicalField(regionalField, sourceEntityName);
			if(isNotBlank(regionalPath)){
				StringBuilder appendedField=new StringBuilder();
	        	appendedField=appendedField.append(ognlValue).append(".").append(regionalPath);                          	   	  
	        	return appendedField.toString();
			}
        	return null;
	}

	/**
     *Get the list Of SearchAttributeBean from UI and read the SearchConfig.xml
     *again with values in the List and make SearchAttributeExpression with these.
     *And Make the SearchExpressionTree.  
     * @throws IOException
     */
    public SearchRequest getSearchRequestData(List<SearchAttributeBean> searchAttributeBeanList, String searchRequestEntityId) {
        try {
            SearchAttributeExpression criteriaExpression = null;
            String className = "";
            SearchRequest searchRequest = new SearchRequest();
            List<String> selectFieldList = new ArrayList<String>();
            List<String> groupByFieldList = new ArrayList<String>();
            String searchType = "";

            // Load XML Config File
            InputStream inputStream = new PathMatchingResourcePatternResolver().getResource("classpath:SearchConfig.xml")
                    .getInputStream();

            if (inputStream != null) {
                /*
                 * Read XmL Config File
                 */
                String input = IOUtils.toString(inputStream);
                Document doc = XmlUtils.readFromXml(input);
                doc.getDocumentElement().normalize();

                NodeList listOfSearchConfig = doc.getElementsByTagName(SEARCH_CONFIG);

                for (int i = 0 ; i < listOfSearchConfig.getLength() ; i++) {
                    Node node = listOfSearchConfig.item(i);
                    String xmlConfigEntityId = ((Element) node).getAttributes().getNamedItem(ENTITY_ID).getNodeValue();
                    /*
                     * Check Entity Id From Coming From UI matches Config File ID
                     */
                    if (xmlConfigEntityId.equalsIgnoreCase(searchRequestEntityId)) {
                        className = ((Element) node).getAttributes().getNamedItem(ENTITY_CLASS).getNodeValue();

                        NodeList searchTypeNodeList = ((Element) node).getElementsByTagName(SEARCH_TYPE);
                        searchType = (((Element) searchTypeNodeList.item(0)).getAttributes().getNamedItem(VALUE)
                                .getNodeValue());

                        if (((Element) node).getElementsByTagName(SELECT_FIELD) != null) {
                            NodeList selectField = ((Element) node).getElementsByTagName(SELECT_FIELD);
                            Node selectFieldNode = selectField.item(0);
                            if (selectFieldNode != null
                                    && ((Element) selectFieldNode).getElementsByTagName(FIELD).getLength() > 0) {
                                NodeList field = ((Element) selectFieldNode).getElementsByTagName(FIELD);
                                /*
                                 * get Field List To Display
                                 */
                                for (int j = 0 ; j < field.getLength() ; j++) {
                                    if (((field.item(j)).getFirstChild()).getTextContent() != null) {
                                        selectFieldList.add(((field.item(j)).getFirstChild()).getTextContent());
                                    }
                                }
                            }
                        }

                        /*
                         * Load Where Clause And send it to createSAExpression()
                         */
                        if (((Element) node).getElementsByTagName(SEARCH_CRITERIA_CLAUSE) != null) {
                            NodeList whereClause = ((Element) node).getElementsByTagName(SEARCH_CRITERIA_CLAUSE);
                            Node whereClauseNode = whereClause.item(0);
                            if (whereClauseNode != null) {
                                criteriaExpression = createSearchAttributeExpression(whereClauseNode.getChildNodes(), AND,
                                        searchAttributeBeanList);
                            }
                        }
                        /*
                         * Group By Clause
                         */
                        if (((Element) node).getElementsByTagName(GROUP_BY_CLAUSE) != null) {
                            NodeList groupByNodeList = ((Element) node).getElementsByTagName(GROUP_BY_CLAUSE);
                            Node groupByNode = groupByNodeList.item(0);
                            if (groupByNode != null && ((Element) groupByNode).getElementsByTagName(FIELD).getLength() > 0) {
                                NodeList groupByField = ((Element) groupByNode).getElementsByTagName(FIELD);
                                /*
                                 * get Group By FieldSet
                                 */
                                for (int j = 0 ; j < groupByField.getLength() ; j++) {
                                    if (((groupByField.item(j)).getFirstChild()).getTextContent() != null) {
                                        groupByFieldList.add(((groupByField.item(j)).getFirstChild()).getTextContent());
                                    }
                                }
                            }
                        }
                    }
                }
                SearchCriteriaClause searchCriteriaClause = new SearchCriteriaClause();
                searchCriteriaClause.setSearchAttributeExpression(criteriaExpression);
                searchRequest.setWhereClause(searchCriteriaClause);
                searchRequest.setSearchOn(className);
                searchRequest.setFieldList(selectFieldList);
                searchRequest.setGroupByList(groupByFieldList);
                searchRequest.setSearchType(searchType);
            }
            // count = 0;
            return searchRequest;
        } catch (IOException io) {
            throw new SearchException("Error occured while loading config file SearchConfig.xml", io);
        }

    }

    /**
     * Method to create SearchAttributeExpression
     * 
     * @param nodes
     * @param logicalGp
     * @param searchAttributeBeanList
     * @return
     */
    private SearchAttributeExpression createSearchAttributeExpression(NodeList nodes, String logicalGp,
            List<SearchAttributeBean> searchAttributeBeanList) {
        SearchAttributeExpression groupExpression = null;

        for (int i = 0 ; i < nodes.getLength() ; i++) {
            Node node = nodes.item(i);
            if (node.getNodeName().equalsIgnoreCase(AND_GROUP)) {
                SearchAttributeExpression expression = createSearchAttributeExpression(node.getChildNodes(), AND,
                        searchAttributeBeanList);
                if (groupExpression == null) {
                    groupExpression = expression;
                    continue;
                }
                SearchAttributeExpression gpExpression = new SearchAttributeExpression();
                gpExpression.setLeftExpression(groupExpression);
                gpExpression.setRightExpression(expression);
                gpExpression.setOperator(logicalGp);
                groupExpression = gpExpression;
                continue;
            }
            if (node.getNodeName().equalsIgnoreCase(OR_GROUP)) {
                SearchAttributeExpression expression = createSearchAttributeExpression(node.getChildNodes(), OR,
                        searchAttributeBeanList);
                if (groupExpression == null) {
                    groupExpression = expression;
                    continue;
                }
                SearchAttributeExpression gpExpression = new SearchAttributeExpression();
                gpExpression.setLeftExpression(groupExpression);
                gpExpression.setRightExpression(expression);
                gpExpression.setOperator(logicalGp);
                groupExpression = gpExpression;
                continue;
            }
            if (node.getNodeName().equalsIgnoreCase(SEARCH_ATTRIBUTE)) {

                NamedNodeMap searchAttributeNodeMap = ((Element) node).getAttributes();
                String ognlValue =validateRegionalFieldsInSearchNodeMap(searchAttributeNodeMap); 
                		


                /*
                 * If the "display" property of Search Attribute is
                 * disable, then directly make a where clause and dont match 
                 * from search attribute bean list
                 */
                if (searchAttributeNodeMap.getNamedItem(DISPLAY).getNodeValue().equalsIgnoreCase(HIDDEN)) {

                    int dataType = getType(StringUtils.substringBefore(node.getFirstChild().getNextSibling().getNodeName(),
                            "-"));
                    String type = StringUtils.substringBefore(node.getFirstChild().getNextSibling().getNodeName(), "-");
                    String value = "";
                    if (node.getFirstChild().getNextSibling().getAttributes().getNamedItem(VALUE) != null) {
                        value = node.getFirstChild().getNextSibling().getAttributes().getNamedItem(VALUE).getNodeValue();
                    }
                    String operator = getOperator(StringUtils.substringAfter(node.getFirstChild().getNextSibling()
                            .getNodeName(), "-"));
                    ObjectGraphSearchAttribute objectGraphSearchAttribute = getObjectGraphSearchAttribute(ognlValue,
                            dataType);

                    ConstantSearchAttribute constantSearchAttribute = getConstantSearchAttribute(value, type, operator,
                            dataType);

                    SearchAttributeExpression leftExpression = new SearchAttributeExpression();
                    leftExpression.setSearchAttribute(objectGraphSearchAttribute);

                    SearchAttributeExpression rightExpression = new SearchAttributeExpression();
                    rightExpression.setSearchAttribute(constantSearchAttribute);

                    SearchAttributeExpression nodeExpression = new SearchAttributeExpression();
                    nodeExpression.setLeftExpression(leftExpression);
                    nodeExpression.setRightExpression(rightExpression);
                    nodeExpression.setOperator(operator);

                    if (groupExpression == null) {
                        groupExpression = nodeExpression;
                        continue;
                    }
                    SearchAttributeExpression gpExpression = new SearchAttributeExpression();
                    gpExpression.setLeftExpression(groupExpression);
                    gpExpression.setRightExpression(nodeExpression);
                    gpExpression.setOperator(logicalGp);
                    groupExpression = gpExpression;

                } else {
                    /*
                     * Iterate SearchAttributeBeanList and
                     * match the id's whose values are there
                     * in list and make where clause 
                     */
                    String configFileOperator = StringUtils.substringAfter(node.getFirstChild().getNextSibling()
                            .getNodeName(), "-");
                    String searchAttributeId = "";
                    if (configFileOperator.equals(BETWEEN)) {
                        searchAttributeId = "From_"
                                + searchAttributeNodeMap.getNamedItem(SEARCH_ATTRIBUTE_ID).getNodeValue();
                    } else {
                        searchAttributeId = searchAttributeNodeMap.getNamedItem(SEARCH_ATTRIBUTE_ID).getNodeValue();
                    }

                    Iterator<SearchAttributeBean> itr = searchAttributeBeanList.iterator();
                    while (itr.hasNext()) {
                        SearchAttributeBean searchAttributeBean = new SearchAttributeBean();
                        searchAttributeBean = itr.next();
                        if ((searchAttributeId.equals(searchAttributeBean.getId()))) {
                            if (configFileOperator.equals(BETWEEN)) {
                                for (int k = 0 ; k < 2 ; k++) {
                                    if (searchAttributeBean.getValue() == null || searchAttributeBean.getValue().equals("")) {
                                        if (itr.hasNext()) {
                                            searchAttributeBean = itr.next();
                                        }
                                        continue;
                                    }
                                    String operator = "";
                                    if (k == 0) {
                                        operator = ">=";
                                    } else {
                                        operator = "<=";
                                    }

                                    int dataType = getType(StringUtils.substringBefore(node.getFirstChild().getNextSibling()
                                            .getNodeName(), "-"));

                                    ObjectGraphSearchAttribute objectGraphSearchAttribute = getObjectGraphSearchAttribute(
                                            ognlValue, dataType);

                                    ConstantSearchAttribute constantSearchAttribute = getConstantSearchAttributeForBetween(
                                            searchAttributeBean.getValue(), searchAttributeBean.getType(), dataType, k);

                                    SearchAttributeExpression leftExpression = new SearchAttributeExpression();
                                    leftExpression.setSearchAttribute(objectGraphSearchAttribute);

                                    SearchAttributeExpression rightExpression = new SearchAttributeExpression();
                                    rightExpression.setSearchAttribute(constantSearchAttribute);

                                    SearchAttributeExpression nodeExpression = new SearchAttributeExpression();
                                    nodeExpression.setLeftExpression(leftExpression);
                                    nodeExpression.setRightExpression(rightExpression);
                                    nodeExpression.setOperator(operator);

                                    if (groupExpression == null) {
                                        groupExpression = nodeExpression;
                                        if (itr.hasNext()) {
                                            searchAttributeBean = itr.next();
                                        }
                                        continue;
                                    }
                                    SearchAttributeExpression gpExpression = new SearchAttributeExpression();
                                    gpExpression.setLeftExpression(groupExpression);
                                    gpExpression.setRightExpression(nodeExpression);
                                    gpExpression.setOperator(logicalGp);
                                    groupExpression = gpExpression;
                                    if (k == 0) {
                                        if (itr.hasNext()) {
                                            searchAttributeBean = itr.next();
                                        }
                                    }
                                }
                            } else {
                                if (searchAttributeBean.getValue() != null && (!searchAttributeBean.getValue().equals(""))) {
                                    int dataType = getType(StringUtils.substringBefore(node.getFirstChild().getNextSibling()
                                            .getNodeName(), "-"));
                                    String operator = getOperator(StringUtils.substringAfter(node.getFirstChild()
                                            .getNextSibling().getNodeName(), "-"));
                                    ObjectGraphSearchAttribute objectGraphSearchAttribute = getObjectGraphSearchAttribute(
                                            ognlValue, dataType);

                                    ConstantSearchAttribute constantSearchAttribute = getConstantSearchAttribute(
                                            searchAttributeBean.getValue(), searchAttributeBean.getType(),
                                            searchAttributeBean.getOperator(), dataType);

                                    SearchAttributeExpression leftExpression = new SearchAttributeExpression();
                                    leftExpression.setSearchAttribute(objectGraphSearchAttribute);

                                    SearchAttributeExpression rightExpression = new SearchAttributeExpression();
                                    rightExpression.setSearchAttribute(constantSearchAttribute);

                                    SearchAttributeExpression nodeExpression = new SearchAttributeExpression();
                                    nodeExpression.setLeftExpression(leftExpression);
                                    nodeExpression.setRightExpression(rightExpression);
                                    nodeExpression.setOperator(operator);

                                    if (groupExpression == null) {
                                        groupExpression = nodeExpression;
                                        continue;
                                    }
                                    SearchAttributeExpression gpExpression = new SearchAttributeExpression();
                                    gpExpression.setLeftExpression(groupExpression);
                                    gpExpression.setRightExpression(nodeExpression);
                                    gpExpression.setOperator(logicalGp);
                                    groupExpression = gpExpression;
                                }
                            }
                        }
                    }
                }
            }
        }
        return groupExpression;
    }

    /**
     * Get the ObjectGraphSearchAttribute object
     * @param ognlValue
     * @param dataType
     * @return
     */
    private ObjectGraphSearchAttribute getObjectGraphSearchAttribute(String ognlValue, int dataType) {
        ObjectGraphSearchAttribute objectGraphSearchAttribute = new ObjectGraphSearchAttribute();
        objectGraphSearchAttribute.setObjectGraph(ognlValue);
        objectGraphSearchAttribute.setDataType(dataType);
        objectGraphSearchAttribute.setName("");
        objectGraphSearchAttribute.setDescription("");
        objectGraphSearchAttribute.setPersistenceStatus(0);
        return objectGraphSearchAttribute;
    }

    /**
     * Get the ConstantSearchAttribute object
     * @param value
     * @param type
     * @param operator
     * @param dataType
     * @return
     */
    private ConstantSearchAttribute getConstantSearchAttribute(String value, String type, String operator, int dataType) {
        ConstantSearchAttribute constantSearchAttribute = new ConstantSearchAttribute();
        String data = "";
        if ("like".equalsIgnoreCase(operator)) {
            data = (value + "%");
        } else {
            if (type.equals(D_VALUE)) {
                if (operator.equals(">=")) {
                    data = value + " 00:00:00";
                }
                if (operator.equals("<=")) {
                    data = value + " 23:59:59";
                }
            } else {
                data = (value);
            }
        }

        constantSearchAttribute.setLiteral(data);
        constantSearchAttribute.setDataType(dataType);
        constantSearchAttribute.setName("");
        constantSearchAttribute.setDescription("");
        constantSearchAttribute.setPersistenceStatus(0);
        return constantSearchAttribute;
    }

    /**
     * Get the ConstantSearchAttribute object
     * for "between" search case
     * @param value
     * @param type
     * @param dataType
     * @param k
     * @return
     */
    private ConstantSearchAttribute getConstantSearchAttributeForBetween(String value, String type, int dataType, int k) {
        ConstantSearchAttribute constantSearchAttribute = new ConstantSearchAttribute();
        String data = "";
        if (k == 0) {
            if (type.equals(D_VALUE)) {
                data = value + " 00:00:00";
            } else {
                data = value;
            }

        } else {
            if (type.equals(D_VALUE)) {
                data = value + " 23:59:59";
            } else {
                data = value;
            }
        }

        constantSearchAttribute.setLiteral(data);
        constantSearchAttribute.setDataType(dataType);
        constantSearchAttribute.setName("");
        constantSearchAttribute.setDescription("");
        constantSearchAttribute.setPersistenceStatus(0);
        return constantSearchAttribute;
    }

    /**
     * Get Operator Symbol from XML
     * @param operatorName
     * @return operatorSymbol
     */
    private String getOperator(String operatorName) {
        String operatorSymbol = "";
        if (operatorName.equals(EQUAL)) {
            operatorSymbol = "=";
        }
        if (operatorName.equals(IN)) {
            operatorSymbol = "in";
        }
        if (operatorName.equals(CONTAIN)) {
            operatorSymbol = "like";
        }
        if (operatorName.equals(GREATER_THEN)) {
            operatorSymbol = ">";
        }
        if (operatorName.equals(LESS_THEN)) {
            operatorSymbol = "<";
        }
        if (operatorName.equals(GREATER_THEN_EQUAL_TO)) {
            operatorSymbol = ">=";
        }
        if (operatorName.equals(LESS_THEN_EQUAL_TO)) {
            operatorSymbol = "<=";
        }
        if (operatorName.equals(BETWEEN)) {
            operatorSymbol = "><";
        }
        if (operatorName.equals(AND)) {
            operatorSymbol = "AND";
        }
        if (operatorName.equals(OR)) {
            operatorSymbol = "OR";
        }
        if (operatorName.equals(ISNULL)) {
            operatorSymbol = "isNull";
        }
        return operatorSymbol;
    }

    /**
     * Get Data Type From XML
     * @param type
     * @return dataType
     */
    private int getType(String type) {
        int dataType = 0;
        if (type.equalsIgnoreCase(S_VALUE)) {
            dataType = 1;
        }
        if (type.equalsIgnoreCase(I_VALUE)) {
            dataType = 2;
        }
        if (type.equalsIgnoreCase(L_VALUE)) {
            dataType = 3;
        }
        if (type.equalsIgnoreCase(N_VALUE)) {
            dataType = 4;
        }
        if (type.equalsIgnoreCase(B_VALUE)) {
            dataType = 5;
        }
        if (type.equalsIgnoreCase(D_VALUE)) {
            dataType = 6;
        }
        if (type.equalsIgnoreCase(R_VALUE)) {
            dataType = 8;
        }
        return dataType;
    }

}
