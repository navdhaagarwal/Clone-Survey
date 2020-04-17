package com.nucleus.core.web.conversation;

import com.nucleus.finnone.pro.cache.common.NeutrinoCachePopulator;
import com.nucleus.security.core.session.SessionAttributeStoreCachePopulator;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.Assert;
import org.springframework.web.bind.support.SessionAttributeStore;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

import java.util.Map;

import static com.nucleus.core.web.conversation.ConversationalSessionAttributeStore.CID_FIELD;
import static com.nucleus.core.web.conversation.ConversationalSessionAttributeStore.LOGGER;
import static com.nucleus.security.core.session.SessionAttributeStoreCachePopulator.KEY_SEPARATOR;

/**
 * This implementation of
 *
 * @see org.springframework.web.bind.support.SessionAttributeStore
 * is similar to
 * @see com.nucleus.core.web.conversation.ConversationalSessionAttributeStore
 * <p>
 * And hence supports multi tab - Conversation ID Handling.
 * The following are the only 2 differences between this class and ConversationalSessionAttributeStore:
 * 1. Implementation - STORAGE
 * 2. Doesn't support - keepAliveConversations
 * <p>
 * 1. Explanation:
 * This class stores the @SessionAttributes in cache rather than HttpSession:
 * @see com.nucleus.finnone.pro.cache.common.NeutrinoCachePopulator
 * Specifically it stores in
 * @see com.nucleus.security.core.session.SessionAttributeStoreCachePopulator
 * <p>
 * 2. Explanation:
 * The usage of keepAliveConversations was used earlier for handling Heap Leakage via HttpSession
 * With Cache - Memory was not a huge concern.
 * Also it the usage was wrong, as it removes the oldest ConversationID in case max ConversationID count is reached
 * This removal of oldest ConversationID is not a correct approach - and which may lead to Functional Loss in rarest scenario
 */
public class ConversationalSessionAttributeCacheBasedStore implements SessionAttributeStore, InitializingBean, ApplicationContextAware {

    private ApplicationContext applicationContext;

    private SessionAttributeStoreCachePopulator sessionAttributeStoreCachePopulator;


    public SessionAttributeStoreCachePopulator getSessionAttributeStoreCachePopulator() {
        return sessionAttributeStoreCachePopulator;
    }

    public void setSessionAttributeStoreCachePopulator(SessionAttributeStoreCachePopulator sessionAttributeStoreCachePopulator) {
        this.sessionAttributeStoreCachePopulator = sessionAttributeStoreCachePopulator;
    }

    @Override
    public void storeAttribute(WebRequest request, String attributeName, Object attributeValue) {
        Assert.notNull(request, "WebRequest must not be null");
        Assert.notNull(attributeName, "Attribute name must not be null");
        Assert.notNull(attributeValue, "Attribute value must not be null");

        String conversationId = getConversationId(request);
        store(request, attributeName, attributeValue, conversationId);
    }

    @Override
    public Object retrieveAttribute(WebRequest request, String attributeName) {
        Assert.notNull(request, "WebRequest must not be null");
        Assert.notNull(attributeName, "Attribute name must not be null");

        String conversationId = getConversationId(request);
        return retrieve(request, attributeName, conversationId);
    }

    @Override
    public void cleanupAttribute(WebRequest request, String attributeName) {
        Assert.notNull(request, "WebRequest must not be null");
        Assert.notNull(attributeName, "Attribute name must not be null");

        String conversationId = getConversationId(request);
        cleanUp(request, attributeName, conversationId);
    }

    private void store(WebRequest request, String attributeName, Object attributeValue, String conversationID) {
        conversationID = getConversationIdToUse(request, conversationID, true);
        String key = request.getSessionId() + KEY_SEPARATOR + conversationID + KEY_SEPARATOR + attributeName;
        sessionAttributeStoreCachePopulator.update(NeutrinoCachePopulator.Action.INSERT, key, attributeValue);
    }

    private Object retrieve(WebRequest request, String attributeName, String conversationID) {
        conversationID = getConversationIdToUse(request, conversationID, false);
        if (StringUtils.isBlank(conversationID)) {
            return null;
        }
        String key = request.getSessionId() + KEY_SEPARATOR + conversationID + KEY_SEPARATOR + attributeName;
        return sessionAttributeStoreCachePopulator.get(key);
    }

    private void cleanUp(WebRequest request, String attributeName, String conversationID) {
        conversationID = getConversationIdToUse(request, conversationID, false);
        if (StringUtils.isBlank(conversationID)) {
            return;
        }
        String key = request.getSessionId() + KEY_SEPARATOR + conversationID + KEY_SEPARATOR + attributeName;
        sessionAttributeStoreCachePopulator.update(NeutrinoCachePopulator.Action.DELETE, key);
    }

    /**
     * Helper method to get conversation id to use for storing, retrieving or cleaning an attribute
     * - either chooses the providedConversationID
     * - or the defaultConversationID
     *
     * @param request                                 - Incoming request
     * @param providedConversationID                  - Provided ConversationID which was retrieved using
     * @param generateDefaultConversationIDIfRequired - Whether to check and generate 'defaultConversationId' for the HttpSession
     *                                                - this is generally applicable only while storing an attribute
     * @return - the conversationId (note that this is a request parameter, and
     * only gets there on form submit)
     * @see ConversationalSessionAttributeCacheBasedStore#getConversationId(WebRequest)
     */
    private String getConversationIdToUse(WebRequest request, String providedConversationID, Boolean generateDefaultConversationIDIfRequired) {
        String sessionId = request.getSessionId();
        String conversationID = null;

        if (generateDefaultConversationIDIfRequired) {
            conversationID = (String) sessionAttributeStoreCachePopulator.get(sessionId + "-defaultConversationId");
            if (StringUtils.isBlank(conversationID)) {
                LOGGER.info(
                        "No initial conversational id found in Session Store.");
                conversationID = StringUtils.isBlank(providedConversationID) ? RandomStringUtils.randomAlphanumeric(5) : providedConversationID;
                sessionAttributeStoreCachePopulator.update(NeutrinoCachePopulator.Action.INSERT, sessionId + "-defaultConversationId", conversationID);
                LOGGER.info(
                        "Setting initial conversational id as [{}]", conversationID);
            }
        }
        if (StringUtils.isNotBlank(providedConversationID)) {
            conversationID = providedConversationID;
        } else if (conversationID == null) {
            conversationID = (String) sessionAttributeStoreCachePopulator.get(sessionId + "-defaultConversationId");
        }

        return conversationID;
    }

    /**
     * Helper method to get conversation id from the web request
     *
     * @param request - Incoming request
     * @return - the conversationId (note that this is a request parameter, and
     * only gets there on form submit)
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
            LOGGER.info("Adding ConversationalSessionAttributeCacheBasedStore to RequestMappingHandlerAdapter.Now it will be used instead of spring's  DefaultSessionAttributeStore");
            adapter.setSessionAttributeStore(this);
        }

    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {

        this.applicationContext = applicationContext;
    }


}
