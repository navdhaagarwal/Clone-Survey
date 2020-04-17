package com.nucleus.core.dynamicform.service;

import java.util.*;

public class FormConfigurationConstant {

    public static String MODEL_NAME = "Model";

    public static String MODEL_DESCRIPTION = "Model";

    public static String PANEL_NAME = "Panel";

    public static String PANEL_HEADER = "Panel";

    public static String CUSTOM_BINDER = "Custom Binder";

    public static String FORM_VERSION_CONTROL = "Version";

    public static String PERSISTENT_FORM_DATA = "persistentFormData";

    public static String UI_META_DATA = "uiMetaData";

    public static String PRIMARY_APPLICANT = "Primary Applicant";
    public static String CO_APPLICANT = "Co Applicant";
    public static String GUARANTOR = "Guarantor";


    public static final Map<String, Integer> dynamicFormPartyRoleMap = new HashMap<>();

    static {
        dynamicFormPartyRoleMap.put(PRIMARY_APPLICANT,0);
        dynamicFormPartyRoleMap.put(CO_APPLICANT,1);
        dynamicFormPartyRoleMap.put(GUARANTOR,2);
    }

}
