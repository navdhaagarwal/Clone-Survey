package com.nucleus.core.datetime.entity;


import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;

import com.nucleus.core.exceptions.SystemException;
import com.nucleus.logging.BaseLoggers;
/**
 *
 *
 *
 * Cryptography utilities for TimeZoneDetails
 *
 * @since GA 2.5
 * @author prateek.chachra
 *
 */
public class TimeZoneAESUtil {


	private TimeZoneAESUtil(){

	}
    private static final String INIT_VECTOR = "databaseTimeZone";
    private static final String KEY = "dbTZdbTZdbTZdbTZ";
    private static final String BYTES_TYPE = "UTF-8";
    public static String encrypt(String value){
    	   try {
               IvParameterSpec iv = new IvParameterSpec(INIT_VECTOR.getBytes(BYTES_TYPE));
               SecretKeySpec skeySpec = new SecretKeySpec(KEY.getBytes(BYTES_TYPE), "AES");

               Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
               cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);

               byte[] encrypted = cipher.doFinal(value.getBytes());
               return Base64.encodeBase64String(encrypted);
           } catch (Exception ex) {
               BaseLoggers.exceptionLogger.error("Some error occured while trying to encrypt the time zone value", ex);
               throw new SystemException();
           }

    }


    public static String decrypt(String encrypted) {
        try {
            IvParameterSpec iv = new IvParameterSpec(INIT_VECTOR.getBytes(BYTES_TYPE));
            SecretKeySpec skeySpec = new SecretKeySpec(KEY.getBytes(BYTES_TYPE), "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);

            byte[] original = cipher.doFinal(Base64.decodeBase64(encrypted));

            return new String(original);
        } catch (Exception ex) {
            BaseLoggers.exceptionLogger.error("Some error occured while trying to decrypt the time zone value", ex);
            throw new SystemException();
        }

    }


}
