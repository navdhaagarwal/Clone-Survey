package com.nucleus.cas.parentChildDeletionHandling;

import com.google.common.collect.ImmutableMap;
import com.nucleus.dao.query.JPAQueryExecutor;
import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.EntityLifeCycleData;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.makerchecker.MasterConfigurationRegistry;
import com.nucleus.master.BaseMasterEntity;
import com.nucleus.persistence.EntityDao;
import com.nucleus.user.User;
import com.nucleus.user.UserInfo;
import com.nucleus.user.UserService;
import org.apache.commons.collections4.MapUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import javax.inject.Inject;
import javax.inject.Named;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Named("parentChildDeleteHandlingService")
public class ParentChildDeleteHandlingServiceImpl implements ParentChildDeleteHandlingService{

    @Inject
    @Named("userService")
    private UserService userService;

    @Inject
    @Named("masterConfigurationRegistry")
    private MasterConfigurationRegistry masterConfigurationRegistry;

    @Inject
    @Named("entityDao")
    protected EntityDao entityDao;

    private static final String ACTIVE ="Active";
    private static final String INACTIVE="Inactive";

    public static final ImmutableMap<String,String> mapForMasterScreenNames = new ImmutableMap.Builder<String, String>()
            .put("DocumentDefinition","Documents")
            .build();

    @Override
    public List<DependencyUsageVO> prepareDependencyData(String masterId,Long id) {
        Class<?> entityClass=null;
        Map<Class,String> dependencyMap= new HashMap<>();
        List<DependencyUsageVO> dependencyUsageVOList = new ArrayList<>();
        if(masterId!=null && !masterId.isEmpty()){
            String entityPath=masterConfigurationRegistry.getEntityClass(masterId);
            if(entityPath!=null && !entityPath.isEmpty()) {
                try {
                    entityClass = Class.forName(entityPath);
                } catch (ClassNotFoundException e) {
                    BaseLoggers.exceptionLogger.error(e.getMessage());
                }
                if(entityClass!=null) {
                    dependencyMap = BaseMasterDependency.getDependencyGraphForEntity(entityClass);
                    if(MapUtils.isNotEmpty(dependencyMap)){
                        for(Map.Entry m : dependencyMap.entrySet()){
                            String query = (String)m.getValue();
                            List<Object> parentList= executeHQL(query,id);
                            prepareDependencyVOlist(parentList,dependencyUsageVOList);
                        }
                    }
                }
            }
        }
        return dependencyUsageVOList;
    }

    public List<Object> executeHQL(String query,Long id){
        JPAQueryExecutor jpaQueryExecutor = new JPAQueryExecutor(query);
        jpaQueryExecutor.addParameter("id",id);
        return entityDao.executeQuery(jpaQueryExecutor);
    }

    private void prepareDependencyVOlist(List<Object> parentList,List<DependencyUsageVO> dependencyUsageVOList){
        if(org.apache.commons.collections4.CollectionUtils.isNotEmpty(parentList)){
            for(Object parentObject : parentList){
                DependencyUsageVO dependencyUsageVO = new DependencyUsageVO();
                String simpleName = parentObject.getClass().getSimpleName();
                String displayName=null;
                Boolean isActive=null;
                EntityLifeCycleData createdByEntityId=null;
                Method getDisplayName = null;
                Method getEntityLifeCycleData = null;
                Method isActiveFlag = null;

                try {

                    getDisplayName = parentObject.getClass().getMethod("getDisplayName");
                    getEntityLifeCycleData= BaseEntity.class.getDeclaredMethod("getEntityLifeCycleData");
                    isActiveFlag= BaseMasterEntity.class.getDeclaredMethod("isActiveFlag");


                    if(getDisplayName!=null)
                        displayName = (String) getDisplayName.invoke(parentObject);
                    if(getEntityLifeCycleData!=null){
                        createdByEntityId = (EntityLifeCycleData) getEntityLifeCycleData.invoke(parentObject);
                    }
                    if(isActiveFlag!=null){
                        isActive=(Boolean) isActiveFlag.invoke(parentObject);
                    }

                } catch (Exception e) {
                    BaseLoggers.exceptionLogger.error(e.getMessage());
                }
                if(simpleName!=null && !simpleName.isEmpty()){
                    if(mapForMasterScreenNames.get(simpleName)!=null && !mapForMasterScreenNames.get(simpleName).isEmpty()){
                        dependencyUsageVO.setMaster(mapForMasterScreenNames.get(simpleName));
                    }else{
                        dependencyUsageVO.setMaster(simpleName);
                    }
                }
                if(displayName!=null && !displayName.isEmpty()){
                        dependencyUsageVO.setMasterName(displayName);
                }

                if(isActive!=null){
                    if(isActive)
                        dependencyUsageVO.setStatus(ACTIVE);
                    else
                        dependencyUsageVO.setStatus(INACTIVE);
                }
                if(createdByEntityId!=null){
                    DateTimeFormatter fmt = DateTimeFormat.forPattern(userService.getUserPreferredDateTimeFormat());
                    if(createdByEntityId.getCreatedByEntityId()!=null && createdByEntityId.getCreatedByEntityId().getLocalId()!=null) {
                        UserInfo userInfo = userService.getUserById(createdByEntityId.getCreatedByEntityId().getLocalId());
                        dependencyUsageVO.setCreatedBy(userInfo.getUsername());
                    }
                    if(createdByEntityId.getCreationTimeStamp()!=null){
                        dependencyUsageVO.setCreationTime(fmt.print(createdByEntityId.getCreationTimeStamp()));
                    }
                    if(createdByEntityId.getLastUpdatedTimeStamp()!=null){
                        dependencyUsageVO.setUpdationTime(fmt.print(createdByEntityId.getLastUpdatedTimeStamp()));
                    }
                    if(createdByEntityId.getLastUpdatedByUri()!=null && !createdByEntityId.getLastUpdatedByUri().isEmpty()){
                        User user = userService.getUserByUri(createdByEntityId.getLastUpdatedByUri());
                        dependencyUsageVO.setUpdatedBy(user.getUsername());
                    }
                }
                dependencyUsageVOList.add(dependencyUsageVO);
            }
        }
    }
}
