package com.nucleus.cfi.integration.common;

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
        if (BaseLoggers.integrationLogger.isDebugEnabled()) {
            BaseLoggers.integrationLogger.debug(getMessageAsString(messageContext.getRequest()));
        }
        return true;
    }

    @Override
    public boolean handleResponse(MessageContext messageContext) throws WebServiceClientException {
        if (BaseLoggers.integrationLogger.isDebugEnabled()) {
            BaseLoggers.integrationLogger.debug(getMessageAsString(messageContext.getResponse()));
        }
        return true;
    }

    @Override
    public boolean handleFault(MessageContext messageContext) throws WebServiceClientException {
        if (BaseLoggers.integrationLogger.isDebugEnabled()) {
            BaseLoggers.integrationLogger.debug(getMessageAsString(messageContext.getResponse()));
        }
        return true;
    }
    
    @Override
    public void afterCompletion(MessageContext paramMessageContext, Exception exception) throws WebServiceClientException{
    	BaseLoggers.integrationLogger.info("Inside afterCompletion method");     
    }

    protected String getMessageAsString(WebServiceMessage message) {

        try {
            Source source = null;
            if (message instanceof SoapMessage) {
                SoapMessage soapMessage = (SoapMessage) message;
                if (soapMessage != null && soapMessage.getEnvelope() != null) {
                    source = soapMessage.getEnvelope().getSource();
                }
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

        } catch (Exception e) {
            return "Error in logging web service soap message[WebServiceMessage to string conversion error]";
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