package com.nucleus.core.misc.util;

import java.util.Date;

public class ClassUtil {

    public static boolean isSimpleType(Class clazz) {
        if (isPrimitiveOrWrapper(clazz)) {
            return true;
        } else if (clazz.equals(String.class)) {
            return true;
        } else if (clazz.equals(Date.class)) {
            return true;
        }
        return false;
    }

    public static boolean isPrimitiveOrWrapper(Class clazz) {
        if (clazz.isPrimitive()) {
            return true;
        }
        if (clazz.equals(Boolean.class)) {
            return true;
        } else if (clazz.equals(Character.class)) {
            return true;
        } else if (clazz.equals(Byte.class)) {
            return true;
        } else if (clazz.equals(Short.class)) {
            return true;
        } else if (clazz.equals(Integer.class)) {
            return true;
        } else if (clazz.equals(Long.class)) {
            return true;
        } else if (clazz.equals(Float.class)) {
            return true;
        } else if (clazz.equals(Double.class)) {
            return true;
        }
        return false;
    }

}
