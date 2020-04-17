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
import com.nucleus.rules.model.assignmentMatrix.AssignmentFieldMetaData;
import com.nucleus.rules.model.assignmentMatrix.AssignmentGrid;
import com.nucleus.rules.service.ParameterService;

@Component("rowValueMapDiffCalculator")
public class RowValueMapDiffCalculator extends MatrixJSONDiffCalculator {

	@Inject
	@Named("masterChangeMessageGenerationUtility")
	private MasterChangeMessageGenerationUtility messageGenerator;

	@Inject
	@Named("masterChangeJaversRegister")
	private MasterChangeJaversRegister register;

	@Inject
	@Named("parameterService")
	private ParameterService paramService;
	

	public RowValueMapDiffCalculator(MasterChangeJaversRegister register,
			MasterChangeMessageGenerationUtility messageGenerator, ParameterService paramService) {
		super();
		this.register = register;
		this.messageGenerator = messageGenerator;
		this.paramService = paramService;
	}

	@Override
	public void calculateDiff(Object oldValue, Object newValue, StringBuilder message, Object oldEntity,
			Object newEntity,
			Stack<MasterChangeTuple2<MasterChangeGetterMethodMeta, MasterChangeSetterMethodMeta>> getSetStack)
					throws Exception {
		// maintaining metadata
		AuditableClassMetadataFactory fct = register.getClassBuilder(newEntity.getClass());
		MasterChangePathResolverOutputMeta outMeta = new MasterChangePathResolverOutputMeta();
		messageGenerator.invokeGetterInSeriesAndExtractDataObject(getSetStack, newEntity,
				fct.getOutputBiDiTree(), outMeta, AssignmentGrid.class);
		AssignmentGrid grid = (AssignmentGrid) outMeta.getDataObject();
		List<AssignmentFieldMetaData> fieldMeta = grid.getAssignmentFieldMetaDataList();

		diffCalculatorForMatrix(newValue, oldValue, message, fieldMeta, paramService);
		// extracting fieldmetadata to know which is text and which is parameter

	}

}
