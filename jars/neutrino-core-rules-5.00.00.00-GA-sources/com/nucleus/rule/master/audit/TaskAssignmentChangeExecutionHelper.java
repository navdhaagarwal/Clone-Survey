package com.nucleus.rule.master.audit;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Stack;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.javers.core.diff.Change;
import org.javers.core.diff.Diff;
import org.javers.core.diff.changetype.map.EntryValueChange;
import org.javers.core.diff.changetype.map.MapChange;
import org.springframework.stereotype.Component;

import com.nucleus.core.team.entity.Team;
import com.nucleus.master.audit.MasterChangeEntityHolder;
import com.nucleus.master.audit.metadata.BiDiTreeNodePointerByField;
import com.nucleus.master.audit.service.diffmessage.MasterChangeMessageGenerationUtility;
import com.nucleus.master.audit.service.util.MasterChangeExecutionHelper;
import com.nucleus.master.audit.service.util.MasterChangeFieldDiffCalculator;
import com.nucleus.master.audit.service.util.MasterChangeGetterMethodMeta;
import com.nucleus.master.audit.service.util.MasterChangeSetterMethodMeta;
import com.nucleus.master.audit.service.util.MasterChangeTuple2;
import com.nucleus.persistence.BaseMasterDao;
import com.nucleus.rules.model.assignmentMatrix.AssignmentCriteriaSet;
import com.nucleus.rules.model.assignmentMatrix.AssignmentExpression;
import com.nucleus.rules.model.assignmentMatrix.AssignmentGrid;
import com.nucleus.rules.model.assignmentMatrix.AssignmentMatrixAction;
import com.nucleus.rules.model.assignmentMatrix.AssignmentMatrixRowData;
import com.nucleus.rules.model.assignmentMatrix.AssignmentSet;
import com.nucleus.rules.model.assignmentMatrix.TaskAssignmentMaster;
import com.nucleus.rules.taskAssignmentMaster.TeamColumnDataHandler;
import com.nucleus.rules.taskAssignmentMaster.UserColumnDataHandler;
import com.nucleus.user.User;

@Component("taskassignmentmasterChangeExecutionHelper")
public class TaskAssignmentChangeExecutionHelper extends MasterChangeExecutionHelper {

	@Inject
	@Named("rowValueMapDiffCalculator")
	private RowValueMapDiffCalculator rowvalueDiffCalculator;

	@Inject
	@Named("expressionChangeDiffCalculator")
	private ExpressionChangeDiffCalculator expDiffCalc;

	@Inject
	@Named("baseMasterDao")
	private BaseMasterDao dao;

	@PostConstruct
	public void init() {
		withCustomFieldDiffhandler(new BiDiTreeNodePointerByField(AssignmentMatrixRowData.class, "rowMapValues"),
				rowvalueDiffCalculator);
		withCustomFieldDiffhandler(new BiDiTreeNodePointerByField(AssignmentMatrixAction.class, "assignActionValues"),
				new ruleMatrixAssignmentActionDiffCalculator(dao));
		withCustomFieldDiffhandler(new BiDiTreeNodePointerByField(AssignmentMatrixRowData.class, "expression"),
				expDiffCalc);
		withFieldFormatterhandler(new BiDiTreeNodePointerByField(AssignmentGrid.class, "effectiveFrom"), getDateFormatterInstance());
		withFieldFormatterhandler(new BiDiTreeNodePointerByField(AssignmentGrid.class, "effectiveTill"), getDateFormatterInstance());
		withFieldFormatterhandler(new BiDiTreeNodePointerByField(AssignmentExpression.class, "effectiveFrom"), getDateFormatterInstance());
		withFieldFormatterhandler(new BiDiTreeNodePointerByField(AssignmentExpression.class, "effectiveTill"), getDateFormatterInstance());
		withFieldFormatterhandler(new BiDiTreeNodePointerByField(AssignmentCriteriaSet.class, "effectiveFrom"), getDateFormatterInstance());
		withFieldFormatterhandler(new BiDiTreeNodePointerByField(AssignmentCriteriaSet.class, "effectiveTill"), getDateFormatterInstance());
	}

