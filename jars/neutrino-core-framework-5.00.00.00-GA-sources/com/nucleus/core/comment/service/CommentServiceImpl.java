package com.nucleus.core.comment.service;

import static com.nucleus.event.EventTypes.COMMENT_ADDED_EVENT;
import static com.nucleus.event.EventTypes.COMMENT_DELETED_EVENT;
import static com.nucleus.event.EventTypes.COMMENT_MODIFIED_EVENT;

import java.util.List;
import java.util.Locale;

import javax.annotation.Resource;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.MessageSource;
import org.springframework.security.access.AccessDeniedException;

import com.nucleus.core.comment.entity.Comment;
import com.nucleus.core.comment.event.UserCommentEvent;
import com.nucleus.core.misc.util.DateUtils;
import com.nucleus.core.notification.CommonMailContent;
import com.nucleus.core.notification.UserMailNotification;

import com.nucleus.core.validation.util.NeutrinoValidator;
import com.nucleus.dao.query.NamedQueryExecutor;
import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.EntityId;
import com.nucleus.persistence.EntityDao;
import com.nucleus.service.BaseServiceImpl;
import com.nucleus.user.User;
import com.nucleus.user.UserInfo;
import com.nucleus.user.UserService;

import net.bull.javamelody.MonitoredWithSpring;

@Named(value = "commentService")
@MonitoredWithSpring(name = "commentService_IMPL_")
public class CommentServiceImpl extends BaseServiceImpl implements CommentService {

    private static final String ALL_COMMENTS_FOR_ENTITIES_WATCHED_BY_USER               = "Generic.getCommentsForEntitiesWatchedByUser";
    private static final String ALL_COMMENTS_FOR_ENTITIES_WATCHED_BY_USER_AFTER_COMMENT = "Generic.getCommentsForEntitiesWatchedByUserAfterComment";
    @Inject
    @Named(value = "entityDao")
    private EntityDao           entityDao;
    
    @Inject
    @Named("messageSource")
    private MessageSource   messageSource;
    
    @Inject
    @Named("userService")
    private UserService                 userService;
    
  

    @Override
    public void addCommentToEntity(EntityId entityId, Comment comment) {
        NeutrinoValidator.notNull(entityId, "entityId can not be null to add Comment to EntityId");
        NeutrinoValidator.notNull(comment, "comment can not be null to set Comment to EntityId");
        BaseEntity entity = entityDao.get(entityId);
        addCommentToEntity(entity, comment);
    }

    @Override
    public void copyExistingCommentsToChangedEntity(String fromOwnerEntityUri, String toOwnerEntityUri) {
        NeutrinoValidator.notNull(fromOwnerEntityUri, "fromOwnerEntityUri can not be null to copy comments");
        NeutrinoValidator.notNull(toOwnerEntityUri, "toOwnerEntityUri can not be null to copy comments");
        NeutrinoValidator.notEmpty(fromOwnerEntityUri, "fromOwnerEntityUri can not be empty to copy comments");
        NeutrinoValidator.notEmpty(toOwnerEntityUri, "toOwnerEntityUri can not be empty to copy comments");
        List<Comment> allComments = getAllCommentsForEntityUri(fromOwnerEntityUri);
        if (!CollectionUtils.isEmpty(allComments)) {
            for (Comment thisComment : allComments) {
                Comment comment = new Comment();
                comment.setAddTimestamp(thisComment.getAddTimestamp());
                comment.setAddedBy(thisComment.getAddedBy());
                comment.setText(thisComment.getText());
                comment.setOwnerEntityUri(EntityId.fromUri(toOwnerEntityUri));
                entityDao.saveOrUpdate(comment);
            }
        }

    }

    @Override
    public void addCommentToEntity(BaseEntity entity, Comment comment) {
        NeutrinoValidator.notNull(entity, "Cannot add comment to null entity");
        NeutrinoValidator.notNull(comment, "Can not add null comment to entity");
        comment.setOwnerEntityUri(entity.getEntityId());
        if (comment.getId() == null) {
            entityDao.persist(comment);
        }

        UserCommentEvent event = new UserCommentEvent(COMMENT_ADDED_EVENT);
        event.setStandardEventPropertiesUsingEntity(entity);
        event.setAssociatedUserUri(comment.getAddedBy().getUri());
        event.setCommentText(comment.getText());
        event.setSuccessFlag(true);
        eventBus.fireEvent(event);
    }

    @Override
    public void deleteComment(Long commentId) {
        NeutrinoValidator.notNull(commentId, "To delete commemt , commentId can not be null");
        Comment comment = entityDao.find(Comment.class, commentId);
        if (comment == null)
            return;
        checkAuthorization(comment);
        UserCommentEvent event = new UserCommentEvent(COMMENT_DELETED_EVENT);
        EntityId ownerEntityId = comment.getOwnerEntityUri();
        BaseEntity ownerEntity = (BaseEntity) entityDao.find(ownerEntityId.getEntityClass(), ownerEntityId.getLocalId());
        event.setStandardEventPropertiesUsingEntity(ownerEntity);
        event.setAssociatedUserUri(comment.getAddedBy().getUri());
        event.setCommentText(comment.getText());
        entityDao.delete(comment);
        event.setSuccessFlag(true);
        eventBus.fireEvent(event);
    }

