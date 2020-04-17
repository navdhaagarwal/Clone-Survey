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
package com.nucleus.core.thread.support;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.nucleus.core.NeutrinoSpringAppContextUtil;
import com.nucleus.finnone.pro.general.util.ValidatorUtils;
import com.nucleus.standard.context.IExecutionContextHolder;

/**
 * @author Nucleus Software Exports Limited
 *
 * MDC is inherited between threads automatically in logback.However, when we use thread pools,
 * it cause problems and you get no data (in case of log4j)  or false data(in logback) in the logs.
 * This is because thread pool threads are going to be created with random parent threads.
 * These thread pool threads will then inherit the MDC from these threads, and *keep* the MDC. So future
 * Runnables passed to these threads will print their starting thread's context info.
 * In such a case the Runnable's you place on the thread pool's queue need to take care of retaining the MDC
 * from the originating thread.
 */
public abstract class MdcRetainingRunnable implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(MdcRetainingRunnable.class);

    @SuppressWarnings("rawtypes")
    private final Map           contextFromMasterThread;
    
    private Map<String, Object> globalContextMap = null;

    public MdcRetainingRunnable() {
        this.contextFromMasterThread = MDC.getCopyOfContextMap();
        LOGGER.debug("MDC-Context From Master Thread ---> {}",
                contextFromMasterThread != null ? contextFromMasterThread.toString() : "NO Master CONTEXT FOUND");
        this.globalContextMap = new HashMap<>();      
        Map<String, Object> globalContextMapFromHolder = getExecutionContextHolder().getAllFromGlobalContext();
        if(globalContextMapFromHolder != null && !globalContextMapFromHolder.isEmpty()){
        	this.globalContextMap.putAll(globalContextMapFromHolder);
        }
        
    }

    @Override
    @SuppressWarnings("rawtypes")
    public void run() {    	
        Map originalContextOFChildThread = MDC.getCopyOfContextMap();
        LOGGER.debug("MDC-Context From Child Thread ---> {}",
                originalContextOFChildThread != null ? originalContextOFChildThread.toString() : "NO Child CONTEXT FOUND");
        if(ValidatorUtils.hasAnyEntry(contextFromMasterThread)){
        	MDC.setContextMap(contextFromMasterThread);        
        }
        addDataToGlobalContext();
        
        try {
            runWithMdc();
        } finally {
            if (ValidatorUtils.hasAnyEntry(originalContextOFChildThread)) {
                MDC.setContextMap(originalContextOFChildThread);
            }
        }
    }

    protected abstract void runWithMdc();
    
    /**
     * This method adds data to global-context by iterating 
     * globalContextMap variable of this class.
     */
    private void addDataToGlobalContext(){
    	if(this.globalContextMap == null || this.globalContextMap.isEmpty()){
    		return;
    	}
    	IExecutionContextHolder executionContextHolder = getExecutionContextHolder();
    	for (Map.Entry<String, Object> entry : this.globalContextMap.entrySet())
    	{
    		executionContextHolder.addToGlobalContext(entry.getKey(), entry.getValue());
    	}
    		
    }
    /**
     * This method returns bean object of ExecutionContextHolder
     * @return 
     */
    private IExecutionContextHolder getExecutionContextHolder(){
    	 return NeutrinoSpringAppContextUtil.getBeanByName("executionContextHolder", IExecutionContextHolder.class);
    }

}
