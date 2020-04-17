package com.nucleus.event;

import com.nucleus.core.transaction.TransactionPostCommitWork;
import com.nucleus.logging.BaseLoggers;

public class EventProcessor extends AbstractEventProcessor implements TransactionPostCommitWork{

    @Override
    protected void processEvent(final Event event) {

        final EligibleWatchers eligibleWatchers = getEligibleWatchersForProcessing(event);

        // perform notification operations synchronously
        if (event.getId() != null) {
            BaseLoggers.eventLogger.info("Processing event notification operations synchronously");
            try {
                eventProcessingHelper.processPopupNotificationTask(event,
                        eligibleWatchers.getWatchersForPopupNotifications());
            } catch (Exception e) {
                BaseLoggers.exceptionLogger.error("Exception while generating notification", e);
            }
            try {
                eventProcessingHelper.processInternalMailNotificationTask(event,
                        eligibleWatchers.getWatchersForInternalMail());
            } catch (Exception e) {
                BaseLoggers.exceptionLogger.error("Exception while generating notification", e);
            }

            // Send SMTP mail only in specific event type cases.
            if (EVENT_TYPES_FOR_SMTP_EMAIL.contains(event.getEventType())) {
                try {
                    eventProcessingHelper.processSmtpMailNotificationTask(event, eligibleWatchers.getWatchersForSmtpMail());
                } catch (Exception e) {
                    BaseLoggers.exceptionLogger.error("Exception while generating notification", e);
                }

            }
        } else {
            BaseLoggers.eventLogger.error("Event not persisted so not processing it");

        }
    }

	@Override
	public void work(Object object){
		if(object instanceof Event){
			Event event = (Event) object;
			  processEvent(event);
		}
		  
		  
	  }
}