    @Override
    public void editComment(Long commentId, String updatedText) {
        NeutrinoValidator.notNull(commentId, "To edit a comment ,  comment Id can not be null");
        Comment comment = entityDao.find(Comment.class, commentId);
        if (comment == null)
            return;
        checkAuthorization(comment);
        comment.setText(updatedText);
        UserCommentEvent event = new UserCommentEvent(COMMENT_MODIFIED_EVENT);
        EntityId ownerEntityId = comment.getOwnerEntityUri();
        BaseEntity ownerEntity = (BaseEntity) entityDao.find(ownerEntityId.getEntityClass(), ownerEntityId.getLocalId());
        event.setStandardEventPropertiesUsingEntity(ownerEntity);
        event.setAssociatedUserUri(comment.getAddedBy().getUri());
        event.setOldText(comment.getText());
        event.setNewText(updatedText);
        event.setCommentText(updatedText);
        event.setSuccessFlag(true);
        eventBus.fireEvent(event);
    }

    @Override
    public List<Comment> getAllCommentsForEntity(EntityId entityId) {
        NeutrinoValidator.notNull(entityId, "EntityId cannot be null");
        BaseEntity entity = entityDao.get(entityId);
        EntityId baseEntityId = entity.getEntityId();
        return getAllCommentsForEntityUri(baseEntityId.getUri());
    }

    @Override
    public List<Comment> getAllCommentsForEntityUri(String entityUri) {
        NeutrinoValidator.notNull(entityUri, "EntityURI cannot be null");
        NamedQueryExecutor<Comment> builder = new NamedQueryExecutor<Comment>("Generic.AllCommentsForEntity").addParameter(
                "ownerEntityUri", entityUri);
        return entityDao.executeQuery(builder);
    }

    @Override
    public Comment getComment(Long commentId) {
        NeutrinoValidator.notNull(commentId, "To get a comment , comment Id can not be null");
        return entityDao.find(Comment.class, commentId);
    }

    @Override
    /*    @MonitoredWithSpring(name = "CSI_FETCH_C0MMENT_FOR_ENTITY")*/
    public List<Comment> getCommentsForEntitiesWatchedByUser(String watcherUserUri, int commentCount) {
        NeutrinoValidator.notNull(watcherUserUri, "User URI cannot be null");
        NamedQueryExecutor<Comment> executor = new NamedQueryExecutor<Comment>(ALL_COMMENTS_FOR_ENTITIES_WATCHED_BY_USER)
                .addParameter("watcherUserUri", watcherUserUri);
        return entityDao.executeQuery(executor, 0, commentCount);
    }

    @Override
    /*    @MonitoredWithSpring(name = "CSI_FETCH_C0MMENT_FOR_ENTITY_AFTER_COMMENT")*/
    public List<Comment> getCommentsForEntitiesWatchedByUserAfterComment(String watcherUserUri, int commentCount,
            long lastCommentId) {
        NeutrinoValidator.notNull(watcherUserUri, "User URI cannot be null");
        NamedQueryExecutor<Comment> executor = new NamedQueryExecutor<Comment>(
                ALL_COMMENTS_FOR_ENTITIES_WATCHED_BY_USER_AFTER_COMMENT).addParameter("watcherUserUri", watcherUserUri)
                .addParameter("lastCommentId", lastCommentId);
        return entityDao.executeQuery(executor, 0, commentCount);

    }

    private void checkAuthorization(Comment comment) {
        if (!(comment.getAddedBy().getUri().equalsIgnoreCase(getCurrentUser().getUserEntityId().getUri()))) {
            throw new AccessDeniedException("User is not authorised to update/delete comment which was added by other user.");
        }
    }
    
    @SuppressWarnings("static-access")
    @Override
    public String getCommentText(String commentText, Locale loc, String contextPath, UserInfo userDetails){
        StringUtils su = null;
     
		String[] users = su.substringsBetween(commentText, "@", " ");
        String[] applications = su.substringsBetween(commentText, "^", " ");
        if (users != null) {
            for (String user : users) {
                if (StringUtils.isNotBlank(user)) {
                    User userName = userService.findUserByUsername(user);
                    String userLink = "<a href='" + contextPath + "/app/UserInfo/view/"
                            + userName.getEntityLifeCycleData().getUuid() + "'>" + userName.getDisplayName() + "</a>";
                    String selUser = "@" + user;
                    commentText = commentText.replace(selUser, userLink);
                }
            }
        }
        if (applications != null) {
            for (String app : applications) {
                if (StringUtils.isNotBlank(app)) {
                    String userLink = "<a href='" + contextPath + "/app/LoanApplication/view/"
                            + app + "'>" + app + "</a>";
                    String selApp = "^" + app;
                    commentText = commentText.replace(selApp, userLink);
                }
            }
        }
    
        return commentText;
    }

}