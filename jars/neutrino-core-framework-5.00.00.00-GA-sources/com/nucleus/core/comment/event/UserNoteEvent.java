/**
 * This file and a proportion of its content is copyright of Nucleus Software Exports Limited - � 2012. All rights reserved.
 * Any redistribution or reproduction of part or all of the contents in any form is prohibited other than the following:
 * - you cannot print or download to a local hard disk extract contents either part or full for personal/ commercial/
 * academic or any other use
 * - you may not copy the content to individual/ third parties for any type of use, either as compiled or source format
 * without the knowledge and consent of Nucleus Software
 * - You may not, except with our express written permission, distribute or commercially exploit the content. Nor may you
 * transmit it or store it in any other web site or other form of electronic retrieval system.
 */
package com.nucleus.core.comment.event;

import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.event.GenericEvent;

/**
 * @author Nucleus Software Exports Limited
 * Event class for note.
 * @see Note 
 */
@Entity
@DynamicUpdate
@DynamicInsert
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public class UserNoteEvent extends GenericEvent {

    private static final long serialVersionUID = 7693381937033385198L;

    protected UserNoteEvent() {
        super();
    }

    public UserNoteEvent(int eventType) {
        super(eventType);
    }

    public void setNoteTitle(String commentText) {
        addPersistentProperty("NOTE_TITLE", String.valueOf(commentText));
    }

    public String getNoteTitle() {
        return getPersistentProperty("NOTE_TITLE");
    }

    public void setNoteText(String commentText) {
        addPersistentProperty("NOTE_TEXT", String.valueOf(commentText));
    }

    public String getNoteText() {
        return getPersistentProperty("NOTE_TEXT");
    }

    public void setOldNoteText(String oldText) {
        addPersistentProperty("OLD_NOTE_TEXT", oldText);
    }

    public String getOldNoteText() {
        return getPersistentProperty("OLD_NOTE_TEXT");

    }

    public void setNewNoteText(String oldText) {
        addPersistentProperty("NEW_NOTE_TEXT", oldText);
    }

    public String getNewNoteText() {
        return getPersistentProperty("NEW_NOTE_TEXT");
    }

    public void setSuccessFlag(boolean successFlag) {

        addPersistentProperty(SUCCESS_FLAG, successFlag ? "success" : "failure");

    }
}
