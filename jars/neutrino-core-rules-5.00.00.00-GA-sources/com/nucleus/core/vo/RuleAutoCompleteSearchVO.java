package com.nucleus.core.vo;

public class RuleAutoCompleteSearchVO {
    private String className; 
    private String itemVal; 
    private String[] searchColumnList;
    private String value; 
    private boolean loadApprovedEntityFlag; 
    private String listOfItems;
    private boolean strictSearchOnListOfItems; 
    private int page;
    public String getClassName() {
        return className;
    }
    public void setClassName(String className) {
        this.className = className;
    }
    public String getItemVal() {
        return itemVal;
    }
    public void setItemVal(String itemVal) {
        this.itemVal = itemVal;
    }
    public String[] getSearchColumnList() {
        return (String[])searchColumnList.clone();
    }
    public void setSearchColumnList(String[] searchColumnList) {
        this.searchColumnList = (String[])searchColumnList.clone();
    }
    public String getValue() {
        return value;
    }
    public void setValue(String value) {
        this.value = value;
    }
    public boolean isLoadApprovedEntityFlag() {
        return loadApprovedEntityFlag;
    }
    public void setLoadApprovedEntityFlag(boolean loadApprovedEntityFlag) {
        this.loadApprovedEntityFlag = loadApprovedEntityFlag;
    }
    public String getListOfItems() {
        return listOfItems;
    }
    public void setListOfItems(String listOfItems) {
        this.listOfItems = listOfItems;
    }
    public boolean isStrictSearchOnListOfItems() {
        return strictSearchOnListOfItems;
    }
    public void setStrictSearchOnListOfItems(boolean strictSearchOnListOfItems) {
        this.strictSearchOnListOfItems = strictSearchOnListOfItems;
    }
    public int getPage() {
        return page;
    }
    public void setPage(int page) {
        this.page = page;
    }
    
}
