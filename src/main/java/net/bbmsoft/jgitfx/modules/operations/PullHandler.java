package net.bbmsoft.jgitfx.modules.operations;

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
import org.eclipse.jgit.lib.ProgressMonitor;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.TextProgressMonitor;
import org.eclipse.jgit.transport.CredentialsProvider;

import net.bbmsoft.jgitfx.event.EventPublisher;
import net.bbmsoft.jgitfx.event.TaskTopic;
import net.bbmsoft.jgitfx.messaging.Message;
import net.bbmsoft.jgitfx.messaging.MessageType;
import net.bbmsoft.jgitfx.modules.InteractiveCredentialsProvider;
import net.bbmsoft.jgitfx.modules.RepositoryActionHandler;
import net.bbmsoft.jgitfx.utils.ErrorHelper;

public class PullHandler extends RepositoryActionHandler<PullResult> {

	public PullHandler(EventPublisher eventPublisher) {
		super(eventPublisher);
	}

	public void pull(Git git, String remote,
			CredentialsProvider credetialsProvider) {

		Repository repository = git.getRepository();

		ProgressMonitor progressMonitor = new TextProgressMonitor();

		Task<PullResult> pullTask = new PullTask(this, () -> doPull(git, remote, credetialsProvider, progressMonitor),
				repository, remote);
		
		publish(TaskTopic.PullTask.STARTED, pullTask);
	}

	private PullResult doPull(Git git, String remote, CredentialsProvider credetialsProvider,
			ProgressMonitor progressMonitor) {
		try {
			return git.pull().setCredentialsProvider(credetialsProvider).setProgressMonitor(progressMonitor)
					.setRemote(remote).call();
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
			
			if(credetialsProvider instanceof InteractiveCredentialsProvider && ((InteractiveCredentialsProvider)credetialsProvider).retry()) {
				return doPull(git, remote, credetialsProvider, progressMonitor);
			}
			
			MessageType type = MessageType.ERROR;
			String title = "Pulling form " + remote + " failed!";
			Throwable cause = ErrorHelper.getRoot((Throwable) e, th -> th.getCause());
			String body = cause.getLocalizedMessage();
			publish(type, new Message(title, body));
		} catch (GitAPIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public static class PullTask extends Task<PullResult> {

		private String remote;

		public PullTask(PullHandler handler, Supplier<PullResult> resultSupplier, Repository repository, String remote) {
			super(handler, resultSupplier, repository, TaskTopic.PullResult.FINISHED);
			this.remote = remote;
			updateTitle("Pull " + repository.getWorkTree().getName());
			updateMessage("Pending...");
		}

		@Override
		protected PullResult call() {
			updateMessage("Pulling from " + remote + "...");
			PullResult result = super.call();
			updateMessage("Done.");
			return result;
		}
	}
}
