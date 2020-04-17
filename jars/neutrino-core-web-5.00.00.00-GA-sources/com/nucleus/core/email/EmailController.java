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
package com.nucleus.core.email;

import static com.nucleus.logging.BaseLoggers.exceptionLogger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;

import com.nucleus.event.UserMailNotificationType;
import com.nucleus.user.UserInfo;
import org.joda.time.DateTime;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.nucleus.core.misc.util.DateUtils;
import com.nucleus.core.notification.CommonMailContent;
import com.nucleus.core.notification.UserMailNotification;
import com.nucleus.entity.EntityId;
import com.nucleus.license.security.web.LicenseSecurityConstants;
import com.nucleus.notificationMaster.service.InAppMailHelper;
import com.nucleus.user.User;
import com.nucleus.user.UserService;
import com.nucleus.web.common.controller.BaseController;

/**
 * @author Nucleus Software Exports Limited
 */
@Controller
@RequestMapping(value = "/email")
public class EmailController extends BaseController {

	
	
	
	
	
    private static final String                              masterId = "email";
    
    
    


    
  
   

    /** The user service. */
    @Inject
    @Named("userService")
    private UserService                 userService;

    @Inject
    @Named("InAppHelper")
    InAppMailHelper                     inAppMailHelper;

    /**
     * View messages in modal window
     * 
     * @param email
     * @param result
     * @param map
     * @return
     */
  /*  @PreAuthorize("hasAuthority('VIEW_MAILBOX') and " + LicenseSecurityConstants.LICENSE_INTER_USER_EMAIL)
    @RequestMapping(value = "/view", method = RequestMethod.POST)
    public String emailModalWindow(ModelMap map) {
    	int defaultViewBoxSize=3;
        try {
			List<Map<String, ?>> notifications = userMailNotificationService.getUserNotifications(
					userService.getUserUriByUserName(getUserDetails().getUsername()), STAT_LIST,
					UserMailNotificationType.TYPE_INBOX, null, defaultViewBoxSize);
            
             * Get List Of Inbox
             

            List<UserMailNotificationVO> mailList = new ArrayList<>();
            for (Map<String, ?> notification : notifications) {
                UserMailNotificationVO mailNotify = new UserMailNotificationVO();
                CommonMailContent cmc = (CommonMailContent) notification.get("commonMailContent");
                UserInfo createdBy = userService.getUserById(EntityId.fromUri(
                        cmc.getFromUserUri() == null ? "" : cmc.getFromUserUri().toString()).getLocalId());
                mailNotify.setFromUser(createdBy.getDisplayName() == null ? "" : createdBy.getDisplayName());
                mailNotify.setSubject(cmc.getSubject() == null ? "" : cmc.getSubject().toString());
                if (cmc.getMsgSentTimeStamp() != null) {
                    DateTime calendar = cmc.getMsgSentTimeStamp();
                    mailNotify.setMsgSentTimeStamp(calendar == null ? "" : getFormattedDate(calendar));
                }
                if (notification.get("msgStatus").equals(UserMailNotificationType.USER_MAIL_READ)) {
                    mailNotify.setReadStatus(true);
                }
                mailList.add(mailNotify);

            }
            map.put("emailNotifier", mailList);
        } catch (Exception e) {
            exceptionLogger.error("Exception : ", e);
        }

        return "emailForm";
    }
*/
    /**
     * View messages in Mail Box
     * 
     * @param email
     * @param result
     * @param map
     * @return
     */
    /*@PreAuthorize("hasAuthority('VIEW_MAILBOX') and " + LicenseSecurityConstants.LICENSE_INTER_USER_EMAIL)
    @RequestMapping(value = "/viewMailBox", method = RequestMethod.GET)
    @MonitoredWithSpring(name = "EC_VIEW_MAILBOX")
    public String viewMailBox(UserMailNotificationVO email, ModelMap map) {
        try {
            // we need to get below logged in user from session

            User user = getUserDetails().getUserReference();
            Map<String, List<UserMailNotificationVO>> dataMap = new HashMap<>();

            map.put("emailNotifier", dataMap);
            map.put("redirectedPage", false);
            map.put("masterID", masterId);

            if (inAppMailHelper.checkUserEnabled(user.getUri())) {
                map.put("inboxDisabled", true);
            }

            List<Map<Long, String>> nameList = userService.getAllUserProfileNameAndId();

            map.put("userProfileList", nameList);

        } catch (Exception e) {
            exceptionLogger.error("Exception : ", e);
        }
        return "mailbox";
    }*/

    
    
 ///////////////////////////////////////////////////////////////////////////////////////
    
