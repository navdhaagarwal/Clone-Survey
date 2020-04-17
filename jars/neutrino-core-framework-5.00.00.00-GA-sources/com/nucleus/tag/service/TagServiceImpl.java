/**
 * This file and a proportion of its content is copyright of Nucleus Software Exports Limited - © 2012. All rights reserved.
 * Any redistribution or reproduction of part or all of the contents in any form is prohibited other than the following:
 * - you cannot print or download to a local hard disk extract contents either part or full for personal/ commercial/
 * academic or any other use
 * - you may not copy the content to individual/ third parties for any type of use, either as compiled or source format
 * without the knowledge and consent of Nucleus Software
 * - You may not, except with our express written permission, distribute or commercially exploit the content. Nor may you
 * transmit it or store it in any other web site or other form of electronic retrieval system.
 */
package com.nucleus.tag.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.inject.Named;

import com.nucleus.tag.entriesTaskTagVo.EntriesAndTaskTagVo;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Value;

import com.nucleus.core.exceptions.InvalidDataException;
import com.nucleus.core.validation.util.NeutrinoValidator;
import com.nucleus.dao.query.NamedQueryExecutor;
import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.EntityId;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.service.BaseServiceImpl;
import com.nucleus.tag.entity.ClassificationTag;

/**
 * @author Nucleus Software Exports Limited
 * TODO -> manas.grover Add documentation to class
 */
@Named("tagService")
public class TagServiceImpl extends BaseServiceImpl implements TagService {
	
	//Read data from properties file
	@Value(value = "${addTag.extraExcludedCharacters:@}")
	private String extraExludedCharacters;
	
	private Set<String> excludedCharacterSet;
	
	private static final String SPECIAL_CHARACTERS = "@%&'(*)?/\\,\";~";

    @Override
    public List<String> getAllClassificationTagsForUri(EntityId entityId) {
        NeutrinoValidator.notNull(entityId, "EntityId cannot be null to add a comment");
        NeutrinoValidator.notEmpty(entityId.getUri(), "entityUri cannot be empty to add a comment");

        String entityUri = entityId.getUri();
        NamedQueryExecutor<String> namedQueryExecutor = new NamedQueryExecutor<String>("Tag.AllTagsForUri").addParameter(
                "entityUri", entityUri);
        return entityDao.executeQuery(namedQueryExecutor);

    }

    @Override
    public List<String> getAllClassificationTagsForUriByTaskId(EntityId entityId,String taskId) {
        NeutrinoValidator.notNull(entityId, "EntityId cannot be null to add a comment");
        NeutrinoValidator.notEmpty(entityId.getUri(), "entityUri cannot be empty to add a comment");

        String entityUri = entityId.getUri();
        NamedQueryExecutor<String> namedQueryExecutor = new NamedQueryExecutor<String>("Tag.AllTagsForUri").addParameter(
                "entityUri", entityUri+ "," +taskId);
        return entityDao.executeQuery(namedQueryExecutor);

    }

    @Override
    public void addClassificationTagToEntity(EntityId entityId, String tagName) {
        NeutrinoValidator.notNull(entityId, "EntityId cannot be null to add a comment");
        NeutrinoValidator.notNull(tagName, "tagName cannot be null");
        NeutrinoValidator.notEmpty(entityId.getUri(), "entityUri cannot be empty to add a comment");
        NeutrinoValidator.notEmpty(tagName, "tagName cannot be empty");

        //To check restricted special characters
        for (int i=0; i< tagName.length(); i++){
        	if (excludedCharacterSet.contains(String.valueOf(tagName.charAt(i)))){
        		throw new InvalidDataException("tagName can not contain special characters");
        	}
        }
        String entityUri = entityId.getUri();
        ClassificationTag classificationTag = this.getTagEntityByName(tagName);

        Set<String> entityUris;

        if (null != classificationTag) {

            if (null != classificationTag.getEntityUris()) {
                entityUris = classificationTag.getEntityUris();
            } else {
                entityUris = new HashSet<String>();
            }
            entityUris.add(entityUri);
            classificationTag.setEntityUris(entityUris);
            entityDao.update(classificationTag);
        } else {
            classificationTag = new ClassificationTag();
            classificationTag.setTagName(tagName);
            entityUris = new HashSet<String>();
            entityUris.add(entityUri);
            classificationTag.setEntityUris(entityUris);
            entityDao.persist(classificationTag);
        }

    }

