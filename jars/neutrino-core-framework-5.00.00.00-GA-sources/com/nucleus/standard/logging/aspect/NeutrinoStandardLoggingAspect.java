package com.nucleus.standard.logging.aspect;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.lang3.ClassUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.BeansException;

import com.nucleus.core.NeutrinoSpringAppContextUtil;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.standard.context.IExecutionContextHolder;
import com.nucleus.standard.logging.annotation.ParameterInterceptor;
import com.nucleus.standard.logging.annotation.ParameterInterceptor.LoggingContext;
import com.nucleus.standard.logging.annotation.ParameterInterceptors;
import com.nucleus.standard.logging.xmlbasedlogging.LoggingContextConstants;
import com.nucleus.standard.logging.xmlbasedlogging.LoggingContextController;
import com.nucleus.standard.logging.xmlbasedlogging.LoggingContextVO;

@Aspect
public class NeutrinoStandardLoggingAspect {

	private static IExecutionContextHolder executionContextHolder = null;

	@Resource
	private LoggingContextController loggingContextController;

	static {
		if (executionContextHolder == null) {
			try {
				executionContextHolder = NeutrinoSpringAppContextUtil
						.getBeanByName("executionContextHolder",
								IExecutionContextHolder.class);
			} catch (BeansException ex) {
				BaseLoggers.flowLogger.error(
						"ExecutionContextHolder not configured", ex);
			}
		}
	}

	private Object aroundAdviceForAnnotation(ProceedingJoinPoint pjp,
			ParameterInterceptors parameterInterceptors) throws Throwable {

		ParameterInterceptor[] paraInterceptorArray = parameterInterceptors
				.value();

		for (ParameterInterceptor param : paraInterceptorArray) {

			Object object;
			if (param.index() > 0) {
				object = pjp.getArgs()[param.index() - 1];

				if (ClassUtils.isPrimitiveOrWrapper(object.getClass())
						|| object instanceof String) {
					addToContext(param.key(), param.name(),
							param.loggingContext(), object);
				} else {
					addNonBasicObjectDataToContext(param.key(), param.name(),
							param.loggingContext(), object);
				}				
			}
		}

		Object result = pjp.proceed();

		executionContextHolder.removeFromLocalContext();
		
		return result;

	}
	
	private LoggingContext getLoggingContext(String loggingContextString){
		LoggingContext loggingContext;
		if (loggingContextString == null
				|| "".equals(loggingContextString)) {
			loggingContext = LoggingContext.LOCAL;
		} else {
			loggingContext = LoggingContext
					.valueOf(loggingContextString);
		}
		
		return loggingContext;
	}
	
	private int getIndexValue(String indexString,LoggingContextVO loggingContextVO){
		int index;
		if (indexString == null || "".equals(indexString)) {
			index = 0;
		} else {
			index = Integer.parseInt(loggingContextVO.getIndex());
		}
		return index;		
	}
	
	private void addLoggingDataToContext(String key, String name,
			LoggingContext loggingContext, Object object) throws Throwable{
		
		if (ClassUtils.isPrimitiveOrWrapper(object.getClass())
				|| object instanceof String) {

			addToContext(key, name, loggingContext, object);

		} else {
			addNonBasicObjectDataToContext(key, name,
					loggingContext, object);
		}
		
	}

	private Object aroundAdviceForXMLBased(ProceedingJoinPoint pjp,
			List<LoggingContextVO> listOfLoggingContextVO) throws Throwable {

		if (loggingContextController == null) {
			return pjp.proceed();
		}

		if (listOfLoggingContextVO != null && !listOfLoggingContextVO.isEmpty()) {
			Iterator<LoggingContextVO> itr = listOfLoggingContextVO.iterator();

			while (itr.hasNext()) {
				LoggingContextVO loggingContextVO = itr.next();

				String name = loggingContextVO.getName();
				String key = loggingContextVO.getKey();
				String loggingContextString = loggingContextVO
						.getLoggingContext();

				LoggingContext loggingContext = getLoggingContext(loggingContextString);
				

				String indexString = loggingContextVO.getIndex();
				int index = getIndexValue(indexString,loggingContextVO);
				Object object;

				if (index > 0) {
					object = pjp.getArgs()[index - 1];
					
					addLoggingDataToContext(key, name, loggingContext, object);
				}
			}

		}

		Object result = pjp.proceed();
		executionContextHolder.removeFromLocalContext();
		
		return result;

	}

	public Object aroundAdvice(ProceedingJoinPoint pjp) throws Throwable {
		MethodSignature signature = (MethodSignature) pjp.getSignature();
		Method method = signature.getMethod();
		ParameterInterceptors parameterInterceptors = method
				.getAnnotation(ParameterInterceptors.class);

		String simpleClassName = pjp.getSignature().getDeclaringTypeName();
		String methodName = pjp.getSignature().getName();

		List<LoggingContextVO> listOfLoggingContextVO =  loggingContextController
				.getLoggingContextMapFromCache(simpleClassName
						+ LoggingContextConstants.UNDERSCORE + methodName);
		


		if (listOfLoggingContextVO != null && listOfLoggingContextVO.size() > 1) {

			aroundAdviceForXMLBased(pjp, listOfLoggingContextVO);

		} else if (parameterInterceptors != null) {

			aroundAdviceForAnnotation(pjp, parameterInterceptors);

		}
		return pjp.proceed();
	}

	private void addToContext(String key, String name,
			LoggingContext loggingContext, Object value) {

		String contextKey;
		if (value != null) {

			if (key != null && !("").equals(key)) {
				contextKey = key;
			} else {
				contextKey = name;
			}

			if (loggingContext
					.equals(ParameterInterceptor.LoggingContext.GLOBAL)) {
				executionContextHolder.addToGlobalContext(contextKey, value);
			} else {

				long startTime = System.nanoTime();
				executionContextHolder.addToLocalContext(contextKey, value);
				long endTime = System.nanoTime();
				long duration = endTime - startTime;
				BaseLoggers.flowLogger
						.debug("Total execution time to add to local context  "
								+ "() is " + duration + "ns");

			}
		}
	}

	private void addNonBasicObjectDataToContext(String key, String name,
			LoggingContext loggingContext, Object object)
			throws IllegalAccessException, InvocationTargetException,
			NoSuchMethodException {
		Object value = new Object();
		String paramComplexName = name;
		if (object != null) {
			if (paramComplexName != null && paramComplexName.contains(".")) {
				String paramComplexNames[] = paramComplexName.split("\\.");

				for (String paramSimpleName : paramComplexNames) {
					value = getValueFromObject(paramSimpleName, object);
					object = value;
				}
			} else {
				value = getValueFromObject(name, object);
			}

			addToContext(key, name, loggingContext, value);
		}
	}

	private Object getValueFromObject(String name, Object object) {
		if (object != null) {
			BeanWrapper beanWrapper = new BeanWrapperImpl(object);
			return beanWrapper.getPropertyValue(name);
		}

		return null;

	}

}
