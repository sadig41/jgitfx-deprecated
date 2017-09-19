package net.bbmsoft.jgitfx.event;

public interface EventBroker extends EventPublisher {

	public interface Listener<T> {
		public void update(Topic<T> topic, T payload);
	}
	
	public interface Topic<T> {
	}

	public <T> void subscribe(Topic<T> topic, Listener<T> listener);
	
	public default <T> void subscribe(Topic<T>[] topics, Listener<T> listener) {
		for(Topic<T> topic : topics) {
			subscribe(topic, listener);
		}
	}

	public <T> void unsubscribe(Topic<T> topic, Listener<T> listener);
}
