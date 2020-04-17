package com.nucleus.web.tag;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;

import com.nucleus.logging.BaseLoggers;

public class TagProtectionUtil {

	public static final String PASS_PHRASE = "PASS_PHRASE";
	public static final String UN_MODIFIABLE_KEY = "unmodifiableDatakey";
	public static final String SALT = "oiybECjo";
	public static final String AES = "AES";

	private static final String DELIMETER = ":-:";
	//	Logging constants
	private static final String LOG_SESSION_ID = "SESSION ID :";
	private static final String LOG_GOT_UNMODIFIABLE_DATA_KEY = " GOT unmodifiableDataKey :";
	private static final String LOG_NULL_LIST_FOR_URI = " GOT NULL param list for URI :";
	private static final String LOG_UNMODIFIABLE_DATA_KEY_IS_NULL = " UN MODIFIABLE DATA KEY IS NULL for URI :";

	
	private TagProtectionUtil(){}
	
	public static void addProtectedFieldToRequest(HttpServletRequest request, String key, String val){
		String sessionID = request.getSession().getId();
		StringBuilder data = new StringBuilder();
		data.append(key).append(DELIMETER).append(val);
		//				First get the attribute it might be already initialized, then append
		String unmodifiableDataKey = (String)request.getSession().getAttribute(UN_MODIFIABLE_KEY);
		if(unmodifiableDataKey!=null){
			BaseLoggers.securityLogger.debug(generateLogLine(LOG_SESSION_ID,sessionID,LOG_GOT_UNMODIFIABLE_DATA_KEY,unmodifiableDataKey));
			List<String> protectedParamList = (List<String>) request.getAttribute(unmodifiableDataKey);
			if(protectedParamList!=null){
				BaseLoggers.securityLogger.trace(generateLogLine(LOG_SESSION_ID,sessionID," GOT NOT NULL param list"));
				protectedParamList.add(data.toString());
				request.setAttribute(unmodifiableDataKey, protectedParamList);
			}else{
				BaseLoggers.securityLogger.debug(generateLogLine(LOG_SESSION_ID,sessionID,LOG_NULL_LIST_FOR_URI,request.getRequestURI()));
			}

		}else{
			BaseLoggers.securityLogger.warn(generateLogLine(LOG_SESSION_ID,sessionID,LOG_UNMODIFIABLE_DATA_KEY_IS_NULL,request.getRequestURI()));
		}


	}

	public static void addProtectedFieldToRequest(HttpServletRequest request, Map<String,String> fieldMap){
		String sessionID = request.getSession().getId();
		StringBuilder data = null;

		//				First get the attribute might be already initialized, then append
		String unmodifiableDataKey = (String)request.getSession().getAttribute(UN_MODIFIABLE_KEY);
		if(unmodifiableDataKey!=null){
			BaseLoggers.securityLogger.debug(generateLogLine(LOG_SESSION_ID,sessionID,LOG_GOT_UNMODIFIABLE_DATA_KEY,unmodifiableDataKey));
			List<String> protectedParamList = (List<String>) request.getAttribute(unmodifiableDataKey);
			if(protectedParamList!=null){
				BaseLoggers.securityLogger.trace(generateLogLine(LOG_SESSION_ID,sessionID," GOT NOT NULL param list"));
				for (Entry<String, String> entry : fieldMap.entrySet()) {
					data = new StringBuilder();
					data.append(entry.getKey()).append(DELIMETER).append(entry.getValue());
					protectedParamList.add(data.toString());
				}

				request.setAttribute(unmodifiableDataKey, protectedParamList);
			}else{
				BaseLoggers.securityLogger.debug(generateLogLine(LOG_SESSION_ID,sessionID,LOG_NULL_LIST_FOR_URI,request.getRequestURI()));
			}

		}else{
			BaseLoggers.securityLogger.warn(generateLogLine(LOG_SESSION_ID,sessionID,LOG_UNMODIFIABLE_DATA_KEY_IS_NULL,request.getRequestURI()));
		}

	}

	private static String generateLogLine(String... vars){

		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < vars.length; i++) {
			sb.append(vars[i]);
		}
		return sb.toString();
	}
}
