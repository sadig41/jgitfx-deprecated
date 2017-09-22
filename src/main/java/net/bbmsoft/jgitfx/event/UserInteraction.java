package net.bbmsoft.jgitfx.event;

import net.bbmsoft.jgitfx.event.EventBroker.Topic;

public enum UserInteraction implements Topic<Object> {

	CLONE, BATCH_CLONE, INIT_REPO, OPEN_REPO, QUIT, SHOW_ABOUT;
}
