package net.bbmsoft.jgitfx.modules;

import static net.bbmsoft.jgitfx.utils.RepoHelper.fromHandler;
import static net.bbmsoft.jgitfx.utils.StagingHelper.getTree;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.dircache.DirCacheIterator;
import org.eclipse.jgit.errors.CorruptObjectException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.EmptyTreeIterator;
import org.eclipse.jgit.treewalk.FileTreeIterator;
import org.eclipse.jgit.util.io.DisabledOutputStream;
import org.eclipse.xtext.xbase.lib.Pair;

import net.bbmsoft.jgitfx.event.DiffTopic;
import net.bbmsoft.jgitfx.event.EventBroker;
import net.bbmsoft.jgitfx.event.EventPublisher;
import net.bbmsoft.jgitfx.event.RepositoryTopic;
import net.bbmsoft.jgitfx.messaging.Message;
import net.bbmsoft.jgitfx.messaging.MessageType;
import net.bbmsoft.jgitfx.wrappers.RepositoryWrapper;

public class RepositoryStatusMonitor {

	private final EventPublisher eventPublisher;
	private final Function<Repository, RepositoryWrapper> wrapperSupplier;

	public RepositoryStatusMonitor(EventBroker broker, Function<Repository, RepositoryWrapper> wrapperSupplier) {
		this.eventPublisher = broker;
		this.wrapperSupplier = wrapperSupplier;

		broker.subscribe(Arrays.asList(RepositoryTopic.REPO_UPDATED, RepositoryTopic.REPO_OPENED),
				(topic, repo) -> updateRepo(fromHandler(repo)));
	}

	private void updateRepo(Repository repo) {

		if (repo == null) {
			return;
		}

		try {

			int unstagedChanges = updateUnstagedChanges(repo);
			int stagedChanges = updateStagedChanges(repo);
			
			RepositoryWrapper wrapper = this.wrapperSupplier.apply(repo);
			
			if(wrapper != null) {
				wrapper.setStagedChanges(stagedChanges > 0);
				wrapper.setUnstagedChanges(unstagedChanges > 0);
			}

		} catch (Throwable th) {
			this.eventPublisher.publish(MessageType.ERROR, new Message("Diff failed",
					"Could not parse diff for repository " + repo.getDirectory().getAbsolutePath(), th));
		}

	}

	private int updateStagedChanges(Repository repo) throws CorruptObjectException, IOException {

		try (DiffFormatter diffFmt = new DiffFormatter(DisabledOutputStream.INSTANCE)) {
			
			diffFmt.setRepository(repo);

			ObjectId id = repo.resolve(Constants.HEAD);
			RevCommit headCommit = id != null ? repo.parseCommit(id) : null;
			AbstractTreeIterator oldTree = headCommit != null ? getTree(headCommit, repo) : new EmptyTreeIterator();
			AbstractTreeIterator newTree = new DirCacheIterator(repo.readDirCache());

			List<DiffEntry> diff = diffFmt.scan(oldTree, newTree);

			this.eventPublisher.publish(DiffTopic.STAGED_CHANGES_FOUND, Pair.of(repo, diff));
			
			return diff.size();
		}

	}

	private int updateUnstagedChanges(Repository repo) throws CorruptObjectException, IOException {

		try (DiffFormatter diffFmt = new DiffFormatter(DisabledOutputStream.INSTANCE)) {
			
			diffFmt.setRepository(repo);

			AbstractTreeIterator oldTree = new DirCacheIterator(repo.readDirCache());
			AbstractTreeIterator newTree = new FileTreeIterator(repo);

			List<DiffEntry> diff = diffFmt.scan(oldTree, newTree);

			this.eventPublisher.publish(DiffTopic.UNSTAGED_CHANGES_FOUND, Pair.of(repo, diff));
			
			return diff.size();
		}
	}

}
