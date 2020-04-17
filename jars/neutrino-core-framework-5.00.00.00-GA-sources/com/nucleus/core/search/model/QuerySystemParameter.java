package com.nucleus.core.search.model;

import java.util.GregorianCalendar;

import javax.persistence.Entity;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.CloneOptions;

/**
 * 
 * @author ruchir.sachdeva
 *
 */
@Entity
@DynamicUpdate
@DynamicInsert
public class QuerySystemParameter extends QueryParam {

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
            throw new RuntimeException("Cannot evaluate System Parameter value ", e);
        }
        return null;
    }

    /**
     * 
     * Default Constructor
     */
    public QuerySystemParameter() {
        super();
    }

    @Override
    protected void populate(BaseEntity baseEntity, CloneOptions cloneOptions) {
        QuerySystemParameter systemParameter = (QuerySystemParameter) baseEntity;
        super.populate(systemParameter, cloneOptions);
        systemParameter.setSystemParameterType(systemParameterType);
    }

}
