package com.nucleus.web.cache;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.nucleus.finnone.pro.cache.common.CacheManager;
import com.nucleus.finnone.pro.cache.common.NeutrinoCachePopulator;
import com.nucleus.finnone.pro.cache.constants.FWCacheConstants;
import com.nucleus.logging.BaseLoggers;

@Controller
@RequestMapping(value = "/downloadCache")
public class FWCacheDownloadController {

	private static final String HEADER_KEY = "Content-Disposition";
	private static final String HEADER_FORMAT_ATTACHMENT = "attachment; filename=\"%s\"";
	private static final String RESPONSE_CONTENT_TYPE_TEXT = "text/plain";
	private static final String FILE_NAME = "FW-Cache";
	private static final String FILE_FORMAT = ".txt";
	private static final String FILE_ENCODING = "UTF-8";
	private static final String BRACES_START = "{";
	private static final String BRACES_END = "}";
	private static final String QUOTES = "\"";

	
	@Inject
	@Named(FWCacheConstants.CACHE_MANAGER)
	private CacheManager cacheManager;

	@Value("${fw.cache.names.excluded.from.downloading}")
	public void setExcludedNeutrinoCache(String excludedNeutrinoCacheNames) {
		this.prepareExcludedNeutrinoCache(excludedNeutrinoCacheNames);
	}

	private static Set<String> excludedNeutrinoCache;

