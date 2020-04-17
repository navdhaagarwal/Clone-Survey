/**
 * @FileName: MailService.java
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

import java.util.List;

import org.joda.time.DateTime;

public interface MailService {

    /**
     * 
     * @return new Object of SimpleMailMessageBuilder
     */
    public SimpleMailMessageBuilder createSimpleMailBuilder();

    /**
     * 
     * @return new Object of MimeMailMessageBuilder
     */
    public MimeMailMessageBuilder createMimeMailBuilder();

    /**
     * 
     * @param simpleMessageBuilder
     */
    public void sendMail(SimpleMailMessageBuilder simpleMessageBuilder);

    /**
     * 
     * @param mimeMessageBuilder
     */
    public void sendMail(MimeMailMessageBuilder mimeMessageBuilder);

    /**
     * 
     * @param required
     * @param optional
     * @param from
     * @param startDate
     * @param durationInMinutes
     * @param location
     * @param subject
     * @param body
     */
    public void sendAppointment(List<String> required, List<String> optional, String from, DateTime startDate,
            int durationInMinutes, String location, String subject, String body);

    /**
     * send mail 
     * @param htmlBody
     * @param userEmailId
     * @param fromEmaiId
     */
    public void sendMail(String htmlBody, String userEmailId, String fromEmaiId);

    /**
     * send mail
     * @param htmlBody
     * @param subject
     * @param userEmailId
     * @param fromEmaiId
     */
    public void sendMail(String htmlBody, String subject, String userEmailId, String fromEmaiId);

}