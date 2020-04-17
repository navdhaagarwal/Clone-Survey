package com.nucleus.entity;

import java.io.Serializable;

public interface Entity extends Serializable {

    public Serializable getId();

    public void setId(Serializable id);

    public EntityId getEntityId();

    public String getUri();

    /**
     * Make the entity load all the lazy-fetch fields
     */
    public void loadLazyFields();

    public Entity cloneYourself(CloneOptions cloneOptions);
    // public Field[] getEntityFields();

}