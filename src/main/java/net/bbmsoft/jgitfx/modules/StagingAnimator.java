package net.bbmsoft.jgitfx.modules;

import java.io.IOException;
import java.util.List;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.dircache.DirCacheIterator;
import org.eclipse.jgit.errors.CorruptObjectException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.NoWorkTreeException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import net.bbmsoft.bbm.utils.concurrent.ThreadUtils;
import net.bbmsoft.jgitfx.event.EventBroker;
import net.bbmsoft.jgitfx.event.RepositoryTopic;
import net.bbmsoft.jgitfx.utils.StagingHelper;

public class StagingAnimator implements ChangeListener<RepositoryHandler> {

	private final TableView<DiffEntry> unstagedFilesTable;
	private final TableColumn<DiffEntry, String> unstagedTypeColum;
	private final TableColumn<DiffEntry, String> unstagedFileColum;
	private final TableView<DiffEntry> stagedFilesTable;
	private final TableColumn<DiffEntry, String> stagedTypeColum;
	private final TableColumn<DiffEntry, String> stagedFileColum;

	private Repository respository;

	public StagingAnimator(TableView<DiffEntry> unstagedFilesTable, TableColumn<DiffEntry, String> unstagedTypeColum,
			TableColumn<DiffEntry, String> unstagedFileColum, TableView<DiffEntry> stagedFilesTable,
			TableColumn<DiffEntry, String> stagedTypeColum, TableColumn<DiffEntry, String> stagedFileColum,
			EventBroker broker) {

		this.unstagedFilesTable = unstagedFilesTable;
		this.unstagedTypeColum = unstagedTypeColum;
		this.unstagedFileColum = unstagedFileColum;
		this.stagedFilesTable = stagedFilesTable;
		this.stagedTypeColum = stagedTypeColum;
		this.stagedFileColum = stagedFileColum;

		this.unstagedTypeColum
				.setCellValueFactory(cdf -> new SimpleStringProperty(cdf.getValue().getChangeType().toString()));
		this.unstagedFileColum
				.setCellValueFactory(cdf -> new SimpleStringProperty(StagingHelper.getFilePath(cdf.getValue())));
		this.stagedTypeColum
				.setCellValueFactory(cdf -> new SimpleStringProperty(cdf.getValue().getChangeType().toString()));
		this.stagedFileColum
				.setCellValueFactory(cdf -> new SimpleStringProperty(StagingHelper.getFilePath(cdf.getValue())));

		broker.subscribe(RepositoryTopic.REPO_UPDATED, (topic, repo) -> {
			if (this.respository != null && repo.getRepository().getDirectory().getAbsolutePath()
					.equals(this.respository.getDirectory().getAbsolutePath())) {
				updateStagedFiles();
			}
		});
	}

	private void setRepository(Repository respository) throws CorruptObjectException, IOException {
		this.respository = respository;
		updateStagedFiles();
		// make sure index gets updated when it changes
	}

	private void updateStagedFiles() {

		ThreadUtils.checkFxThread();
		
		this.unstagedFilesTable.getItems().clear();
		this.stagedFilesTable.getItems().clear();
		
		if (this.respository == null) {	
			return;
		}

		try (Git git = Git.wrap(this.respository)) {

			List<DiffEntry> diff = git.diff().call();
			diff.sort((a, b) -> a.getChangeType().compareTo(b.getChangeType()));

			this.unstagedFilesTable.getItems().setAll(diff);

			AbstractTreeIterator newTree = new DirCacheIterator(git.getRepository().readDirCache());

			ObjectId id = git.getRepository().resolve(Constants.HEAD);
			RevCommit headCommit = this.respository.parseCommit(id);
			AbstractTreeIterator oldTree = getTree(headCommit, this.respository);
			List<DiffEntry> indexDiff = git.diff().setNewTree(newTree).setOldTree(oldTree).call();

			this.stagedFilesTable.getItems().setAll(indexDiff);

		} catch (GitAPIException e) {
			e.printStackTrace();
		} catch (NoWorkTreeException e) {
			e.printStackTrace();
		} catch (CorruptObjectException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private AbstractTreeIterator getTree(RevCommit commit, Repository repo)
			throws IncorrectObjectTypeException, IOException {
		ObjectId treeId = commit.getTree().getId();
		try (ObjectReader reader = repo.newObjectReader()) {
			return new CanonicalTreeParser(null, reader, treeId);
		}
	}

	@Override
	public void changed(ObservableValue<? extends RepositoryHandler> observable, RepositoryHandler oldValue,
			RepositoryHandler newValue) {
		try {
			setRepository(newValue.getRepository());
		} catch (CorruptObjectException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public boolean hasStagedChanges() {
		ThreadUtils.checkFxThread();
		return !this.stagedFilesTable.getItems().isEmpty();
	}
}
