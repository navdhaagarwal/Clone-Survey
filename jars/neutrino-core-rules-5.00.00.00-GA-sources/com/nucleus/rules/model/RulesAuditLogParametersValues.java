package com.nucleus.rules.model;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.annotations.Synonym;
import com.nucleus.entity.BaseEntity;

/**
 * 
 * @author Nucleus Software Exports Limited
 * RulesAuditLogParametersValues class
 */

@Entity
@DynamicUpdate
@DynamicInsert
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "rules_audit_log_param_val")
@Synonym(grant="ALL")
public class RulesAuditLogParametersValues extends BaseEntity {

    private static final long serialVersionUID = 6160445045200717316L;

    @OneToOne(fetch=FetchType.LAZY)
    private Parameter         parameterId;

    private String            parameterValue;

    /**
     * 
     * Getter for parameterId
     * @return
     */
    public Parameter getParameterId() {
        return parameterId;
    }

    /**
     * 
     * Setter for parameterId
     * @param parameterId
     */
    public void setParameterId(Parameter parameterId) {
        this.parameterId = parameterId;
    }

    /**
     * 
     * Getter for parameterValue
     * @return
     */
    public String getParameterValue() {
        return parameterValue;
    }

    /**
     * 
     * Setter for parameterValue
     * @param parameterValue
     */
    public void setParameterValue(String parameterValue) {
        this.parameterValue = parameterValue;
    }

}
