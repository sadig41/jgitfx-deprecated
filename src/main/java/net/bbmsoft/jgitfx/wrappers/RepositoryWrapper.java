package net.bbmsoft.jgitfx.wrappers;

import org.eclipse.jgit.lib.Repository;

public class RepositoryWrapper {

	private final Repository repository;
	private final String name;

	public RepositoryWrapper(Repository repository) {
		this.repository = repository;
		this.name = this.repository != null ? this.repository.getWorkTree().getName() : null;
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
		return name;
	}

	public static class DummyWrapper extends RepositoryWrapper {

		private String name;

		public DummyWrapper(String name) {
			super(null);
			this.name = name;
		}
		
		@Override
		public String toString() {
			return name;
		}
		
	}
}
