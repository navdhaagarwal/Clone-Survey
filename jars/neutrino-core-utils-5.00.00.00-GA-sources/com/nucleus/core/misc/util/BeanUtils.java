/**
 * This file and a proportion of its content is copyright of Nucleus Software Exports Limited - © 2012. All rights reserved.
 * Any redistribution or reproduction of part or all of the contents in any form is prohibited other than the following:
 * - you cannot print or download to a local hard disk extract contents either part or full for personal/ commercial/
 * academic or any other use
 * - you may not copy the content to individual/ third parties for any type of use, either as compiled or source format
 * without the knowledge and consent of Nucleus Software
 * - You may not, except with our express written permission, distribute or commercially exploit the content. Nor may you
 * transmit it or store it in any other web site or other form of electronic retrieval system.
 */
package com.nucleus.core.misc.util;

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.BeanUtilsBean2;
import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.DynaProperty;
import org.apache.commons.beanutils.MethodUtils;
import org.apache.commons.beanutils.PropertyUtilsBean;
import org.apache.commons.lang3.reflect.TypeUtils;
import org.joda.time.DateTime;
import org.springframework.beans.BeanInstantiationException;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import com.nucleus.core.exceptions.SystemException;

/**
 * Utility class to describe and populate bean properties.
 * Useful to convert a POJO to/from a map of string valued properties.
 * 
 * @author Nucleus Software Exports Limited
 */
public class BeanUtils {

    private final static BeanUtilsBean BEAN_UTILS_BEAN2 = BeanUtilsBean2.getInstance();

    /**
     * <p>Return the entire set of properties for which the specified bean
     * provides a read method. This map contains the to <code>String</code>
     * converted property values for all properties for which a read method
     * is provided (i.e. where the getReadMethod() returns non-null).</p>
     * Properties supported are all primitives,wrappers,String,DateTime(as an array or scalar)
     * Arrays are returned as CSV.
     * @param bean
     * @return a map of all bean properties
     * 
     */
    public static Map<String, String> describe(Object bean) {

        if (bean == null) {
            // return (Collections.EMPTY_MAP);
            return (new java.util.HashMap<String, String>());
        }

        Map<String, String> description = new HashMap<String, String>();
        if (bean instanceof DynaBean) {
            DynaProperty[] descriptors = ((DynaBean) bean).getDynaClass().getDynaProperties();
            for (int i = 0 ; i < descriptors.length ; i++) {
                String name = descriptors[i].getName();
                if (!name.equalsIgnoreCase("class")) {
                    description.put(name, getProperty(bean, name));
                }
            }
        } else {
            PropertyDescriptor[] descriptors = getPropertyUtils().getPropertyDescriptors(bean);
            Class<?> clazz = bean.getClass();
            for (int i = 0 ; i < descriptors.length ; i++) {
                String name = descriptors[i].getName();
                if (getReadMethod(clazz, descriptors[i]) != null && !name.equalsIgnoreCase("class")) {
                    description.put(name, getProperty(bean, name));
                }
            }
        }
        return (description);

    }

    /**
     * <p>Populate the JavaBeans properties of the specified bean, based on
     * the specified name/value pairs.  This method uses Java reflection APIs
     * to identify corresponding "property setter" method names, and deals
     * with setter arguments of type <code>String</code>, <code>boolean</code>,
     * <code>int</code>, <code>long</code>, <code>float</code>, and <code>double</code>,
     * <code>DateTime</code>.In addition, array setters for these types (or the
     * corresponding primitive types) can also be identified.</p>
     * @param properties
     * @param bean
     */
    public static void populate(Map<String, String> properties, Object bean) {

        // Do nothing unless both arguments have been specified
        if ((bean == null) || (properties == null)) {
            return;
        }

        // Loop through the property name/value pairs to be set
        for (Map.Entry<String, String> entry : properties.entrySet()) {
            // Identify the property name and value(s) to be assigned
            String name = entry.getKey();
            if (name == null) {
                continue;
            }

            // Invoke the setter method
            try {
                getPropertyUtils().setProperty(bean, name,
                        fromString(entry.getValue(), getPropertyUtils().getPropertyType(bean, name)));
            } catch (NoSuchMethodException e) {
                throw new SystemException("Cannot set " + name + " to bean class " + bean.getClass().getName(), e);
            } catch (IllegalAccessException e) {
                throw new SystemException("Cannot set " + name + " to bean class " + bean.getClass().getName(), e);
            } catch (InvocationTargetException e) {
                throw new SystemException("Cannot set " + name + " to bean class " + bean.getClass().getName(), e);
            }

        }

    }

    public static <T> T instantiate(Class<T> clazz) throws BeanInstantiationException {
        return org.springframework.beans.BeanUtils.instantiate(clazz);
    }

    /**
     * Determine the bean property type for the given property from the
     * given classes/interfaces, if possible.
     * @param propertyName the name of the bean property
     * @param beanClasses the classes to check against
     * @return the property type, or {@code Object.class} as fallback
     */
    public static Class<?> findPropertyType(String propertyName, Class<?>... beanClasses) {
        return org.springframework.beans.BeanUtils.findPropertyType(propertyName, beanClasses);
    }

    @SuppressWarnings("unchecked")
    public static <T> T fromString(String value, Class<T> targetType) {

        if (value == null || targetType == null) {
            return null;
        }
        if (TypeUtils.isArrayType(targetType)) {
            String[] strings = org.springframework.util.StringUtils.commaDelimitedListToStringArray(value);
            if (TypeUtils.getArrayComponentType(targetType).equals(String.class)) {
                return (T) strings;
            } else {
                return (T) BEAN_UTILS_BEAN2.getConvertUtils().convert(strings,
                        (Class<?>) TypeUtils.getArrayComponentType(targetType));
            }

        }

        if (TypeUtils.isAssignable(targetType, DateTime.class)) {
            return (T) DateTime.parse(value);
        }
        return (T) BEAN_UTILS_BEAN2.getConvertUtils().convert(value, targetType);

    }

    // ~~~====================================================
    private static PropertyUtilsBean getPropertyUtils() {
        return BEAN_UTILS_BEAN2.getPropertyUtils();
    }

    private static Method getReadMethod(Class<?> clazz, PropertyDescriptor descriptor) {
        return (MethodUtils.getAccessibleMethod(clazz, descriptor.getReadMethod()));
    }

    private static String getProperty(Object bean, String name) {

        Object value = null;
        try {
            value = getPropertyUtils().getNestedProperty(bean, name);
        } catch (IllegalAccessException e) {
            ExceptionUtility.rethrowSystemException(e);
        } catch (InvocationTargetException e) {
            ExceptionUtility.rethrowSystemException(e);
        } catch (NoSuchMethodException e) {
            ExceptionUtility.rethrowSystemException(e);
        }
        return (toString(value));

    }

    private static String toString(Object value) {

        if (value == null) {
            return null;
        }
        if (TypeUtils.isArrayType(value.getClass())) {
            return StringUtils.arrayToCommaDelimitedString((Object[]) value);
        }
        return value.toString();

    }

}
