package net.bbmsoft.jgitfx.modules;

import java.util.Date;

import org.eclipse.jgit.revwalk.RevCommit;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;

public class CommitInfoAnimator implements ChangeListener<RevCommit>{

	private final Label commitMessageLabel;
	private final Label authorLabel;
	private final Label emailLabel;
	private final Label timeLabel;
	private final Label hashLabel;
	private final Label parentHashLabel;
	private final ToggleButton expandButton;

	public CommitInfoAnimator(Label commitMessageLabel, Label authorLabel, Label emailLabel, Label timeLabel, Label hashLabel, Label parentHashLabel, ToggleButton expandButton) {
		this.commitMessageLabel = commitMessageLabel;
		this.authorLabel = authorLabel;
		this.emailLabel = emailLabel;
		this.timeLabel = timeLabel;
		this.hashLabel = hashLabel;
		this.parentHashLabel = parentHashLabel;
		this.expandButton = expandButton;
	}

	@Override
	public void changed(ObservableValue<? extends RevCommit> observable, RevCommit oldValue, RevCommit newValue) {
		
		if(newValue != null) {
			this.commitMessageLabel.setText(newValue.getShortMessage());
			this.authorLabel.setText(newValue.getAuthorIdent().getName());
			this.emailLabel.setText(newValue.getAuthorIdent().getEmailAddress());
			this.timeLabel.setText(new Date(newValue.getCommitTime() * 1000L).toString());
			this.hashLabel.setText(newValue.getId().toString());
			this.parentHashLabel.setText(newValue.getParentCount() > 0 ? newValue.getParent(0).getId().toString() : "-");
			this.expandButton.setText("+");
		} else {
			this.commitMessageLabel.setText(null);
			this.authorLabel.setText(null);
			this.emailLabel.setText(null);
			this.timeLabel.setText(null);
			this.hashLabel.setText(null);
			this.parentHashLabel.setText(null);
			this.expandButton.setText("+");
		}
		
		this.commitMessageLabel.setDisable(newValue == null);
		this.authorLabel.setDisable(newValue == null);
		this.emailLabel.setDisable(newValue == null);
		this.timeLabel.setDisable(newValue == null);
		this.hashLabel.setDisable(newValue == null);
		this.parentHashLabel.setDisable(newValue == null);
		this.expandButton.setDisable(newValue == null);
	}
}
