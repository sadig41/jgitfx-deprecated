package net.bbmsoft.jgitfx.event;

import net.bbmsoft.jgitfx.event.EventBroker.Topic;
import net.bbmsoft.jgitfx.modules.RepositoryHandler;

public enum RepositoryOperations implements Topic<RepositoryHandler> {

	UNDO, REDO, COMMIT, PUSH, PULL, FETCH, BRANCH, STASH, POP, STAGE, UNSTAGE;
	
	private String message;

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}
