package com.nucleus.core.accesslog.entity;

import javax.inject.Named;

import com.nucleus.logging.BaseLoggers;

@Named("dynamicURILogicalNameMapper")
public class DynamicURILogicalNameMapper {

	private static final String URI_QUALIFIER = "/";
	public static final String DYNAMIC_URI_TOKEN = "{}";

	private URINode root = new URINode();
	
	/**
	 * Insert uri tokens (as nodes) in Trie data structure.
	 * 
	 */
	public void insertUriNode(String uri, String logicalFunctionId) {
		try {
			validateUri(uri);
		}catch (InvalidUriException e) {
			BaseLoggers.exceptionLogger.debug("Invalid Uri Inserted : ", e);
			return;
		}

		String[] uriTokens = uri.split(URI_QUALIFIER);

		addNewUriNode(root, 0, uriTokens, logicalFunctionId);
	}

	private void addNewUriNode(URINode node, int index, String[] uriTokens, String logicalFunctionId) {

		if (index == uriTokens.length - 1) {
			if (node.getMap().get(uriTokens[index]) == null) {
				node.getMap().put(uriTokens[index], new URINode(logicalFunctionId, true));
			}
			node.getMap().get(uriTokens[index]).setVal(logicalFunctionId);
			return;
		}

		if (node.getMap().get(uriTokens[index]) == null) {
			node.getMap().put(uriTokens[index], new URINode());
		}

		addNewUriNode(node.getMap().get(uriTokens[index]), index + 1, uriTokens, logicalFunctionId);

	}

	/**
	 * Search logical function name stored in Trie data structure.
	 * 
	 */
	public String getUriFunctionNameId(String requestUri) {

		try {
			validateUri(requestUri);
		} catch (InvalidUriException e) {
			BaseLoggers.exceptionLogger.debug("Invalid Uri Searched : ", e);
			return null;
		}

		String[] requestUriTokens = requestUri.split(URI_QUALIFIER);

		return searchUriNode(root, 0, requestUriTokens);
	}

	private String searchUriNode(URINode node, int index, String[] requestUriTokens) {

		if (index == requestUriTokens.length - 1) {

			if (node.getMap().get(requestUriTokens[index]) == null) {
				if (node.getMap().get(DYNAMIC_URI_TOKEN) != null)
					return node.getMap().get(DYNAMIC_URI_TOKEN).getVal();

				return null;
			}

			return node.getMap().get(requestUriTokens[index]).getVal();
		}

		if (node.getMap().get(requestUriTokens[index]) == null) {

			if (node.getMap().get(DYNAMIC_URI_TOKEN) != null)
				return searchUriNode(node.getMap().get(DYNAMIC_URI_TOKEN), index + 1, requestUriTokens);

			return null;
		}

		return searchUriNode(node.getMap().get(requestUriTokens[index]), index + 1, requestUriTokens);

	}

	private void validateUri(String uri) throws InvalidUriException {

		if (uri == null || uri.indexOf(URI_QUALIFIER) == -1 || uri.length() == 1) {
			throw new InvalidUriException("Invalid URI :" + uri);
		}
	}

}
