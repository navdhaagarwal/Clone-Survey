/**
 * Copyright Nucleus Software India Pvt Ltd. All rights reserved.
 */
package com.nucleus.logging;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.slf4j.bridge.SLF4JBridgeHandler;

/**
 * Sets up the logger bridge between java.util.logging (JUL) and SLF4J. After calling this initialization, all JUL logging
 * will be redirected through SLF4J.  
 * @author Nucleus Software India Pvt Ltd
 */
public class JULToSlf4jSetupFacade {

    private static boolean julToSlf4JBridgeInitialized = false;

    /**
     * call the setup method to setup the bridge. Note that repeatedly calling the method will not have any effect as a boolean
     * variable internally tracks the initialization state. If already initialized, the control will immediately return from this method.
     */
    public static void setup() {
        if (!julToSlf4JBridgeInitialized) {
          
        	
        	SLF4JBridgeHandler.install();
        	Logger.getLogger("").setLevel(Level.SEVERE);
        	
            
                      
            Logger.getLogger(JULToSlf4jSetupFacade.class.getName()).log(Level.INFO,
                    "------------ JUL to SLF4J bridge has been successfully setup. ------------");
            julToSlf4JBridgeInitialized = true;
        }
    }

}
