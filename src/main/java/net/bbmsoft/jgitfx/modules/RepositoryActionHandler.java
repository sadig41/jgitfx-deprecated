package net.bbmsoft.jgitfx.modules;

import java.util.function.Function;

import net.bbmsoft.bbm.utils.Lockable;
import net.bbmsoft.jgitfx.messaging.MessageType;
import net.bbmsoft.jgitfx.messaging.Messenger;

public abstract class RepositoryActionHandler<R> implements Messenger {
	
	private final Runnable updateCallback;
	private final Messenger messenger;

	public RepositoryActionHandler(Runnable updateCallback, Messenger messenger) {
		this.updateCallback = updateCallback;
		this.messenger = messenger;
	}

	protected void logException(Exception e) {
		// TODO implement proper exception handling
		e.printStackTrace();
	}
	
	protected void done(R result, Lockable lock) {
		
		this.evaluateResult(result);
		
		if (lock != null) {
			lock.unlock();
		}
		if(this.updateCallback != null) {
			this.updateCallback.run();
		}
	}

	protected abstract void evaluateResult(R result);
	
	@Override
	public void showMessage(MessageType type, String title, String body) {
		this.messenger.showMessage(type, title, body);
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
