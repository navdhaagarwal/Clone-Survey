package com.nucleus.rules.model;

import javax.persistence.Cacheable;
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
@Cacheable
public class ObjectGraphParameter extends Parameter {

    private static final long serialVersionUID = 1;

    private String            objectGraph;

    /**
     * 
     * getter for objectGraph
     * @return
     */

    public String getObjectGraph() {
        return objectGraph;
    }

    /**
     * 
     * setter for objectGraph
     * @param objectGraph
     */

    public void setObjectGraph(String objectGraph) {
        this.objectGraph = objectGraph;
    }

    /**
     * 
     * Default Constructor
     */
    public ObjectGraphParameter() {
        super();
    }

    @Override
    protected void populate(BaseEntity baseEntity, CloneOptions cloneOptions) {
        ObjectGraphParameter objectGraphParameter = (ObjectGraphParameter) baseEntity;
        super.populate(objectGraphParameter, cloneOptions);
        objectGraphParameter.setObjectGraph(objectGraph);
    }

    @Override
    protected void populateFrom(BaseEntity baseEntity, CloneOptions cloneOptions) {
        ObjectGraphParameter objectGraphParameter = (ObjectGraphParameter) baseEntity;
        super.populateFrom(objectGraphParameter, cloneOptions);
        this.setObjectGraph(objectGraphParameter.getObjectGraph());
        this.setModuleName(objectGraphParameter.getModuleName());
    }

    @Override
    public String getDisplayName() {
        return getName();
    }
}
