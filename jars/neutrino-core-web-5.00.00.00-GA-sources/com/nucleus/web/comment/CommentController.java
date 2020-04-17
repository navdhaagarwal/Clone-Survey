/**
 * This file and a proportion of its content is copyright of Nucleus Software Exports Limited - � 2012. All rights reserved.
 * Any redistribution or reproduction of part or all of the contents in any form is prohibited other than the following:
 * - you cannot print or download to a local hard disk extract contents either part or full for personal/ commercial/
 * academic or any other use
 * - you may not copy the content to individual/ third parties for any type of use, either as compiled or source format
 * without the knowledge and consent of Nucleus Software
 * - You may not, except with our express written permission, distribute or commercially exploit the content. Nor may you
 * transmit it or store it in any other web site or other form of electronic retrieval system.
 */
package com.nucleus.web.comment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.support.RequestContextUtils;

import com.nucleus.businessmapping.service.UserManagementServiceCore;
import com.nucleus.core.comment.entity.Comment;
import com.nucleus.core.comment.service.CommentService;
import com.nucleus.core.misc.util.DateUtils;
import com.nucleus.entity.Entity;
import com.nucleus.entity.EntityId;
import com.nucleus.user.User;
import com.nucleus.user.UserService;
import com.nucleus.web.common.controller.BaseController;

import flexjson.JSONSerializer;

/**
 * @author Nucleus Software Exports Limited
 * This class is to handle comments added by checker in maker checker workflow 
 */
@Controller
@RequestMapping(value = "/comment")
public class CommentController extends BaseController {

    @Inject
    @Named(value = "commentService")
    private CommentService              commentService;

    @Inject
    @Named("userService")
    private UserService                 userService;
    @Inject
    @Named("userManagementServiceCore")
    private UserManagementServiceCore   userManagementService;
    
    /*@Inject
    @Named("leadService")
    private LeadService                 leadService;*/

    /**
     * This Method binds  the comments 
     * with the  specified entity.
     * @throws ClassNotFoundException 
     */

    @PreAuthorize("hasAuthority('AUTHORITY_COMMENT_ADD')")
    @SuppressWarnings({ "unchecked"})
    @RequestMapping(value = "/addcomment/{id}")
    public @ResponseBody
    String bindComments(ModelMap map, @RequestParam("commentText") String commentText,
            @RequestParam("currentEntityUri") String entityClass, @PathVariable Long id, HttpServletRequest request)
            throws ClassNotFoundException, NumberFormatException {
        EntityId currentUserEntityId = getUserDetails().getUserEntityId();
       
        commentText = commentService.getCommentText(commentText, RequestContextUtils.getLocale(request), request.getContextPath(), getUserDetails());
        Comment comment = new Comment();
        comment.setAddTimestamp(DateUtils.getCurrentUTCTime());
        comment.setAddedBy(currentUserEntityId);
        comment.setText(commentText);
        commentService.addCommentToEntity(new EntityId((Class<Entity>) Class.forName(entityClass), id), comment);
        return "";

    }

    /**
     * This Method retrieves all the comments associated
     * with the entity.
     * @throws ClassNotFoundException 
     */
    @PreAuthorize("hasAuthority('AUTHORITY_COMMENT')")
    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/retrieveComment/{id}")
    public String retrieveComments(ModelMap map, @RequestParam("currentEntityUri") String className, @PathVariable Long id)
            throws ClassNotFoundException, NumberFormatException {
        Class<Entity> entityClass = (Class<Entity>) Class.forName(className);
        List<Comment> allComments = commentService.getAllCommentsForEntity(new EntityId(entityClass, id));
        for (Comment comment : allComments) {
            comment.getViewProperties().put("Date",
                    DateUtils.getFormattedDate(comment.getAddTimestamp(), getUserDateFormat()));
            comment.getViewProperties().put("Time", DateUtils.getFormattedDate(comment.getAddTimestamp(), "HH:mm:ss"));
            EntityId idd = comment.getAddedBy();
            comment.getViewProperties().put("userName", userService.getUserById(idd.getLocalId()).getUsername());
            comment.getViewProperties().put("UserId", idd.getLocalId());
        }
        map.put("commentList", allComments);
        map.put("currentUser", getUsername());
        map.put("userDateFormat", getUserDateFormat());
        return "commentpage";

    }

    /**
     * This Method edit the comment 
     * @throws ClassNotFoundException 
     */

    @PreAuthorize("hasAuthority('AUTHORITY_COMMENT_ADD')")
    @RequestMapping(value = "/editcomment/{comment_edit_id}")
    public @ResponseBody
    String editComment(ModelMap map, @RequestParam("commentText") String commentText, @PathVariable Long comment_edit_id)
            throws ClassNotFoundException {
        if (comment_edit_id != null) {
            commentService.editComment(comment_edit_id, commentText);
        }
        return "";

    }

    /**
     * This Method delete  the comments 
     * with the  specified entity.
     */

    @PreAuthorize("hasAuthority('AUTHORITY_COMMENT_ADD')")
    @RequestMapping(value = "/delcomment/{commentId}")
    @ResponseBody
    public String deleteComment(ModelMap map, @PathVariable("commentId") Long commentId) {
        if (commentId != null) {
            commentService.deleteComment(commentId);
        }
        return "";

    }

    @PreAuthorize("hasAuthority('AUTHORITY_COMMENT')")
    @RequestMapping(value = "/getAllUser")
    @ResponseBody
    public String getAllUser(@RequestParam(value = "term", required = false) String term) {
        List<User> userList = null;
        Map<String, Object> userMap = new HashMap<String, Object>();
        userMap.put("username", term);
        userList = userManagementService.findUser(userMap);
        List<Map<String, Object>> userListFinal = new ArrayList<Map<String, Object>>();
        if (userList != null && userList.size() > 0) {
            for (User user : userList) {
                Map<String, Object> userValueMap = new HashMap<String, Object>();
                userValueMap.put("label", user.getUsername());
                userValueMap.put("value", user.getId());
                userListFinal.add(userValueMap);
            }
        }
        JSONSerializer iSerializer = new JSONSerializer();
        String jsonString = iSerializer.exclude("*.class").include("label", "value").exclude("*")
                .deepSerialize(userListFinal);
        return jsonString;
    }   

}
