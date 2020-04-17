package com.nucleus.mail.service;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import javax.mail.Flags.Flag;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;

import com.nucleus.logging.BaseLoggers;

public abstract class AbstractMailboxManagerService implements MailboxManagerService {

	private static final int BATCH_SIZE = 200;
	private static final String DEFAULT_FOLDER_NAME = "INBOX"; //only inbox is supported in pop3s.

	private Store getStore() throws MessagingException {
		Properties mailProperties = getDefaultMailBoxProperties();
		Session emailSession = Session.getDefaultInstance(mailProperties);
		Store store = emailSession.getStore(mailProperties.getProperty("mail.store.protocol"));
		store.connect(mailProperties.getProperty("mail.pop3.host"), mailProperties.getProperty("username"),  mailProperties.getProperty("password"));
		return store;
	}

	@Override
	public List<Message> getAllMessages() {
		try {
			Store store = getStore();
			Folder emailFolder = store.getFolder(DEFAULT_FOLDER_NAME);
			emailFolder.open(Folder.READ_ONLY);
			Message[] messages = emailFolder.getMessages();
			return Arrays.asList(messages);
		} catch (MessagingException e) {
			return Collections.emptyList();
		}
	}

	@Override
	public List<Message> getAllUnreadMessages() {
		try {
			Store store = getStore();
			Folder emailFolder = store.getFolder(DEFAULT_FOLDER_NAME);
			emailFolder.open(Folder.READ_WRITE);
			Message[] messages = emailFolder.getMessages();
			List<Message> unreadMessages = new LinkedList<>();
			for (Message message : messages) {
				if (!message.getFlags().contains(Flag.SEEN)) {
					unreadMessages.add(message);
				}
			}
			return unreadMessages;
		} catch (MessagingException me) {
			return Collections.emptyList();
		}
	}

	@Override
	public boolean deleteMessagesPermanently() {
		try {
			Store store = getStore();
			Folder emailFolder = store.getFolder(DEFAULT_FOLDER_NAME);
			return emailFolder.expunge().length > 0 ? Boolean.TRUE : Boolean.FALSE;
		} catch (MessagingException e) {
			BaseLoggers.exceptionLogger.debug("Error while permanent deletion of all records that are already marked delete.");
			return false;
		}
	}

	@Override
	public void processUnreadMessagesInBatchAndMarkFlag(Flag flag, MessageProcessor<?> messageProcessor) {
		try {
			Store store = getStore();
			Folder emailFolder = store.getFolder(DEFAULT_FOLDER_NAME);
			emailFolder.open(Folder.READ_WRITE);
			int unreadMessagesCount = emailFolder.getUnreadMessageCount();
			int totalMessageCount = emailFolder.getMessageCount();
			int end = 0;
			int start = 1; //Mail service support indexes from 1, not from 0.
			boolean done = false;
			int parsedUnreadMessagesCount = 0;
			while (!done && unreadMessagesCount > parsedUnreadMessagesCount) {
				end = start + (BATCH_SIZE < totalMessageCount - (start - 1) ? BATCH_SIZE : totalMessageCount - (start - 1));
				Message[] messages = emailFolder.getMessages(start, end);
				for (Message message : messages) {
					if (!message.getFlags().contains(Flag.SEEN)) {
						messageProcessor.processMessage(message);
					} else {
						message.setFlag(Flag.DELETED, Boolean.TRUE);
					}
					parsedUnreadMessagesCount++;
				}
				if ((end - start) < BATCH_SIZE) {
					done = true;
				} else {
					start = end;
				}
			}
		} catch (MessagingException me) {
			BaseLoggers.exceptionLogger.error("Error while processing unread messsages. It may be because email could not be read.");
		}
	}

}
