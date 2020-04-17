package com.nucleus.web.binder;

import java.util.List;
import java.util.Map;

import javax.transaction.Transactional;

import com.nucleus.core.loanproduct.ProductTypeService;

@Transactional
public class ProductTypeDataBinder extends AbstractWebDataBinder<List<?>>  {

	private final String[]  colList;
	
	public ProductTypeDataBinder(String... colList){
		this.colList = colList;
	}

	@SuppressWarnings("unchecked")
	@Override
	
	public List<?> getData() {
		ProductTypeService productTypeService = (ProductTypeService) getWebApplicationContext().getBean("productTypeService");
		List<Map<String, Object>> dataForEntity = (List<Map<String, Object>>) productTypeService.getAllActiveProductTypes(colList);
       
        return dataForEntity;
	}

}
