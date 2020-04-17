package com.nucleus.core.chat;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

import com.nucleus.core.annotations.Synonym;
import com.nucleus.entity.BaseEntity;
import com.nucleus.user.User;

@Entity
@DynamicUpdate
@DynamicInsert
@Synonym(grant="ALL")
public class ChatMessage extends BaseEntity {

    /**
     * 
     */
    private static final long serialVersionUID = 2805065621109128360L;

    private String            content;

    private String            header;

    @OneToOne(fetch = FetchType.LAZY)
    private User              fromUser;

    @ManyToOne(fetch=FetchType.LAZY)
    private Chat              chat;

    @Column
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime          time;

    public String getHeader() {
        return header;
    }

    public User getFromUser() {
        return fromUser;
    }

    public String getContent() {
        return content;
    }

    public Chat getChatId() {
        return chat;
    }

    public DateTime getTime() {
        return time;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public void setFromUser(User fromUser) {
        this.fromUser = fromUser;
    }

    public void setChatId(Chat chatId) {
        this.chat = chatId;
    }

    public void setTime(DateTime time) {
        this.time = time;
    }

}