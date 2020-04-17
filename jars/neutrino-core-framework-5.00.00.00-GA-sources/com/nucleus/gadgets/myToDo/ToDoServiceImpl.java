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

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import net.bull.javamelody.MonitoredWithSpring;

import com.nucleus.dao.query.JPAQueryExecutor;
import com.nucleus.persistence.EntityDao;
import com.nucleus.query.constants.QueryHint;
import com.nucleus.service.BaseServiceImpl;
import com.nucleus.user.User;

/**
 * @author Nucleus Software Exports Limited
 * TODO 
 */
@Named("myToDoService")
public class ToDoServiceImpl extends BaseServiceImpl implements ToDoService {

    private static final String GET_TO_DO_BY_USER_ID = "FROM %s t Where t.userUri=:userUri";
    private static final String GET_NUMBER_OF_TO_DOS = "SELECT COUNT(*) FROM %s t Where t.userUri=:userUri";

    @Inject
    @Named("entityDao")
    private EntityDao           entityDao;

    @Override
    public ToDo findToDoById(Long id) {

        ToDo myToDo = null;
        if (id != null)
            myToDo = entityDao.find(ToDo.class, id);

        return myToDo;
    }

    @Override
    public void updateToDo(ToDo myToDo) {
        if (myToDo != null) {
            entityDao.saveOrUpdate(myToDo);
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    @MonitoredWithSpring(name = "TDSI_FETCH_PAGINATED_TODO_BY_USR")
    public List<ToDo> getPaginatedToDosByUserUri(String userUri, Integer startIndex, Integer pageSize) {

        List<ToDo> myToDos = new ArrayList<ToDo>();

        if (userUri != null) {
            JPAQueryExecutor queryExecutor = new JPAQueryExecutor(String.format(GET_TO_DO_BY_USER_ID,
                    ToDo.class.getSimpleName())).addParameter("userUri", userUri);
            queryExecutor.addQueryHint(QueryHint.QUERY_HINT_READONLY, Boolean.TRUE);
            myToDos = entityDao.executeQuery(queryExecutor, startIndex, pageSize);
        }
        return myToDos;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public List<ToDo> getAllAssociatedToDosByUserUri(String userUri) {

        List<ToDo> myToDos = new ArrayList<ToDo>();

        if (userUri != null) {
            JPAQueryExecutor queryExecutor = new JPAQueryExecutor(String.format(GET_TO_DO_BY_USER_ID,
                    ToDo.class.getSimpleName())).addParameter("userUri", userUri);
            queryExecutor.addQueryHint(QueryHint.QUERY_HINT_READONLY, Boolean.TRUE);
            myToDos = entityDao.executeQuery(queryExecutor);
        }
        return myToDos;
    }

    @Override
    public User findUserById(Long userId) {

        User user = entityDao.find(User.class, userId);
        return user;
    }

    @Override
    public void addToDoToItsUser(ToDo toDo) {

        entityDao.persist(toDo);
    }

    @Override
    public void deleteToDoFromItsUser(ToDo toDo) {

        if (toDo.getId() != null) {
            entityDao.delete(toDo);
        }
    }

    @Override
    @MonitoredWithSpring(name = "TDSI_FETCH_COUNT_OF_TODO_BY_USR")
    public long getNumberOfToDosByUserUri(String userUri) {
        JPAQueryExecutor<Long> queryExecutor = new JPAQueryExecutor<Long>(String.format(GET_NUMBER_OF_TO_DOS,
                ToDo.class.getSimpleName())).addParameter("userUri", userUri);
        queryExecutor.addQueryHint(QueryHint.QUERY_HINT_READONLY, Boolean.TRUE);
        Long noOfToDos = entityDao.executeQueryForSingleValue(queryExecutor);
        return noOfToDos;
    }

}
