package com.nucleus.core.search.model;

import javax.persistence.Entity;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.CloneOptions;

/**
 * 
 * @author Nucleus Software Exports Limited
 * ObjectGraph Parameter Entity class
 */

@Entity
@DynamicUpdate
@DynamicInsert
public class QueryObjectGraphParameter extends QueryParam {

    private static final long serialVersionUID = 1;

    private String            objectGraph;


    public String getObjectGraph() {
        return objectGraph;
    }

    public void setObjectGraph(String objectGraph) {
        this.objectGraph = objectGraph;
    }

    /**
     * 
     * Default Constructor
     */
    public QueryObjectGraphParameter() {
        super();
    }

    @Override
    protected void populate(BaseEntity baseEntity, CloneOptions cloneOptions) {
        QueryObjectGraphParameter objectGraphParameter = (QueryObjectGraphParameter) baseEntity;
        super.populate(objectGraphParameter, cloneOptions);
        objectGraphParameter.setObjectGraph(objectGraph);
    }
}
