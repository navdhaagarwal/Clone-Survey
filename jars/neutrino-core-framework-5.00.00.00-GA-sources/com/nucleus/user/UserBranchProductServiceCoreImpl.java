package com.nucleus.user;

import java.util.List;

import javax.inject.Named;

import com.nucleus.core.validation.util.NeutrinoValidator;
import com.nucleus.dao.query.NamedQueryExecutor;
import com.nucleus.query.constants.QueryHint;
import com.nucleus.service.BaseServiceImpl;

//TODO: Override service

@Named("userBranchProductService")
public class UserBranchProductServiceCoreImpl extends BaseServiceImpl implements UserBranchProductService{

	@Override
	public void updateUserInfoLoggedInBranchProducts() {
		// TODO Auto-generated method stub
		
	}
	
	
	
	
	
    @Override
    public List<String> getProductCodeFromUserId(Long userID) {
    	return null; 
    	}





	@Override
	public void updateUserInfoLoggedInBranchProducts(UserInfo userinfo) {
		// TODO Auto-generated method stub
		
	}

}
