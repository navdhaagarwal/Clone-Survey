package com.nucleus.core.accesslog.entity;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a node in Trie data structure.
 * 
 */
public class URINode {

	private Map<String, URINode> map = new HashMap<>();
	private String val;
	private boolean isLeafNode;

	public URINode() {
	}

	public URINode(String val) {
		this.val = val;
	}

	public URINode(String val, boolean isLeafNode) {
		this.val = val;
		this.isLeafNode = isLeafNode;
	}

	public Map<String, URINode> getMap() {
		return map;
	}

	public String getVal() {
		return val;
	}

	public void setVal(String val) {
		this.val = val;
	}

	public boolean isLeafNode() {
		return isLeafNode;
	}

	public String toString() {

		return val;
	}
}
