/* This file and a proportion of its content is copyright of Nucleus Software Exports Limited - © 2012. All rights reserved. */

package com.nucleus.entity;

import org.apache.commons.collections4.Predicate;
/*
 * A  predicate implementation which evaluates if the the entityId of the element in a collection is same as given entity.
 * e.g to use :  CollectionUtils.find(entityList, new EntityId(User.class, 101l).createPredicate());  
 */

public class EntityIdPredicate implements Predicate {

    private EntityId entityId;

    public EntityIdPredicate(EntityId entityId) {
        this.entityId = entityId;
    }

    @Override
    public boolean evaluate(Object object) {
        Entity instance = ((Entity) object);
        if (instance.getId() == null) {
            return false;
        }
        return ((Entity) object).getEntityId().equals(entityId);
    }

}
