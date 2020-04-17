package com.nucleus.standard.logging.xmlbasedlogging;

import static com.nucleus.finnone.pro.general.util.ValidatorUtils.isNull;
import static com.nucleus.finnone.pro.general.util.ValidatorUtils.notNull;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
//import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.nucleus.core.initialization.NeutrinoResourceLoader;
import com.nucleus.finnone.pro.base.exception.ExceptionBuilder;
import com.nucleus.finnone.pro.base.exception.SystemException;
import com.nucleus.logging.BaseLoggers;

@Transactional
@Named("loggingContextController")
public class LoggingContextController {

	private static Map<String, Object> loggingDataCacheMap = new HashMap<String, Object>();
	private static final String TARGET_LOGGING_XPATH = "/neutrino-auditing-context/parameter-interceptors";
	private static XPathFactory factory = XPathFactory.newInstance();
	private Document document;
	private static final String IS_INNER_CLASS = "$$";
	private static final String PROFILE_NEUTRINO_LOGGING_AUDITING="neutrino-logging-auditing";

	@Autowired
	private Environment environment;

	@Autowired
	private ApplicationContext applicationContext;

	@Inject
	@Named("frameworkConfigResourceLoader")
	protected NeutrinoResourceLoader frameworkConfigResourceLoader;

	@PostConstruct
	public void initializeDocument() throws Exception {
		String[] defaultprofiles = this.environment.getDefaultProfiles();

		if (ArrayUtils.contains(defaultprofiles,PROFILE_NEUTRINO_LOGGING_AUDITING)) {
			//createLogAuditXMLFile();
			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			InputStream inputStream = frameworkConfigResourceLoader.getResource("logging-interceptor-mapping.xml")
					.getInputStream();
			if (notNull(inputStream)) {
				document = builder.parse(inputStream);
				initializeLoggingDataContextMap();
			} else {
				BaseLoggers.exceptionLogger.debug("Failed to initialize LoggingDataCacheMap");
			}

		}

	}

	private void createLogAuditXMLFile() throws TransformerException {
		try {
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder;
			docBuilder = docFactory.newDocumentBuilder();

			// root elements
			Document doc = docBuilder.newDocument();
			Element rootElement = doc.createElement("neutrino-auditing-context");
			doc.appendChild(rootElement);

			String[] beanNames = applicationContext.getBeanDefinitionNames();

			for (String beanName : beanNames) {
				
				try {
					if (beanName != null && applicationContext.getBean(beanName) != null
							&& applicationContext.getBean(beanName).getClass() != null
							&& beanName != "rootConfigResourceLoader" && applicationContext.getBean(beanName).getClass()
									.toString().startsWith("class com.nucleus")) {

						Class clazz = Class.forName(applicationContext.getBean(beanName).getClass().getName());

						String className = applicationContext.getBean(beanName).getClass().getName();
						Boolean isInnerClass = className.contains(IS_INNER_CLASS);

						if (!isInnerClass) {

							for (Method method : clazz.getDeclaredMethods()) {
								
								
								if (method.getModifiers() == Modifier.PUBLIC) {
									
									Element parameterInterceptorsElem = doc.createElement("parameter-interceptors");
									rootElement.appendChild(parameterInterceptorsElem);

									Element parameterInterceptorElem = doc.createElement("parameter-interceptor");
									parameterInterceptorsElem.appendChild(parameterInterceptorElem);
									Attr attr11 = doc.createAttribute("loggingcontext");
									attr11.setValue("LOCAL");
									parameterInterceptorElem.setAttributeNode(attr11);
									Attr attr12 = doc.createAttribute("index");
									attr12.setValue("");
									parameterInterceptorElem.setAttributeNode(attr12);
									Attr attr13 = doc.createAttribute("name");
									attr13.setValue("");
									parameterInterceptorElem.setAttributeNode(attr13);

									Attr attr1 = doc.createAttribute("className");
									attr1.setValue(clazz.getSimpleName());
									parameterInterceptorsElem.setAttributeNode(attr1);

									Attr attr2 = doc.createAttribute("methodName");
									attr2.setValue(method.getName());
									parameterInterceptorsElem.setAttributeNode(attr2);
																		
								}

							}

						}

					}
				} catch (Exception e) {

					BaseLoggers.exceptionLogger.error(e.toString());
				}

			}

			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(new File("D:\\temp\\file.xml"));
			
			transformer.transform(source, result);
		} catch (ParserConfigurationException e) {
			BaseLoggers.exceptionLogger.error(e.toString());
		} catch (Exception e) {
			BaseLoggers.exceptionLogger.error(e.toString());
		}
	}

