package com.nucleus.web.search;

import java.io.Serializable;
import java.util.List;

import com.nucleus.core.searchframework.entity.SearchAttributeBean;

public class SearchAttributeListBean  implements Serializable{

    private static final long serialVersionUID = 1235L;
    public static final String INST_NAME = "parties_customer_institutionInfo_instName";
    public static final String CUST_NAME = "parties_customer_personInfo_customerName";
    public static String          CUSTOMER = "Customer";
    
    private List<SearchAttributeBean> searchAttributeList;

    private String searchRequestEntityId;

    public List<SearchAttributeBean> getSearchAttributeList() {
        return searchAttributeList;
    }

    public void setSearchAttributeList(List<SearchAttributeBean> searchAttributeList) {
        this.searchAttributeList = searchAttributeList;
    }

    public String getSearchRequestEntityId() {
        return searchRequestEntityId;
    }

    public void setSearchRequestEntityId(String searchRequestEntityId) {
        this.searchRequestEntityId = searchRequestEntityId;
    }

}
