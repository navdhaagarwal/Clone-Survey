package com.nucleus.ws.core.inbound.config.interceptor;

import java.io.StringWriter;

import javax.servlet.http.HttpServletRequest;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.util.StringUtils;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.soap.SoapMessage;

import com.nucleus.core.exceptions.SystemException;
import com.nucleus.entity.EntityLifeCycleData;
import com.nucleus.ws.core.inbound.config.InBoundServiceInfoPojo;
import com.nucleus.ws.core.inbound.config.SpringWsUtils;
import com.nucleus.ws.core.inbound.config.msg.IntegrationMessageExchange;
import com.nucleus.ws.core.inbound.config.service.IntegrationConfigurationService;

/**
 * Class for <code>EndpointInterceptor</code> instances that log soap message from
 * <code>WebServiceMessage</code>.
 *
 */

public class NeutrinoWebServiceLoggingInterceptor extends NeutrinoAbstractInterceptor {

    /**
     * The default <code>Log</code> instance used to write trace messages. This instance is mapped to the implementing
     * <code>Class</code>.
     */
    protected transient Logger              logger      = LoggerFactory
                                                                .getLogger(NeutrinoWebServiceLoggingInterceptor.class);

    private boolean                         logRequest  = true;

    private boolean                         logResponse = true;

    private boolean                         logFault    = true;

    private IntegrationConfigurationService integrationConfigurationService;

    public void setIntegrationConfigurationService(IntegrationConfigurationService integrationConfigurationService) {
        this.integrationConfigurationService = integrationConfigurationService;
    }

    /** Indicates whether a SOAP Fault should be logged. Default is <code>true</code>. */
    public void setLogFault(boolean logFault) {
        this.logFault = logFault;
    }

    /** Indicates whether the request should be logged. Default is <code>true</code>. */
    public final void setLogRequest(boolean logRequest) {
        this.logRequest = logRequest;
    }

    /** Indicates whether the response should be logged. Default is <code>true</code>. */
    public final void setLogResponse(boolean logResponse) {
        this.logResponse = logResponse;
    }

    /**
     * Logs the request message payload. Logging only occurs if <code>logRequest</code> is set to <code>true</code>,
     * which is the default.
     *
     * @param messageContext the message context
     * @return <code>true</code>
     * @throws TransformerException when the payload cannot be transformed to a string
     */
    public final boolean handleRequest(MessageContext messageContext, Object endpoint) throws TransformerException {
        String serviceId = integrationConfigurationService.getServiceId(endpoint);
        InBoundServiceInfoPojo infoPojo = integrationConfigurationService.getInboundServiceConfig(serviceId);
        messageContext.setProperty(CURRENT_SERVICE_INFO_POJO, infoPojo);
        messageContext.setProperty(CURRENT_SERVICE_INFO_POJO, infoPojo);

        int contentLength = SpringWsUtils.getCurrentHttpServletRequest().getContentLength();
        if (infoPojo.getMaxPayloadSizeAllowedInBytes() != null && infoPojo.getMaxPayloadSizeAllowedInBytes() < contentLength) {
            throw new SystemException("Payload size exceeded the specified limit of "
                    + infoPojo.getMaxPayloadSizeAllowedInBytes() + " bytes");
        }

        try {
            if (logRequest) {
                logger.info("Request:{} ", getMessageAsString(messageContext.getRequest()));
            }
        } finally {
            // in any case we have to complete handleRequest successfully to call afterCompletion.
        }
        return true;

    }

    /**
     * Logs the response message payload. Logging only occurs if <code>logResponse</code> is set to <code>true</code>,
     * which is the default.
     *
     * @param messageContext the message context
     * @return <code>true</code>
     * @throws TransformerException when the payload cannot be transformed to a string
     */
    public boolean handleResponse(MessageContext messageContext, Object endpoint) throws Exception {
        if (logResponse) {
            logger.info("Response:{} ", getMessageAsString(messageContext.getResponse()));
        }
        return true;
    }

    /** Faults are logged. */
    public boolean handleFault(MessageContext messageContext, Object endpoint) throws Exception {
        if (logFault) {
            logger.info("Fault: {}", getMessageAsString(messageContext.getResponse()));
        }
        messageContext.setProperty(IS_FAULT, true);
        return true;
    }

