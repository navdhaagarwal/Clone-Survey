/**
 * @FileName: Metadata.java
 * @Author: amit.parashar
 * @Copyright: Nucleus Software Exports Ltd
 * @Description:
 * @Program-specification-Referred:
 * @Revision:
 *            --------------------------------------------------------------------------------------------------------------
 *            --
 * @Version | @Last Revision Date | @Name | @Function/Module affected | @Modifications Done
 *          ----------------------------------------------------------------------------------------------------------------
 *          | Jun 11, 2012 | amit.parashar | |
 */

package com.nucleus.metadata;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.Lob;

import com.nucleus.core.annotations.Synonym;
import com.nucleus.entity.BaseEntity;
import com.nucleus.persistence.Base64Data;

/**
 * @author amit.parashar
 * 
 */
@Entity
@DynamicUpdate
@DynamicInsert
@Inheritance(strategy = InheritanceType.JOINED)
@Synonym(grant="ALL")
@Table(indexes={@Index(name="METADATA_FK_index",columnList="METADATA_FK")})
public class MetadataEntry extends BaseEntity {

    @Transient
    private static final long serialVersionUID = -1790223186104541476L;

    private String            dataKey;

    @Lob
    private String            dataValue;

    @Embedded
    private Base64Data        base64data;

    /**
     * Protected default contructor to prevent null value/serializedData but to make reflection based instantiation to work properly
     */
    protected MetadataEntry() {
    }

    public MetadataEntry(String key, String value) {
        this.dataKey = key;
        this.dataValue = value;
    }

    public MetadataEntry(String key, Serializable data) {
        this.dataKey = key;
        setSerializedData(data);
    }

    /**
     * @return the keyName
     */
    public String getKey() {
        return dataKey;
    }

    /**
     * @param keyName the keyName to set
     */
    public void setKey(String key) {
        this.dataKey = key;
    }

    /**
     * @return the keyValue
     */
    public String getValue() {
        return dataValue;
    }

    /**
     * @param keyValue the keyValue to set
     */
    public void setValue(String value) {
        this.dataValue = value;
        this.base64data = null;
    }

    public Serializable getSerializedData() {
        if (base64data != null) {
            return base64data.getData();
        }
        return null;
    }

    public void setSerializedData(Serializable data) {
        this.base64data = new Base64Data(data);
        this.dataValue = null;
    }

    public boolean isStringType() {
        return dataValue != null;
    }
}
