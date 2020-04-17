package com.nucleus.master.audit.service.diffmessage;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.Stack;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.javers.core.diff.Change;
import org.javers.core.diff.Diff;
import org.javers.core.diff.changetype.NewObject;
import org.javers.core.diff.changetype.ObjectRemoved;
import org.javers.core.diff.changetype.ReferenceChange;
import org.javers.core.diff.changetype.ValueChange;
import org.javers.core.diff.changetype.container.ContainerElementChange;
import org.javers.core.diff.changetype.container.ElementValueChange;
import org.javers.core.diff.changetype.container.ListChange;
import org.javers.core.diff.changetype.container.SetChange;
import org.javers.core.diff.changetype.container.ValueAddOrRemove;
import org.javers.core.diff.changetype.container.ValueAdded;
import org.javers.core.diff.changetype.container.ValueRemoved;
import org.javers.core.diff.changetype.map.EntryValueChange;
import org.javers.core.diff.changetype.map.MapChange;
import org.javers.core.metamodel.object.InstanceId;
import org.javers.core.metamodel.object.ValueObjectId;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import com.nucleus.entity.BaseEntity;
import com.nucleus.master.audit.MasterChangeDiffHolder;
import com.nucleus.master.audit.MasterChangeDisJointEntiyDiffHolder;
import com.nucleus.master.audit.MasterChangeEntityHolder;
import com.nucleus.master.audit.metadata.AuditableClassFieldMetadata;
import com.nucleus.master.audit.metadata.AuditableClassMetadataFactory;
import com.nucleus.master.audit.metadata.AuditableClassTraversalUtility;
import com.nucleus.master.audit.metadata.AuditableEntityToBiDiTree;
import com.nucleus.master.audit.metadata.BiDiTreeNode;
import com.nucleus.master.audit.metadata.BiDiTreeNodePointerByField;
import com.nucleus.master.audit.service.MasterChangeJaversRegister;
import com.nucleus.master.audit.service.util.MasterChangeExecutionHelper;
import com.nucleus.master.audit.service.util.MasterChangeFieldDiffCalculator;
import com.nucleus.master.audit.service.util.MasterChangeFieldFormatter;
import com.nucleus.master.audit.service.util.MasterChangeGetterMethodMeta;
import com.nucleus.master.audit.service.util.MasterChangeSetterMethodMeta;
import com.nucleus.master.audit.service.util.MasterChangeTuple2;
import com.nucleus.persistence.BaseMasterDao;

@Component("masterChangeMessageGenerationUtility")
public class MasterChangeMessageGenerationUtility {

	public static final String ONE_BLANK_SPACE = " ";
	public static final String RIGHT_SQ_BRACKET = "]";
	public static final String LEFT_SQ_BRACKET = "[";
	public static final String ARROW = " -> ";
	public static final String TO = " To : ";
	public static final String FROM = " From : ";
	public static final String BLANK = ONE_BLANK_SPACE;
	public static final String CHANGED = "Changed : ";
	public static final String ADDED = "Added : ";
	public static final String REMOVED = "Deleted : ";
	public static final String MESSAGE_PATH_BREAKER = "===";
	public static final String BOLD_START = "<b>";
	public static final String BOLD_END = "</b>";

	@Inject
	@Named("masterChangeJaversRegister")
	private MasterChangeJaversRegister register;

	@Inject
	@Named("baseMasterDao")
	private BaseMasterDao baseDao;

	@Inject
	@Named("messageSource")
	protected MessageSource messageSource;

	public void updateMessagesInDIffObject(MasterChangeDiffHolder diff, MasterChangeEntityHolder oldEntity,
			MasterChangeEntityHolder newEntity) throws Exception {
		if (diff != null) {
			if (diff.getDelta() != null && diff.getDelta().hasChanges()) {
				diff.addAllDeltaInString(postProcessMessages(generateChangeMessages(groupChangesByType(diff.getDelta()),
						oldEntity.getRootEntity(), newEntity.getRootEntity(), oldEntity.getMetadataFactory())));
			}
			if (diff.getDisJointEntityDiff() != null) {
				for (Entry<String, MasterChangeDisJointEntiyDiffHolder> dis : diff.getDisJointEntityDiff().entrySet()) {
					if (dis.getValue().getDelta().hasChanges()) {
						diff.addAllDeltaInString(generateChangeMessages(groupChangesByType(dis.getValue().getDelta()),
								oldEntity.getDisJointChildByName(dis.getKey()).getDisJointEntity(),
								newEntity.getDisJointChildByName(dis.getKey()).getDisJointEntity(), oldEntity.getDisJointChildByName(dis.getKey()).getMetadataFactory()));
					}
				}
			}
			if (CollectionUtils.isEmpty(diff.getDeltaInString())) {
				diff.addDeltaInString("No Change");
			}
		}
	}

