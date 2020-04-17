package com.nucleus.entity;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Base class for all summary classes.
 */
public abstract class BaseSummary implements Serializable {

    //~ Static variables/initializers ==============================================================

    private static final long serialVersionUID = 1L;
    public static final String URI_PART_SEPARATOR = ":";

    //~ Member variables ===========================================================================

    protected Long id;

    //~ Constructors ===============================================================================

    public BaseSummary() {
    }

    public BaseSummary(BaseSummary summary) {
        setId(summary.getId());
    }

    public BaseSummary(BaseEntity entity) {
        setId(entity.getId());
    }

    //~ Methods ====================================================================================

    /**
     * Returns the set of summary property names corresponding
     * to <code>entityPropertyNames</code>.
     * 
     * @param entityPropertyNames             the set of entity property names
     * @param entityToSummaryPropertyNameMap  the mapping from entity property name to summary property names
     * @param summaryPropertyNames            the set of all summary property names
     * @return                                the set of summary property names for the given entity property names
     */
    public static Set<String> getSummaryPropertyNamesFromEntityPropertyNames(Set<String> entityPropertyNames,
                                                                             Map<String,String[]> entityToSummaryPropertyNameMap,
                                                                             Set<String> summaryPropertyNames)
    {
        Set<String> value = null;
        if (entityPropertyNames != null) {
            for (String entityPropertyName : entityPropertyNames) {
                String[] mappedNames = entityToSummaryPropertyNameMap.get(entityPropertyName);
                if (mappedNames == null && summaryPropertyNames.contains(entityPropertyName)) {
                    mappedNames = new String[]{entityPropertyName};
                }
                if (mappedNames != null) {
                    if (value == null) {
                        value = new HashSet<String>();
                    }
                    for (String mappedName : mappedNames) {
                        value.add(mappedName);
                    }
                }
            }
        }
        return value;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        if ((id != null) && (id.longValue() != 0)) {
            this.id = id;
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if ((obj != null) && (this.getClass().equals(obj.getClass())) && (this.getId().equals(((BaseSummary) obj).getId()))) {
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : super.hashCode();
    }

    public String getUri() {
        return getClass().getName() + URI_PART_SEPARATOR + id;
    }

    @Override
    public String toString() {
        return getUri();
    }
}