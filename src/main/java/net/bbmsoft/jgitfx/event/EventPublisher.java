package net.bbmsoft.jgitfx.event;

public interface EventPublisher {
	
	public <T> void publish(Topic<T> topic, T payload);

}
