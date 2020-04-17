/**
 * 
 */
package com.nucleus.finnone.pro.general.util.documentgenerator;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

import com.nucleus.finnone.pro.base.Message;
import com.nucleus.finnone.pro.base.utility.CoreUtility;

/**
 * @author shivani.aggarwal
 *
 */
public class DocumentGeneratorException extends RuntimeException {
	
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private static final int THOUSAND=1000;
		private List<Message> messages;

		private boolean isLogged;

		private String uniqueID;

		private String logMessage;

		private String exceptionCode;

		private Exception originalException;

		public DocumentGeneratorException() {
			this.isLogged = false;
			this.uniqueID = getUniqueId();
		}
		/**
		 * 
		 * @param message
		 * @param cause
		 * @param exceptionCode
		 * @param messages
		 */
		public DocumentGeneratorException(String message, Throwable cause,String exceptionCode) {
			super(cause);
			this.logMessage=message;
			this.isLogged = false;
			this.uniqueID = getUniqueId();
			this.exceptionCode=exceptionCode;
			
		}
		public DocumentGeneratorException(String message, String exceptionCode) {
			this.logMessage=message;
			this.isLogged = false;
			this.uniqueID = getUniqueId();
			this.exceptionCode=exceptionCode;
			
		}
		public DocumentGeneratorException(String message) {
			super(message);
		}

		public DocumentGeneratorException(String message, Throwable cause) {
			super(message, cause);
		}

		public DocumentGeneratorException(Throwable cause) {
			super(cause);
		}

		public String getExceptionCode() {
			return exceptionCode;
		}

		public String getLogMessage() {
			return logMessage;
		}

		public List<Message> getMessages() {
			
			return messages==null?new ArrayList<Message>():messages;
		}

		public Exception getOriginalException() {
			return originalException;
		}

		public String getUniqueID() {
			return uniqueID;
		}

		public boolean isLogged() {
			return isLogged;
		}

		public void setExceptionCode(String exceptionCode) {
			this.exceptionCode = exceptionCode;
		}

		public void setLogged(boolean isLogged) {
			this.isLogged = isLogged;
		}

		public void setLogMessage(String logMessage) {
			this.logMessage = logMessage;
		}

		public void setMessages(List<Message> theMessages) {
			this.messages = theMessages;
		}

		public void setOriginalException(Exception originalException) {
			this.originalException = originalException;
		}

		private String getUniqueId() {
			return CoreUtility.getUniqueId();
		}
}
