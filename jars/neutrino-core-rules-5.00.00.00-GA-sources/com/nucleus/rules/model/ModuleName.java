package com.nucleus.rules.model;

import javax.persistence.Entity;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.genericparameter.entity.GenericParameter;

/**
 * 
 * @author Nucleus Software Exports Limited
 * Object Graph  Module Name
 */
@Entity
@DynamicUpdate
@DynamicInsert
public class ModuleName extends GenericParameter {

    private static final long serialVersionUID = -6860804476026902444L;

}
