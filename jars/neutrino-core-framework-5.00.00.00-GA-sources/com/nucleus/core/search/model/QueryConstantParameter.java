package com.nucleus.core.search.model;

import java.math.BigDecimal;

import javax.persistence.Entity;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.CloneOptions;
import com.nucleus.logging.BaseLoggers;

/**
 * 
 * @author Nucleus Software Exports Limited
 * ConstantParameter Entity class
 * For Calculating constants
 */
@Entity
@DynamicUpdate
@DynamicInsert
public class QueryConstantParameter extends QueryParam {

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
            if (super.getDataType() == QueryParameterDataType.PARAMETER_DATA_TYPE_STRING) {
                return literal;
            }

            if (super.getDataType() == QueryParameterDataType.PARAMETER_DATA_TYPE_INTEGER) {
                return Integer.parseInt(literal);
            }

            if (super.getDataType() == QueryParameterDataType.PARAMETER_DATA_TYPE_NUMBER) {
                return new BigDecimal(literal).doubleValue();
            }

            if (super.getDataType() == QueryParameterDataType.PARAMETER_DATA_TYPE_BOOLEAN) {
                return Boolean.parseBoolean(literal);
            }

            // if (super.getDataType() == ParameterDataType.PARAMETER_DATA_TYPE_DATE) {
            //
            // DateFormat formatter;
            // Date date;
            // formatter = new SimpleDateFormat(RuleConstants.DATE_PATTERN);
            // date = (Date) formatter.parse(literal);
            //
            // return date.getTime();
            // }
            //
            // if (super.getDataType() == ParameterDataType.PARAMETER_DATA_TYPE_YEARS) {
            // return RuleConstants.MILLISECONDS_IN_YEAR * Integer.parseInt(literal);
            // }

        } catch (Exception e) {
           BaseLoggers.exceptionLogger.error(e.getMessage());
        }
        return null;
    }

    /**
     * 
     * Default Constructor
     */
    public QueryConstantParameter() {
        super();

    }

    @Override
    protected void populate(BaseEntity baseEntity, CloneOptions cloneOptions) {
        QueryConstantParameter constantParameter = (QueryConstantParameter) baseEntity;
        super.populate(constantParameter, cloneOptions);
        constantParameter.setLiteral(literal);
    }

}
