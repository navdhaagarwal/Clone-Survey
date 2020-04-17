package com.nucleus.rules.model.assignmentMatrix;

import javax.persistence.Cacheable;
import javax.persistence.Entity;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

/**
 * class to store simple expression based AssignmentSet
 * 
 * @author Nucleus Software Exports Limited
 */

@Entity
@DynamicUpdate
@DynamicInsert
@Cacheable
public class AssignmentExpression extends AssignmentSet {

    private static final long serialVersionUID = 1L;

}
