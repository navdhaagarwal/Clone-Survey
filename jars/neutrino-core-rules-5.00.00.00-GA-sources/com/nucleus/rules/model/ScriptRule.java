package com.nucleus.rules.model;

import javax.persistence.Cacheable;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Transient;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.CloneOptions;

/**
 * Represents a Rule in the system.
 * @author Nucleus Software Exports Limited
 */

@Entity
@DynamicUpdate
@DynamicInsert
@Cacheable
public class ScriptRule extends Rule {
    // ~ Static fields/initializers =================================================================

    private static final long serialVersionUID = 1;

    // ~ Instance fields ============================================================================

    @Lob
    private String            scriptCode;

    @Transient
    private String            scriptCodeValue;

    private Integer           scriptCodeType;

    /**
     * @return the scriptCodeType
     */
    public int getScriptCodeType() {
        return scriptCodeType;
    }

    /**
     * @param scriptCodeType the scriptCodeType to set
     */
    public void setScriptCodeType(int scriptCodeType) {
        this.scriptCodeType = scriptCodeType;
    }

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
    protected void populate(BaseEntity baseEntity, CloneOptions cloneOptions) {
        ScriptRule rule = (ScriptRule) baseEntity;
        super.populate(rule, cloneOptions);
        rule.setScriptCode(scriptCode);
        rule.setScriptCodeType(scriptCodeType);
        rule.setScriptCodeValue(scriptCodeValue);
    }

    @Override
    protected void populateFrom(BaseEntity baseEntity, CloneOptions cloneOptions) {
        ScriptRule rule = (ScriptRule) baseEntity;
        super.populateFrom(rule, cloneOptions);
        this.setScriptCode(rule.getScriptCode());
        this.setScriptCodeType(rule.getScriptCodeType());
        this.setScriptCodeValue(rule.getScriptCodeValue()); 
    }

}