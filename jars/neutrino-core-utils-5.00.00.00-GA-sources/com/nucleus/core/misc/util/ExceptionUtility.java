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
package com.nucleus.core.misc.util;

import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.text.StrBuilder;

import com.nucleus.core.exceptions.SystemException;

/**
 * @author Nucleus Software Exports Limited
 * ExceptionUtility to wrap unchecked exceptions in runtime exceptions
 */
public class ExceptionUtility {

    private static final String CAUSE_CAPTION = "Caused by: ";
    private static final String DOUBLE_SPACE  = "  ";

    public static void rethrowException(Throwable ex) throws Exception {
        if (ex instanceof Exception) {
            throw (Exception) ex;
        }
        if (ex instanceof Error) {
            throw (Error) ex;
        }
        throw new UndeclaredThrowableException(ex);
    }

    public static void rethrowRuntimeException(Throwable ex) {
        if (ex instanceof RuntimeException) {
            throw (RuntimeException) ex;
        }
        if (ex instanceof Error) {
            throw (Error) ex;
        }
        throw new UndeclaredThrowableException(ex);
    }

    public static void rethrowSystemException(Throwable ex) {
        if (ex instanceof SystemException) {
            throw (SystemException) ex;
        }
        throw new SystemException(ex);
    }

    /**
     * Gets a compact message summarising the exception.
     * <p>
     * The message returned is of the form
     * {ClassNameWithPackage}: {ThrowableMessage} and for nullpointer exception
     * It also prints the stack frame where exception occurred
     *
     * @param th  the throwable to get a message for, null returns empty string
     * @return the message, non-null
     * @since Commons Lang 2.2
     */

    public static String getErrorSummary(Throwable throwable) {
        final List<Throwable> list = new ArrayList<>();
        StrBuilder builder = new StrBuilder();
        // TODO:supressed exceptions??
        while (throwable != null && !list.contains(throwable)) {
            if (builder.isEmpty()) {
                builder.append(throwable.toString());
            } else {
                builder.appendNewLine();
                builder.append(CAUSE_CAPTION);
                builder.appendSeparator(DOUBLE_SPACE);
                builder.append(throwable.toString());
            }
            if (throwable instanceof NullPointerException) {
                StackTraceElement[] elem = throwable.getStackTrace();
                if (elem != null && elem.length > 0) {
                    builder.appendNewLine();
                    builder.append(elem[0]);
                }
            }

            throwable = throwable.getCause();
        }
        return builder.toString();
    }

}
