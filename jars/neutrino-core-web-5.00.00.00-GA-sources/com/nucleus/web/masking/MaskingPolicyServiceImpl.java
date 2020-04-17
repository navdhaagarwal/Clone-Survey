package com.nucleus.web.masking;

import javax.inject.Inject;
import javax.inject.Named;

import com.nucleus.core.validation.util.NeutrinoValidator;
import com.nucleus.entity.EntityId;
import com.nucleus.makerchecker.MakerCheckerService;
import com.nucleus.makerchecker.UnapprovedEntityData;
import com.nucleus.security.masking.entities.MaskingPolicy;
import com.nucleus.service.BaseServiceImpl;
import com.nucleus.user.User;

@Named("maskingPolicyService")
public class MaskingPolicyServiceImpl extends BaseServiceImpl implements MaskingPolicyService{
	
    @Inject
    @Named("makerCheckerService")
    private MakerCheckerService     makerCheckerService;

	@Override
	public MaskingPolicy createMaskingPolicy(MaskingPolicy changedEntity, User user) {
        EntityId userEntityId = user.getEntityId();
        UnapprovedEntityData unapprovedEntityData = new UnapprovedEntityData();
        unapprovedEntityData.setUserEntityId(userEntityId);
        NeutrinoValidator.notNull(changedEntity, "MaskingPolicy Entity Cannot be saved null");
        makerCheckerService.masterEntityChangedByUser(changedEntity, user);

        return changedEntity;
	}

	@Override
	public MaskingPolicy updateMaskingPolicy(MaskingPolicy changedEntity, User user) {
        EntityId userEntityId = user.getEntityId();
        UnapprovedEntityData unapprovedEntityData = new UnapprovedEntityData();
        unapprovedEntityData.setUserEntityId(userEntityId);
        NeutrinoValidator.notNull(changedEntity, "MaskingPolicy Entity Cannot be updated to null");
        return (MaskingPolicy) makerCheckerService.masterEntityChangedByUser(changedEntity, user);
	}

}
