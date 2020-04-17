/**
 * This file and a proportion of its content is copyright of Nucleus Software Exports Limited - � 2012. All rights reserved.
 */
package com.nucleus.core.messageSource;

import java.util.List;
import java.util.Map;

import com.nucleus.user.User;

/**
 * 
 * @author Nucleus Software Exports Limited
 */
public interface MessageResourceService {

    /**
     * Load all Message resource values
     * @return
     */
    public List<MessageResource> loadAllMessages();

    /**
     * load message resource on the basic of id
     * @param id
     * @return
     */
    public MessageResource getMessageResourceById(Long id);
    
    public MessageResource getMessageResourceByCode(String code);

    /**
     * save or update Message resource
     * @param messageResource
     * @param user
     */
    public void saveMessageResource(MessageResource messageResource, User user);

    /**
     * Update message resource into cached map holding all labels
     * @param messageResource
     */
    public void updateMessageResourceIntoCache(MessageResource messageResource);

    /**
     * return count of all message resource
     * @return
     */
    public Long getCountOfAllMessage();
    public Long getCountOfMessageResourceByKey (String messageKey,String uuid);

    /**
     * Search on Message Resource filed on the basic of searchquery and return the matching object list
     * @param searchQuery
     * @param iDisplayStart
     * @param iDisplayLength
     * @return
     */
    public List<MessageResource> findEntity(Map<String, Object> searchQuery, Integer iDisplayStart, Integer iDisplayLength);

    /**
     * Load local specific data
     * @param localeKey
     * @return
     */
    public List<Map<String, String>> loadAllMessageResourceByLocale(String localeKey);
}
