package net.bbmsoft.jgitfx.event;

import java.io.File;

public enum RepositoryRegistryTopic implements Topic<File>{

	REPO_ADDED, REPO_REMOVED, REPO_NOT_FOUND;
}
