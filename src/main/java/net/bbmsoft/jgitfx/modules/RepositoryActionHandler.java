package net.bbmsoft.jgitfx.modules;

import java.util.function.Supplier;

import org.eclipse.jgit.lib.ProgressMonitor;
import org.eclipse.jgit.lib.Repository;

import javafx.application.Platform;
import net.bbmsoft.jgitfx.event.EventBroker.Topic;
import net.bbmsoft.jgitfx.event.EventPublisher;
import net.bbmsoft.jgitfx.event.TaskTopic;

public abstract class RepositoryActionHandler<R> {
	
	private final EventPublisher eventPublisher;

	public RepositoryActionHandler(EventPublisher publisher) {
		this.eventPublisher = publisher;
	}

	protected void logException(Exception e) {
		// TODO implement proper exception handling
		e.printStackTrace();
	}
	
	protected <T> void publish(Topic<T> topic, T payload) {
		this.eventPublisher.publish(topic, payload);
	}
	
	public static abstract class Task<T> extends javafx.concurrent.Task<T> implements ProgressMonitor {

		private final Repository repository;
		private final RepositoryActionHandler<T> handler;
		
		private int toDo;
		private int done;
		
		private volatile Supplier<T> resultSupplier;

		public Task(RepositoryActionHandler<T> handler, Repository repository) {
			this.handler = handler;
			this.repository = repository;
		}

		@Override
		protected T call() {
			T result = null;
			Supplier<T> resultSupplier = this.resultSupplier;
			if(resultSupplier != null) {
				result = resultSupplier.get();
			}
			Platform.runLater(() -> this.handler.publish(TaskTopic.TASK_FINISHED, this));
			return result;
		}

		public Repository getRepository() {
			return repository;
		}

		@Override
		public void start(int totalTasks) {
			System.err.printf("Starting %d tasks\n", totalTasks);
		}

		@Override
		public void beginTask(String title, int totalWork) {
			this.done = 0;
			this.toDo = (1 + totalWork);
			updateProgress(done, toDo);
			updateMessage(title);
			System.err.printf("Starting task '%s' with %d subtasks\n", title, totalWork);
		}

		@Override
		public void update(int completed) {
			this.done = Math.min(this.toDo, this.done + completed);
			updateProgress(done, toDo);
			System.err.printf("%d/%d\n", done, toDo);
		}

		@Override
		public void endTask() {
			updateProgress(++this.done, this.toDo);
			System.err.printf("%d/%d\n", done, toDo);
		}

		public Supplier<T> getResultSupplier() {
			return resultSupplier;
		}

		public void setResultSupplier(Supplier<T> resultSupplier) {
			this.resultSupplier = resultSupplier;
		}

	}
}
