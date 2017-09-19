package net.bbmsoft.jgitfx.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SimpleEventBroker implements EventBroker {

	private final Map<Topic<?>, List<Listener<?>>> listenerMap;

	public SimpleEventBroker() {
		this.listenerMap = new HashMap<>();
	}

	private List<Listener<?>> getListeners(Topic<?> topic) {

		List<Listener<?>> listeners = this.listenerMap.get(topic);
		if (listeners == null) {
			listeners = new ArrayList<Listener<?>>();
			this.listenerMap.put(topic, listeners);
		}

		return listeners;

	}

	@Override
	public <T> void subscribe(Topic<T> topic, Listener<T> listener) {
		getListeners(topic).add(listener);
	}

	@Override
	public <T> void unsubscribe(Topic<T> topic, Listener<T> listener) {
		getListeners(topic).remove(listener);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> void publish(Topic<T> topic, T payload) {
		for (Listener<?> l : getListeners(topic)) {
			// we know this cast is safe because the type check in subscription makes sure
			// that all listeners match the topic to which they are subscribed
			((Listener<T>) l).update(topic, payload);
		}

	}
}
