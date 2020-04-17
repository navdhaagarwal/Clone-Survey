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
package com.nucleus.core.mime.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.activation.DataHandler;
import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.util.ByteArrayDataSource;

import org.apache.commons.lang3.text.StrBuilder;
import org.springframework.util.StringUtils;

/**
 * @author Nucleus Software Exports Limited
 * A utility class to perform some operations on {@link MimeMessage}
 */
public class MimeMessageUtils {

    public static String[] getFromAsStringArray(MimeMessage mimeMessage) throws MessagingException {
        return fromAdrressArrayToStringArray(mimeMessage.getFrom());

    }

    public static String getFromAsDelimitedString(MimeMessage mimeMessage) throws MessagingException {

        return fromAdrressArrayToString(mimeMessage.getFrom());
    }

    public static String[] getToAsStringArray(MimeMessage mimeMessage) throws MessagingException {
        return fromAdrressArrayToStringArray(mimeMessage.getRecipients(Message.RecipientType.TO));

    }

    public static String getToAsDelimitedString(MimeMessage mimeMessage) throws MessagingException {

        return fromAdrressArrayToString(mimeMessage.getRecipients(Message.RecipientType.TO));
    }

    public static String[] getCcAsStringArray(MimeMessage mimeMessage) throws MessagingException {
        return fromAdrressArrayToStringArray(mimeMessage.getRecipients(Message.RecipientType.CC));

    }

    public static String getCcAsDelimitedString(MimeMessage mimeMessage) throws MessagingException {

        return fromAdrressArrayToString(mimeMessage.getRecipients(Message.RecipientType.CC));
    }

    public static String[] getBccAsStringArray(MimeMessage mimeMessage) throws MessagingException {
        return fromAdrressArrayToStringArray(mimeMessage.getRecipients(Message.RecipientType.BCC));

    }

    public static String getBccAsDelimitedString(MimeMessage mimeMessage) throws MessagingException {

        return fromAdrressArrayToString(mimeMessage.getRecipients(Message.RecipientType.BCC));
    }

    public static String[] getAllRecipients(MimeMessage mimeMessage) throws MessagingException {

        return fromAdrressArrayToStringArray(mimeMessage.getAllRecipients());
    }

    public static String getAllRecipientsAsDelimitedString(MimeMessage mimeMessage) throws MessagingException {

        return fromAdrressArrayToString(mimeMessage.getAllRecipients());
    }

    public static byte[] getMimeMessageByteStream(MimeMessage mimeMessage) throws MessagingException, IOException {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        mimeMessage.writeTo(baos);

        return baos.toByteArray();
    }

    public static DataHandler getMimeMessageByteDatahandler(MimeMessage mimeMessage) throws MessagingException, IOException {
        ByteArrayDataSource byteArrayDataSource = new ByteArrayDataSource(getMimeMessageByteStream(mimeMessage), "*/*");
        return new DataHandler(byteArrayDataSource);
    }

    private static String[] fromAdrressArrayToStringArray(Address[] addresses) {
        String[] strings = null;
        if (addresses != null) {
            for (Address address : addresses) {

                if (address.getType().equalsIgnoreCase("rfc822") && InternetAddress.class.isAssignableFrom(Address.class)) {
                    StringUtils.addStringToArray(strings, ((InternetAddress) address).toUnicodeString());
                }

            }
        }
        return strings;

    }

    private static String fromAdrressArrayToString(Address[] addresses) {
        StrBuilder string = new StrBuilder();
        if (addresses != null) {
            for (Address address : addresses) {

                if (address.getType().equalsIgnoreCase("rfc822") && InternetAddress.class.isAssignableFrom(Address.class)) {
                    string.appendSeparator(',');
                    string.append(((InternetAddress) address).toUnicodeString());
                }

            }
            return string.toString();
        } else
            return "";

    }

}
