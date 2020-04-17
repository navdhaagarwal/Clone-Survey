package com.nucleus.chat;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;

import org.joda.time.DateTime;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import com.nucleus.core.chat.Chat;
import com.nucleus.core.chat.ChatMessage;
import com.nucleus.core.chat.ChatMessageVO;
import com.nucleus.core.validation.util.NeutrinoValidator;
import com.nucleus.dao.query.NamedQueryExecutor;
import com.nucleus.entity.ApprovalStatus;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.persistence.EntityDao;
import com.nucleus.service.BaseServiceImpl;
import com.nucleus.user.User;
import com.nucleus.user.UserInfo;
import com.nucleus.user.UserService;
import com.nucleus.user.UserSessionManagerService;

import net.bull.javamelody.MonitoredWithSpring;

@Named(value = "chatService")
@MonitoredWithSpring(name = "chatService_IMPL_")
public class ChatServiceImpl extends BaseServiceImpl implements ChatService {

    @Inject
    private SimpMessagingTemplate     messagingTemplate;

    // private MessageSendingOperations<String> messagingTemplate;

    @Inject
    @Named("userSessionManagerService")
    private UserSessionManagerService userSessionManagerService;

    @Inject
    @Named("userService")
    private UserService               userService;

    @Inject
    @Named(value = "entityDao")
    private EntityDao                 entityDao;

    private final String              GROUP = "Group";

    @Override
    public ChatMessageVO initiateChat(String fromUser, String toUser) {
        ChatMessageVO g = ChatMessageVO.startNewChat(fromUser, toUser);
        createChat(g);
        sendNewChatMsg(g, toUser);
        return g;
    }

    public void sendNewChatMsg(ChatMessageVO g, String toUser) {
        String destination = "/topic/greetings";
        messagingTemplate.convertAndSend(destination + toUser, g);
    }

    @Override
    public ChatMessageVO initiateGroupChat(Set<String> users, String fromUser) {
        ChatMessageVO g = ChatMessageVO.startNewGroupChat(fromUser, users);

        // String user = fromUser + toUser;
        // messagingTemplate.convertAndSendToUser(user, destination, g);
        createGroupChat(g);
        // saveChatmessage(g);
        for (String toUser : users) {
            sendNewChatMsg(g, toUser);
        }
        return g;
    }

    private Long createGroupChat(ChatMessageVO g) {
        Chat chat = new Chat();
        chat.setChatType("Group");
        Set<User> users = new HashSet<User>();
        Set<String> userStrs = g.getUsers();
        for (String user : userStrs) {
            users.add(userService.findUserByUsername(user));
        }

        chat.setUsers(users);
        chat.setUserInitiated(userService.findUserByUsername(g.getFromUser()));
        entityDao.persist(chat);
        g.setChatId(chat.getId());
        return chat.getId();
    }

    @Override
    public void sendChatMessage(ChatMessageVO greeting) {
        sendNewChatMsg(greeting, greeting.getChatId().toString());
        saveChatmessage(greeting);
    }

    private Long createChat(ChatMessageVO chatVO) {
        Chat chat = new Chat();
        Set<User> users = new HashSet<User>();
        users.add(userService.findUserByUsername(chatVO.getFromUser()));
        users.add(userService.findUserByUsername(chatVO.getToUser()));
        chat.setUsers(users);
        chat.setUserInitiated(userService.findUserByUsername(chatVO.getFromUser()));
        entityDao.persist(chat);
        chatVO.setChatId(chat.getId());
        return chat.getId();
    }

    private void saveChatmessage(ChatMessageVO chatVo) {
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setContent(chatVo.getContent());
        chatMessage.setHeader(chatVo.getHeader());
        chatMessage.setFromUser(userService.findUserByUsername(chatVo.getFromUser()));
        Chat chat = getChatById(chatVo.getChatId());

        NeutrinoValidator.notNull(chat, "Chat Id does not exists");

        chatMessage.setChatId(chat);

        chatMessage.setTime(chatVo.getTime());
        entityDao.persist(chatMessage);
    }

    public ChatMessageVO addUserToChat(Long chatId, String addUser, String fromUser) {
        Chat chat = getChatById(chatId);
        chat.setChatType(GROUP);
        chat.addUser(userService.findUserByUsername(addUser));
        entityDao.update(chat);
        ChatMessageVO g = ChatMessageVO.chat("newChat", fromUser, addUser, chatId, addUser + " is added to this chat by "
                + fromUser);
        sendNewChatMsg(g, addUser);
        return g;
    }

    public Chat getChatById(Long chatId) {
        NeutrinoValidator.notNull(chatId);
        return entityDao.find(Chat.class, chatId);
    }

    public List<ChatMessageVO> getChatHistory(String fromUser, String toUser, Long noOfDays, Long chatId) {
        NeutrinoValidator.notNull(fromUser);
        NeutrinoValidator.notNull(toUser);
        NeutrinoValidator.notNull(noOfDays);

        DateTime date = new DateTime().minusDays(noOfDays.intValue());
        NamedQueryExecutor<ChatMessage> chatExecutor = new NamedQueryExecutor<ChatMessage>("chat.getHistoryByPeriod")
                .addParameter("date", date).addParameter("fromUser", fromUser).addParameter("toUser", toUser)
                .addParameter("chatId", chatId)
                .addParameter("approvalStatusList", ApprovalStatus.APPROVED_RECORD_STATUS_IN_APPROVED_APPROVED_MODIFIED);
        List<ChatMessage> chatMessages = entityDao.executeQuery(chatExecutor);
        List<ChatMessageVO> chatMessageVOs = new ArrayList<ChatMessageVO>();
        for (ChatMessage chatMessage : chatMessages) {
            chatMessageVOs.add(ChatMessageVO.getChatMessageVo(chatMessage));
        }
        return chatMessageVOs;
    }

    public List<UserInfo> loadLoggedInUsersForChat(Long chatId) {
        List<UserInfo> userList = getChatEnabledUsers(userSessionManagerService.getAllLoggedInUsers());
        
        
        // Remove current chat users from user list
        Set<User> users = getChatById(chatId).getUsers();
        for (User user : users) {
            userList.remove(userService.getUserById(user.getId()));
        }
        BaseLoggers.flowLogger.debug("UserList " + userList);
        return userList;
    }
    
    private List<UserInfo> getChatEnabledUsers(List<UserInfo> list){
    	List<UserInfo> userInfos = new ArrayList<>();
        for(UserInfo info:list){
        	if((info).isChatEnabled())
            {
                userInfos.add(info);
            }
        }
        return userInfos;
    } 
    
    public List<UserInfo> getUsersWithAuthority(List<Object> list,String AuthorityCode){
        List<UserInfo> userInfos = new ArrayList<UserInfo>();
        for(Object info:list){
            if(((UserInfo)info).getUserAuthorities().contains(userService.getAuthorityByCode(AuthorityCode)))
            {
                userInfos.add((UserInfo)info);
            }
        }
        return userInfos;
    }
}
