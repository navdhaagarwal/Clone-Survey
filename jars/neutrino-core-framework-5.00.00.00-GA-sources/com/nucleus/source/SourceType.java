package com.nucleus.source;

import javax.persistence.Entity;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.genericparameter.entity.GenericParameter;

@Entity
@DynamicUpdate
@DynamicInsert
public class SourceType extends GenericParameter {

    private static final long serialVersionUID = 1L;
    public static final String Source_Type_Friends = "Friends / Relatives";
    public static final String Source_Type_Advertisements = "Advertisements";
    public static final String Source_Type_SocialMedia = "Social Media";
    public static final String Source_Type_CallCenter = "Call Center";
    
    
    
}