    @Override
    public void addClassificationTagToEntityWithTaskId(EntityId entityId, String tagName, String taskId) {
        NeutrinoValidator.notNull(entityId, "EntityId cannot be null to add a comment");
        NeutrinoValidator.notNull(tagName, "tagName cannot be null");
        NeutrinoValidator.notEmpty(entityId.getUri(), "entityUri cannot be empty to add a comment");
        NeutrinoValidator.notEmpty(tagName, "tagName cannot be empty");
        NeutrinoValidator.notEmpty(tagName, "taskId cannot be empty");

        String entityUri = entityId.getUri();
        ClassificationTag classificationTag = this.getTagEntityByName(tagName);

        Set<String> entityUris;

        if (null != classificationTag) {

            if (null != classificationTag.getEntityUris()) {
                entityUris = classificationTag.getEntityUris();
            } else {
                entityUris = new HashSet<String>();
            }
            entityUris.add(entityUri + "," + taskId);
            classificationTag.setEntityUris(entityUris);
            entityDao.update(classificationTag);
        } else {
            classificationTag = new ClassificationTag();
            classificationTag.setTagName(tagName);
            entityUris = new HashSet<String>();
            entityUris.add(entityUri + "," + taskId);
            classificationTag.setEntityUris(entityUris);
            entityDao.persist(classificationTag);
        }

    }

    @Override
    public List<String> getAllClassificationTags() {
        NamedQueryExecutor<String> namedQueryExecutor = new NamedQueryExecutor<String>("Tag.AllTags");

        return entityDao.executeQuery(namedQueryExecutor);
    }

    @Override
    public List<ClassificationTag> fetchAllClassificationTags() {

        return entityDao.findAll(ClassificationTag.class);
    }

    @Override
    public <T extends BaseEntity> List<T> fetchAllTagsAssocEntities(String tagName) {
        NeutrinoValidator.notNull(tagName, "Tag name can't be null while retrieving its associated entities.");
        Set<String> entityUris = getAllEntityUrisForClassificationTag(tagName);
        Iterator<String> itr = entityUris.iterator();
        List<T> entities = new ArrayList<T>();
        String entityUri = null;
        while (itr.hasNext()) {
            entityUri = itr.next();
            try {
				EntityId entityId = EntityId.fromUri(entityUri);
				entities.add((T) entityDao.get(entityId));
			} catch (InvalidDataException e) {
				BaseLoggers.exceptionLogger.error(
						"Avoided adding entity for uri: " + entityUri + " [Tag:" + tagName + "] as this class does not exist in current application", e);
			}
        }
        return entities;
    }

    @Override
    public <T extends BaseEntity> List<EntriesAndTaskTagVo> fetchAllTagsAssocEntitiesTaskIds(String tagName) {
        NeutrinoValidator.notNull(tagName, "Tag name can't be null while retrieving its associated entities.");
        Set<String> entityUris = getAllEntityUrisForClassificationTag(tagName);
        Iterator<String> itr = entityUris.iterator();
        List<EntriesAndTaskTagVo> entriesAndTaskTagVoList = new ArrayList<>();
        EntriesAndTaskTagVo entriesAndTaskTagVo;
        String entityUri, taskId;
        while (itr.hasNext()) {
            entriesAndTaskTagVo = new EntriesAndTaskTagVo();
            entityUri = itr.next();
            try {
                if(entityUri.contains(",")){
                    String[] data = entityUri.split(",",2);
                    entityUri = data[0];
                    taskId = data[1];
                }else{
                    taskId=null;
                }
                EntityId entityId = EntityId.fromUri(entityUri);
                entriesAndTaskTagVo.setEntityUri(entityDao.get(entityId));
                entriesAndTaskTagVo.setTaskId(taskId);
                entriesAndTaskTagVoList.add(entriesAndTaskTagVo);
            } catch (InvalidDataException e) {
                BaseLoggers.exceptionLogger.error(
                        "Avoided adding entity for uri: " + entityUri + " [Tag:" + tagName + "] as this class does not exist in current application", e);
            }
        }
        return entriesAndTaskTagVoList;
    }

