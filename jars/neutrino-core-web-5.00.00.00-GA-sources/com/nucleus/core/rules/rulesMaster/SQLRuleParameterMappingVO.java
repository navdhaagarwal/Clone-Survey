package com.nucleus.core.rules.rulesMaster;

import com.nucleus.rules.model.Parameter;


public class SQLRuleParameterMappingVO {

    private Integer seq;

    private String placeHolderName;

    private Parameter parameter;

    public Integer getSeq() {
        return seq;
    }

    public void setSeq(Integer seq) {
        this.seq = seq;
    }

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
}