    /** Save request as per configuration*/
    public void afterCompletion(MessageContext messageContext, Object endpoint, Exception ex) {
        try {
            String serviceId = integrationConfigurationService.getServiceId(endpoint);
            InBoundServiceInfoPojo infoPojo = (InBoundServiceInfoPojo) messageContext.getProperty(CURRENT_SERVICE_INFO_POJO);

            IntegrationMessageExchange messageExchange = null;
            boolean fault = messageContext.getProperty(IS_FAULT) == null ? false : true;
            if (infoPojo.getSaveRequestAlways() || (fault && infoPojo.getSaveRequestResponseOnError())) {

                messageExchange = prepareMessage(messageContext, endpoint, !fault, serviceId);
                String messageXml = getMessageAsString(messageContext.getRequest());
                messageExchange.setRequestMessage(messageXml.getBytes());
            }
            if (infoPojo.getSaveResponseOrFaultAlways() || (fault && infoPojo.getSaveRequestResponseOnError())) {
                messageExchange = messageExchange == null ? prepareMessage(messageContext, endpoint, !fault, serviceId)
                        : messageExchange;
                String messageXml = getMessageAsString(messageContext.getResponse());
                messageExchange.setResponseMessage(messageXml.getBytes());
            }

            if (messageExchange != null) {
                // finally save message in any case.
                integrationConfigurationService.saveMessage(messageExchange);
            }
        } catch (Exception e) {
            logger.error("Error in saving IntegrationMessageExchange.", e);
        }

    }

    private IntegrationMessageExchange prepareMessage(MessageContext messageContext, Object endpoint, boolean success,
            String serviceId) {

        String associatedSystemUserId = (String) messageContext.getProperty(CURRENT_REQUEST_SYSTEM_USER_ID);
        IntegrationMessageExchange messageExchange = new IntegrationMessageExchange();
        messageExchange.setEntityLifeCycleData(new EntityLifeCycleData());
        messageExchange.getEntityLifeCycleData().setCreationTimeStamp(DateTime.now(DateTimeZone.UTC));
        messageExchange.setRequestTimestamp(DateTime.now(DateTimeZone.UTC));
        messageExchange.setServiceId(serviceId);
        // in-bound
        messageExchange.setServiceType(1);
        // soap-xml
        messageExchange.setMessageType(1);
        messageExchange.setAssociatedSystemUserId(associatedSystemUserId);
        HttpServletRequest request = SpringWsUtils.getCurrentHttpServletRequest();
        messageExchange.setSourceIp(request.getRemoteAddr());
        // setting the host name (not ip)
        messageExchange.setTargetIp(request.getServerName());
        String transactionId = StringUtils.hasText(MDC.get(mdcContextKey)) ? MDC.get(mdcContextKey) : "TXN-ID_NOT_AVAILABLE";
        messageExchange.setTransactionId(transactionId);
        messageExchange.setSuccess(success);
        return messageExchange;
    }

    private Transformer createNonIndentingTransformer() throws TransformerConfigurationException {
        Transformer transformer = createTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        return transformer;
    }

    /**
     * Logs the given {@link Source source} to the {@link #logger}, using the message as a prefix.
     * <p/>
     * By default, this message creates a string representation of the given source, and delegates to {@link
     * #logMessage(String)}.
     *
     * @param logMessage the log message
     * @param source     the source to be logged
     * @throws TransformerException in case of errors
     */
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
                logger.error("Error in extracting integration message", e);
            } catch (TransformerException e) {
                logger.error("Error in saving integration message", e);
            }
        }
        return null;
    }

    /**
     * Logs the given string message.
     * <p/>
     * By default, this method uses a "debug" level of logging. Subclasses can override this method to change the level
     * of logging used by the logger.
     *
     * @param message the message
     */
    protected void logMessage(String message) {
        logger.info(message);
    }

    /**
     * Method that returns the <code>Source</code> for the given <code>WebServiceMessage</code>.
     *
     * @param message the message
     * @return the source of the message
     */
    protected Source getSource(WebServiceMessage message) {
        if (message instanceof SoapMessage) {
            SoapMessage soapMessage = (SoapMessage) message;
            return soapMessage.getEnvelope().getSource();
        } else {
            return null;
        }

    }

}
