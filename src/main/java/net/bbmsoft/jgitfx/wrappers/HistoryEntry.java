package net.bbmsoft.jgitfx.wrappers;

import org.eclipse.jgit.revwalk.RevCommit;

public class HistoryEntry {

	private final RevCommit commit;

	public HistoryEntry(RevCommit commit) {
		this.commit = commit;
	}

	public RevCommit getCommit() {
		return commit;
	}
}
