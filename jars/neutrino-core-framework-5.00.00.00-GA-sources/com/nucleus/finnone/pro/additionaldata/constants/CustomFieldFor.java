package com.nucleus.finnone.pro.additionaldata.constants;

import com.nucleus.core.genericparameter.entity.GenericParameter;
import javax.persistence.Entity;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

@Entity
@DynamicUpdate
@DynamicInsert
public class CustomFieldFor extends GenericParameter
{
  private static final long serialVersionUID = 1L;
  public static final String SERVICE_REQUEST = "S";
  public static final String TRANSACTION = "T";
}