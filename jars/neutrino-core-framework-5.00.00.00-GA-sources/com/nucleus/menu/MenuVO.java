package com.nucleus.menu;


import com.nucleus.authority.Authority;
import com.nucleus.core.messageSource.MessageResource;

import javax.persistence.Transient;
import java.util.List;

public class MenuVO {

    private Long id;
    private String menuCode;
    private String menuName;
    private String menuDisplayName;
    private String auth;
    private String toolTip;
    private String url;
    private Long menuOrder;
    private String menuLevel;
    private String product;
    private String parent;
    private String parentName;
    private String shortcut;
    private boolean systemDefined;
    List<MenuVO> subLevel;
    private Authority authorityObject;
    private MessageResource messageResource;
    private Long[] authArrLong;
    private String[] authArrString;
    private boolean isMovable;
    private boolean isActive = true;
    private String linkedFunction;
    private boolean isDivided = false;
    private String imageLinkedFunction;
    private String imageLinkedUrl;
    private String iconClassName;
    private String elementId;
    private String dividedBtnId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public String getParentName() {
        return parentName;
    }

    public void setParentName(String parentName) {
        this.parentName = parentName;
    }

    public String getShortcut() {
        return shortcut;
    }

    public void setShortcut(String shortcut) {
        this.shortcut = shortcut;
    }

    public boolean isSystemDefined() {
        return systemDefined;
    }

    public void setSystemDefined(boolean systemDefined) {
        this.systemDefined = systemDefined;
    }

    public List<MenuVO> getSubLevel() {
        return subLevel;
    }

    public void setSubLevel(List<MenuVO> subLevel) {
        this.subLevel = subLevel;
    }

    public Authority getAuthorityObject() {
        return authorityObject;
    }

    public void setAuthorityObject(Authority authorityObject) {
        this.authorityObject = authorityObject;
    }

    public MessageResource getMessageResource() {
        return messageResource;
    }

    public void setMessageResource(MessageResource messageResource) {
        this.messageResource = messageResource;
    }

    public Long[] getAuthArrLong() {
        return authArrLong;
    }

    public void setAuthArrLong(Long[] authArrLong) {
        this.authArrLong = authArrLong;
    }

    public String[] getAuthArrString() {
        return authArrString;
    }

    public void setAuthArrString(String[] authArrString) {
        this.authArrString = authArrString;
    }

    public String getMenuDisplayName() {
        return menuDisplayName;
    }

    public void setMenuDisplayName(String menuDisplayName) {
        this.menuDisplayName = menuDisplayName;
    }

    public boolean isMovable() {
        return isMovable;
    }

    public void setMovable(boolean movable) {
        isMovable = movable;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public String getLinkedFunction() {
        return linkedFunction;
    }

    public void setLinkedFunction(String linkedFunction) {
        this.linkedFunction = linkedFunction;
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