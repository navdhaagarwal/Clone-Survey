package com.nucleus.shortcut;

import java.io.Serializable;
import java.util.List;

public class HotKeyUI implements Serializable{

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String shortCutKey;
	private String shortCutKeySuggestion;
	private  List<List<String>> identifierList;
	private String description;
	
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getShortCutKey() {
		return shortCutKey;
	}
	public void setShortCutKey(String shortCutKey) {
		this.shortCutKey = shortCutKey;
	}
	public List<List<String>> getIdentifierList() {
		return identifierList;
	}
	public void setIdentifierList(List<List<String>> identifierList) {
		this.identifierList = identifierList;
	}
	public String getShortCutKeySuggestion() {
		return shortCutKeySuggestion;
	}
	public void setShortCutKeySuggestion(String shortCutKeySuggestion) {
		this.shortCutKeySuggestion = shortCutKeySuggestion;
	}
		
}
