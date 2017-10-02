package net.bbmsoft.jgitfx.modules;

import java.util.concurrent.atomic.AtomicInteger;
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
		private final AtomicInteger toDo;
		private final AtomicInteger done;
		
		private volatile Supplier<T> resultSupplier;

		public Task(RepositoryActionHandler<T> handler, Repository repository) {
			this.handler = handler;
			this.repository = repository;
			this.toDo = new AtomicInteger();
			this.done = new AtomicInteger();
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
			this.toDo.addAndGet(totalWork);
			int done = this.done.get();
			updateProgress(done, totalWork);
			updateMessage(title);
			System.err.printf("Starting task '%s' with %d subtasks\n", title, totalWork);
		}

		@Override
		public void update(int completed) {
			int done = this.done.addAndGet(completed);
			int toDo = this.toDo.get();
			updateProgress(done, toDo);
			System.err.printf("%d/%d\n", completed, toDo);
		}

		@Override
		public void endTask() {
			int done = this.done.get();
			int all = this.toDo.get();
			updateProgress(all, all);
			System.err.printf("%d/%d\n", done, all);
		}

		public Supplier<T> getResultSupplier() {
			return resultSupplier;
		}

		public void setResultSupplier(Supplier<T> resultSupplier) {
			this.resultSupplier = resultSupplier;
		}

	}
}