   /* *//**
     * Issue #PDDEV-13988
     * View emails by type(inbox,outbox, trash)    
     * @param iDisplayStart  	Start index number of Data table
     * @param iDisplayLength	Page length of Data table
     * @param sSortDir_0		Sorting direction of Data table column
     * @param iSortCol_0		Sorting Column index of Data table
     * @return DataTableJsonHepler 
     * *//*
    @RequestMapping(value = "/load/{type}", method = RequestMethod.GET)
    @ResponseBody
    public DataTableJsonHepler getEmailDataByType(@PathVariable String type,
    		@RequestParam(value = "iDisplayStart", required = false) Integer iDisplayStart,
            @RequestParam(value = "iDisplayLength", required = false) Integer iDisplayLength,
            @RequestParam(value = "sEcho", required = false) Integer sEcho,
            @RequestParam(value = "sSortDir_0", required = false) String sSortDir_0,
            @RequestParam(value = "iSortCol_0", required = false) Integer iSortCol_0) {

    	User user = getUserDetails().getUserReference();
    	EntityId entityId = EntityId.fromUri(user.toString());
    	DataTableJsonHepler jsonHelper = new DataTableJsonHepler();

    	try {
    		jsonHelper =getEmailsByType(type,entityId,iDisplayStart,iDisplayLength,sEcho,sSortDir_0,iSortCol_0);
    	} catch (Exception e) {
    		exceptionLogger.error("Exception : ", e);
    		e.printStackTrace();
    	}
    	return jsonHelper;
    }*/
    
