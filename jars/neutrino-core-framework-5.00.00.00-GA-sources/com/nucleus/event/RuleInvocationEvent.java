package com.nucleus.event;

import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.EntityId;

/**
 * The Class RuleInvocationEvent.
 *
 * @author Nucleus Software Exports Limited
 */
@Entity
@DynamicUpdate
@DynamicInsert
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public class RuleInvocationEvent extends GenericEvent {

    private static final long  serialVersionUID = -3789287521541903121L;

    public static final String INVOCATION_POINT = "RULE_INVOCATION_EVENT";

    /**
     * Instantiates a new maker checker event.
     */
    protected RuleInvocationEvent() {
        super();
    }

    public String getInvocationPoint() {
        return (String) getContextProperty(INVOCATION_POINT);
    }

    public void setInvocationPoint(String point) {
        addContextProperty(INVOCATION_POINT, point);
    }

    public <T extends BaseEntity> RuleInvocationEvent(int eventType, String invocationPoint, EntityId userEntityId) {
        super(eventType);
        addContextProperty(INVOCATION_POINT, invocationPoint);
        setAssociatedUserUri(userEntityId.getUri());
    }

}
