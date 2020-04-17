package com.nucleus.rules.model.assignmentMatrix;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.OneToMany;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.CloneOptions;
import com.nucleus.master.audit.annotation.EmbedInAuditAsValueObject;

/**
 * 
 * @author Nucleus Software Exports Limited class to hold values for grid
 *         structure
 */

@Entity
@DynamicUpdate
@DynamicInsert
@Cacheable
public class AssignmentGrid extends AssignmentSet {

    private static final long             serialVersionUID = 1L;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "fk_assignment_grid")
    @Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
    @EmbedInAuditAsValueObject
    private List<AssignmentFieldMetaData> assignmentFieldMetaDataList;

    /* Need to removed this field it holds redundant information. */
    @Lob
    private String                        gridLevelExpression;

    @Lob
    private String                        gridLevelExpressionId;

    /**
     * @return the gridLevelExpressionId
     */
    public String getGridLevelExpressionId() {
        return gridLevelExpressionId;
    }

    /**
     * @param gridLevelExpressionId
     *            the gridLevelExpressionId to set
     */
    public void setGridLevelExpressionId(String gridLevelExpressionId) {
        this.gridLevelExpressionId = gridLevelExpressionId;
    }

    /**
     * @return the gridLevelExpression
     */
    public String getGridLevelExpression() {
        return gridLevelExpression;
    }

    /**
     * @param gridLevelExpression
     *            the gridLevelExpression to set
     */
    public void setGridLevelExpression(String gridLevelExpression) {
        this.gridLevelExpression = gridLevelExpression;
    }

    /**
     * @return the assignmentFieldMetaDataList
     */
    public List<AssignmentFieldMetaData> getAssignmentFieldMetaDataList() {
        return assignmentFieldMetaDataList;
    }

    /**
     * @param assignmentFieldMetaDataList
     *            the assignmentFieldMetaDataList to set
     */
    public void setAssignmentFieldMetaDataList(List<AssignmentFieldMetaData> assignmentFieldMetaDataList) {
        this.assignmentFieldMetaDataList = assignmentFieldMetaDataList;
    }

    @Override
    protected void populate(BaseEntity baseEntity, CloneOptions cloneOptions) {
        AssignmentGrid assignmentGrid = (AssignmentGrid) baseEntity;
        super.populate(assignmentGrid, cloneOptions);

        if (assignmentFieldMetaDataList != null && assignmentFieldMetaDataList.size() > 0) {
            List<AssignmentFieldMetaData> newAssignmentFieldMetaDataList = new ArrayList<AssignmentFieldMetaData>();

            for (AssignmentFieldMetaData assignmentFieldMetaData : assignmentFieldMetaDataList) {
                newAssignmentFieldMetaDataList.add((AssignmentFieldMetaData) assignmentFieldMetaData
                        .cloneYourself(cloneOptions));
            }

            assignmentGrid.setAssignmentFieldMetaDataList(newAssignmentFieldMetaDataList);
        }

        assignmentGrid.setGridLevelExpression(gridLevelExpression);
        assignmentGrid.setGridLevelExpressionId(gridLevelExpressionId);
    }

    @Override
    protected void populateFrom(BaseEntity baseEntity, CloneOptions cloneOptions) {
        AssignmentGrid assignmentGrid = (AssignmentGrid) baseEntity;
        super.populateFrom(assignmentGrid, cloneOptions);
        if (assignmentGrid.getAssignmentFieldMetaDataList() != null
                && assignmentGrid.getAssignmentFieldMetaDataList().size() > 0) {
            this.getAssignmentFieldMetaDataList().clear();
            for (AssignmentFieldMetaData assignmentFieldMetaData : assignmentGrid.getAssignmentFieldMetaDataList()) {
                this.getAssignmentFieldMetaDataList().add(
                        (AssignmentFieldMetaData) assignmentFieldMetaData.cloneYourself(cloneOptions));
            }
        }
        this.setGridLevelExpression(assignmentGrid.getGridLevelExpression());
        this.setGridLevelExpressionId(assignmentGrid.getGridLevelExpressionId());

    }
}
