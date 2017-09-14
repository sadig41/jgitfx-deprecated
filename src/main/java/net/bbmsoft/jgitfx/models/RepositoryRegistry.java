package net.bbmsoft.jgitfx.models;

import java.io.File;

import javafx.collections.ObservableList;

public interface RepositoryRegistry {

	public ObservableList<File> getRegisteredRepositories();
}
