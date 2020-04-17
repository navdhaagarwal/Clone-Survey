package com.nucleus.core.misc.util;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 
 * @deprecated use UserPasswordEncodingUtil instead
 *
 */
@Deprecated
public class PasswordEncryptorUtil {

public static String encryptPassword(String text, String hashKey) throws NoSuchAlgorithmException
{
    String plaintext = text + "{" + hashKey + "}";
    MessageDigest m = MessageDigest.getInstance("SHA-256");
    m.reset();
    m.update(plaintext.getBytes());
    byte[] digest = m.digest();
    BigInteger bigInt = new BigInteger(1,digest);
    String hashtext = bigInt.toString(16); // Now we need to zero pad it if you actually want the full 32 chars.
    while(hashtext.length() < 32 )
    {
        hashtext = "0"+hashtext;
    }
    return hashtext;

}
    public static String encryptPasswordMD5(String text, String hashKey) throws NoSuchAlgorithmException
    {
        String plaintext = text + "{" + hashKey + "}";
    MessageDigest m = MessageDigest.getInstance("MD5"); 
    m.reset(); 
    m.update(plaintext.getBytes()); 
    byte[] digest = m.digest(); 
    BigInteger bigInt = new BigInteger(1,digest); 
    String hashtext = bigInt.toString(16); // Now we need to zero pad it if you actually want the full 32 chars. 
    while(hashtext.length() < 32 )
    {  
        hashtext = "0"+hashtext; 
    }
    return hashtext; 
    
}

}
