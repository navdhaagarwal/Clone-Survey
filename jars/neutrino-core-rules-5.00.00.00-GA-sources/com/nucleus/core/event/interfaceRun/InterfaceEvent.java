package com.nucleus.core.event.interfaceRun;


import com.nucleus.core.event.NeutrinoEvent;
import com.nucleus.core.event.NeutrinoEventWorker;

public class InterfaceEvent extends NeutrinoEvent{
    public InterfaceEvent(Object source, String name, NeutrinoEventWorker eventWorker) {
        super(source, name, eventWorker);
    }
}
