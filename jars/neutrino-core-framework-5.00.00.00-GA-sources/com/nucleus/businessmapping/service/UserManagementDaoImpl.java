package com.nucleus.businessmapping.service;

import java.util.List;

import javax.inject.Named;
import javax.persistence.Query;

import net.bull.javamelody.MonitoredWithSpring;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;

import com.nucleus.businessmapping.entity.UserOrgBranchMapping;
import com.nucleus.persistence.BaseDaoImpl;

@Named("userManagementDaoCore")
public class UserManagementDaoImpl extends BaseDaoImpl<UserOrgBranchMapping> implements UserManagementDao {

    public static final String ALL_USER_ORGS_MYSQL  = "SELECT u_o_view.username as username, u_o_view.passwordExpiryDate as passwordExpiryDate ,u_o_view.full_name as fullName, u_o_view.userstatus as userstatus,u_o_view.user_id as user_id ,u_o_view.useruuid as useruuid, u_o_view.orgname as orgname ,usr.username as created_by_name FROM user_org_view u_o_view, users usr where ( u_o_view.orgname is null OR u_o_view.is_primary_branch = true) and (usr.id = SUBSTRING(u_o_view.created_by_uri,23) )  UNION SELECT u_o_view.username as username, u_o_view.passwordExpiryDate as passwordExpiryDate ,u_o_view.full_name as fullName, u_o_view.userstatus as userstatus,u_o_view.user_id as user_id ,u_o_view.useruuid as useruuid, u_o_view.orgname as orgname ,'system' as created_by_name FROM user_org_view u_o_view where ( u_o_view.orgname is null OR u_o_view.is_primary_branch = true) and u_o_view.created_by_uri  is null";

    public static final String ALL_USER_ORGS_ORACLE = "SELECT u_o_view.username as username, u_o_view.passwordExpiryDate as passwordExpiryDate ,u_o_view.full_name as fullName, u_o_view.userstatus as userstatus,u_o_view.user_id as user_id ,u_o_view.useruuid as useruuid, u_o_view.orgname as orgname ,usr.username as created_by_name FROM user_org_view u_o_view, users usr where ( u_o_view.orgname is null OR u_o_view.is_primary_branch = 1) and (usr.id = SUBSTR(u_o_view.created_by_uri,23) )  UNION SELECT u_o_view.username as username, u_o_view.passwordExpiryDate as passwordExpiryDate ,u_o_view.full_name as fullName, u_o_view.userstatus as userstatus,u_o_view.user_id as user_id ,u_o_view.useruuid as useruuid, u_o_view.orgname as orgname ,'system' as created_by_name FROM user_org_view u_o_view where ( u_o_view.orgname is null OR u_o_view.is_primary_branch = 1) and u_o_view.created_by_uri  is null";

	public static final String ALL_USER_ORGS_POSTGRES = "SELECT u_o_view.username as username, u_o_view.passwordExpiryDate as passwordExpiryDate ,u_o_view.full_name as fullName, u_o_view.userstatus as userstatus,u_o_view.user_id as user_id ,u_o_view.useruuid as useruuid, u_o_view.orgname as orgname ,usr.username as created_by_name FROM user_org_view u_o_view, users usr where ( u_o_view.orgname is null OR u_o_view.is_primary_branch = 1) and (usr.id = SUBSTR(u_o_view.created_by_uri,23)::numeric )  UNION SELECT u_o_view.username as username, u_o_view.passwordExpiryDate as passwordExpiryDate ,u_o_view.full_name as fullName, u_o_view.userstatus as userstatus,u_o_view.user_id as user_id ,u_o_view.useruuid as useruuid, u_o_view.orgname as orgname ,'system' as created_by_name FROM user_org_view u_o_view where ( u_o_view.orgname is null OR u_o_view.is_primary_branch = 1) and u_o_view.created_by_uri  is null";

