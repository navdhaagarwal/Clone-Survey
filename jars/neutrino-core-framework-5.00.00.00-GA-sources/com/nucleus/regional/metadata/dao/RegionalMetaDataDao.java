package com.nucleus.regional.metadata.dao;

import java.util.List;

import javax.inject.Named;
import javax.persistence.Query;

import com.nucleus.persistence.EntityDaoImpl;
import com.nucleus.regional.metadata.RegionalMetaData;

@Named("regionalMetaDataDao")
public class RegionalMetaDataDao extends EntityDaoImpl implements IRegionalMetaDataDao{

	@Override
	public List<RegionalMetaData> fetchRegionalMetaDataforSourceEntity(String sourceEntityName) {
		Query query =getEntityManager().createNamedQuery("getRegionalMetaDataForSourceEntity");
		query.setParameter("fullyQualifiedEntityName",sourceEntityName);
		List<RegionalMetaData>  regionalMetaDataList=query.getResultList();
		return regionalMetaDataList;
	}

}
