package com.nucleus.core.rules.comparison.service;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Named;

import org.mvel2.MVEL;

import com.nucleus.core.genericparameter.entity.GenericParameter;
import com.nucleus.core.money.entity.Money;
import com.nucleus.entity.BaseEntity;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.master.BaseMasterEntity;
import com.nucleus.rules.service.BaseRuleServiceImpl;

@Named("comparisonService")
public class CompareUtlityServiceImpl extends BaseRuleServiceImpl implements CompareUtlityService {

    @Override
    public Map<String, Object> compareObjects(Object base, Object compareTo, Object target,
            Map<Class, String> onlyUseFields, Map<Class, String> ignorableFieldMap, Map<Class, String> mandateFieldMap,
            List<String> ignoreCommonFields) {

        Map<String, Object> resultMap = new HashMap<String, Object>();

        if (null != base && null != compareTo && null != target) {
            Map<String, Object> contextMap = new HashMap<String, Object>();
            contextMap.put(ComparisonConstants.BASE_OBJECT + base.getClass().getSimpleName(), base);
            contextMap.put(ComparisonConstants.COMPARE_TO_OBJECT + compareTo.getClass().getSimpleName(), compareTo);
            contextMap.put(ComparisonConstants.TARGET_OBJECT + target.getClass().getSimpleName(), target);

            List<ScriptFieldOgnl> ognls = new ArrayList<ScriptFieldOgnl>();

            String baseOgnl = ComparisonConstants.BASE_OBJECT + base.getClass().getSimpleName();
            loop(base, compareTo, onlyUseFields, ignorableFieldMap, mandateFieldMap, ognls, "", ignoreCommonFields, baseOgnl);

            List<String> diffFields = new ArrayList<String>();

            for (ScriptFieldOgnl scriptOgnl : ognls) {
                try {
                    BaseLoggers.flowLogger.debug("Script to be executed : " + scriptOgnl.getScript());

                    if ((Boolean) MVEL.eval(scriptOgnl.getScript(), contextMap)) {

                        BaseLoggers.flowLogger.debug("Script Executed : Match is false : So executing Then action script=>"
                                + scriptOgnl.getThenActionScript());
                        try {
                            MVEL.eval(scriptOgnl.getThenActionScript(), contextMap);

                        } catch (Exception e) {
                            BaseLoggers.exceptionLogger
                                    .error("Exception occured while Executing Then action script : Script used is "
                                            + scriptOgnl.getThenActionScript());
                        }

                        diffFields.add(scriptOgnl.getFieldOgnl().replace(ComparisonConstants.BASE_OBJECT, ""));
                    }

                } catch (Exception e) {
                    BaseLoggers.exceptionLogger.error("Exception occured while comparing : Script used is "
                            + scriptOgnl.getScript());
                }
            }

            resultMap.put(ComparisonConstants.DIFFERENT_FIELDS_PATH, diffFields);
            resultMap.put(ComparisonConstants.TARGET_AFTER_MODIFICATION,
                    contextMap.get(ComparisonConstants.TARGET_OBJECT + target.getClass().getSimpleName()));

            return resultMap;

        }

        return null;
    }

    private void loop(Object base, Object compareTo, Map<Class, String> onlyUseFields, Map<Class, String> ignorableFieldMap,
            Map<Class, String> mandateFieldMap, List<ScriptFieldOgnl> ognls, String ognl, List<String> ignoreCommonFields,
            String baseOgnl) {
        try {

            Type type;

            if (null != base) {
                Class clazz = base.getClass();

                Field[] fields = clazz.getDeclaredFields();

                for (Field currentField : fields) {
                    currentField.setAccessible(true);
                    boolean processField = true;

                    String currentBaseOgnl = "";

                    if (ognl.equals("")) {
                        ognl = baseOgnl;

                    } else {
                        currentBaseOgnl = ognl;
                    }

                    if ((null != ignorableFieldMap && null != ignorableFieldMap.get(currentField.getDeclaringClass()) && ignorableFieldMap
                            .get(currentField.getDeclaringClass()).indexOf(currentField.getName()) != -1)
                            || (null != ignoreCommonFields && ignoreCommonFields.contains(currentField.getName()))) {
                        processField = false;
                    }

                    if (null != onlyUseFields && null != onlyUseFields.get(currentField.getDeclaringClass())
                            && onlyUseFields.get(currentField.getDeclaringClass()).indexOf(currentField.getName()) == -1) {
                        processField = false;
                    }

                    if (processField) {
                        ognl = ognl + ".?" + currentField.getName();
                        type = currentField.getGenericType();

                        if (type instanceof ParameterizedType) {
                            Type t = ((ParameterizedType) type).getActualTypeArguments()[0];
                            clazz = (Class) t;
                            Class class1 = currentField.getType();

                            if (class1.getCanonicalName().equals("java.util.List")) {
                                List baseList = (List) currentField.get(base);
                                List compareToList = (List) currentField.get(compareTo);
                                compareCollectionObjects(currentField, baseList, compareToList, onlyUseFields,
                                        ignorableFieldMap, mandateFieldMap, ignoreCommonFields, ognl, ognls);
                                ognl = baseOgnl;
                            }
                        } else {
                            if (!((Money.class).isAssignableFrom(currentField.getType())
                                    || (GenericParameter.class).isAssignableFrom(currentField.getType()) || (BaseMasterEntity.class)
                                        .isAssignableFrom(currentField.getType()))
                                    && (BaseEntity.class).isAssignableFrom(currentField.getType())) {
                                loop(currentField.get(base), currentField.get(compareTo), onlyUseFields, ignorableFieldMap,
                                        mandateFieldMap, ognls, ognl, ignoreCommonFields, baseOgnl);
                                ognl = baseOgnl;
                            } else {
                                compareValuesOftwoObjects(currentField, base, compareTo, ognl, ognls);
                                ognl = currentBaseOgnl;
                            }
                        }
                    }

                }
                ognl = baseOgnl;
            }
        } catch (Exception e) {
            BaseLoggers.exceptionLogger.error("Exception occured :" + e);
        }
    }

