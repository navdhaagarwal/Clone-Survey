package com.nucleus.master.audit.metadata;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;

import org.apache.commons.lang3.StringUtils;
import org.javers.core.metamodel.clazz.EntityDefinition;
import org.javers.core.metamodel.clazz.EntityDefinitionBuilder;
import org.javers.core.metamodel.clazz.ValueObjectDefinition;
import org.javers.core.metamodel.clazz.ValueObjectDefinitionBuilder;

import com.nucleus.entity.BaseEntity;
import com.nucleus.master.audit.annotation.EmbedInAuditAsReference;
import com.nucleus.master.audit.annotation.EmbedInAuditAsValue;
import com.nucleus.master.audit.annotation.EmbedInAuditAsValueObject;
import com.nucleus.master.audit.annotation.NeutrinoAuditableDisJointChild;
import com.nucleus.master.audit.annotation.NeutrinoAuditableMaster;

public class AuditableClassMetadataFactory {

	private Class inputClass;

	private AuditableClassMetadata outputClassMetadata = new AuditableClassMetadata();

	private AuditableEntityToBiDiTree outputBiDiTree = new AuditableEntityToBiDiTree();

	public AuditableClassMetadataFactory(Class inputClass) {
		super();
		this.inputClass = inputClass;
	}

	public AuditableClassMetadataFactory startFactory(String... varargs) throws Exception {
		if (inputClass != null) {
			String identifierColumn = null;
			if(varargs!=null && varargs.length>0){
				for (String arg: varargs) {
					identifierColumn= arg;
				}
			}else{
				Annotation anno = inputClass.getAnnotation(NeutrinoAuditableMaster.class);
				if (anno == null) {
					throw new Exception("Class " + inputClass.getSimpleName() + " is Not Valid Candidate for this method");
				}
				identifierColumn = ((NeutrinoAuditableMaster) anno).identifierColumn();
				NeutrinoAuditableDisJointChild[] childs = ((NeutrinoAuditableMaster) anno).disJointChild();
			}
			// TODO parsing child
			outputClassMetadata.setForClass(inputClass);
			outputClassMetadata.setIdentifierColumn(identifierColumn);
			outputClassMetadata.addIncludedField(identifierColumn);
			BiDiTreeNode root = new BiDiTreeNode(identifierColumn);
			root.setForClass(inputClass);
			outputBiDiTree.setRootNode(root);
			extractInformationFromField(inputClass.getDeclaredFields(), inputClass, root, outputClassMetadata);
			outputClassMetadata.addEntityDefinitions(buildEntityDefFromMeta(outputClassMetadata));
			return this;
		}
		return null;
	}

	private void extractInformationFromField(Field[] inputField, Class inputClass, BiDiTreeNode parentNode,
			AuditableClassMetadata outputClassMetadata) throws Exception {
		if (inputField != null) {
			for (Field field : inputField) {
				field.setAccessible(true);
				if (field.isAnnotationPresent(EmbedInAuditAsValue.class)) {
					outputClassMetadata.addIncludedField(field.getName());
					parsePrimitiveValueField(field, inputClass, parentNode,
							field.getAnnotation(EmbedInAuditAsValue.class), outputClassMetadata);
				} else if (field.isAnnotationPresent(EmbedInAuditAsReference.class)) {
					outputClassMetadata.addIncludedField(field.getName());
					parseReferenceValueField(field, inputClass, parentNode,
							field.getAnnotation(EmbedInAuditAsReference.class), outputClassMetadata);
				} else if (field.isAnnotationPresent(EmbedInAuditAsValueObject.class)) {
					outputClassMetadata.addIncludedField(field.getName());
					parseValueAsObjectField(field, inputClass, parentNode,
							field.getAnnotation(EmbedInAuditAsValueObject.class), outputClassMetadata);
				}
			}
		}
		Class superClassRef = inputClass.getSuperclass();
		while (superClassRef != null && !superClassRef.isAssignableFrom(BaseEntity.class)) {
			extractInformationFromField(superClassRef.getDeclaredFields(), superClassRef, parentNode,
					outputClassMetadata);
			superClassRef = superClassRef.getSuperclass();
		}
	}

	private BiDiTreeNode parsePrimitiveValueField(Field f, Class inputClass, BiDiTreeNode parentNode,
			EmbedInAuditAsValue annotation, AuditableClassMetadata metadata) throws Exception {
		// creating child metadata
		AuditableClassFieldMetadata fieldMeta = new AuditableClassFieldMetadata();
		fieldMeta.setFieldName(f.getName());
		fieldMeta.setDisplayKeyKey(annotation.displayKey());
		fieldMeta.setDisplayKeyMessage(
				StringUtils.isNoneEmpty(annotation.displayValue()) ? annotation.displayValue() : f.getName());
		fieldMeta.setSkipInDisplay(annotation.skipInDisplay());
		metadata.addIncludedFieldMetadata(f.getName(), fieldMeta);
		return extractBasicInfo(f, inputClass, parentNode, annotation.getterName(), annotation.setterName(),fieldMeta);

	}

