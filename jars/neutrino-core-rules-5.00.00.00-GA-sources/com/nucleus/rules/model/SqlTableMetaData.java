package com.nucleus.rules.model;

import com.nucleus.core.annotations.Synonym;
import com.nucleus.entity.BaseEntity;

import javax.persistence.Column;
import javax.persistence.Entity;

@Entity
@Synonym(grant = "ALL")
public class SqlTableMetaData extends BaseEntity {

    @Column(name = "TABLE_NAME")
    private String tableName;

    @Column(name = "COLUMN_NAMES")
    private String columnNames;

    public String getTableName() {
        return tableName;
    }

    public String getColumnNames() {
        return columnNames;
    }
}
