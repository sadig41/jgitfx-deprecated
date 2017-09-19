package net.bbmsoft.jgitfx.modules;

import java.util.function.Supplier;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.CredentialsProvider;

import net.bbmsoft.bbm.utils.concurrent.TaskHelper;
import net.bbmsoft.jgitfx.event.EventPublisher;
import net.bbmsoft.jgitfx.event.RepositoryTopic;

public class RepositoryHandler {

	private final Repository repository;
	private final Git git;

	private final PullHandler pullHandler;
	private final PushHandler pushHandler;

	private final TaskHelper taskHelper;
	private final CredentialsProvider credentialsProvider;

	private boolean autoInvalidate;
	
	private EventPublisher eventPublisher;

	public RepositoryHandler(Repository repository, TaskHelper taskHelper, EventPublisher eventPublisher) {
		this.taskHelper = taskHelper;
		this.eventPublisher = eventPublisher;
		this.repository = repository;
		this.git = Git.wrap(repository);
		this.pullHandler = new PullHandler(this::invalidate, eventPublisher);
		this.pushHandler = new PushHandler(this::invalidate, eventPublisher);
		// TODO provide proper credentials provider
		this.credentialsProvider = new DialogUsernamePasswordProvider();
		this.invalidate();
	}

	private void invalidate() {
		if (this.autoInvalidate) {
			eventPublisher.publish(RepositoryTopic.REPO_UPDATED, this.repository);
		}
	}

	public void undo() {
		System.out.println("Performing 'undo' on " + repository);
		this.invalidate();
	}

	public void redo() {
		System.out.println("Performing 'redo' on " + repository);
		this.invalidate();
	}

	public void pull() {
		this.pullHandler.pull(git, taskHelper, Constants.DEFAULT_REMOTE_NAME, this.credentialsProvider);
	}

	public void push() {
		this.pushHandler.push(git, taskHelper, Constants.DEFAULT_REMOTE_NAME);
	}

	public void branch() {
		System.out.println("Performing 'branch' on " + repository);
		this.invalidate();
	}

	public void stash() {
		System.out.println("Performing 'stach' on " + repository);
		this.invalidate();
	}

	public void pop() {
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