	private BiDiTreeNode parseReferenceValueField(Field f, Class inputClass, BiDiTreeNode parentNode,
			EmbedInAuditAsReference annotation, AuditableClassMetadata metadata) throws Exception {
		// creating child metadata
		AuditableClassFieldMetadata fieldMeta = new AuditableClassFieldMetadata();
		fieldMeta.setFieldName(f.getName());
		fieldMeta.setDisplayKeyKey(annotation.displayKey());
		fieldMeta.setDisplayKeyMessage(
				StringUtils.isNoneEmpty(annotation.displayValue()) ? annotation.displayValue() : f.getName());
		fieldMeta.setReferenceField(true);
		fieldMeta.setReferenceClass(AuditableClassTraversalUtility.getActualReference(f));
		fieldMeta.setColumnOfRefClass(
				StringUtils.isNoneEmpty(annotation.columnToDisplay()) ? annotation.columnToDisplay() : "code");
		fieldMeta.setIdentifierColumn("id");
		metadata.addIncludedFieldMetadata(f.getName(), fieldMeta);
		// entity definition to add in javers
		outputClassMetadata.addEntityDefinitions(
				EntityDefinitionBuilder.entityDefinition(AuditableClassTraversalUtility.getActualReference(f)).withShallowReference().build());
		return extractBasicInfo(f, inputClass, parentNode, annotation.getterName(), annotation.setterName(),fieldMeta);
	}

	private BiDiTreeNode parseValueAsObjectField(Field f, Class inputClass, BiDiTreeNode parentNode,
			EmbedInAuditAsValueObject annotation, AuditableClassMetadata metadata) throws Exception {
		
		// creating child metadata
		AuditableClassFieldMetadata fieldMeta = new AuditableClassFieldMetadata();
		fieldMeta.setFieldName(f.getName());
		fieldMeta.setDisplayKeyKey(annotation.displayKey());
		fieldMeta.setDisplayKeyMessage(
				StringUtils.isNoneEmpty(annotation.displayValue()) ? annotation.displayValue() : f.getName());
		fieldMeta.setValueObject(true);
		fieldMeta.setValueObjectClassInstance(AuditableClassTraversalUtility.getActualReference(f));
		fieldMeta.setColumnOfRefClass(
				StringUtils.isNoneEmpty(annotation.columnToDisplay()) ? annotation.columnToDisplay() : "code");
		fieldMeta.setIdentifierColumn(annotation.identifierColumn());
		fieldMeta.setSkipInDisplay(annotation.skipInDisplay());
		metadata.addIncludedFieldMetadata(f.getName(), fieldMeta);
		// calling field parsing of new child class
		Class valueObjectClassType = AuditableClassTraversalUtility.getActualReference(f);
		AuditableClassMetadata childClassMetaData = new AuditableClassMetadata();
		childClassMetaData.setForClass(valueObjectClassType);
		childClassMetaData.setIdentifierColumn(
				StringUtils.isNoneEmpty(annotation.identifierColumn()) ? annotation.identifierColumn() : "id");
		childClassMetaData.addIncludedField(childClassMetaData.getIdentifierColumn());
		metadata.addValueAsObjectList(f.getName(), childClassMetaData);
		BiDiTreeNode node = extractBasicInfo(f, inputClass, parentNode, annotation.getterName(),
				annotation.setterName(),fieldMeta);
		extractInformationFromField(valueObjectClassType.getDeclaredFields(), valueObjectClassType, node,
				childClassMetaData);
		// decide to add as entitydef or Value Object
		//if explictly defined as true then entityDef
		// if not then if class type is list then as entity def otherwise as value object
		if(annotation.addAsEntityDef() && Collection.class.isAssignableFrom(f.getType())){
			// entity definition to add in javers
			outputClassMetadata.addEntityDefinitions(buildEntityDefFromMeta(childClassMetaData));
		}else{
			outputClassMetadata.addValueObjects(buildValueDefFromMeta(childClassMetaData));
		}
		return parentNode;
	}

	private BiDiTreeNode extractBasicInfo(Field f, Class inputClass, BiDiTreeNode parentNode, String getterName,
			String setterName,AuditableClassFieldMetadata fieldMeta) throws Exception {
		// creating node for backtrack
		BiDiTreeNode node = new BiDiTreeNode(f.getName());
		node.setParentNode(parentNode);
		node.setFieldMetaData(fieldMeta);
		parentNode.addChildNode(node);
		BiDiTreeNodePointerByField pointer = new BiDiTreeNodePointerByField(inputClass, f.getName());
		outputBiDiTree.addFieldPointer(pointer, node);
		// extracting getter method name
		Method derivedGetterName = AuditableClassTraversalUtility.getGetterName(f, inputClass, getterName);
		if (derivedGetterName == null) {
			throw new Exception(
					"Unable to determine getter method for field ," + f.getName() + " in Class " + inputClass);
		}
		node.setGetterMethodInParent(derivedGetterName);
		//RofieldMeta.setDisplayGetterMethod(derivedGetterName);
		outputBiDiTree.addGetterToNode(new BiDiTreeNodePointerByGetter(inputClass, derivedGetterName), node);
		Method derivedSetterName = AuditableClassTraversalUtility.getSetterName(f, inputClass, setterName);
		if (derivedSetterName == null) {
			throw new Exception(
					"Unable to determine setter method for field ," + f.getName() + " in Class " + inputClass);
		}
		node.setSetterMethodInparent(derivedSetterName);

		return node;
	}

	

	private EntityDefinition buildEntityDefFromMeta(AuditableClassMetadata metadata) {
		return EntityDefinitionBuilder.entityDefinition(metadata.getForClass())
				.withIdPropertyName(metadata.getIdentifierColumn()).withIncludedProperties(metadata.getIncludedField())
				.build();
	}
	
	private ValueObjectDefinition buildValueDefFromMeta(AuditableClassMetadata metadata) {
		metadata.getIncludedField().remove("id");
		return ValueObjectDefinitionBuilder.valueObjectDefinition(metadata.getForClass())
				.withIncludedProperties(metadata.getIncludedField())
				.build();
	}

	public Class getInputClass() {
		return inputClass;
	}

	public AuditableClassMetadata getOutputClassMetadata() {
		return outputClassMetadata;
	}

	public AuditableEntityToBiDiTree getOutputBiDiTree() {
		return outputBiDiTree;
	}

	

}
