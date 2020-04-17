/**
 * This file and a proportion of its content is copyright of Nucleus Software Exports Limited - © 2012. All rights reserved.
 * Any redistribution or reproduction of part or all of the contents in any form is prohibited other than the following:
 * - you cannot print or download to a local hard disk extract contents either part or full for personal/ commercial/
 * academic or any other use
 * - you may not copy the content to individual/ third parties for any type of use, either as compiled or source format
 * without the knowledge and consent of Nucleus Software
 * - You may not, except with our express written permission, distribute or commercially exploit the content. Nor may you
 * transmit it or store it in any other web site or other form of electronic retrieval system.
 */
package com.nucleus.web.search;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.io.IOUtils;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.nucleus.core.searchframework.entity.ConstantSearchAttribute;
import com.nucleus.core.searchframework.entity.ObjectGraphSearchAttribute;
import com.nucleus.core.searchframework.entity.SearchAttributeExpression;
import com.nucleus.core.searchframework.entity.SearchCriteriaClause;
import com.nucleus.core.searchframework.entity.SearchRequest;
import com.nucleus.core.searchframework.service.SearchException;
import com.nucleus.core.searchframework.service.SearchFrameworkService;
import com.nucleus.core.xml.util.XmlUtils;

/**
 * @author Nucleus Software Exports Limited
 */
@Controller
@RequestMapping(value = "/quickSearchFramework")
public class QuickSearchController {

    @Inject
    @Named("searchFrameworkService")
    protected SearchFrameworkService searchFrameworkService;

    @RequestMapping(value = "/quickSearch/{value}")
    public String quickSearch(@PathVariable("value") String value, @RequestParam(required = false) String id, ModelMap map) {
        try {
            List data = new ArrayList();
            List<String> selectFieldList = new ArrayList<String>();

            InputStream inputStream = new PathMatchingResourcePatternResolver().getResource("classpath:SearchConfig.xml")
                    .getInputStream();
            // id = "customer";
            if (inputStream != null) {
                String input = IOUtils.toString(inputStream);
                /*
                 * Load Document From XML
                 */
                Document doc = XmlUtils.readFromXml(input);
                doc.getDocumentElement().normalize();
                NodeList listOfSearchConfigurations = doc.getElementsByTagName("searchConfigurations");
                Node rootNode = listOfSearchConfigurations.item(0);

                /*
                 * Get list of SearchRequest tag from xml
                 */
                NodeList listOfSearchConfig = ((Element) rootNode).getElementsByTagName("searchConfig");

                // Read The Search Type from xml file
                NodeList searchTypeNodeList = ((Element) rootNode).getElementsByTagName("searchType");
                String searchType = (((Element) searchTypeNodeList.item(0)).getAttributes().getNamedItem("value")
                        .getNodeValue());

                for (int j = 0 ; j < listOfSearchConfig.getLength() ; j++) {
                    Node firstNode = listOfSearchConfig.item(j);

                    /*
                     * Load Entity Id of SearchRequest
                     */
                    String xmlConfigId = ((Element) firstNode).getAttributes().getNamedItem("id").getNodeValue();

                    /*
                     * Check Entity Id From Coming From UI matches Config File ID
                     */
                    if (xmlConfigId.equalsIgnoreCase(id)) {
                        String className = ((Element) firstNode).getAttributes().getNamedItem("entityClass").getNodeValue();

                        NodeList selectField = ((Element) firstNode).getElementsByTagName("selectField");
                        Node selectFieldNode = selectField.item(0);
                        if (((Element) selectFieldNode).getElementsByTagName("field").getLength() > 0) {
                            NodeList field = ((Element) selectFieldNode).getElementsByTagName("field");
                            /*
                             * get Field List To Display
                             */
                            for (int k = 0 ; k < field.getLength() ; k++) {
                                if (((field.item(k)).getFirstChild()).getTextContent() != null) {
                                    selectFieldList.add(((field.item(k)).getFirstChild()).getTextContent());
                                }
                            }
                        }

                        ObjectGraphSearchAttribute objectGraphSearchAttribute = new ObjectGraphSearchAttribute();
                        objectGraphSearchAttribute.setObjectGraph("id");
                        objectGraphSearchAttribute.setDataType(1);
                        objectGraphSearchAttribute.setPersistenceStatus(0);

                        ConstantSearchAttribute constantSearchAttribute = new ConstantSearchAttribute();
                        constantSearchAttribute.setLiteral(value);
                        constantSearchAttribute.setDataType(3);
                        constantSearchAttribute.setPersistenceStatus(0);

                        SearchAttributeExpression leftExpression = new SearchAttributeExpression();
                        leftExpression.setSearchAttribute(objectGraphSearchAttribute);

                        SearchAttributeExpression rightExpression = new SearchAttributeExpression();
                        rightExpression.setSearchAttribute(constantSearchAttribute);

                        SearchAttributeExpression nodeExpression = new SearchAttributeExpression();
                        nodeExpression.setLeftExpression(leftExpression);
                        nodeExpression.setRightExpression(rightExpression);
                        nodeExpression.setOperator("=");

                        SearchCriteriaClause searchCriteriaClause = new SearchCriteriaClause();
                        searchCriteriaClause.setSearchAttributeExpression(nodeExpression);

                        SearchRequest searchRequest = new SearchRequest();
                        searchRequest.setWhereClause(searchCriteriaClause);
                        searchRequest.setSearchOn(className);
                        searchRequest.setFieldList(selectFieldList);
                        searchRequest.setSearchType(searchType);

                        data = searchFrameworkService.executeSearchRequest(searchRequest);
                    }
                }
            }
            map.put("quickSearchResultList", data);
            map.put("quickSearchSelectFieldList", selectFieldList);
            map.put("size", data.size());
            /* if(data.size()==1){
                 if(id.equalsIgnoreCase("schemes")){
                     return "redirect:/app/LoanScheme/view/"+value;    
                 }
                 
             }*/
            return "quickSearchResult";
        } catch (IOException io) {
            throw new SearchException("Error occured while loading config file SearchConfig.xml", io);
        }
    }

}
