package com.nucleus.menu;

import com.nucleus.core.annotations.Synonym;
import com.nucleus.entity.BaseEntity;
import com.nucleus.user.User;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name="FREQUENT_MENU")
@Synonym(grant="ALL")
public class FrequentMenu extends BaseEntity {

    private String menuCode1;
    private String menuCode2;
    private String menuCode3;
    private String menuCode4;
    private String menuCode5;
    @OneToOne(fetch = FetchType.LAZY)
    private User freqMenuUser;
    private String product;

    public String getMenuCode1() {
        return menuCode1;
    }

    public void setMenuCode1(String menuCode1) {
        this.menuCode1 = menuCode1;
    }

    public String getMenuCode2() {
        return menuCode2;
    }

    public void setMenuCode2(String menuCode2) {
        this.menuCode2 = menuCode2;
    }

    public String getMenuCode3() {
        return menuCode3;
    }

    public void setMenuCode3(String menuCode3) {
        this.menuCode3 = menuCode3;
    }

    public String getMenuCode4() {
        return menuCode4;
    }

    public void setMenuCode4(String menuCode4) {
        this.menuCode4 = menuCode4;
    }

    public String getMenuCode5() {
        return menuCode5;
    }

    public void setMenuCode5(String menuCode5) {
        this.menuCode5 = menuCode5;
    }

    public User getFreqMenuUser() {
        return freqMenuUser;
    }

    public void setFreqMenuUser(User freqMenuUser) {
        this.freqMenuUser = freqMenuUser;
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }
}
