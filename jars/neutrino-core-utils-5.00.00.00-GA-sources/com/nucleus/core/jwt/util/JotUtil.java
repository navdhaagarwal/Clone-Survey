package com.nucleus.core.jwt.util;

import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.crypto.SecretKey;

import org.apache.commons.lang3.ArrayUtils;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nucleus.jwt.IJot;
import com.nucleus.jwt.Jot;
import com.nucleus.logging.BaseLoggers;

/**
 * A JWT encryption and decryption utility.
 * 
 * @author syambrij.maurya
 *
 */
public class JotUtil {
	
	private static IJot iJot = new Jot();
	private static ObjectMapper om = new ObjectMapper();
	
	private JotUtil () {
		//Util class. Object creation not allowed.
	}
	
	/**
	 * Encryption method that uses already present secret key and algo from jar.
	 * 
	 * @param key json key against the data is being put.
	 * @param inputData
	 * @return
	 */
	public static Map<String, String> encrypt(String key, Object inputData) {
		Map<String, Object> claims = new HashMap<>();
		Map<String,String> payLoadMap = new HashMap<>();
		try {
			SecretKey resSecretKey = iJot.formHmacShaKeyFor(ArrayUtils.toPrimitive(Jot.getEncodedValHs512()));
			claims.put(key, om.writeValueAsString(inputData));
			String body = iJot.requestForJot(claims, Jot.getEncodedAlgo(), resSecretKey);
			payLoadMap.put("payload", body);
			return payLoadMap;
		} catch (ParseException e) {
	    	BaseLoggers.exceptionLogger.error("Parsing exception. Could not perform jot encryption.", e);
	    } catch (JsonProcessingException e) {
	    	BaseLoggers.exceptionLogger.error("Json Processing Exception. Could not perform jot encryption.", e);
	    }
		return null;
	}

	/**
	 * Decryption method that uses already present secret key and algo from jar.
	 * 
	 * @param key  json key against the data is being put.
	 * @param parameters
	 * @param clazz
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> T decrypt(String key, Map<String, String> parameters, Class<?> clazz) {
		try {
			SecretKey secretKey = iJot.formHmacShaKeyFor(ArrayUtils.toPrimitive(Jot.getEncodedValHs512()));
			Set<Map.Entry<String, Object>> jotSet = iJot.extractJot((String) parameters.get("payload"), secretKey);
			for (Map.Entry<String, Object> jotEntry : jotSet) {
				if (key.equals(jotEntry.getKey())) {
					return (T) om.readValue(jotEntry.getValue().toString(), clazz);
				}
			}
		} catch (JsonParseException e) {
			BaseLoggers.exceptionLogger.error("Json parsing exception. Could not perform jot dencryption.", e);
		} catch (IOException e) {
			BaseLoggers.exceptionLogger.error("Json mapping/io exception. Could not perform jot dencryption.", e);
		}
		return null;
	}
	    
}
