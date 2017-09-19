package net.bbmsoft.jgitfx.modules;

import java.util.function.Supplier;

import org.eclipse.jgit.api.Git;
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
import org.eclipse.jgit.transport.PushResult;

import net.bbmsoft.bbm.utils.concurrent.TaskHelper;
import net.bbmsoft.jgitfx.event.EventPublisher;
import net.bbmsoft.jgitfx.messaging.Message;
import net.bbmsoft.jgitfx.messaging.MessageType;
import net.bbmsoft.jgitfx.modules.RepositoryHandler.Task;

public class PushHandler extends RepositoryActionHandler<Iterable<PushResult>> {

	public PushHandler(Runnable updateCallback, EventPublisher eventPublisher) {
		super(updateCallback, eventPublisher);
	}

	public void push(Git git, TaskHelper taskHelper, String remote) {

		Repository repository = git.getRepository();

		String label = "Pushing " + repository.getWorkTree().getName() + "...";

		Task<Iterable<PushResult>> pushTask = new PushTask(() -> doPush(git, remote), repository, remote);

		taskHelper.submitTask(pushTask, label, r -> done(r), e -> logException(e));
	}

	private Iterable<PushResult> doPush(Git git, String remote) {
		try {
			return git.push().setRemote(remote).call();
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
			MessageType type = MessageType.ERROR;
			String title = "Pushing to " + remote + " failed!";
			Throwable cause = getRoot((Throwable) e, th -> th.getCause());
			String body = cause.getLocalizedMessage();
			publish(type, new Message(title, body));
		} catch (GitAPIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	@Override
	protected void evaluateResult(Iterable<PushResult> result) {
		// TODO Auto-generated method stub
	}

	static class PushTask extends RepositoryHandler.Task<Iterable<PushResult>> {

		private String remote;

		public PushTask(Supplier<Iterable<PushResult>> resultSupplier, Repository repository, String remote) {
			super(resultSupplier, repository);
			this.remote = remote;
			updateTitle("Push " + repository.getWorkTree().getName());
			updateMessage("Pending...");
		}

		@Override
		protected Iterable<PushResult> call() {
			updateMessage("Pushing to " + remote + "...");
			Iterable<PushResult> result = super.call();
			updateMessage("Done.");
			return result;
		}
	}
}
