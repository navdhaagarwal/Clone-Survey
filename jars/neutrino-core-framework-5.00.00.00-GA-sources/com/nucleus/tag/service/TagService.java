package com.nucleus.tag.service;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.EntityId;
import com.nucleus.service.BaseService;
import com.nucleus.tag.entity.ClassificationTag;
import com.nucleus.tag.entriesTaskTagVo.EntriesAndTaskTagVo;

// TODO: Auto-generated Javadoc
/**
 * The Interface TagService.
 */
public interface TagService extends BaseService {

    /**
     * Gets the all the classification tags that are linked with the given entity.
     *
     * @param entityId the entity id
     * @return the all classification tags for the entity
     */
    public List<String> getAllClassificationTagsForUri(EntityId entityId);

    /**
     * Gets the all the classification tags that are linked with the given entity.
     *
     * @param entityId the entity id
     * @param  taskId  the taskId
     * @return the all classification tags for the entity
     */
    public List<String> getAllClassificationTagsForUriByTaskId(EntityId entityId,String taskId);

    /**
     * Adds the entity to the list of entities linked to the classification Tag with tag name tagName.
     *
     * @param entityId the entity id
     * @param tagName the tag name
     */
    public void addClassificationTagToEntity(EntityId entityId, String tagName);

    /**
     * Adds the entity to the list of entities linked to the classification Tag with tag name tagName.
     *
     * @param entityId the entity id
     * @param tagName the tag name
     * @param taskId taskId
     */
    public void addClassificationTagToEntityWithTaskId(EntityId entityId, String tagName, String taskId);

    /**
     * Gets the all classification tags.
     *
     * @return the all classification tags
     */
    public List<String> getAllClassificationTags();

    /**
     * Gets the all entity uris for classification tag.
     *
     * @param tagName the tag name
     * @return the all entity uris for classification tag
     */
    public Set<String> getAllEntityUrisForClassificationTag(String tagName);

    /**
     * Removes the entity with the entity ID entityId from the list of entities linked to the classification Tag with tag name tagName.
     *
     * @param entityId the entity id
     * @param tagName the tag name
     */
    public void removeClassificationTagForEntityUri(EntityId entityId, String tagName);

    /**
     * Gets the Classification tag entity by name.
     *
     * @param tagName the tag name
     * @return the tag entity by name
     */
    public ClassificationTag getTagEntityByName(String tagName);

    /**
     * Completes the tagName on the basis of value specied and it returns only the tags that are not already linked to the given entity.
     *
     * @param entityId the entity id
     * @param value the value
     * @return the list
     */
    public List<Map<String, String>> autocompleteTagName(EntityId entityId, String value);

    /**
     * Gets the list of classification tags entity.
     *
     * @return the all classification tag entity
     */
    public List<ClassificationTag> fetchAllClassificationTags();

    /**
     * Gets the list of all entities associated with the tag name
     * @param tagName 
     * @return the list of entities associated with the tag name.
     */
    public <T extends BaseEntity> List<T> fetchAllTagsAssocEntities(String tagName);

    /**
     * Gets the list of all entities associated with the tag name
     * @param tagName
     * @return the list of entities with taskIds associated with the tag name.
     */
    public <T extends BaseEntity> List<EntriesAndTaskTagVo> fetchAllTagsAssocEntitiesTaskIds(String tagName);

    /**
     * Gets the list of all common entities associated with the list of tag name
     * @param tagName list
     * @return the list of common entities associated with the list of tag name.
     */
    public <T extends BaseEntity> List<T> getAllEntitiesForTagList(List<String> tagName);

    /**
     * Gets the list of all common entities associated with the list of tag name
     * @param tagName list
     * @return the list of common entities associated with the list of tag name.
     */
    public <T extends BaseEntity> List<EntriesAndTaskTagVo> getAllEntitiesWithTaskIdsForTagList(List<String> tagName);
    
    public String getExcludedSpecialCharactersInTag();

}
