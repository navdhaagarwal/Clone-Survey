package com.nucleus.web.security;

import com.nucleus.logging.BaseLoggers;
import com.nucleus.master.BaseMasterUtils;
import com.nucleus.user.UserInfo;
import com.nucleus.web.security.browser.DelegatingServletInputStream;
import com.nucleus.web.security.servlet.api.NeutrinoRequestDispatcherWrapper;
import org.apache.commons.collections4.IteratorUtils;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.Collectors;

import static com.nucleus.web.security.AesUtil.PASS_PHRASE;

/**
 * This class will facilitate encryption while accepting data in request. This
 * will read all parameters from __hkstd_data_st which is encrypted JSON string
 * containing all the parameters, All other parameters in request body will be
 * ignored. This will protect parameter manipulation done by trapping request
 * launched from browser.
 * 
 * @author gajendra.jatav
 *
 */

public class NeutrinoRequestParamHolder extends HttpServletRequestWrapper {

	/**
	 * __hkstd_data_st is encrypted JSON string containing all parameters.
	 */
	public static final String REQUEST_DATA_PARAM = "__hkstd_data_st";

	public static final String PARAM_MANIPULATED_ERROR = "Parameter manipulation detected. ";

	/*
	If this header is set input stream will be decoded.
	 */
	public static final String ENCODED_JSON_HEADER = "JSON-Field";

	private JSONObject parameters = null;

	private String requestData = null;

	private Set<String> queryParams = new HashSet<>();

	private HttpServletRequest actualRequest;

	private byte[] streamContent;

	public NeutrinoRequestParamHolder(HttpServletRequest request) {
		super(request);
		this.actualRequest = request;
		parseQueryParams(request);
		if (request.getParameter(REQUEST_DATA_PARAM) == null) {
			this.parameters = new JSONObject();
			return;
		}
		try {
			this.requestData = AesUtil.decrypt(request.getParameter(REQUEST_DATA_PARAM),
					(String) request.getSession().getAttribute(PASS_PHRASE), true);
		} catch (Exception e) {
			UserInfo userInfo = BaseMasterUtils.getCurrentUser();
			StringBuilder userData = new StringBuilder();
			if (userInfo != null) {
				userData.append("UserId: " + userInfo.getId());
				userData.append("UserName: " + userInfo.getUsername());
			}
			throwParamManipulationException(
					"Not able to parse __hkstd_data_st may the case of parameter manipulation. Request user "
							+ userData,
					e);
		}
		try {
			this.parameters = new JSONObject(this.requestData);
		} catch (JSONException e) {
			throwParamManipulationException("Invalid JSON Found", e);
		}
	}

	private void parseQueryParams(HttpServletRequest request) {
		String queryString = request.getQueryString();
		if (StringUtils.isEmpty(queryString)) {
			return ;
		}
		String[] strParams = queryString.split("&");
		for (String param : strParams) {
			String name = param.split("=")[0];
			queryParams.add(name.trim());
		}
	}

	private void throwParamManipulationException(String message, Exception exception) {
		BaseLoggers.flowLogger.debug("Exception in NeutrinoRequestParamHolder {} {}", message, exception);
		throw new XssException(message);
	}

	@Override
	public String getParameter(String name) {

		try {
			String paramValue = null;

			if (this.parameters.has(name)) {

				Object values = this.parameters.get(name);

				if (values instanceof JSONArray) {
					String[] valueTypeArr = toStringArray(this.parameters.getJSONArray(name));
					if (valueTypeArr.length > 0) {
						paramValue = valueTypeArr[0];
					}
				} else {
					paramValue = this.parameters.getString(name);
				}

			} else if (queryParams.contains(name)) {
				paramValue = super.getParameter(name);
			}
			BaseLoggers.flowLogger.debug("Path {}\nNeutrinoRequestParamHolder::getParameter {} super:: {} custom:: {}",
					this.getRequestURI(), name, super.getParameter(name), paramValue);
			return paramValue;
		} catch (JSONException e) {
			return null;
		}
	}


	@Override
	public Enumeration<String> getParameterNames() {
		List<String> superParamsToInculde =
				Collections.list(super.getParameterNames())
				.stream()
				.filter(param -> queryParams.contains(param))
				.collect(Collectors.toList());
		List<String> paramList = IteratorUtils.toList(this.parameters.keys());
		paramList.addAll(superParamsToInculde);
		return Collections.enumeration(paramList);
	}

