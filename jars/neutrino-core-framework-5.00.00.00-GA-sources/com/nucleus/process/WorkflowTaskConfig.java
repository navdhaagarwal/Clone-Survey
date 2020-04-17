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

import java.util.List;

/**
 * @author Nucleus Software Exports Limited
 */
public class WorkflowTaskConfig {

    private String                      workflowTaskKey;
    private Long                        expectedTatInMillis;
    private String                      stageName;
    private String                      autoAssignment;
    private WorkflowFormConfig          formConfig;
    private List<AssignmentLevelConfig> assignmentLevelConfig;

    public WorkflowTaskConfig(String workflowTaskKey, Long expectedTatInMillis, Integer priority) {
        this.workflowTaskKey = workflowTaskKey;
        this.expectedTatInMillis = expectedTatInMillis;
    }

    public String getWorkflowTaskKey() {
        return workflowTaskKey;
    }

    public Long getExpectedTatInMillis() {
        return expectedTatInMillis;
    }

    public WorkflowFormConfig getFormConfig() {
        if (formConfig == null) {
            formConfig = new WorkflowFormConfig();
        }
        return formConfig;
    }

    /*
     * Deliberate default method
     */
    void setWorkflowFormConfig(WorkflowFormConfig formConfig) {
        this.formConfig = formConfig;
    }

    /*
     * Deliberate default method
     */
    void setEscalationConfig(List<AssignmentLevelConfig> assignmentLevelConfig) {
        this.assignmentLevelConfig = assignmentLevelConfig;
    }

    public AssignmentLevelConfig getAssignmentLevelConfig(int level) {
        if (assignmentLevelConfig != null) {
            for (AssignmentLevelConfig levelConfig : assignmentLevelConfig) {
                if (levelConfig.getLevel() == level) {
                    return levelConfig;
                }
            }
        }
        return null;
    }

    /**
     * @return the stageName
     */
    public String getStageName() {
        return stageName;
    }

    /**
     * @param stageName the stageName to set
     */
    public void setStageName(String stageName) {
        this.stageName = stageName;
    }

    public String getAutoAssignment() {
        return autoAssignment;
    }

    public void setAutoAssignment(String autoAssignment) {
        this.autoAssignment = autoAssignment;
    }

}