    @Override
    public <T extends BaseEntity> List<T> getAllEntitiesForTagList(List<String> tagName) {
        NeutrinoValidator.notNull(tagName, "Tag name can't be null while retrieving its associated entities.");
        NamedQueryExecutor<ClassificationTag> namedQueryExecutor = new NamedQueryExecutor<ClassificationTag>(
                "Tag.TagByTagNameList").addParameter("tagName", tagName);
        List<ClassificationTag> classificationTagList = entityDao.executeQuery(namedQueryExecutor);
        Set<String> entityUris = retrieveCommonEntityUris(classificationTagList);
        if (entityUris != null && entityUris.size() > 0) {
            Iterator<String> itr = entityUris.iterator();
            List<T> entities = new ArrayList<T>();
            String entityUri = null;
            while (itr.hasNext()) {
                entityUri = itr.next();
                try {
    				EntityId entityId = EntityId.fromUri(entityUri);
    				entities.add((T) entityDao.get(entityId));
    			} catch (InvalidDataException e) {
    				BaseLoggers.exceptionLogger.error(
    						"Avoided adding entity for uri: " + entityUri + " as this class does not exist in current application", e);
    			}
            }
            return entities;
        }
        return null;
    }

    @Override
    public <T extends BaseEntity> List<EntriesAndTaskTagVo> getAllEntitiesWithTaskIdsForTagList(List<String> tagName) {
        NeutrinoValidator.notNull(tagName, "Tag name can't be null while retrieving its associated entities.");
        NamedQueryExecutor<ClassificationTag> namedQueryExecutor = new NamedQueryExecutor<ClassificationTag>(
                "Tag.TagByTagNameList").addParameter("tagName", tagName);
        List<ClassificationTag> classificationTagList = entityDao.executeQuery(namedQueryExecutor);
        Set<String> entityUris = retrieveCommonEntityUris(classificationTagList);
        if (entityUris != null && entityUris.size() > 0) {
            Iterator<String> itr = entityUris.iterator();
            List<EntriesAndTaskTagVo> entriesAndTaskTagVoList = new ArrayList<>();
            EntriesAndTaskTagVo entriesAndTaskTagVo;
            String entityUri, taskId;
            while (itr.hasNext()) {
                entityUri = itr.next();
                entriesAndTaskTagVo = new EntriesAndTaskTagVo();
                try {
                    if(entityUri.contains(",")){
                        String[] data = entityUri.split(",",2);
                        entityUri = data[0];
                        taskId = data[1];
                    }else{
                        taskId=null;
                    }
                    EntityId entityId = EntityId.fromUri(entityUri);
                    entriesAndTaskTagVo.setEntityUri(entityDao.get(entityId));
                    entriesAndTaskTagVo.setTaskId(taskId);
                    entriesAndTaskTagVoList.add(entriesAndTaskTagVo);
                } catch (InvalidDataException e) {
                    BaseLoggers.exceptionLogger.error(
                            "Avoided adding entity for uri: " + entityUri + " as this class does not exist in current application", e);
                }
            }
            return entriesAndTaskTagVoList;
        }
        return null;
    }

    private Set<String> retrieveCommonEntityUris(List<ClassificationTag> classificationTagList) {
        Set<String> entityUris = new HashSet<String>();
        if (classificationTagList == null || (classificationTagList != null && classificationTagList.size() == 0)) {
            return null;
        }
        if (classificationTagList.size() == 1) {
            entityUris.addAll(classificationTagList.get(0).getEntityUris());
        } else {
            boolean flag = true;
            for (ClassificationTag classificationTag : classificationTagList) {
                if (flag) {
                    entityUris.addAll(classificationTag.getEntityUris());
                    flag = false;
                } else {
                    Set<String> tempString = new HashSet<String>();
                    tempString.addAll(classificationTag.getEntityUris());
                    List<String> intersectingEntityUri = (List<String>) CollectionUtils.intersection(entityUris, tempString);
                    if (intersectingEntityUri != null && intersectingEntityUri.size() > 0) {
                        entityUris.clear();
                        entityUris.addAll(new HashSet<String>(intersectingEntityUri));
                    } else {
                        entityUris.clear();
                        return null;
                    }
                }
            }
        }
        return entityUris;
    }

