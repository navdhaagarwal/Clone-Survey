package com.nucleus.user;

import java.util.List;

import javax.inject.Named;

import com.nucleus.address.Area;
import com.nucleus.dao.query.NamedQueryExecutor;
import com.nucleus.entity.ApprovalStatus;
import com.nucleus.service.BaseServiceImpl;

@Named("userCityVillageMappingService")
public class UserCityVillageMappingServiceImpl  extends BaseServiceImpl implements UserCityVillageMappingService{

    public List<Area> getAreaFromCity(Long cityId) {

        NamedQueryExecutor<Area> areaFromCityCriteria = new NamedQueryExecutor<Area>(
                "Area.AreaByCityId").addParameter("cityId", cityId)
        		.addParameter("approvalStatus", ApprovalStatus.APPROVED_RECORD_STATUS_LIST);
        return entityDao.executeQuery(areaFromCityCriteria);

    }

    public List<Area> getAreaFromVillage(Long villageId) {
        NamedQueryExecutor<Area> areaFromCityCriteria = new NamedQueryExecutor<Area>(
                "Area.AreaByVillageId").addParameter("villageId", villageId).
        		addParameter("approvalStatus", ApprovalStatus.APPROVED_RECORD_STATUS_LIST);
        return entityDao.executeQuery(areaFromCityCriteria);
    }

    public UserCityVillageMapping getCityVillageMappingByUserId(Long userId) {
        NamedQueryExecutor<UserCityVillageMapping> mappingFromUserId = new NamedQueryExecutor<UserCityVillageMapping>(
                "UserCityVillageMapping.getEntityByUserId").addParameter("userId", userId);
        return entityDao.executeQueryForSingleValue(mappingFromUserId);
    }



    public UserCityVillageMapping deletePrevMapping(UserCityVillageMapping prevUserCityVillageMapping, UserCityVillageMapping userCityVillageMapping) {


        if(prevUserCityVillageMapping!=null) {
            List<UserCityMapping> prevUserCityMappingList = prevUserCityVillageMapping.getUserCityMappings();
            List<UserVillageMapping> prevUserVillageMappingList = prevUserCityVillageMapping.getUserVillageMappings();

            if (prevUserCityMappingList != null && !prevUserCityMappingList.isEmpty()) {
                prevUserCityMappingList.forEach(prevUserCityMapping -> {
                    entityDao.delete(prevUserCityMapping);
                });
            }

            if (prevUserVillageMappingList != null && !prevUserVillageMappingList.isEmpty()) {
                prevUserVillageMappingList.forEach(prevUserVillageMapping -> {
                    entityDao.delete(prevUserVillageMapping);
                });
            }

        }
        else{
            prevUserCityVillageMapping = new UserCityVillageMapping();
        }

        prevUserCityVillageMapping.setUserVillageMappings(userCityVillageMapping.getUserVillageMappings());
        prevUserCityVillageMapping.setUserCityMappings(userCityVillageMapping.getUserCityMappings());
        return prevUserCityVillageMapping;

    }

}



