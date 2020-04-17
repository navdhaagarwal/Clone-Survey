package com.nucleus.finnone.pro.lov.businessobject;

import java.io.Serializable;
import java.util.List;


import com.nucleus.entity.BaseEntity;
import com.nucleus.finnone.pro.lov.LOVSearchVO;
import com.nucleus.master.BaseMasterEntity;


public interface ILOVBusinessObject {

	  int getTotalRecordSize(Class<Serializable> paramClass);
	  
	  <T extends BaseMasterEntity> List<T> 
					loadPaginatedMasterEntityData(Class<T> entityClass,
									  LOVSearchVO lovSearchVO);
	  <T extends BaseEntity> List<T> 
	  				loadPaginatedEntityData(Class<T> entityClass,
	  								  LOVSearchVO lovSearchVO);
}
