package com.nucleus.cas.parentChildDeletionHandling;

import com.nucleus.config.persisted.service.ConfigurationService;
import com.nucleus.config.persisted.vo.ConfigurationVO;
import com.nucleus.core.genericparameter.entity.GenericParameter;
import com.nucleus.core.initialization.ProductInformationLoader;
import com.nucleus.dao.query.JPAQueryExecutor;
import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.SystemEntity;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.master.BaseMasterEntity;
import com.nucleus.persistence.EntityDao;
import com.nucleus.service.BaseServiceImpl;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.Transient;
import javax.persistence.metamodel.EntityType;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class BaseMasterDependency extends BaseServiceImpl {

    @Inject
    @Named("configurationService")
    private ConfigurationService configurationService;

    @Inject
    @Named("entityDao")
    protected EntityDao entityDao;


    private static ConfigurationService staticConfig;

    private static EntityDao staticDao;


    public static final String APPROVAL_STATUS = "AND e.masterLifeCycleData.approvalStatus not in (1,5,10)";

    private static ConcurrentHashMap<Class, Map<Class, String>> dependencyGraph = new ConcurrentHashMap();

    private static ConcurrentHashMap<Class, Map<Class, String>> reverseDependencyGraph = new ConcurrentHashMap();

    private static List<Map<Class,Class>> childParentExclusionMapList = new ArrayList<>();

    public ConcurrentMap getDependencyGraph() {
        return dependencyGraph;
    }

    public static Map<Class,String> getDependencyGraphForEntity(Class rootClass){
        if(rootClass!=null){
            return reverseDependencyGraph.get(rootClass);
        }
        return null;
    }

    public static void addToExclusions(Class child, Class parent){
        Map<Class, Class> childParentExclusionMap = new HashMap<>();
        childParentExclusionMap.put(child,parent);
        childParentExclusionMapList.add(childParentExclusionMap);
    }

    private void prepareExclusions(){

    }

    public static void removeExclusionsFromReverseDependencyMap(){
        if(CollectionUtils.isNotEmpty(childParentExclusionMapList)){
            for(Map<Class,Class> childParentExclusionMap : childParentExclusionMapList){
                for(Map.Entry<Class,Class> me : childParentExclusionMap.entrySet()){
                    Class childCls = me.getKey();
                    Class parentCls = me.getValue();
                    removeParentMapping(childCls,parentCls);
                }
            }
        }
    }

    private static void removeParentMapping(Class childCls, Class parentCls){
        Map<Class,String> parentHqlMap = reverseDependencyGraph.get(childCls);
        if(MapUtils.isNotEmpty(parentHqlMap)){
            if(parentHqlMap.containsKey(parentCls))
                parentHqlMap.remove(parentCls);
        }
    }


    public static void addMapEntry(Class childClass, Class parentClass, String hql){
        if(MapUtils.isNotEmpty(reverseDependencyGraph)){
            if(reverseDependencyGraph.containsKey(childClass)){
                Map<Class,String> parentHqlMap = reverseDependencyGraph.get(childClass);
                if(MapUtils.isNotEmpty(parentHqlMap)){
                    parentHqlMap.put(parentClass,hql);
                }else{
                    parentHqlMap= new HashMap<>();
                    parentHqlMap.put(parentClass,hql);
                    reverseDependencyGraph.put(childClass,parentHqlMap);
                }
            }else{
                Map<Class,String> parentHqlMap = new HashMap<>();
                parentHqlMap.put(parentClass,hql);
                reverseDependencyGraph.put(childClass,parentHqlMap);
            }
        }
    }

    public static Boolean isConfigPresent(){
        Boolean isChildDeletionCheckEnabled = false;
        try {
            String productName = ProductInformationLoader.getProductName();
            if (productName == null || productName.isEmpty()) {
                BaseLoggers.exceptionLogger.error("No product name found");
                return isChildDeletionCheckEnabled;
            }
            String propertyKey = "config." + productName + ".childDeletionCheck.enable";
            ConfigurationVO configurationVO = staticConfig.getConfigurationPropertyFor(SystemEntity.getSystemEntityId(), propertyKey);
            if (configurationVO == null || configurationVO.getPropertyValue() == null || BooleanUtils.toBooleanObject(configurationVO.getPropertyValue()) == null) {
                BaseLoggers.exceptionLogger.error("Exception occured during entering values in dependency map: Property value missing or invalid for : " + propertyKey);
                return isChildDeletionCheckEnabled;
            }
            String childDeletionCheckPropertyValue = configurationVO.getPropertyValue();
            if (BooleanUtils.toBooleanObject(childDeletionCheckPropertyValue) != null) {
                isChildDeletionCheckEnabled = BooleanUtils.toBooleanObject(childDeletionCheckPropertyValue);
            }
        }catch(Exception e){
            BaseLoggers.exceptionLogger.error("Exception Occured : ",e);
        }
        return isChildDeletionCheckEnabled;
    }

    public static Boolean isDependencyPresent(Class childClass, Long id)
    {
        Boolean isDepencyPresent = false;
        DeletionPreValidator deletionPreValidator = AnnotationUtils.findAnnotation(childClass, DeletionPreValidator.class);
        if(deletionPreValidator!=null){
            Map<Class, String> entityValidator = BaseMasterDependency.getDependencyGraphForEntity(childClass);
            if (MapUtils.isNotEmpty(entityValidator)) {
                for (Map.Entry m : entityValidator.entrySet()) {
                    String query = (String) m.getValue();
                    JPAQueryExecutor jpaQueryExecutor = new JPAQueryExecutor(query);
                    jpaQueryExecutor.addParameter("id", id);
                    List<Object> list = staticDao.executeQuery(jpaQueryExecutor);
                    if (CollectionUtils.isNotEmpty(list)) {
                        isDepencyPresent = true;
                        break;
                    }
                }
            }
        }
        return isDepencyPresent;
    }


    public void prepareReverseDependencyMap(){
        if(MapUtils.isNotEmpty(dependencyGraph)){
            for(Map.Entry map : dependencyGraph.entrySet()){
                Class cls = (Class) map.getKey();
                Map<String, String>  m2 = (Map) map.getValue();
                for(Map.Entry columnMap : m2.entrySet()){
                    if(reverseDependencyGraph.containsKey(columnMap.getKey())){
                        Map<Class, String> newMap =reverseDependencyGraph.get(columnMap.getKey());
                        newMap.put(cls,(String)columnMap.getValue());
                    }else{
                        Map<Class, String> newMap = new HashMap<>();
                        newMap.put(cls,(String)columnMap.getValue());
                        reverseDependencyGraph.put((Class)columnMap.getKey(),newMap);
                    }
                }
            }
        }
    }

    @PostConstruct
    public void init(){
        BaseMasterDependency.staticConfig = configurationService;
        BaseMasterDependency.staticDao=entityDao;
        Boolean isChildDeletionCheckEnabled = isConfigPresent();
        try {
            prepareExclusions();
            if (isChildDeletionCheckEnabled) {
                Set<EntityType<?>> entities = entityDao.getEntityManager().getEntityManagerFactory().getMetamodel().getEntities();
                for (EntityType<?> entity : entities) {
                    Class cls = entity.getJavaType();
                    Class cls1 = cls.getSuperclass();
                    while (cls1 != null && cls1 != Object.class && cls1 != BaseEntity.class && cls1 != GenericParameter.class) {
                        if (cls1.equals(BaseMasterEntity.class)) {
                            Map<Class, String> columnMap = new HashMap<>();
                            Field[] fields = cls.getDeclaredFields();
                            for (Field field : fields) {
                                field.setAccessible(true);
                                try {
                                    initializeMap(field, columnMap, cls.getSimpleName());
                                } catch (Exception e) {
                                    throw new RuntimeException("Exception occured during extracting dependency map for entity : " + cls.getSimpleName() + " at field :" + field.getName() + ":" + e.getMessage());
                                }
                            }
                            if (MapUtils.isNotEmpty(columnMap)) {
                                dependencyGraph.putIfAbsent(cls, columnMap);
                            }
                            break;
                        } else {
                            cls1 = cls1.getSuperclass();
                        }
                    }
                }
                prepareReverseDependencyMap();
                removeExclusionsFromReverseDependencyMap();

            }
        } catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }
    }

    private void initializeMap(Field field, Map<Class, String> columnMap, String className){
        if(field.getType()!=null){
            boolean isCollection = Collection.class.isAssignableFrom(field.getType());
            if(isCollection){
                ParameterizedType listType = (ParameterizedType) field.getGenericType();
                if (listType.getActualTypeArguments()[0] instanceof Class) {
                    Class listClass = (Class) listType.getActualTypeArguments()[0];
                    Class superCls = listClass.getSuperclass();
                    while(superCls!=null && superCls!=Object.class  && superCls!=GenericParameter.class) {
                        if (superCls.equals(BaseMasterEntity.class)) {
                            columnMap.putIfAbsent(listClass, prepareHQL(field, className, true));
                            break;
                        }else if(superCls.equals(BaseEntity.class)){
                            thirdLevelCheck(columnMap,field,listClass,className,true);
                            break;
                        }else{
                            superCls = superCls.getSuperclass();
                        }
                    }
                }
            }
            boolean isMap = Map.class.isAssignableFrom(field.getType());
            if(isMap){
                ParameterizedType listType = (ParameterizedType) field.getGenericType();
                for(int i=0; i<listType.getActualTypeArguments().length ; i++ ){
                    Class listClass = (Class) listType.getActualTypeArguments()[i];
                    if(listClass.getSuperclass()!=null && listClass.getSuperclass().equals(BaseMasterEntity.class)){
                        columnMap.putIfAbsent(listClass,prepareHQL(field,className,true));
                    }
                }
            }
            if(field.getType().getSuperclass()!=null){
                Class superCls=field.getType().getSuperclass();
                while(superCls!=null && superCls!=Object.class && superCls!=GenericParameter.class) {
                    if (superCls.equals(BaseMasterEntity.class)) {
                        columnMap.putIfAbsent(field.getType(), prepareHQL(field, className, false));
                        break;
                    }else if(superCls.equals(BaseEntity.class)){
                        thirdLevelCheck(columnMap,field,field.getType(),className,false);
                        break;
                    }else{
                        superCls=superCls.getSuperclass();
                    }
                }
            }
        }
    }

    private void thirdLevelCheck(Map<Class, String> columnMap,Field firstLevelField,Class firstLevelFieldClass,String className,boolean isCollectionFirstLevel){
        Field[] fields = firstLevelFieldClass.getDeclaredFields();
        if(fields.length!=0) {
            for (Field field : fields) {
                if (field.getType() != null && field.getType().getSuperclass() != null && field.getType().getSuperclass() != Object.class && field.getType().getSuperclass() != GenericParameter.class && field.getType() != GenericParameter.class) {
                    boolean isCollectionSecondLevel = Collection.class.isAssignableFrom(field.getType());
                    if (isCollectionSecondLevel) {
                        ParameterizedType listType = (ParameterizedType) field.getGenericType();
                        if (listType.getActualTypeArguments()[0] instanceof Class) {
                            Class listClass = (Class) listType.getActualTypeArguments()[0];
                            if (listClass.getSuperclass().equals(BaseMasterEntity.class)) {
                                columnMap.putIfAbsent(listClass,prepareHQL(firstLevelField,field,className,isCollectionFirstLevel,true));
                            }
                        }
                    } else if (field.getType().getSuperclass() != null && field.getType().getSuperclass().equals(BaseMasterEntity.class)) {
                        columnMap.putIfAbsent(field.getType(),prepareHQL(firstLevelField,field,className,isCollectionFirstLevel,false));
                    }
                }
            }
        }
    }

    private String prepareHQL(Field field, String className, boolean isCollection){
        StringBuilder hql = new StringBuilder();
        if(!isCollection){
            hql.append("SELECT e FROM "+className+" e WHERE e."+field.getName()+ ".id =:id " + APPROVAL_STATUS);
            return hql.toString();
        }else{
            hql.append("SELECT e FROM "+className+" e inner join e."+field.getName()+ " fd WHERE fd.id =:id " + APPROVAL_STATUS);
            return hql.toString();
        }
    }

    private String prepareHQL(Field firstLevelField, Field secondLevelField, String className, boolean isCollectionFirstLevel, boolean isCollectionSecondLevel){
        StringBuilder hql = new StringBuilder();
        hql.append("SELECT e FROM "+className+" e ");
        if(!isCollectionFirstLevel){
            if(!isCollectionSecondLevel){
                hql.append("WHERE e."+firstLevelField.getName()+"."+secondLevelField.getName()+".id =:id " + APPROVAL_STATUS);
            }else{
                hql.append("inner join e."+firstLevelField.getName()+"."+secondLevelField.getName()+" fd WHERE fd.id =:id " + APPROVAL_STATUS);
            }
        }else{
            if(!isCollectionSecondLevel){
                hql.append("inner join e."+firstLevelField.getName()+" e1 where e1."+secondLevelField.getName()+".id =:id "+  APPROVAL_STATUS);
            }else{
                hql.append("inner join e."+firstLevelField.getName()+" e1 inner join "+"e1."+secondLevelField.getName()+" fd WHERE fd.id =:id " + APPROVAL_STATUS);
            }
        }
        return hql.toString();
    }

}