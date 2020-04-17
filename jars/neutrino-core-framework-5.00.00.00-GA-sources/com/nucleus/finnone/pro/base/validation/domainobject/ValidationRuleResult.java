/*
 * Author: Ratna Shankar Mishra Creation Date: 23/07/2012 Copyright: Nucleus
 * Software Exports Ltd. Description:Domain object for storing validation
 * failure message details and calling the validator for that context.
 * ----------
 * --------------------------------------------------------------------
 * ------------------------------------------------------ Revision: Version Last
 * Revision Date Name Function / Module affected Modifications Done
 * --------------
 * ----------------------------------------------------------------
 * ------------------------------------------------------ 1.0 23/07/2012 Ratna
 * Shankar Mishra Initial Version created
 */
package com.nucleus.finnone.pro.base.validation.domainobject;

import com.nucleus.finnone.pro.base.Message;

/**
 * <p>
 * Used for set validation failure message details.
 * </p>
 */
public class ValidationRuleResult {
  
  /** To set Message Object */
  private Message i18message;
  
  /** @deprecated
   * To set id */
  /**
   * @deprecated
   */
  @Deprecated
  private Long id;
  
  /**
   * @deprecated
   *  To set message */
  /**
   * @deprecated
   */
  /**
   * @deprecated
   */
  @Deprecated
  private String message;
  
  /**
   * Default Constructor
   */
  public ValidationRuleResult() {
    super();
  }
  
  /**
   * 
   * @param i18message
   */
  public ValidationRuleResult(Message i18message) {
    this.i18message = i18message;
  }
  
  /**
   * @deprecated
   */
  @Deprecated
  public Long getId() {
    return id;
  }
  
  /**
   * @deprecated
   * @param theId
   *          the theId to set
   */
  @Deprecated
  public void setId(Long theId) {
    id = theId;
    this.i18message.setI18nCode(theId.toString());
  }
  
  /**
   * @return the message
   */
  public String getMessage() {
    return message;
  }
  
  /**
   * @param theMessage
   *          the theMessage to set
   */
  
  public void setMessage(String theMessage) {
    message = theMessage;
  }
  
  /**
   * @return the i18message
   */
  public Message getI18message() {
    return i18message;
  }
  
  /**
   * @param i18message
   *          the i18message to set
   */
  public void setI18message(Message i18message) {
    this.i18message = i18message;
  }
}