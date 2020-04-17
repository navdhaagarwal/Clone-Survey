
package com.nucleus.rule.master.audit;

import java.util.Collections;
import java.util.Comparator;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import com.nucleus.master.audit.MasterChangeEntityHolder;
import com.nucleus.master.audit.metadata.BiDiTreeNodePointerByField;
import com.nucleus.master.audit.service.util.MasterChangeExecutionHelper;
import com.nucleus.rules.model.assignmentMatrix.AssignmentCriteriaSet;
import com.nucleus.rules.model.assignmentMatrix.AssignmentExpression;
import com.nucleus.rules.model.assignmentMatrix.AssignmentGrid;
import com.nucleus.rules.model.assignmentMatrix.AssignmentMaster;
import com.nucleus.rules.model.assignmentMatrix.AssignmentMatrixAction;
import com.nucleus.rules.model.assignmentMatrix.AssignmentMatrixRowData;
import com.nucleus.rules.model.assignmentMatrix.AssignmentSet;
import com.nucleus.rules.model.ruleMatrixMaster.RuleMatrixMaster;

@Component("rulematrixmasterChangeExecutionHelper")
public class RuleMatrixChangeExecutionHelper extends MasterChangeExecutionHelper{

	
	@Inject
	@Named("rowValueMapDiffCalculator")
	private RowValueMapDiffCalculator rowvalueDiffCalculator;
	
	@Inject
	@Named("assignActionValuesDiffCalculator")
	private AssignActionValuesDiffCalculator calculator;

	@Inject
	@Named("expressionChangeDiffCalculator")
	private ExpressionChangeDiffCalculator expDiffCalc;

	@PostConstruct
	public void init() {
		withCustomFieldDiffhandler(new BiDiTreeNodePointerByField(AssignmentMatrixRowData.class, "rowMapValues"),
				rowvalueDiffCalculator);
		withCustomFieldDiffhandler(new BiDiTreeNodePointerByField(AssignmentMatrixAction.class, "assignActionValues"),
				calculator);
		withCustomFieldDiffhandler(new BiDiTreeNodePointerByField(AssignmentMatrixRowData.class, "expression"), expDiffCalc);
		withFieldFormatterhandler(new BiDiTreeNodePointerByField(AssignmentGrid.class, "effectiveFrom"), getDateFormatterInstance());
		withFieldFormatterhandler(new BiDiTreeNodePointerByField(AssignmentGrid.class, "effectiveTill"), getDateFormatterInstance());
		withFieldFormatterhandler(new BiDiTreeNodePointerByField(AssignmentExpression.class, "effectiveFrom"), getDateFormatterInstance());
		withFieldFormatterhandler(new BiDiTreeNodePointerByField(AssignmentExpression.class, "effectiveTill"), getDateFormatterInstance());
		withFieldFormatterhandler(new BiDiTreeNodePointerByField(AssignmentCriteriaSet.class, "effectiveFrom"), getDateFormatterInstance());
		withFieldFormatterhandler(new BiDiTreeNodePointerByField(AssignmentCriteriaSet.class, "effectiveTill"), getDateFormatterInstance());
	}
	
	@Override
	public void preProcess(MasterChangeEntityHolder oldEntity, MasterChangeEntityHolder newEntity) {
		RuleMatrixMaster oldMaster = (RuleMatrixMaster)oldEntity.getRootEntity();
		RuleMatrixMaster newMaster = (RuleMatrixMaster)oldEntity.getRootEntity();
		if(oldMaster!=null && newMaster !=null){
			oldMaster.getAssignmentSet().sort((AssignmentSet _0, AssignmentSet _1)->_0.getPriority() - _1.getPriority());
			newMaster.getAssignmentSet().sort((AssignmentSet _0, AssignmentSet _1)->_0.getPriority() - _1.getPriority());
			
			if(CollectionUtils.isNotEmpty(oldMaster.getAssignmentSet())){
				for (AssignmentSet set : oldMaster.getAssignmentSet()) {
					if(CollectionUtils.isNotEmpty(set.getAssignmentMatrixRowData())){
						set.getAssignmentMatrixRowData().sort((AssignmentMatrixRowData _0, AssignmentMatrixRowData _1)-> _0.getPriority() - _1.getPriority());
					}
				}
			}
			if(CollectionUtils.isNotEmpty(newMaster.getAssignmentSet())){
				for (AssignmentSet set : newMaster.getAssignmentSet()) {
					if(CollectionUtils.isNotEmpty(set.getAssignmentMatrixRowData())){
						set.getAssignmentMatrixRowData().sort((AssignmentMatrixRowData _0, AssignmentMatrixRowData _1)-> _0.getPriority() - _1.getPriority());
					}
				}
			}
		}
	}
}
