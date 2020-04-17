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
package com.nucleus.web.master;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Locale;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.springframework.context.MessageSource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.support.RequestContextUtils;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * @author Nucleus Software Exports Limited
 * 
 */
@Transactional
@Named("masterXMLDocumentBuilder")
public class CommonFileIOMasterGridLoad {

    @Inject
    @Named("messageSource")
    protected MessageSource           messageSource;

    private HashMap<String, Document> _documentMap = new HashMap<String, Document>();

    public Document getMasterXMLDocumentBuilder(String xmlFileName) throws ParserConfigurationException, SAXException,
            IOException {

        if (!_documentMap.containsKey(xmlFileName)) {
            InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(xmlFileName);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(is);
            doc.getDocumentElement().normalize();
            _documentMap.put(xmlFileName, doc);
        }
        return _documentMap.get(xmlFileName);
    }

    public String getResourceBundleFileReader(HttpServletRequest request, String key) throws IOException {
        Locale loc = RequestContextUtils.getLocale(request);
        String result = messageSource.getMessage(key, null, loc);
        return result;

    }
}
