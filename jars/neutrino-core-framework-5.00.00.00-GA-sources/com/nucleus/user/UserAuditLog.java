package com.nucleus.user;

import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.OneToOne;

import com.nucleus.reason.ActiveReason;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.annotations.Synonym;
import com.nucleus.entity.BaseEntity;
import com.nucleus.reason.BlockReason;
import com.nucleus.reason.InactiveReason;

@Entity
@DynamicUpdate
@DynamicInsert
@Synonym(grant="ALL")
public class UserAuditLog extends BaseEntity {

    private static final long serialVersionUID = -2993301295415351889L;

    private Long              userId;
    private String            userEvent;
    private Integer           version;
    @Lob
    private String            userModificationString;
    private Long              userEntityId;

    @OneToOne
    private InactiveReason inactiveReason;

    @OneToOne
    private BlockReason blockReason;

    @OneToOne
    private ActiveReason activeReason;

    private String reasonRemarks;

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public Long getUserEntityId() {
        return userEntityId;
    }

    public void setUserEntityId(Long userEntityId) {
        this.userEntityId = userEntityId;
    }

    public String getUserModificationString() {
        return userModificationString;
    }

    public void setUserModificationString(String userModificationString) {
        this.userModificationString = userModificationString;
    }

    public String getUserEvent() {
        return userEvent;
    }

    public void setUserEvent(String userEvent) {
        this.userEvent = userEvent;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public InactiveReason getInactiveReason() {
        return inactiveReason;
    }

    public void setInactiveReason(InactiveReason inactiveReason) {
        this.inactiveReason = inactiveReason;
    }

    public BlockReason getBlockReason() {
        return blockReason;
    }

    public void setBlockReason(BlockReason blockReason) {
        this.blockReason = blockReason;
    }

    public String getReasonRemarks() {
        return reasonRemarks;
    }

    public void setReasonRemarks(String reasonRemarks) {
        this.reasonRemarks = reasonRemarks;
    }


    public ActiveReason getActiveReason() {
        return activeReason;
    }

    public void setActiveReason(ActiveReason activeReason) {
        this.activeReason = activeReason;
    }
}
