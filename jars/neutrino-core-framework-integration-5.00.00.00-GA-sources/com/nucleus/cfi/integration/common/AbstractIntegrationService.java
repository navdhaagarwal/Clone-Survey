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
package com.nucleus.cfi.integration.common;

import javax.inject.Inject;
import javax.inject.Named;
import javax.xml.bind.JAXBIntrospector;
import javax.xml.namespace.QName;

import org.slf4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.ws.client.core.WebServiceTemplate;

import com.nucleus.logging.BaseLoggers;

/**
 * @author Nucleus Software Exports Limited
 *
 */
public abstract class AbstractIntegrationService implements InitializingBean {

    protected final Logger LOGGER;

    public AbstractIntegrationService() {
        LOGGER = BaseLoggers.integrationLogger;
    }

    protected String             webServiceUrl;
    
    protected String             remoteSystemwebServiceUrl;

    private JAXBIntrospector     jaxbIntrospector;

    @Inject
    @Named("finnOneCoreFrameworkCommonWSClientTemplate")
    protected WebServiceTemplate webServiceTemplate;

    public abstract void setWebServiceUrl(String webServiceUrl);
    
    public abstract void setRemoteSystemWebServiceUrl(String remoteSystemwebServiceUrl);

    @Override
    public void afterPropertiesSet() throws Exception {

        if (Jaxb2Marshaller.class.isAssignableFrom(webServiceTemplate.getMarshaller().getClass())) {
            Jaxb2Marshaller jaxb2Marshaller = (Jaxb2Marshaller) webServiceTemplate.getMarshaller();

            jaxbIntrospector = jaxb2Marshaller.getJaxbContext().createJAXBIntrospector();

        } else {
            LOGGER.warn("Can not create jaxbIntrospector from given webServiceTemplate");
        }

    }

    protected String getNamespaceURIForJaxbObject(Object object) {

        if (jaxbIntrospector != null) {
            QName qName = jaxbIntrospector.getElementName(object);
            return (qName != null ? qName.getNamespaceURI() : null);
        }
        return null;

    }

}
