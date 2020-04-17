package com.nucleus.menu;

import com.nucleus.core.annotations.Synonym;
import com.nucleus.entity.BaseEntity;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name="CAS_MENU")
@Synonym(grant="ALL")
@Cacheable
public class MenuEntity extends BaseEntity{

    private String menuCode;
    private String menuName;
    
    @Column(length=2000)
    private String auth;
    
    private String toolTip;
    private String url;
    private Long menuOrder;
    private String menuLevel;
    private String product;
    private String parent;
    private String shortcut;
    private boolean isMovable = true;
    private boolean isActive = true;
    private String linkedFunction;
    @Column(name = "is_divided_btn" , nullable = false)
    private boolean isDivided = false;
    private String imageLinkedFunction;
    private String imageLinkedUrl;
    private String iconClassName;
    private String elementId;
    private String dividedBtnId;

    public String getMenuCode() {
        return menuCode;
    }

    public void setMenuCode(String menuCode) {
        this.menuCode = menuCode;
    }

    public String getMenuName() {
        return menuName;
    }

    public void setMenuName(String menuName) {
        this.menuName = menuName;
    }

    public String getAuth() {
        return auth;
    }

    public void setAuth(String auth) {
        this.auth = auth;
    }

    public String getToolTip() {
        return toolTip;
    }

    public void setToolTip(String toolTip) {
        this.toolTip = toolTip;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Long getMenuOrder() {
        return menuOrder;
    }

    public void setMenuOrder(Long menuOrder) {
        this.menuOrder = menuOrder;
    }

    public String getMenuLevel() {
        return menuLevel;
    }

    public void setMenuLevel(String menuLevel) {
        this.menuLevel = menuLevel;
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public String getParent() {
        return parent;
    }

    public void setParent(String parent) {
        this.parent = parent;
    }

    public String getShortcut() {
        return shortcut;
    }

    public void setShortcut(String shortcut) {
        this.shortcut = shortcut;
    }

    public boolean isMovable() {
        return isMovable;
    }

    public void setMovable(boolean movable) {
        isMovable = movable;
    }

    public String getLinkedFunction() {
        return linkedFunction;
    }

    public void setLinkedFunction(String linkedFunction) {
        this.linkedFunction = linkedFunction;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public boolean isDivided() {
        return isDivided;
    }

    public void setDivided(boolean divided) {
        isDivided = divided;
    }

    public String getImageLinkedFunction() {
        return imageLinkedFunction;
    }

    public void setImageLinkedFunction(String imageLinkedFunction) {
        this.imageLinkedFunction = imageLinkedFunction;
    }

    public String getImageLinkedUrl() {
        return imageLinkedUrl;
    }

    public void setImageLinkedUrl(String imageLinkedUrl) {
        this.imageLinkedUrl = imageLinkedUrl;
    }

    public String getIconClassName() {
        return iconClassName;
    }

    public void setIconClassName(String iconClassName) {
        this.iconClassName = iconClassName;
    }

    public String getElementId() {
        return elementId;
    }

    public void setElementId(String elementId) {
        this.elementId = elementId;
    }

    public String getDividedBtnId() {
        return dividedBtnId;
    }

    public void setDividedBtnId(String dividedBtnId) {
        this.dividedBtnId = dividedBtnId;
    }
}