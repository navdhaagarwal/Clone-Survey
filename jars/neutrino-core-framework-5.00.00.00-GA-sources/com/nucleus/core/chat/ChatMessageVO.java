package com.nucleus.core.chat;

import java.util.Set;

import org.joda.time.DateTime;

public class ChatMessageVO {

    private String      content;
    private String      header;
    private String      toUser;
    private String      fromUser;
    private Long        chatId;
    private DateTime    time;
    private Set<String> users;

    /**
     * 
     * This constructor is user
     * @param header
     * @param toUser
     * @param fromUser
     * @param content
     */
    private ChatMessageVO() {
        time = new DateTime();
    }

    public static ChatMessageVO getChatMessageVo(ChatMessage chatMessage) {
        ChatMessageVO chatMessageVO = new ChatMessageVO();
        chatMessageVO.chatId = chatMessage.getChatId().getId();
        chatMessageVO.header = chatMessage.getHeader();
        chatMessageVO.content = chatMessage.getContent();
        chatMessageVO.fromUser = chatMessage.getFromUser().getUsername();
        chatMessageVO.time = chatMessage.getTime();

        return chatMessageVO;
    }

    public String getHeader() {
        return header;
    }

    public String getToUser() {
        return toUser;
    }

    public String getFromUser() {
        return fromUser;
    }

    public String getContent() {
        return content;
    }

    /**
     * 
     * to start a new chat between fromUser and toUser
     * @param fromUser
     * @param toUser
     * @return
     */
    public static ChatMessageVO startNewChat(String fromUser, String toUser) {
        ChatMessageVO greeting = new ChatMessageVO();

        greeting.header = "newChat";
        greeting.fromUser = fromUser;
        greeting.toUser = toUser;
        // greeting.chatId = fromUser+toUser;
        return greeting;
    }

    public static ChatMessageVO chat(String header, String fromUser, String toUser, Long chatId, String content) {
        ChatMessageVO greeting = new ChatMessageVO();

        greeting.header = header;
        greeting.fromUser = fromUser;
        greeting.toUser = toUser;
        greeting.chatId = chatId;
        greeting.content = content;
        return greeting;
    }

    public Long getChatId() {
        return chatId;
    }

    public void setChatId(Long chatId) {
        this.chatId = chatId;
    }

    public DateTime getTime() {
        return time;
    }

    public void setTime(DateTime time) {
        this.time = time;
    }

    public Set<String> getUsers() {
        return users;
    }

    public void setUsers(Set<String> users) {
        this.users = users;
    }

    public static ChatMessageVO startNewGroupChat(String fromUser, Set<String> users) {
        ChatMessageVO greeting = new ChatMessageVO();

        greeting.header = "newGroupChat";
        greeting.fromUser = fromUser;
        greeting.users = users;
        // greeting.chatId = fromUser+toUser;
        return greeting;
    }

}