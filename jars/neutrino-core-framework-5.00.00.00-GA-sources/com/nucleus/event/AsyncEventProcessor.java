package com.nucleus.event;

import com.nucleus.core.thread.support.MdcRetainingRunnable;
import com.nucleus.logging.BaseLoggers;

public class AsyncEventProcessor extends AbstractEventProcessor {

    @Override
    protected void processEvent(final Event event) {
        

        // if task executor is present perform notification operations asynchronously
        if (taskExecutor != null && event.getId() != null) {
            BaseLoggers.eventLogger.info("Processing event notification operations asynchronously");
            taskExecutor.execute(new MdcRetainingRunnable() {

                @Override
                public void runWithMdc() {
                	EligibleWatchers eligibleWatchers = getEligibleWatchersForProcessing(event);
                    eventProcessingHelper.processPopupNotificationTask(event,
                            eligibleWatchers.getWatchersForPopupNotifications());
                }
            });

            taskExecutor.execute(new MdcRetainingRunnable() {

                @Override
                public void runWithMdc() {
                	EligibleWatchers eligibleWatchers = getEligibleWatchersForProcessing(event);
                    eventProcessingHelper.processInternalMailNotificationTask(event,
                            eligibleWatchers.getWatchersForInternalMail());
                }
            });

            // Send SMTP mail only in specific event type cases.
            if (EVENT_TYPES_FOR_SMTP_EMAIL.contains(event.getEventType())) {
                taskExecutor.execute(new MdcRetainingRunnable() {

                    @Override
                    public void runWithMdc() {
                    	EligibleWatchers eligibleWatchers = getEligibleWatchersForProcessing(event);
                        eventProcessingHelper.processSmtpMailNotificationTask(event,
                                eligibleWatchers.getWatchersForSmtpMail());
                    }
                });
            }

            // Send SMS only in specific event type cases.

            if (EVENT_TYPES_FOR_SMS.contains(event.getEventType())) {
                taskExecutor.execute(new MdcRetainingRunnable() {

                    @Override
                    public void runWithMdc() {
                    	EligibleWatchers eligibleWatchers = getEligibleWatchersForProcessing(event);
                        eventProcessingHelper.processSMSNotificationTask(event, null);
                    }
                });
            }

        } else {
            BaseLoggers.eventLogger
                    .error("No task executor was found for asynchronous processing of event notification operations ");

        }
    }

}
