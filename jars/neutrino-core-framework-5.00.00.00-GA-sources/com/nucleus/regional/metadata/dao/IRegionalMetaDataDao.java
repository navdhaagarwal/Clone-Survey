package com.nucleus.regional.metadata.dao;

import java.util.List;
import com.nucleus.regional.metadata.RegionalMetaData;

public interface IRegionalMetaDataDao {

	public List<RegionalMetaData> fetchRegionalMetaDataforSourceEntity(String sourceEntityName);
}
