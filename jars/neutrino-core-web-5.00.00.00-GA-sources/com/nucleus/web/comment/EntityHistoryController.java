package com.nucleus.web.comment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import org.joda.time.DateTime;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.EntityId;
import com.nucleus.entityhistory.EntityHistoryService;
import com.nucleus.snapshotinfo.SnapshotInfo;
import com.nucleus.snapshotinfo.SnapshotService;
import com.nucleus.user.UserService;
import com.nucleus.web.common.controller.BaseController;

@Controller
@Transactional
@RequestMapping("/EntityHistory")
public class EntityHistoryController extends BaseController {

    @Inject
    @Named("entityHistoryService")
    private EntityHistoryService entityHistoryService;

    @Inject
    @Named("userService")
    private UserService          userService;

    @Inject
    @Named("snapshotService")
    private SnapshotService      snapshotService;

    /**
     * This method is to retrieve history for given entity. History for a given
     * Entity will be fetched from its UUID. Since, entity which is going to be
     * modified will have the same UUID as that of its original Entity
     */
    // TODO: For now, history will be retrieved by using creationTimeStamp and
    // CreatedByUri fields of EntityLifeCycleData. Later it will retrieve from
    // lastUpdatedByUri and lastUpdatedTimeStamp fields. Since there is no value
    // set for lastUpdatedByUri
    // field of EntityLifeCycleData when an entity is persisted.

    @SuppressWarnings("unchecked")
    @RequestMapping("/retrieveHistory/{id}")
    public String retrieveEntityHistory(@PathVariable Long id, @RequestParam String currentEntityUri, ModelMap map)
            throws ClassNotFoundException {

        Class<? extends BaseEntity> entityClass = (Class<? extends BaseEntity>) Class.forName(currentEntityUri);

        List<Map<String, Object>> entityHistoryList = new ArrayList<Map<String, Object>>();
        List<BaseEntity> affectedEntitiesList = null;
        BaseEntity baseEntity = entityHistoryService.getBaseEntityByEntityId(entityClass, id);

        String entityCreationTimeStamp = "Time is not defined";
        String entityUUId = baseEntity.getEntityLifeCycleData().getUuid();

        if (entityUUId != null) {
            affectedEntitiesList = entityHistoryService.getBaseEntityByEntityUUID(entityClass, entityUUId);
            for (BaseEntity baseEntityData : affectedEntitiesList) {

                Map<String, Object> entitiesMap = new HashMap<String, Object>();

                DateTime entityTimeStamp = baseEntityData.getEntityLifeCycleData().getCreationTimeStamp();
                if (entityTimeStamp != null) {

                    String formatDate = getFormattedDate(entityTimeStamp);
                    String formatTime = getFormattedDate(entityTimeStamp);

                    entityCreationTimeStamp = formatDate + " " + formatTime;
                }
                entitiesMap.put("entityUpdationTimeStamp", entityCreationTimeStamp);

                EntityId userIdOfThisEntity = EntityId.fromUri(baseEntityData.getEntityLifeCycleData().getCreatedByUri());

                if (userIdOfThisEntity != null) {

                    entitiesMap.put("entityUpdatedBy", userService.getUserById(userIdOfThisEntity.getLocalId())
                            .getDisplayName());
                }

                entityHistoryList.add(entitiesMap);
            }
        } else {
            Map<String, Object> entityMap = new HashMap<String, Object>();
            DateTime entityTimesStamp = baseEntity.getEntityLifeCycleData().getCreationTimeStamp();
            if (entityTimesStamp != null) {

                String formatDate = getFormattedDate(entityTimesStamp);
                String formatTime = getFormattedDate(entityTimesStamp);

                entityCreationTimeStamp = formatDate + " " + formatTime;
            }
            entityMap.put("entityUpdationTimeStamp", entityCreationTimeStamp);
            EntityId userId = EntityId.fromUri(baseEntity.getEntityLifeCycleData().getCreatedByUri());
            entityMap.put("entityUpdatedBy", userService.getUserById(userId.getLocalId()).getDisplayName());
            entityHistoryList.add(entityMap);

            List<SnapshotInfo> snapshotInfoList = snapshotService.getSnapshotInfoForEntity(baseEntity.getEntityId());
            for (SnapshotInfo snapshotInfo : snapshotInfoList) {
                Map<String, Object> entitiesMap = new HashMap<String, Object>();
                DateTime entityTimeStamp = snapshotInfo.getEntityLifeCycleData().getCreationTimeStamp();
                if (entityTimeStamp != null) {

                    String formatDate = getFormattedDate(entityTimeStamp);
                    String formatTime = getFormattedDate(entityTimeStamp);

                    entityCreationTimeStamp = formatDate + " " + formatTime;
                }
                entitiesMap.put("entityUpdationTimeStamp", entityCreationTimeStamp);
                EntityId userIdOfThisEntity = EntityId.fromUri(snapshotInfo.getEntityLifeCycleData().getCreatedByUri());

                if (userIdOfThisEntity != null) {

                    entitiesMap.put("entityUpdatedBy", userService.getUserById(userIdOfThisEntity.getLocalId())
                            .getDisplayName());
                }

                entityHistoryList.add(entitiesMap);

            }
        }

        Collections.reverse(entityHistoryList);

        // First element will always be the creator of this entity
        if (entityHistoryList.size() > 1) {
            entityHistoryList.add(0, entityHistoryList.get(entityHistoryList.size() - 1));
            entityHistoryList.remove(entityHistoryList.size() - 1);
        }
        map.put("entityHistoryList", entityHistoryList);

        return "historyTabContent";

    }
}
