package com.nucleus.user;


import com.nucleus.menu.MenuVO;
import com.nucleus.rules.model.SourceProduct;

import java.io.Serializable;

public class UserDefaultUrlMappingVO implements Serializable{

    private static final long   serialVersionUID = -536471494005991976L;

    public Long id;

    public SourceProduct sourceProduct;

    public MenuVO menuVO;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public SourceProduct getSourceProduct() {
        return sourceProduct;
    }

    public void setSourceProduct(SourceProduct sourceProduct) {
        this.sourceProduct = sourceProduct;
    }

    public MenuVO getMenuVO() {
        return menuVO;
    }

    public void setMenuVO(MenuVO menuVO) {
        this.menuVO = menuVO;
    }
}
