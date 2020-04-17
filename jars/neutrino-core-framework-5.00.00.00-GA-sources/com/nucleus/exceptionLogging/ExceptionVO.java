/**
 * Copyright Nucleus Software India Pvt Ltd. All rights reserved.
 */
package com.nucleus.exceptionLogging;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedHashMap;

import org.ektorp.support.CouchDbDocument;

import com.nucleus.core.misc.util.HashCodeUtil;

/**
 * The Class ExceptionVO.
 *
 * @author Nucleus Software India Pvt Ltd
 */
public class ExceptionVO extends CouchDbDocument implements Serializable {

    /** The Constant serialVersionUID. */
    private static final long       serialVersionUID = 1L;

    /** The exception type. */
    private String                  exceptionType;

    /** The message. */
    private String                  message;

    /** The exception occured timestamp. */
    private String                  exceptionOccuredTimestamp;

    /** The stack trace elements. */
    private String                  stackTrace;

    /** The logged in user uri. */
    private String                  loggedInUserUri;

    /** The method name. */
    private String                  methodName;

    /** The file name. */
    private String                  fileName;

    /** The class name. */
    private String                  className;

    /** The cas transaction id. */
    private String                  casTransactionId;

    private String                  node;

    /** The cas transaction id. */
    private String                  exceptionOccuredDate;

    private String requestParameters;

    private String requestURI;

    private String functionalParameter;

    private HashMap<String, Object> viewProperties;

    public String getRequestParameters() {
        return requestParameters;
    }

    public void setRequestParameters(String requestParameters) {
        this.requestParameters = requestParameters;
    }

    public String getRequestURI() {
        return requestURI;
    }

    public void setRequestURI(String requestURI) {
        this.requestURI = requestURI;
    }

    public String getFunctionalParameter() {
        return functionalParameter;
    }

    public void setFunctionalParameter(String functionalParameter) {
        this.functionalParameter = functionalParameter;
    }

    /**
     * Gets the exception type.
     *
     * @return the exceptionType
     */
    public String getExceptionType() {
        return exceptionType;
    }

    /**
     * Sets the exception type.
     *
     * @param exceptionType the exceptionType to set
     */
    public void setExceptionType(String exceptionType) {
        this.exceptionType = exceptionType;
    }

    /**
     * Gets the message.
     *
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Sets the message.
     *
     * @param message the message to set
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Gets the logged in user uri.
     *
     * @return the loggedInUserUri
     */
    public String getLoggedInUserUri() {
        return loggedInUserUri;
    }

    /**
     * Sets the logged in user uri.
     *
     * @param loggedInUserUri the loggedInUserUri to set
     */
    public void setLoggedInUserUri(String loggedInUserUri) {
        this.loggedInUserUri = loggedInUserUri;
    }

    /**
     * Gets the method name.
     *
     * @return the methodName
     */
    public String getMethodName() {
        return methodName;
    }

    /**
     * Sets the method name.
     *
     * @param methodName the methodName to set
     */
    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    /**
     * Gets the file name.
     *
     * @return the fileName
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * Gets the exception occured timestamp.
     *
     * @return the exceptionOccuredTimestamp
     */
    public String getExceptionOccuredTimestamp() {
        return exceptionOccuredTimestamp;
    }

    /**
     * Sets the exception occured timestamp.
     *
     * @param exceptionOccuredTimestamp the exceptionOccuredTimestamp to set
     */
    public void setExceptionOccuredTimestamp(String exceptionOccuredTimestamp) {
        this.exceptionOccuredTimestamp = exceptionOccuredTimestamp;
    }

    /**
     * Sets the file name.
     *
     * @param fileName the fileName to set
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    /**
     * Gets the class name.
     *
     * @return the className
     */
    public String getClassName() {
        return className;
    }

    /**
     * Sets the class name.
     *
     * @param className the className to set
     */
    public void setClassName(String className) {
        this.className = className;
    }

    /**
     * @return the casTransactionId
     */
    public String getCasTransactionId() {
        return casTransactionId;
    }

    /**
     * @param casTransactionId the casTransactionId to set
     */
    public void setCasTransactionId(String casTransactionId) {
        this.casTransactionId = casTransactionId;
    }

    public String getNode() {
        return node;
    }

    public void setNode(String node) {
        this.node = node;
    }

    /**
     * @return the exceptionOccuredDate
     */
    public String getExceptionOccuredDate() {
        return exceptionOccuredDate;
    }

    /**
     * @param exceptionOccuredDate the exceptionOccuredDate to set
     */
    public void setExceptionOccuredDate(String exceptionOccuredDate) {
        this.exceptionOccuredDate = exceptionOccuredDate;
    }

    /**
     * @return the viewProperties
     */
    public HashMap<String, Object> getViewProperties() {
        return viewProperties;
    }

    public String getStackTrace() {
        return stackTrace;
    }

    public void setStackTrace(String stackTrace) {
        this.stackTrace = stackTrace;
    }

    /**
     * @param viewProperties
     *            the viewProperties to set
     */
    public void setViewProperties(HashMap<String, Object> viewProperties) {
        this.viewProperties = viewProperties;
    }

    public void addProperty(String key, Object value) {
        if (viewProperties == null) {
            this.viewProperties = new LinkedHashMap<String, Object>();
        }
        this.viewProperties.put(key, value);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if ((obj != null) && (this.getId() != null) && (this.getId().equals(((ExceptionVO) obj).getId()))) {
            if (this.getId().equals(((ExceptionVO) obj).getId())) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public int hashCode(){
    	int result = HashCodeUtil.SEED;
    	result = HashCodeUtil.hash(result,getId());
    	return result;
    	}
    
}
