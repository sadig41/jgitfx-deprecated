package net.bbmsoft.jgitfx.modules;

import java.io.IOException;
import java.util.List;
import java.util.function.Supplier;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffEntry.ChangeType;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Parent;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import net.bbmsoft.jgitfx.wrappers.HistoryEntry;

public class ChangedFilesAnimator implements ChangeListener<HistoryEntry> {

	private final TableView<DiffEntry> changedFilesOverview;
	private final Parent wipOverview;

	private final Supplier<Repository> repoSupplier;
	private final TableColumn<DiffEntry, ChangeType> typeColumn;
	private final TableColumn<DiffEntry, String> fileColumn;

	public ChangedFilesAnimator(Parent wipOverview, TableView<DiffEntry> changedFilesOverview,
			TableColumn<DiffEntry, ChangeType> typeColumn, TableColumn<DiffEntry, String> fileColumn,
			Supplier<Repository> repoSupplier) {

		this.wipOverview = wipOverview;
		this.changedFilesOverview = changedFilesOverview;
		this.typeColumn = typeColumn;
		this.fileColumn = fileColumn;
		this.repoSupplier = repoSupplier;

		this.typeColumn
				.setCellValueFactory(cdf -> new SimpleObjectProperty<ChangeType>(cdf.getValue().getChangeType()));
		this.fileColumn.setCellValueFactory(cdf -> new SimpleStringProperty(getFilepath(cdf.getValue())));
	}

	private String getFilepath(DiffEntry diff) {
		ChangeType changeType = diff.getChangeType();
		switch (changeType) {
		case ADD:
		case MODIFY:
			return diff.getNewPath();
		case DELETE:
			return diff.getOldPath();
		case COPY:
		case RENAME:
			new StringBuilder(diff.getOldPath()).append(" -> ").append(diff.getNewPath()).toString();
		default:
			throw new IllegalArgumentException("Unknown change type: " + changeType);
		}
	}

	@Override
	public void changed(ObservableValue<? extends HistoryEntry> observable, HistoryEntry oldValue,
			HistoryEntry newValue) {
		updateTable(newValue);
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
			
			this.wipOverview.setVisible(false);
			this.changedFilesOverview.setVisible(true);
			
			if (commit.getParentCount() > 0) {
				
				try (Git git = Git.wrap(repo)) {
					
					List<DiffEntry> diff = git.diff().setOldTree(getTree(commit.getParent(0), repo))
							.setNewTree(getTree(commit, repo)).call();
					diff.sort((a, b) -> a.getChangeType().compareTo(b.getChangeType()));
					this.changedFilesOverview.getItems().setAll(diff);
					
				} catch (IncorrectObjectTypeException e) {
					e.printStackTrace();
				} catch (GitAPIException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else {
				// handle initial commit
			}
			
		} else {
			this.changedFilesOverview.setVisible(false);
			this.wipOverview.setVisible(true);
		}
	}

}
