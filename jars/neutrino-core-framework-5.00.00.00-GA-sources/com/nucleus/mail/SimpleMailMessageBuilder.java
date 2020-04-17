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

import org.springframework.mail.MailMessage;
import org.springframework.mail.SimpleMailMessage;

/**
 * @author Nucleus Software Exports Limited
 */
public class SimpleMailMessageBuilder extends NeutrinoMailMessageBuilder<SimpleMailMessageBuilder> {

    private static final long       serialVersionUID = 7401424132549396770L;

    private final SimpleMailMessage message          = new SimpleMailMessage();

    /**
     * Default constructor to prohibit direct instantiation outside the package
     */
    SimpleMailMessageBuilder() {
    }

    @Override
    protected MailMessage getMailMessage() {
        return message;
    }

    @Override
    public SimpleMailMessageBuilder setPlainTextBody(String body) {
        message.setText(body);
        return getThis();
    }

    @Override
    protected SimpleMailMessageBuilder getThis() {
        return this;
    }

    SimpleMailMessage getNativeMailMessage() {
        return message;
    }

}
