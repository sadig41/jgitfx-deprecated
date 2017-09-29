package net.bbmsoft.jgitfx.event;

import org.eclipse.jgit.dircache.DirCache;
import org.eclipse.jgit.revwalk.RevCommit;

import net.bbmsoft.jgitfx.event.EventBroker.Topic;
import net.bbmsoft.jgitfx.modules.RepositoryHandler.Task;

public class TaskTopic {

	public static enum PullTask implements Topic<Task<org.eclipse.jgit.api.PullResult>> {
		STARTED;
	}

	public static enum PullResult implements Topic<org.eclipse.jgit.api.PullResult> {
		FINISHED;
	}

	public static enum PushTask implements Topic<Task<Iterable<org.eclipse.jgit.transport.PushResult>>> {
		STARTED;
	}

	public static enum PushResult implements Topic<Iterable<org.eclipse.jgit.transport.PushResult>> {
		FINISHED;
	}

	public static enum FetchTask implements Topic<Task<org.eclipse.jgit.transport.FetchResult>> {
		STARTED;
	}

	public static enum FetchResult implements Topic<org.eclipse.jgit.transport.FetchResult> {
		FINISHED;
	}

	public static enum CommitTask implements Topic<Task<RevCommit>> {
		STARTED;
	}

	public static enum CommitResult implements Topic<RevCommit> {
		FINISHED;
	}
	
	public static enum StageTask implements Topic<Task<DirCache>> {
		STARTED;
	}

	public static enum StageResult implements Topic<DirCache> {
		FINISHED;
	}

	public static enum MergeTask implements Topic<Task<org.eclipse.jgit.api.MergeResult>> {
		STARTED;
	}

	public static enum MergeResult implements Topic<org.eclipse.jgit.api.MergeResult> {
		FINISHED;
	}

	public static enum RebaseTask implements Topic<Task<org.eclipse.jgit.api.RebaseResult>> {
		STARTED;
	}

	public static enum RebaseResult implements Topic<org.eclipse.jgit.api.RebaseResult> {
		FINISHED;
	}

}
