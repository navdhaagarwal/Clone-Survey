package com.nucleus.businessmapping.service;

import java.util.List;

import com.nucleus.service.BaseService;

public interface UserApplicationChannelModeService extends BaseService{

	String getFilteredUser(String DB_TYPE);
	
	List<Object> getLoggedInUserByProfile(List<Object> loggedInUserInfoList);
	
	List<Object> getFilteredUsersForChat();

}
