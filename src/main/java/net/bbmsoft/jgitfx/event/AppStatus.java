package net.bbmsoft.jgitfx.event;

import net.bbmsoft.jgitfx.event.EventBroker.Topic;

public enum AppStatus implements Topic<Long> {

	STARTING, STARTED, EXITING, EXITED, FOCUSED;
}
