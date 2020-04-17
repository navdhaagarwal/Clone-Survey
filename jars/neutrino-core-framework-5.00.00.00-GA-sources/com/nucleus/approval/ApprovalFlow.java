package com.nucleus.approval;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import com.nucleus.core.annotations.Synonym;
import com.nucleus.user.User;

@Entity
@DynamicInsert
@DynamicUpdate
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Synonym(grant="ALL")
@Table(indexes={@Index(name="approval_flow_user_index",columnList="initiator")})
public abstract class ApprovalFlow extends ProcessDrivenFlow {

    @Transient
    private static final long serialVersionUID = -8657526404702243156L;

    @ManyToOne(fetch = FetchType.LAZY)
    private User              initiator;

    public User getInitiator() {
        return initiator;
    }

    public void setInitiator(User initiator) {
        this.initiator = initiator;
    }

}