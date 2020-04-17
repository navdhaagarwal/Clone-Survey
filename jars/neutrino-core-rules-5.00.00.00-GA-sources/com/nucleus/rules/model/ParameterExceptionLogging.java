package com.nucleus.rules.model;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.Entity;

@Entity
@DynamicInsert
@DynamicUpdate
public class ParameterExceptionLogging extends RuleEngineExceptionLogging {
}
