package com.nucleus.chat;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.nucleus.core.chat.ChatMessageVO;

@Controller
public class ChatMessageController {

@Inject
@Named("chatService")
private ChatService chatService;

@ResponseBody
@PreAuthorize("hasAuthority('CHAT_ENABLED')")
@RequestMapping("/webSocketEndPoint")
public void greeting(@RequestParam("header") String header,@RequestParam("chatId") Long chatId,
        @RequestParam("fromUser" ) String fromUser,
        @RequestParam("toUser" ) String toUser,
        @RequestParam("content" ) String cntnt) throws Exception {
    
    chatService.sendChatMessage(ChatMessageVO.chat(header, fromUser, toUser, chatId, cntnt));
}


//probably this is not working
//TODO @MessageMapping is not getting registered. May be Java 1.7 and Jetty 9 will fix the issue
/*@MessageMapping("/webSocketEndPoint")
@PreAuthorize("hasAuthority('CHAT_ENABLED')")
public void greetingmessage(ChatMessageVO chat) throws Exception {
  
  chatService.sendChatMessage(chat);
}*/
}
