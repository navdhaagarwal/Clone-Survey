package com.nucleus.spatial;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Transient;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.annotations.Synonym;
import com.nucleus.entity.BaseEntity;

@Entity
@DynamicUpdate
@DynamicInsert
@Synonym(grant="ALL")
public class GeoData extends BaseEntity {

	@Transient
	private static final long serialVersionUID = 4415235277418934011L;

public Double getLatitude() {
		return latitude;
	}

	public void setLatitude(Double latitude) {
		this.latitude = (latitude*100000);
	}

	public Double getLongitude() {
		return (longitude/100000);
	}

	public void setLongitude(Double longitude) {
		this.longitude = longitude;
	}

  @Column(columnDefinition="Decimal(21,18)")
  Double latitude ;

  @Column(columnDefinition="Decimal(21,18)")
  Double longitude ;
}