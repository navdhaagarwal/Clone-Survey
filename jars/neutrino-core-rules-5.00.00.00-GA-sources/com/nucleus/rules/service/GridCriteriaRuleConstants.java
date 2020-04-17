package com.nucleus.rules.service;

public final class GridCriteriaRuleConstants {

    public static final String LEFT_EXPRESSION_MAP = "LEFT_EXPRESSION_MAP";
    public static final String BASE_CONTEXT_OBJECT_NAME_LIST = "BASE_CONTEXT_OBJECT_NAME_LIST";
    public static final String ALLOCATION_ENTITY_MAPPING_LIST = "allocationEntityMappingList";
    public static final String OGNL_FIELD = "OGNL_FIELD";
    public static final String ALIAS_FIELD = "ALIAS_FIELD";
    public static final String INNER_JOIN = " inner join ";
    public static final String TEAM = "Team";
    public static final String USER = "User";
    public static final String DEFAULT_EXPRESSION = " 1 = 1";
    public static final String JOIN_USER_TO_TEAM = "inner join contextObjectAllocationTeam.users contextObjectAllocationUser ";
    public static final String TEAM_FQN = "com.nucleus.core.team.entity.Team";
    public static final String CONTEXT_OBJECT_ALLOCATION_USER = "contextObjectAllocationUser";
    public static final String CONTEXT_OBJECT_ALLOCATION_USER_BRANCH_PRODUCT = "contextObjectAllocationUserBranchProduct";
    public static final String CONTEXT_OBJECT_ALLOCATION_USER_BRANCH = "contextObjectAllocationUserBranch";
    public static final String CONTEXT_OBJECT_ALLOCATION_TEAM = "contextObjectAllocationTeam";
    public static final String CONTEXT_OBJECT_ALLOCATION_USER_BRANCH_SERVED_CITY = "contextObjectAllocationUserBranchServedCity";
    public static final String CONTEXT_OBJECT_ALLOCATION_USER_ROLES = "contextObjectAllocationUserRoles";
    public static final String CONTEXT_OBJECT_ALLOCATION_USER_BRANCH_SERVED_VILLAGES = "contextObjectAllocationUserBranchServedVillages";
    public static final String FROM = " FROM ";
    public static final String SELECT_DISTINCT = "SELECT distinct ";
    public static final String AND = " AND (";
    public static final String STATUS_LIST = "statusList";
    public static final String CONTEXT_OBJECT_ALLOCATION = "contextObjectAllocation";
    public static final String TEAMS = "Teams";
    public static final String USERS = "Users";
    public static final String TEAM_TASK_QUERY = "SELECT I.GROUP_ID_, count(I.GROUP_ID_) FROM ACT_RU_IDENTITYLINK I WHERE I.GROUP_ID_ IN (";
    public static final String GROUP_BY_I_GROUP_ID = " GROUP BY I.GROUP_ID_";
    public static final String USER_FQN = "com.nucleus.user.User";
    public static final String LEAST_LOADED_USER = "Least_Loaded_User";
    public static final String LEAST_LOADED_TEAM = "Least_Loaded_Team";

}
