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
		
		private volatile Supplier<T> resultSupplier;

		public Task(RepositoryActionHandler<T> handler, Repository repository) {
			this.handler = handler;
			this.repository = repository;
			this.toDo = new AtomicInteger();
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
		}

		@Override
		public void beginTask(String title, int totalWork) {
			this.toDo.set(totalWork);
			updateProgress(0, totalWork);
			updateMessage(title);
		}

		@Override
		public void update(int completed) {
			updateProgress(completed, this.toDo.get());
		}

		@Override
		public void endTask() {
			int all = this.toDo.get();
			updateProgress(all, all);
		}

		public Supplier<T> getResultSupplier() {
			return resultSupplier;
		}

		public void setResultSupplier(Supplier<T> resultSupplier) {
			this.resultSupplier = resultSupplier;
		}

	}
}
