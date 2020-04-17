package com.nucleus.security.masking.dao;

import com.nucleus.security.masking.entities.MaskingPolicy;

public interface MaskingPolicyDao {

	MaskingPolicy getMaskingPolicyByCode(String code);
}
