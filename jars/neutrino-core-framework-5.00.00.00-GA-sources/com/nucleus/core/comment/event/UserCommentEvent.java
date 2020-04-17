package com.nucleus.core.comment.event;

import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.event.GenericEvent;

@Entity
@DynamicUpdate
@DynamicInsert
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public class UserCommentEvent extends GenericEvent {

    private static final long serialVersionUID = 8046265248909557734L;

    protected UserCommentEvent() {
        super();
    }

    public UserCommentEvent(int eventType) {
        super(eventType);
    }

    public void setCommentText(String commentText) {
        addPersistentProperty("COMMENT_TEXT", String.valueOf(commentText));
    }

    public String getCommentText() {
        return getPersistentProperty("COMMENT_TEXT");
    }

    public void setOldText(String oldText) {
        addPersistentProperty("OLD_TEXT", oldText);
    }

    public String getOldText() {
        return getPersistentProperty("OLD_TEXT");

    }

    public void setNewText(String oldText) {
        addPersistentProperty("NEW_TEXT", oldText);
    }

    public String getNewText() {
        return getPersistentProperty("NEW_TEXT");
    }

    public void setSuccessFlag(boolean successFlag) {

        addPersistentProperty(SUCCESS_FLAG, successFlag ? "success" : "failure");

    }

}