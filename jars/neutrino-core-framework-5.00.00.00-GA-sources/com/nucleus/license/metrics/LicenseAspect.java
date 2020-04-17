/**
 * This file and a proportion of its content is copyright of Nucleus Software Exports Limited - © 2012. All rights reserved.
 * Any redistribution or reproduction of part or all of the contents in any form is prohibited other than the following:
 * - you cannot print or download to a local hard disk extract contents either part or full for personal/ commercial/
 * academic or any other use
 * - you may not copy the content to individual/ third parties for any type of use, either as compiled or source format
 * without the knowledge and consent of Nucleus Software
 * - You may not, except with our express written permission, distribute or commercially exploit the content. Nor may you
 * transmit it or store it in any other web site or other form of electronic retrieval system.
 */
package com.nucleus.license.metrics;

import javax.inject.Inject;
import javax.inject.Named;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.stajistics.Stats;
import org.stajistics.tracker.manual.ManualTracker;

import com.nucleus.core.event.NeutrinoEventPublisher;
import com.nucleus.license.event.LicenseNamedUserEventWorker;
import com.nucleus.logging.BaseLoggers;

/**
 * This contains aspects which run after methods annonated with Measure annotation.
 * @author Nucleus Software Exports Limited
 * 
 */
@Aspect
public class LicenseAspect {
  
    @Inject
    @Named(value = "neutrinoEventPublisher")
    private NeutrinoEventPublisher neutrinoEventPublisher;
    
        @Pointcut("execution(@com.nucleus.license.metrics.Measure * *(..))")
	     public void onMeasureAnnotationExecution(){
	        }
        
         @Before("onMeasureAnnotationExecution() && @annotation(measure)")
	     public void process(JoinPoint jointPoint,Measure measure) throws Throwable {
	    	BaseLoggers.flowLogger.info("Runnning the measurable aspect");
	        jointPoint.getSourceLocation();
	        String key = measure.key();
	        String eventType=measure.eventType();
	        double increment = measure.increment();
	        ManualTracker tracker = Stats.getManualTracker(key);
	        tracker.addValue(increment);
	        tracker.commit();
	        fireNamedUserEvent(key,eventType);
	    }
	
  	    public void fireNamedUserEvent(String key,String eventType){
	    	LicenseNamedUserEventWorker licenseNamedUserEventWorker=new LicenseNamedUserEventWorker(eventType);
	    	licenseNamedUserEventWorker.setEventType(eventType);
	    	licenseNamedUserEventWorker.setName(key);
	    	neutrinoEventPublisher.publish(licenseNamedUserEventWorker);
	      }
}
