package net.bbmsoft.jgitfx.modules;

import java.util.function.Supplier;

import org.eclipse.jgit.lib.ProgressMonitor;
import org.eclipse.jgit.lib.Repository;

import javafx.application.Platform;
import javafx.scene.control.ProgressIndicator;
import net.bbmsoft.jgitfx.event.EventPublisher;
import net.bbmsoft.jgitfx.event.TaskTopic;
import net.bbmsoft.jgitfx.event.Topic;

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
		}

		@Override
		public void beginTask(String title, int totalWork) {
			this.done = 0;
			this.toDo = totalWork;
			updateProgress(this.done);
			updateMessage(title);
		}

		@Override
		public void update(int completed) {
			this.done += completed;
			updateProgress(this.done);
		}

		@Override
		public void endTask() {
			updateProgress(this.toDo);
		}

		private void updateProgress(int done) {
			updateProgress(this.toDo == UNKNOWN ? ProgressIndicator.INDETERMINATE_PROGRESS : done, this.toDo);
		}

		public Supplier<T> getResultSupplier() {
			return resultSupplier;
		}

		public void setResultSupplier(Supplier<T> resultSupplier) {
			this.resultSupplier = resultSupplier;
		}

	}
}
