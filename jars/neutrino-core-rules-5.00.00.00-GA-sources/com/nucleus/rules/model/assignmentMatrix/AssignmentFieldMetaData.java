package com.nucleus.rules.model.assignmentMatrix;

import javax.persistence.*;

import com.nucleus.rules.model.Parameter;
import com.nucleus.rules.model.Rule;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.annotations.Synonym;
import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.CloneOptions;
import com.nucleus.master.audit.annotation.EmbedInAuditAsReference;
import com.nucleus.master.audit.annotation.EmbedInAuditAsValue;

import java.util.List;

/**
 * 
 * @author Nucleus Software Exports Limited
 * class to hold data for each row
 */

@Entity
@DynamicUpdate
@DynamicInsert
@Cacheable
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Synonym(grant="ALL")
public class AssignmentFieldMetaData extends BaseEntity {

    private static final long serialVersionUID = 1L;

    private String            ognl;

    private int               dataType;

    private String            operator;

    private String            indexId;

    @EmbedInAuditAsValue
    private String            fieldName;

    private Boolean           parameterBased;

    @Transient
    private List<Parameter>   parameterList;

    @Transient
    private List<String>      operators;

    @Transient
    private String            webBinderName;

    @Transient
    private String            itemLabel;

    @Transient
    private String            itemValue;
    
    private Boolean           ruleBased=Boolean.FALSE;

    @ManyToOne
    @EmbedInAuditAsReference
    private Rule rule;


    /**
     * @return the indexId
     */
    public String getIndexId() {
        return indexId;
    }

    /**
     * @param indexId the indexId to set
     */
    public void setIndexId(String indexId) {
        this.indexId = indexId;
    }

    /**
     * @return the fieldName
     */
    public String getFieldName() {
        return fieldName;
    }

    /**
     * @param code the fieldName to set
     */
    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    /**
     * @return the ognl
     */
    public String getOgnl() {
        return ognl;
    }

    /**
     * @param fieldKey the fieldKey to set
     */
    public void setOgnl(String ognl) {
        this.ognl = ognl;
    }

    /**
     * @return the dataType
     */
    public int getDataType() {
        return dataType;
    }

    /**
     * @param dataType the dataType to set
     */
    public void setDataType(int dataType) {
        this.dataType = dataType;
    }

    /**
     * @return the operator
     */
    public String getOperator() {
        return operator;
    }

    /**
     * @param operator the operator to set
     */
    public void setOperator(String operator) {
        this.operator = operator;
    }

    @Override
    protected void populate(BaseEntity baseEntity, CloneOptions cloneOptions) {
        AssignmentFieldMetaData assignmentFieldMetaData = (AssignmentFieldMetaData) baseEntity;
        super.populate(assignmentFieldMetaData, cloneOptions);

        assignmentFieldMetaData.setOgnl(ognl);
        assignmentFieldMetaData.setDataType(dataType);
        assignmentFieldMetaData.setOperator(operator);
        assignmentFieldMetaData.setFieldName(fieldName);
        assignmentFieldMetaData.setIndexId(indexId);
        assignmentFieldMetaData.setParameterBased(parameterBased);
        assignmentFieldMetaData.setRuleBased(ruleBased);
        assignmentFieldMetaData.setRule(rule);
    }

    @Override
    protected void populateFrom(BaseEntity baseEntity, CloneOptions cloneOptions) {
        AssignmentFieldMetaData assignmentFieldMetaData = (AssignmentFieldMetaData) baseEntity;
        super.populateFrom(assignmentFieldMetaData, cloneOptions);
        this.setOgnl(assignmentFieldMetaData.getOgnl());
        this.setDataType(assignmentFieldMetaData.getDataType());
        this.setOperator(assignmentFieldMetaData.getOperator());
        this.setFieldName(assignmentFieldMetaData.getFieldName());
        this.setIndexId(assignmentFieldMetaData.getIndexId());
        this.setParameterBased(assignmentFieldMetaData.getParameterBased());
        this.ruleBased=assignmentFieldMetaData.getRuleBased();
        this.rule=assignmentFieldMetaData.getRule();

    }
    public Boolean getRuleBased() {
        return ruleBased;
    }

    public void setRuleBased(Boolean ruleBased) {
        this.ruleBased = ruleBased;
    }

    public Rule getRule() {
        return rule;
    }

    public void setRule(Rule rule) {
        this.rule = rule;
    }
    public Boolean getParameterBased() {
        return parameterBased;
    }

    public void setParameterBased(Boolean parameterBased) {
        this.parameterBased = parameterBased;
    }

    public List<Parameter> getParameterList() {
        return parameterList;
    }

    public void setParameterList(List<Parameter> parameterList) {
        this.parameterList = parameterList;
    }

    public List<String> getOperators() {
        return operators;
    }

    public void setOperators(List<String> operators) {
        this.operators = operators;
    }

    public String getWebBinderName() {
        return webBinderName;
    }

    public void setWebBinderName(String webBinderName) {
        this.webBinderName = webBinderName;
    }

    public String getItemLabel() {
        return itemLabel;
    }

    public void setItemLabel(String itemLabel) {
        this.itemLabel = itemLabel;
    }

    public String getItemValue() {
        return itemValue;
    }

    public void setItemValue(String itemValue) {
        this.itemValue = itemValue;
    }
}
