package com.nucleus.web.useradministration;

import java.io.Serializable;
import java.util.List;

/**
 * The class offers a general POJO for Tree structure.
 * This can be used to render/update complex tree interface.
 * @author Nucleus Software Exports Limited
 */

public class TreeInfoVO implements Serializable {

    private static final long serialVersionUID = 5515689457046948894L;

    private String            id;
    private String            name;
    private String            title;
    private String            level;
    private String            url;
    private String            key;
    private Boolean           isLazy;
    private Long              childCount;
    private List<TreeInfoVO>      children;

    public TreeInfoVO(String id, String name, List<TreeInfoVO> children) {
        super();
        this.id = id;
        this.name = name;
        this.children = children;
    }

    public TreeInfoVO() {
        super();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Boolean getIsLazy() {
        return isLazy;
    }

    public void setIsLazy(Boolean lazy) {
        this.isLazy = lazy;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Long getChildCount() {
        return childCount;
    }

    public void setChildCount(Long childCount) {
        this.childCount = childCount;
    }

    public List<TreeInfoVO> getChildren() {
        return children;
    }

    public void setChildren(List<TreeInfoVO> childTreeInfoVOList) {
        this.children = childTreeInfoVOList;
    }

}