	public List<String> generateChangeMessages(MasterChangeByType changes, Object oldEntity, Object newEntity,
			AuditableClassMetadataFactory classMeta) throws Exception {
		final List<String> changeMessages = new ArrayList<>();
		final List<InstanceId> newObjectUnTraced = new ArrayList<>();
		final List<InstanceId> deleteObjectUnTraced = new ArrayList<>();
		// processing need to be done in order with new and delete object
		if (changes != null) {
			if (CollectionUtils.isNotEmpty(changes.getNewObject())) {
				for (NewObject objectNew : changes.getNewObject()) {
					String changeMessage = generateMessageByChangeType(objectNew, oldEntity, newEntity, classMeta,
							newObjectUnTraced, deleteObjectUnTraced);
					if (StringUtils.isNotEmpty(changeMessage)) {
						changeMessage = removeEndingArrow(changeMessage);
						if(!changeMessages.contains(changeMessage)){
							changeMessages.add(changeMessage);
						}
						
					}
				}
			}
			if (CollectionUtils.isNotEmpty(changes.getRemoveObject())) {
				for (ObjectRemoved objectRemoved : changes.getRemoveObject()) {
					String changeMessage = generateMessageByChangeType(objectRemoved, oldEntity, newEntity, classMeta,
							newObjectUnTraced, deleteObjectUnTraced);
					if (StringUtils.isNotEmpty(changeMessage)) {
						changeMessage = removeEndingArrow(changeMessage);
						if(!changeMessages.contains(changeMessage)){
							changeMessages.add(changeMessage);
						}
					}
				}
			}
			if (CollectionUtils.isNotEmpty(changes.getRefChange())) {
				for (ReferenceChange objectRemoved : changes.getRefChange()) {
					String changeMessage = generateMessageByChangeType(objectRemoved, oldEntity, newEntity, classMeta,
							newObjectUnTraced, deleteObjectUnTraced);
					if (StringUtils.isNotEmpty(changeMessage)) {
						changeMessage = removeEndingArrow(changeMessage);
						if(!changeMessages.contains(changeMessage)){
							changeMessages.add(changeMessage);
						}
					}
				}
			}
			if (CollectionUtils.isNotEmpty(changes.getValueChanges())) {
				for (ValueChange objectRemoved : changes.getValueChanges()) {
					String changeMessage = generateMessageByChangeType(objectRemoved, oldEntity, newEntity, classMeta,
							newObjectUnTraced, deleteObjectUnTraced);
					if (StringUtils.isNotEmpty(changeMessage)) {
						changeMessage = removeEndingArrow(changeMessage);
						// when it call custome diff calculator, the messaged
						// could be more that one , the
						// seperator used to break the message is \^^/^
						if (changeMessage.contains(MESSAGE_PATH_BREAKER)) {
							changeMessages.addAll(Arrays.asList(changeMessage.split(MESSAGE_PATH_BREAKER)));
						} else {
							if(!changeMessages.contains(changeMessage)){
								changeMessages.add(changeMessage);
							}
						}
					}
				}
			}
			if (CollectionUtils.isNotEmpty(changes.getListChange())) {
				for (ListChange objectRemoved : changes.getListChange()) {
					String changeMessage = generateMessageByChangeType(objectRemoved, oldEntity, newEntity, classMeta,
							newObjectUnTraced, deleteObjectUnTraced);
					if (StringUtils.isNotEmpty(changeMessage)) {
						changeMessage = removeEndingArrow(changeMessage);
						if(!changeMessages.contains(changeMessage)){
							changeMessages.add(changeMessage);
						}
					}
				}
			}
			if (CollectionUtils.isNotEmpty(changes.getSetChanges())) {
				for (SetChange objectRemoved : changes.getSetChanges()) {
					String changeMessage = generateMessageByChangeType(objectRemoved, oldEntity, newEntity, classMeta,
							newObjectUnTraced, deleteObjectUnTraced);
					if (StringUtils.isNotEmpty(changeMessage)) {
						changeMessage = removeEndingArrow(changeMessage);
						if(!changeMessages.contains(changeMessage)){
							changeMessages.add(changeMessage);
						}
					}
				}
			}
			if (CollectionUtils.isNotEmpty(changes.getMapChanges())) {
				for (MapChange objectRemoved : changes.getMapChanges()) {
					String changeMessage = generateMessageByChangeType(objectRemoved, oldEntity, newEntity, classMeta,
							newObjectUnTraced, deleteObjectUnTraced);
					if (StringUtils.isNotEmpty(changeMessage)) {
						changeMessage = removeEndingArrow(changeMessage);
                        if (changeMessage.contains(MESSAGE_PATH_BREAKER)) {
                            changeMessages.addAll(Arrays.asList(changeMessage.split(MESSAGE_PATH_BREAKER)));
                        } else {
                            if(!changeMessages.contains(changeMessage)){
                                changeMessages.add(changeMessage);
                            }
                        }
					}
				}
			}
		}
		return changeMessages;
	}

	private String removeEndingArrow(String changeMessage) {
		if (changeMessage.endsWith(ARROW)) {
			changeMessage = changeMessage.substring(0, changeMessage.length() - ARROW.length());
		}
		return changeMessage;
	}

