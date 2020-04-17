package com.nucleus.web.userpreferences;

import java.io.Serializable;
import java.util.List;

import com.nucleus.config.persisted.vo.ConfigurationVO;

public class PreferenceFormBean implements Serializable {

    private static final long serialVersionUID = 8038119441900774361L;

    List<ConfigurationVO>     configVOList;

    List<ConfigurationVO>     configVOCAList;

    public List<ConfigurationVO> getConfigVOList() {
        return configVOList;
    }

    public List<ConfigurationVO> getConfigVOCAList() {
        return configVOCAList;
    }

    public void setConfigVOCAList(List<ConfigurationVO> configVOCAList) {
        this.configVOCAList = configVOCAList;
    }

    public void setConfigVOList(List<ConfigurationVO> configVOList) {
        this.configVOList = configVOList;

    }

    List<String> myFavs;

    public List<String> getMyFavs() {
        return myFavs;
    }

    public void setMyFavs(List<String> myFavs) {
        this.myFavs = myFavs;
    }

}
