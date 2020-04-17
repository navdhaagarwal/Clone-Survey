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
package com.nucleus.persistence.sequence;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.jdbc.support.incrementer.OracleSequenceMaxValueIncrementer;

import com.nucleus.core.exceptions.SystemException;

/**
 * @author Nucleus Software Exports Limited
 */
public class OracleSequenceGenerator extends DatabaseSequenceGeneratorImpl {

    private Map<String, OracleSequenceMaxValueIncrementer> cache = new LinkedHashMap<String, OracleSequenceMaxValueIncrementer>();

    @Override
    public Long getNextValue(String sequenceName) {
        if (!cache.containsKey(sequenceName)) {
            // We only want to cache the created incrementer if there is no exception. If incrementer.nextLongValue() throws
            // Exception, we do not cache the incrementer
            try {
                OracleSequenceMaxValueIncrementer incrementer = new OracleSequenceMaxValueIncrementer(dataSource,
                        sequenceName);
                Long nextValue = incrementer.nextLongValue();
                cache.put(sequenceName, incrementer);
                return nextValue;
            } catch (Exception e) {
                throw new SystemException("Exception while getting next value from sequence " + sequenceName
                        + ". Please check if the sequence exists in the database", e);
            }
        }
        return cache.get(sequenceName).nextLongValue();
    }
    /*Options to ponder upon:
     *A) 

        You can temporarily increase the cache size and do one dummy select and then reset teh cache size back to 1. So for example
        
        ALTER SEQUENCE mysequence INCREMENT BY 100;
        
        select mysequence.nextval from dual;
        
        ALTER SEQUENCE mysequence INCREMENT BY 1;
    
    B) Java Synchronization: Not a good idea.

     */
    @Override
    public Long getNextValue(String sequenceName, int seqIncr) {
        Long nextval = getNextValue(sequenceName);
        for(int i=0;i<seqIncr;i++){
            getNextValue(sequenceName); 
        }
        return nextval;
    }
}