	private void initializeLoggingDataContextMap() throws XPathExpressionException {

		NodeList parameterInterceptorsNodeList = (NodeList) factory.newXPath().evaluate(TARGET_LOGGING_XPATH, document,
				XPathConstants.NODESET);
		for (int j = 0; j < parameterInterceptorsNodeList.getLength(); j++) {

			Node parameterInterceptorsChildNode = parameterInterceptorsNodeList.item(j);

			String active = parameterInterceptorsChildNode.getAttributes().getNamedItem(LoggingContextConstants.ACTIVE)
					.getNodeValue();

			if (active != null && active != "" && "true".equalsIgnoreCase(active)) {
				if (checkNullOrEmpty(parameterInterceptorsChildNode, LoggingContextConstants.CLASS_NAME,
						LoggingContextConstants.METHOD_NAME)) {
					String className = parameterInterceptorsChildNode.getAttributes()
							.getNamedItem(LoggingContextConstants.CLASS_NAME).getNodeValue();
					String methodName = parameterInterceptorsChildNode.getAttributes()
							.getNamedItem(LoggingContextConstants.METHOD_NAME).getNodeValue();

					if (className != null && methodName != null) {
						createLoggingContextDataMap(className, methodName);
					}
				}
			}
		}
	}

	private Boolean checkNullOrEmpty(Node childNode, String className, String methodName) {
		if (childNode.getAttributes().getNamedItem(className) != null
				&& isNotBlank(childNode.getAttributes().getNamedItem(className).getNodeValue())
				&& childNode.getAttributes().getNamedItem(methodName) != null
				&& isNotBlank(childNode.getAttributes().getNamedItem(methodName).getNodeValue())) {
			return true;
		}
		return false;
	}

	private Boolean checkNullOrEmptyInParameterInterceptor(Node childNode, String loggingfield) {
		if (childNode.getAttributes().getNamedItem(loggingfield) != null
				&& isNotBlank(childNode.getAttributes().getNamedItem(loggingfield).getNodeValue())) {
			return true;
		}
		return false;
	}

	private Map<String, Object> createMapFromXpath(NodeList xpath, Map<String, Object> loggingDataCacheMap,
			String class_Method_Name) {
		
		if (isNull(xpath)) {
			return loggingDataCacheMap;
		}

		List<LoggingContextVO> listOfLoggingContextVO = new ArrayList<LoggingContextVO>();
		for (int i = 0; i < xpath.getLength(); i++) {
			LoggingContextVO loggingInterceptorVO = new LoggingContextVO();
			Node parameterInterceptorChildNode = xpath.item(i);

			if (checkNullOrEmptyInParameterInterceptor(parameterInterceptorChildNode, LoggingContextConstants.INDEX)) {
				loggingInterceptorVO.setIndex(parameterInterceptorChildNode.getAttributes()
						.getNamedItem(LoggingContextConstants.INDEX).getNodeValue());
			}
			if (checkNullOrEmptyInParameterInterceptor(parameterInterceptorChildNode, LoggingContextConstants.KEY)) {
				loggingInterceptorVO.setKey(parameterInterceptorChildNode.getAttributes()
						.getNamedItem(LoggingContextConstants.KEY).getNodeValue());
			}
			if (checkNullOrEmptyInParameterInterceptor(parameterInterceptorChildNode,
					LoggingContextConstants.LOGGING_CONTEXT)) {
				loggingInterceptorVO.setLoggingContext(parameterInterceptorChildNode.getAttributes()
						.getNamedItem(LoggingContextConstants.LOGGING_CONTEXT).getNodeValue());
			}
			if (checkNullOrEmptyInParameterInterceptor(parameterInterceptorChildNode, LoggingContextConstants.NAME)) {
				loggingInterceptorVO.setName(parameterInterceptorChildNode.getAttributes()
						.getNamedItem(LoggingContextConstants.NAME).getNodeValue());
			} else {
				throw ExceptionBuilder
						.getInstance(SystemException.class, LoggingContextConstants.ERROR_IN_LOADING_LOG_AUDIT_FILE,
								"Error in loading logging-interceptor-mapping.xml.Name attribute can not be empty for class_method name : "
										+ class_Method_Name)
						.setSeverity(LoggingContextConstants.SEVERITY_HIGH).build();
			}
			listOfLoggingContextVO.add(loggingInterceptorVO);
		}
		loggingDataCacheMap.put(class_Method_Name, listOfLoggingContextVO);
		
		return loggingDataCacheMap;
	}

	@SuppressWarnings("unchecked")
	public List<LoggingContextVO> getLoggingContextMapFromCache(String key) {

		return (List<LoggingContextVO>) loggingDataCacheMap.get(key);

	}

	private void createLoggingContextDataMap(String className, String methodName) throws XPathExpressionException {
		Object loggingDataCacheMapList = getLoggingContextMapFromCache(
				className + LoggingContextConstants.UNDERSCORE + methodName);

		if (loggingDataCacheMapList == null) {
			prepareLoggingContextDataMapCache(className, methodName);
		}
	}

	private void prepareLoggingContextDataMapCache(String className, String methodName)
			throws XPathExpressionException {


		String loggingXPath = TARGET_LOGGING_XPATH + "[@className=" + "'" + className + "']"

				+ "[@methodName=" + "'" + methodName + "']/parameter-interceptor";

		NodeList resultLoggingXPath = (NodeList) factory.newXPath().evaluate(loggingXPath, document,
				XPathConstants.NODESET);

		loggingDataCacheMap = createMapFromXpath(resultLoggingXPath, loggingDataCacheMap,
				className + LoggingContextConstants.UNDERSCORE + methodName);

	}

}