    /**
     * To show inbox, outbox and trash messages by type(on-demand loading)
     * 
     * @param type Type of the message(inbox/outbox/trash)
     * @param entityId
     * @param startIndex Start index of data table pagination
     * @param pageSize Pagesize of data table pagination
     * @param sEcho data table prop
     * @param sortDir Sorting direction 
     * @param sortCol Sorting column name
     * @return DataTableJsonHepler
     */
   /* public DataTableJsonHepler getEmailsByType(String type,EntityId entityId,Integer startIndex,Integer pageSize,
    		Integer sEcho,String sortDir,Integer sortCol) throws Exception{

    	pageSize =pageSize!=null?pageSize==-1?10:pageSize:null;    	
    	DataTableJsonHepler jsonHelper =new DataTableJsonHepler();

    	List<UserMailNotificationVO> emails = new ArrayList<>();
    	List<String> criterias = new ArrayList<String>();
    	List<Map<String, ?>> notifications =null;
    	Long totalCount=0l;
    	
    	if(UserMailNotificationType.TYPE_INBOX.equalsIgnoreCase(type)){
    		
    		criterias.add(UserMailNotificationType.USER_MAIL_NEW);
    		criterias.add(UserMailNotificationType.USER_MAIL_DELETED_FROM_OUTBOX);
    		criterias.add(UserMailNotificationType.USER_MAIL_DELETED_FROM_OUTBOX_TRASH);
    		criterias.add(UserMailNotificationType.USER_MAIL_READ);
    		
    	}else if(UserMailNotificationType.TYPE_OUTBOX.equalsIgnoreCase(type)){
    		
    		criterias.add(UserMailNotificationType.USER_MAIL_NEW);
    		criterias.add(UserMailNotificationType.USER_MAIL_DELETED_FROM_INBOX);
    		criterias.add(UserMailNotificationType.USER_MAIL_DELETED_FROM_INBOX_TRASH);
    		criterias.add(UserMailNotificationType.USER_MAIL_READ);
    		
    	}else if(UserMailNotificationType.TYPE_TRASH.equalsIgnoreCase(type)){
    		
    		criterias.add(UserMailNotificationType.USER_MAIL_DELETED_FROM_INBOX);
    		criterias.add(UserMailNotificationType.USER_MAIL_DELETED_FROM_OUTBOX);
    		criterias.add(UserMailNotificationType.USER_MAIL_DELETED_FROM_INBOX_TRASH_OUTBOX);
    		criterias.add(UserMailNotificationType.USER_MAIL_DELETED_FROM_OUTBOX_INBOX_TRASH);
    		criterias.add(UserMailNotificationType.USER_MAIL_DELETED_FROM_OUTBOX_TRASH_INBOX);
    		criterias.add(UserMailNotificationType.USER_MAIL_DELETED_FROM_INBOX_OUTBOX_TRASH);    			
    		criterias.add(UserMailNotificationType.USER_MAIL_DELETED_FROM_INBOX_AND_OUTBOX);
    		
    	}
    	notifications = (startIndex!=null && pageSize!=null)?
    				userMailNotificationService.getPaginatedUserNotifications(entityId.getUri(),criterias, type, null, null,startIndex,pageSize)
    				:userMailNotificationService.getUserNotifications(entityId.getUri(),criterias, type, null, null);
    	emails =!notifications.isEmpty()?toUserMailNotificationVo(notifications,type):emails;
    	totalCount =userMailNotificationService.getUserNotificationsCount(entityId.getUri(),criterias, type);
		
    	//do custom sort as per the data table sort index
    	if(sortCol!=null){
    		if(sortCol==UserMailNotificationType.SORT_COL_EMAIL_SUBJECT){
    			sortEmailsBySubject(emails,sortDir);
    		} else if(sortCol==UserMailNotificationType.SORT_COL_EMAIL_USER_NAME){
    			if(!UserMailNotificationType.TYPE_OUTBOX.equalsIgnoreCase(type)){
    				sortEmailsByFromUserName(emails,sortDir);
    			}else {
    				sortEmailsByToUserName(emails,sortDir);
    			}
    		} else if(sortCol==UserMailNotificationType.SORT_COL_EMAIL_DATE){
    			sortEmailsByDate(emails,sortDir);
    		} else if(sortCol==UserMailNotificationType.SORT_COL_EMAIL_NOTIFICATION_PRIORITY){
    			sortEmailsByNotificationPririoty(emails,sortDir);
    		} else if(sortCol == UserMailNotificationType.SORT_COL_EMAIL_FAVOURITE) {
    			sortEmailsByFavourite(emails,sortDir);
    		}
    	}
    	jsonHelper.setiTotalDisplayRecords(totalCount.intValue());
    	jsonHelper.setiTotalRecords(totalCount.intValue());
    	jsonHelper.setAdditionalDataMap(emails);
    	jsonHelper.setsEcho(sEcho!=null?sEcho:0);

    	return jsonHelper;
    }*/
    /**
     * Mapper for converting the object to be transfer on network
     * @param notifications Data retrieved from DB based on the type
     * @param type Type of email(inbox,outbox or trash)
     * 
     * */
   /* private List<UserMailNotificationVO> toUserMailNotificationVo(List<Map<String, ?>> notifications,String type) throws Exception{
    	
    	List<UserMailNotificationVO> emails = new ArrayList<UserMailNotificationVO>();
    	notifications.forEach(notification->{

    		UserMailNotificationVO userMailNotificationVo = new UserMailNotificationVO();
    		userMailNotificationVo.setUserId(Long.parseLong((notification.get("id").toString())));

    		CommonMailContent commonMailContent = (CommonMailContent) notification.get("commonMailContent");
    		UserInfo createdBy = userService.getUserById(EntityId.fromUri(commonMailContent.getFromUserUri() == null ? "" 
    																: commonMailContent.getFromUserUri().toString()).getLocalId());

    		userMailNotificationVo.setFromUser(createdBy.getDisplayName() == null ? "" : createdBy.getDisplayName());
    		userMailNotificationVo.setSubject(commonMailContent.getSubject() == null ? "" : commonMailContent.getSubject().toString());
    		userMailNotificationVo.setFavourite(commonMailContent.getFavourite());
    		if (notification.get("mailNotificationPriority") != null && !(notification.get("mailNotificationPriority").toString().isEmpty())) {
    			userMailNotificationVo.setMailNotificationPriority(commonMailContent.getMsgSentTimeStamp() == null ? "" 
    													: notification.get("mailNotificationPriority").toString());
    		} else {
    			userMailNotificationVo.setMailNotificationPriority("");
    		}

    		//if outbox, set the touser property    		
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
    		if (commonMailContent.getMsgSentTimeStamp() != null) {
    			DateTime calendar = commonMailContent.getMsgSentTimeStamp();
    			userMailNotificationVo.setMsgSentTimeStamp(calendar == null ? "" : getFormattedDate(calendar));
    		}
    		emails.add(userMailNotificationVo);
    	});
    	return emails;
  	}

    
     * Sort the notifications by Subject and specified sorting direction  
     * 
    private void sortEmailsBySubject(List<UserMailNotificationVO> emails,String sortDir){
    	if((SORT_DIRECTION_ASC.equalsIgnoreCase(sortDir))){
    		emails.sort((a,b)->a.getSubject().compareToIgnoreCase(b.getSubject()));
    	}else{
    		emails.sort((a,b)->b.getSubject().compareToIgnoreCase(a.getSubject()));
    	}
    }

    
     * Sort the notifications by fromUserName property and specified sorting direction
     * It is used for inbox and trash notification purpose
     * 
    private void sortEmailsByFromUserName(List<UserMailNotificationVO> emails,String sortDir){
    	if((SORT_DIRECTION_ASC.equalsIgnoreCase(sortDir))){
    		emails.sort((a,b)->a.getFromUser().compareToIgnoreCase(b.getFromUser()));
    	}else{
    		emails.sort((a,b)->b.getFromUser().compareToIgnoreCase(a.getFromUser()));
    	}
    }
    
    
    
     * Sort the notifications by Favourite status and specified sorting direction  
     * 
    private void sortEmailsByFavourite(List<UserMailNotificationVO> emails,String sortDir){
    	if((SORT_DIRECTION_ASC.equalsIgnoreCase(sortDir))){
    		emails.sort((a,b)-> BooleanComparator.getTrueFirstComparator().compare(a.getFavourite(), b.getFavourite()));
    	}else{
    		emails.sort((a,b)->BooleanComparator.getFalseFirstComparator().compare(a.getFavourite(), b.getFavourite()));
    	}
    }
    
    
    
     * Sort the notifications by toUserName property and specified sorting direction
     * It is used for Outbox mail purpose  
     * 
    private void sortEmailsByToUserName(List<UserMailNotificationVO> emails,String sortDir){
    	if((SORT_DIRECTION_ASC.equalsIgnoreCase(sortDir))){
    		emails.sort((a,b)->a.getToUser().compareToIgnoreCase(b.getToUser()));
    	}else{
    		emails.sort((a,b)->b.getToUser().compareToIgnoreCase(a.getToUser()));
    	}
    }
    
    
     * Sort the notifications by mailNotificationPriority property and specified sorting direction
     * 
    private void sortEmailsByNotificationPririoty(List<UserMailNotificationVO> emails,String sortDir){
    	if((SORT_DIRECTION_ASC.equalsIgnoreCase(sortDir))){
    		emails.sort((a,b)->a.getMailNotificationPriority().compareToIgnoreCase(b.getMailNotificationPriority()));
    	}else{
    		emails.sort((a,b)->b.getMailNotificationPriority().compareToIgnoreCase(a.getMailNotificationPriority()));
    	}
    }
    
    
     * Sort the notifications by date property and specified sorting direction
     * 
    private void sortEmailsByDate(List<UserMailNotificationVO> emails,String sortDir){
    	if((SORT_DIRECTION_ASC.equalsIgnoreCase(sortDir))){
    		emails.sort((a,b)->a.getMsgSentTimeStamp().compareToIgnoreCase(b.getMsgSentTimeStamp()));
    	}else{
    		emails.sort((a,b)->b.getMsgSentTimeStamp().compareToIgnoreCase(a.getMsgSentTimeStamp()));
    	}
    }*/
     
