/**
 * @FileName: EmailParameters.java
 * @Author: amit.parashar
 * @Copyright: Nucleus Software Exports Ltd
 * @Description:
 * @Program-specification-Referred:
 * @Revision:
 *            --------------------------------------------------------------------------------------------------------------
 *            --
 * @Version | @Last Revision Date | @Name | @Function/Module affected | @Modifications Done
 *          ----------------------------------------------------------------------------------------------------------------
 *          | May 30, 2012 | amit.parashar | |
 */

package com.nucleus.mail;

import java.io.File;
import java.io.Serializable;

import org.springframework.mail.javamail.JavaMailSenderImpl;

/**
 * @author amit.parashar
 *
 */
public class EmailParameters implements Serializable {

    private static final long serialVersionUID = 6422739924253278145L;

    private String            from;
    private String            toList;
    private String            ccList;
    private String            subject;
    private String            body;
    private boolean           htmlText;
    private String            attachmentFileName;
    private File              attachmentFile;

    /**
     * @param from
     * @param toList
     * @param ccList
     * @param subject
     * @param body
     * @param htmlText
     * @param attachementFileName
     * @param attachmentFile
     */
    public EmailParameters(String from, String toList, String ccList, String subject, String body, boolean htmlText,
            String attachementFileName, File attachmentFile) {
        super();
        this.from = from;
        this.toList = toList;
        this.ccList = ccList;
        this.subject = subject;
        this.body = body;
        this.htmlText = htmlText;
        this.attachmentFileName = attachementFileName;
        this.attachmentFile = attachmentFile;
    }

    public static SimpleMailMessageBuilder convertToSimpleMailMessageBuilder(EmailParameters emailParameters) {
        SimpleMailMessageBuilder simpleMailMessageBuilder = new SimpleMailMessageBuilder()
                .setFrom(emailParameters.getFrom()).setTo(emailParameters.getToList()).setCc(emailParameters.getCcList())
                .setSubject(emailParameters.getSubject()).setPlainTextBody(emailParameters.getBody());
        return simpleMailMessageBuilder;
    }

    public static MimeMailMessageBuilder convertToMimeMessageBuilder(EmailParameters emailParameters) {
        // TODO : Change the way MimeMailMessageBuilder object has been created. Should it be created using the MailService?
        MimeMailMessageBuilder mimeMailMessageBuilder = new MimeMailMessageBuilder(
                new JavaMailSenderImpl().createMimeMessage()).setFrom(emailParameters.getFrom())
                .setTo(emailParameters.getToList()).setCc(emailParameters.getCcList())
                .setSubject(emailParameters.getSubject()).setHtmlBody(emailParameters.getBody())
                .addAttachment(emailParameters.getAttachmentFileName(), emailParameters.getAttachmentFile());
        return mimeMailMessageBuilder;
    }

    public String getAttachmentFileName() {
        return attachmentFileName;
    }

    public void setAttachmentFileName(String attachmentFileName) {
        this.attachmentFileName = attachmentFileName;
    }

    /**
     * @return the attachmentFile
     */
    public File getAttachmentFile() {
        return attachmentFile;
    }

    /**
     * @param attachmentFile the attachmentFile to set
     */
    public void setAttachmentFile(File attachmentFile) {
        this.attachmentFile = attachmentFile;
    }

    /**
     * @return the from
     */
    public String getFrom() {
        return from;
    }

    /**
     * @param from the from to set
     */
    public void setFrom(String from) {
        this.from = from;
    }

    /**
     * @return the toList
     */
    public String getToList() {
        return toList;
    }

    /**
     * @param toList the toList to set
     */
    public void setToList(String toList) {
        this.toList = toList;
    }

    /**
     * @return the ccList
     */
    public String getCcList() {
        return ccList;
    }

    /**
     * @param ccList the ccList to set
     */
    public void setCcList(String ccList) {
        this.ccList = ccList;
    }

    /**
     * @return the subject
     */
    public String getSubject() {
        return subject;
    }

    /**
     * @param subject the subject to set
     */
    public void setSubject(String subject) {
        this.subject = subject;
    }

    /**
     * @return the body
     */
    public String getBody() {
        return body;
    }

    /**
     * @param body the body to set
     */
    public void setBody(String body) {
        this.body = body;
    }

    /**
     * @return the htmlText
     */
    public boolean isHtmlText() {
        return htmlText;
    }

    /**
     * @param htmlText the htmlText to set
     */
    public void setHtmlText(boolean htmlText) {
        this.htmlText = htmlText;
    }

}
