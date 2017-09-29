package net.bbmsoft.jgitfx

import java.io.File
import javafx.scene.Scene
import javafx.stage.Stage
import net.bbmsoft.bbm.utils.concurrent.SimpleTaskHelper
import net.bbmsoft.bbm.utils.concurrent.TaskHelper
import net.bbmsoft.fxtended.annotations.app.launcher.Subapplication
import net.bbmsoft.jgitfx.event.EventBroker
import net.bbmsoft.jgitfx.event.EventBroker.Topic
import net.bbmsoft.jgitfx.event.EventPublisher
import net.bbmsoft.jgitfx.event.RepositoryRegistryTopic
import net.bbmsoft.jgitfx.event.RepositoryTopic
import net.bbmsoft.jgitfx.event.SimpleEventBroker
import net.bbmsoft.jgitfx.event.TaskTopic
import net.bbmsoft.jgitfx.event.UserInteraction
import net.bbmsoft.jgitfx.messaging.DialogMessenger
import net.bbmsoft.jgitfx.messaging.Message
import net.bbmsoft.jgitfx.messaging.MessageType
import net.bbmsoft.jgitfx.messaging.MessengerListener
import net.bbmsoft.jgitfx.modules.AppDirectoryProvider
import net.bbmsoft.jgitfx.modules.GitWorkerExecutorManager
import net.bbmsoft.jgitfx.modules.Preferences
import net.bbmsoft.jgitfx.modules.RepositoryListener
import net.bbmsoft.jgitfx.modules.RepositoryOpener
import net.bbmsoft.jgitfx.registry.impl.JsonFilePersistor
import net.bbmsoft.jgitfx.registry.impl.PersistingRepositoryRegistry

import static extension net.bbmsoft.fxtended.extensions.BindingOperatorExtensions.*

class JGitFX extends Subapplication {

	def static void main(String[] args) {
		launch
	}

	override start(Stage stage) throws Exception {

		val eventBroker = new SimpleEventBroker

		val messageListener = new MessengerListener(new DialogMessenger)
		
		eventBroker.subscribe(MessageType.values, messageListener)
		eventBroker.subscribe(RepositoryRegistryTopic.REPO_NOT_FOUND)[repoNotFound($1, eventBroker)]

		val gitWorker = new GitWorkerExecutorManager
		val prefs = Preferences.loadFromFile(AppDirectoryProvider.getFilePathFromAppDirectory('config.json'))
		val jGitFXMainFrame = new JGitFXMainFrame(prefs, gitWorker, eventBroker)
		val repositoryRegistryListener = new RepositoryListener(jGitFXMainFrame) => [appStarting = true]
		
		eventBroker.subscribe(RepositoryTopic.values, repositoryRegistryListener)

		val TaskHelper gitTaskHelper = new SimpleTaskHelper(jGitFXMainFrame.taskList, gitWorker)

		eventBroker.subscribe(TaskTopic.PullTask.STARTED) [gitTaskHelper.submitTask($1, eventBroker.publish(TaskTopic.PullResult.FINISHED))]
		eventBroker.subscribe(TaskTopic.PushTask.STARTED) [gitTaskHelper.submitTask($1, eventBroker.publish(TaskTopic.PushResult.FINISHED))]
		eventBroker.subscribe(TaskTopic.FetchTask.STARTED) [gitTaskHelper.submitTask($1, eventBroker.publish(TaskTopic.FetchResult.FINISHED))]
		eventBroker.subscribe(TaskTopic.CommitTask.STARTED) [gitTaskHelper.submitTask($1, eventBroker.publish(TaskTopic.CommitResult.FINISHED))]
		eventBroker.subscribe(TaskTopic.MergeTask.STARTED) [gitTaskHelper.submitTask($1, eventBroker.publish(TaskTopic.MergeResult.FINISHED))]
		eventBroker.subscribe(TaskTopic.RebaseTask.STARTED) [gitTaskHelper.submitTask($1, eventBroker.publish(TaskTopic.RebaseResult.FINISHED))]
		eventBroker.subscribe(TaskTopic.StageTask.STARTED) [gitTaskHelper.submitTask($1, eventBroker.publish(TaskTopic.StageResult.FINISHED))]

		val persistor = new JsonFilePersistor
		val repoRegistry = new PersistingRepositoryRegistry(persistor, eventBroker)
		val opener = new RepositoryOpener

		eventBroker.subscribe(UserInteraction.CLONE)[println('clone')]
		eventBroker.subscribe(UserInteraction.BATCH_CLONE)[println('batch clone')]
		eventBroker.subscribe(UserInteraction.INIT_REPO)[println('init')]
		eventBroker.subscribe(UserInteraction.OPEN_REPO)[opener.openRepo(stage, repoRegistry)]
		eventBroker.subscribe(UserInteraction.QUIT)[println('quit')]
		eventBroker.subscribe(UserInteraction.SHOW_ABOUT)[println('about')]

		jGitFXMainFrame => [

			repositoryRegistry = repoRegistry

			val lastOpened = prefs.lastOpened
			if (lastOpened !== null) {
				val lastOpenedHandler = repoRegistry.getRepositoryHandler(lastOpened)
				if (lastOpenedHandler !== null) {
					open(lastOpenedHandler.repository)
				}
			}
		]

		repositoryRegistryListener.appStarting = false

		stage.scene = new Scene(jGitFXMainFrame)

		stage.maximized = prefs.maximized
		stage.maximizedProperty > [
			prefs.maximized = stage.maximized
		]

		stage.show
	}

	private def <T> publish(EventBroker eventBroker, Topic<T> topic) {
		[eventBroker.publish(topic, it)]
	}

	private def repoNotFound(File dir, EventPublisher publisher) {
		val title = 'Failed to load repository'
		val body = '''«dir.absolutePath» does not seem to contain a valid git repository.'''
		publisher.publish(MessageType.ERROR, new Message(title, body))
	}

}
