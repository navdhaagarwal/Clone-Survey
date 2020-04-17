package com.nucleus.core.web.conversation;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.Assert;
import org.springframework.web.bind.support.SessionAttributeStore;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

import com.nucleus.logging.BaseLoggers;

/**
 * This class handles how session scoped model attributes are stored and
 * retrieved from the HttpSession. This implementation uses a random 
 * conversational id to distinguish multiple command objects of the same type.
 * This is needed for users editing the same/different entity(in session) 
 * on multiple tabs of a browser.
 * 
 * Source:
 * http://forum.springsource.org/showthread.php?95016-Using-Session-Model
 * -Attributes-With-Multiple-Browser-Tabs-Patch
 * 
 * This is a modification of the code in the URL.This implementation creates a
 * conversation map on the session - to allow each conversation to have
 * its own store without adding the cid on the object name. In this way
 * - the cid and the session attributes are kept separated.
 * 
 * Currently a new conversation id is created only in these cases:
 * 1.first url after successful login.
 * 2.at login page(to use on IC)
 * 3.when a link is opened in new tab.
 * 4.When no id is provided in request and there is no store.[fall-back for single tab]  
 * 
 */

public class ConversationalSessionAttributeStore implements SessionAttributeStore, InitializingBean, ApplicationContextAware {

    private ApplicationContext applicationContext;

    public static final Logger LOGGER                 = BaseLoggers.conversationalLogger;

    private int                keepAliveConversations = 20;
    private boolean            deleteStoreIfEmpty     = true;

    public final static String CID_FIELD              = "_cid";
    public final static String SESSION_MAP            = "sessionConversationMap";

    @Override
    public void storeAttribute(WebRequest request, String attributeName, Object attributeValue) {
        Assert.notNull(request, "WebRequest must not be null");
        Assert.notNull(attributeName, "Attribute name must not be null");
        Assert.notNull(attributeValue, "Attribute value must not be null");

        String conversationId = getConversationId(request);
        if (StringUtils.isBlank(conversationId)) {
            storeWithFallback(request, attributeName, attributeValue);
        } else {
            LOGGER.debug("Stored [{}] with conversational id [{}].", attributeName, conversationId);
            store(request, attributeName, attributeValue, conversationId);
        }
    }

    @Override
    public Object retrieveAttribute(WebRequest request, String attributeName) {
        Assert.notNull(request, "WebRequest must not be null");
        Assert.notNull(attributeName, "Attribute name must not be null");

        String conversationId = getConversationId(request);

        if (StringUtils.isBlank(conversationId)) {
            return retrieveAttributeWithFallback(request, attributeName);
        } else {
            LOGGER.debug("Retrieving attribute {} with conversational id {}.", attributeName, conversationId);
            return getConversationStore(request, conversationId).get(attributeName);
        }
    }

    @Override
    public void cleanupAttribute(WebRequest request, String attributeName) {
        Assert.notNull(request, "WebRequest must not be null");
        Assert.notNull(attributeName, "Attribute name must not be null");

        String conversationId = getConversationId(request);
        Map<String, Object> conversationStore = null;

        if (StringUtils.isBlank(conversationId)) {
            LOGGER.warn(
                    "No conversational id found in request for cleaning session  attribute [{}].Trying to use fallback Conversation Store.",
                    attributeName);

            // first get SessionConversationsMap (root map)
            Map<String, Map<String, Object>> sessionConversationsMap = getSessionConversationsMap(request);
            if (sessionConversationsMap.size() == 1) {
                sessionConversationsMap.values().iterator().next().remove(attributeName);
            } else if (sessionConversationsMap.size() > 1) {
                LOGGER.warn("No fallback Conversation Store found to clean attribute ["
                        + attributeName
                        + "].Conversation Id is not provided and there is more than one store in session.So no cleanup performed.");
            }

        } else {
            conversationStore = getConversationStore(request, conversationId);
            LOGGER.debug("Cleaning attribute {} with conversational id {}.", attributeName, conversationId);
            conversationStore.remove(attributeName);
        }

        // Delete the conversation store from the session if empty
        if (deleteStoreIfEmpty && conversationStore != null && conversationStore.isEmpty() && conversationId != null) {
            LOGGER.info(
                    "Removing conversationStore with conversational id [{}] as it is empty.Store will be re-created for same conversationId if required.",
                    conversationId);
            getSessionConversationsMap(request).remove(conversationId);
        }
    }

