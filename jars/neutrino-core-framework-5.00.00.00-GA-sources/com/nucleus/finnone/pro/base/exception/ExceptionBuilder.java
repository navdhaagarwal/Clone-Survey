/*
 * Author: Abhishek Pallav
 * Creation Date: 24-Aug-2012
 * Copyright: Nucleus Software Exports Ltd.
 * Description: This is excception builder class that will instantiate the custom exception class and return the same.
 *
 * ------------------------------------------------------------------------------------------------------------------------------------
 * Revision:  Version         Last Revision Date                   Name                Function / Module affected  Modifications Done
 * ------------------------------------------------------------------------------------------------------------------------------------
 *                1.0             24/08/2012                    Abhishek Pallav             Initial Version created

 */
package com.nucleus.finnone.pro.base.exception;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import com.nucleus.finnone.pro.base.Message;
import com.nucleus.finnone.pro.base.constants.BaseExceptionSubjectTypeEnum;
import com.nucleus.finnone.pro.general.constants.ExceptionSeverityEnum;

public class ExceptionBuilder {

    public static ExceptionBuilder getInstance(Class<? extends BaseException> exceptionClass) {
        ExceptionBuilder exceptionBuilder = new ExceptionBuilder();
        exceptionBuilder.setExceptionClass(exceptionClass);
        return exceptionBuilder;
    }

    public static ExceptionBuilder getInstance(Class<? extends BaseException> exceptionClass, String exceptionCode, String logMessage) {
        ExceptionBuilder exceptionBuilder = new ExceptionBuilder();
        exceptionBuilder.setExceptionClass(exceptionClass);
        exceptionBuilder.setExceptionCode(exceptionCode);
        exceptionBuilder.setLogMessage(logMessage);
        return exceptionBuilder;
    }

    public static ExceptionBuilder getInstance(Class<? extends BaseException> exceptionClass, String exceptionCode, String logMessage, Long subjectId, String subjectReferenceNumber, Character subjectType) {
        ExceptionBuilder exceptionBuilder = new ExceptionBuilder();
        exceptionBuilder.setExceptionClass(exceptionClass);
        exceptionBuilder.setExceptionCode(exceptionCode);
        exceptionBuilder.setLogMessage(logMessage);
        exceptionBuilder.setSubjectId(subjectId);
        exceptionBuilder.setSubjectReferenceNumber(subjectReferenceNumber);
        exceptionBuilder.setSubjectType(subjectType);
        return exceptionBuilder;
    }

    private Class<? extends BaseException> exceptionClass;

    private BaseException exception;

    private List<Message> messages;

    private Exception originaleException;

    private String logMessage;

    private String exceptionCode;

    private Integer severity;

    private Boolean reProcessingRequired;

    private Long subjectId;

    private String subjectReferenceNumber;

    private Character subjectType;

    private Boolean isLogged = Boolean.FALSE;

    private Long transactionId;

    private String transactionReferenceNumber;


    private ExceptionBuilder() {
    }

    public void addMessage(Message theMessage) {
        if (this.messages == null) {
            this.messages = new ArrayList<Message>();
        }
        if (theMessage != null) {
            this.messages.add(theMessage);
        }
    }

    public void addMessage(String i18nCode) {
        if (this.messages == null) {
            this.messages = new ArrayList<Message>();
        }
        Message message = new Message();
        message.setI18nCode(i18nCode);
        this.messages.add(message);
    }

    public void addMessage(String i18nCode, String[] messageArguments) {
        if (this.messages == null) {
            this.messages = new ArrayList<Message>();
        }
        Message message = new Message();
        message.setI18nCode(i18nCode);
        message.setMessageArguments(messageArguments);
        this.messages.add(message);
    }

    public void addMessages(List<Message> theMessages) {
        if (this.messages == null) {
            this.messages = new ArrayList<Message>();
        }
        if (theMessages != null) {
            this.messages.addAll(theMessages);
        }
    }

    public BaseException build() {
        initializeException(exceptionClass);
        exception.setMessages(messages);
        exception.setExceptionCode(exceptionCode);
        exception.setSeverity(severity);
        exception.setReProcessingRequired(reProcessingRequired);
        exception.setSubjectId(subjectId);
        exception.setSubjectReferenceNumber(subjectReferenceNumber);
        exception.setSubjectType(subjectType);
        exception.setLogged(isLogged);
        return exception;
    }

