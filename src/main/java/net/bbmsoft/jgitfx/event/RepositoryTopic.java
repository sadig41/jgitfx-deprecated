package net.bbmsoft.jgitfx.event;

import net.bbmsoft.jgitfx.modules.RepositoryHandler;

public enum RepositoryTopic implements Topic<RepositoryHandler> {
	
	REPO_LOADED, REPO_OPENED, REPO_CLOSED, REPO_UPDATED, REPO_REMOVED;
}
