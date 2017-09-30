package net.bbmsoft.jgitfx.modules.operations;

import java.util.function.Supplier;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.AbortedByHookException;
import org.eclipse.jgit.api.errors.ConcurrentRefUpdateException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.api.errors.NoMessageException;
import org.eclipse.jgit.api.errors.UnmergedPathsException;
import org.eclipse.jgit.api.errors.WrongRepositoryStateException;
import org.eclipse.jgit.revwalk.RevCommit;

import net.bbmsoft.jgitfx.event.EventPublisher;
import net.bbmsoft.jgitfx.event.TaskTopic;
import net.bbmsoft.jgitfx.modules.RepositoryActionHandler;

public class CommitHandler extends RepositoryActionHandler<RevCommit> {

	public CommitHandler(EventPublisher publisher) {
		super(publisher);
	}

	public void commit(Git git, String message) {

		if (message == null || message.trim().isEmpty()) {
			throw new IllegalArgumentException("Invalid commit message!");
		}

		Task<RevCommit> commitTask = new CommitTask(this, () -> doCommit(git, message), git, message);

		publish(TaskTopic.CommitTask.STARTED, commitTask);
	}

	private RevCommit doCommit(Git git, String completeMessage) {
		try {
			return git.commit().setMessage(completeMessage).call();
		} catch (NoHeadException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoMessageException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnmergedPathsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ConcurrentRefUpdateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (WrongRepositoryStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (AbortedByHookException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (GitAPIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	static class CommitTask extends Task<RevCommit> {

		public CommitTask(CommitHandler handler, Supplier<RevCommit> resultSupplier, Git git, String message) {
			super(handler, resultSupplier, git.getRepository(), TaskTopic.CommitResult.FINISHED);
			updateTitle("Commit \"" + message.split("\n")[0] + "\"");
			updateMessage("Pending...");
		}

		@Override
		protected RevCommit call() {
			updateMessage("Committing changes ...");
			RevCommit result = super.call();
			updateMessage("Done.");
			return result;
		}
	}
}
