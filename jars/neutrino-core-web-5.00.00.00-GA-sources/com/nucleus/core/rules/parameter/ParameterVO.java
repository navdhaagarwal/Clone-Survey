package com.nucleus.core.rules.parameter;

import com.nucleus.rules.model.EntityType;
import com.nucleus.rules.model.ModuleName;

import java.io.Serializable;
import java.util.List;


public class ParameterVO implements Serializable {

    private String  code;

    private String   name;

    private String  description;

    private Integer dataType;

    private Integer  paramType;

    private String   sourceProduct;

    private boolean   collectionBased;

    private ModuleName   moduleName;

    private String   objectGraph;

    private String   literal;

    private String   reference;

    private EntityType  entityType;

    private String  parameterExpression;

    private String   query;

    private List<QueryDerivedParameterVO> queryDerivedParameterVO;

    private String    contextName;

    private String    scriptCode;

    private Integer   scriptCodeType;

    private String    compiledExpression;

    private String   aggregateFunction;

    private String   targetObjectGraph;

    private Boolean   entityField;

    private Boolean   validateOnAll;

    private String uploadOperationType;

    private String               sqlQuery;

    public String getSqlQuery() {
        return sqlQuery;
    }

    public void setSqlQuery(String sqlQuery) {
        this.sqlQuery = sqlQuery;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getDataType() {
        return dataType;
    }

    public void setDataType(Integer dataType) {
        this.dataType = dataType;
    }

    public Integer getParamType() {
        return paramType;
    }

    public void setParamType(Integer paramType) {
        this.paramType = paramType;
    }

    public String getSourceProduct() {
        return sourceProduct;
    }

    public void setSourceProduct(String sourceProduct) {
        this.sourceProduct = sourceProduct;
    }

    public boolean isCollectionBased() {
        return collectionBased;
    }

    public void setCollectionBased(boolean collectionBased) {
        this.collectionBased = collectionBased;
    }

    public ModuleName getModuleName() {
        return moduleName;
    }

    public void setModuleName(ModuleName moduleName) {
        this.moduleName = moduleName;
    }

    public String getObjectGraph() {
        return objectGraph;
    }

    public void setObjectGraph(String objectGraph) {
        this.objectGraph = objectGraph;
    }

    public String getLiteral() {
        return literal;
    }

    public void setLiteral(String literal) {
        this.literal = literal;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public EntityType getEntityType() {
        return entityType;
    }

    public void setEntityType(EntityType entityType) {
        this.entityType = entityType;
    }

    public String getParameterExpression() {
        return parameterExpression;
    }

    public void setParameterExpression(String parameterExpression) {
        this.parameterExpression = parameterExpression;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public List<QueryDerivedParameterVO> getQueryDerivedParameterVO() {
        return queryDerivedParameterVO;
    }

    public void setQueryDerivedParameterVO(List<QueryDerivedParameterVO> queryDerivedParameterVO) {
        this.queryDerivedParameterVO = queryDerivedParameterVO;
    }

    public String getContextName() {
        return contextName;
    }

    public void setContextName(String contextName) {
        this.contextName = contextName;
    }

    public String getScriptCode() {
        return scriptCode;
    }

    public void setScriptCode(String scriptCode) {
        this.scriptCode = scriptCode;
    }

    public Integer getScriptCodeType() {
        return scriptCodeType;
    }

    public void setScriptCodeType(Integer scriptCodeType) {
        this.scriptCodeType = scriptCodeType;
    }

    public String getCompiledExpression() {
        return compiledExpression;
    }

    public void setCompiledExpression(String compiledExpression) {
        this.compiledExpression = compiledExpression;
    }

    public String getAggregateFunction() {
        return aggregateFunction;
    }

    public void setAggregateFunction(String aggregateFunction) {
        this.aggregateFunction = aggregateFunction;
    }

    public String getTargetObjectGraph() {
        return targetObjectGraph;
    }

    public void setTargetObjectGraph(String targetObjectGraph) {
        this.targetObjectGraph = targetObjectGraph;
    }

    public Boolean getEntityField() {
        return entityField;
    }

    public void setEntityField(Boolean entityField) {
        this.entityField = entityField;
    }

    public Boolean getValidateOnAll() {
        return validateOnAll;
    }

    public void setValidateOnAll(Boolean validateOnAll) {
        this.validateOnAll = validateOnAll;
    }

    public String getUploadOperationType() {
        return uploadOperationType;
    }

    public void setUploadOperationType(String uploadOperationType) {
        this.uploadOperationType = uploadOperationType;
    }
}
