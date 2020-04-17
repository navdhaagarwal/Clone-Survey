package com.nucleus.rules.simulation.service;

import java.util.List;
import java.util.Map;

import com.nucleus.entity.BaseEntity;
import com.nucleus.rules.model.EntityType;
import com.nucleus.service.BaseService;

/**
 * 
 * @author Nucleus Software Exports Limited
 * Service for performing Rule Simulation Process
 */
public interface RuleSimulationProcess extends BaseService {
    /**
     * 
     * Returns list of BaseEntity on the basis of Entity Class Name 
     * @param entityType
     * @param entityTypeFilterCriteria
     * @return
     * @throws ClassNotFoundException
     */
    public List<BaseEntity> listEntityProcess(EntityType entityType, EntityTypeFilterCriteria entityTypeFilterCriteria)
            throws ClassNotFoundException;

    /**
     * 
     * Can this implementation of RuleSimulationProcess can handle it's Rule simulation process
     * @param entityClass
     * @return
     */

    public boolean canHandleEntity(Class<? extends BaseEntity> entityClass);

    /**
     * 
     * Populates the contextObject for Entity Class
     * @param baseEntity
     * @param entityClass
     * @return
     */

    public <T extends BaseEntity> Map<Object, Object> populateContextObject(T baseEntity, Class<T> entityClass);

}