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
package com.nucleus.gadgets.myToDos;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import net.bull.javamelody.MonitoredWithSpring;

import org.joda.time.DateTime;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.nucleus.gadgets.myToDo.ToDo;
import com.nucleus.gadgets.myToDo.ToDoService;
import com.nucleus.user.User;
import com.nucleus.user.UserInfo;
import com.nucleus.user.UserService;
import com.nucleus.web.common.controller.BaseController;

/**
 * @author Nucleus Software Exports Limited
 */

@Controller
@Transactional
@RequestMapping("/gadget/toDo")
public class MyToDoController extends BaseController {

    @Inject
    @Named("myToDoService")
    private ToDoService myToDoService;

    @Inject
    @Named("userService")
    private UserService userService;

    /**
     * Method to retrieve latest ToDos added by user. List of ToDos will retrieve according to its pageSize.
     * @param pageSize
     * @param map
     * @return
     */
    @RequestMapping("/retrieveLastToDos/{pageSize}")
    @MonitoredWithSpring(name = "MTDC_FETCH_LAST_TO_DO")
    public String retrieveLastToDos(@PathVariable("pageSize") int pageSize, ModelMap map) {
        String userUri = getUserDetails().getUserEntityId().getUri();

        if (pageSize > 0) {
            int startIndex = 0;
            long countNumberOfTodos = myToDoService.getNumberOfToDosByUserUri(userUri);
            if (countNumberOfTodos > pageSize) {
                startIndex = (int) (countNumberOfTodos - pageSize);
            }
            List<ToDo> getAllToDos = myToDoService.getPaginatedToDosByUserUri(getUserDetails().getUserEntityId().getUri(),
                    startIndex, pageSize);
            Collections.reverse(getAllToDos);
            map.put("toDoList", getAllToDos);
            map.put("countNumberOfTodos", countNumberOfTodos);
        }
        return "toDoList";
    }

    /**
     * Method to retrieve ToDos according to its pagination parameters.
     * @param startIndex
     * @param pageSize
     * @param map
     * @return
     */
    @RequestMapping("/retrievePaginatedToDos")
    public String retrievePaginatedToDos(@RequestParam("startIndex") int startIndex, @RequestParam("pageSize") int pageSize,
            ModelMap map) {

        if (pageSize > 0 && startIndex >= 0) {
            Long UserIdOfThisToDo = userService.getCurrentUser().getId();
            User userWithToDo = myToDoService.findUserById(UserIdOfThisToDo);
            String userUri = userWithToDo.getUri();

            long countNumberOfTodos = myToDoService.getNumberOfToDosByUserUri(userUri);

            List<ToDo> getAllToDos = myToDoService.getPaginatedToDosByUserUri(getUserDetails().getUserEntityId().getUri(),
                    startIndex, pageSize);
            Collections.reverse(getAllToDos);
            map.put("toDoList", getAllToDos);
            map.put("countNumberOfTodos", countNumberOfTodos);
        }
        return "toDoList";
    }
    
    public UserInfo getCurrentUser() {
        UserInfo userInfo = null;
        SecurityContext securityContext = SecurityContextHolder.getContext();
        if (securityContext != null && null != securityContext.getAuthentication()) {
            Object principal = securityContext.getAuthentication().getPrincipal();
            if (UserInfo.class.isAssignableFrom(principal.getClass())) {
                userInfo = (UserInfo) principal;
            }
        }
        return userInfo;
    }
    
    
    
    /**
     * Method to add new ToDo. This method also checks for maximum number of ToDos that can be added by user. 
     * @param maxNumOfToDosToBeAdded
     * @param myToDoDetail
     * @param dueDate
     */
    @RequestMapping(value = "/addNewToDo", method = RequestMethod.POST)
    @ResponseBody
    public void addNewToDo(@RequestParam("myToDetail") String myToDoDetail, @RequestParam("dueDate") DateTime dueDate) {
    
    	if(myToDoDetail!=null && myToDoDetail.trim().length()>0){
    		myToDoDetail = myToDoDetail.replaceAll("[^\\w\\s\\-_]", "");
        }
    	
    
        
    	if (myToDoDetail != null && dueDate != null) {

            String userUri = getUserDetails().getUserEntityId().getUri();
            Long maxNumOfToDosToBeAdded = Long.valueOf(getUserDetails().getUserPreferences()
                    .get("config.sidebar.gadget.toDo.maxNumber").getText());
            long numOfToDos = myToDoService.getNumberOfToDosByUserUri(userUri);
            if (numOfToDos < maxNumOfToDosToBeAdded) {
                ToDo createToDo = new ToDo();
                createToDo.setMyToDoDetail(myToDoDetail);
                createToDo.setDueDate(dueDate);
                createToDo.setUserUri(userUri);
                myToDoService.addToDoToItsUser(createToDo);
            }
        }
    }
    
    
    private boolean validateUser(ToDo todo){
    	String userId=getCurrentUser().getId()+"";
    	if(todo.getUserUri().contains(userId)){
    		return true;
    	}else
    		return false;
    }
    

