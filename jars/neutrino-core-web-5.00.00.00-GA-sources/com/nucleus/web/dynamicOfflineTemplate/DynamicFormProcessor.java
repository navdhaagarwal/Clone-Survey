package com.nucleus.web.dynamicOfflineTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.message.BasicNameValuePair;

import com.nucleus.logging.BaseLoggers;

/**
 * 
 * @author Nucleus Software Exports Limited Dynamic Form Service Implementation
 */

@Named(value = "dynamicFormProcessor")
public class DynamicFormProcessor {

    @Inject
    @Named("templateDownloadEngine")
    protected TemplateDownloadEngine templateDownloadEngine;

    /**
     * 
     * creates zipUrl of Template for the dynamic form
     * 
     * @param map
     * @param taskId
     * @return
     */
    public String getZipUrl(String dynamicHtmlUrl, String fileUrl, String hostUrl, String formName, String formVersion,
            HttpServletRequest request, String fIChecked) {
        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
        if (null != request.getCookies() && request.getCookies().length > 0) {
            javax.servlet.http.Cookie[] cookies = request.getCookies();
            CookieStore cookieStore = new BasicCookieStore();
            BasicClientCookie cookie = new BasicClientCookie(cookies[0].getName(), cookies[0].getValue());
            cookie.setDomain(request.getServerName());
            cookie.setPath("/");
            cookieStore.addCookie(cookie);
            httpClientBuilder.setDefaultCookieStore(cookieStore);
        }

        // Create a method instance.
        HttpPost httpPost = null;
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        nameValuePairs.add(new BasicNameValuePair("CSRFToken", request.getParameter("CSRFToken")));
        // add request parameters
        if (null == fIChecked || fIChecked.isEmpty()) {
            nameValuePairs.add(new BasicNameValuePair("formName", formName));
            nameValuePairs.add(new BasicNameValuePair("formVersion", formVersion));
        } else {
            nameValuePairs.add(new BasicNameValuePair("formName", formName));
            nameValuePairs.add(new BasicNameValuePair("fIChecked", fIChecked));
        }
        CloseableHttpClient client = null;
        CloseableHttpResponse httpResponse = null;

        try {
            URI uri = new URIBuilder(dynamicHtmlUrl).setParameters(nameValuePairs).build();
            httpPost = new HttpPost(uri);
            client = httpClientBuilder.build();
            // Execute the method.

            httpResponse = client.execute(httpPost);

            if (httpResponse.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                BaseLoggers.exceptionLogger.debug("Method failed: " + httpResponse.getStatusLine());
            }

            // Read the response body.
            HttpEntity httpEntity = null;
            httpEntity = httpResponse.getEntity();

            InputStream content = httpEntity.getContent();
            String dynamicHtmlContent = IOUtils.toString(content);
            templateDownloadEngine.processContent(dynamicHtmlContent, fileUrl, hostUrl, formName, formVersion, client);
        } catch (IOException e) {
            BaseLoggers.exceptionLogger.error("Fatal transport error: " + e.getMessage());
        } catch (URISyntaxException e) {
            BaseLoggers.exceptionLogger.error("Exception creating uri : ", e);
        } finally {
            // Release the connection.
            try {
            	if(httpResponse != null)
                httpResponse.close();
            	if(client != null)
                client.close();
            } catch (IOException e) {
                BaseLoggers.exceptionLogger.warn("Exception while closing http client : " + e.getMessage());
            }
        }
        return fileUrl;
    }
}
