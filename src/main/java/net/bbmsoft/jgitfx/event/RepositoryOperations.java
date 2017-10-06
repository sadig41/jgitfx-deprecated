package net.bbmsoft.jgitfx.event;

import java.util.List;

import org.eclipse.jgit.diff.DiffEntry;

import net.bbmsoft.jgitfx.modules.RepositoryHandler;

public enum RepositoryOperations implements Topic<RepositoryHandler> {

	UNDO, REDO, COMMIT, PUSH, PULL, FETCH, BRANCH, STASH, POP, STAGE, UNSTAGE;
	
	private String message;
	private List<DiffEntry> diffs;

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public List<DiffEntry> getDiffs() {
		return diffs;
	}

	public void setDiffs(List<DiffEntry> diffs) {
		this.diffs = diffs;
	}
}
