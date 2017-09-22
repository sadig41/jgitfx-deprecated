package net.bbmsoft.jgitfx.modules;

import java.util.function.Supplier;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.CredentialsProvider;

import net.bbmsoft.jgitfx.event.EventBroker;
import net.bbmsoft.jgitfx.event.EventPublisher;
import net.bbmsoft.jgitfx.event.RepositoryOperations;
import net.bbmsoft.jgitfx.event.RepositoryTopic;

public class RepositoryHandler {

	private final Repository repository;
	private final Git git;

	private final PullHandler pullHandler;
	private final PushHandler pushHandler;

	private final CredentialsProvider credentialsProvider;

	private boolean autoInvalidate;
	
	private EventPublisher eventPublisher;

	public RepositoryHandler(Repository repository, EventBroker eventBroker) {
		this.eventPublisher = eventBroker;
		this.repository = repository;
		this.git = Git.wrap(repository);
		this.pullHandler = new PullHandler(this::invalidate, eventBroker);
		this.pushHandler = new PushHandler(this::invalidate, eventBroker);
		// TODO provide proper credentials provider
		this.credentialsProvider = new DialogUsernamePasswordProvider();
		eventBroker.subscribe(RepositoryOperations.values(), (topic, repo) -> {
			if(repo == this) {
			switch ((RepositoryOperations)topic) {
			case BRANCH:
				this.branch();
				break;
			case FETCH:
				this.fetch();
				break;
			case POP:
				this.pop();
				break;
			case PULL:
				this.pull();
				break;
			case PUSH:
				this.push();
				break;
			case REDO:
				this.redo();
				break;
			case STASH:
				this.stash();
				break;
			case UNDO:
				this.undo();
				break;
			case COMMIT:
				this.commit();
				break;
			default:
				throw new IllegalArgumentException("Unknown operation: " + topic);
			}
		}
		});
		this.invalidate();
	}

	private void invalidate() {
		if (this.autoInvalidate) {
			eventPublisher.publish(RepositoryTopic.REPO_UPDATED, this);
		}
	}
	
	private void commit() {
		System.out.println("Performing 'commit' on " + repository);
		this.invalidate();
	}

	private void undo() {
		System.out.println("Performing 'undo' on " + repository);
		this.invalidate();
	}
	
	private void fetch() {
		System.out.println("Performing 'undo' on " + repository);
		this.invalidate();
	}

	private void redo() {
		System.out.println("Performing 'redo' on " + repository);
		this.invalidate();
	}

	private void pull() {
		this.pullHandler.pull(git, Constants.DEFAULT_REMOTE_NAME, this.credentialsProvider);
	}

	private void push() {
		this.pushHandler.push(git, Constants.DEFAULT_REMOTE_NAME, this.credentialsProvider);
	}

	private void branch() {
		System.out.println("Performing 'branch' on " + repository);
		this.invalidate();
	}

	private void stash() {
		System.out.println("Performing 'stach' on " + repository);
		this.invalidate();
	}

	private void pop() {
		System.out.println("Performing 'pop' on " + repository);
		this.invalidate();
	}

	public Repository getRepository() {
		return repository;
	}

	public static class Task<T> extends javafx.concurrent.Task<T> {

		private final Supplier<T> resultSupplier;
		private final Repository repository;

		public Task(Supplier<T> resultSupplier, Repository repository) {
			this.resultSupplier = resultSupplier;
			this.repository = repository;
		}

		@Override
		protected T call() {
			return this.resultSupplier.get();
		}

		public Repository getRepository() {
			return repository;
		}

	}

	public void setAutoInvalidate(boolean value) {
		this.autoInvalidate = value;
		invalidate();
	}
}
