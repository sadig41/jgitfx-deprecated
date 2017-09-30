package net.bbmsoft.jgitfx.registry.impl

import java.io.File
import java.io.IOException
import java.util.HashMap
import java.util.List
import java.util.Map
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javax.inject.Inject
import javax.inject.Singleton
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
import org.eclipse.xtend.lib.annotations.Accessors

import static extension net.bbmsoft.fxtended.extensions.BindingOperatorExtensions.*

@Singleton
class PersistingRepositoryRegistry implements RepositoryRegistry {
	
	@Accessors final ObservableList<Repository> registeredRepositories
	final Persistor<List<File>> persistor
	final Map<String, RepositoryHandler> handlers

	final EventBroker eventBroker

	@Inject
	private new(Persistor<List<File>> persistor, EventBroker eventBroker) {
		
		this.registeredRepositories = FXCollections.observableArrayList
		this.persistor = persistor
		this.eventBroker = eventBroker
		this.handlers = new HashMap
		this.registeredRepositories >> [ added, removed |
			added.forEach[this.eventBroker.publish(RepositoryRegistryTopic.REPO_ADDED, directory)]
			removed.forEach[this.eventBroker.publish(RepositoryRegistryTopic.REPO_REMOVED, directory)]
		]
		this.persistor.load[forEach[registerRepository]]
		this.registeredRepositories > [this.persistor.persist(this.registeredRepositories.map[directory])]
		
//		Executors.newSingleThreadScheduledExecutor[new Thread(it, 'Refresh Scheduler') => [daemon = true]] => [
//			scheduleAtFixedRate([Platform.runLater[refreshAll]], 3, 3, TimeUnit.SECONDS)
//		]
	}
	
//	private def refreshAll() {
//		this.handlers.values.forEach[invalidate]
//	}

	override RepositoryHandler getRepositoryHandler(Repository repository) {
		getRepositoryHandler(repository.directory)
	}

	override RepositoryHandler getRepositoryHandler(File repositoryDirectory) {
		this.handlers.get(repositoryDirectory.absolutePath)
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

	override registerRepository(File repositoryFile) {

		if (!this.registeredRepositories.exists[directory.absolutePath == repositoryFile.absolutePath]) {
			try {
				val repo = loadRepo(repositoryFile)
				val handler = createHandler(repo)
				this.handlers.put(repo.workTree.absolutePath, handler)
				this.handlers.put(repo.directory.absolutePath, handler)
				val result = this.registeredRepositories.add(repo)
				if (result) {
					this.eventBroker.publish(RepositoryTopic.REPO_LOADED, handler)
				}
				result
			} catch (RepositoryNotFoundException e) {
				this.eventBroker.publish(RepositoryRegistryTopic.REPO_NOT_FOUND, repositoryFile)
				false
			} catch (Throwable e) {
				this.eventBroker.publish(MessageType.ERROR,
					new Message(
						'Could not open', '''Repositroy at «repositoryFile» could not be loaded: «IF e.message !== null»«e.message»«ELSE»«e.class.simpleName»«ENDIF»''',
						e))
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
	