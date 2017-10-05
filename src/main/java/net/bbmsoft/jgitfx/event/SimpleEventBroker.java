package net.bbmsoft.jgitfx.event;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.bbmsoft.bbm.utils.concurrent.ThreadUtils;

public class SimpleEventBroker implements EventBroker {

	private final Map<Topic<?>, List<Listener<?>>> listenerMap;

	public SimpleEventBroker() {
		this.listenerMap = new ConcurrentHashMap<>();
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

	@Override
	@SuppressWarnings("unchecked")
	public <T> void publish(Topic<T> topic, T payload) {
		ThreadUtils.checkFxThread();
		List<Listener<?>> listeners = getListeners(topic);
		for (Listener<?> l : listeners) {
			((Listener<T>) l).update(topic, payload);
		}

	}
}
