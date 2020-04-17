package com.nucleus.makerchecker;

import java.util.List;

public class AccordianHeaders {

    private String headerName;
    private String headerDisplayName;
    private List<ColumnConfiguration> columnConfigurationList;

    public String getHeaderName() {
        return headerName;
    }

    public void setHeaderName(String headerName) {
        this.headerName = headerName;
    }

    public List<ColumnConfiguration> getColumnConfigurationList() {
        return columnConfigurationList;
    }

    public void setColumnConfigurationList(List<ColumnConfiguration> columnConfigurationList) {
        this.columnConfigurationList = columnConfigurationList;
    }

    public String getHeaderDisplayName() {
        return headerDisplayName;
    }

    public void setHeaderDisplayName(String headerDisplayName) {
        this.headerDisplayName = headerDisplayName;
    }
}