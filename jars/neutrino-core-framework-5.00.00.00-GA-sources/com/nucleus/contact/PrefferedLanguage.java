package com.nucleus.contact;

import javax.persistence.Entity;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.genericparameter.entity.GenericParameter;

@Entity
@DynamicUpdate
@DynamicInsert
public class PrefferedLanguage extends GenericParameter {

    private static final long  serialVersionUID            = 364800915905928417L;

    public static final String PREFFERED_LANGUAGE_ENGLISH  = "ENGLISH";
    public static final String PREFFERED_LANGUAGE_JAPANESE = "JAPANESE";
    public static final String PREFFERED_LANGUAGE_FRENCH   = "FRENCH";

}
