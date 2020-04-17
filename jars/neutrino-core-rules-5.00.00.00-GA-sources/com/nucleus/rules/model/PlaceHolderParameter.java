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
 * PlaceHolder Parameter class
 */

@Entity
@DynamicUpdate
@DynamicInsert
@Cacheable
public class PlaceHolderParameter extends ObjectGraphParameter {

    private static final long serialVersionUID = 4552243773997878361L;

    private String            contextName;

    /**
     * @return the contextName
     */
    public String getContextName() {
        return contextName;
    }

    /**
     * @param contextName the contextName to set
     */
    public void setContextName(String contextName) {
        this.contextName = contextName;
    }

    /**
     * 
     * Default Constructor
     */
    public PlaceHolderParameter() {
        super();
    }

    @Override
    protected void populate(BaseEntity baseEntity, CloneOptions cloneOptions) {
        PlaceHolderParameter placeHolderParameter = (PlaceHolderParameter) baseEntity;
        super.populate(placeHolderParameter, cloneOptions);
        placeHolderParameter.setContextName(contextName);
    }

    @Override
    protected void populateFrom(BaseEntity baseEntity, CloneOptions cloneOptions) {
        PlaceHolderParameter placeHolderParameter = (PlaceHolderParameter) baseEntity;
        super.populateFrom(placeHolderParameter, cloneOptions);
        this.setContextName(placeHolderParameter.getContextName());
    }

}