    /**
     * Method to delete ToDo.
     * @param toDoId
     */
    @RequestMapping("/deleteToDo/{toDoId}")
    @ResponseBody
    public void deleteToDo(@PathVariable("toDoId") Long toDoId) {

        if (toDoId != null) {
            ToDo toDo = null;
            toDo = myToDoService.findToDoById(toDoId);
            if(!validateUser(toDo)){
            	throw new RuntimeException("operation not supported");
            }
            myToDoService.deleteToDoFromItsUser(toDo);
        }
    }

    /**
     * Method to edit ToDo.
     * @param pageSize
     * @param startIndex
     * @param myToDoDetail
     * @param dueDate
     * @param toDoId
     * @param map
     * @return
     */
    @RequestMapping("/editToDo/{toDoId}")
    public String editToDo(@RequestParam("pageSize") int pageSize, @RequestParam("paginatedIndex") int startIndex,
            @RequestParam("myToDetail") String myToDoDetail, @RequestParam("dueDate") DateTime dueDate,
            @PathVariable("toDoId") long toDoId, ModelMap map) {

    	
    	if(myToDoDetail!=null && myToDoDetail.trim().length()>0){
    		myToDoDetail = myToDoDetail.replaceAll("[^\\w\\s\\-_]", "");
        }
    	
        if (myToDoDetail != null && dueDate != null) {
            String userUri = getUserDetails().getUserEntityId().getUri();

            ToDo myToDo = myToDoService.findToDoById(toDoId);
            
            if(!validateUser(myToDo)){
            	throw new RuntimeException("operation not supported");
            }

            myToDo.setMyToDoDetail(myToDoDetail);
            myToDo.setDueDate(dueDate);

            myToDoService.updateToDo(myToDo);

            if (pageSize > 0 && startIndex >= 0) {
                long countNumberOfTodos = myToDoService.getNumberOfToDosByUserUri(userUri);
                List<ToDo> getAllToDos = myToDoService.getPaginatedToDosByUserUri(getUserDetails().getUserEntityId()
                        .getUri(), startIndex, pageSize);
                Collections.reverse(getAllToDos);
                map.put("toDoList", getAllToDos);
                map.put("countNumberOfTodos", countNumberOfTodos);
            }
        }
        return "toDoList";
    }

    /**
     * Method to mark read/unread of ToDo 
     * @param markedAsRead
     * @param startIndex
     * @param pageSize
     * @param toDoId
     * @param map
     * @return
     */
    @RequestMapping("/markedAsRead/{toDoId}")
    public String markedAsRead(@RequestParam("markedAsRead") Boolean markedAsRead,
            @RequestParam("paginatedIndex") int startIndex, @RequestParam("pageSize") int pageSize,
            @PathVariable long toDoId, ModelMap map) {

        if (markedAsRead != null) {
            String userUri = getUserDetails().getUserEntityId().getUri();

            ToDo myToDo = myToDoService.findToDoById(toDoId);
            if(!validateUser(myToDo)){
            	throw new RuntimeException("operation not supported");
            }            

            myToDo.setMarkedAsRead(markedAsRead);

            myToDoService.updateToDo(myToDo);

            if (pageSize > 0 && startIndex >= 0) {
                long countNumberOfTodos = myToDoService.getNumberOfToDosByUserUri(userUri);
                List<ToDo> getAllToDos = myToDoService.getPaginatedToDosByUserUri(getUserDetails().getUserEntityId()
                        .getUri(), startIndex, pageSize);
                Collections.reverse(getAllToDos);
                map.put("toDoList", getAllToDos);
                map.put("countNumberOfTodos", countNumberOfTodos);
            }
        }
        return "toDoList";
    }
}
