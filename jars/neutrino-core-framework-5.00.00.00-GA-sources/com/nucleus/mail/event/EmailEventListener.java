package com.nucleus.mail.event;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Named;
import javax.mail.MessagingException;

import org.springframework.context.ApplicationListener;

import com.nucleus.logging.BaseLoggers;
import com.nucleus.notificationMaster.service.NotificationMasterService;

@Named(value = "emailEventListener")
public class EmailEventListener implements ApplicationListener<EmailEvent> {

    @Inject
    @Named("notificationMasterService")
    protected NotificationMasterService notificationMasterService;

    @Override
    public void onApplicationEvent(EmailEvent event) {
        EmailEventWorker worker = (EmailEventWorker) event.getEventWorker();
        try {
            if (worker.getBuilder() != null) {
                notificationMasterService.sendEmail(worker.getBuilder());
            } else {
                notificationMasterService.constructEmailAndSend(worker.getEmailList(), worker.getContextmap(),
                        worker.getEmailSubject(), worker.getEmailBody(), worker.getAttachedDocument());
            }

        } catch (MessagingException e) {
            BaseLoggers.exceptionLogger.error("MessagingException Occur in sending email ", e);
        } catch (IOException e) {
            BaseLoggers.exceptionLogger.error("IOException Occur in sending email", e);
        }
    }
}
