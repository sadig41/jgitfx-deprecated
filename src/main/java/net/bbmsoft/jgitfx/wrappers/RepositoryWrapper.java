package net.bbmsoft.jgitfx.wrappers;

import org.eclipse.jgit.lib.Repository;

public class RepositoryWrapper {

	private final Repository repository;
	private final String name;
	private final String fullName;

	public RepositoryWrapper(Repository repository) {
		this.repository = repository;
		this.name = this.repository.getWorkTree().getName();
		this.fullName = String.format("%s (%s)", this.name, this.repository.getDirectory().getAbsolutePath());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((repository == null) ? 0 : repository.hashCode());
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
		RepositoryWrapper other = (RepositoryWrapper) obj;
		if (repository == null) {
			if (other.repository != null)
				return false;
		} else if (!repository.equals(other.repository))
			return false;
		return true;
	}

	public Repository getRepository() {
		return repository;
	}
	
	@Override
	public String toString() {
		return fullName;
	}

}
