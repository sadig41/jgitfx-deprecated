package net.bbmsoft.jgitfx.modules;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.eclipse.jgit.lib.Repository;

import net.bbmsoft.jgitfx.modules.RepositoryActionHandler.Task;

public class GitWorkerExecutorManager extends AbstractExecutorService {

	private final ExecutorService fallbackExecutor = Executors
			.newSingleThreadExecutor(new GitThreadFactory("Fallback"));

	private final Map<Repository, ExecutorService> executors;

	public GitWorkerExecutorManager() {
		this.executors = new HashMap<>();
	}

	@Override
	public void shutdown() {
		fallbackExecutor.shutdown();
	}

	@Override
	public List<Runnable> shutdownNow() {
		return fallbackExecutor.shutdownNow();
	}

	@Override
	public boolean isShutdown() {
		return fallbackExecutor.isShutdown();
	}

	@Override
	public boolean isTerminated() {
		return fallbackExecutor.isTerminated();
	}

	@Override
	public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
		return fallbackExecutor.awaitTermination(timeout, unit);
	}

	@Override
	public Future<?> submit(Runnable task) {

		if (task instanceof Task<?>) {
			Repository repo = ((Task<?>) task).getRepository();
			ExecutorService exec = this.executors.get(repo);
			if (exec == null) {
				exec = Executors.newSingleThreadExecutor(new GitThreadFactory(repo.getWorkTree().getName()));
				this.executors.put(repo, exec);
			}
			return exec.submit(task);
		} else {
			return super.submit(task);
		}
	}

	@Override
	public void execute(Runnable command) {
		this.fallbackExecutor.execute(command);
	}

	private static class GitThreadFactory implements ThreadFactory {

		private final String repoName;

		public GitThreadFactory(String repoName) {
			this.repoName = repoName;
		}

		@Override
		public Thread newThread(Runnable r) {
			Thread t = new Thread(r, String.format("Git Worker Thread %s", this.repoName));
			t.setDaemon(true);
			return t;
		}
	}
}
