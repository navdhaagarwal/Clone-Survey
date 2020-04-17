package com.nucleus.core.datastore.service;

import java.io.Serializable;
import java.sql.Blob;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import com.nucleus.core.annotations.Synonym;
import com.nucleus.entity.CloneOptions;
import com.nucleus.entity.Entity;
import com.nucleus.entity.EntityId;

@javax.persistence.Entity
@Synonym(grant="ALL")
public class OracleDocumentEntity implements Entity {

    private static final long serialVersionUID = 1330351689109799978L;
    @Id
    @GenericGenerator(name = "sequencePerEntityGenerator", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator", parameters = {
            @Parameter(name = "prefer_sequence_per_entity", value = "true"),
            @Parameter(name = "sequence_per_entity_suffix", value = "_seq"),
            @Parameter(name = "initial_value", value = "250000") })
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "sequencePerEntityGenerator")
    private Long              id;
    private String            description;
    private String            filename;

    /**
     * The usage of Byte[] array would have been easy and clean implementation, however for large objects Byte[] will create problems. Use Blob datatype instead as Blob supports streaming while read.   
     */
    @Column(name = "content", length = 16777215)
    @Lob
    private Blob              content;

    @Column(name = "content_type")
    private String            contentType;

    @Column(name = "created")
    private Date              created;
    
    public OracleDocumentEntity() {
        super();
    }

    public OracleDocumentEntity(String description, String filename, Blob content, String contentType, Date created) {
        super();
        this.description = description;
        this.filename = filename;
        this.content = content;
        this.contentType = contentType;
        this.created = created;
    }

    @Override
    public Serializable getId() {
        return id;
    }

    @Override
    public void setId(Serializable id) {
        this.id = (Long) id;
    }

    @Override
    public EntityId getEntityId() {
        throw new UnsupportedOperationException("Operation not supported");
    }

    @Override
    public String getUri() {
        throw new UnsupportedOperationException("Operation not supported");
    }

    @Override
    public void loadLazyFields() {
        throw new UnsupportedOperationException("Operation not supported");
    }

    @Override
    public Entity cloneYourself(CloneOptions cloneOptions) {
        throw new UnsupportedOperationException("Operation not supported");
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public Blob getContent() {
        return content;
    }

    public void setContent(Blob content) {
        this.content = content;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

}
