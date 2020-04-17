package com.nucleus.finnone.pro.general.constants;

/**
 * @author merajul.ansari
 * 
 */
public enum YesNoCharacterEnum {
      YES('Y',"lbl.yes","Yes"),
      NO('N',"lbl.no","No"),
      NULL(null,"NULL","Null Enum"),
      NOT_SUPPORTED(' ',"Not Supported","This Enum code is Not Supported");
    private Character enumValue;
    private String description;
    private String i18nCode;
    
  private YesNoCharacterEnum(Character enumValue, String i18nCode, String description) {
    this.enumValue = enumValue;
    this.i18nCode = i18nCode;
    this.description = description;
  }
  
  public static YesNoCharacterEnum getEnumFromValue(Character enumValue) {
    YesNoCharacterEnum yesNoE = null;
    if (enumValue == null) {
      return NULL;
    }
    
    for (YesNoCharacterEnum supportedEnum : values()) {
      if (enumValue.equals(supportedEnum.enumValue)) {
        yesNoE = supportedEnum;
      }
    }
    if (yesNoE == null) {
      yesNoE = NOT_SUPPORTED;
    }
    return yesNoE;
  }
  
  
  
  public Character getEnumValue() {
    return enumValue;
  }
  

  
  public String getDescription() {
    return description;
  }
  

  
  public String getI18nCode() {
    return i18nCode;
  }
  

  public boolean equalsValue(Character theEnumValue) {
    return theEnumValue != null ? enumValue.equals(theEnumValue) : false;
  }
}
