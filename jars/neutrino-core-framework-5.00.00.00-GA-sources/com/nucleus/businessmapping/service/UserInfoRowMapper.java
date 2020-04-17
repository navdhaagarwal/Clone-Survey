package com.nucleus.businessmapping.service;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.nucleus.entity.EntityLifeCycleData;
import com.nucleus.user.OrgBranchInfo;
import com.nucleus.user.User;
import com.nucleus.user.UserInfo;

@SuppressWarnings("rawtypes")
public class UserInfoRowMapper implements RowMapper {

    @Override
    public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
        User user = new User();
        user.setUsername(rs.getString("username"));
        user.setId(rs.getLong("user_id"));
        user.setUserStatus(rs.getInt("userstatus"));
        user.setPasswordExpirationDate(rs.getDate("passwordExpiryDate"));
        EntityLifeCycleData entityLifeCycleData = new EntityLifeCycleData();
        entityLifeCycleData.setUuid(rs.getString("useruuid"));
        entityLifeCycleData.setCreatedByUri(rs.getString("created_by_name"));
        user.setEntityLifeCycleData(entityLifeCycleData);
        UserInfo userInfo = new UserInfo(user);
        userInfo.setFullName(rs.getString("fullName"));
        if (rs.getString("orgname") != null) {
            OrgBranchInfo orgBranchInfo = new OrgBranchInfo();
            orgBranchInfo.setOrgName(rs.getString("orgname"));
            userInfo.setPrimaryOrgBranchInfo(orgBranchInfo);
        }
        return userInfo;
    }

}