	public static final String ALL_ACTIVE_USER_ORGS_MYSQL = "SELECT u_o_view.username as username, u_o_view.passwordExpiryDate as passwordExpiryDate ,u_o_view.full_name as fullName, u_o_view.userstatus as userstatus,u_o_view.user_id as user_id ,u_o_view.useruuid as useruuid, u_o_view.orgname as orgname ,usr.username as created_by_name FROM user_org_view u_o_view, users usr where u_o_view.is_primary_branch = true and u_o_view.userstatus NOT in (1) and usr.id = SUBSTRING(u_o_view.created_by_uri,23)  UNION SELECT u_o_view.username as username,u_o_view.passwordExpiryDate as passwordExpiryDate,u_o_view.full_name as fullName, u_o_view.userstatus as userstatus  ,u_o_view.user_id as user_id ,u_o_view.useruuid  as useruuid,u_o_view.orgname as orgname, usr.username FROM user_org_view u_o_view, users usr where usr.id = SUBSTRING(u_o_view.created_by_uri,23) and u_o_view.userstatus NOT in (1) and u_o_view.orgname is null";

	public static final String ALL_ACTIVE_USER_ORGS_ORACLE = "SELECT u_o_view.username as username, u_o_view.passwordExpiryDate as passwordExpiryDate ,u_o_view.full_name as fullName, u_o_view.userstatus as userstatus,u_o_view.user_id as user_id ,u_o_view.useruuid as useruuid, u_o_view.orgname as orgname ,usr.username as created_by_name FROM user_org_view u_o_view, users usr where u_o_view.is_primary_branch = 1 and u_o_view.userstatus NOT in (1) and usr.id = SUBSTR(u_o_view.created_by_uri,23)  UNION SELECT u_o_view.username as username,u_o_view.passwordExpiryDate as passwordExpiryDate,u_o_view.full_name as fullName, u_o_view.userstatus as userstatus  ,u_o_view.user_id as user_id ,u_o_view.useruuid  as useruuid,u_o_view.orgname as orgname, usr.username FROM user_org_view u_o_view, users usr where usr.id = SUBSTR(u_o_view.created_by_uri,23) and u_o_view.userstatus NOT in (1) and u_o_view.orgname is null  ";

	public static final String ALL_ACTIVE_USER_ORGS_POSTGRES = "SELECT u_o_view.username as username, u_o_view.passwordExpiryDate as passwordExpiryDate ,u_o_view.full_name as fullName, u_o_view.userstatus as userstatus,u_o_view.user_id as user_id ,u_o_view.useruuid as useruuid, u_o_view.orgname as orgname ,usr.username as created_by_name FROM user_org_view u_o_view, users usr where u_o_view.is_primary_branch = 1 and u_o_view.userstatus NOT in (1) and usr.id = SUBSTR(u_o_view.created_by_uri,23)::numeric  UNION SELECT u_o_view.username as username,u_o_view.passwordExpiryDate as passwordExpiryDate,u_o_view.full_name as fullName, u_o_view.userstatus as userstatus  ,u_o_view.user_id as user_id ,u_o_view.useruuid  as useruuid,u_o_view.orgname as orgname, usr.username FROM user_org_view u_o_view, users usr where usr.id = SUBSTR(u_o_view.created_by_uri,23)::numeric and u_o_view.userstatus NOT in (1) and u_o_view.orgname is null ";

	@Value(value = "#{'${database.type}'}")
    private String             DB_TYPE;
    
    @Value(value = "#{'${core.web.config.appChannel.mode}'}")



	private String APP_CHANNEL_MODE;

	@Value(value = "#{'${core.web.config.appChannel.mode.service.name}'}")
	private String APP_CHANNEL_MODE_SERVICE;

    public static final String DB_MYSQL             = "mysql";

    public static final String DB_ORACLE            = "oracle";

	public static final String DB_POSTGRES            = "postgres";
    
    public static final String INTERNET_CHANNEL = "I";

	@Autowired
	protected ApplicationContext applicationContext;

