package net.bbmsoft.jgitfx.messaging;

import net.bbmsoft.jgitfx.event.EventBroker.Topic;

public enum MessageType implements Topic<Message> {
	
	SUCCESS, ERROR, INFO;
}