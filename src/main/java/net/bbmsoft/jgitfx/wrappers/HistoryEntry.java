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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((commit == null) ? 0 : commit.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		HistoryEntry other = (HistoryEntry) obj;
		if (commit == null) {
			if (other.commit != null)
				return false;
		} else if (!commit.equals(other.commit))
			return false;
		return true;
	}

}
