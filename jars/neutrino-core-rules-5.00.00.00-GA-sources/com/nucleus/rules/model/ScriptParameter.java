package com.nucleus.rules.model;

import java.io.Serializable;

import javax.persistence.Cacheable;
import javax.persistence.Column;
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
 * ObjectGraph Parameter Entity class
 */

@Entity
@DynamicUpdate
@DynamicInsert
@Cacheable
public class ScriptParameter extends Parameter {

    private static final long serialVersionUID = 1;

    @Lob
    private String            scriptCode;

    @Transient
    private String            scriptCodeValue;

    private Integer           scriptCodeType;

    @Lob
    @Column(name = "compiledExp")
    private Serializable      compiledExpression;

    @Transient
    private boolean isScriptParameterSimulationInvoked;
    
    /**
     * @return the scriptCode
     */
    public String getScriptCode() {
        return scriptCode;
    }

    /**
     * @param scriptCode the scriptCode to set
     */
    public void setScriptCode(String scriptCode) {
        this.scriptCode = scriptCode;
    }

    /**
     * @return the scriptCodeValue
     */
    public String getScriptCodeValue() {
        return scriptCodeValue;
    }

    /**
     * @param scriptCodeValue the scriptCodeValue to set
     */
    public void setScriptCodeValue(String scriptCodeValue) {
        this.scriptCodeValue = scriptCodeValue;
    }

    @Override
    public String getDisplayName() {
        return getName();
    }

    /**
     * @return the scriptCodeType
     */
    public Integer getScriptCodeType() {
        return scriptCodeType;
    }

    public void setScriptCodeType(Integer scriptCodeType) {
        this.scriptCodeType = scriptCodeType;
    }

    /**
     * @return the compiledExpression
     */
    public Serializable getCompiledExpression() {
        return compiledExpression;
    }

    /**
     * @param compiledExpression the compiledExpression to set
     */
    public void setCompiledExpression(Serializable compiledExpression) {
        this.compiledExpression = compiledExpression;
    }

    @Override
    protected void populate(BaseEntity baseEntity, CloneOptions cloneOptions) {
        ScriptParameter scriptParameter = (ScriptParameter) baseEntity;
        super.populate(scriptParameter, cloneOptions);
        scriptParameter.setScriptCode(scriptCode);
        scriptParameter.setScriptCodeType(scriptCodeType);
    }

    @Override
    protected void populateFrom(BaseEntity baseEntity, CloneOptions cloneOptions) {
        ScriptParameter scriptParameter = (ScriptParameter) baseEntity;
        super.populateFrom(scriptParameter, cloneOptions);
        this.setScriptCode(scriptParameter.getScriptCode());
        this.setScriptCodeType(scriptParameter.getScriptCodeType());

    }
    
	public boolean isScriptParameterSimulationInvoked() {
		return this.isScriptParameterSimulationInvoked;
	}

	public void setScriptParameterSimulationInvoked(boolean isScriptParameterSimulationInvoked) {
		this.isScriptParameterSimulationInvoked = isScriptParameterSimulationInvoked;
	}
    
}
