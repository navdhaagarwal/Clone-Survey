package com.nucleus.rules.model;

import java.util.List;

import javax.persistence.*;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.annotations.Synonym;
import com.nucleus.entity.BaseEntity;
import com.nucleus.user.User;

/**
 * 
 * @author Nucleus Software Exports Limited
 * RulesAuditLog class
 */

@Entity
@DynamicUpdate
@DynamicInsert
@Inheritance(strategy = InheritanceType.JOINED)
@Synonym(grant="ALL")
@Table(indexes={@Index(name="ruleInvocationUUID_index",columnList="ruleInvocationUUID"),@Index(name="ruleId_index",columnList="ruleId"),@Index(name="ruleInvokerId_index",columnList="INVOKER_ID")})
public class RulesAuditLog extends BaseEntity {

    private static final long                  serialVersionUID = -7558705358280871288L;

    private String                             ruleInvocationUUID;

    private Long                               ruleId;

    private String                             ruleResult;

    private String                             ruleInvocationPoint;

    @OneToMany(cascade = CascadeType.REMOVE, orphanRemoval = true)
    @JoinColumn(name = "rules_audit_log_fk")
    public List<RulesAuditLogParametersValues> rulesParametersValues;

    @ManyToOne(fetch=FetchType.LAZY)
    private User                               associatedUser;

    private Long                               elapsedTime;
    
    private boolean 						   purgingRequired=true;

    @Column(name = "INVOKER_URI")
    private String                             invokerURI;
    @Column(name = "INVOKER_ID")
    private String                             invokerId;

    @Column(length = 1000)
    private String                             errorMessage;
    @Column(length = 1000)
    private String                             successMessage;


	public boolean getPurgingRequired() {
		return purgingRequired;
	}

	public void setPurgingRequired(boolean purgingRequired) {
		this.purgingRequired = purgingRequired;
	}

	/**
     * @return the associatedUser
     */
    public User getAssociatedUser() {
        return associatedUser;
    }

    /**
     * @param associatedUser the associatedUser to set
     */
    public void setAssociatedUser(User associatedUser) {
        this.associatedUser = associatedUser;
    }

    /**
    * @return the ruleInvocationPoint
    */
    public String getRuleInvocationPoint() {
        return ruleInvocationPoint;
    }

    /**
     * @param ruleInvocationPoint the ruleInvocationPoint to set
     */
    public void setRuleInvocationPoint(String ruleInvocationPoint) {
        this.ruleInvocationPoint = ruleInvocationPoint;
    }

    /**
     * @return the ruleResult
     */
    public String getRuleResult() {
        return ruleResult;
    }

    /**
     * @param ruleResult the ruleResult to set
     */
    public void setRuleResult(String ruleResult) {
        this.ruleResult = ruleResult;
    }

    /**
     * 
     * Getter for rulesParametersValues
     * @return
     */
    public List<RulesAuditLogParametersValues> getRulesParametersValues() {
        return rulesParametersValues;
    }

    /**
     * 
     * Setter for rulesParametersValues
     * @param rulesParametersValues
     */
    public void setRulesParametersValues(List<RulesAuditLogParametersValues> rulesParametersValues) {
        this.rulesParametersValues = rulesParametersValues;
    }

    /**
     * 
     * Getter for ruleInvocationUUID
     * @return
     */
    public String getRuleInvocationUUID() {
        return ruleInvocationUUID;
    }

    /**
     * 
     * Setter for ruleInvocationUUID
     * @param ruleInvocationUUID
     */
    public void setRuleInvocationUUID(String ruleInvocationUUID) {
        this.ruleInvocationUUID = ruleInvocationUUID;
    }

    /**
     * 
     * Getter for ruleId
     * @return
     */
    public Long getRuleId() {
        return ruleId;
    }

    /**
     * 
     * Setter for ruleId
     * @param ruleId
     */
    public void setRuleId(Long ruleId) {
        this.ruleId = ruleId;
    }

    /**
     * get elapsed time
     * @return
     */
    public Long getElapsedTime() {
        return elapsedTime;
    }

    /**
     * set elapsed Time
     * @param elapsedTime
     */
    public void setElapsedTime(Long elapsedTime) {
        this.elapsedTime = elapsedTime;
    }

    /**
     * Getter for property 'invokerURI'.
     *
     * @return Value for property 'invokerURI'.
     */
    public String getInvokerURI() {
        return invokerURI;
    }

    /**
     * Setter for property 'invokerURI'.
     *
     * @param invokerURI Value to set for property 'invokerURI'.
     */
    public void setInvokerURI(String invokerURI) {
        this.invokerURI = invokerURI;
    }

    /**
     * Getter for property 'invokerId'.
     *
     * @return Value for property 'invokerId'.
     */
    public String getInvokerId() {
        return invokerId;
    }

    /**
     * Setter for property 'invokerId'.
     *
     * @param invokerId Value to set for property 'invokerId'.
     */
    public void setInvokerId(String invokerId) {
        this.invokerId = invokerId;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getSuccessMessage() {
        return successMessage;
    }

    public void setSuccessMessage(String successMessage) {
        this.successMessage = successMessage;
    }
}