	public String generateMessageByChangeType(Change change, Object oldEntity, Object newEntity,
			AuditableClassMetadataFactory classMeta, List<InstanceId> newObjectUntraced,
			List<InstanceId> deleteObjectUntraced) throws Exception {
		StringBuilder messageBuilder = new StringBuilder();
		if (change instanceof ValueChange) {
			getValueChangeMessage(change, newEntity, oldEntity, messageBuilder, classMeta);
		} else if (change instanceof ReferenceChange) {
			getReferenceChangeMessage(change, newEntity, messageBuilder, classMeta);
		} else if (change instanceof ListChange) {
			getListChangeMessage(change, newEntity, messageBuilder, classMeta, newObjectUntraced, deleteObjectUntraced);
		} else if (change instanceof NewObject) {
			getNewObjectMessage(change, newEntity, messageBuilder, classMeta, newObjectUntraced);
		} else if (change instanceof ObjectRemoved) {
			getRemoveObjectMessage(change, newEntity, messageBuilder, classMeta, deleteObjectUntraced);
		} else if(change instanceof SetChange){
			getSetChangeMessage(change, newEntity, messageBuilder, classMeta, newObjectUntraced, deleteObjectUntraced);
		} else if(change instanceof MapChange){
			getMapChangeMessage(change, newEntity,oldEntity, messageBuilder, classMeta);
		}
		return messageBuilder.toString();
	}

	private void getValueChangeMessage(Change change, Object newEntity, Object oldEntity, StringBuilder messageBuilder,
			AuditableClassMetadataFactory classMeta) throws Exception {
		StringBuilder localMessage = new StringBuilder();
		ValueChange valChange = (ValueChange) change;
		MasterChangePathResolverOutputMeta resolverOutput = new MasterChangePathResolverOutputMeta();
		resolvePathAndCreateMessage(localMessage,
				new MasterChangePathResolverInputMeta(classMeta, valChange.getAffectedGlobalId(),
						valChange.getPropertyName(), valChange.getAffectedObject().get(), newEntity, CHANGED),
				resolverOutput);
		Object leftvalue = valChange.getLeft();
		Object rightValue = valChange.getRight();
		MasterChangeExecutionHelper executionHelper = register
				.getDiffHelperInstance(classMeta.getOutputClassMetadata().getForClass());
		boolean customDiffCalculatorNotRun = true;
		if (executionHelper != null) {
			// calling diff calculator if any
			MasterChangeFieldDiffCalculator fieldDiff = executionHelper.getfieldDiffCalculator(
					new BiDiTreeNodePointerByField(resolverOutput.getAffectedClass(), valChange.getPropertyName()));
			if (fieldDiff != null) {
				// in case custom diff run, it is expected that all message will
				// be generated by that only.
				Integer messageOriginalSize = localMessage.toString().length();
				fieldDiff.calculateDiff(leftvalue, rightValue, localMessage, oldEntity, newEntity,
						resolverOutput.getFieldStack());
				Integer messageOriginalChanges = localMessage.toString().length();
				if(messageOriginalChanges > messageOriginalSize){
					// diff found
					messageBuilder.append(localMessage);
				}
				customDiffCalculatorNotRun = false;
			}else{
				messageBuilder.append(localMessage);
			}
			// calling formatter if any
			MasterChangeFieldFormatter fieldFormatter = executionHelper.getFieldFormatter(
					new BiDiTreeNodePointerByField(resolverOutput.getAffectedClass(), valChange.getPropertyName()));
			if (fieldFormatter != null) {
				leftvalue = fieldFormatter.format(leftvalue);
				rightValue = fieldFormatter.format(rightValue);
			}
		}
		String leftValueInString =( leftvalue != null && StringUtils.isNotBlank(leftvalue.toString())) ? leftvalue.toString() : BLANK;
		String rightValueInString = ( rightValue != null && StringUtils.isNotBlank(rightValue.toString())) ? rightValue.toString() : BLANK;;
		if (customDiffCalculatorNotRun) {
			messageBuilder.append(FROM).append(BOLD_START).append(LEFT_SQ_BRACKET).append(leftValueInString)
					.append(RIGHT_SQ_BRACKET).append(BOLD_END).append(TO).append(BOLD_START).append(LEFT_SQ_BRACKET)
					.append(rightValueInString).append(RIGHT_SQ_BRACKET).append(BOLD_END).append(ONE_BLANK_SPACE);
		}
	}

