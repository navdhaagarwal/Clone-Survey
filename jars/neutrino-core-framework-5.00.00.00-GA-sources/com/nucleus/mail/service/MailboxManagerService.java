package com.nucleus.mail.service;

import java.util.List;
import java.util.Properties;

import javax.mail.Flags.Flag;
import javax.mail.Message;

public interface MailboxManagerService {
	
	List<Message> getAllMessages();
	
	List<Message> getAllUnreadMessages();
	
	boolean deleteMessagesPermanently();
	
	void processUnreadMessagesInBatchAndMarkFlag(Flag flag, MessageProcessor<?> messageProcessor);
	
	Properties getDefaultMailBoxProperties();

}
