package com.nucleus.core.formsConfiguration;

import com.nucleus.core.annotations.Synonym;
import com.nucleus.entity.BaseEntity;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.Cacheable;
import javax.persistence.Entity;
import java.sql.Clob;
import java.util.List;

@Entity
@DynamicUpdate
@DynamicInsert
@Synonym(grant="ALL")
public class SpecialTable extends BaseEntity {

    private String keyy;
    private String primaryValue;
    private Clob selectClause;
    private Clob fromClause;
    private String description;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPrimaryValue() {
        return primaryValue;
    }

    public void setPrimaryValue(String primaryValue) {
        this.primaryValue = primaryValue;
    }

    public String getKeyy() {
        return keyy;
    }

    public void setKeyy(String keyy) {
        this.keyy = keyy;
    }

    public Clob getSelectClause() {
        return selectClause;
    }

    public void setSelectClause(Clob selectClause) {
        this.selectClause = selectClause;
    }

    public Clob getFromClause() {
        return fromClause;
    }

    public void setFromClause(Clob fromClause) {
        this.fromClause = fromClause;
    }

}
