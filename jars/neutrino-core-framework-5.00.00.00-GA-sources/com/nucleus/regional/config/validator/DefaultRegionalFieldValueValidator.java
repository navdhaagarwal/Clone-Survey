package com.nucleus.regional.config.validator;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import com.nucleus.regional.config.dao.IRegionalRequestResponseDao;
import com.nucleus.regional.metadata.RegionalMetaData;

@Named("defaultRegionalFieldValueValidator")
public class DefaultRegionalFieldValueValidator implements
        IRegionalFieldValueValidator {

    @Inject
    @Named("regionalRequestResponseDao")
    private IRegionalRequestResponseDao regionalRequestResponseDao;

    @Override
    public Object getTargetValueForRegionalField(String sourceFieldValue,
            RegionalMetaData regionalMetaDataObj, List fields) {
        return regionalRequestResponseDao.fetchTargetValueFromValidationQuery(
                sourceFieldValue, regionalMetaDataObj);
    }

}
