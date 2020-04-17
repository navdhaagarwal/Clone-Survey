package com.nucleus.rules.eventdefinition.service;

import static com.nucleus.finnone.pro.general.util.ValidatorUtils.hasElements;
import static com.nucleus.finnone.pro.general.util.ValidatorUtils.notNull;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Named;
import javax.persistence.Entity;
import javax.persistence.EntityManagerFactory;
import javax.persistence.metamodel.EntityType;

import org.hibernate.FlushMode;

import com.nucleus.core.event.EventDefinition;
import com.nucleus.core.event.EventTask;
import com.nucleus.core.validation.util.NeutrinoValidator;
import com.nucleus.dao.query.NamedQueryExecutor;
import com.nucleus.entity.ApprovalStatus;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.query.constants.QueryHint;
import com.nucleus.rules.model.Rule;
import com.nucleus.rules.model.RuleActionMapping;
import com.nucleus.rules.model.RuleGroup;
import com.nucleus.rules.model.RuleInvocationMapping;
import com.nucleus.rules.model.RuleSetActionMapping;
import com.nucleus.rules.model.eventDefinition.RuleInvocationMappingTask;
import com.nucleus.rules.model.eventDefinition.RuleValidationTask;
import com.nucleus.rules.service.BaseRuleServiceImpl;

/**
 * @author rohit.singh
 *
 */
