package net.bbmsoft.jgitfx.modules;

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

import net.bbmsoft.bbm.utils.Lockable;
import net.bbmsoft.bbm.utils.concurrent.TaskHelper;
import net.bbmsoft.jgitfx.modules.RepositoryHandler.Task;

public class PullHandler extends RepositoryActionHandler<PullResult> {

	public PullHandler(Runnable updateCallback) {
		super(updateCallback);
		// TODO Auto-generated constructor stub
	}

	public void pull(Git git, Lockable lock, TaskHelper taskHelper, String remote) {

		Repository repository = git.getRepository();

		System.out.println("Performing 'pull' on " + repository);

		if (lock != null) {
			lock.lock();
		}
		// TODO decide, which remote to pull
		String label = "Pulling " + repository.getWorkTree().getName() + "...";

		Task<PullResult> pullTask = new PullTask(() -> doPull(git, remote), repository, remote);

		taskHelper.submitTask(pullTask, label, r -> done(r, lock), e -> logException(e));
	}

	private PullResult doPull(Git git, String remote) {
		try {
			return git.pull().setRemote(remote).call();
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

	@Override
	protected void evaluateResult(PullResult result) {
		// TODO Auto-generated method stub
	}

	static class PullTask extends RepositoryHandler.Task<PullResult> {

		public PullTask(Supplier<PullResult> resultSupplier, Repository repository, String remote) {
			super(resultSupplier, repository);
			updateTitle("Pull from " + remote);
			updateMessage("Pending...");
		}
		
		@Override
		protected PullResult call() {
			updateMessage("Pulling from remote...");
			PullResult result = super.call();
			updateMessage("Done.");
			return result;
		}
	}
}
