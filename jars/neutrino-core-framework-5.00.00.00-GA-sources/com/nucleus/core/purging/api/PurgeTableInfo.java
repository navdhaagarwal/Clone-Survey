package com.nucleus.core.purging.api;

import org.apache.commons.lang3.StringUtils;

public class PurgeTableInfo {

    private String tableName;

    private String archiveTableName;

    private String idColumnName;

    private PurgeTableInfo(String tableName, String archiveTableName, String idColumnName) {
        super();
        this.tableName = tableName;
        this.archiveTableName = archiveTableName;
        this.idColumnName = idColumnName;
    }

    public String getTableName() {
        return tableName;
    }

    public String getArchiveTableName() {
        return archiveTableName;
    }

    public String getIdColumnName() {
        return idColumnName;
    }

    public static PurgeTableInfo from(String tableName, String archiveTableName, String idColumnName) {

        checkNull(tableName, "tableName");
        checkNull(archiveTableName, "archiveTableName");
        checkNull(idColumnName, "idColumnName");

        return new PurgeTableInfo(tableName.toUpperCase(), archiveTableName.toUpperCase(), idColumnName);
    }

    private static void checkNull(String argument, String argName) {

        if (StringUtils.isBlank(argument)) {
            throw new IllegalArgumentException(argName + " can't be null/empty");
        }
    }

}
