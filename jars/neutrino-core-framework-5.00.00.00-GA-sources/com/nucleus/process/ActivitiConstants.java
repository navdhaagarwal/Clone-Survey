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
package com.nucleus.process;

/**
 * Constants to represent the JPA compliant persistence provider hints which can be passed to enable
 * specific query behaviours on Queries  
 * @author Nucleus Software Exports Limited
 *
 */
public class ActivitiConstants {

    public static String RIGHT_BRACES_STRING                                 = ")";
    public static String LEFT_BRACES_STRING                                  = "(";
    public static String AND_STRING                                          = "AND ";
    public static String SPACE_STRING                                        = " ";
    public static String APSTROPHE_STRING                                    = "'";
    public static String COUNT_STRING                                        = "SELECT COUNT(DISTINCT RES.ID_) FROM ACT_RU_TASK RES ";
    public static String TASK_OBJECT_STRING                                  = "SELECT DISTINCT RES.* FROM ACT_RU_TASK RES ";
    public static String JOIN_STRING_1                                       = "INNER JOIN ACT_RU_VARIABLE A on RES.PROC_INST_ID_ = A.PROC_INST_ID_ ";
    public static String JOIN_STRING_2                                       = "INNER JOIN ACT_RE_PROCDEF D on RES.PROC_DEF_ID_ = D.ID_ ";
    public static String JOIN_STRING_3                                       = "INNER JOIN ACT_RU_IDENTITYLINK I on I.TASK_ID_ = RES.ID_ ";
    public static String WHERE_STRING                                        = "WHERE ";
    public static String WHERE_PROC_DEF_CONDITION_STRING                     = "D.KEY_ =  ";
    public static String TASK_ASSIGNEE_CONDITION_STRING                      = "RES.ASSIGNEE_ =  ";
    public static String TASK_ASSIGNEE_NULL_CONDITION_STRING                 = "RES.ASSIGNEE_ IS NULL AND I.TYPE_ = 'candidate' AND I.GROUP_ID_ IN (";
    public static String PROC_INST_ID_VARIABLE_IN_STRING                     = "A.PROC_INST_ID_ IN ";
    public static String STAGE_VARIABLE_NOT_IN_QUERY_STRING                  = "WHERE (A.NAME_ = 'stageName' AND A.TEXT_ NOT IN (";
    public static String WORKFLOW_CONFIGURATION_VARIABLE_NOT_IN_QUERY_STRING = "WHERE (A.NAME_ = 'workflowConfigurationType' AND A.TEXT_ NOT IN (";
    public static String ASSIGNED_TEAM_QUERY_STRING                          = "WHERE (A.NAME_ = 'assignedToTeam' AND A.LONG_ =";
    public static String PROC_INST_ID_FROM_VARIABLE                          = "SELECT A.PROC_INST_ID_ FROM ACT_RU_VARIABLE A ";
    public static String STAGE_VARIABLE_IN_QUERY_STRING                      = "WHERE (A.NAME_ = 'stageName' AND A.TEXT_ IN (";
    public static String WORKFLOW_CONFIGURATION_VARIABLE_IN_QUERY_STRING     = "WHERE (A.NAME_ = 'workflowConfigurationType' AND A.TEXT_ IN (";

}
