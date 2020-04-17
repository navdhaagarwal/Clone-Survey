package com.nucleus.rules.model;

import java.util.GregorianCalendar;

import javax.persistence.Cacheable;
import javax.persistence.Entity;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.CloneOptions;
import com.nucleus.rules.exception.RuleException;

/**
 * 
 * @author Nucleus Software Exports Limited
 * System Parameters
 */

@Entity
@DynamicUpdate
@DynamicInsert
@Cacheable
public class SystemParameter extends Parameter {

    private static final long serialVersionUID = 1;

    private int               systemParameterType;

    /**
     * 
     * Getter for the SystemParameterType property
     * 
     * @return
     */
    public int getSystemParameterType() {
        return systemParameterType;
    }

    /**
     * 
     * Setter for the SystemParameterType property
     * 
     * @param dataType
     */
    public void setSystemParameterType(int systemParameterType) {
        this.systemParameterType = systemParameterType;
    }

    /**
     * 
     * Comparing the systemParameterType with the matched System Parameter Type
     * and returning the value
     * 
     * @return
     */

    public Object getSystemParameterValue() {
        try {
            if (systemParameterType == SystemParameterType.SYSTEM_PARAMETER_TYPE_CURRENT_DATE) {
                return new GregorianCalendar().getTimeInMillis();
            }
        } catch (Exception e) {
            throw new RuleException("Cannot evaluate System Parameter value ", e);
        }
        return null;
    }

    /**
     * 
     * Default Constructor
     */
    public SystemParameter() {
        super();
    }

    @Override
    protected void populate(BaseEntity baseEntity, CloneOptions cloneOptions) {
        SystemParameter systemParameter = (SystemParameter) baseEntity;
        super.populate(systemParameter, cloneOptions);
        systemParameter.setSystemParameterType(systemParameterType);
    }

}
