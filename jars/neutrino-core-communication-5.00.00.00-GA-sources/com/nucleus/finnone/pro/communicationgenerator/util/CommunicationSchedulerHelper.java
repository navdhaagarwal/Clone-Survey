package com.nucleus.finnone.pro.communicationgenerator.util;

import static com.nucleus.finnone.pro.communicationgenerator.constants.CommunicationSchedulerConstants.ALL;
import static com.nucleus.finnone.pro.communicationgenerator.constants.CommunicationSchedulerConstants.DAYOFWEEKLIST;
import static com.nucleus.finnone.pro.communicationgenerator.constants.CommunicationSchedulerConstants.DAYOFWEEKLISTFORCOMPACTCRON;
import static com.nucleus.finnone.pro.communicationgenerator.constants.CommunicationSchedulerConstants.DESCRIPTION;
import static com.nucleus.finnone.pro.communicationgenerator.constants.CommunicationSchedulerConstants.EVERY_DESCRIPTION;
import static com.nucleus.finnone.pro.communicationgenerator.constants.CommunicationSchedulerConstants.EVERY_ID;
import static com.nucleus.finnone.pro.communicationgenerator.constants.CommunicationSchedulerConstants.FIFTH;
import static com.nucleus.finnone.pro.communicationgenerator.constants.CommunicationSchedulerConstants.FIRST;
import static com.nucleus.finnone.pro.communicationgenerator.constants.CommunicationSchedulerConstants.FOURTH;
import static com.nucleus.finnone.pro.communicationgenerator.constants.CommunicationSchedulerConstants.MONTHLIST;
import static com.nucleus.finnone.pro.communicationgenerator.constants.CommunicationSchedulerConstants.MONTHLISTCOMPACTCRON;
import static com.nucleus.finnone.pro.communicationgenerator.constants.CommunicationSchedulerConstants.NO_SPECIFIC_VALUE;
import static com.nucleus.finnone.pro.communicationgenerator.constants.CommunicationSchedulerConstants.ONETOSIXTY;
import static com.nucleus.finnone.pro.communicationgenerator.constants.CommunicationSchedulerConstants.ONETOTHIRTYONE;
import static com.nucleus.finnone.pro.communicationgenerator.constants.CommunicationSchedulerConstants.ONETOTWENTYFOUR;
import static com.nucleus.finnone.pro.communicationgenerator.constants.CommunicationSchedulerConstants.QUESTION_MARK;
import static com.nucleus.finnone.pro.communicationgenerator.constants.CommunicationSchedulerConstants.SECOND;
import static com.nucleus.finnone.pro.communicationgenerator.constants.CommunicationSchedulerConstants.SELECTED_VALUE_ID;
import static com.nucleus.finnone.pro.communicationgenerator.constants.CommunicationSchedulerConstants.SELECTPATTERN;
import static com.nucleus.finnone.pro.communicationgenerator.constants.CommunicationSchedulerConstants.SMALL_ID;
import static com.nucleus.finnone.pro.communicationgenerator.constants.CommunicationSchedulerConstants.SPECIFIC_VALUES;
import static com.nucleus.finnone.pro.communicationgenerator.constants.CommunicationSchedulerConstants.STAR;
import static com.nucleus.finnone.pro.communicationgenerator.constants.CommunicationSchedulerConstants.THIRD;
import static com.nucleus.finnone.pro.communicationgenerator.constants.CommunicationSchedulerConstants.WEEK_OF_MONTH;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.ui.ModelMap;

import com.nucleus.core.genericparameter.entity.GenericParameter;
import com.nucleus.core.genericparameter.service.GenericParameterService;
import com.nucleus.finnone.pro.general.domainobject.DaysOfWeekType;
import com.nucleus.finnone.pro.general.domainobject.MonthsOfYearType;


@Named("communicationSchedulerHelper")
public class CommunicationSchedulerHelper {
	
	@Inject
	@Named("genericParameterService")
	private GenericParameterService genericParameterService;

