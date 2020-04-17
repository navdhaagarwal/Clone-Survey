package com.nucleus.rules.model;

import javax.persistence.Cacheable;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.CloneOptions;
import com.nucleus.entity.EntityId;

/**
 * 
 * @author Nucleus Software Exports Limited
 * ReferenceParameter Entity class
 */

@Entity
@DynamicUpdate
@DynamicInsert
@Cacheable
public class ReferenceParameter extends Parameter {

    private static final long serialVersionUID = 1;

    private String            referenceURI;

    @ManyToOne
    private EntityType        entityType;

    /**
     * 
     * Getter for entityType property
     * @return
     */
    public EntityType getEntityType() {
        return entityType;
    }

    /**
     * 
     * Setter for entityType property
     * @param entityType
     */
    public void setEntityType(EntityType entityType) {
        this.entityType = entityType;
    }

    /**
     * 
     * Getter method for referenceURI property
     * @return
     */
    public EntityId getReferenceEntityId() {
        return EntityId.fromUri(referenceURI);
    }

    /**
     * 
     * Setter method for referenceURI property
     * @param referenceEntityUri
     */
    public void setReferenceEntityId(EntityId referenceEntityUri) {
        this.referenceURI = referenceEntityUri.getUri();
    }

    public ReferenceParameter() {
        super();
    }

    @Override
    protected void populate(BaseEntity baseEntity, CloneOptions cloneOptions) {
        ReferenceParameter referenceParameter = (ReferenceParameter) baseEntity;
        super.populate(referenceParameter, cloneOptions);
        referenceParameter.setReferenceEntityId(EntityId.fromUri(referenceURI));
    }

    @Override
    protected void populateFrom(BaseEntity baseEntity, CloneOptions cloneOptions) {
        ReferenceParameter referenceParameter = (ReferenceParameter) baseEntity;
        super.populateFrom(referenceParameter, cloneOptions);
        this.setReferenceEntityId(EntityId.fromUri(referenceParameter.getReferenceEntityId().getUri()));
    }

    @Override
    public String getDisplayName() {
        return getName();
    }

    /**
     * 
     * Added to display the selected value
     * in reference parameter page
     * @return
     */
    public String getClassName() {
        String className = "";

        if (!StringUtils.isBlank(referenceURI)) {
            className = referenceURI.split(":")[0];
        }

        return className;
    }
}
