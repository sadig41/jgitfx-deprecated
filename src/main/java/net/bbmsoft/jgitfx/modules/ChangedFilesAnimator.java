package net.bbmsoft.jgitfx.modules;

import java.io.IOException;
import java.util.List;
import java.util.function.Supplier;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffEntry.ChangeType;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.EmptyTreeIterator;
import org.eclipse.xtext.xbase.lib.Pair;

import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Parent;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import net.bbmsoft.jgitfx.event.DiffTopic;
import net.bbmsoft.jgitfx.event.EventPublisher;
import net.bbmsoft.jgitfx.messaging.Message;
import net.bbmsoft.jgitfx.messaging.MessageType;
import net.bbmsoft.jgitfx.utils.StagingHelper;
import net.bbmsoft.jgitfx.wrappers.HistoryEntry;

public class ChangedFilesAnimator implements ChangeListener<HistoryEntry> {

	private final TableView<DiffEntry> changedFilesOverview;
	private final Parent wipOverview;

	private final Supplier<Repository> repoSupplier;
	private final TableColumn<DiffEntry, ChangeType> typeColumn;
	private final TableColumn<DiffEntry, String> fileColumn;
	private final EventPublisher eventPublisher;

	@SuppressWarnings("unchecked")
	public ChangedFilesAnimator(Parent wipOverview, TableView<DiffEntry> changedFilesOverview,
			TableColumn<DiffEntry, ChangeType> typeColumn, TableColumn<DiffEntry, String> fileColumn,
			Supplier<Repository> repoSupplier, EventPublisher eventPublisher) {

		this.wipOverview = wipOverview;
		this.changedFilesOverview = changedFilesOverview;
		this.typeColumn = typeColumn;
		this.fileColumn = fileColumn;
		this.repoSupplier = repoSupplier;
		this.eventPublisher = eventPublisher;

		this.typeColumn
				.setCellValueFactory(cdf -> new SimpleObjectProperty<ChangeType>(cdf.getValue().getChangeType()));
		this.fileColumn.setCellValueFactory(cdf -> new SimpleStringProperty(StagingHelper.getFilePath(cdf.getValue())));
		
		this.changedFilesOverview.getSelectionModel().getSelectedItems().addListener((Observable o) -> publishSelectedEntries((List<DiffEntry>) o));
	}

	private void publishSelectedEntries(List<DiffEntry> diff) {
		this.eventPublisher.publish(DiffTopic.DIFF_ENTRY_SELECTED, Pair.of(this.repoSupplier.get(), diff));
	}

	@Override
	public void changed(ObservableValue<? extends HistoryEntry> observable, HistoryEntry oldValue,
			HistoryEntry newValue) {
		Platform.runLater(() -> updateTable(newValue));
	}

	private AbstractTreeIterator getTree(RevCommit commit, Repository repo)
			throws IncorrectObjectTypeException, IOException {
		ObjectId treeId = commit.getTree().getId();
		try (ObjectReader reader = repo.newObjectReader()) {
			return new CanonicalTreeParser(null, reader, treeId);
		}
	}

	private void updateTable(HistoryEntry entry) {
		
		
		Repository repo = this.repoSupplier.get();
		if (repo == null) {
			return;
		}

		if (entry == null) {
			this.wipOverview.setVisible(false);
			this.changedFilesOverview.setVisible(false);
			return;
		}
		
		RevCommit commit = entry.getCommit();

		if (commit != null) {
			
			DiffEntry selected = this.changedFilesOverview.getSelectionModel().getSelectedItem();
			
			this.wipOverview.setVisible(false);
			this.changedFilesOverview.setVisible(true);
			
			RevCommit parent = commit.getParentCount() > 0 ? commit.getParent(0) : null;
			
			try (Git git = Git.wrap(repo)) {

				AbstractTreeIterator newTree = getTree(commit, repo);
				AbstractTreeIterator oldTree = parent != null ? getTree(parent, repo) : null;
				
				List<DiffEntry> diff = git.diff().setOldTree(oldTree != null ? oldTree : new EmptyTreeIterator()).setNewTree(newTree).call();
				
				diff.sort((a, b) -> a.getChangeType().compareTo(b.getChangeType()));
				this.changedFilesOverview.getItems().setAll(diff);
				StagingHelper.applyToMatching(diff, selected, d -> this.changedFilesOverview.getSelectionModel().select(d));
				
			} catch (Throwable th) {
//				this.eventPublisher.publish(MessageType.ERROR, new Message("Diff failed",
//						"Could not parse diff for repository " + repo.getDirectory().getAbsolutePath(), th));
			}
			
		} else {
			this.changedFilesOverview.setVisible(false);
			this.wipOverview.setVisible(true);
		}
		
	}

}
