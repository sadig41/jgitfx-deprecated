package net.bbmsoft.jgitfx.modules;

import java.util.function.Supplier;

import org.eclipse.jgit.lib.Repository;

import javafx.application.Platform;
import net.bbmsoft.jgitfx.event.EventBroker.Topic;
import net.bbmsoft.jgitfx.event.EventPublisher;

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
	
	public static class Task<T> extends javafx.concurrent.Task<T> {

		private final Supplier<T> resultSupplier;
		private final Repository repository;
		private final Topic<T> resultTopic;
		private RepositoryActionHandler<T> handler;

		public Task(RepositoryActionHandler<T> handler, Supplier<T> resultSupplier, Repository repository, Topic<T> resultTopic) {
			this.handler = handler;
			this.resultSupplier = resultSupplier;
			this.repository = repository;
			this.resultTopic = resultTopic;
		}

		@Override
		protected T call() {
			T result = this.resultSupplier.get();
			Platform.runLater(() -> this.handler.publish(this.resultTopic, result));
			return result;
		}

		public Repository getRepository() {
			return repository;
		}

	}
}
