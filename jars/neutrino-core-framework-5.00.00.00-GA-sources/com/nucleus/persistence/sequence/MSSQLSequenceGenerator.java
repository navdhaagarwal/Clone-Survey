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
 * TODO -> shailendra.kumar Add documentation to class
 */
public class MSSQLSequenceGenerator extends DatabaseSequenceGeneratorImpl {

    StoredProcedureSubclass procedureExecutor;

    @Override
    public Long getNextValue(String sequenceName) {
        Map<String, Object> input = new LinkedHashMap<String, Object>();
        input.put("sequenceName", sequenceName);
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
            super(dataSource, "next_val");
            declareParameter(new SqlParameter("sequenceName", Types.VARCHAR));
            declareParameter(new SqlOutParameter("nextval", Types.BIGINT));
            compile();
        }
    }

    private StoredProcedureSubclass getExecutor() {
        if (procedureExecutor == null) {
            procedureExecutor = new StoredProcedureSubclass();
        }
        return procedureExecutor;
    }

    @Override
    public Long getNextValue(String sequenceName, int seqIncr) {
        return null;
    }

}
