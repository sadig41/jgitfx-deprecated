package net.bbmsoft.jgitfx.modules;

import java.util.Date;

import org.eclipse.jgit.revwalk.RevCommit;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Label;

public class CommitInfoAnimator implements ChangeListener<RevCommit> {

	private final Label commitMessageLabel;
	private final Label authorLabel;
	private final Label emailLabel;
	private final Label timeLabel;
	private final Label hashLabel;
	private final Label parentHashLabel;

	private RevCommit currentCommit;

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
	public void changed(ObservableValue<? extends RevCommit> observable, RevCommit oldValue, RevCommit newValue) {

		this.currentCommit = newValue;
		updatePanel();
	}

	private void updatePanel() {

		if (this.currentCommit != null) {
			this.commitMessageLabel.setText(this.currentCommit.getFullMessage().trim());
			this.authorLabel.setText(this.currentCommit.getAuthorIdent().getName());
			this.emailLabel.setText(this.currentCommit.getAuthorIdent().getEmailAddress());
			this.timeLabel.setText(new Date(this.currentCommit.getCommitTime() * 1000L).toString());
			this.hashLabel.setText(this.currentCommit.getId().name());
			this.parentHashLabel.setText(
					this.currentCommit.getParentCount() > 0 ? this.currentCommit.getParent(0).getId().name() : "-");
		} else {
			this.commitMessageLabel.setText(null);
			this.authorLabel.setText(null);
			this.emailLabel.setText(null);
			this.timeLabel.setText(null);
			this.hashLabel.setText(null);
			this.parentHashLabel.setText(null);
		}

		boolean noCommit = this.currentCommit == null;

		this.commitMessageLabel.setDisable(noCommit);
		this.authorLabel.setDisable(noCommit);
		this.emailLabel.setDisable(noCommit);
		this.timeLabel.setDisable(noCommit);
		this.hashLabel.setDisable(noCommit);
		this.parentHashLabel.setDisable(noCommit);

	}
}
