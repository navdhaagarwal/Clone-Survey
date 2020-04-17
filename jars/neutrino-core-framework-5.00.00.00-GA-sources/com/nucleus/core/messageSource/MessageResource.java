/**
 * This file and a proportion of its content is copyright of Nucleus Software Exports Limited - � 2012. All rights reserved.
 */
package com.nucleus.core.messageSource;

import java.util.List;

import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.annotations.Synonym;
import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.CloneOptions;


/**Entity to hold Resource message
 * @author Nucleus Software Exports Limited
 */
@Entity
@DynamicUpdate
@DynamicInsert
@Table(indexes = { @Index(name = "MESSAGE_RESOURCE_IDX1", columnList = "messageKey") })
@Cacheable
@Synonym(grant="ALL")
public class MessageResource extends BaseEntity{

    @Transient
    private static final long          serialVersionUID = 6249347148537316293L;
    private String                     messageKey;
    private static String ENTITY_DISPLAY_NAME="Message Resource";

    /*
     *List to hole value for locale and lcoale value  
     */
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "message_resource_fk")
    private List<MessageResourceValue> messageResourceValues;

    public String getMessageKey() {
        return messageKey;
    }

    public void setMessageKey(String messageKey) {
        this.messageKey = messageKey;
    }

    public List<MessageResourceValue> getMessageResourceValues() {
        return messageResourceValues;
    }

    public void setMessageResourceValues(List<MessageResourceValue> messageResourceValues) {
        this.messageResourceValues = messageResourceValues;
    }

    /**
     * this method is used by datatable to show default value in grid
     * @return
     */
    public String getDefaultValue() {
        if (messageResourceValues != null && messageResourceValues.size() > 0) {
            for (MessageResourceValue resource : messageResourceValues) {
                if (DatabaseDrivenMessageSource.default_locale.equals(resource.getLocaleKey())) {
                    return resource.getLocaleValue();
                }
            }
        }
        return "";
    }
    
    @Override
    public String getEntityDisplayName() {
        return ENTITY_DISPLAY_NAME;
    }
    
    /**
     * Gets the display name 
     */
    @Override
    public String getDisplayName() {
    	if(StringUtils.isNotBlank(messageKey)){
    	 return "with Message key "+messageKey;	
    	}
        return messageKey;
    }
    
    @Override
    protected void populate(BaseEntity baseEntity, CloneOptions cloneOptions) {
    	MessageResource messageResource = (MessageResource) baseEntity;
        super.populate(messageResource, cloneOptions);
        messageResource.setMessageKey(messageKey);
        messageResource.setMessageResourceValues(messageResourceValues);
    }

    @Override
    protected void populateFrom(BaseEntity baseEntity, CloneOptions cloneOptions) {
    	MessageResource messageResource = (MessageResource) baseEntity;
        super.populateFrom(messageResource, cloneOptions);
        this.setMessageKey(messageResource.getMessageKey());
        this.setMessageResourceValues(messageResource.getMessageResourceValues());
    }

}
