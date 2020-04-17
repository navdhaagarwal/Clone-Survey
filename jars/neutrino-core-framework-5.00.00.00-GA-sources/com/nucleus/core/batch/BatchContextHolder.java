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
package com.nucleus.core.batch;

import java.lang.reflect.Constructor;

import org.springframework.util.ReflectionUtils;

/**
 * Associates a given {@link BatchProcessContext} with the current execution thread.
 * 
 * @author Nucleus Software Exports Limited
 * 
 */
public class BatchContextHolder {

    public static final String                MODE_THREADLOCAL            = "MODE_THREADLOCAL";
    public static final String                MODE_INHERITABLETHREADLOCAL = "MODE_INHERITABLETHREADLOCAL";
    public static final String                MODE_GLOBAL                 = "MODE_GLOBAL";
    public static final String                SYSTEM_PROPERTY             = "neutrino.batch.strategy";
    private static String                     strategyName                = System.getProperty(SYSTEM_PROPERTY);
    private static BatchContextHolderStrategy strategy;
    private static int                        initializeCount             = 0;

    static {
        initialize();
    }

    public static void clearContext() {
        strategy.clearContext();
    }

    public static BatchProcessContext getContext() {
        return strategy.getContext();
    }

    public static int getInitializeCount() {
        return initializeCount;
    }

    private static void initialize() {
        if ((strategyName == null) || "".equals(strategyName)) {
            // Set default
            strategyName = MODE_THREADLOCAL;
        }

        if (strategyName.equals(MODE_THREADLOCAL)) {
            strategy = new ThreadLocalBatchContextHolderStrategy();
        } else if (strategyName.equals(MODE_INHERITABLETHREADLOCAL)) {
            strategy = new InheritableThreadLocalBatchContextHolderStrategy();
        } else {

            try {
                Class<?> clazz = Class.forName(strategyName);
                Constructor<?> customStrategy = clazz.getConstructor();
                strategy = (BatchContextHolderStrategy) customStrategy.newInstance();
            } catch (Exception ex) {
                ReflectionUtils.handleReflectionException(ex);
            }
        }

        initializeCount++;
    }

    public static void setContext(BatchProcessContext context) {
        strategy.setContext(context);
    }

    public static void setStrategyName(String strategyName) {
        BatchContextHolder.strategyName = strategyName;
        initialize();
    }

    public static BatchContextHolderStrategy getContextHolderStrategy() {
        return strategy;
    }

    public static BatchProcessContext createEmptyContext() {
        return strategy.createEmptyContext();
    }

    public String toString() {
        return "BatchContextHolder[strategy='" + strategyName + "'; initializeCount=" + initializeCount + "]";
    }

}
