package net.bbmsoft.jgitfx.event;

import javafx.concurrent.Task;
import net.bbmsoft.jgitfx.event.EventBroker.Topic;

public enum TaskTopic implements Topic<Task<?>> {
	
	TASK_STARTED, TASK_FINISHED;

}
