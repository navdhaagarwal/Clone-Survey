/**
 * This file and a proportion of its content is copyright of Nucleus Software Exports Limited - © 2012. All rights reserved.
 * Any redistribution or reproduction of part or all of the contents in any form is prohibited other than the following:
 * - you cannot print or download to a local hard disk extract contents either part or full for personal/ commercial/
 * academic or any other use
 * - you may not copy the content to individual/ third parties for any type of use, either as compiled or source format
 * without the knowledge and consent of Nucleus SOftware
 * - You may not, except with our express written permission, distribute or commercially exploit the content. Nor may you
 * transmit it or store it in any other web site or other form of electronic retrieval system.
 */
package com.nucleus.process;

/**
 * @author Nucleus Software Exports Limited
 */
public class AssignmentLevelConfig {

    private int     level;

    /*priority, tat and notification threshold are wrapper Integers so that if configuration has no value, they are not defaulted to zero*/
    private Integer priority;
    private Long    tatInMillis;
    private Long    notificationThresholdMillis;

    public AssignmentLevelConfig(int level, Integer priority, Long tatInMillis, Long notificationThresholdMillis) {
        this.level = level;
        this.priority = priority;
        this.tatInMillis = tatInMillis;
        this.notificationThresholdMillis = notificationThresholdMillis;
    }

    public int getLevel() {
        return level;
    }

    public Integer getPriority() {
        return priority;
    }

    public Long getTatInMillis() {
        return tatInMillis;
    }

    public Long getNotificationThresholdMillis() {
        return notificationThresholdMillis;
    }

}
