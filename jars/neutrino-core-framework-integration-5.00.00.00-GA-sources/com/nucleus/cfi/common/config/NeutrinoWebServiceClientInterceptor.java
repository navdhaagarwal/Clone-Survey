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
package com.nucleus.cfi.common.config;

import java.io.StringWriter;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;

import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.client.WebServiceClientException;
import org.springframework.ws.client.support.interceptor.ClientInterceptor;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.xml.transform.TransformerObjectSupport;

import com.nucleus.logging.BaseLoggers;

/**
 * @author Nucleus Software Exports Limited
 */
public class NeutrinoWebServiceClientInterceptor extends TransformerObjectSupport implements ClientInterceptor {

    @Override
    public boolean handleRequest(MessageContext messageContext) throws WebServiceClientException {
        BaseLoggers.integrationLogger.info(getMessageAsString(messageContext.getRequest()));
        return true;
    }

    @Override
    public boolean handleResponse(MessageContext messageContext) throws WebServiceClientException {
        return true;
    }

    @Override
    public boolean handleFault(MessageContext messageContext) throws WebServiceClientException {
        return true;
    }
       
    @Override
    public void afterCompletion(MessageContext paramMessageContext, Exception exception) throws WebServiceClientException{
    	BaseLoggers.integrationLogger.info("Inside afterCompletion method");     
    }

    protected String getMessageAsString(WebServiceMessage message) {

        Source source = null;
        if (message instanceof SoapMessage) {
            SoapMessage soapMessage = (SoapMessage) message;
            source = soapMessage.getEnvelope().getSource();
        }
        if (source != null) {
            Transformer transformer;
            try {
                transformer = createNonIndentingTransformer();
                StringWriter writer = new StringWriter();
                transformer.transform(source, new StreamResult(writer));
                return writer.toString();
            } catch (TransformerConfigurationException e) {
            	BaseLoggers.exceptionLogger.error("Error in extracting integration message", e);
            } catch (TransformerException e) {
                BaseLoggers.exceptionLogger.error("Error in saving integration message", e);
            }
        }
        return null;
    }

    private Transformer createNonIndentingTransformer() throws TransformerConfigurationException {
        Transformer transformer = createTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        return transformer;
    }



}
