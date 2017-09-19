package net.bbmsoft.jgitfx.models.impl

import java.io.File
import java.util.HashMap
import java.util.List
import java.util.Map
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import net.bbmsoft.bbm.utils.Persistor
import net.bbmsoft.bbm.utils.concurrent.TaskHelper
import net.bbmsoft.jgitfx.models.RepositoryRegistry
import net.bbmsoft.jgitfx.modules.RepositoryHandler
import org.eclipse.jgit.lib.Repository

import static extension net.bbmsoft.fxtended.extensions.BindingOperatorExtensions.*
import org.eclipse.xtend.lib.annotations.Accessors
import net.bbmsoft.jgitfx.messaging.Messenger

class PersistingRepositoryRegistry implements RepositoryRegistry {

	final ObservableList<File> repositories
	final Persistor<List<File>> persistor
	final Map<Repository, RepositoryHandler> handlers
	final Messenger messenger
	
	@Accessors TaskHelper taskHelper
	

	new(Persistor<List<File>> persistor, Messenger messenger) {
		this(persistor, FXCollections.observableArrayList, messenger)
	}

	new(Persistor<List<File>> persistor, ObservableList<File> repositories, Messenger messenger) {
		this.repositories = repositories
		this.persistor = persistor
		this.messenger = messenger
		this.persistor.load[this.repositories.all = it]
		this.repositories >> [this.persistor.persist(this.repositories)]
		this.handlers = new HashMap
	}

	override getRegisteredRepositories() {
		return this.repositories
	}
	
	override registerRepository(File repositoryFile) {
		if(!this.repositories.contains(repositoryFile)) {
			this.repositories.add(repositoryFile)
		} else {
			false
		}
	}
	
	override getHandler(Repository repository) {
		this.handlers.get(repository) ?: (new RepositoryHandler(repository, this.taskHelper, this.messenger) => [this.handlers.put(repository, it)])
	}
	
	override removeRepository(File repositoryFile) {
		throw new UnsupportedOperationException("TODO: auto-generated method stub")
	}

}
