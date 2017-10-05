package net.bbmsoft.jgitfx.modules;

import static net.bbmsoft.jgitfx.utils.RepoHelper.fromHandler;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.xtext.xbase.lib.Pair;

import javafx.beans.Observable;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import net.bbmsoft.bbm.utils.concurrent.ThreadUtils;
import net.bbmsoft.jgitfx.event.DiffTopic;
import net.bbmsoft.jgitfx.event.EventBroker;
import net.bbmsoft.jgitfx.event.EventPublisher;
import net.bbmsoft.jgitfx.event.RepositoryTopic;
import net.bbmsoft.jgitfx.utils.StagingHelper;

public class StagingAnimator {

	private final TableView<DiffEntry> unstagedFilesTable;
	private final TableColumn<DiffEntry, String> unstagedTypeColum;
	private final TableColumn<DiffEntry, String> unstagedFileColum;
	private final TableView<DiffEntry> stagedFilesTable;
	private final TableColumn<DiffEntry, String> stagedTypeColum;
	private final TableColumn<DiffEntry, String> stagedFileColum;

	private Repository repository;
	private EventPublisher eventPublisher;

	@SuppressWarnings("unchecked")
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
		this.eventPublisher = broker;

		this.unstagedTypeColum
				.setCellValueFactory(cdf -> new SimpleStringProperty(cdf.getValue().getChangeType().toString()));
		this.unstagedFileColum
				.setCellValueFactory(cdf -> new SimpleStringProperty(StagingHelper.getFilePath(cdf.getValue())));
		this.stagedTypeColum
				.setCellValueFactory(cdf -> new SimpleStringProperty(cdf.getValue().getChangeType().toString()));
		this.stagedFileColum
				.setCellValueFactory(cdf -> new SimpleStringProperty(StagingHelper.getFilePath(cdf.getValue())));

		broker.subscribe(RepositoryTopic.REPO_OPENED, (topic, repo) -> this.repository = fromHandler(repo));

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

		this.unstagedFilesTable.getSelectionModel().getSelectedItems()
				.addListener((Observable o) -> publishSelectedEntries((List<DiffEntry>) o));
		this.stagedFilesTable.getSelectionModel().getSelectedItems()
				.addListener((Observable o) -> publishSelectedEntries((List<DiffEntry>) o));
	}

	private void publishSelectedEntries(List<DiffEntry> diffs) {
		this.eventPublisher.publish(DiffTopic.DIFF_ENTRY_SELECTED,
				Pair.of(this.repository, diffs));
	}

	private boolean isRelevant(Repository repo) {
		return repo == null
				|| repo.getDirectory().getAbsolutePath().equals(this.repository.getDirectory().getAbsolutePath());
	}

	private void updateUnstagedFiles(Pair<Repository, List<DiffEntry>> diffs) {

		if (this.repository == null) {
			this.unstagedFilesTable.getItems().clear();
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

	private void updateStagedFiles(Pair<Repository, List<DiffEntry>> diffs) {

		if (this.repository == null) {
			this.unstagedFilesTable.getItems().clear();
			this.stagedFilesTable.getItems().clear();
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

	public boolean hasStagedChanges() {
		ThreadUtils.checkFxThread();
		return !this.stagedFilesTable.getItems().isEmpty();
	}
}
