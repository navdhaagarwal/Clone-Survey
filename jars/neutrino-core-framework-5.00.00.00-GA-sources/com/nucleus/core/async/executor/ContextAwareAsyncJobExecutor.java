package com.nucleus.core.async.executor;

import org.activiti.engine.impl.asyncexecutor.DefaultAsyncJobExecutor;
import org.springframework.beans.factory.SmartInitializingSingleton;

import com.nucleus.logging.BaseLoggers;

/**
 * @author Nucleus Software Exports Limited
 * 
 * A custom implementation of DefaultAsyncJobExecutor where we delay the start/initialization
 * of executor till the spring conetxt is ready.This is done to avoid concurrency issues caused
 * by beans referenced in activti async/timer job expressions.
 * 
 */
public class ContextAwareAsyncJobExecutor extends DefaultAsyncJobExecutor implements SmartInitializingSingleton {

    // to avoid double initialization of async executor
    private volatile boolean asyncExecutorStarted = false;

    @Override
    public void start() {
        // start is called while spring context creation is still in progress
        BaseLoggers.flowLogger.info(
                "start in AsyncJobExecutor is called while spring context creation is still in progress.Delaying the start till context is ready.");
    }

    @Override
    public void afterSingletonsInstantiated() {
        // start executing and aquiring jobs once the spring context is ready
        if (!asyncExecutorStarted && isAutoActivate()) {
            BaseLoggers.flowLogger.info("Starting  AsyncJobExecutor on ContextRefreshedEvent");
            super.start();
            asyncExecutorStarted = true;
            BaseLoggers.flowLogger.info("AsyncJobExecutor started successfully");
        } else {
            BaseLoggers.flowLogger.info("AsyncJobExecutor already started so ignoring the start call");
        }
    }

}