	@Override
	public ServletInputStream getInputStream() throws IOException {
		if ( ServletFileUpload.isMultipartContent(this.actualRequest) ||
				super.getInputStream() == null ) {
			return super.getInputStream();
		}
		try {
			if ( this.streamContent != null ) {
				return byteStream(this.streamContent);
			}
			String decodedString = AesUtil.decrypt(IOUtils.toString(this.streamContent,
					this.actualRequest.getCharacterEncoding()),
					(String) this.getSession().getAttribute(PASS_PHRASE), true);

			byte[] decodedBytes = decodedString.getBytes(this.actualRequest.getCharacterEncoding());
			this.streamContent = decodedBytes;
			return byteStream(decodedBytes);
		} catch (Exception e) {
			BaseLoggers.flowLogger.error("could not decrypt message ", e);
			throw new RuntimeException(e);
		}
	}

	private ServletInputStream byteStream(byte[] content) {
		return new DelegatingServletInputStream(new ByteArrayInputStream(
				content));
	}

	@Override
	public BufferedReader getReader() throws IOException {
		return new BufferedReader(new InputStreamReader(this.getInputStream()));
	}

	@Override
	public String[] getParameterValues(String name) {
		try {
			String[] returnValue = null;
			if (this.parameters.has(name)) {
				Object values = this.parameters.get(name);
				if (values instanceof JSONArray) {
					returnValue = toStringArray(this.parameters.getJSONArray(name));
				} else {
					returnValue = new String[1];
					returnValue[0] = (String) values;
				}
			} else if (queryParams.contains(name)) {
				returnValue = super.getParameterValues(name);
			}
			BaseLoggers.flowLogger.debug(
					"Path {}\nNeutrinoRequestParamHolder::getParameterValues {} super:: {} custom:: {}",
					this.getRequestURI(), name, super.getParameterValues(name), returnValue);
			return returnValue;
		} catch (JSONException e) {
			return new String[0];
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, String[]> getParameterMap() {
		Map<String, String[]> map = new HashMap<>();
		parameters.keys().forEachRemaining(key -> {
			try {
				String paramKey = (String) key;
				Object value = parameters.get(paramKey);
				if (value instanceof JSONArray) {
					map.put(paramKey, toStringArray((JSONArray) value));
				} else {
					String[] values = new String[1];
					values[0] = (String) value;
					map.put(paramKey, values);
				}
			} catch (JSONException e) {
				throwParamManipulationException(PARAM_MANIPULATED_ERROR, e);
			}
		});
		Map<String, String[]> superParamMap = super.getParameterMap();
		superParamMap.forEach((key, value) -> {
			if (!REQUEST_DATA_PARAM.equals(key) && queryParams.contains(key)) {
				map.put(key, value);
			}
		});
		BaseLoggers.flowLogger.debug("Path {}\nNeutrinoRequestParamHolder::getParameterMap  super:: {} custom:: {}",
				this.getRequestURI(), super.getParameterMap(), map);

		return map;
	}

	@Override
	public RequestDispatcher getRequestDispatcher(String path) {
		RequestDispatcher requestDispatcher = super.getRequestDispatcher(path);
		if (NeutrinoRequestDispatcherWrapper.class.isInstance(requestDispatcher)) {
			return requestDispatcher;
		} else {
			return new NeutrinoRequestDispatcherWrapper(requestDispatcher, this,path);
		}
	}

    public void preForward(String path){
		if(!StringUtils.isEmpty(path) && (path.contains("?") || path.contains("&"))){
			try{
				MultiValueMap<String, String> pathParameters =
						UriComponentsBuilder.fromUriString(path).build().getQueryParams();
				if(pathParameters!=null && !pathParameters.isEmpty()){
					queryParams.addAll(pathParameters.keySet());
				}
			}catch (Exception e){
				BaseLoggers.flowLogger.debug("Error occurred in preForward while resolving query parameters");
			}
		}
    }
	
	
	public static String[] toStringArray(JSONArray array) {
		if (array == null) {
			return new String[0];
		}
		String[] arr = new String[array.length()];
		for (int i = 0; i < arr.length; i++) {
			arr[i] = array.optString(i);
		}
		return arr;
	}

}
