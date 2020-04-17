package com.nucleus.rules.model;

import javax.persistence.Cacheable;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.annotations.Synonym;
import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.CloneOptions;

/**
 * 
 * @author Nucleus Software Exports Limited
 * Query Parameter Attribute Entity class
 */

@Entity
@DynamicUpdate
@DynamicInsert
@Cacheable
@Inheritance(strategy = InheritanceType.JOINED)
@Synonym(grant="ALL")
public class QueryParameterAttribute extends BaseEntity {
    private static final long serialVersionUID = 4366231031666006681L;

    private String            queryParameterName;

    public String             objectGraph;

    /**
     * 
     * Get objectGraph property
     * @return
     */
    public String getObjectGraph() {
        return objectGraph;
    }

    /**
     * 
     * Set objectGraph property
     * @param objectGraph
     */
    public void setObjectGraph(String objectGraph) {
        this.objectGraph = objectGraph;
    }

    /**
     * 
     * Get queryParameterName property
     * @return
     */
    public String getQueryParameterName() {
        return queryParameterName;
    }

    /**
     * 
     * Set queryParameterName property
     * @param queryParameterName
     */
    public void setQueryParameterName(String queryParameterName) {
        this.queryParameterName = queryParameterName;
    }

    @Override
    protected void populate(BaseEntity baseEntity, CloneOptions cloneOptions) {
        QueryParameterAttribute queryParameterAttribute = (QueryParameterAttribute) baseEntity;
        super.populate(queryParameterAttribute, cloneOptions);
        queryParameterAttribute.setObjectGraph(objectGraph);
        queryParameterAttribute.setQueryParameterName(queryParameterName);
    }

    @Override
    protected void populateFrom(BaseEntity baseEntity, CloneOptions cloneOptions) {
        QueryParameterAttribute queryParameterAttribute = (QueryParameterAttribute) baseEntity;
        super.populateFrom(queryParameterAttribute, cloneOptions);
        this.setQueryParameterName(queryParameterAttribute.getQueryParameterName());
        this.setObjectGraph(queryParameterAttribute.getObjectGraph());

    }

}