	@Override
	public void preProcess(MasterChangeEntityHolder oldEntity, MasterChangeEntityHolder newEntity) {
		TaskAssignmentMaster oldMaster = (TaskAssignmentMaster) oldEntity.getRootEntity();
		TaskAssignmentMaster newMaster = (TaskAssignmentMaster) oldEntity.getRootEntity();
		if (oldMaster != null && newMaster != null) {
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

class ruleMatrixAssignmentActionDiffCalculator implements MasterChangeFieldDiffCalculator {

	
	private BaseMasterDao dao;

	public ruleMatrixAssignmentActionDiffCalculator(BaseMasterDao dao) {
		super();
		this.dao = dao;
	}

	@Override
	public void calculateDiff(Object newValue, Object oldValue, StringBuilder message, Object oldEntity,
			Object newEntity,
			Stack<MasterChangeTuple2<MasterChangeGetterMethodMeta, MasterChangeSetterMethodMeta>> getSetStack)
					throws Exception {
		Diff diff = compareJSONwithoutNesting(oldValue.toString(), newValue.toString());
		String intiMessage = message.toString();
		message.setLength(0);
		List<Change> changes = diff.getChanges();
		for (Change change : changes) {
			if (change instanceof MapChange) {
				
				MapChange mapChange = (MapChange) change;
				List<EntryValueChange> valueChange = mapChange.getEntryValueChanges();
				for (EntryValueChange entryValueChange : valueChange) {
					StringBuilder localMessage = new StringBuilder();
					// key can be uri for team or user only
					String key = entryValueChange.getKey().toString();
					String oldValueID = entryValueChange.getLeftValue().toString();
					String newValueID = entryValueChange.getRightValue().toString();
					if (key.equals("com.nucleus.user.User")) {
						localMessage.append(" User")
						.append(MasterChangeMessageGenerationUtility.FROM)
						.append(MasterChangeMessageGenerationUtility.BOLD_START
								+ MasterChangeMessageGenerationUtility.LEFT_SQ_BRACKET + getUserById(oldValueID)
								+ MasterChangeMessageGenerationUtility.RIGHT_SQ_BRACKET
								+ MasterChangeMessageGenerationUtility.BOLD_END
								+ MasterChangeMessageGenerationUtility.TO
								+ MasterChangeMessageGenerationUtility.BOLD_START
								+ MasterChangeMessageGenerationUtility.LEFT_SQ_BRACKET + getUserById(newValueID))
						.append(MasterChangeMessageGenerationUtility.RIGHT_SQ_BRACKET)
						.append(MasterChangeMessageGenerationUtility.BOLD_END)
						.append(MasterChangeMessageGenerationUtility.MESSAGE_PATH_BREAKER);
					} else if (key.equals("com.nucleus.core.team.entity.Team")) {
						localMessage.append(" Team")
						.append(MasterChangeMessageGenerationUtility.FROM)
						.append(MasterChangeMessageGenerationUtility.BOLD_START
								+ MasterChangeMessageGenerationUtility.LEFT_SQ_BRACKET + getTeamById(oldValueID)
								+ MasterChangeMessageGenerationUtility.RIGHT_SQ_BRACKET
								+ MasterChangeMessageGenerationUtility.BOLD_END
								+ MasterChangeMessageGenerationUtility.TO
								+ MasterChangeMessageGenerationUtility.BOLD_START
								+ MasterChangeMessageGenerationUtility.LEFT_SQ_BRACKET + getTeamById(newValueID))
						.append(MasterChangeMessageGenerationUtility.RIGHT_SQ_BRACKET)
						.append(MasterChangeMessageGenerationUtility.BOLD_END)
						.append(MasterChangeMessageGenerationUtility.MESSAGE_PATH_BREAKER);
					}
					message.append(intiMessage).append(localMessage);
				}
				
			}
		}

	}

	private String getUserById(String id) {
		if(id == null){
			return StringUtils.EMPTY;
		}
		switch (id) {
		case UserColumnDataHandler.lastDDEUser:
			return "last DDE User";
		case UserColumnDataHandler.leadOfLoggedInUser:
			return "lead Of Logged In User";
		case UserColumnDataHandler.leastLoadedUser:
			return "least Loaded User";
		case UserColumnDataHandler.loggedInUser:
			return "logged In User";
		case UserColumnDataHandler.loggedInUserWithPreviousTeam:
			return "logged In User With Previous Team";
		case UserColumnDataHandler.previousUser:
			return "previous User";
		case UserColumnDataHandler.teamLead:
			return "Team Lead";
		default:
			Object userName = dao.getColumnValueFromEntity(User.class, Long.parseLong(id), "username"); 
			return userName != null ? (String) userName : null;
		}
	}
	
	
	private String getTeamById(String id) {
		if(id == null){
			return StringUtils.EMPTY;
		}
		switch (id) {
		case TeamColumnDataHandler.leastLoadedTeam:
			return "least Loaded Team";
		case TeamColumnDataHandler.lastDDETeam:
			return "last DDE Team";
		default:
			Object userName = dao.getColumnValueFromEntity(Team.class, Long.parseLong(id), "name"); 
			return userName != null ? (String) userName : null;
		}
	}

}
