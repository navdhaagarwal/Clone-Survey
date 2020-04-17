package com.nucleus.menu;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class MenuRootVO {

    @JsonProperty("subLevel")
    List<MenuVO> menuEntities;

    public List<MenuVO> getMenuEntities() {
        return menuEntities;
    }

    public void setMenuEntities(List<MenuVO> menuEntities) {
        this.menuEntities = menuEntities;
    }


}