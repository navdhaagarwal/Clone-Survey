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
package com.nucleus.rules.model;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.annotations.Synonym;
import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.CloneOptions;

/**
 * @author Nucleus Software Exports Limited
 * RuntimeRuleMapping class
 */

@Entity
@DynamicUpdate
@DynamicInsert
@Cacheable
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Table(name = "runtime_rule_map")
@Synonym(grant="ALL")
public class RuntimeRuleMapping extends BaseEntity {

    private static final long serialVersionUID = -5658617196379117754L;

    @ManyToMany
    @JoinColumn(name = "runtime_parameter_fk")
    @JoinTable(name="RUNTIME_RULE_MAP_PARAMETERS", joinColumns= {@JoinColumn(name="RUNTIME_RULE_MAP", referencedColumnName = "ID")},
   	inverseJoinColumns = {@JoinColumn(name="PARAMETERS", referencedColumnName = "ID")})
    @Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
    private Set<Parameter>    parameters;

    @ElementCollection
    @JoinTable(name = "runtime_ogparam_in_rule")
    @Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
    private Set<String>       objectGraphs;

    @Lob
    @Column(name = "compiledExp")
    private Serializable      compiledExpression;

    @Lob
    @Column(name = "compiledNullSafeExp")
    private Serializable      compiledNullSafeExpression;

    /**
     * @return the compiledNullSafeExpression
     */
    public Serializable getCompiledNullSafeExpression() {
        return compiledNullSafeExpression;
    }

    /**
     * @param compiledNullSafeExpression the compiledNullSafeExpression to set
     */
    public void setCompiledNullSafeExpression(Serializable compiledNullSafeExpression) {
        this.compiledNullSafeExpression = compiledNullSafeExpression;
    }

    /**
     * @return the compiledExp
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

    /**
     * @return the objectGraph
     */
    public Set<String> getObjectGraphs() {
        return objectGraphs;
    }

    /**
     * @param objectGraph the objectGraph to set
     */
    public void setObjectGraphs(Set<String> objectGraphs) {
        this.objectGraphs = objectGraphs;
    }

    /**
     * @return the parameters
     */
    public Set<Parameter> getParameters() {
        return parameters;
    }

    /**
     * @param parameters the parameters to set
     */
    public void setParameters(Set<Parameter> parameters) {
        this.parameters = parameters;
    }

    @Override
    protected void populate(BaseEntity baseEntity, CloneOptions cloneOptions) {
        RuntimeRuleMapping runtimeRuleMapping = (RuntimeRuleMapping) baseEntity;
        super.populate(runtimeRuleMapping, cloneOptions);

        Set<Parameter> orgPrametersSet = this.getParameters();

        if (orgPrametersSet != null && orgPrametersSet.size() > 0) {
            Set<Parameter> clonedPrametersSet = new HashSet<Parameter>();
            for (Parameter param : orgPrametersSet) {
                clonedPrametersSet.add(param);
            }
            runtimeRuleMapping.setParameters(clonedPrametersSet);
        }

        runtimeRuleMapping
                .setObjectGraphs((getObjectGraphs() != null && getObjectGraphs().size() > 0) ? new HashSet<String>(
                        getObjectGraphs()) : null);
        runtimeRuleMapping.setCompiledExpression(compiledExpression);
        runtimeRuleMapping.setCompiledNullSafeExpression(compiledNullSafeExpression);

    }

    @Override
    protected void populateFrom(BaseEntity baseEntity, CloneOptions cloneOptions) {
        RuntimeRuleMapping runtimeRuleMapping = (RuntimeRuleMapping) baseEntity;
        super.populateFrom(runtimeRuleMapping, cloneOptions);
        this.setCompiledExpression(runtimeRuleMapping.getCompiledExpression());
        this.setCompiledNullSafeExpression(runtimeRuleMapping.getCompiledNullSafeExpression());

        this.setParameters((runtimeRuleMapping.getParameters() != null && runtimeRuleMapping.getParameters().size() > 0) ? runtimeRuleMapping
                .getParameters() : null);

        this.setObjectGraphs((runtimeRuleMapping.getObjectGraphs() != null && runtimeRuleMapping.getObjectGraphs().size() > 0) ? new HashSet<String>(
                runtimeRuleMapping.getObjectGraphs()) : null);

    }
}
