package com.nucleus.finnone.pro.lov.serviceinterface;

import java.util.List;
import java.util.Map;

import com.nucleus.finnone.pro.lov.LOVSearchVO;

public interface ILOVService {

	Map<String, Object> loadLOVData(LOVSearchVO lovSearchVO);
	void setRequiredParametersForLOV(List searchRecords,LOVSearchVO lovSearchVO,Map<String, Object> searchRecordMap);
	<T extends Object> Map<String, Object> setRequiredParametersForLOVWithAllData(List<T> searchRecords, LOVSearchVO lovSearchVO);
	
}
