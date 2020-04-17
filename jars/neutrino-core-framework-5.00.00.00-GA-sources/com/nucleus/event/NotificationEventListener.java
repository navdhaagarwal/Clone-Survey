/**
 * This file and a proportion of its content is copyright of Nucleus Software Exports Limited - � 2012. All rights reserved.
 * Any redistribution or reproduction of part or all of the contents in any form is prohibited other than the following:
 * - you cannot print or download to a local hard disk extract contents either part or full for personal/ commercial/
 * academic or any other use
 * - you may not copy the content to individual/ third parties for any type of use, either as compiled or source format
 * without the knowledge and consent of Nucleus Software
 * - You may not, except with our express written permission, distribute or commercially exploit the content. Nor may you
 * transmit it or store it in any other web site or other form of electronic retrieval system.
 */
package com.nucleus.event;

import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

import javax.inject.Inject;
import javax.inject.Named;

import com.nucleus.core.transaction.TransactionPostCommitWorker;
import com.nucleus.persistence.EntityDao;

/**
 * This class is used to listen all events fired from event bus.Here some common tasks 
 * like creating event entries creating pop-up notifications and sending internal as well 
 * as SMTP e-mail will be performed asynchronously. 
 * 
 * @author Nucleus Software Exports Limited
 *
 */
@Named
public class NotificationEventListener extends GenericEventListener {

    @Inject
    @Named("eventProcessingHelper")
    private EventProcessingHelper  eventProcessingHelper;

    @Inject
    @Named("entityDao")
    protected EntityDao            entityDao;

    @Inject
    @Named("eventProcessor")
    private EventProcessor eventProcessor;

    @Override
    public boolean canHandleEvent(Event event) {
        boolean canHandleEvent = false;

        if (isNotEmpty(event.getPersistentPropertyKeys()) && (event instanceof GenericEvent)) {
            canHandleEvent = true;
        }

        return canHandleEvent;
    }

    @Override
    public void handleEvent(final Event event) {

        // First of all persist the event in DB.
        eventProcessingHelper.saveEvent(event);

        // We try to flush all changes to DB before proceeding further.
        // No event processing should happen in case there is any database error.
        //entityDao.flush();

        //eventProcessor.processEvent(event);
        TransactionPostCommitWorker.handlePostCommit(eventProcessor, event, true);  
    }

}