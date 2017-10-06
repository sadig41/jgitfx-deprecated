package net.bbmsoft.jgitfx.messaging;

import net.bbmsoft.jgitfx.event.Topic;

public interface Messenger {

	public abstract void showMessage(Topic<Message> topic, Message message);
}
