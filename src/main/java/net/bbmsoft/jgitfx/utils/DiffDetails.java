package net.bbmsoft.jgitfx.utils;

import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;

public class DiffDetails {

	private final Repository repository;
	private final String filePath;
	private final AbstractTreeIterator commit;
	private final AbstractTreeIterator parent;

	public DiffDetails(Repository repository, String filePath, AbstractTreeIterator commit, AbstractTreeIterator parent) {
		this.repository = repository;
		this.filePath = filePath;
		this.commit = commit;
		this.parent = parent;
	}

	public String getFilePath() {
		return filePath;
	}

	public AbstractTreeIterator getCommit() {
		return commit;
	}

	public AbstractTreeIterator getParent() {
		return parent;
	}

	public Repository getRepository() {
		return repository;
	}
}
