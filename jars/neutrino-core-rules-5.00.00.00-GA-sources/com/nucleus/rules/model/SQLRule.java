package com.nucleus.rules.model;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import com.nucleus.core.annotations.Synonym;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.util.CollectionUtils;

import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.CloneOptions;

@Entity
@DynamicUpdate
@DynamicInsert
@Cacheable
@Synonym(grant = "ALL")
public class SQLRule extends Rule{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@Lob
	private String sqlQuery;
	
	@OneToMany(fetch = FetchType.LAZY,cascade = CascadeType.ALL)
	@JoinColumn(name = "re_rule_fk")
	private List<SQLRuleParameterMapping> paramMapping;

	@Transient
	private String sqlQueryPlain;
	
	public List<SQLRuleParameterMapping> getParamMapping() {
		return paramMapping;
	}

	public void setParamMapping(List<SQLRuleParameterMapping> paramMapping) {
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
		SQLRule sqlRule = (SQLRule)baseEntity;
		super.populate(sqlRule, cloneOptions);
		sqlRule.setSqlQuery(getSqlQuery());
		if(!CollectionUtils.isEmpty(getParamMapping())){
			sqlRule.setParamMapping(getParamMapping().stream().map(new Function<SQLRuleParameterMapping, SQLRuleParameterMapping>() {

				@Override
				public SQLRuleParameterMapping apply(SQLRuleParameterMapping p) {
					return (SQLRuleParameterMapping) p.cloneYourself(cloneOptions);
				}
			}).collect(Collectors.toList()));
		}
	}
	
	@Override
	protected void populateFrom(BaseEntity baseEntity, CloneOptions cloneOptions) {
		SQLRule sqlRule = (SQLRule)baseEntity;
		super.populateFrom(sqlRule, cloneOptions);
		this.setSqlQuery(sqlRule.getSqlQuery());
		if(!CollectionUtils.isEmpty(sqlRule.getParamMapping())){
			this.setParamMapping(sqlRule.getParamMapping().stream().map(new Function<SQLRuleParameterMapping, SQLRuleParameterMapping>() {

				@Override
				public SQLRuleParameterMapping apply(SQLRuleParameterMapping p) {
					return (SQLRuleParameterMapping) p.cloneYourself(cloneOptions);
				}
			}).collect(Collectors.toList()));
		}
	}

}
