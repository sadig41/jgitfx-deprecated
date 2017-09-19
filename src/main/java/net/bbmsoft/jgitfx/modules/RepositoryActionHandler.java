package net.bbmsoft.jgitfx.modules;

import java.util.function.Function;

import net.bbmsoft.jgitfx.event.EventBroker.Topic;
import net.bbmsoft.jgitfx.event.EventPublisher;

public abstract class RepositoryActionHandler<R> {
	
	private final Runnable updateCallback;
	private final EventPublisher eventPublisher;

	public RepositoryActionHandler(Runnable updateCallback, EventPublisher publisher) {
		this.updateCallback = updateCallback;
		this.eventPublisher = publisher;
	}

	protected void logException(Exception e) {
		// TODO implement proper exception handling
		e.printStackTrace();
	}
	
	protected void done(R result) {
		
		this.evaluateResult(result);
		
		if(this.updateCallback != null) {
			this.updateCallback.run();
		}
	}

	protected abstract void evaluateResult(R result);
	
	protected <T> void publish(Topic<T> topic, T payload) {
		this.eventPublisher.publish(topic, payload);
	}
	
	protected <T> T getRoot(T child, Function<T, T> parentProvider) {
		
		T parent = parentProvider.apply((T)child);
		if(parent == null) {
			return child;
		} else {
			return getRoot(parent, parentProvider);
		}
	}
}
