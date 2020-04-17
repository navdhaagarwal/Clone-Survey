/**
 * This file and a proportion of its content is copyright of Nucleus Software Exports Limited - � 2012. All rights reserved.
 * Any redistribution or reproduction of part or all of the contents in any form is prohibited other than the following:
 * - you cannot print or download to a local hard disk extract contents either part or full for personal/ commercial/
 * academic or any other use
 * - you may not copy the content to individual/ third parties for any type of use, either as compiled or source format
 * without the knowledge and consent of Nucleus SOftware
 * - You may not, except with our express written permission, distribute or commercially exploit the content. Nor may you
 * transmit it or store it in any other web site or other form of electronic retrieval system.
 */
package com.nucleus.mail;

import java.io.File;
import java.io.InputStream;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.mail.MailMessage;
import org.springframework.mail.javamail.MimeMailMessage;
import org.springframework.mail.javamail.MimeMessageHelper;

import com.nucleus.core.exceptions.InvalidDataException;
import com.nucleus.core.exceptions.SystemException;

/**
 * @author Nucleus Software Exports Limited
 */
public class MimeMailMessageBuilder extends NeutrinoMailMessageBuilder<MimeMailMessageBuilder> {

    private static final long     serialVersionUID = -7501514887732985909L;

    private final MimeMailMessage message;

    /*Flag to keep track that inline resources have been added or not*/
    private boolean               inlineAdded      = false;
    
    private String				  uniqueRequestId;

    /**
     * Default constructor to prohibit direct instantiation outside the package
     */
    MimeMailMessageBuilder(MimeMessage mimeMessage) {
        MimeMessageHelper mimeMessageHelper = null;
        try {
            mimeMessageHelper = new MimeMessageHelper(mimeMessage, true);
            this.message = new MimeMailMessage(mimeMessageHelper);
        } catch (MessagingException e) {
            throw new SystemException("Exception occured while creating MimeMessageHelper", e);
        }
    }

    @Override
    protected MailMessage getMailMessage() {
        return message;
    }
    
    public MimeMessage getMimeMessage() {
        return message.getMimeMessage();
    }

    @Override
    public MimeMailMessageBuilder setPlainTextBody(String body) {
        setBody(body, false);
        return getThis();
    }

    public MimeMailMessageBuilder setHtmlBody(String body) {
        setBody(body, true);
        return getThis();
    }

    /**
     * Sets the priority of mail message. 1=Highest 5=Lowest
     * @param throwExceptionForFailure boolean to indicate that underlying API exception should be thrown (wrapped in {@link SystemException} if priority setting fails.  
     */
    public MimeMailMessageBuilder setPriority(int priority, boolean throwExceptionForFailure) {
        try {
            message.getMimeMessageHelper().setPriority(priority);
        } catch (MessagingException e) {
            if (throwExceptionForFailure) {
                throw new SystemException("Priority setting failed on MimeMessage", e);
            }
        }
        return getThis();
    }

    /**
     * Sets the priority of mail message. 1=Highest 5=Lowest
     */
    public MimeMailMessageBuilder setPriority(int priority) {
        return setPriority(priority, false);
    }

    public MimeMailMessageBuilder addAttachment(String fileName, File file) {
        try {
            message.getMimeMessageHelper().addAttachment(fileName, file);
        } catch (MessagingException e) {
            throw new InvalidDataException("Exception while adding attachment to the mail", e);
        }
        return getThis();
    }

    public MimeMailMessageBuilder addAttachment(String fileName, InputStream inputStream) {
        try {
            message.getMimeMessageHelper().addAttachment(fileName, new InputStreamResource(inputStream));
        } catch (MessagingException e) {
            throw new InvalidDataException("Exception while adding attachment to the mail", e);
        }
        return getThis();

    }

    public MimeMailMessageBuilder addAttachment(String fileName, Resource resource) {
        try {
            message.getMimeMessageHelper().addAttachment(fileName, resource);
        } catch (MessagingException e) {
            throw new InvalidDataException("Exception while adding attachment to the mail", e);
        }

        return getThis();

    }

    /**
     * See {@link MimeMessageHelper#addInline(String, File)} for more information
     * @throws InvalidDataException if there is an exception caught from underlying {@link MimeMessageHelper}
     */
    public MimeMailMessageBuilder addInline(String contentId, File file) {
        addInline(contentId, file);
        return getThis();

    }

    /**
     * See {@link MimeMessageHelper#addInline(String, Resource)} for more information
     * @throws InvalidDataException if there is an exception caught from underlying {@link MimeMessageHelper}
     */
    public MimeMailMessageBuilder addInline(String contentId, Resource resource) {
        addInline(contentId, resource);
        return getThis();

    }

    public void addInline(String contentId, Object objectToAdd) {
        try {
            if (objectToAdd instanceof File) {
                message.getMimeMessageHelper().addInline(contentId, (File) objectToAdd);
            }
            if (objectToAdd instanceof Resource) {
                message.getMimeMessageHelper().addInline(contentId, (Resource) objectToAdd);
            }
            inlineAdded = true;
        } catch (MessagingException e) {
            throw new InvalidDataException("Exception while adding inline attachment to the body", e);
        }
    }

    private void setBody(String text, boolean html) {
        try {
            if (inlineAdded) {
                throw new SystemException(
                        "Cannot set body after adding inline attachments. Please change your API calls to use set body before adding any inline attachments.");
            }
            message.getMimeMessageHelper().setText(text, html);
        } catch (MessagingException e) {
            throw new InvalidDataException("Exception while adding text to body", e);
        }
    }

    @Override
    protected MimeMailMessageBuilder getThis() {
        return this;
    }

    MimeMessage getNativeMailMessage() {
        return message.getMimeMessage();
    }

	public String getUniqueRequestId() {
		return uniqueRequestId;
	}

	public MimeMailMessageBuilder setUniqueRequestId(String uniqueRequestId) {
		this.uniqueRequestId = uniqueRequestId;
		return getThis();
	}

}