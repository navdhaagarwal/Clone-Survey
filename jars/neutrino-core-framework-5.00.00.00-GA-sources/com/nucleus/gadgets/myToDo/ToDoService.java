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
package com.nucleus.gadgets.myToDo;

import java.util.List;

import com.nucleus.service.BaseService;
import com.nucleus.user.User;

/**
 * @author Nucleus Software Exports Limited
 */

public interface ToDoService extends BaseService{

    /**
     * Method to add new ToDo
     * @param toDo
     */
    public void addToDoToItsUser(ToDo toDo);
    
    /**
     * Method to delete ToDo
     * @param toDo
     */
    public void deleteToDoFromItsUser(ToDo toDo);
    
    /**
     * Method to update ToDo
     * @param myTodo
     */
    public void updateToDo(ToDo myTodo);
    
    /**
     * Method to find ToDo by its Id
     * @param Id
     * @return
     */
    public ToDo findToDoById(Long Id);
    
    /**
     * Method to retrieve ToDos of given user according to its pagination parameters. 
     * @param userUri
     * @param startIndex
     * @param pageSize
     * @return
     */
    public List<ToDo> getPaginatedToDosByUserUri(String userUri, Integer startIndex , Integer pageSize);
    
    /**
     * Method to retrieve all ToDos of given User
     * @param userUri
     * @return
     */
    public List<ToDo> getAllAssociatedToDosByUserUri(String userUri);
    
    /**
     * Method to find user by its Id
     * @param userId
     * @return
     */
    public User findUserById(Long userId);
    
    /**
     * Method to retrieve ToDos of given user by UserUri
     * @param userUri
     * @return
     */
    public long getNumberOfToDosByUserUri(String userUri);
}
