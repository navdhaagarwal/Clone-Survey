package com.nucleus.rules.model.ruleMatrixMaster.pojo;

import com.nucleus.rules.model.Parameter;

import java.io.Serializable;
import java.util.List;


public class RuleMatrixColumnData implements Serializable{

    private Boolean ruleBased;
    private String stringValue;
    private String stringValueFrom;
    private String stringValueTo;
    private String fieldType;
    private String fieldName;
    private String operator;
    private String dataType;
    private Long[] parameterArr;
    private Long parameterFrom;
    private Long parameterTo;
    private Long parameter;
    private String displayName;
    private List<Parameter> parameters;
    private String indexId;
    private String webDataBinderName;
    private String itemValue;
    private String itemLabel;
    private String objectGraph;

    public String getObjectGraph() {
        return objectGraph;
    }

    public void setObjectGraph(String objectGraph) {
        this.objectGraph = objectGraph;
    }

    public String getWebDataBinderName() {
        return webDataBinderName;
    }

    public void setWebDataBinderName(String webDataBinderName) {
        this.webDataBinderName = webDataBinderName;
    }

    public String getItemValue() {
        return itemValue;
    }

    public void setItemValue(String itemValue) {
        this.itemValue = itemValue;
    }

    public String getItemLabel() {
        return itemLabel;
    }

    public void setItemLabel(String itemLabel) {
        this.itemLabel = itemLabel;
    }

    public Boolean getRuleBased() {
        return ruleBased;
    }

    public void setRuleBased(Boolean ruleBased) {
        this.ruleBased = ruleBased;
    }

    public String getStringValue() {
        return stringValue;
    }

    public void setStringValue(String stringValue) {
        this.stringValue = stringValue;
    }

    public String getStringValueFrom() {
        return stringValueFrom;
    }

    public void setStringValueFrom(String stringValueFrom) {
        this.stringValueFrom = stringValueFrom;
    }

    public String getStringValueTo() {
        return stringValueTo;
    }

    public void setStringValueTo(String stringValueTo) {
        this.stringValueTo = stringValueTo;
    }

    public String getFieldType() {
        return fieldType;
    }

    public void setFieldType(String fieldType) {
        this.fieldType = fieldType;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public Long[] getParameterArr() {
        return parameterArr;
    }

    public void setParameterArr(Long[] parameterArr) {
        this.parameterArr = parameterArr;
    }

    public Long getParameterFrom() {
        return parameterFrom;
    }

    public void setParameterFrom(Long parameterFrom) {
        this.parameterFrom = parameterFrom;
    }

    public Long getParameterTo() {
        return parameterTo;
    }

    public void setParameterTo(Long parameterTo) {
        this.parameterTo = parameterTo;
    }

    public Long getParameter() {
        return parameter;
    }

    public void setParameter(Long parameter) {
        this.parameter = parameter;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public List<Parameter> getParameters() {
        return parameters;
    }

    public void setParameters(List<Parameter> parameters) {
        this.parameters = parameters;
    }

    public String getIndexId() {
        return indexId;
    }

    public void setIndexId(String indexId) {
        this.indexId = indexId;
    }
}
