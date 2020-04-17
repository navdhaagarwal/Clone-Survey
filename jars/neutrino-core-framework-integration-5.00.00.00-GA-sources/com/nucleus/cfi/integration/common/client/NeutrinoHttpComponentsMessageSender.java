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
package com.nucleus.cfi.integration.common.client;

import java.io.IOException;

import javax.inject.Inject;

import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.GzipCompressingEntity;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.springframework.ws.transport.http.HttpComponentsMessageSender;

import com.nucleus.logging.BaseLoggers;

/**
 * @author Nucleus Software Exports Limited
 */

public class NeutrinoHttpComponentsMessageSender extends HttpComponentsMessageSender {

    private static final int DEFAULT_REQUEST_COMPRESSION_THRESHOLD   = (10 * 1024);                            // 10 kb

    @Inject
    private HttpClient httpClient;
    
    private boolean          compressRequestToGzip;
    /**
     *sets the size of the smallest request that will be compressed, in bytes. That is,
     * if less than requestCompressionThreshold bytes are written to the request, it will not be compressed 
     * and the request will go to the server unmodified.
     */
    private long             requestCompressionThreshold             = DEFAULT_REQUEST_COMPRESSION_THRESHOLD;
    @Deprecated
    private int              readTimeout;
    @Deprecated
    private int              connectionTimeout;

    public NeutrinoHttpComponentsMessageSender() {

    }

    /**
     * @author Nucleus Software Exports Limited
     * a HttpRequestInterceptor to gzip compress http request.
     */
    private class GzipCompressSoapRequestInterceptor implements HttpRequestInterceptor {

        public void process(HttpRequest request, HttpContext context) throws HttpException, IOException {
            if (compressRequestToGzip) {
                BaseLoggers.integrationLogger.debug("GZIP compression is on for soap requests.");
                if (request instanceof HttpEntityEnclosingRequest) {
                    HttpEntityEnclosingRequest enclosingRequest = (HttpEntityEnclosingRequest) request;
                    HttpEntity httpEntity = enclosingRequest.getEntity();
                    if (httpEntity.getContentLength() >= requestCompressionThreshold) {
                        BaseLoggers.integrationLogger
                                .debug("GZIP compression requestCompressionThreshold ({} bytes) is reached so compressing request to gzip.",
                                        requestCompressionThreshold);
                        enclosingRequest.setHeader(HTTP.CONTENT_ENCODING, "gzip");
                        enclosingRequest.setEntity(new GzipCompressingEntity(httpEntity));
                    } else {
                        BaseLoggers.integrationLogger
                                .debug("GZIP compression requestCompressionThreshold ({} bytes) is not reached so not compressing request to gzip.",
                                        requestCompressionThreshold);
                    }
                }
            } else {
                BaseLoggers.integrationLogger.debug("GZIP compression is off for soap requests.");
            }

        }
    }

    public boolean isCompressRequestToGzip() {
        return compressRequestToGzip;
    }

    public void setCompressRequestToGzip(boolean compressRequestToGzip) {
        this.compressRequestToGzip = compressRequestToGzip;
    }

    @Override
    public void setConnectionTimeout(int timeout) {
        connectionTimeout = timeout;
    }

    @Override
    public void setReadTimeout(int timeout) {
        readTimeout = timeout;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        setHttpClient(httpClient);

    }

    public long getRequestCompressionThreshold() {
        return requestCompressionThreshold;
    }

    public void setRequestCompressionThreshold(long requestCompressionThreshold) {
        this.requestCompressionThreshold = requestCompressionThreshold;
    }

}
