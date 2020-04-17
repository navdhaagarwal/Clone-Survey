package com.nucleus.master.audit.metadata;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Stack;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;

import com.nucleus.logging.BaseLoggers;
import com.nucleus.master.audit.service.util.MasterChangeGetterMethodMeta;
import com.nucleus.master.audit.service.util.MasterChangeSetterMethodMeta;
import com.nucleus.master.audit.service.util.MasterChangeTuple2;

public class AuditableClassTraversalUtility {

	public static Stack<MasterChangeTuple2<Method, Method>> getGetterMethodsFromRootToCurrentNode(Class className,
			String fieldName, AuditableEntityToBiDiTree bidiTree) {
		if (bidiTree != null && MapUtils.isNotEmpty(bidiTree.getFieldToNode())) {
			BiDiTreeNode node = bidiTree.getNodeByField(className, fieldName);
			if (node != null) {
				Stack<MasterChangeTuple2<Method, Method>> getterMethods = new Stack<>();
				getterOfParent(node, getterMethods);
				return getterMethods;
			}
		}
		return null;
	}

	private static void getterOfParent(BiDiTreeNode node, Stack<MasterChangeTuple2<Method, Method>> getterNames) {
		if (node.getParentNode() == null) {
			return;
		}
		getterNames.push(
				new MasterChangeTuple2<Method, Method>(node.getGetterMethodInParent(), node.getSetterMethodInparent()));
		getterOfParent(node.getParentNode(), getterNames);
	}

	public static Method getListGetMethod() {
		for (Method met : List.class.getDeclaredMethods()) {
			if (met.getName().equals("get")) {
				return met;
			}
		}
		return null;
	}
	
	public static Method getListSetMethod() {
		for (Method met : List.class.getDeclaredMethods()) {
			if (met.getName().equals("set")) {
				return met;
			}
		}
		return null;
	}

	public static Object executeGetterOfLazyObject(Object rootObject, Method getter) {
		try {
			return getter.invoke(rootObject);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			BaseLoggers.exceptionLogger.error(
					"Erro in invoking Method " + getter.getName() + " on Class " + rootObject.getClass().getName(), e);
		}
		return null;
	}
	
	public static Object executeGetterOfWithArgument(Object rootObject, Method getter,Object[] args) {
		try {
			return getter.invoke(rootObject,args);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			BaseLoggers.exceptionLogger.error(
					"Erro in invoking Method " + getter.getName() + " on Class " + rootObject.getClass().getName(), e);
		}
		return null;
	}

	public static Object executeSetterOfLazyObject(Object rootObject, Method setter, Object valueToSet) {
		try {
			return setter.invoke(rootObject, valueToSet);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			BaseLoggers.exceptionLogger.error(
					"Erro in invoking Method " + setter.getName() + " on Class " + rootObject.getClass().getName(), e);
		}
		return null;
	}

	public static Class getActualReference(Field f) {
		if (Collection.class.isAssignableFrom(f.getType())) {
			ParameterizedType type = (ParameterizedType) f.getGenericType();
			if (type != null) {
				Type[] types = type.getActualTypeArguments();
				if (types[0] instanceof Object) {
					return (Class) types[0];
				}
			}
		}

		return f.getType();

	}

	public static Method getGetterName(Field f, Class inputClass, String defaultGetterMethodName) {
		Method m = null;
		if (StringUtils.isNotEmpty(defaultGetterMethodName)) {
			try {
				m = inputClass.getMethod(defaultGetterMethodName);
				return m;
			} catch (NoSuchMethodException | SecurityException e) {
				//ignore
			}
		}
		// try with get+fieldname
		try {
			m = inputClass.getMethod("get" + StringUtils.capitalize(f.getName()));
			return m;
		} catch (NoSuchMethodException | SecurityException e) {
			// try with Is
		}
		try {
			m = inputClass.getMethod("is" + StringUtils.capitalize(f.getName()));
			return m;
		} catch (NoSuchMethodException | SecurityException e1) {
			// ignore
		}
		for (Method method : inputClass.getMethods()) {
			if (validMethod(f, method)) {
				m = method;
				break;
			}
		}
		if (m != null) {
			return m;
		}
		return null;
	}

	private static boolean validMethod(Field f, Method method) {
		return method.getReturnType() != null && method.getReturnType().isAssignableFrom(f.getType())
				&& method.getName().toLowerCase().contains(f.getName().toLowerCase());
	}

