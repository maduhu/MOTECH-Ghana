package org.motech.messaging;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ScheduledMessage {

	Long id;
	Date scheduledFor;
	MessageDefinition message;
	Integer recipientId;
	List<Message> messageAttempts = new ArrayList<Message>();

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Date getScheduledFor() {
		return scheduledFor;
	}

	public void setScheduledFor(Date scheduledFor) {
		this.scheduledFor = scheduledFor;
	}

	public MessageDefinition getMessage() {
		return message;
	}

	public void setMessage(MessageDefinition message) {
		this.message = message;
	}

	public Integer getRecipientId() {
		return recipientId;
	}

	public void setRecipientId(Integer recipientId) {
		this.recipientId = recipientId;
	}

	public List<Message> getMessageAttempts() {
		return messageAttempts;
	}

	public void setMessageAttempts(List<Message> messageAttempts) {
		this.messageAttempts = messageAttempts;
	}

}
