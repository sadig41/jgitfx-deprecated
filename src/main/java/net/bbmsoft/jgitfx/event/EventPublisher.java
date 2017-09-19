package net.bbmsoft.jgitfx.event;

import net.bbmsoft.jgitfx.event.EventBroker.Topic;

public interface EventPublisher {
	
	public <T> void publish(Topic<T> topic, T payload);

}
