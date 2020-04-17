package com.nucleus.event;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;

import javax.inject.Inject;
import javax.inject.Named;

import com.nucleus.core.notification.Notification;
import com.nucleus.entity.EntityId;
import com.nucleus.finnone.pro.general.util.ValidatorUtils;

public abstract class AbstractEventProcessor {

    protected static final String        SEND_INTERNAL_EMAIL_NOTIFICATIONS = "config.notifications.sendInternalEmailNotifications";
    protected static final String        SEND_SMTP_EMAIL_NOTIFICATIONS     = "config.notifications.sendSMTPNotifications";

    // We are not sending SMTP mail for each event type.Only specific events
    // types listed here will be notified with SMTP
    // email system keeping user preferences in consideration.
    protected static final List<Integer> EVENT_TYPES_FOR_SMTP_EMAIL        = Arrays.asList(new Integer[] {
            EventTypes.WORKFLOW_ESCALATION_NOTIFICATION_EVENT, EventTypes.APPOINTMENT_ESCALATION_NOTIFICATION_EVENT, EventTypes.WORKFLOW_APPLICATION_ESCALATION_NOTIFICATION_EVENT });

    protected static final List<Integer> EVENT_TYPES_FOR_SMS               = Arrays.asList(new Integer[] {
            EventTypes.WORKFLOW_QUICKLEAD_CREATE_EVENT, EventTypes.WORKFLOW_INTERNET_CHANNEL_LEAD_CREATE_EVENT });

    @Inject
    @Named("neutrinoThreadPoolExecutor")
    protected Executor                   taskExecutor;

    @Inject
    @Named("eventProcessingHelper")
    protected EventProcessingHelper      eventProcessingHelper;

    abstract protected void processEvent(final Event event);

    protected EligibleWatchers getEligibleWatchersForProcessing(final Event event) {
        final EntityId entityId = event.getOwnerEntityId();
        final Set<String> temporaryWatchers = event.getNonWatchersToNotify();
        Set<String> permanentWatchersForEntity = eventProcessingHelper.getAllWatchersForEntity(entityId);

        Set<String> consolidatedWatchersSet = new HashSet<String>(permanentWatchersForEntity);

        // will be added only if not already present in watchers list(set
        // doesn't allow duplicates)
        consolidatedWatchersSet.addAll(temporaryWatchers);

        // Create three buckets for three type of notifications based on some
        // criteria
        // create pop-up notifications for users without considering user
        // preferences.
        final Set<String> eligibleWatchersForPopupNotifications = new HashSet<String>(consolidatedWatchersSet);
        final Set<String> eligibleWatchersForInternalMail = new HashSet<String>();
        final Set<String> eligibleWatchersForSmtpMail = new HashSet<String>();

        // if notification is mandatory for this event,send Internal mail and
        // SMTP mail notifications without checking user
        // preferences
        if (event.isNotificationMandatory()) {
            eligibleWatchersForInternalMail.addAll(consolidatedWatchersSet);
            eligibleWatchersForSmtpMail.addAll(consolidatedWatchersSet);
        } else {
            // send Internal mail and SMTP mail notifications to users by
            // considering user preferences.(if user want such
            // type of notification)
            for (String watcherUri : consolidatedWatchersSet) {

                if (eventProcessingHelper.getUserPreference(watcherUri, SEND_INTERNAL_EMAIL_NOTIFICATIONS)) {
                    eligibleWatchersForInternalMail.add(watcherUri);
                }
                if (eventProcessingHelper.getUserPreference(watcherUri, SEND_SMTP_EMAIL_NOTIFICATIONS)) {
                    eligibleWatchersForSmtpMail.add(watcherUri);
                }

            }
        }

        EligibleWatchers eligibleWatchers = new EligibleWatchers();
        eligibleWatchers.setWatchersForPopupNotifications(eligibleWatchersForPopupNotifications);
        eligibleWatchers.setWatchersForInternalMail(eligibleWatchersForInternalMail);
        eligibleWatchers.setWatchersForSmtpMail(eligibleWatchersForSmtpMail);
        return eligibleWatchers;
    }

    class EligibleWatchers {

        private Set<String> watchersForPopupNotifications;
        private Set<String> watchersForInternalMail;
        private Set<String> watchersForSmtpMail;

        public Set<String> getWatchersForPopupNotifications() {
            return watchersForPopupNotifications;
        }

        public void setWatchersForPopupNotifications(Set<String> watchersForPopupNotifications) {
            this.watchersForPopupNotifications = watchersForPopupNotifications;
        }

        public Set<String> getWatchersForInternalMail() {
            return watchersForInternalMail;
        }

        public void setWatchersForInternalMail(Set<String> watchersForInternalMail) {
            this.watchersForInternalMail = watchersForInternalMail;
        }

        public Set<String> getWatchersForSmtpMail() {
            return watchersForSmtpMail;
        }

        public void setWatchersForSmtpMail(Set<String> watchersForSmtpMail) {
            this.watchersForSmtpMail = watchersForSmtpMail;
        }

    }

}
