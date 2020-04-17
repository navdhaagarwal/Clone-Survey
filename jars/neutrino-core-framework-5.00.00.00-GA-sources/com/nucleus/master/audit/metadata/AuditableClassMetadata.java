package com.nucleus.master.audit.metadata;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.javers.core.JaversBuilder;
import org.javers.core.diff.ListCompareAlgorithm;
import org.javers.core.diff.custom.CustomValueComparator;
import org.javers.core.metamodel.clazz.EntityDefinition;
import org.javers.core.metamodel.clazz.EntityDefinitionBuilder;
import org.javers.core.metamodel.clazz.ValueObjectDefinition;
import org.joda.time.DateTime;

import com.nucleus.core.money.entity.Money;

public class AuditableClassMetadata {

	private List<String> includedField;
	
	private Map<String, AuditableClassFieldMetadata> includedFieldMetadata;
	
	private JaversBuilder javersBuilder = JaversBuilder.javers();
	
	private Map<String,AuditableClassMetadata> disJointChildMetadata;
	
	private List<EntityDefinition> entityDefinitions; 
	
	private List<ValueObjectDefinition> valueObjects;
	
	private Map<String,AuditableClassMetadata> valueAsObjectList;
	
	private Class forClass;
	
	private String identifierColumn;
	
	private String displayKeyKey;
	
	private String displayKeyMessage;

	public List<String> getIncludedField() {
		return includedField;
	}

	public void setIncludedField(List<String> includedField) {
		this.includedField = includedField;
	}
	
	public void addIncludedField(String includedField) {
		if(this.includedField ==null){
			this.includedField = new ArrayList<>();
		} 
		this.includedField.add(includedField);
	}

	public Map<String, AuditableClassFieldMetadata> getIncludedFieldMetadata() {
		return includedFieldMetadata;
	}

	public void setIncludedFieldMetadata(Map<String, AuditableClassFieldMetadata> includedFieldMetadata) {
		this.includedFieldMetadata = includedFieldMetadata;
	}
	
	
	public void addIncludedFieldMetadata(String fieldName, AuditableClassFieldMetadata includedFieldMetadata) {
		if( this.includedFieldMetadata == null){
			this.includedFieldMetadata = new HashMap<>();
		}
		this.includedFieldMetadata.put(fieldName, includedFieldMetadata);
	}

	public JaversBuilder getJaversBuilder() {
		return javersBuilder;
	}

	public void setJaversBuilder(JaversBuilder javersBuilder) {
		this.javersBuilder = javersBuilder;
	}

	public Map<String, AuditableClassMetadata> getDisJointChildMetadata() {
		return disJointChildMetadata;
	}

	public void setDisJointChildMetadata(Map<String, AuditableClassMetadata> disJointChildMetadata) {
		this.disJointChildMetadata = disJointChildMetadata;
	}
	
	public void addDisJointChildMetadata(String key, AuditableClassMetadata disJointChildMetadata) {
		if(this.disJointChildMetadata == null){
			this.disJointChildMetadata = new HashMap<>();
		}
		this.disJointChildMetadata.put(key, disJointChildMetadata);
	}

	public String getIdentifierColumn() {
		return identifierColumn;
	}

	public void setIdentifierColumn(String identifierColumn) {
		this.identifierColumn = identifierColumn;
	}

	public List<EntityDefinition> getEntityDefinitions() {
		return entityDefinitions;
	}

	public void setEntityDefinitions(List<EntityDefinition> entityDefinitions) {
		this.entityDefinitions = entityDefinitions;
	}
	
	public void addEntityDefinitions(EntityDefinition entityDefinition) {
		if( this.entityDefinitions == null){
			this.entityDefinitions = new ArrayList<>();
		}
		this.entityDefinitions.add(entityDefinition);
	}
	
	public JaversBuilder build() throws Exception{
		if(StringUtils.isEmpty(identifierColumn)){
			throw new Exception("Identifier Column can not be blank");
		}
		if(CollectionUtils.isEmpty(includedField) || CollectionUtils.isEmpty(entityDefinitions)){
			throw new Exception("Included field/entity def can not be blank");
		}
		if(!includedField.contains(identifierColumn)){
			throw new Exception("identifier column should be in included column");
		}
		EntityDefinition entityDef = EntityDefinitionBuilder.entityDefinition(forClass).withIdPropertyName(identifierColumn).withIncludedProperties(includedField).build();
		javersBuilder.registerEntity(entityDef);
		javersBuilder.registerValue(DateTime.class, new DateTimeCustomeComparator());
		javersBuilder.registerValue(Date.class,new DateCustomComparator());
		javersBuilder.registerValue(Money.class,new MoneyCustomComparator());
		entityDefinitions.forEach((d)->{
			if(!d.getBaseJavaClass().isAssignableFrom(forClass)){
				javersBuilder.registerEntity(d);
			}
		});
		if(CollectionUtils.isNotEmpty(valueObjects)){
			valueObjects.forEach((d)->{
				javersBuilder.registerValueObject(d);
			});
		}
		return javersBuilder.withListCompareAlgorithm(ListCompareAlgorithm.LEVENSHTEIN_DISTANCE);
	}

	public Class getForClass() {
		return forClass;
	}

	public void setForClass(Class forClass) {
		this.forClass = forClass;
	}

	public String getDisplayKeyKey() {
		return displayKeyKey;
	}

	public void setDisplayKeyKey(String displayKeyKey) {
		this.displayKeyKey = displayKeyKey;
	}

	public String getDisplayKeyMessage() {
		return displayKeyMessage;
	}

	public void setDisplayKeyMessage(String displayKeyMessage) {
		this.displayKeyMessage = displayKeyMessage;
	}

	public Map<String, AuditableClassMetadata> getValueAsObjectList() {
		return valueAsObjectList;
	}

	public void setValueAsObjectList(Map<String, AuditableClassMetadata> valueAsObjectList) {
		this.valueAsObjectList = valueAsObjectList;
	}
	
	public void addValueAsObjectList(String key, AuditableClassMetadata valueAsObjectList) {
		if(this.valueAsObjectList == null){
			this.valueAsObjectList = new HashMap<>();
		};
		this.valueAsObjectList.put(key, valueAsObjectList);
	}

	public List<ValueObjectDefinition> getValueObjects() {
		return valueObjects;
	}

	public void setValueObjects(List<ValueObjectDefinition> valueObjects) {
		this.valueObjects = valueObjects;
	}
	
	public void addValueObjects(ValueObjectDefinition valueObject) {
		if(this.valueObjects == null){
			this.valueObjects = new ArrayList<>();
		}
		this.valueObjects.add(valueObject);
	}
	
	
}

class DateTimeCustomeComparator implements CustomValueComparator<DateTime>{

	@Override
	public boolean equals(DateTime left, DateTime right) {
		if(left == null && right == null ){
			return true;
		}
		if(left !=null && right != null){
			return left.isEqual(right);
		}
		return false;
	}
}

class DateCustomComparator implements CustomValueComparator<Date>{

	@Override
	public boolean equals(Date left, Date right) {
		if(left == null && right == null ){
			return true;
		}
		if(left !=null && right != null){
			return left.compareTo(right) == 0;
		}
		return false;
	}
	
}


class MoneyCustomComparator implements CustomValueComparator<Money>{

	@Override
	public boolean equals(Money left, Money right) {
		if(left == null && right == null ){
			return true;
		}
		if(left !=null && right != null){
			return left.compareTo(right) == 0;
		}
		return false;
	}
	
}
