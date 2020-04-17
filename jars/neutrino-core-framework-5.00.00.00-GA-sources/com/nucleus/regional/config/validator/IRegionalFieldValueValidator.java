package com.nucleus.regional.config.validator;

import java.util.List;

import com.nucleus.regional.metadata.RegionalMetaData;

public interface IRegionalFieldValueValidator {

    Object getTargetValueForRegionalField(String sourceFieldValue,
            RegionalMetaData regionalMetaDataObj, List fields);
}
