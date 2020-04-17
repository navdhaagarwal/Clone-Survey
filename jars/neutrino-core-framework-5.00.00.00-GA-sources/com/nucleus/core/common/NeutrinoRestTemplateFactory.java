package com.nucleus.core.common;

import javax.inject.Inject;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class NeutrinoRestTemplateFactory {

	private static final int DEFAULT_MAX_CONN_TOTAL = 1000;

	private static final int DEFAULT_MAX_CONN_PER_ROUTE = 500;

	private static final int DEFAULT_CONNECTION_TIMEOUT_MILLISECONDS = (60 * 1000);

	private static final int DEFAULT_READ_TIMEOUT_MILLISECONDS = (60 * 1000);

	private static final int DEFAULT_CONNECTION_REQUEST_TIMEOUT_MILLISECONDS = (60 * 1000);
	
	@Inject
	NeutrinoResponseErrorHandler neutrinoResponseErrorHandler;

	/**
	 * @return the restTemplate
	 */
	public RestTemplate createRestTemplate(Integer maxConnTotal, Integer defaultMaxPerRoute, Integer readTimeOut,
			Integer connRequestTimeOut, Integer connTimeOut) {
		RestTemplate restTemplate = new RestTemplate(createHttpRequestFactory(maxConnTotal, defaultMaxPerRoute, readTimeOut,
				connRequestTimeOut, connTimeOut));
		restTemplate.setErrorHandler(neutrinoResponseErrorHandler);
		return restTemplate;
	}
	

	public RestTemplate createRestTemplate() {
		RestTemplate restTemplate = new RestTemplate(createHttpRequestFactory(null, null, null,
				null, null));
		restTemplate.setErrorHandler(neutrinoResponseErrorHandler);
		return restTemplate;
	}
	
	
	
	

	private HttpComponentsClientHttpRequestFactory createHttpRequestFactory(Integer maxConnTotal,
			Integer defaultMaxPerRoute, Integer readTimeOut, Integer connRequestTimeOut, Integer connTimeOut) {
		HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
		PoolingHttpClientConnectionManager poolingHttpClientConnectionManager = createPoolingHttpClientConnectionManager(
				maxConnTotal, defaultMaxPerRoute);
		requestFactory.setHttpClient((CloseableHttpClient) createHttpClient(poolingHttpClientConnectionManager));
		requestFactory.setReadTimeout(checkNull(readTimeOut, DEFAULT_READ_TIMEOUT_MILLISECONDS));
		requestFactory.setConnectionRequestTimeout(
				checkNull(connRequestTimeOut, DEFAULT_CONNECTION_REQUEST_TIMEOUT_MILLISECONDS));
		requestFactory.setConnectTimeout(checkNull(connTimeOut, DEFAULT_CONNECTION_TIMEOUT_MILLISECONDS));
		return requestFactory;
	}
	

	private PoolingHttpClientConnectionManager createPoolingHttpClientConnectionManager(Integer maxConnTotal,
			Integer defaultMaxPerRoute) {
		PoolingHttpClientConnectionManager poolConnManager = new PoolingHttpClientConnectionManager();
		poolConnManager.setDefaultMaxPerRoute(checkNull(defaultMaxPerRoute, DEFAULT_MAX_CONN_PER_ROUTE));
		poolConnManager.setMaxTotal(checkNull(maxConnTotal, DEFAULT_MAX_CONN_TOTAL));
		return poolConnManager;
	}
	

	
	private HttpClient createHttpClient(PoolingHttpClientConnectionManager poolingHttpClientConnectionManager) {
		return HttpClientBuilder.create().disableCookieManagement().setConnectionManager(poolingHttpClientConnectionManager).build();
	}
	
	private int checkNull(Integer actualValue, Integer defaultValue) {
		return actualValue == null ? defaultValue : actualValue;
	}
	
}
