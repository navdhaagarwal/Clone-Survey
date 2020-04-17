package com.nucleus.core.comment.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

import com.nucleus.core.annotations.Synonym;
import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.EntityId;

@Entity
@DynamicUpdate
@DynamicInsert
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
// 'Comment' table name is not supported in Oracle therefore renaming the table
@Table(name = "TBL_COMMENT",indexes={@Index(name="ownerEntityUri_index",columnList="ownerEntityUri"),@Index(name="addedBy_index",columnList="addedBy"),
		@Index(name="COMMENT_TEXT_INDEX",columnList="text")})
@Synonym(grant="ALL")

public class Comment extends BaseEntity {

    @Transient
    private static final long serialVersionUID = 6365592271840257069L;

    @Column
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime          addTimestamp;
    
    private String            ownerEntityUri;
    
    @Column(length=4000)
    private String            text;
    
    private String            addedBy;

    public EntityId getAddedBy() {
        return EntityId.fromUri(addedBy);
    }

    public void setAddedBy(EntityId addedBy) {
        this.addedBy = addedBy.getUri();
    }

    public DateTime getAddTimestamp() {
        return addTimestamp;
    }

    public void setAddTimestamp(DateTime addTimestamp) {
        this.addTimestamp = addTimestamp;
    }

    public EntityId getOwnerEntityUri() {
        return EntityId.fromUri(ownerEntityUri);
    }

    public void setOwnerEntityUri(EntityId ownerEntityId) {
        this.ownerEntityUri = ownerEntityId.getUri();
    }

    public String getText() {
        return text;
    }

    public void setText(String commentText) {
        this.text = commentText;
    }

}