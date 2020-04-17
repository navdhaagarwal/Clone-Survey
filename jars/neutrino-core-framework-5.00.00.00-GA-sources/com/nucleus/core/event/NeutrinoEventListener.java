/**
 * Copyright Nucleus Software India Pvt Ltd. All rights reserved.
 */
package com.nucleus.core.event;
/**
 * @author Nucleus Software India Pvt Ltd 
 */
import javax.inject.Named;

import org.springframework.context.ApplicationListener;

@Named(value = "neutrinoEventListener")
public class NeutrinoEventListener implements ApplicationListener<NeutrinoEvent> {
    
    public void onApplicationEvent(NeutrinoEvent e) {
        
        //ruleInvocationService.invokeRule(e.getInvocationPoint().getInvocationPoint(), e.getMap());
    }
}