    //////////////////////////////////////////////////////
    
    
    /**
     * To create new message
     * 
     * @param email
     * @param map
     * @return
     */
    @PreAuthorize("hasAuthority('CREATE_MAIL') and " + LicenseSecurityConstants.LICENSE_INTER_USER_EMAIL)
    @RequestMapping(value = "/save", method = RequestMethod.POST)
    public String saveEmail(UserMailNotificationVO email, ModelMap map) {
        try {
            DateTime d = DateUtils.getCurrentUTCTime();
            User user = getUserDetails().getUserReference();

            if (user != null) {

                /*
                 * Get Data From UI And First Set The CommonMailContent and then
                 * UserMailNotification
                 */
                CommonMailContent cmc = new CommonMailContent();
                cmc.setBody(email.getBody().replaceAll("\r\n", "<br/>"));
                cmc.setFromUserUri(getUserDetails().getUserEntityId().getUri());
                cmc.setMsgSentTimeStamp(d);
                cmc.setSubject(email.getSubject());
                cmc.getEntityLifeCycleData().setCreatedByUri(user.getUri());
                List<String> arr = email.getToList();

                List<User> userList = getListofUsers();

                Map<String, Long> usernameid = new HashMap<String, Long>();
                Set<String> userUriList = new HashSet<String>();
                for (User u : userList) {
                    usernameid.put(u.getUsername(), u.getId());
                    if (arr.contains(u.getId().toString())) {
                        userUriList.add(u.getUri());
                    }
                }
                inAppMailHelper.sendNotificationAndCorporateEmails(userUriList, cmc, false,
                        email.getMailNotificationPriority());

                map.put("masterID", masterId);

                EntityId entityId = EntityId.fromUri(user.toString());
                map.put("userList", userList);

                Map<String, List<UserMailNotificationVO>> dataMap =  new HashMap<>();
                map.put("entityID", entityId);
                map.put("emailNotifier", dataMap);

            }
        } catch (Exception e) {
            exceptionLogger.error("Exception : ", e);
        }
        return "redirect:/app/email/viewMailBox";

    }


    /**
     * To show message on click of a particular row of table
     */

    /*@PreAuthorize(LicenseSecurityConstants.LICENSE_INTER_USER_EMAIL)
    @RequestMapping(value = "/viewMessage/{recordId}/{mailListType}", method = RequestMethod.GET)
    public @ResponseBody
    UserMailNotificationVO viewMessage(@PathVariable("recordId") String recordId, @PathVariable String mailListType, ModelMap map) {
    	boolean isValidRequest=false;
        UserMailNotificationVO mailNotify = new UserMailNotificationVO();

        User user = getUserDetails().getUserReference();
        String userId=user.getId().toString();
        UserMailNotification oldNotification = userMailNotificationService.getNotification(Long.valueOf(recordId));
        fwCacheHelper.detachEntity(oldNotification);
       
        UserMailNotification notification = userMailNotificationService.getNotification(Long.valueOf(recordId));
        
        CommonMailContent cmc = notification.getCommonMailContent();
       
        //get the type
        //if inbox/trash/outbox
        //for outbox, check is the fromUserUri , for inbox ..toUserUri , for trash check both
        String type =TYPE_INBOX_LIST.equalsIgnoreCase(mailListType)?UserMailNotificationType.TYPE_INBOX:
        	TYPE_OUTBOX_LIST.equalsIgnoreCase(mailListType)?UserMailNotificationType.TYPE_OUTBOX:
        		TYPE_TRASH_LIST.equalsIgnoreCase(mailListType)?UserMailNotificationType.TYPE_TRASH:"";
        
        List<String> touserList = new ArrayList<String>();
        List<String> touserListId = new ArrayList<String>();
        UserInfo sendBy = null;
        if (notification.getToUserUriList() == null) {
            sendBy = userService.getUserById(EntityId.fromUri(
                    notification.getToUserUri() == null ? "" : notification.getToUserUri().toString()).getLocalId());
            touserList.add(sendBy.getDisplayName() == null ? "" : sendBy.getDisplayName().toString());
            touserListId.add(sendBy.getId().toString());
        } else {
            String[] toUserUriList = notification.getToUserUriList().split(",");
            for (String toUser : toUserUriList) {
                sendBy = userService.getUserById(EntityId.fromUri(toUser == null ? "" : toUser).getLocalId());
                touserList.add(sendBy.getDisplayName() == null ? "" : sendBy.getDisplayName().toString());
                touserListId.add(sendBy.getId().toString());
            }

        }
        UserInfo createdBy = userService.getUserById(EntityId.fromUri(
    			cmc.getFromUserUri() == null ? "" : cmc.getFromUserUri().toString()).getLocalId());
        
        isValidRequest =isValidEmailRequest(type,touserListId,createdBy,userId);
      
        if(isValidRequest){
        	notification.setMsgStatus(UserMailNotificationType.USER_MAIL_READ);
        	userMailNotificationService.updateStatus(notification,oldNotification);
        	mailNotify.setSubject(cmc.getSubject() == null ? "" : cmc.getSubject().toString());
        	mailNotify.setFromUser(createdBy.getDisplayName() == null ? "" : createdBy.getDisplayName());
        	mailNotify.setFromUserId(createdBy.getId().toString());
        	mailNotify.setToList(touserList);
        	mailNotify.setToListId(touserListId);
        	if (cmc.getMsgSentTimeStamp() != null) {
        		DateTime calendar = cmc.getMsgSentTimeStamp();
        		mailNotify.setMsgSentTimeStamp(calendar == null ? "" : getFormattedDate(calendar));
        	}
        	mailNotify.setBody(cmc.getBody() == null ? "" : cmc.getBody().toString());
        	if (notification.getMailNotificationPriority() != null && !(notification.getMailNotificationPriority().isEmpty())) {
        		mailNotify.setMailNotificationPriority(cmc.getMsgSentTimeStamp() == null ? "" : notification
        				.getMailNotificationPriority().toString());
        	} else {
        		mailNotify.setMailNotificationPriority("");
        	}
        	mailNotify.setNotificationID(notification.getId());

        	EntityId entityId = EntityId.fromUri(user.toString());

        	List<UserMailNotification> successiveNotificatios = userMailNotificationService.getNextSuccessiveMailNotification(entityId.getUri(),type, Long.valueOf(recordId)); 
        	Long  prevNotificationId =getIdForMailNotificationRecordNavigation(successiveNotificatios,SUCCESSOR);
        	mailNotify.setPreviousMailUserId(prevNotificationId!=null?prevNotificationId:notification.getId());

        	List<UserMailNotification> predecessorNotificatios = userMailNotificationService.getPredecessorMailNotification(entityId.getUri(),type, Long.valueOf(recordId));
        	Long  nextNotificationId =getIdForMailNotificationRecordNavigation(predecessorNotificatios,PREDECESSOR);
        	mailNotify.setNextMailUserId(nextNotificationId);
        	mailNotify.setError(FALSE);
        	
        }else{
        	mailNotify.setError("User is not authorized to view this mail message.");
        }
        return mailNotify;
    }*/

