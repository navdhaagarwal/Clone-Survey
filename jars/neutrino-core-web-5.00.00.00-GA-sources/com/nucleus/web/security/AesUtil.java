package com.nucleus.web.security;

import org.springframework.web.util.UriUtils;

public class AesUtil {
	
    private static String iv;
    private static String salt;

    private static int keysize;
    private static int iterationCount;
    
    public static final String PASS_PHRASE = "PASS_PHRASE";
    
    public static String getIv() {
		return iv;
	}

	public static synchronized void setIv(String iv) {
		AesUtil.iv = iv;
	}

	public static String getSalt() {
		return salt;
	}

	public static synchronized void setSalt(String salt) {
		AesUtil.salt = salt;
	}

	public static int getKeysize() {
		return keysize;
	}

	public static synchronized void setKeysize(int keysize) {
		AesUtil.keysize = keysize;
	}

	public static int getIterationCount() {
		return iterationCount;
	}

	public static synchronized void setIterationCount(int iterationCount) {
		AesUtil.iterationCount = iterationCount;
	}

	
	public static String encrypt(String plainText,String passPhrase) {
       AesUtilHelper util = new AesUtilHelper(keysize, iterationCount);
       String encrypt = util.encrypt(salt, iv, passPhrase, plainText);
        return encrypt;
    }

    public static String Decrypt(String CIPHER_TEXT, String passPhrase) {
	        AesUtilHelper util = new AesUtilHelper(keysize, iterationCount);
	        String decrypt = util.decrypt(salt, iv, passPhrase, CIPHER_TEXT);
	        return decrypt;
	    }


    public static String decrypt(String providedpwd, String passPhrase,Boolean toBeDecoded) {
    	String decryptedPwd = providedpwd;
        String docodedpswd;
        if (toBeDecoded) {
            try {
                docodedpswd = UriUtils.decode(decryptedPwd, "UTF-8");
            } catch (Exception e) {
            	throw new com.nucleus.core.exceptions.SystemException("Exception during decoding the supplied password");

            }
            decryptedPwd = Decrypt(docodedpswd, passPhrase);
        }
        return decryptedPwd;
    }


    public static void main(String args[]) {
        // CIPHER_TEXT = "HnSq5tsxFWUDGDDuByeKjQ%3D%3D";
        // String s2;
        //
        // s2 = StringEscapeUtils.unescapeHtml(CIPHER_TEXT);
        //
        // Decrypt(CIPHER_TEXT);
    }
}
