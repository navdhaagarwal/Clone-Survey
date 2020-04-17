package com.nucleus.persistence.sequence;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.jdbc.support.incrementer.PostgreSQLSequenceMaxValueIncrementer;

import com.nucleus.core.exceptions.SystemException;

public class PostGreSequenceGenerator extends DatabaseSequenceGeneratorImpl {

    // PostgreSQLSequenceMaxValueIncrementer
    private Map<String, PostgreSQLSequenceMaxValueIncrementer> cache = new LinkedHashMap<>();

    @Override
    public Long getNextValue(String sequenceName) {
        if (!cache.containsKey(sequenceName)) {
            // We only want to cache the created incrementer if there is no
            // exception. If incrementer.nextLongValue() throws
            // Exception, we do not cache the incrementer
            try {
                PostgreSQLSequenceMaxValueIncrementer incrementer = new PostgreSQLSequenceMaxValueIncrementer(
                        dataSource, sequenceName);
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

    @Override
    public Long getNextValue(String sequenceName, int seqIncr) {
        Long nextval = getNextValue(sequenceName);
        for (int i = 0; i < seqIncr; i++) {
            getNextValue(sequenceName);
        }
        return nextval;
    }
}
