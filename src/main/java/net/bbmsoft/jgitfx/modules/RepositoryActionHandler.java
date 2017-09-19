package net.bbmsoft.jgitfx.modules;

import net.bbmsoft.bbm.utils.Lockable;

public abstract class RepositoryActionHandler<R> {
	
	private final Runnable updateCallback;

	public RepositoryActionHandler(Runnable updateCallback) {
		this.updateCallback = updateCallback;
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
}
