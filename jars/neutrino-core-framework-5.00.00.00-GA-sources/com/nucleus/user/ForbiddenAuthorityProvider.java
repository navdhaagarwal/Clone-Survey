package com.nucleus.user;

import javax.inject.Named;

@Named("forbiddenAuthorityProvider")
public class ForbiddenAuthorityProvider implements IForbiddenAuthorityProvider{

	@Override
	public ForbiddenAuthorityVO getForbiddenAuthorityVO() {
		ForbiddenAuthorityVO authorityVO=new ForbiddenAuthorityVO();
		
		return authorityVO;
	}

}
