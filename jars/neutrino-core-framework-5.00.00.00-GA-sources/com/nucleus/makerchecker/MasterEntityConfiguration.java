package com.nucleus.makerchecker;

import java.util.List;

public class MasterEntityConfiguration {

    private boolean                autoApproval;
    private List<EntityUpdateInfo> entityUpdateInfoList;

    public boolean getAutoApproval() {
        return autoApproval;
    }

    public void setAutoApproval(boolean autoApproval) {
        this.autoApproval = autoApproval;
    }

    public List<EntityUpdateInfo> getEntityUpdateInfoList() {
        return entityUpdateInfoList;
    }

    public void setEntityUpdateInfoList(List<EntityUpdateInfo> entityUpdateInfoList) {
        this.entityUpdateInfoList = entityUpdateInfoList;
    }

}
