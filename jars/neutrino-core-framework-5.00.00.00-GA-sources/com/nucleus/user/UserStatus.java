package com.nucleus.user;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Constants for user status
 */
public interface UserStatus {

    public static final int           STATUS_ACTIVE   = 0;
    public static final int           STATUS_INACTIVE = 1;
    public static final int           STATUS_LOCKED   = 2;
    public static final int           STATUS_DELETED  = 3;

    public static final List<Integer> ALL_STATUSES    = Collections.unmodifiableList(Arrays.asList(STATUS_ACTIVE,
                                                              STATUS_INACTIVE, STATUS_LOCKED, STATUS_DELETED));
    public static final List<Integer> ALL_STATUSES_EXCLUDING_DELETED    = Collections.unmodifiableList(Arrays.asList(STATUS_ACTIVE,
            STATUS_INACTIVE, STATUS_LOCKED));
    public static final List<Integer> ACTIVE_AND_LOCKED    = Collections.unmodifiableList(Arrays.asList(STATUS_ACTIVE,
            STATUS_LOCKED));
}