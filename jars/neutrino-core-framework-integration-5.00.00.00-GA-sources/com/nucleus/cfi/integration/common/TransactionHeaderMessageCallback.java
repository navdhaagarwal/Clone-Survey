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

import java.io.IOException;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.util.StringUtils;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.client.core.WebServiceMessageCallback;
import org.springframework.ws.soap.SoapHeader;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.xml.transform.StringSource;

/**
 * @author Nucleus Software Exports Limited
 *
 * This class is used to add transaction id as a soap message header.This id  is propagated to remote servers
 * and used to track/debug user requests in remote and local systems.
 *
 */
public class TransactionHeaderMessageCallback implements WebServiceMessageCallback {

	private final static Logger LOGGER                = LoggerFactory.getLogger(TransactionHeaderMessageCallback.class);

	private String              txnHeaderNamespaceURI;

	private String              transactionHeaderName = "transactionId";

	private String              mdcContextKey         = "UUID";

	private static TransformerFactory transformerFactory;

	static {
		try {
			transformerFactory = TransformerFactory.newInstance();
		} catch (TransformerFactoryConfigurationError tfce) {
			LOGGER.error("TransactionFactory could not be initialized due to: " + tfce.getMessage(), tfce);
		}
	}

	public TransactionHeaderMessageCallback(String txnHeaderNamespaceURI) {
		super();
		this.txnHeaderNamespaceURI = txnHeaderNamespaceURI;
	}

	@Override
	public void doWithMessage(WebServiceMessage message) throws IOException, TransformerException {

		if (SoapMessage.class.isAssignableFrom(message.getClass()) && StringUtils.hasText(txnHeaderNamespaceURI)) {

			String transactionId = StringUtils.hasText(MDC.get(mdcContextKey)) ? MDC.get(mdcContextKey)
					: "CASTXN-ID_NOT_AVAILABLE";
			addTransactionIdHeader((SoapMessage) message, transactionId);
		}
	}

	private void addTransactionIdHeader(SoapMessage soapMessage, String transactionId2)
			throws TransformerFactoryConfigurationError, TransformerException {

		try {
			String headerString = "<txnidprefix:" + transactionHeaderName + " xmlns:txnidprefix=" + "\""
					+ txnHeaderNamespaceURI + "\" " + ">" + transactionId2 + "</txnidprefix:" + transactionHeaderName + ">";
			SoapHeader soapHeader = soapMessage.getSoapHeader();
			StringSource headerSource = new StringSource(headerString);
			Transformer transformer = transformerFactory.newTransformer();
			transformer.transform(headerSource, soapHeader.getResult());
			LOGGER.debug("Added transactionId header with namespace :{} in WS message", txnHeaderNamespaceURI);
		} catch (Exception exception) {
			LOGGER.error("Unable to set transactionId header in WS message", exception);
		}

	}

}
