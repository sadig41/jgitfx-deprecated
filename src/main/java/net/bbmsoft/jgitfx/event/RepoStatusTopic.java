package net.bbmsoft.jgitfx.event;

import org.eclipse.jgit.lib.Repository;

public enum RepoStatusTopic implements Topic<Repository> {

	STAGED_CHANGES, UNSTAGED_CHANGES, COMMITS_AHEAD, COMMITS_BEHIND, CHILDREN_OUT_OF_SYNC;
	
	private int count;

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}
}