	private void getMapChangeMessage(Change change, Object newEntity,Object oldEntity, StringBuilder messageBuilder,
									  AuditableClassMetadataFactory classMeta) throws Exception {
		//MapChange mapChange = (MapChange) change;
		/*mapChange.getEntryValueChanges().forEach((mapC)->{
		mapC.
		});*/
		String messageHead = messageBuilder.toString();
		// clearling the message
		messageBuilder.setLength(0);
		MapChange mapChn = (MapChange) change;
		List<EntryValueChange> valueChange = mapChn.getEntryValueChanges();
		// key check
        MasterChangePathResolverOutputMeta resolverOutput = new MasterChangePathResolverOutputMeta();

        for (EntryValueChange valChn : valueChange) {
            StringBuilder localMessage = new StringBuilder();
            String keyId = valChn.getKey().toString();
            String oldMapping = valChn.getLeftValue().toString();
            String newMapping = valChn.getRightValue().toString();
            resolvePathAndCreateMessage(localMessage,
                    new MasterChangePathResolverInputMeta(classMeta, mapChn.getAffectedGlobalId(),
                            mapChn.getPropertyName(), mapChn.getAffectedObject().get(), newEntity, CHANGED),
                    resolverOutput);
			localMessage.append(MasterChangeMessageGenerationUtility.ONE_BLANK_SPACE).append(ARROW).append(MasterChangeMessageGenerationUtility.ONE_BLANK_SPACE)
					.append(keyId);
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
			if(!localMessage.toString().isEmpty()){
				messageBuilder.append(localMessage.toString()).append(MasterChangeMessageGenerationUtility.MESSAGE_PATH_BREAKER);
			}
		}
	}

	private void getListChangeMessage(Change change, Object newEntity, StringBuilder messageBuilder,
			AuditableClassMetadataFactory classMeta, List<InstanceId> newObjectUntracked,
			List<InstanceId> deleteObjectUntracked) throws Exception {
		ListChange listChange = (ListChange) change;
		StringBuilder listChangeMessage = new StringBuilder();
		MasterChangePathResolverOutputMeta resolverOutput = new MasterChangePathResolverOutputMeta();
		resolvePathAndCreateMessage(listChangeMessage,
				new MasterChangePathResolverInputMeta(classMeta, listChange.getAffectedGlobalId(),
						listChange.getPropertyName(), listChange.getAffectedObject().get(), newEntity, CHANGED),
				resolverOutput);

		StringBuilder listMemberChangeMessage = new StringBuilder();
		List<ContainerElementChange> elementChanges = listChange.getChanges();
		if (CollectionUtils.isNotEmpty(elementChanges)) {
			for (ContainerElementChange containerElementChange : elementChanges) {
				if (containerElementChange instanceof ValueAdded) {
					Object actualObjectIdentifier = (((ValueAddOrRemove) containerElementChange).getValue());
					if (actualObjectIdentifier instanceof InstanceId
							&& newObjectUntracked.contains(actualObjectIdentifier)) {
						// check if this is valid add in untracker list
						// it means actually added
						parseAndCreateMessage(listMemberChangeMessage, classMeta, listChange.getAffectedObject().get().getClass(),
								listChange.getPropertyName(), actualObjectIdentifier,
								ADDED);
					}

				} else if (containerElementChange instanceof ValueRemoved) {
					Object actualObjectIdentifier = (((ValueAddOrRemove) containerElementChange).getValue());
					if (actualObjectIdentifier instanceof InstanceId
							&& deleteObjectUntracked.contains(actualObjectIdentifier)) {
						parseAndCreateMessage(listMemberChangeMessage, classMeta, listChange.getAffectedObject().get().getClass(),
								listChange.getPropertyName(), actualObjectIdentifier,
								REMOVED);
					}

				} else if (containerElementChange instanceof ElementValueChange) {
					Object actualLeftObjectIdentifier = ((ElementValueChange) containerElementChange).getLeftValue();
					if (actualLeftObjectIdentifier instanceof InstanceId
							&& deleteObjectUntracked.contains(actualLeftObjectIdentifier)) {
						parseAndCreateMessage(listMemberChangeMessage, classMeta, listChange.getAffectedObject().get().getClass(),
								listChange.getPropertyName(),
								actualLeftObjectIdentifier, REMOVED);
					} else if (actualLeftObjectIdentifier instanceof InstanceId
							&& newObjectUntracked.contains(actualLeftObjectIdentifier)) {
						// check if this is valid add in untracker list
						// it means actually added
						parseAndCreateMessage(listMemberChangeMessage, classMeta, listChange.getAffectedObject().get().getClass(),
								listChange.getPropertyName(),
								actualLeftObjectIdentifier, ADDED);
					}

					Object actualRightObjectIdentifier = ((ElementValueChange) containerElementChange).getRightValue();
					if (actualRightObjectIdentifier instanceof InstanceId
							&& deleteObjectUntracked.contains(actualRightObjectIdentifier)) {
						parseAndCreateMessage(listMemberChangeMessage, classMeta, listChange.getAffectedObject().get().getClass(),
								listChange.getPropertyName(),
								actualRightObjectIdentifier, REMOVED);
					} else if (actualRightObjectIdentifier instanceof InstanceId
							&& newObjectUntracked.contains(actualRightObjectIdentifier)) {
						// check if this is valid add in untracker list
						// it means actually added
						parseAndCreateMessage(listMemberChangeMessage, classMeta, listChange.getAffectedObject().get().getClass(),
								listChange.getPropertyName(),
								actualRightObjectIdentifier, ADDED);
					}

				}
			}
			if (StringUtils.isNotEmpty(listMemberChangeMessage.toString())) {
				messageBuilder.append(listChangeMessage.toString()).append(listMemberChangeMessage.toString());
			}
		}
	}
	
