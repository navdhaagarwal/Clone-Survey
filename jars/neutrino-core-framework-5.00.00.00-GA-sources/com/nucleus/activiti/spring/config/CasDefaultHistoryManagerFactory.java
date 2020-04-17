package com.nucleus.activiti.spring.config;

import org.activiti.engine.impl.history.HistoryManager;
import org.activiti.engine.impl.interceptor.Session;
import org.activiti.engine.impl.interceptor.SessionFactory;

public class CasDefaultHistoryManagerFactory implements SessionFactory {

        public Class<?> getSessionType() {
            return HistoryManager.class;
        }

        public Session openSession() {
            return new CasDefaultHistoryManager();
        }

}
