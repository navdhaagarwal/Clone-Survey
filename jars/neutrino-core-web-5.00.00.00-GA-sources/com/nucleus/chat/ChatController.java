package com.nucleus.chat;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.nucleus.core.chat.ChatMessageVO;
import com.nucleus.user.UserInfo;
import com.nucleus.web.common.controller.BaseController;

@PreAuthorize("hasAuthority('CHAT_ENABLED')")
@Controller
@RequestMapping("/chat")
public class ChatController extends BaseController {

    @Inject
    @Named("chatService")
    private ChatService chatService;

    @ResponseBody
    @RequestMapping(value = "openChat")
    public ChatMessageVO openChat(@RequestParam("toUser") String toUser, ModelMap map) {
        map.put("fromUser", getUserDetails().getId().toString());
        map.put("toUser", toUser);
        ChatMessageVO g = chatService.initiateChat(getUsername(), toUser);
        return g;
    }

    @ResponseBody
    @RequestMapping(value = "addUserToChat")
    public ChatMessageVO addUserToChat(@RequestParam("fromUser") String fromUser, @RequestParam("addUser") String addUser,
            @RequestParam("chatId") Long chatId, ModelMap map) {

        return chatService.addUserToChat(chatId, addUser, fromUser);

    }

    @ResponseBody
    @RequestMapping(value = "gethistory")
    public List<ChatMessageVO> getHistory(@RequestParam("fromUser") String fromUser, @RequestParam("toUser") String toUser,
            @RequestParam("noOfDays") Long noOfDays, @RequestParam("chatId") Long chatId) throws Exception {

        return chatService.getChatHistory(fromUser, toUser, noOfDays, chatId);
        // return "olderChatMessage";
    }

    @RequestMapping(value = "loadLoggedinUsers")
    public String loadAllLoggedinUsers(@RequestParam("chatId") Long chatId, ModelMap map) {
        List<UserInfo> userList = chatService.loadLoggedInUsersForChat(chatId);
        map.put("userList", userList);
        return "chat/loggedinUsersForGroupChat";
    }

    /*   @RequestMapping(value = "/chat")
       public String openChats()
       {
           return "chat/chat";
       }*/
}