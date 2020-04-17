package com.nucleus.entity;

/**
 * Constants for persistence status of an entity
 */

public class PersistenceStatus {
    public static final int ACTIVE   = 0;
    public static final int INACTIVE = 1;
    public static final int DELETED  = 2;
    public static final int SNAPSHOT = 3;
    public static final int UNKNOWN  = -1;
    public static final int DRAFT    = 100;
    public static final int TEMP     = 200;
    //for empty parent in case only child is persisted
    public static final int EMPTY_PARENT = 50;
    //Special case for partial saved applications (saved as draft)
    public static final int APP_SAD = 400;
}
