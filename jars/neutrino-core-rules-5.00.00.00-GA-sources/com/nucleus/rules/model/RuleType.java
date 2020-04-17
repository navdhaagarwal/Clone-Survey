package com.nucleus.rules.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.persistence.Entity;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.genericparameter.entity.GenericParameter;

/**
 * @author Nucleus Software Exports Limited
 * Constants for Rule type
 */
@Entity
@DynamicUpdate
@DynamicInsert
public class RuleType extends GenericParameter {

    private static final long         serialVersionUID           = 1839708468307241389L;

    public static final int           RULE_TYPE_EXPRESSION_BASED = 1;

    public static final int           RULE_TYPE_SCRIPT_BASED     = 2;
    
    public static final int           RULE_TYPE_SQL_BASED     = 3;

    public static final List<Integer> ALL_STATUSES               = Collections
                                                                         .unmodifiableList(Arrays.asList(
                                                                                 RULE_TYPE_EXPRESSION_BASED,
                                                                                 RULE_TYPE_SCRIPT_BASED,
                                                                                 RULE_TYPE_SQL_BASED));
}