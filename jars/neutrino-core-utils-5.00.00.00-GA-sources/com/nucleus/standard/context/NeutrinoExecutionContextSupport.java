package com.nucleus.standard.context;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.ClassUtils;
import org.slf4j.MDC;

class NeutrinoExecutionContextSupport {

	private static ThreadLocal<Map<Thread, Map<String, Object>>> threadLocalContextMap = new ThreadLocal<Map<Thread, Map<String, Object>>>();
	private static ThreadLocal<Map<String, Object>> threadGlobalContextMap = new InheritableThreadLocal<Map<String, Object>>(){
		@Override
		protected Map<String, Object> childValue(Map<String, Object> parentValue) {
			if(parentValue==null) return parentValue;
			Map<String,Object> childValue = new ConcurrentHashMap<>();
			childValue.putAll(parentValue);
			return childValue;
		}
	};

	private NeutrinoExecutionContextSupport() {

	}

	public static void addToLocalContext(String key, Object value) {
		if (threadLocalContextMap.get() == null) {
			threadLocalContextMap
					.set(new ConcurrentHashMap<Thread, Map<String, Object>>());
		}

		Map<String, Object> innerMap = threadLocalContextMap.get().get(
				Thread.currentThread());
		if (innerMap == null) {
			innerMap = new ConcurrentHashMap<String, Object>();
			threadLocalContextMap.get().put(Thread.currentThread(), innerMap);
		}
		innerMap.put(key, value);

	}

	public static Map<Thread, Map<String, Object>> getAllFromLocalContext() {
		Map<Thread, Map<String, Object>> localMap = new HashMap<Thread, Map<String, Object>>();
		if (threadLocalContextMap.get() != null) {
			localMap.putAll(threadLocalContextMap.get());
		}

		return localMap;
	}

	public static Object getFromLocalContext(String key) {
		Map<Thread, Map<String, Object>> localMap = new HashMap<Thread, Map<String, Object>>();
		if (threadLocalContextMap.get() != null) {
			localMap.putAll(threadLocalContextMap.get());
		}

		Map<String, Object> localInnerMap = localMap
				.get(Thread.currentThread());
		return localInnerMap.get(key);
	}

	public static void clearLocalContext() {
		if (threadLocalContextMap.get() != null) {
			threadLocalContextMap.get().clear();
		}
	}

	public static void removeFromLocalContext() {
		if (threadLocalContextMap.get() != null) {
			threadLocalContextMap.get().remove(Thread.currentThread());
		}
	}

	public static void addToGlobalContext(String key, Object value) {
		if(key==null || value ==null)
		{
			return;
		}
		if (ClassUtils.isPrimitiveOrWrapper(value.getClass())
				|| value instanceof String) {
			MDC.put(key, String.valueOf(value));
		}

		if (threadGlobalContextMap.get() == null) {
			threadGlobalContextMap.set(new ConcurrentHashMap<String, Object>());
		}

		threadGlobalContextMap.get().put(key, value);

	}

	public static Map<String, Object> getAllFromGlobalContext() {
		Map<String, Object> localMap = new HashMap<String, Object>();
		if (threadGlobalContextMap.get() != null) {
			localMap.putAll(threadGlobalContextMap.get());

		}
		return localMap;
	}

	public static Object getFromGlobalContext(String key) {
		if (threadGlobalContextMap.get() != null) {
			return threadGlobalContextMap.get().get(key);
		}
		return null;
	}

	public static void clearGlobalContext() {
		MDC.clear();
		if (threadGlobalContextMap.get() != null) {
			threadGlobalContextMap.get().clear();
		}
	}

	public static void removeFromGlobalContext(String key) {
		MDC.remove(key);
		if (threadGlobalContextMap.get() != null) {
			threadGlobalContextMap.get().remove(key);
		}
	}

}
