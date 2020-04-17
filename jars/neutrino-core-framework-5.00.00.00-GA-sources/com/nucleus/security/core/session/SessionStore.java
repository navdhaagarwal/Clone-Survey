package com.nucleus.security.core.session;

import com.nucleus.core.exceptions.OperationNotSupportedException;
import com.nucleus.pubsub.PubSubListener;
import com.nucleus.pubsub.PubSubService;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

public class SessionStore extends ConcurrentHashMap<String, NeutrinoMapSession> {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private static final String ERROR_MSG = "Operation not supported for SessionStore";

    protected static final String SESSION_CACHE_NAME = "SESSION_CACHE";

    protected final String serverNodeId;
    protected final Set<String> ignoreSet = Collections.newSetFromMap(new ConcurrentHashMap<>());
    protected final Map<String, Integer> sessionIdToListenerIdMap = new ConcurrentHashMap<>();
    protected PubSubService pubSubService;
    protected PubSubListener attributeTopicListener;

    public SessionStore(String serverNodeId, PubSubService pubSubService, PubSubListener attributeTopicListener) {
        super();
        this.serverNodeId = serverNodeId;
        this.pubSubService = pubSubService;
        this.attributeTopicListener = attributeTopicListener;
    }

    protected NeutrinoMapSession getFromStore(Object sessionId) {
        return null;
    }

    @Override
    public NeutrinoMapSession get(Object sessionId) {
        NeutrinoMapSession session = super.get(sessionId);
        return (session != null ? session : getFromStoreWithLocalPut((String) sessionId));
    }

    protected NeutrinoMapSession getFromLocal(Object sessionId) {
        return super.get(sessionId);
    }

    private NeutrinoMapSession getFromStoreWithLocalPut(String sessionId) {
        NeutrinoMapSession session = getFromStore(sessionId);
        if (session != null) {
            put(sessionId, session);
        }
        return session;
    }

    @Override
    public NeutrinoMapSession remove(Object arg0) {
        NeutrinoMapSession oldSession = super.remove(arg0);
        return oldSession;
    }

    @Override
    public NeutrinoMapSession put(String sessionId, NeutrinoMapSession session) {
        NeutrinoMapSession oldSession = super.put(sessionId, session);
        return oldSession;
    }

    @Override
    public void clear() {
        throw new OperationNotSupportedException(ERROR_MSG);
    }

    @Override
    public NeutrinoMapSession compute(String arg0, BiFunction<? super String, ? super NeutrinoMapSession, ? extends NeutrinoMapSession> arg1) {
        throw new OperationNotSupportedException(ERROR_MSG);
    }

    @Override
    public NeutrinoMapSession computeIfAbsent(String arg0, Function<? super String, ? extends NeutrinoMapSession> arg1) {
        throw new OperationNotSupportedException(ERROR_MSG);
    }

    @Override
    public NeutrinoMapSession computeIfPresent(String arg0, BiFunction<? super String, ? super NeutrinoMapSession, ? extends NeutrinoMapSession> arg1) {
        throw new OperationNotSupportedException(ERROR_MSG);
    }

    @Override
    public boolean containsKey(Object arg0) {
        throw new OperationNotSupportedException(ERROR_MSG);
    }

    public boolean containsKeyInLocal(Object arg0) {
        return super.containsKey(arg0);
    }

    @Override
    public boolean containsValue(Object arg0) {
        throw new OperationNotSupportedException(ERROR_MSG);
    }

    @Override
    public Set<Entry<String, NeutrinoMapSession>> entrySet() {
        return super.entrySet();
    }

    @Override
    public void forEach(BiConsumer<? super String, ? super NeutrinoMapSession> arg0) {
        throw new OperationNotSupportedException(ERROR_MSG);
    }

    @Override
    public NeutrinoMapSession getOrDefault(Object arg0, NeutrinoMapSession arg1) {
        throw new OperationNotSupportedException(ERROR_MSG);
    }

    @Override
    public boolean isEmpty() {
        throw new OperationNotSupportedException(ERROR_MSG);
    }

    @Override
    public KeySetView<String, NeutrinoMapSession> keySet() {
        throw new OperationNotSupportedException(ERROR_MSG);
    }

    @Override
    public NeutrinoMapSession merge(String arg0, NeutrinoMapSession arg1,
                                    BiFunction<? super NeutrinoMapSession, ? super NeutrinoMapSession, ? extends NeutrinoMapSession> arg2) {
        throw new OperationNotSupportedException(ERROR_MSG);
    }

    @Override
    public void putAll(Map<? extends String, ? extends NeutrinoMapSession> arg0) {
        throw new OperationNotSupportedException(ERROR_MSG);
    }

    @Override
    public NeutrinoMapSession putIfAbsent(String arg0, NeutrinoMapSession arg1) {
        throw new OperationNotSupportedException(ERROR_MSG);
    }

    @Override
    public boolean remove(Object arg0, Object arg1) {
        throw new OperationNotSupportedException(ERROR_MSG);
    }

    @Override
    public NeutrinoMapSession replace(String arg0, NeutrinoMapSession arg1) {
        throw new OperationNotSupportedException(ERROR_MSG);
    }

    @Override
    public boolean replace(String arg0, NeutrinoMapSession arg1, NeutrinoMapSession arg2) {
        throw new OperationNotSupportedException(ERROR_MSG);
    }

    @Override
    public void replaceAll(BiFunction<? super String, ? super NeutrinoMapSession, ? extends NeutrinoMapSession> arg0) {
        throw new OperationNotSupportedException(ERROR_MSG);
    }

    @Override
    public int size() {
        throw new OperationNotSupportedException(ERROR_MSG);
    }

    @Override
    public Collection<NeutrinoMapSession> values() {
        throw new OperationNotSupportedException(ERROR_MSG);
    }

    protected boolean isSessionExpiredInRemoteStore(String sessionId){
        return true;
    }

    protected void putInIgnoreSet(String sessionId){
        this.ignoreSet.add(sessionId);
    }

    protected void removeFromIgnoreSet(String sessionId){
        this.ignoreSet.remove(sessionId);
    }

    protected void registerAttributeListenerId(String sessionId, int listenerId) {
        sessionIdToListenerIdMap.put(sessionId, listenerId);
    }

    protected int getAndDeRegisterAttributeListenerId(String sessionId) {
        return sessionIdToListenerIdMap.remove(sessionId);
    }


}
