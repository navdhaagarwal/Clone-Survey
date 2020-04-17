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

import com.nucleus.core.dynamicform.entities.ServicePlaceholderMapping;
import com.nucleus.master.BaseMasterEntity;

public interface ServicePlaceHolderdao extends BaseDao<BaseMasterEntity> {

	List<ServicePlaceholderMapping> getServicePlaceholderMappingListFromServiceIdentifier(Long serviceIdentifierId);

    }
