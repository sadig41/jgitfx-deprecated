package net.bbmsoft.jgitfx.modules;

import static net.bbmsoft.jgitfx.utils.RepoHelper.fromHandler;
import static net.bbmsoft.jgitfx.utils.StagingHelper.getTree;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.dircache.DirCacheIterator;
import org.eclipse.jgit.errors.CorruptObjectException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.FileTreeIterator;
import org.eclipse.jgit.util.io.DisabledOutputStream;
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

		broker.subscribe(Arrays.asList(RepositoryTopic.REPO_UPDATED, RepositoryTopic.REPO_OPENED),
				(topic, repo) -> updateStagedFiles(fromHandler(repo)));
	}

	private void updateStagedFiles(Repository repo) {

		if (repo == null) {
			return;
		}

		try {

			updateUnstagedChanges(repo);
			updateStagedChanges(repo);

		} catch (Throwable th) {
			this.eventPublisher.publish(MessageType.ERROR, new Message("Diff failed",
					"Could not parse diff for repository " + repo.getDirectory().getAbsolutePath(), th));
		}

	}

	private void updateStagedChanges(Repository repo) throws CorruptObjectException, IOException {

		try (DiffFormatter diffFmt = new DiffFormatter(DisabledOutputStream.INSTANCE)) {
			
			diffFmt.setRepository(repo);

			ObjectId id = repo.resolve(Constants.HEAD);
			RevCommit headCommit = repo.parseCommit(id);
			AbstractTreeIterator oldTree = getTree(headCommit, repo);
			AbstractTreeIterator newTree = new DirCacheIterator(repo.readDirCache());

			List<DiffEntry> diff = diffFmt.scan(oldTree, newTree);

			this.eventPublisher.publish(DiffTopic.STAGED_CHANGES_FOUND, Pair.of(repo, diff));
		}

	}

	private void updateUnstagedChanges(Repository repo) throws CorruptObjectException, IOException {

		try (DiffFormatter diffFmt = new DiffFormatter(DisabledOutputStream.INSTANCE)) {
			
			diffFmt.setRepository(repo);

			AbstractTreeIterator oldTree = new DirCacheIterator(repo.readDirCache());
			AbstractTreeIterator newTree = new FileTreeIterator(repo);

			List<DiffEntry> diff = diffFmt.scan(oldTree, newTree);

			this.eventPublisher.publish(DiffTopic.UNSTAGED_CHANGES_FOUND, Pair.of(repo, diff));
		}
	}

}
