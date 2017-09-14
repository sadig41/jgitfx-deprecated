package net.bbmsoft.jgitfx.modules;

import java.util.Date;

import org.eclipse.jgit.revwalk.RevCommit;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;

public class CommitInfoAnimator implements ChangeListener<RevCommit> {

	private final Label commitMessageLabel;
	private final Label authorLabel;
	private final Label emailLabel;
	private final Label timeLabel;
	private final Label hashLabel;
	private final Label parentHashLabel;
	private final ToggleButton expandButton;

	private RevCommit currentCommit;

	public CommitInfoAnimator(Label commitMessageLabel, Label authorLabel, Label emailLabel, Label timeLabel,
			Label hashLabel, Label parentHashLabel, ToggleButton expandButton) {

		this.commitMessageLabel = commitMessageLabel;
		this.authorLabel = authorLabel;
		this.emailLabel = emailLabel;
		this.timeLabel = timeLabel;
		this.hashLabel = hashLabel;
		this.parentHashLabel = parentHashLabel;
		this.expandButton = expandButton;

		this.expandButton.selectedProperty().addListener((o, ov, nv) -> expandCommitMessage(nv));

		this.expandButton.prefWidthProperty().bind(this.expandButton.heightProperty());
	}

	private void expandCommitMessage(boolean expanded) {

		this.expandButton.setText(expanded ? "-" : "+");
		this.commitMessageLabel.setText(expanded ? getExpandedCommitMessage() : getCompactCommitMessage());
	}

	private String getCompactCommitMessage() {
		if (this.currentCommit == null) {
			return null;
		}

		return this.currentCommit.getShortMessage();
	}

	private String getExpandedCommitMessage() {
		if (this.currentCommit == null) {
			return null;
		}

		return this.currentCommit.getFullMessage();
	}

	@Override
	public void changed(ObservableValue<? extends RevCommit> observable, RevCommit oldValue, RevCommit newValue) {

		this.currentCommit = newValue;
		updatePanel();
	}

	private void updatePanel() {

		if (this.currentCommit != null) {
			this.commitMessageLabel.setText(this.currentCommit.getShortMessage());
			this.authorLabel.setText(this.currentCommit.getAuthorIdent().getName());
			this.emailLabel.setText(this.currentCommit.getAuthorIdent().getEmailAddress());
			this.timeLabel.setText(new Date(this.currentCommit.getCommitTime() * 1000L).toString());
			this.hashLabel.setText(this.currentCommit.getId().toString());
			this.parentHashLabel.setText(
					this.currentCommit.getParentCount() > 0 ? this.currentCommit.getParent(0).getId().toString() : "-");
			this.expandButton.setText("+");
			this.expandButton.setSelected(false);
		} else {
			this.commitMessageLabel.setText(null);
			this.authorLabel.setText(null);
			this.emailLabel.setText(null);
			this.timeLabel.setText(null);
			this.hashLabel.setText(null);
			this.parentHashLabel.setText(null);
			this.expandButton.setText("+");
		}

		boolean noCommit = this.currentCommit == null;

		this.commitMessageLabel.setDisable(noCommit);
		this.authorLabel.setDisable(noCommit);
		this.emailLabel.setDisable(noCommit);
		this.timeLabel.setDisable(noCommit);
		this.hashLabel.setDisable(noCommit);
		this.parentHashLabel.setDisable(noCommit);

		this.expandButton.setDisable(
				noCommit || this.currentCommit.getShortMessage().equals(this.currentCommit.getFullMessage().trim()));
	}
}
