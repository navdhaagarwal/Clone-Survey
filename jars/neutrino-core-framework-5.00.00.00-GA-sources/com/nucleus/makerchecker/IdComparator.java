/**
 * Copyright Nucleus Software India Pvt Ltd. All rights reserved.
 */
package com.nucleus.makerchecker;

import java.util.Comparator;

import com.nucleus.entity.Entity;

/**
 * @author Nucleus Software India Pvt Ltd
 * TODO -> amit.parashar Add documentation to class
 */
public class IdComparator implements Comparator<Entity> {
    @Override
    public int compare(Entity arg0, Entity arg1) {
        return ((Long)arg0.getId()).compareTo((Long)arg1.getId());
    }

}
