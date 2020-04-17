package com.nucleus.web.security;

import java.util.Base64;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriUtils;

import com.nucleus.logging.BaseLoggers;

public class URLBuilderHelper {

	private URLBuilderHelper(){
		
	}
	public static String appendSecurityTokenAndTimeStampToURL(String location, String csrfToken) {
		String locationWithHash = location;
		/*
		 * If location string already contains _hkstd query string that means
		 * uri location has processed already into this response wrapper.
		 */
		if (!locationWithHash.contains(NeutrinoUrlValidatorFilter.SECURITY_TOKEN)
				&& StringUtils.isNoneEmpty(csrfToken)) {

			UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromUriString(locationWithHash);
			UriComponents uri = uriComponentsBuilder.build();
			String path = uri.getPath();
			String queryString = uri.getQuery();
			String hashableUri = path;
			if (StringUtils.isNoneEmpty(queryString)) {
				if (locationWithHash.contains("enc_")) {
					try {
						queryString = UriUtils.decode(queryString, "UTF-8");
					} catch (Exception e) {
						BaseLoggers.flowLogger.error("Error in UriUtils.encodeQuery()", e);
						// throw fail(e);
					}

				}
				hashableUri = path + "?" + queryString;
			}
			String urlHash = NeutrinoUrlValidatorFilter.getHashValueOfUrl(hashableUri, csrfToken);
			StringBuilder urlStringBuilder = new StringBuilder(urlHash);
			long currentTimeMillis = System.currentTimeMillis();
			String currentTimeMillisString = String.valueOf(currentTimeMillis);
			String currentTimeMillisMD5 = DigestUtils.md5Hex(currentTimeMillisString);
			String currentTimeMillisStringEncoded = new String(
					Base64.getEncoder().encode(currentTimeMillisString.getBytes()));
			urlStringBuilder.append(currentTimeMillisMD5).append(currentTimeMillisStringEncoded);
			urlHash = urlStringBuilder.toString();
			locationWithHash = uriComponentsBuilder.queryParam(NeutrinoUrlValidatorFilter.SECURITY_TOKEN, urlHash)
					.queryParam(NeutrinoUrlValidatorFilter.SECURITY_TOKEN_REDIRECT, "redirect").build().toString();
		}
		return locationWithHash;
	}
}
