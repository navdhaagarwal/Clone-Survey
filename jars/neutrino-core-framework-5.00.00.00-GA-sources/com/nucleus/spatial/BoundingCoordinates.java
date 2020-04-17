package com.nucleus.spatial;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.annotations.Synonym;
import com.nucleus.entity.BaseEntity;

@Entity
@DynamicUpdate
@DynamicInsert
@Synonym(grant="ALL")
@Table(indexes={@Index(name="minmaxlatlonglatitude_index",columnList="minmaxlatlonglatitude")})
public class BoundingCoordinates extends BaseEntity {

    @Transient
    private static final long serialVersionUID = 4415235277418934011L;

    @Column
    String                    minmaxlatlonglatitude;

    @Column
    String                    negativeName;

    @Column
    String                    negativeCode;

    @Column
    String                    description;

    public String getNegativeName() {
        return negativeName;
    }

    public void setNegativeName(String negativeName) {
        this.negativeName = negativeName;
    }

    public String getMinmaxlatlonglatitude() {
        return minmaxlatlonglatitude;
    }

    public void setMinmaxlatlonglatitude(String minmaxlatlonglatitude) {
        this.minmaxlatlonglatitude = minmaxlatlonglatitude;
    }

    public String getNegativeCode() {
        return negativeCode;
    }

    public void setNegativeCode(String negativeCode) {
        this.negativeCode = negativeCode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
