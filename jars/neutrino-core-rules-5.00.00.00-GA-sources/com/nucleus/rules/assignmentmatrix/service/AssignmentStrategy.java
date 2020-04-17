/**
 * Copyright Nucleus Software India Pvt Ltd. All rights reserved.
 */
package com.nucleus.rules.assignmentmatrix.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.nucleus.entity.Entity;

/**
 * The Interface AssignmentStrategy.
 *
 * @author Nucleus Software India Pvt Ltd
 */
public interface AssignmentStrategy {

    /**
     * Handle entity list - This function takes  entities( example: Team , User )as a list and calls their respective strategies 
     * for setting team uri and branch uri 
     *
     * @param entitiesList the entities list
     * @return the map
     */
    public Map<Object, Object> handleEntityList(List<? extends Entity> entitiesList);

    default Map<Object, Object> findLeastLoadedEntity(List entitiesList){
        return new HashMap<Object, Object>();
    }

}