	public void getBasicInitParameters(ModelMap map){
		 	 	
		map.put(ONETOSIXTY, createOneToSixty());
	 	map.put(ONETOTWENTYFOUR, createOneToTwentyFour());
	 	map.put(ONETOTHIRTYONE, createOneToThirtyOne());
	 	map.put(DAYOFWEEKLIST, createDayOfWeekList());
	 	map.put(MONTHLIST, createMonthList());
	 	map.put(SELECTPATTERN, createListOfPattern());
	 	map.put(DAYOFWEEKLISTFORCOMPACTCRON, createWeekdaysListForCompactCron());
	 	map.put(MONTHLISTCOMPACTCRON, createMonthListForCompactCron());
	 	map.put(WEEK_OF_MONTH, createWeekOfMonthList());
	 	
 }
	
	public List createListOfPattern(){
	    	List<Map<String,String>> listOfValues = new ArrayList<Map<String,String>>();
	    	
			Map<String,String> everyMap = new HashMap<String,String>();
			everyMap.put(SMALL_ID, EVERY_ID);
			everyMap.put(DESCRIPTION, EVERY_DESCRIPTION);
			listOfValues.add(everyMap);
			
			Map<String,String> selectedValuesMap = new HashMap<String,String>();
			selectedValuesMap.put(SMALL_ID, SELECTED_VALUE_ID);
			selectedValuesMap.put(DESCRIPTION, SPECIFIC_VALUES);
			listOfValues.add(selectedValuesMap);
			
			return listOfValues;
	    }
	 public List createWeekOfMonthList(){    	
	    	List<Map<String,String>> listOfValues = new ArrayList<Map<String,String>>();
	    	    	
	    	Map<String,String> firstmap = new HashMap<String,String>();
	    	firstmap.put(SMALL_ID, 1+"");
	    	firstmap.put(DESCRIPTION, FIRST);
	    	listOfValues.add(firstmap);
	    	
	    	Map<String,String> secondmap = new HashMap<String,String>();
	    	secondmap.put(SMALL_ID, 2+"");
	    	secondmap.put(DESCRIPTION, SECOND);
	    	listOfValues.add(secondmap);
	    	
	    	Map<String,String> thirdmap = new HashMap<String,String>();
	    	thirdmap.put(SMALL_ID, 3+"");
	    	thirdmap.put(DESCRIPTION, THIRD);
	    	listOfValues.add(thirdmap);
	    	
	    	Map<String,String> fourthmap = new HashMap<String,String>();
	    	fourthmap.put(SMALL_ID, 4+"");
	    	fourthmap.put(DESCRIPTION, FOURTH);
	    	listOfValues.add(fourthmap);
	    	
	    	Map<String,String> fifthmap = new HashMap<String,String>();
	    	fifthmap.put(SMALL_ID, 5+"");
	    	fifthmap.put(DESCRIPTION, FIFTH);
	    	listOfValues.add(fifthmap);
	    	
	    	return listOfValues;
	    }
		
	    public List createWeekdaysListForCompactCron(){
		    	List<DaysOfWeekType> dayOfWeek = getDayOfWeekDTypes(DaysOfWeekType.class);
		    	List<Map<String,String>> listOfValues = new ArrayList<Map<String,String>>();
		    				
		    	for(int index = 0; index<dayOfWeek.size();index++){
		    		Map<String,String> map1 = new HashMap<String,String>();
		    		map1.put(SMALL_ID, dayOfWeek.get(index).getCode());
		    		map1.put(DESCRIPTION, dayOfWeek.get(index).getDescription());
		    		listOfValues.add(map1);
		    	}
		    	
		    	return listOfValues;
		    }
		    
		 public List createMonthListForCompactCron(){
		    	List<MonthsOfYearType> monthList = getDayOfWeekDTypes(MonthsOfYearType.class);
		    	List<Map<String,String>> listOfValues = new ArrayList<Map<String,String>>();			
		    	for(int index = 0; index<monthList.size();index++){
		    		Map<String,String> map1 = new HashMap<String,String>();
		    		map1.put(SMALL_ID, monthList.get(index).getCode());
		    		map1.put(DESCRIPTION, monthList.get(index).getDescription());
		    		listOfValues.add(map1);
		    	}
		    	
		    	return listOfValues;
		    }

	    
	    public List createDayOfWeekList(){
	    	List<DaysOfWeekType> dayOfWeek = getDayOfWeekDTypes(DaysOfWeekType.class);
	    	List<Map<String,String>> listOfValues = new ArrayList<Map<String,String>>();
	    	Map<String,String> map = new HashMap<String,String>();
			map.put(SMALL_ID, STAR);
			map.put(DESCRIPTION, ALL);
			listOfValues.add(map);
			
			Map<String,String> irrelevant = new HashMap<String,String>();
			irrelevant.put(SMALL_ID, QUESTION_MARK);
			irrelevant.put(DESCRIPTION, NO_SPECIFIC_VALUE);
			listOfValues.add(irrelevant);		
	    	for(int index = 0; index<dayOfWeek.size();index++){
	    		Map<String,String> map1 = new HashMap<String,String>();
	    		map1.put(SMALL_ID, dayOfWeek.get(index).getCode());
	    		map1.put(DESCRIPTION, dayOfWeek.get(index).getDescription());
	    		listOfValues.add(map1);
	    	}
	    	
	    	
	    	return listOfValues;
	    }
	    
