package com.nucleus.logging;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class LogEncrypter {
    private static final String encryptionKey = "<A,./?{}|[]a12~!@#3$5%7^8&2*1(7)0_q-z+`-=A>";

    private static class Encryptor {

        private static SecretKeySpec secretKey;
        private static byte[]        key;

        public static void setKey(String myKey) {
            MessageDigest sha = null;
            try {
                key = myKey.getBytes("UTF-8");
                sha = MessageDigest.getInstance("SHA-1");
                key = sha.digest(key);
                key = Arrays.copyOf(key, 16);
                secretKey = new SecretKeySpec(key, "AES");
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        public static String encrypt(String strToEncrypt, String secret) {
            try {
                setKey(secret);
                Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
                cipher.init(Cipher.ENCRYPT_MODE, secretKey);
                return Base64.getEncoder().encodeToString(cipher.doFinal(strToEncrypt.getBytes("UTF-8")));
            } catch (Exception e) {
                System.out.println("Error while encrypting: " + e.toString());
            }
            return "";
        }
    }

    public static Object[] encrypt(Object[] args) {
        String message = null;
        if (args.length > 1) {
            message = formatString((String) args[0], Arrays.copyOfRange(args, 1, args.length));
        } else if (args.length == 1) {
            message = (String) args[0];
        }
        args[0] = Encryptor.encrypt(message, encryptionKey);
        return args;

    }

    private static String formatString(String message, Object[] args) {
        return MessageFormat.format(message, args);
    }
}
