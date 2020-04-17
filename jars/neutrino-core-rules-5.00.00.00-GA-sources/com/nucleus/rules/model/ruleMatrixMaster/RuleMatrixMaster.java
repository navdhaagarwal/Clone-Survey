package com.nucleus.rules.model.ruleMatrixMaster;

import javax.persistence.*;

import org.apache.commons.collections.CollectionUtils;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.CloneOptions;
import com.nucleus.master.audit.annotation.EmbedInAuditAsValue;
import com.nucleus.master.audit.annotation.NeutrinoAuditableMaster;
import com.nucleus.rules.model.assignmentMatrix.BaseAssignmentMaster;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 
 * @author Nucleus Software Exports Limited
 * Master class for rule matrix master
 */
@Entity
@DynamicUpdate
@DynamicInsert
@NeutrinoAuditableMaster(identifierColumn="code")
public class RuleMatrixMaster extends BaseAssignmentMaster {

    private static final long    serialVersionUID = 1L;

    @EmbedInAuditAsValue(displayKey="label.executeAll")
    private Boolean              executeAll;

    @ManyToOne
    private RuleMatrixMasterType ruleMatrixMasterType;

    @Transient
    private List<RuleMatrixGridData> ifGridData;

    @Transient
    private List<RuleMatrixGridData> thenGridData;

    @EmbedInAuditAsValue(displayKey="label.assignment.master.multipleResults")
    private Boolean           multipleResults;

    /**
     * @return the executeAll
     */
    public Boolean getExecuteAll() {
        return executeAll;
    }

    /**
     * @param executeAll the executeAll to set
     */
    public void setExecuteAll(Boolean executeAll) {
        this.executeAll = executeAll;
    }

    public RuleMatrixMasterType getRuleMatrixMasterType() {
        return ruleMatrixMasterType;
    }

    public void setRuleMatrixMasterType(RuleMatrixMasterType ruleMatrixMasterType) {
        this.ruleMatrixMasterType = ruleMatrixMasterType;
    }

    public List<RuleMatrixGridData> getIfGridData() {
        return ifGridData;
    }

    public void setIfGridData(List<RuleMatrixGridData> ifGridData) {
        this.ifGridData = ifGridData;
    }

    public List<RuleMatrixGridData> getThenGridData() {
        return thenGridData;
    }

    public void setThenGridData(List<RuleMatrixGridData> thenGridData) {
        this.thenGridData = thenGridData;
    }

    public Boolean getMultipleResults() {
        return multipleResults;
    }

    public void setMultipleResults(Boolean multipleResults) {
        this.multipleResults = multipleResults;
    }

    @Override
    protected void populate(BaseEntity baseEntity, CloneOptions cloneOptions) {
        RuleMatrixMaster ruleMatrixMaster = (RuleMatrixMaster) baseEntity;
        super.populate(ruleMatrixMaster, cloneOptions);
        ruleMatrixMaster.setExecuteAll(executeAll);
        ruleMatrixMaster.setRuleMatrixMasterType(ruleMatrixMasterType);
        if(CollectionUtils.isNotEmpty(ifGridData)) {
            List<RuleMatrixGridData> list = ifGridData.stream().filter(Objects::nonNull).
                    map(elem -> (RuleMatrixGridData)elem.cloneYourself(cloneOptions)).collect(Collectors.toList());
            ruleMatrixMaster.setIfGridData(list);
        }
        if(CollectionUtils.isNotEmpty(thenGridData)) {
            List<RuleMatrixGridData> list = thenGridData.stream().filter(Objects::nonNull).
                    map(elem -> (RuleMatrixGridData)elem.cloneYourself(cloneOptions)).collect(Collectors.toList());
            ruleMatrixMaster.setThenGridData(list);
        }

        ruleMatrixMaster.setMultipleResults(multipleResults);
    }

    @Override
    protected void populateFrom(BaseEntity baseEntity, CloneOptions cloneOptions) {
        RuleMatrixMaster ruleMatrixMaster = (RuleMatrixMaster) baseEntity;
        super.populateFrom(ruleMatrixMaster, cloneOptions);
        this.setExecuteAll(ruleMatrixMaster.getExecuteAll());
        this.setRuleMatrixMasterType(ruleMatrixMaster.getRuleMatrixMasterType());
        if(CollectionUtils.isNotEmpty(ruleMatrixMaster.getIfGridData())) {
            this.setIfGridData(ruleMatrixMaster.getIfGridData().stream().filter(Objects::nonNull).
                    map(elem -> (RuleMatrixGridData)elem.cloneYourself(cloneOptions)).collect(Collectors.toList()));
        }
        if(CollectionUtils.isNotEmpty(ruleMatrixMaster.getThenGridData())) {
            this.setThenGridData(ruleMatrixMaster.getThenGridData().stream().filter(Objects::nonNull).
                    map(elem -> (RuleMatrixGridData)elem.cloneYourself(cloneOptions)).collect(Collectors.toList()));
        }
        this.setMultipleResults(ruleMatrixMaster.getMultipleResults());

    }

}