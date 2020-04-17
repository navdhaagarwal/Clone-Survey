package com.nucleus.event;

import java.util.LinkedList;
import java.util.List;

import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

import com.nucleus.core.validation.util.NeutrinoValidator;
import com.nucleus.logging.BaseLoggers;

/**
 * This class is the single entry point for all events to register and propagate events
 */
public class EventBusImpl implements EventBus, BeanPostProcessor {

    private final List<EventListener> eventListenerRegistry = new LinkedList<EventListener>();

    public void fireEvent(Event event) {
        NeutrinoValidator.notNull(event, "Event object cannot be null");       
        for (EventListener listener : eventListenerRegistry) {
            if (listener.canHandleEvent(event)) {
                String eventListenerClass = AopUtils.getTargetClass(listener).getName();
                BaseLoggers.eventLogger.debug("Propagating event type : {} to handler class: {}", event.getEventType(),
                        eventListenerClass);
                listener.handleEvent(event);
            }
         }
    }

    /**
     *  No customizations done in this method.. 
     *  @see org.springframework.beans.factory.config.BeanPostProcessor#postProcessBeforeInitialization(java.lang.Object, java.lang.String) 
     */
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    /**
     *  Here we check if the created bean is instance of {@link EventListener} and if yes, we register it into current list of listeners
     *  @see org.springframework.beans.factory.config.BeanPostProcessor#postProcessAfterInitialization(java.lang.Object, java.lang.String) 
     */
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof EventListener) {
            EventListener listener = (EventListener) bean;
            // All Spring proxies implement TargetClassAware interface
            String eventListenerClass = AopUtils.getTargetClass(listener).getName();
            BaseLoggers.eventLogger.info("Registered event listener class : -> {} <- into event bus", eventListenerClass);
            eventListenerRegistry.add(listener);
        }
        return bean;
    }
}