    /*
     * Get the successor OR predecessor notification id for Notification view navigation
     * */
   /* private Long getIdForMailNotificationRecordNavigation(List<UserMailNotification> notifications,String type){
    	Long id=null;
    	if(SUCCESSOR.equalsIgnoreCase(type)){
    		id= !notifications.isEmpty()?
        			notifications.size()>=2?notifications.get(notifications.size()-2).getId():null:null;	
    	}else if(PREDECESSOR.equalsIgnoreCase(type)){
    		id= !notifications.isEmpty()?
        			notifications.size()>=2?notifications.get(1).getId():null:null;
    	}
    	return id;
    }
    */
    
    /**
     * Currently it deletes message from inbox
     */

    @PreAuthorize("hasAuthority('DELETE_MAIL') and " + LicenseSecurityConstants.LICENSE_INTER_USER_EMAIL)
    @RequestMapping(value = "/deleteMsg", method = RequestMethod.POST)
    @ResponseBody
   /* public void deleteMessasge(@RequestParam Long[] idList) {

        *//**
         * when message is deleted from in-box tab
         *//*

        if (idList[idList.length - 1] == 1L) {
            idList = Arrays.copyOf(idList, idList.length - 1);
            for (Long id : idList) {
            	UserMailNotification oldMailNotification = userMailNotificationService.getNotification(id);
            	fwCacheHelper.detachEntity(oldMailNotification);
            	
            	UserMailNotification mailNotification = userMailNotificationService.getNotification(id);
                checkUserAccess(EmailOperation.DEL_FROM_INBOX,mailNotification);

                if (mailNotification.getMsgStatus().equalsIgnoreCase(UserMailNotificationType.USER_MAIL_NEW)
                        || mailNotification.getMsgStatus().equalsIgnoreCase(UserMailNotificationType.USER_MAIL_READ))
                    mailNotification.setMsgStatus(UserMailNotificationType.USER_MAIL_DELETED_FROM_INBOX);

                else if (mailNotification.getMsgStatus().equalsIgnoreCase(
                        UserMailNotificationType.USER_MAIL_DELETED_FROM_OUTBOX)) {

                    mailNotification.setMsgStatus(UserMailNotificationType.USER_MAIL_DELETED_FROM_INBOX_AND_OUTBOX);
                } else if (mailNotification.getMsgStatus().equalsIgnoreCase(
                        UserMailNotificationType.USER_MAIL_DELETED_FROM_OUTBOX_TRASH))
                    mailNotification.setMsgStatus(UserMailNotificationType.USER_MAIL_DELETED_FROM_OUTBOX_TRASH_INBOX);
                userMailNotificationService.updateStatus(mailNotification,oldMailNotification);
            }
        }*/

