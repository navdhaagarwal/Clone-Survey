package com.nucleus.rules.usage.eventMapping;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import com.nucleus.dao.query.NamedQueryExecutor;
import com.nucleus.persistence.EntityDao;
import com.nucleus.rules.usage.RuleUsageDataExtractor;

/**
 * 
 * @author Nucleus Software Exports Limited
 * Data Usage Extractor class
 * @param <T>
 */

public class EventMappingUsageDataExtractor<T> implements RuleUsageDataExtractor<T> {

    @Inject
    @Named("entityDao")
    protected EntityDao entityDao;

    public String       key;

    @Override
    public List<T> getData(Long id) {
        NamedQueryExecutor<Map<String, ?>> executor = new NamedQueryExecutor<Map<String, ?>>("RuleUsage.getEventMapping");
        executor.addParameter("rule", id);
        List result = entityDao.executeQuery(executor);
        return result;
    }

    @Override
    public String getKey() {
        return this.key;
    }

    public void setKey(String key) {
        this.key = key;
    }

}