	private void getSetChangeMessage(Change change, Object newEntity, StringBuilder messageBuilder,
			AuditableClassMetadataFactory classMeta, List<InstanceId> newObjectUntracked,
			List<InstanceId> deleteObjectUntracked) throws Exception {
		SetChange setChange = (SetChange) change;
		StringBuilder listChangeMessage = new StringBuilder();
		MasterChangePathResolverOutputMeta resolverOutput = new MasterChangePathResolverOutputMeta();
		resolvePathAndCreateMessage(listChangeMessage,
				new MasterChangePathResolverInputMeta(classMeta, setChange.getAffectedGlobalId(),
						setChange.getPropertyName(), setChange.getAffectedObject().get(), newEntity, CHANGED),
				resolverOutput);

		StringBuilder listMemberChangeMessage = new StringBuilder();
		List<ContainerElementChange> elementChanges = setChange.getChanges();
		if (CollectionUtils.isNotEmpty(elementChanges)) {
			for (ContainerElementChange containerElementChange : elementChanges) {
				if (containerElementChange instanceof ValueAdded) {
					Object actualObjectIdentifier = (((ValueAddOrRemove) containerElementChange).getValue());
					if (actualObjectIdentifier instanceof InstanceId
							&& newObjectUntracked.contains(actualObjectIdentifier)) {
						// check if this is valid add in untracker list
						// it means actually added
						parseAndCreateMessage(listMemberChangeMessage, classMeta, setChange.getAffectedObject().get().getClass(),
								setChange.getPropertyName(), actualObjectIdentifier,
								ADDED);
					}

				} else if (containerElementChange instanceof ValueRemoved) {
					Object actualObjectIdentifier = (((ValueAddOrRemove) containerElementChange).getValue());
					if (actualObjectIdentifier instanceof InstanceId
							&& deleteObjectUntracked.contains(actualObjectIdentifier)) {
						parseAndCreateMessage(listMemberChangeMessage, classMeta, setChange.getAffectedObject().get().getClass(),
								setChange.getPropertyName(), actualObjectIdentifier,
								REMOVED);
					}

				} else if (containerElementChange instanceof ElementValueChange) {
					Object actualLeftObjectIdentifier = ((ElementValueChange) containerElementChange).getLeftValue();
					if (actualLeftObjectIdentifier instanceof InstanceId
							&& deleteObjectUntracked.contains(actualLeftObjectIdentifier)) {
						parseAndCreateMessage(listMemberChangeMessage, classMeta, setChange.getAffectedObject().get().getClass(),
								setChange.getPropertyName(),
								actualLeftObjectIdentifier, REMOVED);
					} else if (actualLeftObjectIdentifier instanceof InstanceId
							&& newObjectUntracked.contains(actualLeftObjectIdentifier)) {
						// check if this is valid add in untracker list
						// it means actually added
						parseAndCreateMessage(listMemberChangeMessage, classMeta, setChange.getAffectedObject().get().getClass(),
								setChange.getPropertyName(),
								actualLeftObjectIdentifier, ADDED);
					}

					Object actualRightObjectIdentifier = ((ElementValueChange) containerElementChange).getRightValue();
					if (actualRightObjectIdentifier instanceof InstanceId
							&& deleteObjectUntracked.contains(actualRightObjectIdentifier)) {
						parseAndCreateMessage(listMemberChangeMessage, classMeta, setChange.getAffectedObject().get().getClass(),
								setChange.getPropertyName(),
								actualRightObjectIdentifier, REMOVED);
					} else if (actualRightObjectIdentifier instanceof InstanceId
							&& newObjectUntracked.contains(actualRightObjectIdentifier)) {
						// check if this is valid add in untracker list
						// it means actually added
						parseAndCreateMessage(listMemberChangeMessage, classMeta, setChange.getAffectedObject().get().getClass(),
								setChange.getPropertyName(),
								actualRightObjectIdentifier, ADDED);
					}

				}
			}
			if (StringUtils.isNotEmpty(listMemberChangeMessage.toString())) {
				messageBuilder.append(listChangeMessage.toString()).append(listMemberChangeMessage.toString());
			}
		}
	}