@Named(value = "eventDefinitionService")
public class EventDefinitionServiceImpl extends BaseRuleServiceImpl implements
        EventDefinitionService {
	
	private static Map<String,Class<?>> entityMap = new HashMap<>();
	
	private static final String START_BRACKET = "( ";
	private static final String END_BRACKET = " )";
	private static final String EMPTY = "";
	private static final String QUESTION_CHAR = "\\?";
	private static final String DOT = ".";
	private static final String DOT_CHAR = "\\.";
	
    @Override
    public EventDefinition getEventDefinitionByCode(String code) {
        List<Integer> statusList = new ArrayList<>();
        statusList.add(ApprovalStatus.APPROVED);
        statusList.add(ApprovalStatus.APPROVED_MODIFIED);
        NamedQueryExecutor<EventDefinition> evNamedQueryExecutor = new NamedQueryExecutor<EventDefinition>(
                "EventDefinition.GetByCode")
                .addParameter("code", code)
                .addParameter("statusList", statusList)
                .addQueryHint(QueryHint.QUERY_HINT_CACHEABLE, Boolean.TRUE)
                .addQueryHint(QueryHint.QUERY_HINT_FLUSHMODE, FlushMode.COMMIT);

        List<EventDefinition> eventDefinitionList = entityDao
                .executeQuery(evNamedQueryExecutor);
        if (!eventDefinitionList.isEmpty())
            return eventDefinitionList.get(0);
        else
            return null;
    }

    @Override
    public void sortEventTaskList(List<EventTask> eventTaskList) {

        Collections.sort(eventTaskList, new Comparator<EventTask>() {
            @Override
            public int compare(EventTask o1, EventTask o2) {

                if (o1.getTaskSequence() == null
                        && o2.getTaskSequence() == null) {
                    return 0;
                }

                else if (o1.getTaskSequence() == null) {
                    return 1;
                }

                else if (o2.getTaskSequence() == null) {
                    return -1;
                }

                else if (o1.getTaskSequence() == o2.getTaskSequence()) {
                    return 0;
                }

                return o1.getTaskSequence() < o2.getTaskSequence() ? -1 : 1;
            }
        });

    }

    /**
     * Returns Set of object graphs for any event code
     * @param eventExecutionPoint
     * @return
     */
    private Set<String> getObjectGraphsFromEventDefinition(
            String eventExecutionPoint) {
        EventDefinition eventDefinition = null;
        eventDefinition = getEventDefinitionByCode(eventExecutionPoint);
        List<EventTask> eventTaskList = eventDefinition.getEventTaskList();
        List<EventTask> filteredTaskLists = getAllRuleInvocationAndValidationTasks(eventTaskList);
        Set<String> objectGraphs = new HashSet<>();
        if (hasElements(filteredTaskLists)) {
            for (EventTask eventTask : filteredTaskLists) {
            	
            	objectGraphs = getObjectGraphsForFilteredEventTask(eventTask);
            }
        }
        return objectGraphs;
    }

    
    
    
    /**
     * Checks the instance of RuleInvocationMappingTask or RuleValidationTask 
     * task and gets the object graph accordingly.
     * @param eventTask
     * @return
     */
    private Set<String>  getObjectGraphsForFilteredEventTask(EventTask eventTask){
    	Set<String> objectGraphs = new HashSet<>();
    	if (eventTask instanceof RuleInvocationMappingTask) {
            RuleInvocationMapping ruleInvocationMapping = ((RuleInvocationMappingTask) eventTask)
                    .getRuleInvocationMapping();
            if (hasElements(ruleInvocationMapping.getRuleMapping())) {
                objectGraphs
                        .addAll(getObjectGraphsFromRuleAction(ruleInvocationMapping));
            }
            if (hasElements(ruleInvocationMapping.getRulesetMapping())) {
                objectGraphs
                        .addAll(getObjectGraphsFromRuleSetAction(ruleInvocationMapping));
            }
            if (notNull(ruleInvocationMapping.getRuleGroup())) {
                objectGraphs
                        .addAll(getObjectGraphsFromRuleGroup(ruleInvocationMapping
                                .getRuleGroup()));
            }
            if (notNull(ruleInvocationMapping.getCriteriaRules())
                    && notNull(ruleInvocationMapping.getCriteriaRules()
                            .getRuleGroup())) {
                objectGraphs
                        .addAll(getObjectGraphsFromRuleGroup(ruleInvocationMapping
                                .getCriteriaRules().getRuleGroup()));
            }
        
    	}
    	if (eventTask instanceof RuleValidationTask) {
    		RuleGroup ruleGroup = ((RuleValidationTask) eventTask).getRuleGroup();
    		 if (notNull(ruleGroup)) {
                 objectGraphs
                         .addAll(getObjectGraphsFromRuleGroup(ruleGroup));
             }
    	}
    	return objectGraphs;
    }
   
    
    
    /**
     * Invoke method for the list of object graphs to consolidate final data
     * into map.
     * @param objectGraphs
     * @return
     */
    private  Map<String,List> getRootContextObjectFromObjectGraphs(
            Set<String> objectGraphs)  {
	 Map<String,List> mapOfObjectGraphs = new HashMap<>();
        Iterator<String> iterator = objectGraphs.iterator();
        while (iterator.hasNext()) {
            String[] parts = iterator.next().split(QUESTION_CHAR);
            
            if(parts[1].contains("contextObject")){
            	int lastIndex= parts[1].lastIndexOf(DOT);
            	String rootElement =  null;
            	if(lastIndex!=0 && lastIndex == parts[1].length()-1){
            		rootElement = parts[1].substring(13, parts[1].length() - 1);
            	}else{
            		rootElement = parts[1].substring(13, parts[1].length());
            	}
            	createMapData(parts, rootElement, mapOfObjectGraphs,true);
            }
        }
       return mapOfObjectGraphs;
    }
    
    
    /**
     * Method to create the final leaf elements 
     * @param leaf
     * @param parts
     * @param i
     */
    private void prepareLeafObjects(StringBuilder leaf,String[] parts,int i){
    	leaf.append((parts[i]));
 		if(leaf.length()-1 != leaf.lastIndexOf(DOT)){
 			leaf.append(DOT);
 		}
    }
    
    /**
     * @param mapOfObjectGraphs
     * @param rootElement
     * @param list
     */
    private void createFinalDataMap(Map<String,List> mapOfObjectGraphs,String rootElement, List<String> list){
    	mapOfObjectGraphs.put(rootElement,list);
    }
    
    
    /**
     * Returns the simple name of next coming field
     * @param rootElement
     * @param newParts
     * @return
     */
    private String getSimpleNameOfNextField(String rootElement,String[] newParts){
    	 try {
				Field field = getClassObject(rootElement).getDeclaredField(newParts[0]);
				String fullyQualifiedNameOfField = field.getType().getName();
				int lastIndexOfDot = fullyQualifiedNameOfField.lastIndexOf(DOT);
				if(lastIndexOfDot > 1){
					return fullyQualifiedNameOfField.substring(lastIndexOfDot+1, fullyQualifiedNameOfField.length());
				}
			} catch (NoSuchFieldException e) {
				BaseLoggers.exceptionLogger.error("NoSuchFieldException %s",e);
				return EMPTY;
			} catch (SecurityException e) {
				BaseLoggers.exceptionLogger.error("SecurityException %s",e);
			} catch (Exception e) {
				BaseLoggers.exceptionLogger.error("Exception %s",e);
				return EMPTY;
			} 
    	 
    	 return EMPTY;
    }
    
    
    /**
     * Prepares the leaf-object .
     * Delegate call to prepareLeafObjects method for each part
     * @param parts
     * @param s
     * @param firstCall
     */
    private void callToPrepareLeafObject(String[] parts, StringBuilder s,boolean firstCall){
    	if(firstCall){
          	 for(int i=0;i<parts.length;i++){
                	if(i!=0 && i!=1){
                		prepareLeafObjects(s, parts, i);             		
                	}	                	
                }
           }else{
          	 for(int i=0;i<parts.length;i++){
                 	if(i!=0){
                 		prepareLeafObjects(s, parts, i);                		
                 	}	                	
                 }
           }
    }
    
    
    /**
     * Method responsible to create the final map data of object-graphs
     * @param parts
     * @param rootElement
     * @param mapOfObjectGraphs
     * @param firstCall
     */
    private  void createMapData(String[] parts,String rootElement,Map<String,List> mapOfObjectGraphs ,boolean firstCall){
		 StringBuilder s = new StringBuilder();
        List<String> list = new ArrayList<>();        
        
        callToPrepareLeafObject(parts, s, firstCall);
		 
		 String s1 = "";
		 if(s.length()>0){
			 s1 = s.substring(0, s.length() - 1);
		 }
        
        if(mapOfObjectGraphs.containsKey(rootElement)){
        	List<String> l = mapOfObjectGraphs.get(rootElement);
        	if(!l.contains(s1)){
        		l.add(s1);
        	}         	
        	createFinalDataMap(mapOfObjectGraphs, rootElement, l);
        }else{
        	list.add(s1);
        	createFinalDataMap(mapOfObjectGraphs, rootElement, list);
        }
        
        String[] newParts = s1.split(DOT_CHAR);
        while(newParts.length > 1){       	 
       	 
       	 String simpleNameOfField = "";
       	 if(isEntity(rootElement)){ 
       		simpleNameOfField = getSimpleNameOfNextField(rootElement, newParts);
          }
       	 
       	 if(simpleNameOfField == null || EMPTY.equals(simpleNameOfField)){
       		 return;
       	 }
       	 if(!isEntity(simpleNameOfField)){
       		 return;
       	 }
       	 
       	 createMapData(newParts, simpleNameOfField, mapOfObjectGraphs,false);
       	 
       	 StringBuilder sb = new StringBuilder();
       	 for(int i=0; i<newParts.length;i++){
       		 if(i!=0){
       			 sb.append(newParts[i]);
       			 sb.append(DOT);
       		 }
       	 }
       	 String ss = sb.substring(0, sb.length() - 1);       	 
       	 newParts = ss.split(DOT_CHAR);
        }
	 }
    
    
    /**
     * Checks if given String name is an Entity or not.
     * @param name
     * @return true/false
     */
    private boolean isEntity(String name){		
		 return entityMap.containsKey(name);
	 }
    
    
    /**
     * Returns the entity class object from the entity Map
     * @param name
     * @return Class
     */
    private Class getClassObject(String name) {
		return entityMap.get(name);
	}
    
    /**
     * Fetches object graphs from RuleAction and create a HashSet.
     * @param ruleInvocationMapping
     * @return Set
     */
    private Set<String> getObjectGraphsFromRuleAction(
            RuleInvocationMapping ruleInvocationMapping) {
        Set<String> objectGraphs = new HashSet<>();
        for (RuleActionMapping ruleActionMapping : ruleInvocationMapping
                .getRuleMapping()) {
        	objectGraphs.addAll(getObjectGraphsFromRule(ruleActionMapping.getRule()));
        }
        return objectGraphs;
    }
    
    /**
     * From RuntimeRuleMapping fetches object graph for each rule
     * Trim the string value
     * @param rule
     * @return
     */
    private Set<String> getObjectGraphsForRule(Rule rule){
    	if(rule.getRuntimeRuleMapping()!=null){
    		Set<String> objectGraphsInDB = rule.getRuntimeRuleMapping().getObjectGraphs();
    		Set<String> trimmedObjectGraphsInDB = new HashSet<>();
    		for(String objectGraphInDB : objectGraphsInDB){
    			objectGraphInDB=objectGraphInDB.replace(START_BRACKET, EMPTY);
    			objectGraphInDB=objectGraphInDB.replace(END_BRACKET, EMPTY);
    			objectGraphInDB=objectGraphInDB.trim();
    			trimmedObjectGraphsInDB.add(objectGraphInDB);
    		}
    		return trimmedObjectGraphsInDB;
    	}
    	return new HashSet<>();
    }
    
    /**
     * Fetches object graphs from Rule and create a HashSet.
     * @param rule
     * @return
     */
    private Set<String> getObjectGraphsFromRule(Rule rule){
    	 Set<String> objectGraph = new HashSet<>();
    	if (rule.getRuleType() == 1) {
            objectGraph.addAll(getObjectGraphsForRule(rule));
        }
    	
    	return objectGraph;
    }

    /**
     * Fetches object graphs from RuleSetAction and create a HashSet.
     * @param ruleInvocationMapping
     * @return
     */
    private Set<String> getObjectGraphsFromRuleSetAction(
            RuleInvocationMapping ruleInvocationMapping) {
        Set<String> objectGraphs = new HashSet<>();
        for (RuleSetActionMapping ruleSetActionMapping : ruleInvocationMapping
                .getRulesetMapping()) {
            for (Rule rule : ruleSetActionMapping.getRuleSet().getRules()) {
                if (rule.getRuleType() == 1) {
                    objectGraphs.addAll(getObjectGraphsForRule(rule));
                }
            }
        }
        return objectGraphs;
    }

    /**
     * Fetches object graphs from Rule Group and create a HashSet. 
     * @param ruleGroup
     * @return
     */
    private Set<String> getObjectGraphsFromRuleGroup(RuleGroup ruleGroup) {
        Set<String> objectGraphs = new HashSet<>();
        for (Rule rule : ruleGroup.getRules()) {
            if (rule.getRuleType() == 1) {
                objectGraphs.addAll(getObjectGraphsForRule(rule));
            }
        }
        return objectGraphs;
    }
    

    /**
     * Create list for all RuleInvocationMappingTask and RuleValidationTask
     * @param eventTaskList
     * @return
     */
    private List<EventTask> getAllRuleInvocationAndValidationTasks(
            List<EventTask> eventTaskList) {
        List<EventTask> taskLists = new ArrayList<>();
        if (hasElements(eventTaskList)) {
            for (EventTask eventTask : eventTaskList) {
                if (eventTask instanceof RuleInvocationMappingTask) {
                	taskLists.add(eventTask);
                }
                
                if (eventTask instanceof RuleValidationTask) {
                	taskLists.add(eventTask);
                }
            }
        }
        return taskLists;
    }
    

    
    /**
     * Load the list of available entities
     */
    private void loadEntities(){
		prepareDataToProcess(entityDao.getEntityManager().getEntityManagerFactory());
    }
    
    
    
  
  	/**
  	 * Method responsible to create Map which holds data of Entities present .
  	 * 
  	 * @param EntityManagerFactory emf
  	 */
  	private void prepareDataToProcess(EntityManagerFactory emf) {
  		Set<EntityType<?>> entities = emf.getMetamodel().getEntities();
  		
  		if(!entityMap.isEmpty()){
  			return;
  		}
  		 for (EntityType<?> en : entities) {
  			 Class<?> clas = en.getJavaType();
  			if(clas.isAnnotationPresent(Entity.class)){
  				entityMap.put(en.getName(), clas);
  			}
  		 }
  	}
  	
  	/**
	 * This function takes event-code as input and returns 
	 * a map of object graphs associated with it in  
	 * predefined pattern.
	 * 
	 * @param eventCode
     * @return 
	 */
    @Override
    public Map<String, List> getRootContextObjectFromEventCode(String eventCode) {
    	NeutrinoValidator.notNull(eventCode,"EventCode can not be null.");
    	loadEntities();
    	BaseLoggers.exceptionLogger.debug("===========================================================");
        Set<String> objectGraphs = getObjectGraphsFromEventDefinition(eventCode);
        BaseLoggers.exceptionLogger.debug("Object Graphs associated with : %s",eventCode ," are ::::%s",objectGraphs);
        return getRootContextObjectFromObjectGraphs(objectGraphs);
    }
    
    
    /**
     * 
     * Takes list of rules as input and returns 
	 * a map of object graphs associated with it in  
	 * predefined pattern.
	 * 
     * @param rules
     * @return
     */
    @Override
	public Map<String,List> getRootContextObjectFromRule(List<Rule> rules) {
    	NeutrinoValidator.notNull(rules,"Rules can not be null.");
    	Map<String,List> mapOfObjectGraphs = new HashMap<>();
		for (Rule rule : rules) {
			Set<String> objectGraphsForRule = getObjectGraphsFromRule(rule);
			mapOfObjectGraphs.putAll(getRootContextObjectFromObjectGraphs(objectGraphsForRule));
		}
		return mapOfObjectGraphs;
	}

    
    
}
