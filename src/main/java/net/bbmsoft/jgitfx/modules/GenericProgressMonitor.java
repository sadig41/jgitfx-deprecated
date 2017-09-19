package net.bbmsoft.jgitfx.modules;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.eclipse.jgit.lib.ProgressMonitor;

public class GenericProgressMonitor implements ProgressMonitor {
	
	private BiConsumer<Integer, Integer> progressConsumer;
	private Consumer<String> messageConsumer;
	private Supplier<Boolean> canceledProvider;
	
	private AtomicInteger totalTasks;
	private AtomicInteger done;
	
	public GenericProgressMonitor() {
		this.totalTasks = new AtomicInteger();
		this.done = new AtomicInteger();
	}

	@Override
	public void start(int totalTasks) {
		this.totalTasks.set(totalTasks);
		if(this.progressConsumer != null) {
			this.progressConsumer.accept(0, totalTasks);
		}
	}

	@Override
	public void beginTask(String title, int totalWork) {
		this.totalTasks.set(totalWork);
		if(this.progressConsumer != null) {
			this.progressConsumer.accept(done.get(), totalWork);
		}
		if(this.messageConsumer != null) {
			this.messageConsumer.accept(title);
		}
	}

	@Override
	public void update(int completed) {
		int done = this.done.addAndGet(completed);
		if(this.progressConsumer != null) {
			this.progressConsumer.accept(done, this.totalTasks.get());
		}
	}

	@Override
	public void endTask() {
		if(this.progressConsumer != null) {
			int tasks = this.totalTasks.get();
			this.progressConsumer.accept(tasks, tasks);
		}
	}

	@Override
	public boolean isCancelled() {
		if(this.canceledProvider != null) {
			return canceledProvider.get();
		} else {
			return false;
		}
	}

	public BiConsumer<Integer, Integer> getProgressConsumer() {
		return progressConsumer;
	}

	public void setProgressConsumer(BiConsumer<Integer, Integer> progressConsumer) {
		this.progressConsumer = progressConsumer;
	}

	public Consumer<String> getMessageConsumer() {
		return messageConsumer;
	}

	public void setMessageConsumer(Consumer<String> messageConsumer) {
		this.messageConsumer = messageConsumer;
	}

}