    private void storeWithFallback(WebRequest request, String attributeName, Object attributeValue) {

        LOGGER.warn(
                "No conversational id found in request for storing session attribute [{}].Trying to use fallback Conversation Store",
                attributeName);
        // first get SessionConversationsMap (root map)
        Map<String, Map<String, Object>> sessionConversationsMap = getSessionConversationsMap(request);
        if (sessionConversationsMap.size() == 1) {
            LOGGER.info(
                    "Found exactly one conversation store.so using it to store attribute [{}] as conversational id not found in request",
                    attributeName);
            Map<String, Object> defaultSingleMap = sessionConversationsMap.values().iterator().next();
            defaultSingleMap.put(attributeName, attributeValue);
        } else if (sessionConversationsMap.isEmpty()) {
            // only in this case we can create new map (conversational id=null and there is no existing map)
            String conversationID = RandomStringUtils.randomAlphanumeric(15);
            Map<String, Object> defaultSingleMap = getConversationStore(request, conversationID);
            defaultSingleMap.put(attributeName, attributeValue);
            // now propagate this id
            request.setAttribute(CID_FIELD, conversationID, WebRequest.SCOPE_REQUEST);
            LOGGER.info(
                    "Found no conversation store.so creating new with conversation id [{}] to store attribute [{}].Conversational id was not found in request",
                    conversationID, attributeName);
        } else {
        	String initialConversationalId= (String) request.getAttribute("initialConversationalId", WebRequest.SCOPE_SESSION);
        	Map<String, Object>  firstConversationaMap = sessionConversationsMap.get(initialConversationalId);
        	if(firstConversationaMap!=null) {
        		 firstConversationaMap.put(attributeName, attributeValue);
            }else{
            	 throw new ConversationalSessionException("No fallback Conversation Store found to store attribute [" + attributeName
                         + "].Conversation Id is not provided and there is more than one store in session.");
            }      	
        }
    }

    private Object retrieveAttributeWithFallback(WebRequest request, String attributeName) {

        LOGGER.warn(
                "No conversational id found in request to retrieve attribute [{}].Trying to use fallback Conversation Store",
                attributeName);
        // first get SessionConversationsMap (root map)
        Map<String, Map<String, Object>> sessionConversationsMap = getSessionConversationsMap(request);
        if (sessionConversationsMap.size() == 1) {
            LOGGER.info(
                    "Found exactly one conversation store.so using it to retrieve attribute [{}] as conversational id not found in request.",
                    attributeName);
            Map<String, Object> defaultSingleMap = sessionConversationsMap.values().iterator().next();
            return defaultSingleMap.get(attributeName);
        } else if (sessionConversationsMap.isEmpty()) {
            // this is same as what happens when we try to retrieve attribute from session which was not stored earlier
            LOGGER.warn(
                    "Found no conversation store to retrieve attribute [{}].so returning null as conversational id not found in request.",
                    attributeName);
            return null;

        } else {
        	String initialConversationalId= (String) request.getAttribute("initialConversationalId", WebRequest.SCOPE_SESSION);
        	Map<String, Object>  firstConversationaMap = sessionConversationsMap.get(initialConversationalId);
        	if(firstConversationaMap!=null) {
        		 return firstConversationaMap.get(attributeName);
            }else{
            	throw new ConversationalSessionException("No fallback Conversation Store found to retrieve attribute [" + attributeName
                        + "].Conversation Id is not provided and there is more than one store in session.");
            }      	
        }
    }

