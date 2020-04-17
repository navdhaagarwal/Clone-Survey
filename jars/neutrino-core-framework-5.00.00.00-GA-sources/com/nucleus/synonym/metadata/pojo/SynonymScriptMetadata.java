package com.nucleus.synonym.metadata.pojo;

public class SynonymScriptMetadata {
	
	private String authority;
	
	private String tableName;
	
	private boolean sequence;
	
	private boolean grant;
	
	private boolean synonym;
	
	private String origin;

	private String synonymName;

	public String getAuthority() {
		return authority;
	}
 
	public void setAuthority(String authority) {
		this.authority = authority;
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public boolean isSequence() {
		return sequence;
	}

	public void setSequence(boolean sequence) {
		this.sequence = sequence;
	}

	public boolean isGrant() {
		return grant;
	}

	public void setGrant(boolean grant) {
		this.grant = grant;
	}

	public boolean isSynonym() {
		return synonym;
	}

	public void setSynonym(boolean synonym) {
		this.synonym = synonym;
	}

	public String getOrigin() {
		return origin;
	}

	public void setOrigin(String origin) {
		this.origin = origin;
	}

	public String getSynonymName() {
		return synonymName;
	}

	public void setSynonymName(String synonymName) {
		this.synonymName = synonymName;
	}
}
