package com.nucleus.exceptionLogging;

import java.util.HashMap;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Transient;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

import com.nucleus.core.annotations.Synonym;
import com.nucleus.entity.BaseEntity;

@Entity
@DynamicUpdate
@DynamicInsert
@Synonym(grant="ALL")
public class NeutrinoExceptionEntity extends BaseEntity {

    /**
     * 
     */
    private static final long       serialVersionUID = 1L;

    /** The exception type. */
    private String                  exceptionType;

    /** The message. */
    private String                  message;

    @Lob
    private String                  stackTrace;

    @Lob
    private String                  requestParameters;

    private String                  functionalParameter;

    private String                  requestURI;

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

    @Transient
    private HashMap<String, Object> viewProperties;

    @Column(updatable = false)
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime                creationDate;

    public String getExceptionType() {
        return exceptionType;
    }

    public void setExceptionType(String exceptionType) {
        this.exceptionType = exceptionType;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getLoggedInUserUri() {
        return loggedInUserUri;
    }

    public void setLoggedInUserUri(String loggedInUserUri) {
        this.loggedInUserUri = loggedInUserUri;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getCasTransactionId() {
        return casTransactionId;
    }

    public void setCasTransactionId(String casTransactionId) {
        this.casTransactionId = casTransactionId;
    }

    public HashMap<String, Object> getViewProperties() {
        return viewProperties;
    }

    public void setViewProperties(HashMap<String, Object> viewProperties) {
        this.viewProperties = viewProperties;
    }

    public String getStackTrace() {
        return stackTrace;
    }

    public void setStackTrace(String stackTrace) {
        this.stackTrace = stackTrace;
    }

    public DateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(DateTime creationDate) {
        this.creationDate = creationDate;
    }

    public String getNode() {
        return node;
    }

    public void setNode(String node) {
        this.node = node;
    }

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

}
