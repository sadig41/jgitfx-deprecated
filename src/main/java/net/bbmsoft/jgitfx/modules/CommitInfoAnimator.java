package net.bbmsoft.jgitfx.modules;

import java.util.Date;

import org.eclipse.jgit.revwalk.RevCommit;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Label;
import net.bbmsoft.jgitfx.wrappers.HistoryEntry;

public class CommitInfoAnimator implements ChangeListener<HistoryEntry> {

	private final Label commitMessageLabel;
	private final Label authorLabel;
	private final Label emailLabel;
	private final Label timeLabel;
	private final Label hashLabel;
	private final Label parentHashLabel;

	private HistoryEntry currentCommit;

	public CommitInfoAnimator(Label commitMessageLabel, Label authorLabel, Label emailLabel, Label timeLabel,
			Label hashLabel, Label parentHashLabel) {

		this.commitMessageLabel = commitMessageLabel;
		this.authorLabel = authorLabel;
		this.emailLabel = emailLabel;
		this.timeLabel = timeLabel;
		this.hashLabel = hashLabel;
		this.parentHashLabel = parentHashLabel;
	}

	@Override
	public void changed(ObservableValue<? extends HistoryEntry> observable, HistoryEntry oldValue, HistoryEntry newValue) {

		this.currentCommit = newValue;
		updatePanel();
	}

	private void updatePanel() {
		
		boolean noCommit = this.currentCommit == null;

		if (!noCommit && this.currentCommit.getCommit() != null) {
			RevCommit commit = this.currentCommit.getCommit();
			this.commitMessageLabel.setText(commit.getFullMessage().trim());
			this.authorLabel.setText(commit.getAuthorIdent().getName());
			this.emailLabel.setText(commit.getAuthorIdent().getEmailAddress());
			this.timeLabel.setText(new Date(commit.getCommitTime() * 1000L).toString());
			this.hashLabel.setText(commit.getId().name());
			this.parentHashLabel.setText(
					commit.getParentCount() > 0 ? commit.getParent(0).getId().name() : "-");
		} else {
			this.commitMessageLabel.setText(null);
			this.authorLabel.setText(null);
			this.emailLabel.setText(null);
			this.timeLabel.setText(null);
			this.hashLabel.setText(null);
			this.parentHashLabel.setText(null);
		}

		this.commitMessageLabel.setDisable(noCommit);
		this.authorLabel.setDisable(noCommit);
		this.emailLabel.setDisable(noCommit);
		this.timeLabel.setDisable(noCommit);
		this.hashLabel.setDisable(noCommit);
		this.parentHashLabel.setDisable(noCommit);

	}
}
