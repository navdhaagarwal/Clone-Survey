package com.nucleus.rules.model;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.persistence.Cacheable;
import javax.persistence.Entity;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.CloneOptions;
import com.nucleus.rules.exception.RuleException;
import com.nucleus.rules.service.RuleConstants;

/**
 * 
 * @author Nucleus Software Exports Limited
 * ConstantParameter Entity class
 * For Calculating constants
 */

@Entity
@DynamicUpdate
@DynamicInsert
@Cacheable
public class ConstantParameter extends Parameter {

    private static final long serialVersionUID = 1;

    private String            literal;

    /**
     * 
     * Getter method for literal property 
     * @return
     */
    public String getLiteral() {
        return literal;
    }

    /**
     * 
     * Setter method for literal property
     * @param literal
     */
    public void setLiteral(String literal) {
        this.literal = literal;
    }

    /**
     * 
     * Comparing the literal with the matched Data Type and returning the value
     * @return
     */

    public Object getLiteralValue() {
        try {
            if (super.getDataType() == ParameterDataType.PARAMETER_DATA_TYPE_STRING) {
                return literal;
            }

            if (super.getDataType() == ParameterDataType.PARAMETER_DATA_TYPE_INTEGER) {
                return Long.parseLong(literal);
            }

            if (super.getDataType() == ParameterDataType.PARAMETER_DATA_TYPE_NUMBER) {
                return new BigDecimal(literal).doubleValue();
            }

            if (super.getDataType() == ParameterDataType.PARAMETER_DATA_TYPE_BOOLEAN) {
                return Boolean.parseBoolean(literal);
            }

            if (super.getDataType() == ParameterDataType.PARAMETER_DATA_TYPE_DATE || 
            		super.getDataType() == ParameterDataType.PARAMETER_DATA_TYPE_JAVA_UTIL_DATE) {

                DateFormat formatter;
                Date date;
                formatter = new SimpleDateFormat(RuleConstants.DATE_PATTERN);
                date = formatter.parse(literal);

                return date.getTime();
            }

        } catch (Exception e) {
            throw new RuleException("Cannot evaluate Constant Parameter :: " + super.getName() + " with parameter value ::"
                    + literal, e);
        }
        return null;
    }

    /**
     * 
     * Default Constructor
     */
    public ConstantParameter() {
        super();

    }

    @Override
    protected void populate(BaseEntity baseEntity, CloneOptions cloneOptions) {
        ConstantParameter constantParameter = (ConstantParameter) baseEntity;
        super.populate(constantParameter, cloneOptions);
        constantParameter.setLiteral(literal);
    }

    @Override
    protected void populateFrom(BaseEntity baseEntity, CloneOptions cloneOptions) {
        ConstantParameter constantParameter = (ConstantParameter) baseEntity;
        super.populateFrom(constantParameter, cloneOptions);
        this.setLiteral(constantParameter.getLiteral());
    }

    @Override
    public String getDisplayName() {
        return getName();
    }
}
