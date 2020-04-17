package com.nucleus.user;

import com.nucleus.core.genericparameter.entity.GenericParameter;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.Entity;


@Entity
@DynamicUpdate
@DynamicInsert
public class AccessType extends GenericParameter {

    public static final String INTERNET="Internet";
    public static final String INTRANET="Intranet";
    public static final String BOTH="Both";

    @Override
    public String toString(){
        return this.getCode();
    }
}