        /**
         * message is deleted from out-box
         */
       /* else if (idList[idList.length - 1] == 2L) {
            idList = Arrays.copyOf(idList, idList.length - 1);
             mailbox=UserMailNotificationType.USER_MAIL_DELETED_FROM_OUTBOX; 
            for (Long id : idList) {
            	UserMailNotification oldUserMailNotification = userMailNotificationService.getNotification(id);
            	fwCacheHelper.detachEntity(oldUserMailNotification);
                UserMailNotification mailNotification = userMailNotificationService.getNotification(id);
                checkUserAccess(EmailOperation.DEL_FROM_OUTBOX,mailNotification);
            	if (mailNotification.getMsgStatus().equalsIgnoreCase(UserMailNotificationType.USER_MAIL_NEW)
                        || mailNotification.getMsgStatus().equalsIgnoreCase(UserMailNotificationType.USER_MAIL_READ))
                    mailNotification.setMsgStatus(UserMailNotificationType.USER_MAIL_DELETED_FROM_OUTBOX);

                else if (mailNotification.getMsgStatus().equalsIgnoreCase(
                        UserMailNotificationType.USER_MAIL_DELETED_FROM_INBOX))
                    mailNotification.setMsgStatus(UserMailNotificationType.USER_MAIL_DELETED_FROM_INBOX_AND_OUTBOX);

                else if (mailNotification.getMsgStatus().equalsIgnoreCase(
                        UserMailNotificationType.USER_MAIL_DELETED_FROM_INBOX_TRASH))
                    mailNotification.setMsgStatus(UserMailNotificationType.USER_MAIL_DELETED_FROM_INBOX_TRASH_OUTBOX);
                userMailNotificationService.updateStatus(mailNotification,oldUserMailNotification);
            }
        }*/

        /**
         * message is deleted from trash
         */
      /*  else if (idList[idList.length - 1] == 3L) {
            idList = Arrays.copyOf(idList, idList.length - 1);
             mailbox = UserMailNotificationType.USER_MAIL_DELETED_FROM_TRASH; 
            for (Long id : idList) {
            	UserMailNotification oldUserMailNotification = userMailNotificationService.getNotification(id);
            	fwCacheHelper.detachEntity(oldUserMailNotification);
                UserMailNotification mailNotification = userMailNotificationService.getNotification(id);
                checkUserAccess(EmailOperation.DEL_FROM_TRASH,mailNotification);
                User user = getUserDetails().getUserReference();
                EntityId entityId = EntityId.fromUri(user.toString());
                if (mailNotification.getMsgStatus().equalsIgnoreCase(UserMailNotificationType.USER_MAIL_DELETED_FROM_INBOX)
                        || mailNotification.getMsgStatus().equalsIgnoreCase(UserMailNotificationType.USER_MAIL_READ))
                    mailNotification.setMsgStatus(UserMailNotificationType.USER_MAIL_DELETED_FROM_INBOX_TRASH);
                else if (mailNotification.getMsgStatus().equalsIgnoreCase(
                        UserMailNotificationType.USER_MAIL_DELETED_FROM_OUTBOX))
                    mailNotification.setMsgStatus(UserMailNotificationType.USER_MAIL_DELETED_FROM_OUTBOX_TRASH);
                else if (mailNotification.getMsgStatus().equalsIgnoreCase(
                        UserMailNotificationType.USER_MAIL_DELETED_FROM_INBOX_TRASH_OUTBOX))
                    mailNotification.setMsgStatus(UserMailNotificationType.USER_MAIL_DELETED);
                else if (mailNotification.getMsgStatus().equalsIgnoreCase(
                        UserMailNotificationType.USER_MAIL_DELETED_FROM_OUTBOX_TRASH_INBOX))
                    mailNotification.setMsgStatus(UserMailNotificationType.USER_MAIL_DELETED);
                else if (mailNotification.getMsgStatus().equalsIgnoreCase(
                        UserMailNotificationType.USER_MAIL_DELETED_FROM_INBOX_AND_OUTBOX)
                        && mailNotification.getToUserUri().equals(entityId.getUri())
                        && !mailNotification.getCommonMailContent().getFromUserUri().equals(entityId.getUri()))
                    mailNotification.setMsgStatus(UserMailNotificationType.USER_MAIL_DELETED_FROM_OUTBOX_INBOX_TRASH);
                else if (mailNotification.getMsgStatus().equalsIgnoreCase(
                        UserMailNotificationType.USER_MAIL_DELETED_FROM_INBOX_AND_OUTBOX)
                        && !mailNotification.getToUserUri().equals(entityId.getUri())
                        && mailNotification.getCommonMailContent().getFromUserUri().equals(entityId.getUri()))
                    mailNotification.setMsgStatus(UserMailNotificationType.USER_MAIL_DELETED_FROM_INBOX_OUTBOX_TRASH);
                else if (mailNotification.getMsgStatus().equalsIgnoreCase(
                        UserMailNotificationType.USER_MAIL_DELETED_FROM_INBOX_AND_OUTBOX)
                        && mailNotification.getToUserUri().equals(entityId.getUri())
                        && mailNotification.getCommonMailContent().getFromUserUri().equals(entityId.getUri()))
                    mailNotification.setMsgStatus(UserMailNotificationType.USER_MAIL_DELETED);
                else if (mailNotification.getMsgStatus().equalsIgnoreCase(
                        UserMailNotificationType.USER_MAIL_DELETED_FROM_INBOX_OUTBOX_TRASH))
                    mailNotification.setMsgStatus(UserMailNotificationType.USER_MAIL_DELETED);
                else if (mailNotification.getMsgStatus().equalsIgnoreCase(
                        UserMailNotificationType.USER_MAIL_DELETED_FROM_OUTBOX_INBOX_TRASH))
                    mailNotification.setMsgStatus(UserMailNotificationType.USER_MAIL_DELETED);
                userMailNotificationService.updateStatus(mailNotification,oldUserMailNotification);
            }
        }

    }
*/
	private void checkUserAccess(EmailOperation emailOperation, UserMailNotification mailNotification) {
		String currentUserUri = getUserDetails().getUserEntityId().getUri();
		if (emailOperation == EmailOperation.DEL_FROM_INBOX) {
			if (!isInboxMessageRelatesToUser(currentUserUri,mailNotification)) {
				throw new AccessDeniedException("User is not authorized to delete this mail message.");
			}
			return;
		}
		if (emailOperation == EmailOperation.DEL_FROM_OUTBOX) {
			if (!isOutboxMessageRelatesToUser(currentUserUri,mailNotification)) {
				throw new AccessDeniedException("User is not authorized to delete this mail message.");
			}
			return;
		}

		if (emailOperation == EmailOperation.DEL_FROM_TRASH) {
			if (!isInboxMessageRelatesToUser(currentUserUri, mailNotification)
					|| !isOutboxMessageRelatesToUser(currentUserUri, mailNotification)) {
				throw new AccessDeniedException("User is not authorized to delete this mail message.");
			}
		}

	}

