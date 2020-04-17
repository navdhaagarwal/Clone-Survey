package com.nucleus.core.genericparameter.entity;

import javax.persistence.Entity;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

@Entity
@DynamicUpdate
@DynamicInsert
public class Channel extends GenericParameter {

    private static final long serialVersionUID = -7557390363219595646L;
    
    public Channel(){
        super();
    }

}
