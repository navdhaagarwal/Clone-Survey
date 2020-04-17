package com.nucleus.rules.model;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;

import com.nucleus.cas.parentChildDeletionHandling.DeletionPreValidator;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.annotations.Sortable;
import com.nucleus.core.annotations.Synonym;
import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.CloneOptions;
import com.nucleus.master.BaseMasterEntity;

/**
 * 
 * @author Nucleus Software Exports Limited
 */
@Entity
@DynamicUpdate
@DynamicInsert
@Cacheable
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Synonym(grant="ALL")
@DeletionPreValidator
public class RuleAction extends BaseMasterEntity {

    private static final long serialVersionUID = 1L;

    @Sortable(index = 1)
    private String            name;

    private String            description;

    private String            sourceProduct;

    @Lob
    @Column(name = "compiledExp")
    private Serializable      compiledExpression;

    @ManyToMany
    @JoinColumn(name = "runtime_parameter_fk")
    @Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
    private Set<Parameter>    parameters;

    /**
     * 
     * Getter for description property
     * @return
     */
    public String getDescription() {
        return description;
    }

    /**
     * 
     * Setter for description property
     * @param description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * 
     * Getter for sourceProduct property
     * @return
     */
    public String getSourceProduct() {
        return sourceProduct;
    }

    /**
     * 
     * Setter for sourceProduct property
     * @param sourceProduct
     */
    public void setSourceProduct(String sourceProduct) {
        this.sourceProduct = sourceProduct;
    }

    /**
     * 
     * Getter for name property
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * 
     * Setter for name property
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getDisplayName() {
        return getName();
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

    @Override
    protected void populate(BaseEntity baseEntity, CloneOptions cloneOptions) {
        RuleAction ruleAction = (RuleAction) baseEntity;
        super.populate(ruleAction, cloneOptions);
        ruleAction.setName(name);
        ruleAction.setSourceProduct(sourceProduct);
        ruleAction.setDescription(description);
        ruleAction.setCompiledExpression(compiledExpression);

        Set<Parameter> orgPrametersSet = this.getParameters();

        if (orgPrametersSet != null && orgPrametersSet.size() > 0) {
            Set<Parameter> clonedPrametersSet = new HashSet<Parameter>();
            for (Parameter param : orgPrametersSet) {
                clonedPrametersSet.add(param);
            }
            ruleAction.setParameters(clonedPrametersSet);
        }
    }

    @Override
    protected void populateFrom(BaseEntity baseEntity, CloneOptions cloneOptions) {
        RuleAction ruleAction = (RuleAction) baseEntity;
        super.populateFrom(ruleAction, cloneOptions);
        this.setName(ruleAction.getName());
        this.setSourceProduct(ruleAction.getSourceProduct());
        this.setDescription(ruleAction.getDescription());

        this.setCompiledExpression(ruleAction.getCompiledExpression());

        this.setParameters((ruleAction.getParameters() != null && ruleAction.getParameters().size() > 0) ? ruleAction
                .getParameters() : null);
    }

}