	private boolean isOutboxMessageRelatesToUser(String currentUserUri, UserMailNotification mailNotification) {

		return currentUserUri.equalsIgnoreCase(mailNotification.getCommonMailContent().getFromUserUri());
	}

	private boolean isInboxMessageRelatesToUser(String currentUserUri, UserMailNotification mailNotification) {
		return (currentUserUri.equalsIgnoreCase(mailNotification.getToUserUri())
				|| (mailNotification.getToUserUriList() != null
				&& mailNotification.getToUserUriList().contains(currentUserUri)));
	}

	/*@PreAuthorize(LicenseSecurityConstants.LICENSE_INTER_USER_EMAIL)
    @RequestMapping(value = "/count", method = RequestMethod.GET)
    @MonitoredWithSpring(name = "EC_FETCH_MAIL_COUNT")
    @ResponseBody
    public long getCount(UserMailNotificationVO email, ModelMap map) {
        long notificationsSize = 0L;
        try {
            // we need to get below logged in user from session
            User user = getUserDetails().getUserReference();
			notificationsSize = userMailNotificationService.getUserNotificationsCount(user.getUri(), STAT_LIST,
					"inbox");
        } catch (Exception e) {
            exceptionLogger.error("Exception : ", e);
        }
        return notificationsSize;
    }*/

    /*@PreAuthorize(LicenseSecurityConstants.LICENSE_INTER_USER_EMAIL)
    @RequestMapping(value = "/countOutbox", method = RequestMethod.GET)
    @ResponseBody
    public long getCountOutbox(UserMailNotificationVO email, ModelMap map) {
        long notificationsSize = 0L;
        try {
            // we need to get below logged in user from session
            User user = getUserDetails().getUserReference();
            List<String> statOutboxList = new ArrayList<String>();
            statOutboxList.add(UserMailNotificationType.USER_MAIL_NEW);
            statOutboxList.add(UserMailNotificationType.USER_MAIL_READ);
            statOutboxList.add(UserMailNotificationType.USER_MAIL_DELETED_FROM_INBOX);
            statOutboxList.add(UserMailNotificationType.USER_MAIL_DELETED_FROM_INBOX_TRASH);

            notificationsSize = userMailNotificationService.getUserNotificationsCount(user.getUri(), statOutboxList,
                    "outbox");
        } catch (Exception e) {
            exceptionLogger.error("Exception : ", e);
        }
        return notificationsSize;
    }
*/
   /* @PreAuthorize(LicenseSecurityConstants.LICENSE_INTER_USER_EMAIL)
    @RequestMapping(value = "/countTrash", method = RequestMethod.GET)
    @ResponseBody
    public long getCountTrash(UserMailNotificationVO email, ModelMap map) {
        long notificationsSize = 0L;
        try {
            // we need to get below logged in user from session
            User user = getUserDetails().getUserReference();
            List<String> trashMapStatusList = new ArrayList<String>();
            trashMapStatusList.add(UserMailNotificationType.USER_MAIL_DELETED_FROM_INBOX);
            trashMapStatusList.add(UserMailNotificationType.USER_MAIL_DELETED_FROM_OUTBOX);

            notificationsSize = userMailNotificationService.getUserNotificationsCount(user.getUri(), trashMapStatusList,
                    "trash");
        } catch (Exception e) {
            exceptionLogger.error("Exception : ", e);
        }
        return notificationsSize;
    }
*/
    public List<User> getListofUsers() {

        List<User> ul = new ArrayList<User>();
        ul = userService.getAllUser();

        return ul;
    }

