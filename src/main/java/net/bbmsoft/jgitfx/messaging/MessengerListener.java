package net.bbmsoft.jgitfx.messaging;

import net.bbmsoft.jgitfx.event.EventBroker;
import net.bbmsoft.jgitfx.event.EventBroker.Topic;

public class MessengerListener implements EventBroker.Listener<Message> {

	private Messenger messenger;

	public MessengerListener(Messenger messenger) {
		this.messenger = messenger;
	}
	
	@Override
	public void update(Topic<Message> topic, Message message) {
		this.messenger.showMessage(topic, message);
	}

}
