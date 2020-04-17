package com.nucleus.core.formsConfiguration;

import javax.persistence.Cacheable;
import javax.persistence.Entity;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.genericparameter.entity.GenericParameter;

/**
 * Constants for Form Configuration Mapping
 */
@Entity
@DynamicUpdate
@DynamicInsert
@Cacheable
public class FormConfigInvocMapping extends GenericParameter {

    private static final long  serialVersionUID               = 8367136165014143098L;

    public static final String FORM_CONFIGURATION_MAPPING_FI  = "FI";

    public static final String FORM_CONFIGURATION_MAPPING_DDE = "DDE";

    public static final String FORM_CONFIGURATION_MAPPING_QDE = "QDE";
}
