/*package com.nucleus.sso;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nucleus.core.cache.FWCacheHelper;
import com.nucleus.notificationMaster.service.InAppMailHelper;
import com.nucleus.sso.utils.SsoConfigUtility;
import com.nucleus.user.UserService;
import com.nucleus.web.common.controller.BaseController;

@RestController
@RequestMapping("/sso")
public class SsoEmailController extends BaseController {

	
	  private static final String MAIL_NOTIFICATION_PRIORITY = "mailNotificationPriority";
	  @Inject
	  @Named("userService")
	  private UserService userService;
	  
	  
	
	  
	  @Inject
	  @Named("InAppHelper")
	  InAppMailHelper                     inAppMailHelper;
	  
		@Inject
		@Named("fwCacheHelper")
		private FWCacheHelper fwCacheHelper;
		
		@Inject
		@Named("ssoConfigUtility")
		private SsoConfigUtility ssoConfigUtility;
	
	
	  @RequestMapping(value = "/getEmailCount", method = RequestMethod.POST)
	  public ResponseEntity<Long> getUserMailCount(HttpServletRequest request) {
		  long notificationsSize = 0L;
	      
		  try {
			 
			  String username = ssoConfigUtility.decryptuserName(request.getHeader(USERNAME_CONSTANT));
			 

			  if(ssoConfigUtility.checkIfUserExists(username))
			  {	  
		          User user = userService.getUserFromUsername(username).getUserReference();
		          List<String> statList = new ArrayList<>();
		          statList.add(UserMailNotificationType.USER_MAIL_NEW);
		          statList.add(UserMailNotificationType.USER_MAIL_DELETED_FROM_OUTBOX);
		          statList.add(UserMailNotificationType.USER_MAIL_DELETED_FROM_OUTBOX_TRASH);
	
		          notificationsSize = userMailNotificationService.getUserNotificationsCount(user.getUri(), statList, INBOX);
			  }
	      } catch (SystemException e) {
	          exceptionLogger.error(EXCEPTION + ":", e);
	          return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
	      }
	      return new ResponseEntity<>(notificationsSize, HttpStatus.OK);
		
	  }
	  
	  *//**
	   * View messages in modal window
	   * 
	   * @param email
	   * @param result
	   * @param map
	   * @return
	 * @throws Exception 
	   *//*
	  @RequestMapping(value = "/viewMailModal", method = RequestMethod.POST)
	  public ResponseEntity<List<UserMailNotificationVO>> viewMailModal(HttpServletRequest request) {
          List<UserMailNotificationVO> mailList = new ArrayList<>();
	      try {
			  String username = ssoConfigUtility.decryptuserName(request.getHeader(USERNAME_CONSTANT));
			  
			  if(!isUserInValid(username)){
			  
				  int defaultViewBoxSize=3;
		          List<String> statList = new ArrayList<>();
		          statList.add(UserMailNotificationType.USER_MAIL_NEW);
		          statList.add(UserMailNotificationType.USER_MAIL_DELETED_FROM_OUTBOX);
		          statList.add(UserMailNotificationType.USER_MAIL_DELETED_FROM_OUTBOX_TRASH);
		          List<Map<String, ?>> notifications = userMailNotificationService.getUserNotifications(userService.getUserUriByUserName(username),
		                  statList, UserMailNotificationType.TYPE_INBOX, null, defaultViewBoxSize);
	
		          
		           * Get List Of Inbox
		           
	
		          for (Map<String, ?> notification : notifications) {
		              UserMailNotificationVO mailNotify = new UserMailNotificationVO();
		              CommonMailContent cmc = (CommonMailContent) notification.get("commonMailContent");
		              UserInfo createdBy = userService.getUserById(EntityId.fromUri(
		                      cmc.getFromUserUri() == null ? "" : cmc.getFromUserUri()).getLocalId());
		              mailNotify.setFromUser(createdBy.getDisplayName() == null ? "" : createdBy.getDisplayName());
		              mailNotify.setSubject(cmc.getSubject() == null ? "" : cmc.getSubject());
		              setMessageSentTimeStamp(cmc,mailNotify,username);
		              if (notification.get("msgStatus").equals(UserMailNotificationType.USER_MAIL_READ)) {
		                  mailNotify.setReadStatus(true);
		              }
		              mailList.add(mailNotify);
	
		          }
			  }
	      } catch (SystemException e) {
	          exceptionLogger.error(EXCEPTION+ ":" , e);
	          return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
	      }

	     return new ResponseEntity<>(mailList, HttpStatus.OK);
		
	  }
	  
	  
	  @RequestMapping(value = "/viewInboxMails", method = RequestMethod.POST)
	  public ResponseEntity<List<UserMailNotificationVO>> viewInboxMails(HttpServletRequest request)  {
			 
		List<UserMailNotificationVO> emails = new ArrayList<>();
		List<String> criterias = new ArrayList<>();
		List<Map<String, ?>> notifications =null;  
		try{  
			String username = ssoConfigUtility.decryptuserName(request.getHeader(USERNAME_CONSTANT));
			if(ssoConfigUtility.checkIfUserExists(username)){
			
			User user = userService.getUserFromUsername(username).getUserReference();
			EntityId entityId = EntityId.fromUri(user.toString());
			
			criterias.add(UserMailNotificationType.USER_MAIL_NEW);
			criterias.add(UserMailNotificationType.USER_MAIL_DELETED_FROM_OUTBOX);
			criterias.add(UserMailNotificationType.USER_MAIL_DELETED_FROM_OUTBOX_TRASH);
			criterias.add(UserMailNotificationType.USER_MAIL_READ);
	
			notifications =userMailNotificationService.getPaginatedUserNotifications(entityId.getUri(),criterias, INBOX, null, null,0,20);
					
			emails = !notifications.isEmpty()?toUserMailNotificationVo(notifications,INBOX, username):emails;
		 
			}
		  }catch(Exception e){
			  return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		  }
		return new ResponseEntity<>(emails, HttpStatus.OK);
	 }
	  
	  private List<UserMailNotificationVO> toUserMailNotificationVo(List<Map<String, ?>> notifications,String type, String username) {
	  	
	  	List<UserMailNotificationVO> emails = new ArrayList<>();
	  	notifications.forEach(notification->{

	  		UserMailNotificationVO userMailNotificationVo = new UserMailNotificationVO();
	  		userMailNotificationVo.setUserId(Long.parseLong((notification.get("id").toString())));

	  		CommonMailContent commonMailContent = (CommonMailContent) notification.get("commonMailContent");
	  		UserInfo createdBy = userService.getUserById(EntityId.fromUri(commonMailContent.getFromUserUri() == null ? "" 
	  																: commonMailContent.getFromUserUri()).getLocalId());

	  		userMailNotificationVo.setFromUser(createdBy.getDisplayName() == null ? "" : createdBy.getDisplayName());
	  		userMailNotificationVo.setSubject(commonMailContent.getSubject() == null ? "" : commonMailContent.getSubject());
	  		
	  		setMailNotificationPriority(userMailNotificationVo, notification, commonMailContent);

	  		setToUser(userMailNotificationVo, notification, type);
	  		
	  		setMessageSentTimeStamp(commonMailContent, userMailNotificationVo, username);

	  		emails.add(userMailNotificationVo);
	  	});
	  	return emails;
	}
	  
	 		
		 @RequestMapping(value = "/viewMessage", method = RequestMethod.POST)
		  public ResponseEntity<UserMailNotificationVO> viewMessages(HttpServletRequest request){
			 
			 String username;
			 String userId = request.getHeader("userId");
			 UserMailNotificationVO mailNotify = new UserMailNotificationVO();
		     Map<String, Object> queryMapInboxData = new HashMap<>();

			try {
				username = ssoConfigUtility.decryptuserName(request.getHeader(USERNAME_CONSTANT));
				if(!ssoConfigUtility.checkIfUserExists(username)){
					return null;
				}

		        queryMapInboxData.put("u.id", userId);
		        UserMailNotification oldNotification = userMailNotificationService.getNotification(Long.valueOf(userId));
		        fwCacheHelper.detachEntity(oldNotification);
		        UserMailNotification notification = userMailNotificationService.getNotification(Long.valueOf(userId));
		        notification.setMsgStatus(UserMailNotificationType.USER_MAIL_READ);
		        userMailNotificationService.updateStatus(notification,oldNotification);
		        CommonMailContent cmc = notification.getCommonMailContent();
		        List<String> touserList = new ArrayList<>();
		        List<String> touserListId = new ArrayList<>();
		        mailNotify.setSubject(cmc.getSubject() == null ? "" : cmc.getSubject());
		        
		        createToUserList(touserList, touserListId, notification);
		        
		        
		        UserInfo createdBy = userService.getUserById(EntityId.fromUri(
		                cmc.getFromUserUri() == null ? "" : cmc.getFromUserUri()).getLocalId());
		        mailNotify.setFromUser(createdBy.getDisplayName() == null ? "" : createdBy.getDisplayName());
		        mailNotify.setFromUserId(createdBy.getId().toString());
		        mailNotify.setToList(touserList);
		        mailNotify.setToListId(touserListId);
		        if (cmc.getMsgSentTimeStamp() != null) {
		            DateTime calendar = cmc.getMsgSentTimeStamp();
		            mailNotify.setMsgSentTimeStamp(calendar == null ? "" : getFormattedDate(calendar, username));
		        }
		        mailNotify.setBody(cmc.getBody() == null ? "" : cmc.getBody());
		        if (notification.getMailNotificationPriority() != null && !(notification.getMailNotificationPriority().isEmpty())) {
		            mailNotify.setMailNotificationPriority(cmc.getMsgSentTimeStamp() == null ? "" : notification
		                    .getMailNotificationPriority());
		        } else {
		            mailNotify.setMailNotificationPriority("");
		        }
		        mailNotify.setNotificationID(notification.getId());
		        
			} catch (SystemException e) {
				exceptionLogger.error(EXCEPTION + " : ", e);
				return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
			}
			return new ResponseEntity<>(mailNotify, HttpStatus.OK);
			 
		 }
		 
		 private void createToUserList(List<String> touserList, List<String> touserListId, UserMailNotification notification ){
			 
			 UserInfo sendBy = null;
			 
			 if (notification.getToUserUriList() == null) {
		            sendBy = userService.getUserById(EntityId.fromUri(
		                    notification.getToUserUri() == null ? "" : notification.getToUserUri()).getLocalId());
		            touserList.add(sendBy.getDisplayName() == null ? "" : sendBy.getDisplayName());
		            touserListId.add(sendBy.getId().toString());
		        } else {
		            String[] toUserUriList = notification.getToUserUriList().split(",");
		            for (String toUser : toUserUriList) {
		                sendBy = userService.getUserById(EntityId.fromUri(toUser == null ? "" : toUser).getLocalId());
		                touserList.add(sendBy.getDisplayName() == null ? "" : sendBy.getDisplayName());
		                touserListId.add(sendBy.getId().toString());
		            }

		        }
			 
			 
		 }
		 
		 
		 private void setMailNotificationPriority(UserMailNotificationVO userMailNotificationVo, Map<String, ?> notification, CommonMailContent commonMailContent){

		  		if (notification.get(MAIL_NOTIFICATION_PRIORITY) != null && !(notification.get(MAIL_NOTIFICATION_PRIORITY).toString().isEmpty())) {
		  			userMailNotificationVo.setMailNotificationPriority(commonMailContent.getMsgSentTimeStamp() == null ? "" 
		  													: notification.get(MAIL_NOTIFICATION_PRIORITY).toString());
		  		} else {
		  			userMailNotificationVo.setMailNotificationPriority("");
		  		}
		  }
		  
		  private void setToUser(UserMailNotificationVO userMailNotificationVo,Map<String, ?> notification, String type ){
		  		if(!UserMailNotificationType.TYPE_INBOX.equalsIgnoreCase(type)){
		  			UserInfo toUser = userService.getUserById(EntityId.fromUri(notification.get("toUserUri") == null ? "" 
		  					: notification.get("toUserUri").toString()).getLocalId());
		  			userMailNotificationVo.setToUser(toUser.getDisplayName());
		  		}else{
		  			//for inbox type
		  			if (notification.get("msgStatus").equals(UserMailNotificationType.USER_MAIL_READ)) {
		  				userMailNotificationVo.setReadStatus(true);
		  			}
		  		}
		  }
		  
		  private String getFormattedDate(DateTime dateTime, String username) {
				DateTimeFormatter format = DateTimeFormat.forPattern(getUserDateFormat(username));
				return format.print(dateTime);

			}


			private String getUserDateFormat(String username) {
				String pattern = "dd/MM/yyyy";
				UserInfo userinfo = userService.getUserFromUsername(username);
				if (userinfo != null && userinfo.getUserPreferences() != null) {
					ConfigurationVO confVO = userinfo.getUserPreferences().get("config.date.formats");
					if (confVO != null) {
						pattern = confVO.getText();
					}
				}
				return pattern;

			}
			
			  private boolean isUserInValid(String username){
				  return (!ssoConfigUtility.checkIfUserExists(username) && !ssoConfigUtility.hasAuthority(username, "VIEW_MAILBOX"));
			  }
			  
			  private void setMessageSentTimeStamp(CommonMailContent cmc, UserMailNotificationVO mailNotify, String username){
				  if (cmc.getMsgSentTimeStamp() != null) {
		              DateTime calendar = cmc.getMsgSentTimeStamp();
		              mailNotify.setMsgSentTimeStamp(calendar == null ? "" : getFormattedDate(calendar, username));
		          }
			  }
		

}

*/