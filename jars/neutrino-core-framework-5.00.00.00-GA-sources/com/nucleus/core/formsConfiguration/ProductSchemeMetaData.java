package com.nucleus.core.formsConfiguration;



import java.sql.Clob;

import javax.persistence.Column;
import javax.persistence.Entity;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.annotations.Synonym;
import com.nucleus.entity.BaseEntity;

@Entity
@DynamicUpdate
@DynamicInsert
@Synonym(grant="ALL")
public class ProductSchemeMetaData extends BaseEntity {

	private String keyy;
	@Column(length = 1000)
    private String hql;
    private String fieldReference;

	public String getFieldReference() {
		return fieldReference;
	}

	public void setFieldReference(String fieldReference) {
		this.fieldReference = fieldReference;
	}

	public String getKeyy() {
		return keyy;
	}
	public void setKeyy(String keyy) {
		this.keyy = keyy;
	}
	public String getHql() {
		return hql;
	}
	public void setHql(String hql) {
		this.hql = hql;
	}
}
