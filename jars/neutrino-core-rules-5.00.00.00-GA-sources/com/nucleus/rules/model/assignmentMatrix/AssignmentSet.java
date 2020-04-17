package com.nucleus.rules.model.assignmentMatrix;

import static com.nucleus.finnone.pro.general.util.ValidatorUtils.hasElements;

import java.util.*;

import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.annotations.Synonym;
import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.CloneOptions;
import com.nucleus.master.audit.annotation.EmbedInAuditAsReference;
import com.nucleus.master.audit.annotation.EmbedInAuditAsValue;
import com.nucleus.master.audit.annotation.EmbedInAuditAsValueObject;
import com.nucleus.rules.model.Rule;

/**
 * The Class AssignmentSet.
 *
 * @author Nucleus Software Exports Limited
 * class to hold both grid structure and expression
 */

@Entity
@DynamicUpdate
@DynamicInsert
@Cacheable
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Synonym(grant="ALL")
public class AssignmentSet extends BaseEntity {

    /** The Constant serialVersionUID. */
    private static final long             serialVersionUID = 1L;

    /** The assignment matrix row data. */
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "fk_assignment_set")
    @Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
    @EmbedInAuditAsValueObject(displayValue="Assignment Set Row")
    private List<AssignmentMatrixRowData> assignmentMatrixRowData;

    /** The assignment set name. */
    @EmbedInAuditAsValue(displayKey="label.AssignmentMaster.AssignmentSet.name")
    private String                        assignmentSetName;

    /** The is task assignment. */
    private boolean                       isTaskAssignment = false;

    /** The is rule matrix. */
    private Boolean                       isRuleMatrix     = false;

    /** The assignment action field meta data list. */
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "fk_assmnt_matrix_act_fld")
    @Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
    @EmbedInAuditAsValueObject(displayValue="Actions Field Meta")
    private List<AssignmentFieldMetaData> assignmentActionFieldMetaDataList;

    /** The entity type meta data list. */
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "fk_assmnt_matrix_entity_fld")
    @Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
    @EmbedInAuditAsValueObject(displayValue="Actions Field Meta")
    private List<EntityTypeMetaData>      entityTypeMetaDataList;

    /** The priority. */
    @EmbedInAuditAsValue(displayKey="label.AssignmentMaster.AssignmentSet.priority")
    private Integer                       priority;

    /** The assignment set type. */
    private int                           assignmentSetType;

    /** The assignment set rule. */
    @EmbedInAuditAsReference(displayKey="label.ruleMaster.rule")
    @ManyToOne
    private Rule                          assignmentSetRule;

    /** The execute all. */
    @EmbedInAuditAsValue(displayKey="label.executeAll")
    private Boolean                       executeAll;

    /** The default set. */
    @EmbedInAuditAsValue(displayKey="label.defaultSet")
    private Boolean                       defaultSet;
    
    /** The aggregate function. */
    private String                           aggregateFunction;
   
    @EmbedInAuditAsValue(displayKey="label.assignmentSet.effectiveFrom")
    private Date effectiveFrom;

    @EmbedInAuditAsValue(displayKey="label.assignmentSet.effectiveTill")
    private Date effectiveTill;

    @EmbedInAuditAsValue(displayKey="label.assignmentSet.bufferDays")
    private Integer bufferDays;
	/**
     * Gets the default set.
     *
     * @return the default set
     */
    public Boolean getDefaultSet() {
        return defaultSet;
    }

    /**
     * Sets the default set.
     *
     * @param defaultSet the new default set
     */
    public void setDefaultSet(Boolean defaultSet) {
        this.defaultSet = defaultSet;
    }

    /**
     * Gets the execute all.
     *
     * @return the executeAll
     */
    public Boolean getExecuteAll() {
        return executeAll;
    }

    /**
     * Sets the execute all.
     *
     * @param executeAll the executeAll to set
     */
    public void setExecuteAll(Boolean executeAll) {
        this.executeAll = executeAll;
    }

    /**
     * Gets the assignment set rule.
     *
     * @return the assignmentSetRule
     */
    public Rule getAssignmentSetRule() {
        return assignmentSetRule;
    }

    /**
     * Sets the assignment set rule.
     *
     * @param assignmentSetRule the assignmentSetRule to set
     */
    public void setAssignmentSetRule(Rule assignmentSetRule) {
        this.assignmentSetRule = assignmentSetRule;
    }

    /**
     * Gets the assignment set name.
     *
     * @return the assignmentSetName
     */
    public String getAssignmentSetName() {
        return assignmentSetName;
    }

    /**
     * Sets the assignment set name.
     *
     * @param assignmentSetName the assignmentSetName to set
     */
    public void setAssignmentSetName(String assignmentSetName) {
        this.assignmentSetName = assignmentSetName;
    }

    /**
     * Gets the assignment matrix row data.
     *
     * @return the assignmentMatrixRowData
     */
    public List<AssignmentMatrixRowData> getAssignmentMatrixRowData() {
        return assignmentMatrixRowData;
    }

    /**
     * Sets the assignment matrix row data.
     *
     * @param assignmentMatrixRowData the assignmentMatrixRowData to set
     */
    public void setAssignmentMatrixRowData(List<AssignmentMatrixRowData> assignmentMatrixRowData) {
        this.assignmentMatrixRowData = assignmentMatrixRowData;
    }

    /**
     * Gets the assignment action field meta data list.
     *
     * @return the assignmentActionFieldMetaDataList
     */
    public List<AssignmentFieldMetaData> getAssignmentActionFieldMetaDataList() {
        return assignmentActionFieldMetaDataList;
    }

    /**
     * Gets the priority.
     *
     * @return the priority
     */
    public Integer getPriority() {
        return priority;
    }

    /**
     * Sets the priority.
     *
     * @param priority the priority to set
     */
    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    /**
     * Sets the assignment action field meta data list.
     *
     * @param assignmentActionFieldMetaDataList the assignmentActionFieldMetaDataList to set
     */
    public void setAssignmentActionFieldMetaDataList(List<AssignmentFieldMetaData> assignmentActionFieldMetaDataList) {
        this.assignmentActionFieldMetaDataList = assignmentActionFieldMetaDataList;
    }

    /**
     * returns type of set i.e Grid or Expression
     *
     * @return the assignment set type
     */
    public int getAssignmentSetType() {
        return assignmentSetType;
    }

    /**
     * Sets the assignment set type.
     *
     * @param assignmentSetType the new assignment set type
     */
    public void setAssignmentSetType(int assignmentSetType) {
        this.assignmentSetType = assignmentSetType;
    }

    /**
     * Checks if is task assignment.
     *
     * @return true, if is task assignment
     */
    public boolean isTaskAssignment() {
        return isTaskAssignment;
    }

    /**
     * Sets the task assignment.
     *
     * @param isTaskAssignment the new task assignment
     */
    public void setTaskAssignment(boolean isTaskAssignment) {
        this.isTaskAssignment = isTaskAssignment;
    }

    /**
     * Gets the entity type meta data list.
     *
     * @return the entity type meta data list
     */
    public List<EntityTypeMetaData> getEntityTypeMetaDataList() {
        return entityTypeMetaDataList;
    }

    /**
     * Sets the entity type meta data list.
     *
     * @param entityTypeMetaDataList the new entity type meta data list
     */
    public void setEntityTypeMetaDataList(List<EntityTypeMetaData> entityTypeMetaDataList) {
        this.entityTypeMetaDataList = entityTypeMetaDataList;
    }

    /**
     * Checks if is rule matrix.
     *
     * @return true, if is rule matrix
     */
    public Boolean isRuleMatrix() {
        return isRuleMatrix;
    }

    /**
     * Sets the rule matrix.
     *
     * @param isRuleMatrix the new rule matrix
     */
    public void setRuleMatrix(Boolean isRuleMatrix) {
        this.isRuleMatrix = isRuleMatrix;
    }

    public Date getEffectiveFrom() {
        return effectiveFrom;
    }

    public void setEffectiveFrom(Date effectiveFrom) {
        this.effectiveFrom = effectiveFrom;
    }

    public Date getEffectiveTill() {
        return effectiveTill;
    }

    public void setEffectiveTill(Date effectiveTill) {
        this.effectiveTill = effectiveTill;
    }

    public Integer getBufferDays() {
        return bufferDays;
    }

    public void setBufferDays(Integer bufferDays) {
        this.bufferDays = bufferDays;
    }

    /* (non-Javadoc) @see com.nucleus.entity.BaseEntity#populate(com.nucleus.entity.BaseEntity, com.nucleus.entity.CloneOptions) */
    @Override
    protected void populate(BaseEntity baseEntity, CloneOptions cloneOptions) {
        AssignmentSet assignmentSet = (AssignmentSet) baseEntity;
        super.populate(assignmentSet, cloneOptions);

        if (hasElements(assignmentActionFieldMetaDataList)) {
            assignmentSet.setAssignmentActionFieldMetaDataList(new ArrayList<AssignmentFieldMetaData>());
            for (AssignmentFieldMetaData assignmentFieldMetaData : assignmentActionFieldMetaDataList) {
                assignmentSet.getAssignmentActionFieldMetaDataList().add(
                        (AssignmentFieldMetaData) assignmentFieldMetaData.cloneYourself(cloneOptions));
            }

        }

        if (hasElements(entityTypeMetaDataList)) {
            assignmentSet.setEntityTypeMetaDataList(new ArrayList<EntityTypeMetaData>());
            for (EntityTypeMetaData entityTypeMetaData : entityTypeMetaDataList) {
                assignmentSet.getEntityTypeMetaDataList().add(
                        (EntityTypeMetaData) entityTypeMetaData.cloneYourself(cloneOptions));
            }

        }

        if (hasElements(assignmentMatrixRowData)) {
            assignmentSet.setAssignmentMatrixRowData(new ArrayList<AssignmentMatrixRowData>());
            for (AssignmentMatrixRowData rowData : assignmentMatrixRowData) {
                assignmentSet.getAssignmentMatrixRowData()
                        .add((AssignmentMatrixRowData) rowData.cloneYourself(cloneOptions));
            }

        }

        assignmentSet.setAssignmentSetName(assignmentSetName);

        assignmentSet.setPriority(priority);

        assignmentSet.setTaskAssignment(isTaskAssignment);
        assignmentSet.setRuleMatrix(isRuleMatrix);
        assignmentSet.setAssignmentSetType(assignmentSetType);

        assignmentSet.setAssignmentSetRule(assignmentSetRule);
        assignmentSet.setExecuteAll(executeAll);
        assignmentSet.setDefaultSet(defaultSet);
        assignmentSet.setAggregateFunction(aggregateFunction);
        assignmentSet.setEffectiveFrom(effectiveFrom);
        assignmentSet.setEffectiveTill(effectiveTill);
        assignmentSet.setBufferDays(bufferDays);
    }

    /* (non-Javadoc) @see com.nucleus.entity.BaseEntity#populateFrom(com.nucleus.entity.BaseEntity, com.nucleus.entity.CloneOptions) */
    @Override
    protected void populateFrom(BaseEntity baseEntity, CloneOptions cloneOptions) {
        AssignmentSet assignmentSet = (AssignmentSet) baseEntity;
        super.populateFrom(assignmentSet, cloneOptions);

        this.setAssignmentSetName(assignmentSet.getAssignmentSetName());
        this.setPriority(assignmentSet.getPriority());
        this.setTaskAssignment(assignmentSet.isTaskAssignment());
        this.setRuleMatrix(assignmentSet.isRuleMatrix());
        this.setAssignmentSetType(assignmentSet.getAssignmentSetType());
        this.setAssignmentSetRule(assignmentSet.getAssignmentSetRule());
        this.setExecuteAll(assignmentSet.getExecuteAll());
        this.setDefaultSet(assignmentSet.getDefaultSet());
        this.setAggregateFunction(assignmentSet.getAggregateFunction());
        this.setEffectiveFrom(assignmentSet.getEffectiveFrom());
        this.setEffectiveTill(assignmentSet.getEffectiveTill());
        this.setBufferDays(assignmentSet.getBufferDays());
        getAssignmentMatrixRowData().clear();
        if (assignmentSet.getAssignmentMatrixRowData() != null && assignmentSet.getAssignmentMatrixRowData().size() > 0) {
            for (AssignmentMatrixRowData assignmentMatrixRowData : assignmentSet.getAssignmentMatrixRowData()) {
                getAssignmentMatrixRowData().add(
                        (AssignmentMatrixRowData) assignmentMatrixRowData.cloneYourself(cloneOptions));
            }
        }
        getEntityTypeMetaDataList().clear();
        if (assignmentSet.getEntityTypeMetaDataList() != null && assignmentSet.getEntityTypeMetaDataList().size() > 0) {
            for (EntityTypeMetaData entityTypeMetaData : assignmentSet.getEntityTypeMetaDataList()) {
                getEntityTypeMetaDataList().add((EntityTypeMetaData) entityTypeMetaData.cloneYourself(cloneOptions));
            }
        }
        getAssignmentActionFieldMetaDataList().clear();
        if (assignmentSet.getAssignmentActionFieldMetaDataList() != null
                && assignmentSet.getAssignmentActionFieldMetaDataList().size() > 0) {
            for (AssignmentFieldMetaData actionAssignmentFieldMetaData : assignmentSet
                    .getAssignmentActionFieldMetaDataList()) {
                getAssignmentActionFieldMetaDataList().add(
                        (AssignmentFieldMetaData) actionAssignmentFieldMetaData.cloneYourself(cloneOptions));
            }
        }

    }
	 public String getAggregateFunction() {
			return aggregateFunction;
		}

		public void setAggregateFunction(String aggregateFunction) {
			this.aggregateFunction = aggregateFunction;
		}

}
