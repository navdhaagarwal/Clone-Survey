package com.nucleus.rule.master.audit;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.javers.core.diff.Change;
import org.javers.core.diff.Diff;
import org.javers.core.diff.changetype.map.EntryValueChange;
import org.javers.core.diff.changetype.map.MapChange;

import com.nucleus.master.audit.service.diffmessage.MasterChangeMessageGenerationUtility;
import com.nucleus.master.audit.service.util.MasterChangeFieldDiffCalculator;
import com.nucleus.rules.model.assignmentMatrix.AssignmentFieldMetaData;
import com.nucleus.rules.service.ParameterService;

public abstract class MatrixJSONDiffCalculator implements MasterChangeFieldDiffCalculator {

	public void diffCalculatorForMatrix(Object newValue, Object oldValue, StringBuilder message,
			List<AssignmentFieldMetaData> fieldMeta, ParameterService paramService) {
		if(oldValue == null && newValue !=null){
			message.append(MasterChangeMessageGenerationUtility.ADDED);
			return;
		}else if(newValue == null && oldValue !=null){
			message.append(MasterChangeMessageGenerationUtility.REMOVED);
			return;
		}
		String messageHead = message.toString();
		// clearling the message
		message.setLength(0);
		Diff mapDiff = compareJSONwithoutNesting(oldValue.toString(), newValue.toString());
		List<Change> change = mapDiff.getChanges();
		// here no new value can be add, as key is fixed so working with
		// entryValueChange only.
		for (Change chn : change) {
			if (chn instanceof MapChange) {
				
				MapChange mapChn = (MapChange) chn;
				List<EntryValueChange> valueChange = mapChn.getEntryValueChanges();
				// key check
				
				for (EntryValueChange valChn : valueChange) {
					StringBuilder localMessage = new StringBuilder();
					String keyId = valChn.getKey().toString();
					String oldMapping = valChn.getLeftValue().toString();
					String newMapping = valChn.getRightValue().toString();
					AssignmentFieldMetaData ifFieldMeta = fieldMeta.stream().filter(f -> {
						return f.getIndexId().equals(keyId);
					}).collect(Collectors.toList()).get(0);
					// multi value game need one more time check -> for parameter
					localMessage.append(MasterChangeMessageGenerationUtility.ONE_BLANK_SPACE)
							.append(ifFieldMeta.getFieldName())
							;
					if (oldMapping.contains("#") || newMapping.contains("#")) {
						List<String> oldIds = java.util.Arrays.asList(oldMapping.split("#"));
						List<String> oldIdsClone = new ArrayList<>(oldIds);
						List<String> newIds = java.util.Arrays.asList(newMapping.split("#"));
						for (String id : newIds) {
							if (oldIds.contains(id)) {
								oldIdsClone.remove(id);
							} else {
								localMessage
								.append(MasterChangeMessageGenerationUtility.ONE_BLANK_SPACE).append(MasterChangeMessageGenerationUtility.BOLD_START+MasterChangeMessageGenerationUtility.ADDED
												+ MasterChangeMessageGenerationUtility.LEFT_SQ_BRACKET
												+ ((ifFieldMeta.getParameterBased() != null
														&& ifFieldMeta.getParameterBased())
																? paramService
																		.getParametersFromCacheById(Long.parseLong(id))
																		.getCode()
																: id)
												+ MasterChangeMessageGenerationUtility.RIGHT_SQ_BRACKET)
										.append(MasterChangeMessageGenerationUtility.BOLD_END)
										;
							}
						}
						for (String id : oldIdsClone) {
							if (id != null) {
								localMessage
								.append(MasterChangeMessageGenerationUtility.ONE_BLANK_SPACE).append(MasterChangeMessageGenerationUtility.BOLD_START+MasterChangeMessageGenerationUtility.REMOVED 
												+ MasterChangeMessageGenerationUtility.LEFT_SQ_BRACKET
												+ ((ifFieldMeta.getParameterBased() != null
														&& ifFieldMeta.getParameterBased())
																? paramService
																		.getParametersFromCacheById(Long.parseLong(id))
																		.getCode()
																: id)
												+ MasterChangeMessageGenerationUtility.RIGHT_SQ_BRACKET)
										.append(MasterChangeMessageGenerationUtility.BOLD_END);
							}
						}
					} else {
						if (ifFieldMeta.getParameterBased() != null && ifFieldMeta.getParameterBased()) {
							String oldParameter = paramService.getParametersFromCacheById(Long.parseLong(oldMapping))
									.getCode();
							String newParameter = paramService.getParametersFromCacheById(Long.parseLong(newMapping))
									.getCode();
							localMessage
							.append(MasterChangeMessageGenerationUtility.ONE_BLANK_SPACE).append(MasterChangeMessageGenerationUtility.FROM)
									.append(MasterChangeMessageGenerationUtility.BOLD_START
											+ MasterChangeMessageGenerationUtility.LEFT_SQ_BRACKET + oldParameter
											+ MasterChangeMessageGenerationUtility.RIGHT_SQ_BRACKET
											+ MasterChangeMessageGenerationUtility.BOLD_END
											+ MasterChangeMessageGenerationUtility.TO
											+ MasterChangeMessageGenerationUtility.BOLD_START
											+ MasterChangeMessageGenerationUtility.LEFT_SQ_BRACKET + newParameter)
									.append(MasterChangeMessageGenerationUtility.RIGHT_SQ_BRACKET)
									.append(MasterChangeMessageGenerationUtility.BOLD_END);
						} else {
							localMessage
							.append(MasterChangeMessageGenerationUtility.ONE_BLANK_SPACE).append(MasterChangeMessageGenerationUtility.FROM)
							.append(MasterChangeMessageGenerationUtility.BOLD_START
									+ MasterChangeMessageGenerationUtility.LEFT_SQ_BRACKET + oldMapping
									+ MasterChangeMessageGenerationUtility.RIGHT_SQ_BRACKET
									+ MasterChangeMessageGenerationUtility.BOLD_END
									+ MasterChangeMessageGenerationUtility.TO
									+ MasterChangeMessageGenerationUtility.BOLD_START
									+ MasterChangeMessageGenerationUtility.LEFT_SQ_BRACKET + newMapping)
							.append(MasterChangeMessageGenerationUtility.RIGHT_SQ_BRACKET)
							.append(MasterChangeMessageGenerationUtility.BOLD_END);
						}
					}
					if(!localMessage.toString().isEmpty()){
						message.append((messageHead.endsWith(MasterChangeMessageGenerationUtility.ARROW)? messageHead
							:messageHead+MasterChangeMessageGenerationUtility.ARROW)+localMessage).append(MasterChangeMessageGenerationUtility.MESSAGE_PATH_BREAKER);
					}
				}
			}
		}
	}

}
