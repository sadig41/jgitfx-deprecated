package net.bbmsoft.jgitfx.modules;

import java.io.IOException;
import java.util.ArrayList;
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
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class RepositoryTableVisualizer {

	private final TableView<RevCommit> table;
	private final Map<String, List<Ref>> refMap;

	private Repository repository;

	public RepositoryTableVisualizer(TableView<RevCommit> table, TableColumn<RevCommit, String> branchColumn,
			TableColumn<RevCommit, String> commitMessageColumn, TableColumn<RevCommit, String> authorColumn,
			TableColumn<RevCommit, String> timeColumn) {

		this.table = table;
		this.refMap = new HashMap<>();

		branchColumn.setCellValueFactory(cdf -> {
			return new SimpleStringProperty(getRefs(cdf.getValue()));
		});

		commitMessageColumn.setCellValueFactory(cdf -> {
			return new SimpleStringProperty(cdf.getValue().getShortMessage());
		});

		authorColumn.setCellValueFactory(cdf -> {
			return new SimpleStringProperty(toString(cdf.getValue().getAuthorIdent()));
		});

		timeColumn.setCellValueFactory(cdf -> {
			return new SimpleStringProperty(toString(new Date(cdf.getValue().getCommitTime() * 1000L)));
		});
	}

	private String getRefs(RevCommit commit) {

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

		try {
			this.updateRepositoryView();
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

	private void updateRepositoryView() throws NoHeadException, GitAPIException, IOException {

		// TODO possibly move to background thread if too slow

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

		Iterable<RevCommit> commitsIterable = git.log().all().call();

		List<RevCommit> commits = new ArrayList<>();
		commitsIterable.forEach(rev -> commits.add(rev));
		this.table.getItems().setAll(commits);
	}
}
