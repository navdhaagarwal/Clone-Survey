package com.nucleus.rules.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.CloneOptions;

/**
 * 
 * @author Nucleus Software Exports Limited
 * Query Parameter Entity class
 */

@Entity
@DynamicUpdate
@DynamicInsert
@Cacheable
public class QueryParameter extends Parameter {

    private static final long             serialVersionUID = 1;

    @Column(length = 4000)
    private String                        query;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "parametersEntity")
    @Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
    private List<QueryParameterAttribute> queryParameterAttributes;

    @Transient
    private List<QueryParameterAttribute> tempQueryParamAttribute;

    /**
     * @return the tempQueryParamAttribute
     */
    public List<QueryParameterAttribute> getTempQueryParamAttribute() {
        return tempQueryParamAttribute;
    }

    /**
     * @param tempQueryParamAttribute the tempQueryParamAttribute to set
     */
    public void setTempQueryParamAttribute(List<QueryParameterAttribute> tempQueryParamAttribute) {
        this.tempQueryParamAttribute = tempQueryParamAttribute;
    }

    /**
     * 
     * Getter for the query property
     * @return
     */
    public String getQuery() {
        return query;
    }

    /**
     * 
     * Setter for the query property
     * @param query
     */
    public void setQuery(String query) {
        this.query = query;
    }

    /**
     * 
     * Default Constructor
     */
    public QueryParameter() {
        super();
    }

    @Override
    protected void populate(BaseEntity baseEntity, CloneOptions cloneOptions) {
        QueryParameter queryParameter = (QueryParameter) baseEntity;
        super.populate(queryParameter, cloneOptions);
        queryParameter.setQuery(query);

        if (queryParameterAttributes != null && queryParameterAttributes.size() > 0) {
            for (QueryParameterAttribute queryParameterAttr : queryParameterAttributes) {
                queryParameter.addToQueryParameterAttributes((QueryParameterAttribute) queryParameterAttr
                        .cloneYourself(cloneOptions));
            }
        }

    }

    @Override
    protected void populateFrom(BaseEntity baseEntity, CloneOptions cloneOptions) {
        QueryParameter queryParameter = (QueryParameter) baseEntity;
        super.populateFrom(queryParameter, cloneOptions);
        this.setQuery(queryParameter.getQuery());
        this.getQueryParameterAttributes().clear();
        if (queryParameter.getQueryParameterAttributes() != null && queryParameter.getQueryParameterAttributes().size() > 0) {
            for (QueryParameterAttribute queryParameterAttr : queryParameter.getQueryParameterAttributes()) {
                this.getQueryParameterAttributes().add(
                        (QueryParameterAttribute) queryParameterAttr.cloneYourself(cloneOptions));
            }
        }
    }

    /**
     * @return queryParameterAttributes
     */

    public List<QueryParameterAttribute> getQueryParameterAttributes() {
        return queryParameterAttributes;
    }

    /**
     * @param queryParameterAttributes the queryParameterAttributes to set
     */

    public void setQueryParameterAttributes(List<QueryParameterAttribute> queryParameterAttributes) {
        this.queryParameterAttributes = queryParameterAttributes;
    }

    /**
     * 
     * Add queryParameterAttributes
     * @param queryParameterAttribute
     */

    public void addToQueryParameterAttributes(QueryParameterAttribute queryParameterAttribute) {
        if (this.queryParameterAttributes == null) {
            queryParameterAttributes = new ArrayList<QueryParameterAttribute>();
        }
        this.queryParameterAttributes.add(queryParameterAttribute);
    }

    @Override
    public String getDisplayName() {
        return getName();
    }
}
