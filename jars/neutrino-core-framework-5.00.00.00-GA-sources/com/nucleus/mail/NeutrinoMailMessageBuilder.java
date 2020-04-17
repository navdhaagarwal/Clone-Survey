/**
 * This file and a proportion of its content is copyright of Nucleus Software Exports Limited - © 2012. All rights reserved.
 * Any redistribution or reproduction of part or all of the contents in any form is prohibited other than the following:
 * - you cannot print or download to a local hard disk extract contents either part or full for personal/ commercial/
 * academic or any other use
 * - you may not copy the content to individual/ third parties for any type of use, either as compiled or source format
 * without the knowledge and consent of Nucleus SOftware
 * - You may not, except with our express written permission, distribute or commercially exploit the content. Nor may you
 * transmit it or store it in any other web site or other form of electronic retrieval system.
 */
package com.nucleus.mail;

import java.io.Serializable;
import java.util.Date;

import org.apache.commons.lang3.ArrayUtils;
import org.springframework.mail.MailMessage;

import com.nucleus.core.exceptions.InvalidDataException;

/**
 * @author Nucleus Software Exports Limited
 */
abstract class NeutrinoMailMessageBuilder<T> implements Serializable {

    private static final long serialVersionUID = 4981827417767288327L;

    protected abstract MailMessage getMailMessage();

    protected abstract T setPlainTextBody(String body);

    protected abstract T getThis();

    public T setFrom(String from) {
        getMailMessage().setFrom(from);
        return getThis();
    }

    public T setReplyTo(String replyTo) {
        getMailMessage().setReplyTo(replyTo);
        return getThis();
    }

    public T setTo(String... to) {
        validateForEmpty("To", to);
        getMailMessage().setTo(to);
        return getThis();
    }

    public T setCc(String... cc) {
        validateForEmpty("Cc", cc);
        getMailMessage().setCc(cc);
        return getThis();
    }

    public T setBcc(String... bcc) {
        validateForEmpty("Bcc", bcc);
        getMailMessage().setBcc(bcc);
        return getThis();
    }

    public T setSentDate(Date sentDate) {
        getMailMessage().setSentDate(sentDate);
        return getThis();
    }

    public T setSubject(String subject) {
        getMailMessage().setSubject(subject);
        return getThis();
    }

    private void validateForEmpty(String listType, String... array) {
        if (ArrayUtils.isEmpty(array)) {
            throw new InvalidDataException(listType + " list cannot be null");
        }
    }
}