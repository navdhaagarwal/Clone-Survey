/**
 * This file and a proportion of its content is copyright of Nucleus Software Exports Limited - © 2012. All rights reserved.
 * Any redistribution or reproduction of part or all of the contents in any form is prohibited other than the following:
 * - you cannot print or download to a local hard disk extract contents either part or full for personal/ commercial/
 * academic or any other use
 * - you may not copy the content to individual/ third parties for any type of use, either as compiled or source format
 * without the knowledge and consent of Nucleus SOftware
 * - You may not, except with our express written permission, distribute or commercially exploit the content. Nor may you
 * transmit it or store it in any other web site or other form of electronic retrieval system.
 */
package com.nucleus.core.common;

import java.util.Comparator;

import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.Entity;

/**
 * Common class to contain all common comparators which can be used by Neutrino code
 * @author Nucleus Software India Pvt Ltd
 */
public class NeutrinoComparators {

    private NeutrinoComparators() {
    }

    //@formatter:off        //This will switch off the automatic code formatting for this file
    
    public static Comparator<Entity> ID_COMPARATOR = new Comparator<Entity>() {
        
        @Override
        public int compare(Entity arg0, Entity arg1) {
            return ((Long)arg0.getId()).compareTo((Long)arg1.getId());
        }
    };
    
    public static Comparator<Entity> CREATION_TIME_STAMP_COMPARATOR = new Comparator<Entity>() {
        @Override
        public int compare(Entity arg0, Entity arg1) {
        	BaseEntity baseEntity0 = (BaseEntity)arg0;
        	BaseEntity baseEntity1 = (BaseEntity)arg1;
        	
        	if (baseEntity0.getEntityLifeCycleData() == null ||
        			baseEntity1.getEntityLifeCycleData() == null ||
        					baseEntity0.getEntityLifeCycleData().getCreationTimeStamp() == null ||
        							baseEntity1.getEntityLifeCycleData().getCreationTimeStamp() == null){
        		return 0;
        	}
        	
        	return baseEntity0.getEntityLifeCycleData().getCreationTimeStamp().compareTo(baseEntity1.getEntityLifeCycleData().getCreationTimeStamp());
        }
    };
}
