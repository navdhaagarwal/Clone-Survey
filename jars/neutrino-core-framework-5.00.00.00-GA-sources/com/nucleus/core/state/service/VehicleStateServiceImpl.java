package com.nucleus.core.state.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.Query;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.PredicateUtils;

import com.nucleus.address.State;
import com.nucleus.dao.query.JPAQueryExecutor;
import com.nucleus.entity.ApprovalStatus;
import com.nucleus.entity.EntityId;
import com.nucleus.master.BaseMasterEntity;
import com.nucleus.master.BaseMasterService;
import com.nucleus.service.BaseServiceImpl;

@Named("vehicleStateService")
public class VehicleStateServiceImpl extends BaseServiceImpl implements VehicleStateService  {

    @Inject
    @Named("baseMasterService")
    private BaseMasterService baseMasterService;
    
    public static String QUERY_FOR_STATE_RTOC_CODE  = "select new map(state.stateCode as stateCode,vehicleRegMapp.stateRTOCode as stateRTOCode) from State state inner join state.vehicleStateRegistraionMappings vehicleRegMapp where state.masterLifeCycleData.approvalStatus in :approvalStatusList"
                                                    + " and vehicleRegMapp.stateRTOCode in( :stateRTOCodes)";
    
    
    @Override
    public Map<String, List<String>> checkForDuplicateStateRTOCode(Long stateId,List<String> stateRtoCodes) {
        
        List<String> rtoCodes = new ArrayList<String>();
        rtoCodes.addAll(stateRtoCodes);
        CollectionUtils.filter(rtoCodes, PredicateUtils.notNullPredicate());
        if(CollectionUtils.isEmpty(rtoCodes)) {
            return null;
        }
        StringBuilder quertString = new StringBuilder();
        quertString.append(QUERY_FOR_STATE_RTOC_CODE);
        Map<String, Object> boundParameters = new HashMap<String, Object>();
        boundParameters.put("stateRTOCodes", rtoCodes);
        if(stateId != null) {
            quertString.append(" and Not (state.id  =:stateId)");
            boundParameters.put("stateId", stateId);
            BaseMasterEntity baseMasterEntity = baseMasterService.getLastApprovedEntityByUnapprovedEntityId(new EntityId(State.class, stateId));
            if (baseMasterEntity != null) {
                quertString.append(" and Not (state.entityLifeCycleData.uuid = :uuid)");
                boundParameters.put("uuid",baseMasterEntity.getEntityLifeCycleData().getUuid());
            }
        }
        
        boundParameters.put("approvalStatusList",getApprocalStatus());

        JPAQueryExecutor<Map<String,String>> jpaQueryExecutor = new JPAQueryExecutor<Map<String,String>>(quertString.toString());
        addAllParametersIntoQuery(jpaQueryExecutor,boundParameters);
        List<Map<String,String>> results = entityDao.executeQuery(jpaQueryExecutor);
        return convertToSingleMap(results);
    }


    private List<Integer> getApprocalStatus() {
        return Arrays.asList(
                ApprovalStatus.UNAPPROVED_ADDED,
                ApprovalStatus.APPROVED_MODIFIED,
                ApprovalStatus.UNAPPROVED_MODIFIED,
                ApprovalStatus.WORFLOW_IN_PROGRESS,
                ApprovalStatus.APPROVED_DELETED_IN_PROGRESS,
                ApprovalStatus.APPROVED_DELETED,
                ApprovalStatus.APPROVED);
    }
    private void addAllParametersIntoQuery(JPAQueryExecutor jpaQueryExecutor,Map<String, Object> boundParameters) {
        for (Entry<String, Object> parameter : boundParameters.entrySet()) {
            jpaQueryExecutor.addParameter(parameter.getKey(), parameter.getValue());
        }
    }
    private Map<String, List<String>> convertToSingleMap(List<Map<String,String>> list) {
        Map<String, List<String>> singleMap  = new HashMap<String,List<String>>();
        for (Map<String, String> map : list) {
            
            if (!singleMap.containsKey(map.get("stateCode"))) {
                List<String> rtoCode = new ArrayList<String>();
                rtoCode.add(map.get("stateRTOCode"));
                singleMap.put(map.get("stateCode"), rtoCode);
            } else {
                if (singleMap.get(map.get("stateCode")) != null) {
                    singleMap.get(map.get("stateCode")).add(map.get("stateRTOCode"));
                }
            }
        }
        return singleMap;
    }
}
