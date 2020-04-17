package com.nucleus.core.comment.service;

import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import com.nucleus.core.comment.entity.Comment;
import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.EntityId;
import com.nucleus.service.BaseService;
import com.nucleus.user.UserInfo;

public interface CommentService extends BaseService {

    /**
     * @description Adds comment to given entity.
     * @param entityId id of entity for which the comment is to be added.
     * @param comment Comment to be added
     */
    public void addCommentToEntity(EntityId entityId, Comment comment);

    /**
     * @description Adds comment to given entity.
     * @param entity object for which the comment is to be added.
     * @param comment Comment to be added
     */
    public void addCommentToEntity(BaseEntity entity, Comment comment);

    /**
     * @description Deletes the comment. Note: This will be a hard delete.
     * @param commentId The comment id to be deleted
     */
    public void deleteComment(Long commentId);

    /**
     * @description Edits the comment with given updated text.
     */
    public void editComment(Long commentId, String updatedText);

    /**
     * @description Retrieves all comments for the passed {@link EntityId}
     * @param entityId The entity id for which comments are to be retrieved
     * @return List of comments
     */
    public List<Comment> getAllCommentsForEntity(EntityId entityId);

    /**
     * @description Loads the comment with given id from database
     */
    public Comment getComment(Long commentId);

    /**
     * @description find watched entities by userURI and returns comments by commentcount as max limit.
     */
    public List<Comment> getCommentsForEntitiesWatchedByUser(String watcherUserUri, int commentCount);

    /**
     * @description find watched entities by userURI and returns comments after the time of comment(with id = lastCommentId) by commentcount as max limit.
     */
    public List<Comment> getCommentsForEntitiesWatchedByUserAfterComment(String watcherUserUri, int commentCount,
            long lastCommentId);

    /**
     * @description Retrieves all comments for the passed {@link entityUri}
     * @param entityId The entity id for which comments are to be retrieved
     * @return List of comments
     */
    public List<Comment> getAllCommentsForEntityUri(String entityUri);

    /**
     * @description Copies the comment. Note: This will retain all the comments of 1st entity class and Create new comments for 2nd entity class.
     * @param entityClassTo object for which the comment is to be added.
     */
    public void copyExistingCommentsToChangedEntity(String fromOwnerEntityUri, String toOwnerEntityUri);

    /**
     * Parse the entered text and generate user notification.
     * */
	public String getCommentText(String commentText, Locale loc, String contextPath, UserInfo userDetails);


}
