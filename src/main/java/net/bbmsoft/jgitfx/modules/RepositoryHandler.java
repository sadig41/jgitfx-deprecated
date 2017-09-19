package net.bbmsoft.jgitfx.modules;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Repository;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import net.bbmsoft.bbm.utils.Lockable;
import net.bbmsoft.bbm.utils.concurrent.TaskHelper;

public class RepositoryHandler implements Observable {

	private final Repository repository;
	private final Git git;
	private final List<InvalidationListener> listeners;
	
	private final PullHandler pullHandler;
	
	private final Lockable lockCallback;
	private final TaskHelper taskHelper;

	public RepositoryHandler(Repository repository, TaskHelper taskHelper) {
		this(repository, taskHelper, null, null);
	}

	public RepositoryHandler(Repository repository, TaskHelper taskHelper, Lockable lockCallback, InvalidationListener listener) {
		this.taskHelper = taskHelper;
		this.lockCallback = lockCallback;
		this.listeners = new ArrayList<>();
		if (listener != null) {
			this.listeners.add(listener);
		}
		this.repository = repository;
		this.git = Git.wrap(repository);
		this.pullHandler = new PullHandler(this::invalidate);
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
		this.pullHandler.pull(git, lockCallback, taskHelper, Constants.DEFAULT_REMOTE_NAME);
	}

	public void push() {
		System.out.println("Performing 'push' on " + repository);
		this.invalidate();
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
