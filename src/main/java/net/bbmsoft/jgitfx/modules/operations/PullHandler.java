package net.bbmsoft.jgitfx.modules.operations;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.lib.ProgressMonitor;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.CredentialsProvider;

import net.bbmsoft.jgitfx.event.EventPublisher;
import net.bbmsoft.jgitfx.event.TaskTopic;
import net.bbmsoft.jgitfx.messaging.Message;
import net.bbmsoft.jgitfx.messaging.MessageType;
import net.bbmsoft.jgitfx.modules.InteractiveCredentialsProvider;
import net.bbmsoft.jgitfx.modules.RepositoryActionHandler;

public class PullHandler extends RepositoryActionHandler<PullResult> {

	public PullHandler(EventPublisher eventPublisher) {
		super(eventPublisher);
	}

	public void pull(Git git, String remote, CredentialsProvider credetialsProvider) {

		Repository repository = git.getRepository();

		Task<PullResult> pullTask = new PullTask(this, repository, remote);
		pullTask.setResultSupplier(() -> doPull(git, remote, credetialsProvider, pullTask));
		
		publish(TaskTopic.PullTask.STARTED, pullTask);
	}

	private PullResult doPull(Git git, String remote, CredentialsProvider credetialsProvider,
			ProgressMonitor progressMonitor) {
		try {
			return git.pull().setCredentialsProvider(credetialsProvider).setProgressMonitor(progressMonitor)
					.setRemote(remote).call();
		} catch (TransportException e) {
			if (credetialsProvider instanceof InteractiveCredentialsProvider
					&& ((InteractiveCredentialsProvider) credetialsProvider).retry()) {
				return doPull(git, remote, credetialsProvider, progressMonitor);
			} else {
				publishError(remote, e);
			}
		} catch (Throwable th) {
			publishError(remote, th);
		}
		return null;
	}

	private void publishError(String remote, Throwable th) {
		StringBuilder sb = new StringBuilder("An unexpected error occurred while trying to pull from remote repository ")
				.append(remote).append(":");
		publish(MessageType.ERROR, new Message(String.format("Pull from %s failed", remote), sb.toString(), th));
	}

	public static class PullTask extends Task<PullResult> {

		private String remote;

		public PullTask(PullHandler handler, Repository repository, String remote) {
			super(handler, repository, TaskTopic.PullResult.FINISHED);
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
