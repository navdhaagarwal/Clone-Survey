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

/**
 * @author Nucleus Software Exports Limited
 * 
 */
public class ThreadLocalBatchContextHolderStrategy implements BatchContextHolderStrategy {
    private static final ThreadLocal<BatchProcessContext> contextHolder = new ThreadLocal<BatchProcessContext>();

    @Override
    public void clearContext() {
        contextHolder.remove();
    }

    @Override
    public BatchProcessContext getContext() {
        BatchProcessContext ctx = contextHolder.get();

        if (ctx == null) {
            ctx = createEmptyContext();
            contextHolder.set(ctx);
        }

        return ctx;

    }

    @Override
    public void setContext(BatchProcessContext context) {
        if (context == null)
            throw new IllegalArgumentException("Only non-null BatchProcessContext instances are permitted");
        contextHolder.set(context);
    }

    @Override
    public BatchProcessContext createEmptyContext() {
        return new BatchProcessContextImpl();
    }

}
