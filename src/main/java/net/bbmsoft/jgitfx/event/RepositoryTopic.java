package net.bbmsoft.jgitfx.event;

import org.eclipse.jgit.lib.Repository;

import net.bbmsoft.jgitfx.event.EventBroker.Topic;

public enum RepositoryTopic implements Topic<Repository> {
	
	REPO_LOADED, REPO_OPENED, REPO_CLOSED, REPO_UPDATED, REPO_REMOVED;
}
