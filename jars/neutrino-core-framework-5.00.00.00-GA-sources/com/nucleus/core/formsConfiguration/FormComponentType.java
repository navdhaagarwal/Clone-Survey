package com.nucleus.core.formsConfiguration;

import javax.persistence.Cacheable;
import javax.persistence.Entity;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.genericparameter.entity.GenericParameter;

/**
 * Constants for Dynamic Form Field Type
 */
@Entity
@DynamicUpdate
@DynamicInsert
@Cacheable
public class FormComponentType extends GenericParameter {

    private static final long serialVersionUID = 2852708086471441004L;

    public static String      TEXT_BOX         = "TextBox";

    public static String      TEXT_AREA        = "TextArea";

    public static String      DROP_DOWN        = "DropDown";

    public static String      DATE             = "Calendar";

    public static String      RADIO            = "Radio";

    public static String      CHECKBOX         = "CheckBox";

    public static String      MONEY            = "Money";

    public static String      PANEL            = "Panel";

    public static String      TABLE            = "Table";

    public static String      MULTISELECTBOX   = "MultiSelectBox";

    public static String      AUTOCOMPLETE     = "AutoComplete";

    public static String      PHONE            = "Phone";

    public static String      EMAIL            = "Email";

    public static String      CASCADED_SELECT  = "CascadedSelect";
    
    public static String      HYPERLINK  = "Hyperlink";
    	
    public static String      BUTTON  = "Button";

    public static String      SPECIAL_TABLE     = "AutoPopulate_Table";
    public static String      CUSTOM_CASCADED_SELECT  = "CustomCascadedSelect";

    public static String      CURRENT_TIME_STAMP  = "Current_Time_Stamp";

    public static String      LOV               = "LOV";

}
