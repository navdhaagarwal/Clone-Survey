package com.nucleus.rules.model;

import com.nucleus.core.formsConfiguration.validationcomponent.Tuple_2;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SQLParseResponse {
    private Map<String,String> tableDetail = new HashMap<String, String>();
    private Map<String,Set<Tuple_2>> tableSelectColumn = new HashMap<String, Set<Tuple_2>>();
    private Map<String,Set<String>> tableWhereColumn = new HashMap<String, Set<String>>();
    private Set<String> whereColumnNameWithoutTable = new HashSet<String>();
    private Set<Tuple_2> selectColumnNameWithoutTable = new HashSet<Tuple_2>();
    private Set<String> dbFunctions = new HashSet<>();
    private Map<String,String> complexAlias = new HashMap<>();
    private Set<String> usedConstants = new HashSet<>();
    private Set<String> whereUsedConstants = new HashSet<>();
    private Set<String> whereInjections = new HashSet<>();

    public Set<String> getWhereInjections() {
        return whereInjections;
    }
    public void setWhereInjections(Set<String> whereInjections) {
        this.whereInjections = whereInjections;
    }

    public Set<String> getWhereUsedConstants() {
        return whereUsedConstants;
    }

    public void setWhereUsedConstants(Set<String> whereUsedConstants) {
        this.whereUsedConstants = whereUsedConstants;
    }

    public Map<String, String> getTableDetail() {
        return tableDetail;
    }
    public void setTableDetail(Map<String, String> tableDetail) {
        this.tableDetail = tableDetail;
    }
    public Map<String, Set<Tuple_2>> getTableSelectColumn() {
        return tableSelectColumn;
    }
    public void setTableSelectColumn(Map<String, Set<Tuple_2>> tableSelectColumn) {
        this.tableSelectColumn = tableSelectColumn;
    }
    public Map<String, Set<String>> getTableWhereColumn() {
        return tableWhereColumn;
    }
    public void setTableWhereColumn(Map<String, Set<String>> tableWhereColumn) {
        this.tableWhereColumn = tableWhereColumn;
    }

    public Set<String> getWhereColumnNameWithoutTable() {
        return whereColumnNameWithoutTable;
    }
    public void setWhereColumnNameWithoutTable(Set<String> whereColumnNameWithoutTable) {
        this.whereColumnNameWithoutTable = whereColumnNameWithoutTable;
    }
    public Set<Tuple_2> getSelectColumnNameWithoutTable() {
        return selectColumnNameWithoutTable;
    }
    public void setSelectColumnNameWithoutTable(Set<Tuple_2> selectColumnNameWithoutTable) {
        this.selectColumnNameWithoutTable = selectColumnNameWithoutTable;
    }

    public void addWhereColumnNameWithoutTable(String columnNameWithoutTable) {
        this.whereColumnNameWithoutTable.add(columnNameWithoutTable);
    }

    public void addSelectColumnNameWithoutTable(String columnNameWithoutTable,String alias) {
        this.selectColumnNameWithoutTable.add(new Tuple_2(columnNameWithoutTable,alias));
    }

    public void addSelectColumnNameWithoutTable(String columnNameWithoutTable) {
        addSelectColumnNameWithoutTable(columnNameWithoutTable,null);
    }

    public void addTableWhereColumn(String tableName,String whereColumnName) {
        Set<String> whereColumn = this.tableWhereColumn.get(tableName);
        if(CollectionUtils.isEmpty(whereColumn)){
            whereColumn = new HashSet<>();
            tableWhereColumn.put(tableName, whereColumn);
        }
        whereColumn.add(whereColumnName);
    }

    public void addTableSelectColumn(String tableName,String whereColumnName,String alias) {
        Set<Tuple_2> selectColumn = this.tableSelectColumn.get(tableName);
        if(CollectionUtils.isEmpty(selectColumn)){
            selectColumn = new HashSet<>();
            tableSelectColumn.put(tableName, selectColumn);
        }
        selectColumn.add(new Tuple_2(whereColumnName,alias));
    }


    public void addTableSelectColumn(String tableName,String whereColumnName) {
        addTableSelectColumn(tableName,whereColumnName,null);
    }

    public void addTableDetail(String tableName,String aliasName) {
        this.tableDetail.put(tableName, aliasName);
    }

    public String getTableNameByAlias(String alias){
        for (String tableName : tableDetail.keySet()) {
            if(tableDetail.get(tableName).equals(alias)){
                return tableName;
            }
        }
        return null;
    }

    public Set<String> getDbFunctions() {
        return dbFunctions;
    }
    public void setDbFunctions(Set<String> dbFunctions) {
        this.dbFunctions = dbFunctions;
    }
    public void addDbFunctions(String dbFunction) {
        this.dbFunctions.add(dbFunction);
    }

    public Map<String, String> getComplexAlias() {
        return complexAlias;
    }
    public void setComplexAlias(Map<String, String> complexAlias) {
        this.complexAlias = complexAlias;
    }
    public void addComplexAlias(String alias,String structure) {
        this.complexAlias.put(alias, structure);
    }

    public Set<String> getUsedConstants() {
        return usedConstants;
    }
    public void setUsedConstants(Set<String> usedConstants) {
        this.usedConstants = usedConstants;
    }

    public void addUsedConstants(String usedConstant) {
        this.usedConstants.add(usedConstant);
    }
    public void addWhereUsedConstants(String constant){
        this.whereUsedConstants.add(constant);
    }
    public void addWhereInjections(String injection){
        this.whereInjections.add(injection);
    }
    @Override
    public String toString() {
        return "SQLParseResponse [tableDetail=" + tableDetail + ", tableSelectColumn=" + tableSelectColumn
                + ", tableWhereColumn=" + tableWhereColumn + ", whereColumnNameWithoutTable="
                + whereColumnNameWithoutTable + ", selectColumnNameWithoutTable=" + selectColumnNameWithoutTable
                + ", dbFunctions=" + dbFunctions + ", complexAlias=" + complexAlias + ", usedConstants=" + usedConstants+", "
                + "whereUsedConstants="+whereUsedConstants+","+"whereInjections="+whereInjections
                + "]";
    }


}