	private void parseAndCreateMessage(StringBuilder messageBuilder, AuditableClassMetadataFactory classMeta,
			Class changeClass,String fieldName,
			 Object actualObjectIdentifier, String init)
					throws ClassNotFoundException, IllegalAccessException, InvocationTargetException {
		InstanceId instId = (InstanceId) actualObjectIdentifier;
		Object actualObject = instId.getCdoId();
		String typeName = instId.getTypeName();
		// if reference then it should be long only
		if (actualObject instanceof Long) {
			BiDiTreeNode fieldNode = classMeta.getOutputBiDiTree()
					.getNodeByField(changeClass, fieldName);
			if (fieldNode != null) {
				AuditableClassFieldMetadata fieldMeta = fieldNode.getFieldMetaData();
				String displayValue = (String) baseDao.getColumnValueFromEntity(Class.forName(typeName),
						(Long) actualObject, fieldMeta.getColumnOfRefClass());
				messageBuilder.append(BOLD_START).append(LEFT_SQ_BRACKET).append(init).append(displayValue)
						.append(RIGHT_SQ_BRACKET).append(BOLD_END);
			}
		}
	}

	private void resolvePathAndCreateMessage(StringBuilder messageBuilder, MasterChangePathResolverInputMeta inMeta)
			throws Exception {
		resolvePathAndCreateMessage(messageBuilder, inMeta, new MasterChangePathResolverOutputMeta());

	}

	private void resolvePathAndCreateMessage(StringBuilder messageBuilder, MasterChangePathResolverInputMeta inMeta,
			MasterChangePathResolverOutputMeta outMeta) throws Exception {

		// change is returned from value object comparision
		if (inMeta.getAffectedGlobalObjectId() instanceof ValueObjectId) {
			ValueObjectId globalObjectAsValueObjet = (ValueObjectId) inMeta.getAffectedGlobalObjectId();
			String typeNameOfParent = globalObjectAsValueObjet.getOwnerId().getTypeName();
			Class parentClass = Class.forName(typeNameOfParent);
			// path is from parent
			if (parentClass.isAssignableFrom(inMeta.getClassMeta().getOutputBiDiTree().getRootNode().getForClass())) {
				Stack<MasterChangeTuple2<MasterChangeGetterMethodMeta, MasterChangeSetterMethodMeta>> getSetStack = AuditableClassTraversalUtility
						.getGetSetByFieldListInOrder(breakPathIntoSeries(globalObjectAsValueObjet.getFragment()),
								inMeta.getDataObject(), inMeta.getClassMeta().getOutputBiDiTree());
				outMeta.setFieldStack(
						(Stack<MasterChangeTuple2<MasterChangeGetterMethodMeta, MasterChangeSetterMethodMeta>>) getSetStack
								.clone());
				messageBuilder.append(inMeta.getInitBuilder() + invokeGetterInSeries(getSetStack,
						inMeta.getDataObject(), inMeta.getClassMeta().getOutputBiDiTree(), outMeta));
				if (inMeta.getFieldName() != null) {
					messageBuilder.append(getDisplayNameByFieldName(inMeta.getAffectedObject().getClass(),
							inMeta.getClassMeta().getOutputBiDiTree(), inMeta.getFieldName(),
							inMeta.getAffectedObject()));
				}
			}
		} // it means its an immediate property
		else if (inMeta.getAffectedGlobalObjectId() instanceof InstanceId) {
			AuditableClassFieldMetadata fieldMeta = inMeta.getClassMeta().getOutputClassMetadata()
					.getIncludedFieldMetadata().get(inMeta.getFieldName());
			messageBuilder.append(
					inMeta.getInitBuilder() + extractDisplayNameForField(inMeta.getAffectedObject(), fieldMeta));
			outMeta.setAffectedClass(inMeta.getAffectedObject().getClass());

		}
	}

	private List<String> breakPathIntoSeries(String nestedPathToObject) {
		if (StringUtils.isNotEmpty(nestedPathToObject)) {
			return java.util.Arrays.asList(nestedPathToObject.split("/"));
		}
		return null;
	}

	public String invokeGetterInSeries(
			Stack<MasterChangeTuple2<MasterChangeGetterMethodMeta, MasterChangeSetterMethodMeta>> getterMethods,
			Object rootObject, AuditableEntityToBiDiTree bidiTree, MasterChangePathResolverOutputMeta outMeta)
					throws Exception {
		return invokeGetterInSeriesAndExtractDataObject(getterMethods, rootObject, bidiTree, outMeta, null);

	}

