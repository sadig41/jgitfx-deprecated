package net.bbmsoft.jgitfx.models;

import java.io.File;

import org.eclipse.jgit.lib.Repository;

import javafx.collections.ObservableList;
import net.bbmsoft.jgitfx.modules.RepositoryHandler;

public interface RepositoryRegistry {

	public ObservableList<File> getRegisteredRepositories();
	
	public boolean registerRepository(File repositoryFile);

	public boolean removeRepository(File repositoryFile);

	public RepositoryHandler getHandler(Repository repository);
}
