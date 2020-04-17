package com.nucleus.core.rules.rulesMaster;

import com.nucleus.rules.model.ModuleName;
import com.nucleus.rules.model.SQLRuleParameterMapping;

import java.io.Serializable;
import java.util.List;


public class RuleVO implements Serializable {

    private String               code;

    private String               name;

    private String               description;

    private String               sourceProduct;

    private ModuleName           moduleName;

    private Integer              ruleType;

    private String               ruleExpression;

    private String               ruleTagNames;

    private String               errorMessage;

    private String               errorMessageKey;

    private String               successMessage;

    private String               successMessageKey;

    private boolean              criteriaRuleFlag;

    private String               scriptCode;

    private Integer              scriptCodeType;

    private String               sqlQuery;

    private List<SQLRuleParameterMappingVO> paramMapping;

    private String uploadOperationType;

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

    public String getSourceProduct() {
        return sourceProduct;
    }

    public void setSourceProduct(String sourceProduct) {
        this.sourceProduct = sourceProduct;
    }

    public ModuleName getModuleName() {
        return moduleName;
    }

    public void setModuleName(ModuleName moduleName) {
        this.moduleName = moduleName;
    }

    public Integer getRuleType() {
        return ruleType;
    }

    public void setRuleType(Integer ruleType) {
        this.ruleType = ruleType;
    }

    public String getRuleExpression() {
        return ruleExpression;
    }

    public void setRuleExpression(String ruleExpression) {
        this.ruleExpression = ruleExpression;
    }

    public String getRuleTagNames() {
        return ruleTagNames;
    }

    public void setRuleTagNames(String ruleTagNames) {
        this.ruleTagNames = ruleTagNames;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getErrorMessageKey() {
        return errorMessageKey;
    }

    public void setErrorMessageKey(String errorMessageKey) {
        this.errorMessageKey = errorMessageKey;
    }

    public String getSuccessMessage() {
        return successMessage;
    }

    public void setSuccessMessage(String successMessage) {
        this.successMessage = successMessage;
    }

    public String getSuccessMessageKey() {
        return successMessageKey;
    }

    public void setSuccessMessageKey(String successMessageKey) {
        this.successMessageKey = successMessageKey;
    }

    public boolean isCriteriaRuleFlag() {
        return criteriaRuleFlag;
    }

    public void setCriteriaRuleFlag(boolean criteriaRuleFlag) {
        this.criteriaRuleFlag = criteriaRuleFlag;
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

    public String getSqlQuery() {
        return sqlQuery;
    }

    public void setSqlQuery(String sqlQuery) {
        this.sqlQuery = sqlQuery;
    }

    public List<SQLRuleParameterMappingVO> getParamMapping() {
        return paramMapping;
    }

    public void setParamMapping(List<SQLRuleParameterMappingVO> paramMapping) {
        this.paramMapping = paramMapping;
    }

    public String getUploadOperationType() {
        return uploadOperationType;
    }

    public void setUploadOperationType(String uploadOperationType) {
        this.uploadOperationType = uploadOperationType;
    }
}
