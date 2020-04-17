package com.nucleus.rules.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.CloneOptions;

// TODO: Auto-generated Javadoc
/**
 * The Class DerivedParameter.
 *
 * @author Nucleus Software Exports Limited
 * Derived Parameter Entity class
 */

@Entity
@DynamicUpdate
@DynamicInsert
@Cacheable
public class DerivedParameter extends ScriptParameter {

    /** The Constant serialVersionUID. */
    private static final long                serialVersionUID = 1L;

    /** The filter criterias. */
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    
    @Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
    @JoinTable(name="RE_PARAMETERS_FILTER_CRITERIAS", joinColumns= {@JoinColumn(name="RE_PARAMETERS", referencedColumnName = "ID")},
	inverseJoinColumns = {@JoinColumn(name="FILTER_CRITERIAS", referencedColumnName = "ID")})
    private List<DerivedParamFilterCriteria> filterCriterias;

    /** The aggregate function. */
    private String                           aggregateFunction;

    /** The target object graph. */
    @ManyToOne
    private ObjectGraphParameter             targetObjectGraph;

    /** True when return value is an entity field */
    private Boolean                          entityField;

    /** True when entityField is false and validation is done on all elements  */
    private Boolean                          validateOnAll;

    /**
     * getter for whereExpression.
     *
     * @return the filter criterias
     */

    public List<DerivedParamFilterCriteria> getFilterCriterias() {
        return filterCriterias;
    }

    /**
     * setter for whereExpression.
     *
     * @param filterCriterias the new filter criterias
     */

    public void setFilterCriterias(List<DerivedParamFilterCriteria> filterCriterias) {
        this.filterCriterias = filterCriterias;
    }

    /**
     * getter for aggregateFunction.
     *
     * @return the aggregate function
     */

    public String getAggregateFunction() {
        return aggregateFunction;
    }

    /**
     * setter for aggregateFunction.
     *
     * @param aggregateFunction the new aggregate function
     */

    public void setAggregateFunction(String aggregateFunction) {
        this.aggregateFunction = aggregateFunction;
    }

    /**
     * getter for targetObjectGraph.
     *
     * @return the target object graph
     */

    public ObjectGraphParameter getTargetObjectGraph() {
        return targetObjectGraph;
    }

    /**
     * setter for targetObjectGraph.
     *
     * @param targetObjectGraph the new target object graph
     */

    public void setTargetObjectGraph(ObjectGraphParameter targetObjectGraph) {
        this.targetObjectGraph = targetObjectGraph;
    }

    /**
     * @return the entityField
     */
    public Boolean getEntityField() {
        return entityField;
    }

    /**
     * @param entityField the entityField to set
     */
    public void setEntityField(Boolean entityField) {
        this.entityField = entityField;
    }

    /**
     * @return the validateOnAll
     */
    public Boolean getValidateOnAll() {
        return validateOnAll;
    }

    /**
     * @param validateOnAll the validateOnAll to set
     */
    public void setValidateOnAll(Boolean validateOnAll) {
        this.validateOnAll = validateOnAll;
    }

    /* (non-Javadoc) @see com.nucleus.rules.model.ScriptParameter#populate(com.nucleus.entity.BaseEntity, com.nucleus.entity.CloneOptions) */
    @Override
    protected void populate(BaseEntity baseEntity, CloneOptions cloneOptions) {
        DerivedParameter derivedParameter = (DerivedParameter) baseEntity;
        super.populate(derivedParameter, cloneOptions);
        derivedParameter.setAggregateFunction(aggregateFunction);
        derivedParameter.setTargetObjectGraph(targetObjectGraph);
        derivedParameter.setEntityField(entityField);
        derivedParameter.setValidateOnAll(validateOnAll);
        if (filterCriterias != null && filterCriterias.size() > 0) {
            List<DerivedParamFilterCriteria> criterias = new ArrayList<DerivedParamFilterCriteria>();
            for (DerivedParamFilterCriteria derivedParamFilterCriteria : filterCriterias) {
                criterias.add((DerivedParamFilterCriteria) derivedParamFilterCriteria.cloneYourself(cloneOptions));
            }
            derivedParameter.setFilterCriterias(criterias);
        }
    }

    /* (non-Javadoc) @see com.nucleus.rules.model.ScriptParameter#populateFrom(com.nucleus.entity.BaseEntity, com.nucleus.entity.CloneOptions) */
    @Override
    protected void populateFrom(BaseEntity baseEntity, CloneOptions cloneOptions) {
        DerivedParameter derivedParameter = (DerivedParameter) baseEntity;
        super.populateFrom(derivedParameter, cloneOptions);
        this.setAggregateFunction(derivedParameter.getAggregateFunction());
        if (derivedParameter.getFilterCriterias() != null && derivedParameter.getFilterCriterias().size() > 0) {
            if (this.getFilterCriterias() != null) {
                this.getFilterCriterias().clear();
            } else {
                this.setFilterCriterias(new ArrayList<DerivedParamFilterCriteria>());
            }
            for (DerivedParamFilterCriteria derivedParamFilterCriteria : derivedParameter.getFilterCriterias()) {
                this.getFilterCriterias().add(
                        (DerivedParamFilterCriteria) derivedParamFilterCriteria.cloneYourself(cloneOptions));
            }
        }
        this.setTargetObjectGraph(derivedParameter.getTargetObjectGraph());
        this.setEntityField(derivedParameter.getEntityField());
        this.setValidateOnAll(derivedParameter.getValidateOnAll());

    }

}
