package com.nucleus.address;

import com.nucleus.core.genericparameter.entity.GenericParameter;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.Cacheable;
import javax.persistence.Entity;

@Entity
@DynamicInsert
@DynamicUpdate
@Cacheable
public class AddressGeneric extends GenericParameter {
    private static final long serialVersionUID = 1L;

    public AddressGeneric() {
    }
}
