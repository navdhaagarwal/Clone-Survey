package com.nucleus.rules.model.ruleMatrixMaster;

import com.nucleus.core.annotations.Synonym;
import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.CloneOptions;
import com.nucleus.rules.model.ObjectGraphTypes;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import java.util.List;
import java.util.Objects;

/**
 * Created by shivanshi.garg on 10/17/2018.
 */

public class RuleMatrixGridData extends BaseEntity {

    private String ruleOgnlType;
    private String paramType;

    private ObjectGraphTypes objectGraphType;

    private String operator;

    private List<String> operators;

    public String getParamType() {
        return paramType;
    }

    public void setParamType(String paramType) {
        this.paramType = paramType;
    }

    public ObjectGraphTypes getObjectGraphType() {
        return objectGraphType;
    }

    public void setObjectGraphType(ObjectGraphTypes objectGraphType) {
        this.objectGraphType = objectGraphType;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public List<String> getOperators() {
        return operators;
    }

    public void setOperators(List<String> operators) {
        this.operators = operators;
    }

    public String getRuleOgnlType() {
        return ruleOgnlType;
    }

    public void setRuleOgnlType(String ruleOgnlType) {
        this.ruleOgnlType = ruleOgnlType;
    }

    public boolean isGridDataSame(RuleMatrixGridData ruleMatrixGridData){

        if(Objects.nonNull(this.getObjectGraphType()) && Objects.nonNull(ruleMatrixGridData.getObjectGraphType())){
            if(this.getObjectGraphType().getId().compareTo(ruleMatrixGridData.getObjectGraphType().getId()) != 0){
                return false;
            }
        }else if(Objects.nonNull(this.getObjectGraphType()) ^ Objects.nonNull(ruleMatrixGridData.getObjectGraphType())){
            return false;
        }
        if(!compareStringsWithNullCheck(this.getOperator(),ruleMatrixGridData.getOperator())){
            return false;
        }
        if(!compareStringsWithNullCheck(this.getParamType(),ruleMatrixGridData.getParamType())){
            return false;
        }
        return true;
    }

    private boolean compareStringsWithNullCheck(String str1, String str2) {
        return (str1 == null ? str2 == null : str1.equals(str2));
    }
}
