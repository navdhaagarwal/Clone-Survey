package com.nucleus.regional.config.dao;

import com.nucleus.regional.metadata.RegionalMetaData;

public interface IRegionalRequestResponseDao {

    Object fetchTargetValueFromValidationQuery(String fieldValue, RegionalMetaData regionalMetaDataObj);
}