    public Boolean initializeException(Class<? extends BaseException> exceptionClass) {
        try {
            if ((originaleException != null) && (logMessage != null)) {
                Constructor constructor = Class.forName(exceptionClass.getName()).getConstructor(String.class, Throwable.class);
                exception = (BaseException) constructor.newInstance(logMessage, originaleException);
                exception.setStackTrace(originaleException.getStackTrace());
            } else if ((originaleException == null) && (logMessage != null)) {
                Constructor constructor = Class.forName(exceptionClass.getName()).getConstructor(String.class);
                exception = (BaseException) constructor.newInstance(logMessage);
            } else if ((originaleException != null) && (logMessage == null)) {
                Constructor constructor = Class.forName(exceptionClass.getName()).getConstructor(Throwable.class);
                exception = (BaseException) constructor.newInstance(originaleException);
                exception.setStackTrace(originaleException.getStackTrace());
            } else {
                exception = exceptionClass.newInstance();
            }
        } catch (InstantiationException instantiationException) {
            return false;
        } catch (IllegalAccessException illegalAccessException) {
            return false;
        } catch (IllegalArgumentException e) {
            return false;
        } catch (SecurityException e) {
            return false;
        } catch (InvocationTargetException e) {
            return false;
        } catch (NoSuchMethodException e) {
            return false;
        } catch (ClassNotFoundException e) {
            return false;
        }
        return true;
    }

    public ExceptionBuilder setExceptionClass(Class<? extends BaseException> exceptionClass) {
        this.exceptionClass = exceptionClass;
        return this;
    }

    public ExceptionBuilder setExceptionCode(String exceptionCode) {
        this.exceptionCode = exceptionCode;
        return this;
    }

    public ExceptionBuilder setLogMessage(String logMessage) {
        this.logMessage = logMessage;
        return this;
    }

    public ExceptionBuilder setMessage(Message message) {
        this.addMessage(message);
        return this;
    }

    public ExceptionBuilder setMessage(String i18nCode) {
        this.addMessage(i18nCode);
        return this;
    }

    public ExceptionBuilder setMessage(String i18nCode, String[] messageArguments) {
        this.addMessage(i18nCode, messageArguments);
        return this;
    }

    public ExceptionBuilder setMessages(List<Message> messages) {
        this.addMessages(messages);
        return this;
    }

    public ExceptionBuilder setOriginalException(Exception originalException) {
        this.originaleException = originalException;
        return this;
    }

    public void setSeverity(ExceptionSeverityEnum exceptionSeverityE) {
        this.severity = exceptionSeverityE.getEnumValue();
    }

    public ExceptionBuilder setSeverity(Integer severity) {
        this.severity = severity;
        return this;
    }


    /**
     * set
     * PrimaryKey of the subjectReferenceNumber as per the subjectType,
     * for e.g. if the subjectType = LOAN then
     * pass loanId as parameter i.e. setSubjectId(loanId)
     *
     * @return
     */
    public ExceptionBuilder setSubjectId(Long subjectId) {
        this.subjectId = subjectId;
        return this;
    }


    /**
     * set
     * LAN Number if the subjectType = LOAN
     * CustomerNumber if the subjectType = CUSTOMER
     *
     * @return
     */
    public ExceptionBuilder setSubjectReferenceNumber(String subjectReferenceNumber) {
        this.subjectReferenceNumber = subjectReferenceNumber;
        return this;
    }

    public ExceptionBuilder setSubjectType(Character subjectType) {
        this.subjectType = subjectType;
        return this;
    }

    public ExceptionBuilder markSubjectTypeLoan() {
        return setSubjectType(BaseExceptionSubjectTypeEnum.SUBJECT_TYPE_LOAN.getEnumValue());
    }

    public ExceptionBuilder markSubjectTypeCustomer() {
        return setSubjectType(BaseExceptionSubjectTypeEnum.SUBJECT_TYPE_CUSTOMER.getEnumValue());
    }

    public ExceptionBuilder markSubjectTypeNull() {
        return setSubjectType(BaseExceptionSubjectTypeEnum.NULL.getEnumValue());
    }

    public ExceptionBuilder setReProcessingRequired(Boolean reProcessingRequired) {
        this.reProcessingRequired = reProcessingRequired;
        return this;
    }

    public ExceptionBuilder setTransactionId(Long transactionId) {
        this.transactionId = transactionId;
        return this;
    }

    public ExceptionBuilder setTransactionReferenceNumber(String transactionReferenceNumber) {
        this.transactionReferenceNumber = transactionReferenceNumber;
        return this;
    }

    public ExceptionBuilder markLogged() {
        this.isLogged = Boolean.TRUE;
        return this;
    }

}
