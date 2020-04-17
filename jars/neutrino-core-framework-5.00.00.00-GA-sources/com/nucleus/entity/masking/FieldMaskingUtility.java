package com.nucleus.entity.masking;

import com.nucleus.service.BaseService;
import com.nucleus.user.UserInfo;


public interface FieldMaskingUtility  extends BaseService{


	public void findMaskingBeanAndMaskData(Object object,UserInfo userInfo);

}
