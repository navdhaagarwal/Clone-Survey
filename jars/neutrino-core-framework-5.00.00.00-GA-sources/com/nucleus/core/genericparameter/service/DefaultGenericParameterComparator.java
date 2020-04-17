/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nucleus.core.genericparameter.service;

import com.nucleus.core.genericparameter.entity.GenericParameter;
import com.nucleus.finnone.pro.base.exception.ExceptionBuilder;
import com.nucleus.finnone.pro.base.exception.SystemException;
import com.nucleus.user.User;
import java.lang.reflect.InvocationTargetException;
import org.apache.commons.beanutils.PropertyUtils;

/**
 *
 * @author rakesh.kumar
 */
public class DefaultGenericParameterComparator implements GenericParameterComparator<GenericParameter> {

    private String sortByColName;
    private String sortDir;

    private static final String NESTED_PROPERTY_EXCEPTION = "Exception occured while accessing nested property  value for column configuration.";
    private static final String SORT_DIR_ASC = "ASC";
    private static final String SORT_DIR_DESC = "DESC";

    public DefaultGenericParameterComparator(String sortByColName, String sortDir) {
        this.sortByColName = sortByColName;
        this.sortDir = (sortDir == null) ? SORT_DIR_ASC : sortDir;
    }

    @Override
    public int compare(GenericParameter o1, GenericParameter o2) {
        return getSortingResult(o1, o2);
    }

    private int getSortingResult(GenericParameter o1, GenericParameter o2) {

        int sortResult = 0;

        try {

            Object genericParameterAttributeValue1 = PropertyUtils.getNestedProperty(o1, sortByColName);
            Object genericParameterAttributeValue2 = PropertyUtils.getNestedProperty(o2, sortByColName);

            sortResult = getSortingResultBySortDirection(genericParameterAttributeValue1, genericParameterAttributeValue2, sortDir);

        } catch (IllegalAccessException illegalAccessException) {
            throw ExceptionBuilder.getInstance(SystemException.class, NESTED_PROPERTY_EXCEPTION, NESTED_PROPERTY_EXCEPTION).
                    setOriginalException(illegalAccessException)
                    .setMessage(NESTED_PROPERTY_EXCEPTION).build();
        } catch (InvocationTargetException invocationTargetException) {
            throw ExceptionBuilder.getInstance(SystemException.class, NESTED_PROPERTY_EXCEPTION, NESTED_PROPERTY_EXCEPTION).
                    setOriginalException(invocationTargetException)
                    .setMessage(NESTED_PROPERTY_EXCEPTION).build();
        } catch (NoSuchMethodException noSuchMethodException) {
            throw ExceptionBuilder.getInstance(SystemException.class, NESTED_PROPERTY_EXCEPTION, NESTED_PROPERTY_EXCEPTION).
                    setOriginalException(noSuchMethodException)
                    .setMessage(NESTED_PROPERTY_EXCEPTION).build();
        }

        return sortResult;
    }

    private int getSortingResultBySortDirection(Object value1, Object value2, String sortDir) {

        int sortResult = 0;

        if (String.valueOf(value1) != null && String.valueOf(value1).compareTo(String.valueOf(value2)) < 0) {
            if (SORT_DIR_ASC.equalsIgnoreCase(sortDir)) {
                sortResult = -1;
            } else if (SORT_DIR_DESC.equalsIgnoreCase(sortDir)) {
                sortResult = 1;
            }
        } else if (String.valueOf(value1) != null && String.valueOf(value1).compareTo(String.valueOf(value2)) > 0) {
            if (SORT_DIR_ASC.equalsIgnoreCase(sortDir)) {
                sortResult = 1;
            } else if (SORT_DIR_DESC.equalsIgnoreCase(sortDir)) {
                sortResult = -1;
            }
        }

        return sortResult;
    }
}
