package com.nucleus.rule.master.audit;

import java.util.Stack;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.stereotype.Component;

import com.nucleus.master.audit.metadata.AuditableClassMetadataFactory;
import com.nucleus.master.audit.service.MasterChangeJaversRegister;
import com.nucleus.master.audit.service.diffmessage.MasterChangeMessageGenerationUtility;
import com.nucleus.master.audit.service.diffmessage.MasterChangePathResolverOutputMeta;
import com.nucleus.master.audit.service.util.MasterChangeFieldDiffCalculator;
import com.nucleus.master.audit.service.util.MasterChangeGetterMethodMeta;
import com.nucleus.master.audit.service.util.MasterChangeSetterMethodMeta;
import com.nucleus.master.audit.service.util.MasterChangeTuple2;
import com.nucleus.rules.model.assignmentMatrix.AssignmentExpression;

@Component("expressionChangeDiffCalculator")
public class ExpressionChangeDiffCalculator implements MasterChangeFieldDiffCalculator{

	@Inject
	@Named("masterChangeJaversRegister")
	private MasterChangeJaversRegister register;
	
	@Inject
	@Named("masterChangeMessageGenerationUtility")
	private MasterChangeMessageGenerationUtility messageGenerator;
	
	@Override
	public void calculateDiff(Object newValue, Object oldValue, StringBuilder message, Object oldEntity,
			Object newEntity,
			Stack<MasterChangeTuple2<MasterChangeGetterMethodMeta, MasterChangeSetterMethodMeta>> getSetStack)
					throws Exception {
		AuditableClassMetadataFactory fct = register.getClassBuilder(newEntity.getClass());
		MasterChangePathResolverOutputMeta outMeta = new MasterChangePathResolverOutputMeta();
		
		messageGenerator.invokeGetterInSeriesAndExtractDataObject(getSetStack, newEntity, fct.getOutputBiDiTree(),
				outMeta, AssignmentExpression.class);
		AssignmentExpression exp = (AssignmentExpression) outMeta.getDataObject();
		if(exp !=null){
			message
			.append(MasterChangeMessageGenerationUtility.FROM)
			.append(MasterChangeMessageGenerationUtility.BOLD_START
					+ MasterChangeMessageGenerationUtility.LEFT_SQ_BRACKET + oldValue.toString()
					+ MasterChangeMessageGenerationUtility.RIGHT_SQ_BRACKET
					+ MasterChangeMessageGenerationUtility.BOLD_END
					+ MasterChangeMessageGenerationUtility.TO
					+ MasterChangeMessageGenerationUtility.BOLD_START
					+ MasterChangeMessageGenerationUtility.LEFT_SQ_BRACKET + newValue.toString())
			.append(MasterChangeMessageGenerationUtility.RIGHT_SQ_BRACKET)
			.append(MasterChangeMessageGenerationUtility.BOLD_END)
			.append(MasterChangeMessageGenerationUtility.MESSAGE_PATH_BREAKER);
		}
	}

}
