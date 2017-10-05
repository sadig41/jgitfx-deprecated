package net.bbmsoft.jgitfx.modules;

import static net.bbmsoft.jgitfx.utils.RepoHelper.equal;
import static net.bbmsoft.jgitfx.utils.RepoHelper.fromHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import net.bbmsoft.jgitfx.event.CommitMessageTopic;
import net.bbmsoft.jgitfx.event.EventBroker;
import net.bbmsoft.jgitfx.event.RepositoryTopic;
import net.bbmsoft.jgitfx.wrappers.HistoryEntry;;
public class RepositoryTableVisualizer {

	private final TableView<HistoryEntry> table;
	private final Map<String, List<Ref>> refMap;

	private Repository repository;
	
	private final StringProperty wipCommitMessage;

	public RepositoryTableVisualizer(TableView<HistoryEntry> table, TableColumn<HistoryEntry, String> branchColumn,
			TableColumn<HistoryEntry, String> commitMessageColumn, TableColumn<HistoryEntry, String> authorColumn,
			TableColumn<HistoryEntry, String> timeColumn, EventBroker eventBroker) {

		this.table = table;
		this.refMap = new HashMap<>();
		this.wipCommitMessage = new SimpleStringProperty("// WIP");
		
		eventBroker.subscribe(CommitMessageTopic.COMMIT_MESSAGE_UPDATED, (topic, message) -> {
			this.wipCommitMessage.set(message != null && !message.trim().isEmpty() ? "// WIP: " + message : "// WIP");
		});

		branchColumn.setCellValueFactory(cdf -> {
			return new SimpleStringProperty(getRefs(cdf.getValue().getCommit()));
		});

		commitMessageColumn.setCellValueFactory(cdf -> {
			
			if(cdf.getValue().getCommit() == null) {
				return wipCommitMessage;
			} else {
				return new SimpleStringProperty(getShortMessage(cdf.getValue().getCommit()));	
			}
		});

		authorColumn.setCellValueFactory(cdf -> {
			return new SimpleStringProperty(getAuthorIdent(cdf.getValue().getCommit()));
		});

		timeColumn.setCellValueFactory(cdf -> {
			return new SimpleStringProperty(getCommitTime(cdf.getValue().getCommit()));
		});
		
		eventBroker.subscribe(RepositoryTopic.REPO_UPDATED, (topic, repo) -> {
			if (this.repository != null && equal(fromHandler(repo),  this.repository)) {
				updateRepositoryView();
			}
		});
	}

	private String getCommitTime(RevCommit commit) {

		if (commit == null) {
			return "";
		} else {
			return toString(new Date(commit.getCommitTime() * 1000L));
		}
	}

	private String getAuthorIdent(RevCommit commit) {

		if (commit == null) {
			return "";
		} else {
			return toString(commit.getAuthorIdent());
		}
	}

	private String getShortMessage(RevCommit commit) {

		if (commit == null) {
			return "";
		} else {
			return commit.getShortMessage();
		}
	}

	private String getRefs(RevCommit commit) {
		
		if(commit == null) {
			return "";
		}

		String id = commit.getId().getName();
		List<Ref> refs = this.refMap.get(id);
		if (refs != null) {
			StringBuilder sb = new StringBuilder();
			Iterator<Ref> it = refs.iterator();
			while (it.hasNext()) {
				sb.append(it.next().getName());
				if (it.hasNext()) {
					sb.append(", ");
				}
			}
			return sb.toString();
		}

		return "";
	}

	private String toString(Date date) {
		return date.toString();
	}

	private String toString(PersonIdent authorIdent) {
		return new StringBuilder(authorIdent.getName()).append(" <").append(authorIdent.getEmailAddress()).append(">")
				.toString();
	}

	public void setRepository(Repository repository) {

		this.repository = repository;

		updateRepositoryView();
	}

	private void updateRepositoryView() {
		try {
			this.doUpdateRepositoryView();
		} catch (NoHeadException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (GitAPIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void doUpdateRepositoryView() throws GitAPIException, IOException {

		if(this.repository == null) {
			this.table.getItems().clear();
			return;
		}
		
		HistoryEntry selected = this.table.getSelectionModel().getSelectedItem();

		Git git = Git.wrap(this.repository);

		this.refMap.clear();

		List<Ref> allRefs = new ArrayList<>();
		allRefs.addAll(git.branchList().call());
		allRefs.addAll(git.tagList().call());

		allRefs.forEach(ref -> {
			String id = ref.getObjectId().getName();
			List<Ref> refs = this.refMap.get(id);
			if (refs == null) {
				refs = new ArrayList<>();
				this.refMap.put(id, refs);
			}
			refs.add(ref);
		});

		List<HistoryEntry> commits = new ArrayList<>();
		commits.add(new HistoryEntry(null));
		
		Iterable<RevCommit> commitsIterable = Collections.emptyList();
		try {
			commitsIterable = git.log().all().call();
		} catch (NoHeadException e) {
			this.table.getItems().setAll(commits);
			return;
		}

		commitsIterable.forEach(rev -> commits.add(new HistoryEntry(rev)));
		this.table.getItems().setAll(commits);
		this.table.getSelectionModel().select(selected);
	}
}