	public String invokeGetterInSeriesAndExtractDataObject(
			Stack<MasterChangeTuple2<MasterChangeGetterMethodMeta, MasterChangeSetterMethodMeta>> getterMethods,
			Object rootObject, AuditableEntityToBiDiTree bidiTree, MasterChangePathResolverOutputMeta outMeta,
			Class outputCLassType) throws Exception {
		if (rootObject != null) {
			String message = StringUtils.EMPTY;
			// actualy init getter if last
			if (getterMethods.isEmpty()) {
				// represent last member in stack
				/// return getDisplayNameByFieldName(rootClass, bidiTree,
				// getterMethods.pop().get_1(), rootObject);
				outMeta.setAffectedClass(rootObject.getClass());
				return message;
			}
			while (!getterMethods.isEmpty()) {
				MasterChangeTuple2<MasterChangeGetterMethodMeta, MasterChangeSetterMethodMeta> currentGetSet = getterMethods
						.pop();
				Method getter = currentGetSet.get_1().getGetterMethod();
				Object result = null;
				if (currentGetSet.get_1().getGetterMethodInput() != null) {
					result = AuditableClassTraversalUtility.executeGetterOfWithArgument(rootObject, getter,
							currentGetSet.get_1().getGetterMethodInput());
				} else {
					result = AuditableClassTraversalUtility.executeGetterOfLazyObject(rootObject, getter);
				}
				if (outputCLassType != null && result.getClass().isAssignableFrom(outputCLassType)) {
					outMeta.setDataObject(result);
				}
				String joinKey = getDisplayNameByFieldName(rootObject.getClass(), bidiTree, getter, result);
				if (StringUtils.isEmpty(joinKey) && currentGetSet.get_1().isListMember()) {
					joinKey = "[ Row : " + (Integer.parseInt(currentGetSet.get_1().getPathName()) + 1)
							+ RIGHT_SQ_BRACKET;
				}
				message = (StringUtils.isNotEmpty(joinKey) ? joinKey + ARROW : StringUtils.EMPTY)
						+ invokeGetterInSeriesAndExtractDataObject(getterMethods, result, bidiTree, outMeta,
								outputCLassType);
			}
			return message;
		}
		return StringUtils.EMPTY;
	}

	private String getDisplayNameByFieldName(Class rootClass, AuditableEntityToBiDiTree bidiTree, Method getter,
			Object result) throws IllegalAccessException, InvocationTargetException {
		BiDiTreeNode node = bidiTree.getNodeByGetter(rootClass, getter);
		String message = StringUtils.EMPTY;
		if (node != null && node.getFieldMetaData() != null) {
			AuditableClassFieldMetadata fieldMeta = node.getFieldMetaData();
			if (fieldMeta.isSkipInDisplay()) {
				return message;
			}
			message = extractDisplayNameForField(result, fieldMeta);
		}
		return message;
	}

	private String getDisplayNameByFieldName(Class rootClass, AuditableEntityToBiDiTree bidiTree, String fieldName,
			Object result) throws IllegalAccessException, InvocationTargetException {
		BiDiTreeNode node = bidiTree.getNodeByField(rootClass, fieldName);
		String message = StringUtils.EMPTY;
		if (node != null && node.getFieldMetaData() != null) {
			AuditableClassFieldMetadata fieldMeta = node.getFieldMetaData();
			message = extractDisplayNameForField(result, fieldMeta);
		}
		return message;
	}

	private String extractDisplayNameForField(Object result, AuditableClassFieldMetadata fieldMeta)
			throws IllegalAccessException, InvocationTargetException {
		String message = StringUtils.EMPTY;
		if (fieldMeta.isSkipInDisplay()) {
			return message;
		}
		// message starter
		if (StringUtils.isNotBlank(fieldMeta.getDisplayKeyKey())) {
			message = messageSource.getMessage(fieldMeta.getDisplayKeyKey(), null, fieldMeta.getDisplayKeyMessage(),
					Locale.getDefault());
		}
		if (StringUtils.isEmpty(message)) {
			message = fieldMeta.getDisplayKeyMessage();
		}
		if (StringUtils.isEmpty(message)) {
			message = fieldMeta.getFieldName();
		}
		// append actual reference
		if (fieldMeta.getDisplayGetterMethod() != null) {
			message = message + LEFT_SQ_BRACKET + fieldMeta.getDisplayGetterMethod().invoke(result) + RIGHT_SQ_BRACKET;
		}
		return message;
	}

	private void getReferenceChangeMessage(Change change, Object oldEntity, StringBuilder messageBuilder,
			AuditableClassMetadataFactory classMeta) throws Exception {
		ReferenceChange refChange = (ReferenceChange) change;
		BiDiTreeNode fieldNode = classMeta.getOutputBiDiTree()
				.getNodeByField(refChange.getAffectedObject().get().getClass(), refChange.getPropertyName());
		// no impact in case it is a parent in change chain like Organization ->
		// branch calender (reference change)
		if (fieldNode.getFieldMetaData().isValueObject()) {
			return;
		}
		resolvePathAndCreateMessage(messageBuilder,
				new MasterChangePathResolverInputMeta(classMeta, refChange.getAffectedGlobalId(),
						refChange.getPropertyName(), refChange.getAffectedObject().get(), oldEntity, CHANGED));

		if (fieldNode != null) {
			AuditableClassFieldMetadata fieldMeta = fieldNode.getFieldMetaData();
			String code = fieldMeta.getColumnOfRefClass();
			String oldValue = BLANK;
			String newValue = BLANK;
			if (refChange.getLeftObject().isPresent()) {
				Object result = baseDao.getColumnValueFromEntity(refChange.getLeftObject().get().getClass(),
						((BaseEntity) refChange.getLeftObject().get()).getId(), code);
				if(result != null){
					oldValue = result.toString();
				}
			}
			if (refChange.getRightObject().isPresent()) {
				Object result = baseDao.getColumnValueFromEntity(refChange.getRightObject().get().getClass(),
						((BaseEntity) refChange.getRightObject().get()).getId(), code);
				if(result != null){
					newValue = result.toString();
				}
			}
			messageBuilder.append(FROM).append(BOLD_START).append(LEFT_SQ_BRACKET).append(oldValue)
					.append(RIGHT_SQ_BRACKET).append(BOLD_END).append(TO).append(BOLD_START).append(LEFT_SQ_BRACKET)
					.append(newValue).append(RIGHT_SQ_BRACKET).append(BOLD_END);
		}

	}

