package net.bbmsoft.jgitfx.modules;

import static net.bbmsoft.jgitfx.utils.RepoHelper.fromHandler;
import static net.bbmsoft.jgitfx.utils.StagingHelper.getTree;

import java.util.Arrays;
import java.util.List;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.dircache.DirCacheIterator;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.xtext.xbase.lib.Pair;

import net.bbmsoft.jgitfx.event.DiffTopic;
import net.bbmsoft.jgitfx.event.EventBroker;
import net.bbmsoft.jgitfx.event.EventPublisher;
import net.bbmsoft.jgitfx.event.RepositoryTopic;
import net.bbmsoft.jgitfx.messaging.Message;
import net.bbmsoft.jgitfx.messaging.MessageType;

public class RepositoryStatusMonitor {

	private final EventPublisher eventPublisher;

	public RepositoryStatusMonitor(EventBroker broker) {
		this.eventPublisher = broker;

		broker.subscribe(Arrays.asList(RepositoryTopic.REPO_UPDATED, RepositoryTopic.REPO_OPENED), (topic, repo) -> updateStagedFiles(fromHandler(repo)));
	}

	private void updateStagedFiles(Repository repo) {
		
		if(repo == null) {
			return;
		}

		try (Git git = Git.wrap(repo)) {

			List<DiffEntry> diff = git.diff().call();

			AbstractTreeIterator newTree = new DirCacheIterator(repo.readDirCache());
			ObjectId id = repo.resolve(Constants.HEAD);
			RevCommit headCommit = repo.parseCommit(id);
			AbstractTreeIterator oldTree = getTree(headCommit, repo);
			List<DiffEntry> indexDiff = git.diff().setNewTree(newTree).setOldTree(oldTree).call();

			this.eventPublisher.publish(DiffTopic.UNSTAGED_CHANGES_FOUND, Pair.of(repo, diff));
			this.eventPublisher.publish(DiffTopic.STAGED_CHANGES_FOUND, Pair.of(repo, indexDiff));
			
		} catch (Throwable th) {
			this.eventPublisher.publish(MessageType.ERROR, new Message("Diff failed",
					"Could not parse diff for repository " + repo.getDirectory().getAbsolutePath(), th));
		}

	}

}
