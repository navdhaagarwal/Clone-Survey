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
package com.nucleus.event;

/**
 * FormatType is used to specify which message(by specifying key) should be read
 * from localized property file to represent given event type.
 * For Example you may need different statements for same events to use on different
 * display screens(one for notification,for activity panel etc.).
 *
 * @author Nucleus Software Exports Limited
 *
 */
public enum FormatType {

    NOTIFICATION {
        @Override
        public String getKey(int eventType) {
            return "notification" + String.format(MESSAGE_KEY_SUFFIX, String.valueOf(eventType));
        }
    },
    USER_PROFILE {
        @Override
        public String getKey(int eventType) {
            return "generic" + String.format(MESSAGE_KEY_SUFFIX, String.valueOf(eventType));
        }
    },
    ACTIVITY_STREAM {
        @Override
        public String getKey(int eventType) {
            return "generic" + String.format(MESSAGE_KEY_SUFFIX, String.valueOf(eventType));
        }
    },
    EMAIL_SUBJECT {
        @Override
        public String getKey(int eventType) {
            return "mail" + String.format(SUBJECT_KEY_SUFFIX, String.valueOf(eventType));
        }
    },
    EMAIL_BODY {
        @Override
        public String getKey(int eventType) {
            return "mail" + String.format(BODY_KEY_SUFFIX, String.valueOf(eventType));
        }
    },

    EMAIL_INTERNAL_FROM {
        @Override
        public String getKey(int eventType) {
            return "mail" + String.format(IN_FROM_KEY_SUFFIX, String.valueOf("all"));
        }
    },

    EMAIL_SMTP_FROM {
        @Override
        public String getKey(int eventType) {
            return "mail" + String.format(SMTP_FROM_KEY_SUFFIX, String.valueOf("all"));
        }
    },

    SMS {
        @Override
        public String getKey(int eventType) {
            return "sms" + String.format(MESSAGE_KEY_SUFFIX, String.valueOf(eventType));
        }
    };

    private static final String MESSAGE_KEY_SUFFIX   = ".event.type.%s.message";
    private static final String SUBJECT_KEY_SUFFIX   = ".event.type.%s.subject";
    private static final String BODY_KEY_SUFFIX      = ".event.type.%s.body";
    private static final String IN_FROM_KEY_SUFFIX   = ".event.type.%s.internal.from";
    private static final String SMTP_FROM_KEY_SUFFIX = ".event.type.%s.smtp.from";

    public abstract String getKey(int eventType);

}