    @Override
    public Set<String> getAllEntityUrisForClassificationTag(String tagName) {
        NeutrinoValidator.notNull(tagName, "tagName cannot be null");
        NeutrinoValidator.notEmpty(tagName, "tagName cannot be empty");

        ClassificationTag classificationTag = this.getTagEntityByName(tagName);
        if (null != classificationTag)
            return classificationTag.getEntityUris();
        else
            return null;
    }

    @Override
    public void removeClassificationTagForEntityUri(EntityId entityId, String tagName) {
        NeutrinoValidator.notNull(entityId, "EntityId cannot be null to add a comment");
        NeutrinoValidator.notNull(tagName, "tagName cannot be null");
        NeutrinoValidator.notEmpty(entityId.getUri(), "entityUri cannot be empty to add a comment");
        NeutrinoValidator.notEmpty(tagName, "tagName cannot be empty");

        String removeEntityUri = entityId.getUri();
        ClassificationTag classificationTag = this.getTagEntityByName(tagName);
        if (null != classificationTag) {
            Set<String> entityUris = classificationTag.getEntityUris();
            if (null != classificationTag.getEntityUris()) {
                Set<String> newEntityUris = new HashSet<>();
                Iterator<String> itr = entityUris.iterator();
                String entityUri;
                while (itr.hasNext()) {
                    entityUri = itr.next();
                    String entityUriForNewSet = entityUri;
                    if(entityUri.contains(",")){
                        String[] data = entityUri.split(",",2);
                        entityUri = data[0];
                    }
                    if (!entityUri.contains(removeEntityUri)) {
                        newEntityUris.add(entityUriForNewSet);
                    }
                }
                classificationTag.setEntityUris(newEntityUris);
                entityDao.update(classificationTag);
            }
        }

    }

    @Override
    public ClassificationTag getTagEntityByName(String tagName) {
        NeutrinoValidator.notNull(tagName, "tagName cannot be null");
        NeutrinoValidator.notEmpty(tagName, "tagName cannot be empty");

        NamedQueryExecutor<ClassificationTag> namedQueryExecutor = new NamedQueryExecutor<ClassificationTag>("Tag.TagByName")
                .addParameter("tagName", tagName);
        List<ClassificationTag> classificationTag = entityDao.executeQuery(namedQueryExecutor);
        if (null != classificationTag && classificationTag.size() > 0)
            return classificationTag.get(0);
        else
            return null;

    }

    @Override
    public List<Map<String, String>> autocompleteTagName(EntityId entityId, String value) {
        NeutrinoValidator.notNull(entityId, "EntityId cannot be null to add a comment");
        NeutrinoValidator.notNull(value, "value cannot be null");
        NeutrinoValidator.notEmpty(entityId.getUri(), "entityUri cannot be empty to add a comment");
        /*NeutrinoValidator.notEmpty(value, "value cannot be empty");*/

        String entityuri = entityId.getUri();
        NamedQueryExecutor<String> namedQueryExecutor = new NamedQueryExecutor<String>("Tag.AutoComplete").addParameter(
                "entityUri", entityuri).addLikeParameter("tagName", value, true);

        List<String> tagList = entityDao.executeQuery(namedQueryExecutor);
        List<Map<String, String>> result = new ArrayList<Map<String, String>>();
        for (int i = 0 ; i < tagList.size() ; i++) {
            Map<String, String> map = new HashMap<String, String>();
            map.put("tagName", tagList.get(i));
            result.add(map);
        }

        return result;

    }

	@Override
	public String getExcludedSpecialCharactersInTag() {
		if (extraExludedCharacters==null){
        	extraExludedCharacters="";
        }else{
        	extraExludedCharacters = extraExludedCharacters.trim();
        }
        
        return SPECIAL_CHARACTERS + extraExludedCharacters;
        
	}
	
	@PostConstruct
	public void getExcludedCharactersSet(){
		String excludedCharacters = getExcludedSpecialCharactersInTag();
		excludedCharacterSet = new HashSet<>(Arrays.asList(excludedCharacters.split("")));
	}
}
