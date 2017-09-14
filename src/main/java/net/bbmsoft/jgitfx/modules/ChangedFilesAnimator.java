package net.bbmsoft.jgitfx.modules;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.function.Supplier;

import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.errors.CorruptObjectException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.treewalk.TreeWalk;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TableView;

public class ChangedFilesAnimator implements ChangeListener<RevCommit> {

	private final TableView<File> changedFilesOverview;
	private final Supplier<Repository> repoSupplier;

	public ChangedFilesAnimator(TableView<File> changedFilesOverview, Supplier<Repository> repoSupplier) {
		this.changedFilesOverview = changedFilesOverview;
		this.repoSupplier = repoSupplier;
	}

	@Override
	public void changed(ObservableValue<? extends RevCommit> observable, RevCommit oldValue, RevCommit newValue) {
		if (newValue != null) {
			updateTable(newValue);
		} else {
			this.changedFilesOverview.getItems().clear();
		}
		this.changedFilesOverview.setVisible(newValue != null);
	}

	private void updateTable(RevCommit commit) {

		Repository repo = this.repoSupplier.get();
		if (repo == null) {
			return;
		}

		RevTree tree = commit.getTree();
		try (TreeWalk treeWalk = new TreeWalk(repo)) {
			treeWalk.addTree(tree);
			treeWalk.setRecursive(true);

			while (treeWalk.next()) {
				String pathString = treeWalk.getPathString();
				File file = new File(pathString);
				this.changedFilesOverview.getItems().add(file);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
