package com.nucleus.core.formsConfiguration;

import java.util.List;
import java.util.Map;

public interface MultipleForm extends IDynamicForm{

     DynamicForm getDynamicForm(String formName);

     List<DynamicForm> getAllDynamicForms();

     void addMultiDynamicForm(DynamicForm dynamicForm);

     Map<String,DynamicForm> getAllDynamicFormAsMap();

}
