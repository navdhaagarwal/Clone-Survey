package com.nucleus.core.feedback.entity;

import javax.persistence.Entity;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.genericparameter.entity.GenericParameter;

@Entity
@DynamicUpdate
@DynamicInsert
public class FeedbackType extends GenericParameter{

    private static final long serialVersionUID = -669176368168975107L;

    public static final String QUESTION = "Feeback.question";    
    public static final String PRAISE = "Feeback.praise";
    public static final String IDEA = "Feeback.idea";
    public static final String PROBLEM = "PROBLEM";
        

}
