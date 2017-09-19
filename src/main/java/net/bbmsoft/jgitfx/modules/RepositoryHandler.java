package net.bbmsoft.jgitfx.modules;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import net.bbmsoft.bbm.utils.Lockable;
import net.bbmsoft.bbm.utils.concurrent.TaskHelper;
import net.bbmsoft.jgitfx.messaging.Messenger;

public class RepositoryHandler implements Observable {

	private final Repository repository;
	private final Git git;
	private final List<InvalidationListener> listeners;
	
	private final PullHandler pullHandler;
	private final PushHandler pushHandler;
	
	private final Lockable lockCallback;
	private final TaskHelper taskHelper;
	private final CredentialsProvider credentialsProvider;

	public RepositoryHandler(Repository repository, TaskHelper taskHelper, Messenger messenger) {
		this(repository, taskHelper, null, null, messenger);
	}

	public RepositoryHandler(Repository repository, TaskHelper taskHelper, Lockable lockCallback, InvalidationListener listener, Messenger messenger) {
		this.taskHelper = taskHelper;
		this.lockCallback = lockCallback;
		this.listeners = new ArrayList<>();
		if (listener != null) {
			this.listeners.add(listener);
		}
		this.repository = repository;
		this.git = Git.wrap(repository);
		this.pullHandler = new PullHandler(this::invalidate, messenger);
		this.pushHandler = new PushHandler(this::invalidate, messenger);
		// TODO provide proper credentials provider
		this.credentialsProvider = new UsernamePasswordCredentialsProvider("username", "password");
		this.invalidate();
	}

	private void invalidate() {
		this.listeners.forEach(l -> l.invalidated(this));
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
		this.pullHandler.pull(git, lockCallback, taskHelper, Constants.DEFAULT_REMOTE_NAME, this.credentialsProvider);
	}

	public void push() {
		this.pushHandler.push(git, lockCallback, taskHelper, Constants.DEFAULT_REMOTE_NAME);
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

	@Override
	public void addListener(InvalidationListener listener) {
		this.listeners.add(listener);
	}

	@Override
	public void removeListener(InvalidationListener listener) {
		this.listeners.remove(listener);
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
}
