package net.bbmsoft.jgitfx.modules;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;

import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class RepositoryTableVisualizer {

	private TableView<RevCommit> table;
	private Repository repository;

	public RepositoryTableVisualizer(TableView<RevCommit> table, TableColumn<RevCommit, String> branchColumn,
			TableColumn<RevCommit, String> commitMessageColumn, TableColumn<RevCommit, String> authorColumn,
			TableColumn<RevCommit, String> timeColumn) {

		this.table = table;

		// branchColumn.setCellValueFactory(cdf -> {
		// return new SimpleStringProperty(cdf.getValue().getBranch());
		// });

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
		Iterable<RevCommit> commitsIterable = git.log().all().call();

		List<RevCommit> commits = new ArrayList<>();
		commitsIterable.forEach(rev -> commits.add(rev));
		this.table.getItems().setAll(commits);
	}
}