    public void compareCollectionObjects(Field currentField, List baseList, List compareToList,
            Map<Class, String> onlyUseFields, Map<Class, String> ignorableFieldMap, Map<Class, String> mandateFieldMap,
            List<String> ignoreCommonFields, String ognl, List<ScriptFieldOgnl> ognls) throws InstantiationException,
            IllegalAccessException {

        if (null != baseList && null != compareToList && baseList.size() > 0 && compareToList.size() > 0
                && baseList.size() == compareToList.size()) {
            Type type;
            Class clazz = null;
            Field[] fields;
            int baseListCount = 0;

            for (Object obj : baseList) {
                clazz = obj.getClass();
                fields = clazz.getDeclaredFields();
                for (Field tempField : fields) {
                    String script = "";
                    String thenActionScript = "";
                    boolean processField = true;

                    tempField.setAccessible(true);

                    boolean isMandate = false;
                    if (null != mandateFieldMap && mandateFieldMap.containsKey(tempField.getDeclaringClass())) {
                        if (mandateFieldMap.get(tempField.getDeclaringClass()).indexOf(tempField.getName()) != -1) {
                            isMandate = true;
                        }
                    }

                    if ((null != ignorableFieldMap && null != ignorableFieldMap.get(tempField.getDeclaringClass()) && ignorableFieldMap
                            .get(tempField.getDeclaringClass()).indexOf(tempField.getName()) != -1)
                            || (null != ignoreCommonFields && ignoreCommonFields.contains(tempField.getName()))) {
                        processField = false;
                    }

                    if (null != onlyUseFields && null != onlyUseFields.get(tempField.getDeclaringClass())
                            && onlyUseFields.get(tempField.getDeclaringClass()).indexOf(tempField.getName()) == -1) {
                        processField = false;
                    }

                    String baseOgnl = "";
                    if (processField) {
                        String toOgnl = ognl;
                        String ognlWithField = ognl + "[" + baseListCount + "]" + ".?" + tempField.getName();
                        ognlWithField = ognlWithField.replace(ComparisonConstants.BASE_OBJECT,
                                ComparisonConstants.TARGET_OBJECT);

                        String ognlWthtField = ognl + "[" + baseListCount + "]";
                        ognlWthtField = ognlWthtField.replace(ComparisonConstants.BASE_OBJECT,
                                ComparisonConstants.TARGET_OBJECT);

                        baseOgnl = ognl + "[" + baseListCount + "]" + ".?" + tempField.getName();

                        toOgnl = toOgnl.replace(ComparisonConstants.BASE_OBJECT, ComparisonConstants.COMPARE_TO_OBJECT);
                        String andCond = "";

                        for (int i = 0 ; i < compareToList.size() ; i++) {

                            String newToOgnl = toOgnl + "[" + i + "]" + ".?" + tempField.getName();
                            andCond = andCond + baseOgnl + " != " + newToOgnl;
                            if (i < compareToList.size() - 1) {
                                andCond = andCond + " && ";
                            }
                        }

                        script = script + "" + ognlWthtField + " != null && " + " (" + andCond + ")";
                        if (isMandate) {
                            thenActionScript = ognlWthtField + " = " + returnActualType(tempField);
                        } else {
                            thenActionScript = ognlWithField + " = " + returnActualType(tempField);
                        }

                        ScriptFieldOgnl scriptOgnl = new ScriptFieldOgnl();
                        scriptOgnl.setFieldOgnl(baseOgnl);
                        scriptOgnl.setScript(script);
                        scriptOgnl.setThenActionScript(thenActionScript);
                        ognls.add(scriptOgnl);
                    }
                }
                baseListCount++;
            }

        } else {
            ScriptFieldOgnl scriptOgnl = new ScriptFieldOgnl();
            scriptOgnl.setFieldOgnl("");
            scriptOgnl.setScript("true");
            ognl = ognl.replace(ComparisonConstants.BASE_OBJECT, ComparisonConstants.TARGET_OBJECT);
            scriptOgnl.setThenActionScript(ognl + " = null");
            ognls.add(scriptOgnl);
        }
    }

    public void compareValuesOftwoObjects(Field currentField, Object base, Object compareTo, String ognl,
            List<ScriptFieldOgnl> ognls) throws IllegalArgumentException, IllegalAccessException {

        ScriptFieldOgnl scriptOgnl = new ScriptFieldOgnl();
        String fromOgnl = ognl;
        String toOgnl = ognl.replace(ComparisonConstants.BASE_OBJECT, ComparisonConstants.COMPARE_TO_OBJECT);
        String targetOgnl = ognl.replace(ComparisonConstants.BASE_OBJECT, ComparisonConstants.TARGET_OBJECT);
        scriptOgnl.setFieldOgnl(ognl);
        scriptOgnl.setScript(fromOgnl + " != " + toOgnl);
        String thenActionScript = targetOgnl + " = " + returnActualType(currentField);
        scriptOgnl.setThenActionScript(thenActionScript);
        ognls.add(scriptOgnl);
    }

    private Object returnActualType(Field currentField) {

        if (currentField.getGenericType().equals(boolean.class)) {
            return false;
        } else if (currentField.getGenericType().equals(int.class)) {
            return 0;
        }

        return null;
    }
}
