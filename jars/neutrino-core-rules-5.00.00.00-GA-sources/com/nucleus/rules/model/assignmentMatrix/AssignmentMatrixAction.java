package com.nucleus.rules.model.assignmentMatrix;

import java.util.Map;

import javax.persistence.Cacheable;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Transient;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.CloneOptions;
import com.nucleus.entity.PersistenceStatus;
import com.nucleus.master.audit.annotation.EmbedInAuditAsValue;
import com.nucleus.rules.model.RuleAction;

/**
 * 
 * @author Nucleus Software Exports Limited
 * class to create actions for the assignment based matrix
 */

@Entity
@DynamicUpdate
@DynamicInsert
@Cacheable
public class AssignmentMatrixAction extends RuleAction {

    private static final long   serialVersionUID = 5686477459496866318L;

    // Stores AssignmentFieldMetaData name and its value
    @Lob
    @EmbedInAuditAsValue(skipInDisplay=true)
    private String              assignActionValues;

    @Transient
    private Map<Object, Object> jsonAssignActionMap;

    public Map<Object, Object> getJsonAssignActionMap() {
        return jsonAssignActionMap;
    }

    public void setJsonAssignActionMap(Map<Object, Object> jsonAssignActionMap) {
        this.jsonAssignActionMap = jsonAssignActionMap;
    }

    /**
     * @return the assignActionValues
     */
    public String getAssignActionValues() {
        return assignActionValues;
    }

    /**
     * @param assignActionValues the assignActionValues to set
     */
    public void setAssignActionValues(String assignActionValues) {
        this.assignActionValues = assignActionValues;
    }

    @Override
    protected void populate(BaseEntity baseEntity, CloneOptions cloneOptions) {
        AssignmentMatrixAction assignmentMatrixAction = (AssignmentMatrixAction) baseEntity;
        super.populate(assignmentMatrixAction, cloneOptions);

        assignmentMatrixAction.setAssignActionValues(assignActionValues);
        assignmentMatrixAction.setPersistenceStatus(PersistenceStatus.EMPTY_PARENT);
    }

    @Override
    protected void populateFrom(BaseEntity baseEntity, CloneOptions cloneOptions) {
        AssignmentMatrixAction assignmentMatrixAction = (AssignmentMatrixAction) baseEntity;
        super.populateFrom(assignmentMatrixAction, cloneOptions);
        this.setAssignActionValues(assignActionValues);

    }
}
