package com.nucleus.rules.datatable;

import java.io.Serializable;
import java.util.List;


public class DataTableJsonHelper implements Serializable{

    private int                sEcho;
    private int                iTotalRecords;
    private int                iTotalDisplayRecords;
    private List<List<Object>> aaData;

    /*Holds the list of object as key-value pair JSON .... used as data object in Data table of sAjaxSource property for server side pagination*/
    private List<? extends Object> additionalDataMap;
    private String taskId;

    public int getsEcho() {
        return sEcho;
    }

    public void setsEcho(int sEcho) {
        this.sEcho = sEcho;
    }

    public int getiTotalRecords() {
        return iTotalRecords;
    }

    public void setiTotalRecords(int iTotalRecords) {
        this.iTotalRecords = iTotalRecords;
    }

    public int getiTotalDisplayRecords() {
        return iTotalDisplayRecords;
    }

    public void setiTotalDisplayRecords(int iTotalDisplayRecords) {
        this.iTotalDisplayRecords = iTotalDisplayRecords;
    }

    public List<List<Object>> getAaData() {
        return aaData;
    }

    public void setAaData(List<List<Object>> aaData) {
        this.aaData = aaData;
    }

    public List<? extends Object> getAdditionalDataMap() {
        return additionalDataMap;
    }

    public void setAdditionalDataMap(List<? extends Object> additionalDataMap) {
        this.additionalDataMap = additionalDataMap;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }
}
