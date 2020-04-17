package com.nucleus.user;

import java.util.List;

public interface UserBranchProductService {

	void updateUserInfoLoggedInBranchProducts();
	public List<String> getProductCodeFromUserId(Long userID);
	void updateUserInfoLoggedInBranchProducts(UserInfo userinfo);
	
}
