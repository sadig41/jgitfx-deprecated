package net.bbmsoft.jgitfx.event;

import net.bbmsoft.jgitfx.event.EventBroker.Topic;
import net.bbmsoft.jgitfx.modules.RepositoryHandler;

public enum RepositoryOperations implements Topic<RepositoryHandler> {

	UNDO, REDO, PUSH, PULL, FETCH, BRANCH, STASH, POP;
}
