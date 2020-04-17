package com.nucleus.rules.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Cacheable;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Transient;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.CloneOptions;

/**
 * 
 * @author Nucleus Software Exports Limited
 * CompoundParameter class
 */

@Entity
@DynamicUpdate
@DynamicInsert
@Cacheable
public class CompoundParameter extends Parameter {
    // ~ Static fields/initializers =================================================================

    private static final long         serialVersionUID = 1;

    // ~ Instance fields ============================================================================

    @Transient
    private List<ParameterExpression> expression;

    @Lob
    private String                    parameterExpression;

    public String getParameterExpression() {
        return parameterExpression;
    }

    public void setParameterExpression(String parameterExpression) {
        this.parameterExpression = parameterExpression;
    }

    /**
     * @return
     */

    public ParameterExpression getExpression() {
        if (expression == null || expression.size() == 0) {
            return null;
        }
        return expression.get(0);
    }

    /**
     * @param expression
     */

    public void setExpression(ParameterExpression expression) {
        this.expression = new ArrayList<ParameterExpression>();
        this.expression.add(expression);
    }

    /**
     * @param expression
     */

    public CompoundParameter(ParameterExpression expression) {
        this.expression = new ArrayList<ParameterExpression>();
        this.expression.add(expression);
    }

    /**
     * Default Constructor 
     */

    public CompoundParameter() {

    }

    @Override
    protected void populate(BaseEntity baseEntity, CloneOptions cloneOptions) {
        CompoundParameter compoundParameter = (CompoundParameter) baseEntity;
        super.populate(compoundParameter, cloneOptions);
        compoundParameter.setParameterExpression(parameterExpression);
    }

    @Override
    protected void populateFrom(BaseEntity baseEntity, CloneOptions cloneOptions) {
        CompoundParameter compoundParameter = (CompoundParameter) baseEntity;
        super.populateFrom(compoundParameter, cloneOptions);
        this.setParameterExpression(compoundParameter.getParameterExpression());
    }

    @Override
    public String getDisplayName() {
        return getName();
    }

}
