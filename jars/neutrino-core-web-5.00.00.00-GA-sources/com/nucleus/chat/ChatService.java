package com.nucleus.chat;

import java.util.List;
import java.util.Set;

import com.nucleus.core.chat.Chat;
import com.nucleus.core.chat.ChatMessageVO;
import com.nucleus.user.UserInfo;

public interface ChatService {

    ChatMessageVO initiateChat(String fromUser, String toUser);

    void sendChatMessage(ChatMessageVO greeting);

    ChatMessageVO initiateGroupChat(Set<String> users, String fromUser);

    List<ChatMessageVO> getChatHistory(String fromUser, String toUser, Long noOfDays, Long chatId);

    public Chat getChatById(Long chatId);

    public List<UserInfo> loadLoggedInUsersForChat(Long chatId);

    public ChatMessageVO addUserToChat(Long chatId, String addUser, String fromUser);
}
