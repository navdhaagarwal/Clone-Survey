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
 * Null Parameter type class
 */

@Entity
@DynamicUpdate
@DynamicInsert
@Cacheable
public class NullParameter extends Parameter {

    private static final long serialVersionUID = -6048591625010596525L;

    /**
     * @return
     */

    public Object getNullParameterValue() {
        return null;
    }

    /**
     * 
     * default constructor
     */

    public NullParameter() {
        super();
    }

    @Override
    protected void populate(BaseEntity baseEntity, CloneOptions cloneOptions) {
        NullParameter nullParameter = (NullParameter) baseEntity;
        super.populate(nullParameter, cloneOptions);
    }
}
