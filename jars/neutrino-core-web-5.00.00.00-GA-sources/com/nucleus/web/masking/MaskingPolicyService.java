package com.nucleus.web.masking;

import com.nucleus.security.masking.entities.MaskingPolicy;
import com.nucleus.user.User;

public interface MaskingPolicyService {

    public MaskingPolicy createMaskingPolicy(MaskingPolicy changedEntity, User user);

    public MaskingPolicy updateMaskingPolicy(MaskingPolicy changedEntity, User user);
}