	public static Method getSetterName(Field f, Class inputClass, String defaultSetterMethodName) {
		Method m = null;
		if (StringUtils.isNotEmpty(defaultSetterMethodName)) {
			try {
				m=inputClass.getMethod(defaultSetterMethodName, f.getType());
				return m;
			} catch (NoSuchMethodException | SecurityException e) {
				//ignore
			}
		}
		// try with get+fieldname
		try {
			m = inputClass.getMethod("set" + StringUtils.capitalize(f.getName()), f.getType());
			return m;
		} catch (NoSuchMethodException | SecurityException e) {
			// ignore

			/*if(defaultSetterMethodName.contains("is") ) {
				String tempMethodName  = defaultSetterMethodName;
				tempMethodName = tempMethodName.replace("is","");
				for (Method method : inputClass.getMethods()) {

					if (method.getParameterCount() == 1 && method.getParameters()[0].getType().isAssignableFrom(f.getType())
							&& method.getName().toLowerCase().contains(tempMethodName.toLowerCase())) {
						m = method;
						break;
					}
				}
			}*/
		}
		for (Method method : inputClass.getMethods()) {

			if (method.getParameterCount() == 1 && method.getParameters()[0].getType().isAssignableFrom(f.getType())
					&& method.getName().toLowerCase().contains(defaultSetterMethodName.toLowerCase())) {
				m = method;
				break;
			}
		}
		if (m != null) {
			return m;
		}
		return null;
	}

	public static Stack<MasterChangeTuple2<MasterChangeGetterMethodMeta, MasterChangeSetterMethodMeta>> getGetSetByFieldListInReverseOrder(
			List<String> pathList, Object rootObject, AuditableEntityToBiDiTree tree) throws Exception {
		if (CollectionUtils.isNotEmpty(pathList) && rootObject != null && tree != null) {
			Stack<MasterChangeTuple2<MasterChangeGetterMethodMeta, MasterChangeSetterMethodMeta>> getSetStack = new Stack<>();
			for (String path : pathList) {
				rootObject = buildStackInRecursion(rootObject, tree, getSetStack, path);
				if(rootObject == null){
					break;
				}
			}
			return getSetStack;
		}

		return null;
	}

	public static Stack<MasterChangeTuple2<MasterChangeGetterMethodMeta, MasterChangeSetterMethodMeta>> getGetSetByFieldListInOrder(
			List<String> pathList, Object rootObject, AuditableEntityToBiDiTree tree) throws Exception {
		Stack<MasterChangeTuple2<MasterChangeGetterMethodMeta, MasterChangeSetterMethodMeta>> getSetStack = new Stack<>();
		Stack<MasterChangeTuple2<MasterChangeGetterMethodMeta, MasterChangeSetterMethodMeta>> reverseStack = getGetSetByFieldListInReverseOrder(
				pathList, rootObject, tree);
		if (reverseStack != null) {
			while (!reverseStack.isEmpty()) {
				getSetStack.push(reverseStack.pop());
			}
		}
		return getSetStack;
	}

	private static Object buildStackInRecursion(Object rootObject, AuditableEntityToBiDiTree tree,
			Stack<MasterChangeTuple2<MasterChangeGetterMethodMeta, MasterChangeSetterMethodMeta>> getSetStack,
			String path) throws Exception {
		if (rootObject == null) {
			return null;
		}
		Object childObject = null;
		// handling for collection type data
		if (List.class.isAssignableFrom(rootObject.getClass())) {
			try {
				Integer pathInIntegre = Integer.parseInt(path);
				getSetStack.add(new MasterChangeTuple2<MasterChangeGetterMethodMeta, MasterChangeSetterMethodMeta>(
						new MasterChangeGetterMethodMeta(AuditableClassTraversalUtility.getListGetMethod(), path,new Object[]{pathInIntegre},true),
						new MasterChangeSetterMethodMeta(AuditableClassTraversalUtility.getListSetMethod())));
				//below handling is needed when object is deleted from list
				if(CollectionUtils.isEmpty((List)rootObject)){
					return null;
				}else if(((List)rootObject).size() <= pathInIntegre){
					return null;
				}
				childObject = AuditableClassTraversalUtility.getListGetMethod().invoke(rootObject,pathInIntegre);
			} catch (NumberFormatException e) {
				throw new Exception("path is not Integer " + path + " for List data" + rootObject.getClass());
			}
		}else{
			BiDiTreeNode node = tree.getNodeByField(rootObject.getClass(), path);
			// check if we have to parse in list
			getSetStack.add(new MasterChangeTuple2<MasterChangeGetterMethodMeta, MasterChangeSetterMethodMeta>(
					new MasterChangeGetterMethodMeta(node.getGetterMethodInParent(), path),
					new MasterChangeSetterMethodMeta(node.getSetterMethodInparent())));
			childObject = node.getGetterMethodInParent().invoke(rootObject);
		}
		
		return childObject;
	}
}