	@PreAuthorize("hasAuthority('ADMIN_AUTHORITY')")
	@RequestMapping(value = "/printAllCache")
	public @ResponseBody void printAllFwCache(HttpServletResponse response) {

		String textFile = FILE_NAME + FILE_FORMAT;
		String headerValue = String.format(HEADER_FORMAT_ATTACHMENT, textFile);
		response.setHeader(HEADER_KEY, headerValue);
		response.setContentType(RESPONSE_CONTENT_TYPE_TEXT);

		try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
				OutputStream outputStream = response.getOutputStream();) {

			ObjectMapper mapperObj = new ObjectMapper();
			StringBuilder stringBuilder = new StringBuilder();

			byteArrayOutputStream.write(stringBuilder.append(BRACES_START).append(QUOTES).append("ALL_CACHES")
					.append(QUOTES).append(":").append(BRACES_START).append("\n").toString().getBytes(FILE_ENCODING));

			String cacheName = null;
			String regionName = null;
			for (String cacheKeyIdentifier : cacheManager.getCacheNames()) {
				if (!excludedNeutrinoCache.contains(cacheKeyIdentifier)) {
					String[] keys = cacheKeyIdentifier.split(FWCacheConstants.REGEX_DELIMITER);
					regionName = keys[0];
					cacheName = keys[1];
					try {
						stringBuilder = new StringBuilder();
						String jsonString = cacheManager.getNeutrinoCachePopulatorInstance(regionName, cacheName)
								.getCacheAsJson(mapperObj);
						byteArrayOutputStream.write(
								stringBuilder.append(QUOTES).append(cacheKeyIdentifier).append(QUOTES).append(":")
										.append(jsonString).append(",\n").toString().getBytes(FILE_ENCODING));
					} catch (JsonMappingException ex) {
						BaseLoggers.exceptionLogger
								.error("JsonMappingException while parsing NeutrinoCache : " + cacheName);
						BaseLoggers.exceptionLogger.error("JsonMappingException : " + ex.getMessage());
					}
				}
			}

			stringBuilder = new StringBuilder();
			byteArrayOutputStream.write(stringBuilder.append(QUOTES).append("DUMMY").append(QUOTES).append(":")
					.append("{}").append("\n").toString().getBytes(FILE_ENCODING));

			stringBuilder = new StringBuilder();
			byteArrayOutputStream.write(stringBuilder.append(BRACES_END).append(BRACES_END).append("\n").toString()
					.getBytes(FILE_ENCODING));
			byteArrayOutputStream.writeTo(outputStream);

		} catch (Exception e) {
			BaseLoggers.exceptionLogger
					.error("Exception occurred while downloading file : " + textFile + " :: " + e.getMessage());
		}

	}

	@PreAuthorize("hasAuthority('ADMIN_AUTHORITY')")
	@RequestMapping(value = "/printCache")
	public @ResponseBody void printFwCache(@RequestParam(required = false) String region,
			@RequestParam(required = false) String cache, HttpServletResponse response) {
		if (region == null || region.isEmpty() || cache == null || cache.isEmpty()) {
			BaseLoggers.flowLogger.error("'cacheName' or 'region' in Request Param is either NULL or Empty.");
			return;
		}

		String cacheKeyIdentifier = new StringBuilder(region).append(FWCacheConstants.KEY_DELIMITER).append(cache)
				.toString();

		if (!cacheManager.getCacheNames().contains(cacheKeyIdentifier)) {
			BaseLoggers.flowLogger.error(
					"No Neutrino Cache found with regionName : '" + region + "' and cacheName : '" + cache + "'");
			return;
		}

		NeutrinoCachePopulator neutrinoCachePopulator = cacheManager.getNeutrinoCachePopulatorInstance(region, cache);
		if (neutrinoCachePopulator == null) {
			BaseLoggers.flowLogger.error(
					"Neutrino Cache Object with regionName : '" + region + "' and cacheName : '" + cache + "' is null");
			return;
		}

		String textFile = new StringBuilder().append(FILE_NAME).append("-").append(region).append("-").append(cache)
				.append(FILE_FORMAT).toString();
		String headerValue = String.format(HEADER_FORMAT_ATTACHMENT, textFile);
		response.setHeader(HEADER_KEY, headerValue);
		response.setContentType(RESPONSE_CONTENT_TYPE_TEXT);

		try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
				OutputStream outputStream = response.getOutputStream();) {

			ObjectMapper mapperObj = new ObjectMapper();
			StringBuilder stringBuilder = new StringBuilder();

			if (!excludedNeutrinoCache.contains(cacheKeyIdentifier)) {
				String jsonString = neutrinoCachePopulator.getCacheAsJson(mapperObj);
				byteArrayOutputStream.write(stringBuilder.append(BRACES_START).append(QUOTES).append(cacheKeyIdentifier)
						.append(QUOTES).append(":").append(jsonString).append(BRACES_END).append("\n").toString()
						.getBytes(FILE_ENCODING));
			} else {
				byteArrayOutputStream
						.write(stringBuilder
								.append("Content of Cache for Region : '" + region + "' cacheName: '" + cache
										+ " not allowed to be downloaded")
								.append("\n").toString().getBytes(FILE_ENCODING));
				BaseLoggers.flowLogger.error("Content of Cache for Region : '" + region + "' cacheName: '" + cache
						+ " not allowed to be downloaded");
			}
			byteArrayOutputStream.writeTo(outputStream);
		} catch (JsonMappingException e) {
			BaseLoggers.exceptionLogger.error("JsonMappingException while parsing NeutrinoCache for Region : '" + region
					+ "' cacheName : '" + cache + "'");
			BaseLoggers.exceptionLogger.error("JsonMappingException : " + e.getMessage());
		} catch (Exception e) {
			BaseLoggers.exceptionLogger
					.error("Exception occurred while downloading file : " + textFile + " :: " + e.getMessage());
		}

	}

	private void prepareExcludedNeutrinoCache(String excludedNeutrinoCacheNames) {
		if (excludedNeutrinoCache == null) {
			excludedNeutrinoCache = new HashSet<>();
			if(excludedNeutrinoCacheNames == null) {
				return;
			}
			String[] cacheNames = excludedNeutrinoCacheNames.split(",");
			for (String cacheName : cacheNames) {
				excludedNeutrinoCache.add(cacheName.trim().toUpperCase());
			}
		}
	}

}