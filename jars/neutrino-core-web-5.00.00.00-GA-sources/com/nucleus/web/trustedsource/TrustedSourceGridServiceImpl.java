package com.nucleus.web.trustedsource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;

import com.nucleus.entity.EntityId;
import com.nucleus.finnone.pro.general.util.ValidatorUtils;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.makerchecker.GridVO;
import com.nucleus.master.BaseMasterService;
import com.nucleus.security.oauth.domainobject.OauthClientDetails;
import com.nucleus.security.oauth.util.TrustedSourceHelper;
import com.nucleus.web.master.MakerCheckerWebUtils;
import com.nucleus.web.master.MasterGridServiceImpl;

@Named("trustedSourceGridService")
public class TrustedSourceGridServiceImpl extends MasterGridServiceImpl {
	@Inject
	@Named("baseMasterService")
	private BaseMasterService baseMasterService;
	private static final String ERROR_MSG = "Exception in rendering data to Grid";

	@Override
	public Map<String, Object> loadPaginatedData(GridVO gridVO, Class entityName, String userUri, Long parentId) {

		Map<String, Object> returnMap = new HashMap<>();
		List<Object> bmeList = baseMasterService.loadPaginatedData(gridVO, entityName, userUri);
		Integer recordCount = baseMasterService.getTotalRecordSize(entityName, userUri);
		gridVO.setiDisplayStart(null);
		gridVO.setiDisplayLength(null);
		Long searchRecordsCount;
		if (ValidatorUtils.hasNoEntry(gridVO.getSearchMap())) {
			searchRecordsCount = (long) recordCount;
		} else {
			searchRecordsCount = baseMasterService.getSearchRecordsCount(gridVO, entityName, userUri);
			if (searchRecordsCount == null) {
				searchRecordsCount = 0l;
			}
		}
		try {
			for (Object o : bmeList) {
				OauthClientDetails singleEntity = (OauthClientDetails) o;
				if (TrustedSourceHelper.isInternalModule(singleEntity.getClientId())) {
					Object action = singleEntity.getViewProperties().get("actions");
					if (action != null && action != "") {
						((List<String>) action).remove("Delete");
						singleEntity.addProperty("actions", action);
					}

				}
				String createdBy = null;
				String reviewedBy = null;
				String createdByUserFullName = null;
				String reviewedByUserFullName = null;
				if (singleEntity.getEntityLifeCycleData() != null) {
					EntityId createorEntityId;

					if (StringUtils.isNotBlank(singleEntity.getEntityLifeCycleData().getLastUpdatedByUri()))
						createorEntityId = EntityId
								.fromUri(singleEntity.getEntityLifeCycleData().getLastUpdatedByUri());
					else
						createorEntityId = singleEntity.getEntityLifeCycleData().getCreatedByEntityId();

					if (createorEntityId != null) {
						createdBy = userService.getUserNameByUserId(createorEntityId.getLocalId());
						createdByUserFullName = userService.getUserFullNameForUserId(createorEntityId.getLocalId());
					}
					EntityId revieworEntityId = singleEntity.getMasterLifeCycleData().getReviewedByEntityId();
					if (revieworEntityId != null) {
						reviewedBy = userService.getUserNameByUserId(revieworEntityId.getLocalId());
						reviewedByUserFullName = userService.getUserFullNameForUserId(revieworEntityId.getLocalId());
					}
					singleEntity.addProperty("uuid", singleEntity.getEntityLifeCycleData().getUuid());
				}

				if (createdBy != null) {
					singleEntity.addProperty("createdBy", createdBy);
					singleEntity.addProperty("createdByUserWithFullName",
							createdBy + " [" + createdByUserFullName + "]");
				}
				if (reviewedBy != null) {
					singleEntity.addProperty("reviewedBy", reviewedBy);
					singleEntity.addProperty("reviewedByUserWithFullName",
							reviewedBy + " [" + reviewedByUserFullName + "]");
				}
				singleEntity.addProperty("approvalStatus",
						MakerCheckerWebUtils.getApprovalStatus(singleEntity.getApprovalStatus()));

			}
		} catch (Exception e) {
			BaseLoggers.exceptionLogger.error(ERROR_MSG, e);
		}
		returnMap.put("entityList", bmeList);
		returnMap.put("recordCount", searchRecordsCount.intValue());
		returnMap.put("totalRecordCount", recordCount);
		return returnMap;
	}

}
