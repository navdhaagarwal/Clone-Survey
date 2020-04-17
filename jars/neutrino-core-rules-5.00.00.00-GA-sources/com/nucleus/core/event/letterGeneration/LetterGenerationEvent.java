package com.nucleus.core.event.letterGeneration;


import com.nucleus.core.event.NeutrinoEvent;
import com.nucleus.core.event.NeutrinoEventWorker;

public class LetterGenerationEvent extends NeutrinoEvent{
    public LetterGenerationEvent(Object source, String name, NeutrinoEventWorker eventWorker) {
        super(source, name, eventWorker);
    }
}
