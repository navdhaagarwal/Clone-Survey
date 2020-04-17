package com.nucleus.rules.tablemetadata;

import java.io.Serializable;

public class TableJoinPathResponse implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private boolean pathFound;
	
	private String message;
	
	private	TableJoinNodes path;
	
	private String suggestedQueryToUse;

	public boolean isPathFound() {
		return pathFound;
	}

	public void setPathFound(boolean pathFound) {
		this.pathFound = pathFound;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public TableJoinNodes getPath() {
		return path;
	}

	public void setPath(TableJoinNodes path) {
		this.path = path;
	}

	public String getSuggestedQueryToUse() {
		return suggestedQueryToUse;
	}

	public void setSuggestedQueryToUse(String suggestedQueryToUse) {
		this.suggestedQueryToUse = suggestedQueryToUse;
	}
	
}



