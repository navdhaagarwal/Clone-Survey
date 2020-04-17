package com.nucleus.regional.config.dao;

import javax.inject.Named;
import javax.persistence.Query;

import com.nucleus.persistence.EntityDaoImpl;
import com.nucleus.regional.metadata.RegionalMetaData;

@Named("regionalRequestResponseDao")
public class RegionalRequestResponseDao extends EntityDaoImpl implements
        IRegionalRequestResponseDao {

    @Override
    public Object fetchTargetValueFromValidationQuery(String fieldValue,
            RegionalMetaData regionalMetaDataObj) {
        Query query = getEntityManager().createQuery(regionalMetaDataObj.getValidationQuery());
        query.setParameter("parameterValue", fieldValue);
        return query.getSingleResult();
    }
}