    /**
     * 
     * Method to send mail to a specific recipient through contacts tab
     * 
     */
/*
    @PreAuthorize(LicenseSecurityConstants.LICENSE_INTER_USER_EMAIL)
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @RequestMapping(value = "/sendMail/{user}", method = RequestMethod.GET)
    public String sendMail(UserMailNotificationVO email, ModelMap map, @PathVariable("user") String username) {
        try {
            // we need to get below logged in user from session

            User user = getUserDetails().getUserReference();
            Map<String, List<UserMailNotificationVO>> dataMap =new HashMap<>();
            map.put("emailNotifier", dataMap);
            map.put("redirectedPage", true);
            map.put("masterID", masterId);
            
            if (inAppMailHelper.checkUserEnabled(user.getUri())) {
                map.put("inboxDisabled", true);
            }
            
            List<Map<String, String>> nameList = prepareUserProfileList(username);

            map.put("userProfileList", nameList);
            
            
        } catch (Exception e) {
            exceptionLogger.error("Exception : ", e);
        }
        return "mailbox";
    }*/

   /* @PostMapping("/{type}/favourite")
    @ResponseBody
    public UserMailNotificationVO saveFavourite(@PathVariable String type, UserMailNotificationVO userMailNotificationVO) {
        try {
        	UserMailNotification mailNotification = userMailNotificationService.getNotification(Long.valueOf(userMailNotificationVO.getUserId()));        	
        	CommonMailContent commonMailContent = mailNotification.getCommonMailContent();
        	
             List<String> toUserListId = new ArrayList<>();
             UserInfo sendBy = null;
             if (mailNotification.getToUserUriList() == null) {
                 sendBy = userService.getUserById(EntityId.fromUri(
                		 mailNotification.getToUserUri() == null ? "" : mailNotification.getToUserUri()).getLocalId());
                 toUserListId.add(sendBy.getId().toString());
             } else {
                 String[] toUserUriList = mailNotification.getToUserUriList().split(",");
                 for (String toUser : toUserUriList) {
                     sendBy = userService.getUserById(EntityId.fromUri(toUser == null ? "" : toUser).getLocalId());
                     toUserListId.add(sendBy.getId().toString());
                 }

             }
             UserInfo createdBy = userService.getUserById(EntityId.fromUri(commonMailContent.getFromUserUri() == null ? "" 
            		 														: commonMailContent.getFromUserUri()).getLocalId());
             
             boolean isValidRequest =isValidEmailRequest(type,toUserListId,createdBy,getUserDetails().getUserReference().getId().toString());
             if(isValidRequest){
            	commonMailContent.setFavourite(userMailNotificationVO.getFavourite());
             	userMailNotificationService.saveNotification(mailNotification);
             	
             	userMailNotificationVO.setError(FALSE);
             	userMailNotificationVO.setFavourite(true);
             }else{
            	userMailNotificationVO.setError("User is not authorized for this email");
              	userMailNotificationVO.setFavourite(false);
             }
        } catch (Exception e) {
            exceptionLogger.error("Exception : ", e);
        }
        return userMailNotificationVO;
    }
    */
   
  
	
	 @PreAuthorize("hasAuthority('VIEW_MAILBOX') and " + LicenseSecurityConstants.LICENSE_INTER_USER_EMAIL)
    @RequestMapping(value = "/viewAllMail", method = RequestMethod.GET)
    public @ResponseBody List<UserMailNotificationVO> emailJson() {
        int Default_View_Box_Size=3;
        List<UserMailNotificationVO> mailList = new ArrayList<UserMailNotificationVO>();
       /* try {
            List<String> statList = new ArrayList<String>();
            statList.add(UserMailNotificationType.USER_MAIL_NEW);
            statList.add(UserMailNotificationType.USER_MAIL_DELETED_FROM_OUTBOX);
            statList.add(UserMailNotificationType.USER_MAIL_DELETED_FROM_OUTBOX_TRASH);
            List<Map<String, ?>> notifications = userMailNotificationService.getUserNotifications(userService.getUserUriByUserName(getUserDetails().getUsername()),
                    statList, UserMailNotificationType.TYPE_INBOX, null, Default_View_Box_Size);
            for (Map<String, ?> notification : notifications) {
                UserMailNotificationVO mailNotify = new UserMailNotificationVO();
                CommonMailContent cmc = (CommonMailContent) notification.get("commonMailContent");
                UserInfo createdBy = userService.getUserById(EntityId.fromUri(
                        cmc.getFromUserUri() == null ? "" : cmc.getFromUserUri().toString()).getLocalId());
                mailNotify.setFromUser(createdBy.getDisplayName() == null ? "" : createdBy.getDisplayName());
                mailNotify.setSubject(cmc.getSubject() == null ? "" : cmc.getSubject().toString());
                if (cmc.getMsgSentTimeStamp() != null) {
                    DateTime calendar = cmc.getMsgSentTimeStamp();
                    mailNotify.setMsgSentTimeStamp(calendar == null ? "" : getFormattedDate(calendar));
                }
                if (notification.get("msgStatus").equals(UserMailNotificationType.USER_MAIL_READ)) {
                    mailNotify.setReadStatus(true);
                }
                mailList.add(mailNotify);

            }
            //map.put("emailNotifier", mailList);
        } catch (Exception e) {
            exceptionLogger.error("Exception : ", e);
        }*/

        return mailList;
    }
	
}