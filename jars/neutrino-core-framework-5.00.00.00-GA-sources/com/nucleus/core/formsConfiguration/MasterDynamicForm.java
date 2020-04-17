package com.nucleus.core.formsConfiguration;

public interface MasterDynamicForm extends IDynamicForm{

	DynamicForm getDynamicForm();
	
	void setDynamicForm(DynamicForm dynamicForm);
	
	UIMetaDataVo getUiMetaDataVo();
	
	void setUiMetaDataVo(UIMetaDataVo uIMetaDataVo);
}
