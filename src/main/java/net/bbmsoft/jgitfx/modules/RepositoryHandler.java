package net.bbmsoft.jgitfx.modules;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.errors.CanceledException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidConfigurationException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.api.errors.RefNotAdvertisedException;
import org.eclipse.jgit.api.errors.RefNotFoundException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.api.errors.WrongRepositoryStateException;
import org.eclipse.jgit.lib.Repository;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import net.bbmsoft.bbm.utils.Lockable;
import net.bbmsoft.bbm.utils.concurrent.TaskHelper;

public class RepositoryHandler implements Observable {

	private final Repository repository;
	private final Git git;
	private final List<InvalidationListener> listeners;
	
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
		
		System.out.println("Performing 'pull' on " + repository);
		
		if(this.lockCallback != null) {
			this.lockCallback.lock();
		}
		// TODO decide, which remote to pull
		String label = "Pulling " + this.repository.getWorkTree().getName() + "...";
		
		Task<PullResult> pullTask = new Task<>(() -> doPull(), this.repository);
		
		this.taskHelper.submitTask(pullTask, label, r -> pullDone(r), e -> logException(e));
	}

	private void logException(Exception e) {
		// TODO implement proper exception handling
		e.printStackTrace();
	}

	private void pullDone(PullResult result) {
		
		this.evaluatePullResult(result);
		
		if (this.lockCallback != null) {
			this.lockCallback.unlock();
		}
		this.invalidate();
	}

	private void evaluatePullResult(PullResult result) {
		// TODO Auto-generated method stub	
	}

	private PullResult doPull() {
		try {
			return this.git.pull().call();
		} catch (WrongRepositoryStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidRemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CanceledException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RefNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RefNotAdvertisedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoHeadException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransportException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (GitAPIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
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
