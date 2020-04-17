package com.nucleus.metadata;

import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import com.nucleus.core.annotations.Synonym;
import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.EntityId;

@Entity
@DynamicUpdate
@DynamicInsert
@Inheritance(strategy = InheritanceType.JOINED)
@Synonym(grant="ALL")
public class Metadata extends BaseEntity {

    @Transient
    private static final long  serialVersionUID = -1790223186104541476L;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @Fetch(FetchMode.SUBSELECT)
    @JoinColumn(name = "METADATA_FK")
    private Set<MetadataEntry> metadataEntries;

    private String             ownerUri;

    /**
     * Set the owner entity's {@link EntityId}.
     * This is required if you plan to store backward pointing soft reference.
     * @param ownerEntityId The EntityId of entity to which this metadata belongs. 
     */
    public void setOwnerEntityId(EntityId ownerEntityId) {
        this.ownerUri = ownerEntityId.getUri();
    }

    public EntityId getOwnerEntityId() {
        return EntityId.fromUri(ownerUri);
    }

    public Set<String> getKeys() {
        Set<String> keys = null;
        if (isNotEmpty(metadataEntries)) {
            keys = new LinkedHashSet<String>();
            for (MetadataEntry entry : metadataEntries) {
                keys.add(entry.getKey());
            }
        }
        return keys;
    }

    public boolean entryExist(String key) {
        return getKeys().contains(key);
    }

    private MetadataEntry getEntry(String key) {
        if (metadataEntries == null) {
            return null;
        }
        for (MetadataEntry entry : metadataEntries) {
            if (entry.getKey().equals(key)) {
                return entry;
            }
        }
        return null;
    }

    private void createEntry(MetadataEntry entry) {
        if (metadataEntries == null) {
            metadataEntries = new LinkedHashSet<MetadataEntry>();
        }
        metadataEntries.add(entry);
    }

    public Metadata createOrUpdate(String key, String value) {
        MetadataEntry entry = getEntry(key);
        if (entry != null) {
            entry.setValue(value);
        } else {
            entry = new MetadataEntry(key, value);
        }
        createEntry(entry);
        return this;
    }

    public Metadata createOrUpdate(String key, Serializable serializedvalue) {
        MetadataEntry entry = getEntry(key);
        if (entry != null) {
            entry.setSerializedData(serializedvalue);
        } else {
            entry = new MetadataEntry(key, serializedvalue);
        }
        createEntry(entry);
        return this;
    }

    public String getValue(String key) {
        MetadataEntry entry = getEntry(key);
        return entry == null ? null : entry.getValue();
    }

    public Serializable getSerializedValue(String key) {
        MetadataEntry entry = getEntry(key);
        return entry == null ? null : entry.getSerializedData();
    }

    public Map<String, String> getStringValues() {
        if (metadataEntries == null) {
            return null;
        }
        Map<String, String> values = new LinkedHashMap<String, String>();
        for (MetadataEntry entry : metadataEntries) {
            if (entry.isStringType()) {
                values.put(entry.getKey(), entry.getValue());
            }
        }
        return values;
    }

    public Map<String, Serializable> getSerializableValues() {
        if (metadataEntries == null) {
            return null;
        }
        Map<String, Serializable> values = new LinkedHashMap<String, Serializable>();
        for (MetadataEntry entry : metadataEntries) {
            if (!entry.isStringType()) {
                values.put(entry.getKey(), entry.getSerializedData());
            }
        }
        return values;
    }

    public void addValues(Map<String, String> values) {
        for (Entry<String, String> entry : values.entrySet()) {
            createOrUpdate(entry.getKey(), entry.getValue());
        }
    }

    public void addSerializableValues(Map<String, Serializable> values) {
        for (Entry<String, Serializable> entry : values.entrySet()) {
            createOrUpdate(entry.getKey(), entry.getValue());
        }
    }
}
