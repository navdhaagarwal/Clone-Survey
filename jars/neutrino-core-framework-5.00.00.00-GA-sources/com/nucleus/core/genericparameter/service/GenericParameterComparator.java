package com.nucleus.core.genericparameter.service;

import java.util.Comparator;

import com.nucleus.core.genericparameter.entity.GenericParameter;

public interface GenericParameterComparator<T extends GenericParameter> extends Comparator<T> {

}
