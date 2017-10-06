package net.bbmsoft.jgitfx.event;

import javafx.concurrent.Task;

public enum TaskTopic implements Topic<Task<?>> {
	
	TASK_STARTED, TASK_FINISHED;

}
