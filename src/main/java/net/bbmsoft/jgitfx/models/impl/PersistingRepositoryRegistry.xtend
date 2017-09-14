package net.bbmsoft.jgitfx.models.impl

import java.io.File
import java.util.List
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import net.bbmsoft.bbm.utils.Persistor
import net.bbmsoft.jgitfx.models.RepositoryRegistry

import static extension net.bbmsoft.fxtended.extensions.BindingOperatorExtensions.*

class PersistingRepositoryRegistry implements RepositoryRegistry {
	
	final ObservableList<File> repositories
	final Persistor<List<File>> persistor
	
	new(Persistor<List<File>> persistor) {
		this(persistor, FXCollections.observableArrayList)
	}
	
	new(Persistor<List<File>> persistor, ObservableList<File> repositories) {
		this.repositories = repositories
		this.persistor = persistor
		this.persistor.load[this.repositories.all = it]
		this.repositories >> [this.persistor.persist(this.repositories)]
	}
	
	override getRegisteredRepositories() {
		return this.repositories
	}
	
}