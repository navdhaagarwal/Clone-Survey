package com.nucleus.rules.model;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.nucleus.core.annotations.Synonym;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.CloneOptions;

@Entity
@DynamicUpdate
@DynamicInsert
@Cacheable
@Table(name="SQL_Rule_Para_Map")
@Synonym(grant = "ALL")
public class SQLRuleParameterMapping extends BaseEntity{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Integer seq;
	
	private String placeHolderName;
	
	@ManyToOne
	@JoinColumn(name="re_param")
	private Parameter parameter;

	public String getPlaceHolderName() {
		return placeHolderName;
	}

	public void setPlaceHolderName(String placeHolderName) {
		this.placeHolderName = placeHolderName;
	}

	public Parameter getParameter() {
		return parameter;
	}

	public void setParameter(Parameter parameter) {
		this.parameter = parameter;
	}
	


	public Integer getSeq() {
		return seq;
	}

	public void setSeq(Integer seq) {
		this.seq = seq;
	}

	@Override
	protected void populate(BaseEntity baseEntity, CloneOptions cloneOptions) {
		SQLRuleParameterMapping mapping = (SQLRuleParameterMapping) baseEntity;
		super.populate(mapping, cloneOptions);
		mapping.setParameter(getParameter());
		mapping.setPlaceHolderName(getPlaceHolderName());
		mapping.setSeq(getSeq());
	}
	
	@Override
	protected void populateFrom(BaseEntity copyEntity, CloneOptions cloneOptions) {
		SQLRuleParameterMapping mapping = (SQLRuleParameterMapping) copyEntity;
		super.populateFrom(mapping, cloneOptions);
		this.setParameter(mapping.getParameter());
		this.setPlaceHolderName(mapping.getPlaceHolderName());
		this.setSeq(mapping.getSeq());
	}
}
