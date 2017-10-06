package net.bbmsoft.jgitfx.event;

public enum UserInteraction implements Topic<Object> {

	CLONE, BATCH_CLONE, INIT_REPO, ADD_REPO, QUIT, SHOW_ABOUT;
}
