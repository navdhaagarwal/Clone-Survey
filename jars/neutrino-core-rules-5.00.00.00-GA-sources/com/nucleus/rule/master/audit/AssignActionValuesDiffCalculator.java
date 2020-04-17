package com.nucleus.rule.master.audit;

import java.util.List;
import java.util.Stack;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.stereotype.Component;

import com.nucleus.master.audit.metadata.AuditableClassMetadataFactory;
import com.nucleus.master.audit.service.MasterChangeJaversRegister;
import com.nucleus.master.audit.service.diffmessage.MasterChangeMessageGenerationUtility;
import com.nucleus.master.audit.service.diffmessage.MasterChangePathResolverOutputMeta;
import com.nucleus.master.audit.service.util.MasterChangeGetterMethodMeta;
import com.nucleus.master.audit.service.util.MasterChangeSetterMethodMeta;
import com.nucleus.master.audit.service.util.MasterChangeTuple2;
import com.nucleus.rules.model.assignmentMatrix.AssignmentCriteriaSet;
import com.nucleus.rules.model.assignmentMatrix.AssignmentExpression;
import com.nucleus.rules.model.assignmentMatrix.AssignmentFieldMetaData;
import com.nucleus.rules.model.assignmentMatrix.AssignmentGrid;
import com.nucleus.rules.service.ParameterService;

@Component("assignActionValuesDiffCalculator")
public class AssignActionValuesDiffCalculator extends MatrixJSONDiffCalculator {

	@Inject
	@Named("masterChangeMessageGenerationUtility")
	private MasterChangeMessageGenerationUtility messageGenerator;

	@Inject
	@Named("masterChangeJaversRegister")
	private MasterChangeJaversRegister register;

	@Inject
	@Named("parameterService")
	private ParameterService paramService;

	public AssignActionValuesDiffCalculator(MasterChangeJaversRegister register,
			MasterChangeMessageGenerationUtility messageGenerator, ParameterService paramService) {
		super();
		this.register = register;
		this.messageGenerator = messageGenerator;
		this.paramService = paramService;
	}

	@Override
	public void calculateDiff(Object newValue, Object oldValue, StringBuilder message, Object oldEntity,
			Object newEntity,
			Stack<MasterChangeTuple2<MasterChangeGetterMethodMeta, MasterChangeSetterMethodMeta>> getSetStack)
					throws Exception {
		AuditableClassMetadataFactory fct = register.getClassBuilder(newEntity.getClass());
		MasterChangePathResolverOutputMeta outMeta = new MasterChangePathResolverOutputMeta();
		// clone stack for expression type
		List<AssignmentFieldMetaData> fieldMeta = null;
		Stack<MasterChangeTuple2<MasterChangeGetterMethodMeta, MasterChangeSetterMethodMeta>> getSetStackCloneForExpression = (Stack<MasterChangeTuple2<MasterChangeGetterMethodMeta, MasterChangeSetterMethodMeta>>) getSetStack
				.clone();
		Stack<MasterChangeTuple2<MasterChangeGetterMethodMeta, MasterChangeSetterMethodMeta>> getSetStackCloneForCriteria = (Stack<MasterChangeTuple2<MasterChangeGetterMethodMeta, MasterChangeSetterMethodMeta>>) getSetStack
				.clone();
		messageGenerator.invokeGetterInSeriesAndExtractDataObject(getSetStack, newEntity, fct.getOutputBiDiTree(),
				outMeta, AssignmentGrid.class);
		AssignmentGrid grid = (AssignmentGrid) outMeta.getDataObject();
		if (grid != null) {
			// expressin type
			fieldMeta = grid.getAssignmentActionFieldMetaDataList();
		} else {
			messageGenerator.invokeGetterInSeriesAndExtractDataObject(getSetStackCloneForExpression, newEntity, fct.getOutputBiDiTree(),
					outMeta, AssignmentExpression.class);
			AssignmentExpression expression = (AssignmentExpression) outMeta.getDataObject();
			if (expression != null) {
				fieldMeta = expression.getAssignmentActionFieldMetaDataList();
			}else{
				messageGenerator.invokeGetterInSeriesAndExtractDataObject(getSetStackCloneForCriteria, newEntity, fct.getOutputBiDiTree(),
						outMeta, AssignmentCriteriaSet.class);
				AssignmentCriteriaSet ctiteria = (AssignmentCriteriaSet) outMeta.getDataObject();
				if(ctiteria !=null){
					fieldMeta = ctiteria.getAssignmentActionFieldMetaDataList();
				}
			}
		}
		if (fieldMeta != null) {
			diffCalculatorForMatrix(newValue, oldValue, message, fieldMeta, paramService);
		}
	}
	
	

}
