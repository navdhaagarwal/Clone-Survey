package com.nucleus.mail;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;

import net.bull.javamelody.MonitoredWithSpring;
import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.ValidationException;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.parameter.PartStat;
import net.fortuna.ical4j.model.parameter.Role;
import net.fortuna.ical4j.model.parameter.Rsvp;
import net.fortuna.ical4j.model.property.Attendee;
import net.fortuna.ical4j.model.property.Location;
import net.fortuna.ical4j.model.property.Method;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.Summary;
import net.fortuna.ical4j.model.property.Uid;
import net.fortuna.ical4j.model.property.Version;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.nucleus.core.event.NeutrinoEventPublisher;
import com.nucleus.core.exceptions.SystemException;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.mail.event.EmailEventWorker;

/**
 * @author amit.parashar
 *
 */
@Service("mailService")
public class SMTPMailServiceImpl implements MailService {

    @Autowired
    private JavaMailSender         mailSender;

    @Inject
    private NeutrinoEventPublisher neutrinoEventPublisher;

    @Override
    public MimeMailMessageBuilder createMimeMailBuilder() {
        return new MimeMailMessageBuilder(mailSender.createMimeMessage());
    }

    @Override
    @MonitoredWithSpring(name = "SMTPMSI_CREATE_MAIL_BUILDER")
    public SimpleMailMessageBuilder createSimpleMailBuilder() {
        return new SimpleMailMessageBuilder();
    }

    @Override
    public void sendMail(MimeMailMessageBuilder mimeMessageBuilder) {
        EmailEventWorker emailEventWorker = new EmailEventWorker("Mail Event");
        emailEventWorker.setBuilder(mimeMessageBuilder);
        neutrinoEventPublisher.publish(emailEventWorker);
    }

    @Override
    @MonitoredWithSpring(name = "SMTPMSI_SEND_MAIL")
    public void sendMail(SimpleMailMessageBuilder simpleMessageBuilder) {
        mailSender.send(simpleMessageBuilder.getNativeMailMessage());
    }

    @Override
    public void sendAppointment(List<String> required, List<String> optional, String from, org.joda.time.DateTime startDate,
            int durationInMinutes, String location, String subject, String body) {
        List<String> tos = new ArrayList<String>();
        // Set the time zone
        org.joda.time.DateTime endDate = new org.joda.time.DateTime(startDate.toDate()).plusMinutes(durationInMinutes);

        DateTime start = new DateTime(startDate.toDate());
        DateTime end = new DateTime(endDate.toDate());

        String eventName = "Progress Meeting";
        VEvent meeting = new VEvent(start, end, eventName);

        meeting.getProperties().add(new Location(location));
        meeting.getProperties().add(new Summary(subject));

        String uidValue = String.valueOf(System.currentTimeMillis());
        Uid uid = new Uid();
        uid.setValue(uidValue);
        meeting.getProperties().add(uid);

        if (required != null && !required.isEmpty()) {
            for (String req : required) {
                if (StringUtils.isNotBlank(req) && !"null".equals(req)) {
                    Attendee attendee = new Attendee(URI.create("mailto:" + req));
                    attendee.getParameters().add(Role.REQ_PARTICIPANT);
                    attendee.getParameters().add(PartStat.NEEDS_ACTION);
                    attendee.getParameters().add(Rsvp.TRUE);
                    meeting.getProperties().add(attendee);
                    tos.add(req);
                }
            }
        }
        if (optional != null && !optional.isEmpty()) {
            for (String op : optional) {
                if (StringUtils.isNotBlank(op) && !"null".equals(op)) {
                    Attendee attendee2 = new Attendee(URI.create("mailto:" + op));
                    attendee2.getParameters().add(Role.OPT_PARTICIPANT);
                    meeting.getProperties().add(attendee2);
                    tos.add(op);
                }
            }
        }

        if (StringUtils.isBlank(from) || "null".equals(from)) {
            from = "neutrino@nucleussoftware.com";
        }

        net.fortuna.ical4j.model.Calendar icsCalendar = new net.fortuna.ical4j.model.Calendar();
        icsCalendar.getProperties().add(new ProdId("-//Events Calendar//iCal4j 1.0//EN"));
        icsCalendar.getProperties().add(Version.VERSION_2_0);
        icsCalendar.getProperties().add(Method.REQUEST);
        icsCalendar.getComponents().add(meeting);

        CalendarOutputter co = new CalendarOutputter(false);
        Writer wtr = new StringWriter();
        try {
            co.output(icsCalendar, wtr);
        } catch (IOException e) {
            throw new SystemException("Exception occured while creating meeting invite", e);
        } catch (ValidationException e) {
            throw new SystemException("Exception occured while creating meeting invite", e);
        }
        String mailContent = wtr.toString();
        sendAppointmentMail(tos, from, subject, mailContent, body);
        BaseLoggers.flowLogger.debug("iCalendar meeting invitation sent");
    }

    private void sendAppointmentMail(List<String> tos, String from, String subject, String content, String body) {

        Multipart multipart = new MimeMultipart("alternative");

        try {
            BodyPart messageBodyPart = buildHtmlTextPart(body);
            multipart.addBodyPart(messageBodyPart);

            BodyPart calendarPart = buildCalendarPart(content);
            multipart.addBodyPart(calendarPart);

            MimeMailMessageBuilder mimeMailMessageBuilder = createMimeMailBuilder();
            mimeMailMessageBuilder.getMimeMessage().setSubject(subject);

            for (String to : tos) {
                mimeMailMessageBuilder.getMimeMessage().addRecipient(Message.RecipientType.TO, new InternetAddress(to));
            }

            mimeMailMessageBuilder.getMimeMessage().setFrom(new InternetAddress(from));

            mimeMailMessageBuilder.getMimeMessage().setContent(multipart);

            sendMail(mimeMailMessageBuilder);
        } catch (MessagingException me) {
            throw new SystemException("Exception occured while sending meeting invite", me);
        } catch (Exception ex) {
            throw new SystemException("Exception occured while sending meeting invite", ex);
        }
    }

    private BodyPart buildHtmlTextPart(String content) throws MessagingException {

        MimeBodyPart descriptionPart = new MimeBodyPart();

        // Note: even if the content is spcified as being text/html, outlook won't read correctly tables at all
        // and only some properties from div:s. Thus, try to avoid too fancy content
        descriptionPart.setContent(content, "text/html; charset=utf-8");

        return descriptionPart;
    }

    private BodyPart buildCalendarPart(String calendarContent) throws Exception {

        BodyPart calendarPart = new MimeBodyPart();
        calendarPart.addHeader("Content-Class", "urn:content-classes:calendarmessage");
        calendarPart.setContent(calendarContent, "text/calendar;method=CANCEL");

        return calendarPart;
    }

    @Override
    public void sendMail(String htmlBody, String userEmailId, String fromEmaiId) {
        MimeMailMessageBuilder mimeMailMessageBuilder = createMimeMailBuilder();
        mimeMailMessageBuilder.setFrom(fromEmaiId).setTo(userEmailId).setSubject(htmlBody);
        sendMail(mimeMailMessageBuilder);
    }

    @Override
    public void sendMail(String htmlBody, String subject, String userEmailId, String fromEmaiId) {
        MimeMailMessageBuilder mimeMailMessageBuilder = createMimeMailBuilder();
        mimeMailMessageBuilder.setFrom(fromEmaiId).setTo(userEmailId).setSubject(subject).setHtmlBody(htmlBody);
        sendMail(mimeMailMessageBuilder);
    }

}