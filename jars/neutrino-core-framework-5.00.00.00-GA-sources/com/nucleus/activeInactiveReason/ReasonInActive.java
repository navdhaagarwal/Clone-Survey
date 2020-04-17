package com.nucleus.activeInactiveReason;

import com.nucleus.core.genericparameter.entity.GenericParameter;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.Entity;

@Entity
@DynamicUpdate
@DynamicInsert
public class ReasonInActive extends GenericParameter {

}
