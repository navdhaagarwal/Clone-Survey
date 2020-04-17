package com.nucleus.core.security.entities;

import com.nucleus.core.annotations.Synonym;
import com.nucleus.entity.BaseEntity;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.Cacheable;
import javax.persistence.Entity;

/**
 * Created by gajendra.jatav on 9/15/2019.
 */
@Entity
@DynamicUpdate
@DynamicInsert
@Synonym(grant="ALL")
public class UriUploadLimitConfiguration extends BaseEntity{

    private String uri;

    private Long uploadLimit;

    @Override
    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public Long getUploadLimit() {
        return uploadLimit;
    }

    public void setUploadLimit(Long uploadLimit) {
        this.uploadLimit = uploadLimit;
    }
}
