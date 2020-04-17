package com.nucleus.rules.assignmentmatrix.service;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;

import com.nucleus.core.exceptions.SystemException;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.rules.exception.RuleException;

/**
 * 
 * @author Nucleus Software Exports Limited
 * Utility class for Assignment Master
 *          converts json string to map
 */

public class AssignmentMatrixMasterUtility {
    /**
     * 
     * Method to convert the Json Expression to map object
     * @param json
     * @return
     */

    public static Map<String, Object> convertJsonToMap(String json) {
        Map<String, Object> map = new HashMap<String, Object>();
        JSONObject jsonObject = null;

        try {
            if (null != json) {
                jsonObject = new JSONObject(json);
                map = getMap(jsonObject, json);
            }

        } catch (Exception e) {
            throw new SystemException("Unable to read JSOn Object");
            // TODO : Handle Exception
        }
        return map;
    }

    private static Map getMap(JSONObject object, String json) throws Exception {
        Map<String, Object> map = new HashMap<String, Object>();
        Object jsonObject = null;

        Iterator<String> keys = object.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            Object value = object.get(key);

            if (value instanceof JSONObject) {
                map.put(key, getMap((JSONObject) value, json));
                continue;
            }

            if (value instanceof JSONArray) {
                JSONArray array = ((JSONArray) value);
                List list = new ArrayList();
                for (int i = 0 ; i < array.length() ; i++) {
                    jsonObject = array.get(i);
                    if (jsonObject instanceof JSONObject) {
                        list.add(getMap((JSONObject) jsonObject, json));
                    } else {
                        list.add(jsonObject);

                    }
                }
                map.put(key, list);
                continue;
            }

            map.put(key, value);
        }
        return map;
    }

    /**
     * 
     * Method to get the package name of the last property
     * @param clazz
     * @param ognl
     * @return
     */

    static String getPackageName(Class clazz, String ognl) {

        String packageName = "";

        Field field;
        Type type;
        try {
            String[] ognlArray = ognl.split("\\.");
            for (int index = 1 ; index < ognlArray.length ; index++) {
                field = retrieveField(ognlArray[index], clazz);
                type = field.getGenericType();
                if (type instanceof ParameterizedType) {
                    Type t = ((ParameterizedType) type).getActualTypeArguments()[0];
                    clazz = (Class) t;
                } else {
                    clazz = (Class) type;
                }
            }

            packageName = clazz.getName();

        } catch (SecurityException e) {
            BaseLoggers.exceptionLogger.debug("Error" + e);
            throw new RuleException(e.getMessage());
        }

        return packageName;
    }

    /**
     * 
     * Method to retrieve the field
     * @param ognl
     * @param clazz
     * @return
     */

    private static Field retrieveField(String ognl, Class clazz) {
        Field field = null;
        try {
            field = clazz.getDeclaredField(ognl);
            return field;
        } catch (SecurityException e) {
            BaseLoggers.exceptionLogger.debug("Error" + e);
            throw new RuleException(e.getMessage());
        } catch (NoSuchFieldException e) {
            if (clazz.getSimpleName().equalsIgnoreCase("BaseEntity")) {
                BaseLoggers.exceptionLogger.debug("Error" + e);
                throw new RuleException(e.getMessage());
            } else {
                clazz = clazz.getSuperclass();
                return retrieveField(ognl, clazz);
            }
        }
    }
}
