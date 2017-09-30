package net.bbmsoft.jgitfx.event;

import net.bbmsoft.jgitfx.event.EventBroker.Topic;
import net.bbmsoft.jgitfx.modules.RepositoryActionHandler.Task;

public enum TaskTopic implements Topic<Task<?>> {
	
	TASK_STARTED, TASK_FINISHED;

}
