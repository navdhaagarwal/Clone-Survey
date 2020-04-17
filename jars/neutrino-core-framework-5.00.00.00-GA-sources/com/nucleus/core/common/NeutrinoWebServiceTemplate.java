package com.nucleus.core.common;

import java.io.IOException;
import java.net.URI;

import javax.xml.transform.TransformerException;

import org.springframework.oxm.Marshaller;
import org.springframework.oxm.Unmarshaller;
import org.springframework.util.Assert;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.client.WebServiceIOException;
import org.springframework.ws.client.WebServiceTransportException;
import org.springframework.ws.client.core.WebServiceMessageCallback;
import org.springframework.ws.client.core.WebServiceMessageExtractor;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.context.DefaultMessageContext;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.support.MarshallingUtils;
import org.springframework.ws.transport.HeadersAwareSenderWebServiceConnection;
import org.springframework.ws.transport.TransportException;
import org.springframework.ws.transport.WebServiceConnection;
import org.springframework.ws.transport.context.DefaultTransportContext;
import org.springframework.ws.transport.context.TransportContext;
import org.springframework.ws.transport.context.TransportContextHolder;
import org.springframework.ws.transport.http.HttpServletConnection;
import org.springframework.ws.transport.support.TransportUtils;

public class NeutrinoWebServiceTemplate extends WebServiceTemplate{
	
	
	public Object marshalSendAndReceive(String uri,
										final Object requestPayload,
										final WebServiceMessageCallback requestCallback,NeutrinoHttpHeaders neutrinoHeaders) {
		return sendAndReceive(uri, new WebServiceMessageCallback() {

			public void doWithMessage(WebServiceMessage request) throws IOException, TransformerException {
				if (requestPayload != null) {
					Marshaller marshaller = getMarshaller();
					if (marshaller == null) {
						throw new IllegalStateException(
								"No marshaller registered. Check configuration of WebServiceTemplate.");
					}
					MarshallingUtils.marshal(marshaller, requestPayload, request);
					if (requestCallback != null) {
						requestCallback.doWithMessage(request);
					}
				}
			}
		}, new WebServiceMessageExtractor<Object>() {

			public Object extractData(WebServiceMessage response) throws IOException {
				Unmarshaller unmarshaller = getUnmarshaller();
				if (unmarshaller == null) {
					throw new IllegalStateException(
							"No unmarshaller registered. Check configuration of WebServiceTemplate.");
				}
				return MarshallingUtils.unmarshal(unmarshaller, response);
			}
		},neutrinoHeaders);
	}
	
	
	public <T> T sendAndReceive(String uriString,
								 WebServiceMessageCallback requestCallback,
								 WebServiceMessageExtractor<T> responseExtractor,NeutrinoHttpHeaders neutrinoHeaders) {
		Assert.notNull(responseExtractor, "'responseExtractor' must not be null");
		Assert.hasLength(uriString, "'uri' must not be empty");
		TransportContext previousTransportContext = TransportContextHolder.getTransportContext();

		WebServiceConnection prevConection = null;
		WebServiceConnection connection = null;
		if (previousTransportContext != null) {
			prevConection = previousTransportContext.getConnection();
		}
		try {
			connection = createConnection(URI.create(uriString));
			TransportContextHolder.setTransportContext(new DefaultTransportContext(connection));
			MessageContext messageContext = new DefaultMessageContext(getMessageFactory());
			if (prevConection != null && prevConection instanceof HttpServletConnection) {

				/*String payloadHeader = ((HttpServletConnection) prevConection).getHttpServletRequest()
						.getHeader("payload");
				if (payloadHeader != null) {
					((HeadersAwareSenderWebServiceConnection) connection).addRequestHeader("payload", payloadHeader);
				}*/
				HeadersAwareSenderWebServiceConnection headersAwareSenderWebServiceConnection = (HeadersAwareSenderWebServiceConnection) connection;
				neutrinoHeaders.entrySet().forEach(header -> {
					try {
						headersAwareSenderWebServiceConnection.addRequestHeader(header.getKey(), header.getValue().get(0));
					} catch (IOException e) {
						e.printStackTrace();
					}
				});
				
			}
			return doSendAndReceive(messageContext, connection, requestCallback, responseExtractor);
		} catch (TransportException ex) {
			throw new WebServiceTransportException("Could not use transport: " + ex.getMessage(), ex);
		} catch (IOException ex) {
			throw new WebServiceIOException("I/O error: " + ex.getMessage(), ex);
		} finally {
			TransportUtils.closeConnection(connection);
			TransportContextHolder.setTransportContext(previousTransportContext);
		}
	}
	
	

}
