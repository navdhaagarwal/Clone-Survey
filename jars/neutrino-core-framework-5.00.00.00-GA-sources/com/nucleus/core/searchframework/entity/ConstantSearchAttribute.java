/**
 * This file and a proportion of its content is copyright of Nucleus Software Exports Limited - � 2012. All rights reserved.
 * Any redistribution or reproduction of part or all of the contents in any form is prohibited other than the following:
 * - you cannot print or download to a local hard disk extract contents either part or full for personal/ commercial/
 * academic or any other use
 * - you may not copy the content to individual/ third parties for any type of use, either as compiled or source format
 * without the knowledge and consent of Nucleus Software
 * - You may not, except with our express written permission, distribute or commercially exploit the content. Nor may you
 * transmit it or store it in any other web site or other form of electronic retrieval system.
 */
package com.nucleus.core.searchframework.entity;

import java.math.BigDecimal;

import javax.persistence.Entity;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.CloneOptions;
import com.nucleus.entity.EntityId;
import com.nucleus.logging.BaseLoggers;

/**
 * @author Nucleus Software Exports Limited
 * 
 */
@Entity
@DynamicUpdate
@DynamicInsert
public class ConstantSearchAttribute extends SearchAttribute {

    // ~ Static fields/initializers =================================================================

    private static final long serialVersionUID = -5265654164895690972L;

    // ~ Instance fields ============================================================================

    private String            literal;
    /**
     * Default Constructor
     */
    public ConstantSearchAttribute() {
        super();
    }
    /**
     * Getter method for literal property 
     * @return
     */
    public String getLiteral() {
        return literal;
    }
    /**
     * Setter method for literal property
     * @param literal
     */
    public void setLiteral(String literal) {
        this.literal = literal;
    }
    /**
     * Comparing the literal with the matched Data Type and returning the value
     * @return
     */
    public Object getLiteralValue() {
        try {
            if (super.getDataType() == SearchAttributeDataType.PARAMETER_DATA_TYPE_STRING) {
                return literal;
            }
            if (super.getDataType() == SearchAttributeDataType.PARAMETER_DATA_TYPE_INTEGER) {
                return Integer.parseInt(literal);
            }
            if (super.getDataType() == SearchAttributeDataType.PARAMETER_DATA_TYPE_LONG) {
                return Long.parseLong(literal);
            }
            if (super.getDataType() == SearchAttributeDataType.PARAMETER_DATA_TYPE_NUMBER) {
                return new BigDecimal(literal).doubleValue();
            }
            if (super.getDataType() == SearchAttributeDataType.PARAMETER_DATA_TYPE_BOOLEAN) {
                return Boolean.parseBoolean(literal);
            }
            if (super.getDataType() == SearchAttributeDataType.PARAMETER_DATA_TYPE_DATE) {
            	DateTimeFormatter formatter = DateTimeFormat.forPattern(SearchAttributeDataType.DATE_PATTERN);
            	return formatter.parseDateTime(literal);            	
            }
            if (super.getDataType() == SearchAttributeDataType.PARAMETER_DATA_TYPE_REFERENCE) {
                return EntityId.fromUri(literal);
            }

        } catch (Exception e) {
            BaseLoggers.exceptionLogger.error(e.getMessage());
        }
        return null;
    }
   
    @Override
    protected void populate(BaseEntity baseEntity, CloneOptions cloneOptions) {
        ConstantSearchAttribute constantSearchAttribute = (ConstantSearchAttribute) baseEntity;
        super.populate(constantSearchAttribute, cloneOptions);
        constantSearchAttribute.setLiteral(literal);
    }
}