    @Override
    public List<Object> getAllUsers() {
		String sql = null;
		if (DB_TYPE.equals(DB_MYSQL)) {
			if (APP_CHANNEL_MODE.equals(INTERNET_CHANNEL)) {
				UserApplicationChannelModeService userApplicationChannelModeService;
				userApplicationChannelModeService = (UserApplicationChannelModeService) applicationContext
						.getBean(APP_CHANNEL_MODE_SERVICE);
				sql = userApplicationChannelModeService
						.getFilteredUser(DB_TYPE);
			} else {
				sql = ALL_USER_ORGS_MYSQL;
			}

		} else if (DB_TYPE.equals(DB_ORACLE)) {
			if (APP_CHANNEL_MODE.equals(INTERNET_CHANNEL)) {
				UserApplicationChannelModeService userApplicationChannelModeService;
				userApplicationChannelModeService = (UserApplicationChannelModeService) applicationContext
						.getBean(APP_CHANNEL_MODE_SERVICE);
				sql = userApplicationChannelModeService
						.getFilteredUser(DB_TYPE);
			} else {
				sql = ALL_USER_ORGS_ORACLE;
			}
		}else if (DB_TYPE.equals(DB_POSTGRES)){
			if (APP_CHANNEL_MODE.equals(INTERNET_CHANNEL)) {
				UserApplicationChannelModeService userApplicationChannelModeService;
				userApplicationChannelModeService = (UserApplicationChannelModeService) applicationContext
						.getBean(APP_CHANNEL_MODE_SERVICE);
				sql = userApplicationChannelModeService
						.getFilteredUser(DB_TYPE);
			} else {
				sql = ALL_USER_ORGS_POSTGRES;
			}
		}

		@SuppressWarnings("unchecked")
		List<Object> userInfoList = getJdbcTemplate().query(sql,
				new Object[] {}, new UserInfoRowMapper());
		return userInfoList;
    }
    
    @Override
	public List<Object> getAllActiveUsers() {
		String sql = null;
		if (DB_TYPE.equals(DB_MYSQL)) {
			if (APP_CHANNEL_MODE.equals(INTERNET_CHANNEL)) {
				UserApplicationChannelModeService userApplicationChannelModeService;
				userApplicationChannelModeService = (UserApplicationChannelModeService) applicationContext
						.getBean(APP_CHANNEL_MODE_SERVICE);
				sql = userApplicationChannelModeService
						.getFilteredUser(DB_TYPE);
			} else {
				sql = ALL_ACTIVE_USER_ORGS_MYSQL;
			}

		} else if (DB_TYPE.equals(DB_ORACLE)) {
			if (APP_CHANNEL_MODE.equals(INTERNET_CHANNEL)) {
				UserApplicationChannelModeService userApplicationChannelModeService;
				userApplicationChannelModeService = (UserApplicationChannelModeService) applicationContext
						.getBean(APP_CHANNEL_MODE_SERVICE);
				sql = userApplicationChannelModeService
						.getFilteredUser(DB_TYPE);
			} else {
				sql = ALL_ACTIVE_USER_ORGS_ORACLE;
			}

		} else if (DB_TYPE.equals(DB_POSTGRES)) {
			if (APP_CHANNEL_MODE.equals(INTERNET_CHANNEL)) {
				UserApplicationChannelModeService userApplicationChannelModeService;
				userApplicationChannelModeService = (UserApplicationChannelModeService) applicationContext
						.getBean(APP_CHANNEL_MODE_SERVICE);
				sql = userApplicationChannelModeService
						.getFilteredUser(DB_TYPE);
			} else {
				sql = ALL_ACTIVE_USER_ORGS_POSTGRES;
			}

		}


		@SuppressWarnings("unchecked")
		List<Object> userInfoList = getJdbcTemplate().query(sql,
				new Object[] {}, new UserInfoRowMapper());
		return userInfoList;
	}

    @Override
    @MonitoredWithSpring(name = "UMDI_FETCH_USERS_IN_BRANCH")
    public List<Object> getAllUsersInBranch(String branchId) {
        String sql = "SELECT u_o_view.username as username,u_o_view.passwordExpiryDate as passwordExpiryDate ,u_o_view.full_name as fullName, u_o_view.userstatus as userstatus,u_o_view.user_id as user_id,u_o_view.useruuid as useruuid, u_o_view.orgname as orgname, u_o_view.created_by_uri as created_by_name FROM user_org_view u_o_view where u_o_view.org_id = ?";
        @SuppressWarnings("unchecked")
        List<Object> usersList = getJdbcTemplate().query(sql, new Object[] {branchId}, new UserInfoRowMapper());
        return usersList;
    }

    @Override
    public Query createQuery(String qlString) {
        return getEntityManager().createQuery(qlString);
    }
}
