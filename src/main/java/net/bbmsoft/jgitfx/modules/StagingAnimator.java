package net.bbmsoft.jgitfx.modules;

import static net.bbmsoft.jgitfx.utils.RepoHelper.fromHandler;
import static net.bbmsoft.jgitfx.utils.StagingHelper.getTree;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.dircache.DirCacheIterator;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.EmptyTreeIterator;
import org.eclipse.jgit.treewalk.FileTreeIterator;
import org.eclipse.xtext.xbase.lib.Pair;

import javafx.scene.control.ListView;
import net.bbmsoft.bbm.utils.concurrent.ThreadUtils;
import net.bbmsoft.jgitfx.event.DetailedDiffTopic;
import net.bbmsoft.jgitfx.event.DiffTopic;
import net.bbmsoft.jgitfx.event.EventBroker;
import net.bbmsoft.jgitfx.event.EventPublisher;
import net.bbmsoft.jgitfx.event.RepositoryTopic;
import net.bbmsoft.jgitfx.utils.DiffDetails;
import net.bbmsoft.jgitfx.utils.StagingHelper;
import net.bbmsoft.jgitfx.wrappers.DiffListCellFactory;

public class StagingAnimator {

	private final ListView<DiffEntry> unstagedFilesTable;
	private final ListView<DiffEntry> stagedFilesTable;
	private final EventPublisher eventPublisher;

	private Repository repository;

	public StagingAnimator(ListView<DiffEntry> unstagedFilesTable, ListView<DiffEntry> stagedFilesTable,
			EventBroker broker) {

		this.unstagedFilesTable = unstagedFilesTable;
		this.stagedFilesTable = stagedFilesTable;
		this.eventPublisher = broker;
		
		this.unstagedFilesTable.setCellFactory(new DiffListCellFactory());
		this.stagedFilesTable.setCellFactory(new DiffListCellFactory());

		broker.subscribe(RepositoryTopic.REPO_OPENED, (topic, repo) -> {
			this.repository = fromHandler(repo);
			this.stagedFilesTable.getItems().clear();
			this.unstagedFilesTable.getItems().clear();
		});

		broker.subscribe(DiffTopic.STAGED_CHANGES_FOUND, (topic, diffs) -> {
			if (this.repository != null && isRelevant(diffs.getKey())) {
				updateStagedFiles(diffs);
			}
		});

		broker.subscribe(DiffTopic.UNSTAGED_CHANGES_FOUND, (topic, diffs) -> {
			if (this.repository != null && isRelevant(diffs.getKey())) {
				updateUnstagedFiles(diffs);
			}
		});

		this.unstagedFilesTable.getSelectionModel().selectedItemProperty()
				.addListener((o, ov, nv) -> publishSelectedEntry(nv, false));
		this.stagedFilesTable.getSelectionModel().selectedItemProperty()
				.addListener((o, ov, nv) -> publishSelectedEntry(nv, true));
	}

	private void publishSelectedEntry(DiffEntry diff, boolean staged) {

		if (diff == null) {
			this.eventPublisher.publish(DetailedDiffTopic.DIFF_ENTRY_SELECTED, null);
			return;
		}

		try {
			AbstractTreeIterator oldTree, newTree;

			if (staged) {
				ObjectId id = this.repository.resolve(Constants.HEAD);
				RevCommit headCommit = id != null ? this.repository.parseCommit(id) : null;
				oldTree = headCommit != null ? getTree(headCommit, this.repository) : new EmptyTreeIterator();
				newTree = new DirCacheIterator(this.repository.readDirCache());
			} else {
				oldTree = new DirCacheIterator(this.repository.readDirCache());
				newTree = new FileTreeIterator(this.repository);
			}

			DiffDetails dd = new DiffDetails(this.repository, StagingHelper.getFilePath(diff), newTree, oldTree);

			this.eventPublisher.publish(DetailedDiffTopic.DIFF_ENTRY_SELECTED, dd);
		} catch (Throwable th) {
			th.printStackTrace();
		}
	}

	private boolean isRelevant(Repository repo) {
		return repo == null
				|| repo.getDirectory().getAbsolutePath().equals(this.repository.getDirectory().getAbsolutePath());
	}

	private void updateUnstagedFiles(Pair<Repository, List<DiffEntry>> diffs) {

		if (this.repository == null) {
			this.unstagedFilesTable.getItems().clear();
			return;
		}

		List<DiffEntry> selectedUnstaged = new ArrayList<>(
				this.unstagedFilesTable.getSelectionModel().getSelectedItems());

		List<DiffEntry> diff = new ArrayList<>(diffs.getValue());
		diff.sort((a, b) -> a.getChangeType().compareTo(b.getChangeType()));

		this.unstagedFilesTable.getItems().setAll(diff);
		StagingHelper.applyToMatching(diff, selectedUnstaged,
				d -> this.unstagedFilesTable.getSelectionModel().select(d));
	}

	private void updateStagedFiles(Pair<Repository, List<DiffEntry>> diffs) {

		if (this.repository == null) {
			this.stagedFilesTable.getItems().clear();
			return;
		}

		List<DiffEntry> selectedStaged = new ArrayList<>(this.stagedFilesTable.getSelectionModel().getSelectedItems());

		List<DiffEntry> indexDiff = new ArrayList<>(diffs.getValue());
		indexDiff.sort((a, b) -> a.getChangeType().compareTo(b.getChangeType()));

		this.stagedFilesTable.getItems().setAll(indexDiff);
		StagingHelper.applyToMatching(indexDiff, selectedStaged,
				d -> this.stagedFilesTable.getSelectionModel().select(d));
	}

	public boolean hasStagedChanges() {
		ThreadUtils.checkFxThread();
		return !this.stagedFilesTable.getItems().isEmpty();
	}
}
