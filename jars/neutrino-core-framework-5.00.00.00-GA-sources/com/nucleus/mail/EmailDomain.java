package com.nucleus.mail;

import javax.persistence.Entity;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.genericparameter.entity.GenericParameter;

@Entity
@DynamicUpdate
@DynamicInsert
public class EmailDomain extends GenericParameter {

    private static final long serialVersionUID = 7479151583960522098L;

}
