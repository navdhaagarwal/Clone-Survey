package com.nucleus.regional.config.util;

import static com.nucleus.finnone.pro.general.util.ValidatorUtils.isNull;
import static com.nucleus.finnone.pro.general.util.ValidatorUtils.notNull;
import static org.apache.commons.collections.MapUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


import com.nucleus.core.initialization.NeutrinoResourceLoader;
import com.nucleus.core.misc.util.ExceptionUtility;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.regional.config.constant.RegionalFieldConstants;
import com.nucleus.regional.config.constant.RegionalFieldsAttributes;

@Named("regionalXMLReader")
public class RegionalXMLReader {

	private static Map<String, Object> regionalResponseDataCacheMap = new HashMap<String, Object>();

	private static final String SOURCE_ENTITY_XPATH = "/regional-config/source-entity";
	private static XPathFactory factory = XPathFactory.newInstance();
	private Document document;

	@Inject
	@Named("frameworkConfigResourceLoader")
	protected NeutrinoResourceLoader frameworkConfigResourceLoader;

	@PostConstruct
	public void initializeDocument() {
		BaseLoggers.flowLogger.debug("Initializing document");
		try {
			DocumentBuilder builder = DocumentBuilderFactory.newInstance()
					.newDocumentBuilder();
			InputStream inputStream = frameworkConfigResourceLoader
					.getResource("regional-mapping-config.xml")
					.getInputStream();
			if (notNull(inputStream)) {
				document = builder.parse(inputStream);
				initializeRegionalResponseDataMap();
			} else {
				BaseLoggers.exceptionLogger
						.debug("Failed to initialize regionalDataCacheMap");
			}
		} catch (XPathExpressionException e) {
			BaseLoggers.flowLogger.error("XPathExpression Exception occured[",
					e);
			ExceptionUtility.rethrowSystemException(e);
		} catch (ParserConfigurationException e) {
			BaseLoggers.flowLogger.error(
					"ParserConfigurationException  occured[", e);
			ExceptionUtility.rethrowSystemException(e);
		} catch (IOException e) {
			BaseLoggers.flowLogger.error("IOException occured[", e);
			ExceptionUtility.rethrowSystemException(e);
		} catch (SAXException e) {
			BaseLoggers.flowLogger.error("SAXException occured[", e);
			ExceptionUtility.rethrowSystemException(e);
		}
	}

	public Map<String, Object> getAllRegionalResponseFieldMap() {
		return getRegionalFieldResponseConfigMapFromCache();
	}

	private void initializeRegionalResponseDataMap()
			throws XPathExpressionException {
		NodeList sourceEntityNodeList = (NodeList) factory.newXPath().evaluate(
				SOURCE_ENTITY_XPATH, document, XPathConstants.NODESET);
		for (int j = 0; j < sourceEntityNodeList.getLength(); j++) {
			Node childNode = sourceEntityNodeList.item(j);
			if (checkNullOrEmpty(childNode, RegionalFieldConstants.NAME)) {
				String sourceEntityName = childNode.getAttributes()
						.getNamedItem(RegionalFieldConstants.NAME)
						.getNodeValue();
				if (notNull(sourceEntityName)) {
					prepareRegionalResponseMapCache(sourceEntityName);
				}
			}
		}
	}

	/*
	 * This method checks whether attribute is present and whether its value is
	 * null or empty
	 */
	private Boolean checkNullOrEmpty(Node childNode, String nodeName) {
		if (childNode.getAttributes().getNamedItem(nodeName) != null
				&& isNotBlank(childNode.getAttributes().getNamedItem(nodeName)
						.getNodeValue())) {
			return true;
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> getRegionalFieldResponseConfigMapFromCache() {

		return regionalResponseDataCacheMap;
	}

	private void prepareRegionalResponseMapCache(String sourceEntityName)
			throws XPathExpressionException {
		List<RegionalFieldsAttributes> regionalResonseFieldConfigList = new ArrayList<RegionalFieldsAttributes>();

		String regionalFieldXPath = SOURCE_ENTITY_XPATH + "[@name=" + "'"
				+ sourceEntityName + "']/field";

		NodeList resultRegionalFieldXPath = (NodeList) factory.newXPath()
				.evaluate(regionalFieldXPath, document, XPathConstants.NODESET);

		regionalResonseFieldConfigList = createMapFromXpath(
				resultRegionalFieldXPath, regionalResonseFieldConfigList);

		if (notNull(regionalResonseFieldConfigList)
				&& !regionalResonseFieldConfigList.isEmpty()) {
			putRegionalFieldConfigMapIntoCache(sourceEntityName,
					regionalResonseFieldConfigList);
		}
	}

	private List<RegionalFieldsAttributes> createMapFromXpath(NodeList xpath,
			List<RegionalFieldsAttributes> regionalResonseFieldConfigList) {

		if (isNull(xpath)) {
			return regionalResonseFieldConfigList;
		}
		for (int i = 0; i < xpath.getLength(); i++) {
			RegionalFieldsAttributes regionalFieldsAttributes = new RegionalFieldsAttributes();
			Node childNode = xpath.item(i);
			if (checkNullOrEmpty(childNode, RegionalFieldConstants.NAME)) {
				regionalFieldsAttributes.setLogicalName(childNode
						.getAttributes()
						.getNamedItem(RegionalFieldConstants.NAME)
						.getNodeValue());
			}
			if (checkNullOrEmpty(childNode,
					RegionalFieldConstants.FIELD_NAME)) {
				regionalFieldsAttributes
						.setFieldName(childNode
								.getAttributes()
								.getNamedItem(
										RegionalFieldConstants.FIELD_NAME)
								.getNodeValue());
			}
			regionalResonseFieldConfigList.add(regionalFieldsAttributes);
		}
		return regionalResonseFieldConfigList;
	}

	private void putRegionalFieldConfigMapIntoCache(String key,
			List<RegionalFieldsAttributes> regionalResonseFieldConfigList) {
		regionalResponseDataCacheMap.put(key, regionalResonseFieldConfigList);
	}
}
