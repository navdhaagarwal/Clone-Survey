/**
 * This file and a proportion of its content is copyright of Nucleus Software Exports Limited - � 2012. All rights reserved.
 * Any redistribution or reproduction of part or all of the contents in any form is prohibited other than the following:
 * - you cannot print or download to a local hard disk extract contents either part or full for personal/ commercial/
 * academic or any other use
 * - you may not copy the content to individual/ third parties for any type of use, either as compiled or source format
 * without the knowledge and consent of Nucleus Software
 * - You may not, except with our express written permission, distribute or commercially exploit the content. Nor may you
 * transmit it or store it in any other web site or other form of electronic retrieval system.
 */
package com.nucleus.core.purging.api;

import java.sql.Connection;
import java.util.List;

/**
 * @author Nucleus Software Exports Limited
 * Builder class to pass multiple arguments to methods
 */
public class PurgeParameterHolder {

    private String         sqlString;
    private List<?>        idList;
    private Connection     conn;
    private PurgeTableInfo tableInfo;

    public static class Builder {
        private String         sqlString;
        private List<?>        idList;
        private Connection     conn;
        private PurgeTableInfo tableInfo;

        public Builder sqlString(String sqlString) {
            this.sqlString = sqlString;
            return this;
        }

        public Builder idList(List<?> idList) {
            this.idList = idList;
            return this;
        }

        public Builder conn(Connection conn) {
            this.conn = conn;
            return this;
        }

        public Builder tableInfo(PurgeTableInfo tableInfo) {
            this.tableInfo = tableInfo;
            return this;
        }

        public PurgeParameterHolder build() {
            return new PurgeParameterHolder(this);
        }
    }

    private PurgeParameterHolder(Builder builder) {
        this.sqlString = builder.sqlString;
        this.idList = builder.idList;
        this.conn = builder.conn;
        this.tableInfo = builder.tableInfo;
    }

    public String getTableName() {
        return tableInfo.getTableName();
    }

    public String getArchiveTableName() {
        return tableInfo.getArchiveTableName();
    }

    public String getIdColumnName() {
        return tableInfo.getIdColumnName();
    }

    public String getSqlString() {
        return sqlString;
    }

    public List getIdList() {
        return idList;
    }

    public Connection getConn() {
        return conn;
    }

    public PurgeTableInfo getTableInfo() {
        return tableInfo;
    }
}
