package com.nucleus.master.audit.metadata;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.apache.commons.collections.MapUtils;
import org.hibernate.proxy.HibernateProxy;
import org.springframework.stereotype.Component;

import com.nucleus.entity.BaseEntity;
import com.nucleus.master.BaseMasterEntity;
import com.nucleus.master.audit.MasterChangeDisjointChildEntityHolder;
import com.nucleus.master.audit.MasterChangeEntityHolder;
import com.nucleus.master.audit.service.util.MasterChangeTuple2;
import com.nucleus.persistence.HibernateUtils;

@Component("auditableClassReferenceInitlizer")
public class AuditableClassReferenceInitlizer {

	public void intializedReference(AuditableClassMetadata classMeta,
			MasterChangeEntityHolder oldEntiy, MasterChangeEntityHolder newEntity, Class inputClass) {
		if (classMeta != null) {
			// searching in field metadata
			Map<String, AuditableClassFieldMetadata> includedField = classMeta.getIncludedFieldMetadata();
			if (MapUtils.isNotEmpty(includedField)) {
				includedField.forEach((fieldName, fieldMeta) -> {
					if (fieldMeta.isReferenceField()) {
						Stack<MasterChangeTuple2<Method, Method>> gettersFromRootForOldEntity = AuditableClassTraversalUtility.getGetterMethodsFromRootToCurrentNode(inputClass, fieldName,
								oldEntiy.getMetadataFactory().getOutputBiDiTree());
						// creating clone for new entity parsing
						Stack<MasterChangeTuple2<Method, Method>> gettersFromRootForNewEntity = (Stack<MasterChangeTuple2<Method, Method>>) gettersFromRootForOldEntity.clone();
						oldEntiy.setRootEntity((BaseMasterEntity)invokeGetterInSeries(gettersFromRootForOldEntity, (Object) oldEntiy.getRootEntity()));
						newEntity.setRootEntity((BaseMasterEntity) invokeGetterInSeries(gettersFromRootForNewEntity, (Object) newEntity.getRootEntity()));
					}
				});
			}
			// searching in object as value
			Map<String, AuditableClassMetadata> valueObject = classMeta.getValueAsObjectList();
			if (MapUtils.isNotEmpty(valueObject)) {
				valueObject.forEach((fieldName, innerClassMeta) -> {
					intializedReference(innerClassMeta,oldEntiy, newEntity, innerClassMeta.getForClass());
				});
			}
		}
	}
	
	
	
	public void intializedReference(AuditableClassMetadata classMeta,
			MasterChangeDisjointChildEntityHolder oldEntiy, MasterChangeDisjointChildEntityHolder newEntity, Class inputClass) {
		if (classMeta != null) {
			// searching in field metadata
			Map<String, AuditableClassFieldMetadata> includedField = classMeta.getIncludedFieldMetadata();
			if (MapUtils.isNotEmpty(includedField)) {
				includedField.forEach((fieldName, fieldMeta) -> {
					if (fieldMeta.isReferenceField()) {
						Stack<MasterChangeTuple2<Method, Method>> gettersFromRootForOldEntity = AuditableClassTraversalUtility.getGetterMethodsFromRootToCurrentNode(inputClass, fieldName,
								oldEntiy.getMetadataFactory().getOutputBiDiTree());
						// creating clone for new entity parsing
						Stack<MasterChangeTuple2<Method, Method>> gettersFromRootForNewEntity = (Stack<MasterChangeTuple2<Method, Method>>) gettersFromRootForOldEntity.clone();
						oldEntiy.setDisJointEntity(invokeGetterInSeries(gettersFromRootForOldEntity, (Object) oldEntiy.getDisJointEntity()));
						newEntity.setDisJointEntity(invokeGetterInSeries(gettersFromRootForNewEntity, (Object) newEntity.getDisJointEntity()));
					}
				});
			}
			// searching in object as value
			Map<String, AuditableClassMetadata> valueObject = classMeta.getValueAsObjectList();
			if (MapUtils.isNotEmpty(valueObject)) {
				valueObject.forEach((fieldName, innerClassMeta) -> {
					intializedReference(innerClassMeta,oldEntiy, newEntity, innerClassMeta.getForClass());
				});
			}
		}
	}

	

	

	private Object invokeGetterInSeries(Stack<MasterChangeTuple2<Method, Method>> getterMethods, Object rootObject) {
		if (rootObject != null) {
			// actualy init getter if last
			if (getterMethods.empty()) {
				// represent last member in stack
				if (rootObject instanceof HibernateProxy) {
					return HibernateUtils.initializeAndUnproxy(rootObject);
				}
				return rootObject;
			}
			while (!getterMethods.empty()) {
				MasterChangeTuple2<Method, Method> currentGetSet = getterMethods.pop();
				Method getter = currentGetSet.get_1();
				Object result = AuditableClassTraversalUtility.executeGetterOfLazyObject(rootObject, getter);
				if(result == null) {
					break;
				}
				else {
					// if result is collection
					if (List.class.isAssignableFrom(result.getClass())) {
						List resultAsList = (List) result;
						for (int i = 0; i < resultAsList.size(); i++) {
							Stack<MasterChangeTuple2<Method, Method>> getSetCopy = (Stack<MasterChangeTuple2<Method, Method>>) getterMethods.clone();
							resultAsList.set(i, invokeGetterInSeries(getSetCopy, resultAsList.get(i)));
						}
						// clearing as clone is created for recursion
						getterMethods.clear();
					} else if (java.util.Set.class.isAssignableFrom(result.getClass())) {
						java.util.Set resultInSet = (java.util.Set)result;
						for (Object object : resultInSet) {
							Stack<MasterChangeTuple2<Method, Method>> getSetCopy = (Stack<MasterChangeTuple2<Method, Method>>) getterMethods.clone();
							object = invokeGetterInSeries(getSetCopy, object);
						}
						getterMethods.clear();
					} else if (Map.class.isAssignableFrom(result.getClass())) {
						Map resultInMap = (Map)result;
						for (Object key : resultInMap.keySet()) {
							Stack<MasterChangeTuple2<Method, Method>> getSetCopy = (Stack<MasterChangeTuple2<Method, Method>>) getterMethods.clone();
							resultInMap.put(key,invokeGetterInSeries(getSetCopy, resultInMap.get(key)));
						}
						getterMethods.clear();
					} else {
						result=HibernateUtils.initializeAndUnproxy(result);
						result = invokeGetterInSeries(getterMethods, result);
					}
					AuditableClassTraversalUtility.executeSetterOfLazyObject( rootObject, currentGetSet.get_2(), result);
				}
			}
		}
		return rootObject;
	}

	
}
