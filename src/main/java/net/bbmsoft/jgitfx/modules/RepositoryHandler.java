package net.bbmsoft.jgitfx.modules;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jgit.lib.Repository;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;

public class RepositoryHandler implements Observable {

	private final Repository repository;
	private final List<InvalidationListener> listeners;

	public RepositoryHandler(Repository repository, InvalidationListener listener) {
		this.listeners = new ArrayList<>();
		this.listeners.add(listener);
		this.repository = repository;
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
		this.invalidate();
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
}
