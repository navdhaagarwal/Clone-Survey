package com.nucleus.mail.event;

import com.nucleus.core.event.NeutrinoEvent;
import com.nucleus.core.event.NeutrinoEventWorker;

public class EmailEvent extends NeutrinoEvent {

    public EmailEvent(Object source, String name, NeutrinoEventWorker neutrinoEventWorker) {
        super(source, name, neutrinoEventWorker);
    }

}
