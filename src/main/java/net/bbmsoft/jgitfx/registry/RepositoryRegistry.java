package net.bbmsoft.jgitfx.registry;

import java.io.File;
import java.util.List;

import org.eclipse.jgit.lib.Repository;

import net.bbmsoft.jgitfx.modules.RepositoryHandler;

public interface RepositoryRegistry {

	public List<Repository> getRegisteredRepositories();

	public RepositoryHandler getRepositoryHandler(File directory);

	public RepositoryHandler getRepositoryHandler(Repository repository);
	
	public boolean registerRepository(File repositoryFile);

	public boolean removeRepository(File repositoryFile);
}
