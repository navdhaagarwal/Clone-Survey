package com.nucleus.core.notification;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.annotations.Synonym;
import com.nucleus.entity.BaseEntity;

@Entity
@DynamicUpdate
@DynamicInsert
@Inheritance(strategy = InheritanceType.JOINED)
@Synonym(grant="ALL")
@Table(indexes={@Index(name="toUserUri_index",columnList="toUserUri"),@Index(name="msgStatus_index",columnList="msgStatus"),
		@Index(name="USER_MAIL_NOTIF_IDX1",columnList="toUserUri, msgStatus, snapshotRecord")})
public class UserMailNotification extends BaseEntity {

    private static final long serialVersionUID = -5658617196379117754L;

    private String            toUserUri;

    @Column(length = 4000)
    private String            toUserUriList;

    private String            msgStatus;

    private String            mailNotificationPriority;

    @ManyToOne(fetch=FetchType.LAZY)
    private CommonMailContent commonMailContent;

    public String getToUserUri() {
        return toUserUri;
    }

    public void setToUserUri(String toUserUri) {
        this.toUserUri = toUserUri;
    }

    public String getMsgStatus() {
        return msgStatus;
    }

    public void setMsgStatus(String msgStatus) {
        this.msgStatus = msgStatus;
    }

    public String getMailNotificationPriority() {
        return mailNotificationPriority;
    }

    public void setMailNotificationPriority(String mailNotificationPriority) {
        this.mailNotificationPriority = mailNotificationPriority;
    }

    public CommonMailContent getCommonMailContent() {
        return commonMailContent;
    }

    public void setCommonMailContent(CommonMailContent commonMailContent) {
        this.commonMailContent = commonMailContent;
    }

    public String getToUserUriList() {
        return toUserUriList;
    }

    public void setToUserUriList(String toUserUriList) {
        this.toUserUriList = toUserUriList;
    }
    
    @Override
	public void loadLazyFields() {
		super.loadLazyFields();
		CommonMailContent commonMailContentTemp = getCommonMailContent();
		if (commonMailContentTemp != null) {
			commonMailContentTemp.loadLazyFields();
		}
	}
	

}
