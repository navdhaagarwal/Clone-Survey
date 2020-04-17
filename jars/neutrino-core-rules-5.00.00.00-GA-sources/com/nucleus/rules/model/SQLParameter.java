package com.nucleus.rules.model;

import com.nucleus.core.annotations.Synonym;
import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.CloneOptions;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.util.CollectionUtils;

import javax.persistence.*;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Entity
@DynamicUpdate
@DynamicInsert
@Cacheable
public class SQLParameter extends Parameter{
    @Lob
    private String sqlQuery;

    @OneToMany(fetch = FetchType.EAGER,cascade = CascadeType.ALL)
    @JoinColumn(name = "re_param_fk")
    private List<SQLParameterMapping> paramMapping;

    @Transient
    private String sqlQueryPlain;

    public List<SQLParameterMapping> getParamMapping() {
        return paramMapping;
    }

    public void setParamMapping(List<SQLParameterMapping> paramMapping) {
        this.paramMapping = paramMapping;
    }

    public String getSqlQuery() {
        return sqlQuery;
    }

    public void setSqlQuery(String sqlQuery) {
        this.sqlQuery = sqlQuery;
    }


    public String getSqlQueryPlain() {
        return sqlQueryPlain;
    }

    public void setSqlQueryPlain(String sqlQueryPlain) {
        this.sqlQueryPlain = sqlQueryPlain;
    }

    @Override
    protected void populate(BaseEntity baseEntity, CloneOptions cloneOptions) {
        SQLParameter sqlParam = (SQLParameter)baseEntity;
        super.populate(sqlParam, cloneOptions);
        sqlParam.setSqlQuery(getSqlQuery());
        if(!CollectionUtils.isEmpty(getParamMapping())){
            sqlParam.setParamMapping(getParamMapping().stream().map(new Function<SQLParameterMapping, SQLParameterMapping>() {

                @Override
                public SQLParameterMapping apply(SQLParameterMapping p) {
                    return (SQLParameterMapping) p.cloneYourself(cloneOptions);
                }
            }).collect(Collectors.toList()));
        }
    }

    @Override
    protected void populateFrom(BaseEntity baseEntity, CloneOptions cloneOptions) {
        SQLParameter sqlParam = (SQLParameter)baseEntity;
        super.populateFrom(sqlParam, cloneOptions);
        this.setSqlQuery(sqlParam.getSqlQuery());
        if(!CollectionUtils.isEmpty(sqlParam.getParamMapping())){
            this.setParamMapping(sqlParam.getParamMapping().stream().map(new Function<SQLParameterMapping, SQLParameterMapping>() {

                @Override
                public SQLParameterMapping apply(SQLParameterMapping p) {
                    return (SQLParameterMapping) p.cloneYourself(cloneOptions);
                }
            }).collect(Collectors.toList()));
        }
    }
}
