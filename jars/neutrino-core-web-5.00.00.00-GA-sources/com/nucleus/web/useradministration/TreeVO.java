package com.nucleus.web.useradministration;

import java.io.Serializable;
import java.util.List;

/**
 * The class offers a general POJO for Tree structure.
 * This can be used to render/update complex tree interface.
 * @author Nucleus Software Exports Limited
 */

public class TreeVO implements Serializable {

    private static final long serialVersionUID = 5515689457046948893L;

    private String            id;
    private String            name;
    private String            title;
    private String            key;
    private Boolean           lazy;
    private Long              childCount;
    private List<TreeVO>      children;

    public TreeVO(String id, String name, List<TreeVO> children) {
        super();
        this.id = id;
        this.name = name;
        this.children = children;
    }

    public TreeVO() {
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

    public Boolean getLazy() {
        return lazy;
    }

    public void setLazy(Boolean lazy) {
        this.lazy = lazy;
    }

    public Long getChildCount() {
        return childCount;
    }

    public void setChildCount(Long childCount) {
        this.childCount = childCount;
    }

    public List<TreeVO> getChildren() {
        return children;
    }

    public void setChildren(List<TreeVO> childTreeVoList) {
        this.children = childTreeVoList;
    }

}