    /**
     * Retrieve a specific conversation's map of objects from the session. Will
     * create the conversation map if it does not exist.
     * 
     * The conversation map is stored inside a session map - which is a map of
     * maps. If this does not exist yet- it will be created too.
     * 
     * @param request
     *            - the incoming request
     * @param conversationId
     *            - the conversation id we are dealing with
     * @return - the conversation's map
     */
    private Map<String, Object> getConversationStore(WebRequest request, String conversationId) {

        // get map for a specific conversation with given conversationId
        Map<String, Object> conversationMap = getSessionConversationsMap(request).get(conversationId);
        if (conversationId != null && conversationMap == null) {
            conversationMap = new HashMap<String, Object>();
            getSessionConversationsMap(request).put(conversationId, conversationMap);
        }
        return conversationMap;
    }

    /**
     * Get the session's conversations map.It will return a single map having all conversations.
     * 
     * @param request
     *            - the request
     * @return - LinkedHashMap of all the conversations and their maps
     */
    private LinkedHashMap<String, Map<String, Object>> getSessionConversationsMap(WebRequest request) {
        @SuppressWarnings("unchecked")
        LinkedHashMap<String, Map<String, Object>> sessionMap = (LinkedHashMap<String, Map<String, Object>>) request
                .getAttribute(SESSION_MAP, WebRequest.SCOPE_SESSION);
        if (sessionMap == null) {
            sessionMap = new LinkedHashMap<String, Map<String, Object>>();
            request.setAttribute(SESSION_MAP, sessionMap, WebRequest.SCOPE_SESSION);
        }
        return sessionMap;
    }

    /**
     * Store an object on the session. If the configured maximum number of live
     * conversations to keep is reached - clear out the oldest conversation. (If
     * max number is configured as 0 - no removal will happen)
     * 
     * @param request
     *            - the web request
     * @param attributeName
     *            - the name of the attribute (from @SessionAttributes)
     * @param attributeValue
     *            - the value to store
     */
    private void store(WebRequest request, String attributeName, Object attributeValue, String cId) {
        LinkedHashMap<String, Map<String, Object>> sessionConversationsMap = getSessionConversationsMap(request);
        if (keepAliveConversations > 0 && sessionConversationsMap.size() >= keepAliveConversations
                && !sessionConversationsMap.containsKey(cId)) {
            // clear oldest conversation
            String key = sessionConversationsMap.keySet().iterator().next();
            sessionConversationsMap.remove(key);
            LOGGER.error(
                    "Reached maximum number ({}) of live conversations.Removed data for conversational id {} from session",
                    keepAliveConversations, getKeepAliveConversations(), key);
        }
        getConversationStore(request, cId).put(attributeName, attributeValue);

    }

    public int getKeepAliveConversations() {
        return keepAliveConversations;
    }

    public void setKeepAliveConversations(int numConversationsToKeep) {
        keepAliveConversations = numConversationsToKeep;
    }

    public boolean isDeleteStoreIfEmpty() {
        return deleteStoreIfEmpty;
    }

    public void setDeleteStoreIfEmpty(boolean deleteStoreIfEmpty) {
        this.deleteStoreIfEmpty = deleteStoreIfEmpty;
    }

    /**
     * Helper method to get conversation id from the web request
     * 
     * @param request
     *            - Incoming request
     * @return - the conversationId (note that this is a request parameter, and
     *         only gets there on form submit)
     */
    private String getConversationId(WebRequest request) {
        String cid = request.getParameter(CID_FIELD);

        if (StringUtils.isBlank(cid)) {
            cid = (String) request.getHeader(CID_FIELD);
        }
        if (StringUtils.isBlank(cid)) {
            cid = (String) request.getAttribute(CID_FIELD, WebRequest.SCOPE_REQUEST);
        }
        return cid;
    }

    /**
     * Required for wiring the RequestMappingHandlerAdapter
     */
    @Override
    public void afterPropertiesSet() throws Exception {

        Map<String, RequestMappingHandlerAdapter> beans = applicationContext
                .getBeansOfType(RequestMappingHandlerAdapter.class);

        for (RequestMappingHandlerAdapter adapter : beans.values()) {
            LOGGER.info("Adding ConversationalSessionAttributeStore to RequestMappingHandlerAdapter.Now it will be used instead of spring's  DefaultSessionAttributeStore");
            adapter.setSessionAttributeStore(this);
        }

    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {

        this.applicationContext = applicationContext;
    }

}
