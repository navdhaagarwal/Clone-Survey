package com.nucleus.event;

/**
 * Event listener interface defining contract methods that all event listeners should abide to. 
 */
public interface EventListener {

    /**
     * Implement this method to indicate if the listener can handle this event. Typically the method will
     * check the class of event to understand if it can handle it or not. It can also further put checks on other 
     * conditions based on event data.
     */
    public boolean canHandleEvent(Event event);

    /**
     * Implement this method for actual event handling
     * @param event The propagated event which is to be handled
     */
    public void handleEvent(Event event);

}