package net.bbmsoft.jgitfx.modules.operations;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.lib.ProgressMonitor;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.PushResult;

import net.bbmsoft.jgitfx.event.EventPublisher;
import net.bbmsoft.jgitfx.event.TaskTopic;
import net.bbmsoft.jgitfx.messaging.Message;
import net.bbmsoft.jgitfx.messaging.MessageType;
import net.bbmsoft.jgitfx.modules.InteractiveCredentialsProvider;
import net.bbmsoft.jgitfx.modules.RepositoryActionHandler;

public class PushHandler extends RepositoryActionHandler<Iterable<PushResult>> {

	public PushHandler(EventPublisher eventPublisher) {
		super(eventPublisher);
	}

	public void push(Git git, String remote, CredentialsProvider credetialsProvider) {

		Repository repository = git.getRepository();

		Task<Iterable<PushResult>> pushTask = new PushTask(this, repository, remote);
		pushTask.setResultSupplier(() -> doPush(git, remote, credetialsProvider, pushTask));

		publish(TaskTopic.TASK_STARTED, pushTask);
	}

	private Iterable<PushResult> doPush(Git git, String remote, CredentialsProvider credetialsProvider,
			ProgressMonitor progressMonitor) {

		try {
			return git.push().setCredentialsProvider(credetialsProvider).setProgressMonitor(progressMonitor)
					.setRemote(remote).call();
		} catch (TransportException e) {
			if (credetialsProvider instanceof InteractiveCredentialsProvider
					&& ((InteractiveCredentialsProvider) credetialsProvider).retry()) {
				return doPush(git, remote, credetialsProvider, progressMonitor);
			} else {
				publishError(remote, e);
			}
		} catch (Throwable th) {
			publishError(remote, th);
		}

		return null;
	}

	private void publishError(String remote, Throwable th) {
		StringBuilder sb = new StringBuilder("An unexpected error occurred while trying to push to remote repository ")
				.append(remote).append(":");
		publish(MessageType.ERROR, new Message(String.format("Push to %s failed", remote), sb.toString(), th));
	}

	static class PushTask extends Task<Iterable<PushResult>> {

		private String remote;

		public PushTask(PushHandler handler, Repository repository, String remote) {
			super(handler, repository);
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