	private void getNewObjectMessage(Change change, Object oldEntity, StringBuilder messageBuilder,
			AuditableClassMetadataFactory classMeta, List<InstanceId> newObjectUntraced) throws Exception {
		NewObject newObj = (NewObject) change;
		if (newObj.getAffectedGlobalId() instanceof ValueObjectId) {
			ValueObjectId valueObjectId = (ValueObjectId) newObj.getAffectedGlobalId();
			// represent list operation, we can extract the same from list
			// change
			if (valueObjectId.getFragment().contains("#")) {
				return;
			}

			resolvePathAndCreateMessage(messageBuilder, new MasterChangePathResolverInputMeta(classMeta,
					newObj.getAffectedGlobalId(), null, newObj.getAffectedObject().get(), oldEntity, ADDED));
		} else if (newObj.getAffectedGlobalId() instanceof InstanceId) {
			newObjectUntraced.add((InstanceId) newObj.getAffectedGlobalId());
		}
	}

	private void getRemoveObjectMessage(Change change, Object oldEntity, StringBuilder messageBuilder,
			AuditableClassMetadataFactory classMeta, List<InstanceId> deleteObjectUntraced) throws Exception {
		ObjectRemoved deleteObj = (ObjectRemoved) change;
		if (deleteObj.getAffectedGlobalId() instanceof ValueObjectId) {
			ValueObjectId valueObjectId = (ValueObjectId) deleteObj.getAffectedGlobalId();
			// represent list operation, we can extract the same from list
			// change
			if (valueObjectId.getFragment().contains("#")) {
				return;
			}
			resolvePathAndCreateMessage(messageBuilder, new MasterChangePathResolverInputMeta(classMeta,
					deleteObj.getAffectedGlobalId(), null, deleteObj.getAffectedObject().get(), oldEntity, REMOVED));
		} else if (deleteObj.getAffectedGlobalId() instanceof InstanceId) {
			deleteObjectUntraced.add((InstanceId) deleteObj.getAffectedGlobalId());
		}
	}

	private MasterChangeByType groupChangesByType(Diff changes) {
		final MasterChangeByType changeByType = new MasterChangeByType();
		if (changes != null) {
			changes.getChanges().forEach((c) -> {
				if (c instanceof ValueChange) {
					changeByType.addValueChanges((ValueChange) c);
				} else if (c instanceof ReferenceChange) {
					changeByType.addRefChange((ReferenceChange) c);
				} else if (c instanceof ListChange) {
					changeByType.addListChange((ListChange) c);
				} else if (c instanceof NewObject) {
					changeByType.addNewObject((NewObject) c);
				} else if (c instanceof ObjectRemoved) {
					changeByType.addRemoveObject((ObjectRemoved) c);
				} else if(c  instanceof SetChange){
					changeByType.addSetChanges((SetChange)c);
				} else if(c  instanceof MapChange){
                    changeByType.addMapChanges((MapChange)c);
                }
			});
		}
		return changeByType;
	}

	private List<String> postProcessMessages(List<String> originalChangeMessages) {
		// removing child added messaged

		if (CollectionUtils.isNotEmpty(originalChangeMessages)) {
			List<String> cloneChangeMessage = new ArrayList<>(originalChangeMessages);
			for (String stringToCheck : cloneChangeMessage) {
				// added message cruncking
				// Added : assignmentSet -> [ Index :2] ->
				// assignmentMatrixRowData -> [ Index :1] -- delete
				// Added : assignmentSet -> [ Index :2] ->
				// assignmentActionFieldMetaDataList -> [ Index :2] -- delete
				// Added : assignmentSet -> [ Index :2] - remain
				if (stringToCheck.startsWith(MasterChangeMessageGenerationUtility.ADDED)) {
					for (Iterator iterator = originalChangeMessages.iterator(); iterator.hasNext();) {
						String stringToDelete = (String) iterator.next();
						if (!stringToCheck.equals(stringToDelete) && stringToDelete.startsWith(stringToCheck)) {
							iterator.remove();
						}
					}
				}
				// changes message crunching
				// Ch
				if (stringToCheck.startsWith(MasterChangeMessageGenerationUtility.REMOVED)) {
					for (Iterator iterator = originalChangeMessages.iterator(); iterator.hasNext();) {
						String stringToDelete = (String) iterator.next();
						if (!stringToCheck.equals(stringToDelete) && stringToDelete.startsWith(stringToCheck)) {
							iterator.remove();
						}
					}
				}
			}
		}
		return originalChangeMessages;
	}
}