	    public List createMonthList(){
	    	List<MonthsOfYearType> monthList = getDayOfWeekDTypes(MonthsOfYearType.class);
	    	List<Map<String,String>> listOfValues = new ArrayList<Map<String,String>>();
	    	Map<String,String> map = new HashMap<String,String>();
			map.put(SMALL_ID, STAR);
			map.put(DESCRIPTION, ALL);
			listOfValues.add(map);	
			
	    	for(int index = 0; index<monthList.size();index++){
	    		Map<String,String> map1 = new HashMap<String,String>();
	    		map1.put(SMALL_ID, monthList.get(index).getCode());
	    		map1.put(DESCRIPTION, monthList.get(index).getDescription());
	    		listOfValues.add(map1);
	    	}
	    	
	    	return listOfValues;
	    }
	    
	    
	    public List createOneToSixty(){
	    	List<Map<String,String>> listOfValues = new ArrayList<Map<String,String>>();
	    	Map<String,String> map = new HashMap<String,String>();
			map.put(SMALL_ID, STAR);
			map.put(DESCRIPTION, ALL);
			listOfValues.add(map);
			
	    	for(int counter = 0; counter<60;counter++){
	    		Map<String,String> map1 = new HashMap<String,String>();
	    		map1.put(SMALL_ID, counter+"");
	    		map1.put(DESCRIPTION, counter+"");
	    		listOfValues.add(map1);
	    	}
	    	
	    	return listOfValues;
	    }
	    
	    public List createOneToThirtyOne(){
	    	List<Map<String,String>> listOfValues = new ArrayList<Map<String,String>>();
	    	Map<String,String> map = new HashMap<String,String>();
			map.put(SMALL_ID, STAR);
			map.put(DESCRIPTION, ALL);
			listOfValues.add(map);
			
			Map<String,String> irrelevant = new HashMap<String,String>();
			irrelevant.put(SMALL_ID, QUESTION_MARK);
			irrelevant.put(DESCRIPTION, NO_SPECIFIC_VALUE);
			listOfValues.add(irrelevant);
	    	for(int counter = 1; counter<31;counter++){
	    		Map<String,String> map1 = new HashMap<String,String>();
	    		map1.put(SMALL_ID, counter+"");
	    		map1.put(DESCRIPTION, counter+"");
	    		listOfValues.add(map1);
	    	}
	    	
	    	return listOfValues;
	    }
	    
	    public List createOneToTwentyFour(){
	    	List<Map<String,String>> listOfValues = new ArrayList<Map<String,String>>();
	    	Map<String,String> map = new HashMap<String,String>();
			map.put(SMALL_ID, STAR);
			map.put(DESCRIPTION, ALL);
			listOfValues.add(map);
			
	    	for(int counter = 0; counter<24;counter++){
	    		Map<String,String> map1 = new HashMap<String,String>();
	    		map1.put(SMALL_ID, counter+"");
	    		map1.put(DESCRIPTION, counter+"");
	    		listOfValues.add(map1);
	    	}
	    	
	    	return listOfValues;
	    }    
	   
	    
	    public <T extends GenericParameter> List<T> getDayOfWeekDTypes(Class<T> entityClass) {		
			 List<T> genericParameterList= genericParameterService.retrieveTypes(entityClass);		 
				class GenerateSorted implements Comparator<GenericParameter> {
					public int compare(GenericParameter parameter1, GenericParameter parameter2) {
						return ((Integer)Integer.parseInt(parameter1.getCode())).compareTo(Integer.parseInt(parameter2.getCode()));
					}
				}
				GenerateSorted sorted = new GenerateSorted();
				Collections.sort(genericParameterList, sorted);
				return genericParameterList;
			}
	    
		
	
}
