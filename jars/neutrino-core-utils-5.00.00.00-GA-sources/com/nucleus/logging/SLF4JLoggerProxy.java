package com.nucleus.logging;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Map;

import org.springframework.beans.BeansException;

import com.nucleus.core.NeutrinoSpringAppContextUtil;
import com.nucleus.standard.context.IExecutionContextHolder;

public class SLF4JLoggerProxy implements InvocationHandler {

	private Object loggerInstance;
	private static final String ERROR = "error";
	private static final String SPACE = " ";
	private static final String COLON = ":";
	private static final String START = "<--**** ";
	private static final String END = " ****-->";
	private IExecutionContextHolder executionContextHolder = null;
    private static final String     ENCRYPTOR              = "com.nucleus.encryption";
    private static final String     INFO                   = "info";
    private static final String     WARN                   = "warn";
    private static final String     DEBUG                  = "debug";
    private static final String     STRING_PATH            = "java.lang.String";
    

	private SLF4JLoggerProxy(Object loggerInstance) {
		this.loggerInstance = loggerInstance;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable {

		if (executionContextHolder == null) {
			try {
				executionContextHolder = NeutrinoSpringAppContextUtil
						.getBeanByName("executionContextHolder",
								IExecutionContextHolder.class);
			} catch (BeansException ex) {
				BaseLoggers.flowLogger.info(
						"executionContextHolder not configured", ex);
			}
		}

		/*if (executionContextHolder != null && method.getName().equals(ERROR)) {
			java.util.Map<Thread, java.util.Map<String, Object>> threadWiseLogDataMap = executionContextHolder
					.getAllFromLocalContext();
			if (threadWiseLogDataMap != null) {
				Map<String, Object> innerMap = threadWiseLogDataMap.get(Thread
						.currentThread());

				StringBuilder sb = new StringBuilder();
				if (innerMap != null) {
					for (String key : innerMap.keySet()) {
						Object value = innerMap.get(key);
						sb.append(key + COLON + value + SPACE);
					}
					args[0] = START + sb.toString() + END + args[0];
				}

			}
		}*/
        if (ENCRYPTOR.equalsIgnoreCase(((org.slf4j.Logger) loggerInstance).getName())
                && (ERROR.equals(method.getName()) || INFO.equals(method.getName()) || WARN.equals(method.getName())
                        || DEBUG.equals(method.getName()))
                && args != null && args.length > 0 && method.getParameterTypes() != null
                && method.getParameterTypes().length > 0
                && STRING_PATH.equalsIgnoreCase(method.getParameterTypes()[0].getName())) {
            args = LogEncrypter.encrypt(args);
        }
        return method.invoke(loggerInstance, args);

	}

	public static Object newInstance(org.slf4j.Logger obj) {
		return java.lang.reflect.Proxy.newProxyInstance(obj.getClass()
				.getClassLoader(), obj.getClass().getInterfaces(),
				new SLF4JLoggerProxy(obj));
	}

}
