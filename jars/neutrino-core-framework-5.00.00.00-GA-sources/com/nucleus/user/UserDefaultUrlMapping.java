package com.nucleus.user;

import com.nucleus.core.annotations.Synonym;
import com.nucleus.entity.BaseEntity;
import com.nucleus.master.audit.annotation.EmbedInAuditAsReference;
import com.nucleus.menu.MenuEntity;
import com.nucleus.rules.model.SourceProduct;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@DynamicUpdate
@DynamicInsert
@Synonym(grant="ALL")
@Table(name = "USER_DEFAULT_URL_MAPPING")
public class UserDefaultUrlMapping extends BaseEntity{

    @ManyToOne
    private User mappedUser;

    @EmbedInAuditAsReference(columnToDisplay = "menuCode")
    @ManyToOne
    private MenuEntity menuEntity;

    @EmbedInAuditAsReference
    @ManyToOne
    private SourceProduct sourceProduct;

    public User getMappedUser() {
        return mappedUser;
    }

    public void setMappedUser(User mappedUser) {
        this.mappedUser = mappedUser;
    }

    public MenuEntity getMenuEntity() {
        return menuEntity;
    }

    public void setMenuEntity(MenuEntity menuEntity) {
        this.menuEntity = menuEntity;
    }

    public SourceProduct getSourceProduct() {
        return sourceProduct;
    }

    public void setSourceProduct(SourceProduct sourceProduct) {
        this.sourceProduct = sourceProduct;
    }
}
