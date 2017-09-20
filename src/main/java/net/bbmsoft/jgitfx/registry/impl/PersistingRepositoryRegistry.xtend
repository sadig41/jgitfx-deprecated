package net.bbmsoft.jgitfx.registry.impl

import java.io.File
import java.io.IOException
import java.util.HashMap
import java.util.List
import java.util.Map
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import net.bbmsoft.bbm.utils.Persistor
import net.bbmsoft.jgitfx.event.EventBroker
import net.bbmsoft.jgitfx.event.RepositoryRegistryTopic
import net.bbmsoft.jgitfx.event.RepositoryTopic
import net.bbmsoft.jgitfx.messaging.Message
import net.bbmsoft.jgitfx.messaging.MessageType
import net.bbmsoft.jgitfx.modules.RepositoryHandler
import net.bbmsoft.jgitfx.registry.RepositoryRegistry
import org.eclipse.jgit.errors.RepositoryNotFoundException
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.storage.file.FileRepositoryBuilder

import static extension net.bbmsoft.fxtended.extensions.BindingOperatorExtensions.*

class PersistingRepositoryRegistry implements RepositoryRegistry {

	final ObservableList<File> registeredRepositories
	final Persistor<List<File>> persistor
	final Map<File, Repository> repositories
	final Map<Repository, RepositoryHandler> handlers

	final EventBroker eventBroker

	new(Persistor<List<File>> persistor, EventBroker eventBroker) {
		this(persistor, FXCollections.observableArrayList, eventBroker)
	}

	new(Persistor<List<File>> persistor, ObservableList<File> repositories, EventBroker eventBroker) {

		this.registeredRepositories = repositories
		this.persistor = persistor
		this.eventBroker = eventBroker
		this.repositories = new HashMap
		this.handlers = new HashMap
		this.registeredRepositories >> [ added, removed |
			added.forEach[this.eventBroker.publish(RepositoryRegistryTopic.REPO_ADDED, it)]
			removed.forEach[this.eventBroker.publish(RepositoryRegistryTopic.REPO_REMOVED, it)]
		]
		this.persistor.load[forEach[registerRepository]]
		this.registeredRepositories > [this.persistor.persist(this.registeredRepositories)]
	}

	override RepositoryHandler getRepositoryHandler(Repository repository) {
		this.handlers.get(repository)
	}

	override RepositoryHandler getRepositoryHandler(File repositoryDirectory) {
		val repository = this.repositories.get(repositoryDirectory)
		if (repository !== null) {
			this.handlers.get(repository)
		}
	}
	
	private def RepositoryHandler createHandler(Repository repository) {
		return new RepositoryHandler(repository, this.eventBroker)
	}

	private def Repository loadRepo(File dir) throws RepositoryNotFoundException, IOException {

		val builder = new FileRepositoryBuilder => [
			mustExist = true
			if ('.git'.equals(dir.name)) {
				gitDir = dir
			} else {
				workTree = dir
			}
			readEnvironment
		]

		builder.build
	}

	override getRegisteredRepositories() {
		return this.registeredRepositories
	}

	override registerRepository(File repositoryFile) {

		if (!this.registeredRepositories.contains(repositoryFile)) {
			try {
				val repo = loadRepo(repositoryFile)
				this.repositories.put(repo.directory, repo)
				this.repositories.put(repo.workTree, repo)
				val handler = createHandler(repo)
				this.handlers.put(repo, handler)
				val result = this.registeredRepositories.add(repo.directory)
				if(result) {
					this.eventBroker.publish(RepositoryTopic.REPO_LOADED, handler)
				}
				result
			} catch (RepositoryNotFoundException e) {
				this.eventBroker.publish(RepositoryRegistryTopic.REPO_NOT_FOUND, repositoryFile)
				false
			} catch (Throwable e) {
				this.eventBroker.publish(MessageType.ERROR, new Message('Could not open', '''Repositroy at «repositoryFile» could not be loaded:''', e))
				false
			}
		} else {
			false
		}
	}

	override removeRepository(File repositoryFile) {
		throw new UnsupportedOperationException("TODO: auto-generated method stub")
	}

}
