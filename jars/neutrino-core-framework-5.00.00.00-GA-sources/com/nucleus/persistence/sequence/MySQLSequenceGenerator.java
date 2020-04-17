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

import java.sql.Types;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.object.StoredProcedure;

import com.nucleus.core.exceptions.SystemException;

/**
 * @author Nucleus Software Exports Limited
 */
public class MySQLSequenceGenerator extends DatabaseSequenceGeneratorImpl {

    StoredProcedureSubclass procedureExecutor;

    @Override
    public Long getNextValue(String sequenceName) {
        Map<String, Object> input = new LinkedHashMap<String, Object>();
        input.put("seq_name", sequenceName);
        input.put("seq_incr", null);
        Map<String, Object> result = getExecutor().execute(input);
        Object value = result.get("nextval");
        if (value == null) {
            throw new SystemException(
                    "No value returned from database for sequence: '{}'. Check if the entry exists for this sequence");
        }
        return Long.valueOf(value.toString());
    }
    
    @Override
    public Long getNextValue(String sequenceName,int seqIncr) {
        Map<String, Object> input = new LinkedHashMap<String, Object>();
        input.put("seq_name", sequenceName);
        input.put("seq_incr", seqIncr);
        Map<String, Object> result = getExecutor().execute(input);
        Object value = result.get("nextval");
        if (value == null) {
            throw new SystemException(
                    "No value returned from database for sequence: '{}'. Check if the entry exists for this sequence");
        }
        return Long.valueOf(value.toString());
    }
    
    class StoredProcedureSubclass extends StoredProcedure {
        public StoredProcedureSubclass() {
            super(dataSource, "nextval");
            // Always put return parameters first.. banged my head for hours before discovering this --- Praveen Jain
            declareParameter(new SqlOutParameter("nextval", Types.BIGINT));
            declareParameter(new SqlParameter("seq_name", Types.VARCHAR));
            declareParameter(new SqlParameter("seq_incr", Types.VARCHAR));
            setFunction(true);
            compile();
        }
    }

    private StoredProcedureSubclass getExecutor() {
        if (procedureExecutor == null) {
            procedureExecutor = new StoredProcedureSubclass();
        }
        return procedureExecutor;
    }

}