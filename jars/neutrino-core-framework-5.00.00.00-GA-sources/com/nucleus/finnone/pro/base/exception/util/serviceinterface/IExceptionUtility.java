package com.nucleus.finnone.pro.base.exception.util.serviceinterface;

public interface IExceptionUtility {
	/**
	 * @deprecated
	 * This method was initially developed to dig out 
	 * ORA errors occurred during failure of the 
	 * DB operations
	 * It is recommended to use following method to get complete cause 
	 * <code>getCauseFromStackHierarchy(Exception):String</code>
	 * @param exception
	 * @return
	 */
	String getCauseOfORAErrorIfExists(Exception exception);
	
	/**
	 * Utility Method to prepare detail exception stackTrace 
	 * by traversing through the cause of the errors in <code>Exception</code>
	 * Stack Hierarchy.
	 * 
	 * @param exception
	 * @return
	 */
	String getCauseFromStackHierarchy(Exception exception);
}  