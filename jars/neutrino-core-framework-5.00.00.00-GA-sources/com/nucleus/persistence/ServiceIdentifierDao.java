/**
 * @FileName: ServicePlaceHolderdao.java
 * @Author: sachin
 * @Copyright: Nucleus Software Exports Ltd
 * @Description:
 * @Program-specification-Referred:
 * @Revision:
 *            --------------------------------------------------------------------------------------------------------------
 *            --
 * @Version | @Last Revision Date | @Name | @Function/Module affected | @Modifications Done
 *          ----------------------------------------------------------------------------------------------------------------
 *          | FEB 23, 2017 | sachin | |
 */

package com.nucleus.persistence;

import java.util.List;

import com.nucleus.master.BaseMasterEntity;
import com.nucleus.ws.core.entities.ServiceIdentifier;

public interface ServiceIdentifierDao extends BaseDao<BaseMasterEntity> {

	public List<ServiceIdentifier> getUnmappedServiceIdentifiersListDaoForFields(Class<ServiceIdentifier> entityClass, List<Integer> statusList);
	public List<ServiceIdentifier> getUnmappedServiceIdentifiersListDaoForPlaceholders(Class<ServiceIdentifier> entityClass, List<Integer> statusList);
	public ServiceIdentifier getServiceIdentifierByCode(String code);

    }
