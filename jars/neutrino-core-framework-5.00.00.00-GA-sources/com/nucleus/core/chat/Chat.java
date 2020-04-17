package com.nucleus.core.chat;

import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.annotations.Synonym;
import com.nucleus.entity.BaseEntity;
import com.nucleus.user.User;

@Entity
@DynamicUpdate
@DynamicInsert
@Synonym(grant="ALL")
public class Chat extends BaseEntity {

    private static final long serialVersionUID = -1473514099801378515L;

    private String            chatType;

    @ManyToMany(fetch=FetchType.LAZY)
    private Set<User>         users;

    @ManyToOne(fetch=FetchType.LAZY)
    private User              userInitiated;

    @OneToMany(fetch=FetchType.LAZY)
    private Set<ChatMessage>  chatMessages;

    public Set<User> getUsers() {
        return users;
    }

    public void setUsers(Set<User> users) {
        this.users = users;
    }

    public Set<ChatMessage> getChatMessages() {
        return chatMessages;
    }

    public void setChatMessages(Set<ChatMessage> chatMessages) {
        this.chatMessages = chatMessages;
    }

    public User getUserInitiated() {
        return userInitiated;
    }

    public void setUserInitiated(User userInitiated) {
        this.userInitiated = userInitiated;
    }

    public String getChatType() {
        return chatType;
    }

    public void setChatType(String chatType) {
        this.chatType = chatType;
    }

    public void addUser(User u) {
        users.add(u);
    }

}
