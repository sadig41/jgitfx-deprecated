package net.bbmsoft.jgitfx

import com.google.inject.Guice
import java.io.File
import javafx.scene.Scene
import javafx.stage.Stage
import javax.inject.Inject
import net.bbmsoft.bbm.utils.concurrent.TaskHelper
import net.bbmsoft.fxtended.annotations.app.launcher.Subapplication
import net.bbmsoft.jgitfx.event.AppStatus
import net.bbmsoft.jgitfx.event.EventBroker
import net.bbmsoft.jgitfx.event.EventPublisher
import net.bbmsoft.jgitfx.event.RepositoryRegistryTopic
import net.bbmsoft.jgitfx.event.TaskTopic
import net.bbmsoft.jgitfx.event.UserInteraction
import net.bbmsoft.jgitfx.inject.impl.JGitFXModule
import net.bbmsoft.jgitfx.messaging.Message
import net.bbmsoft.jgitfx.messaging.MessageType
import net.bbmsoft.jgitfx.messaging.MessengerListener
import net.bbmsoft.jgitfx.modules.Preferences
import net.bbmsoft.jgitfx.modules.RepositoryOpener
import net.bbmsoft.jgitfx.registry.RepositoryRegistry

import static extension net.bbmsoft.fxtended.extensions.BindingOperatorExtensions.*
import net.bbmsoft.jgitfx.modules.RepositoryStatusMonitor

class JGitFX extends Subapplication {

	final JGitFXMainFrame jGitFXMainFrame
	final EventBroker eventBroker
	final Preferences prefs

	Stage stage

	def static void main(String[] args) {
		Subapplication.launch[|Guice.createInjector(new JGitFXModule).getInstance(JGitFX)]
	}

	@Inject
	private new(
		JGitFXMainFrame jGitFXMainFrame,
		TaskHelper gitTaskHelper,
		EventBroker eventBroker,
		MessengerListener messageListener,
		RepositoryRegistry repoRegistry,
		RepositoryOpener opener,
		Preferences prefs
	) {
		this.jGitFXMainFrame = jGitFXMainFrame
		this.eventBroker = eventBroker
		this.prefs = prefs
		
		new RepositoryStatusMonitor(eventBroker)

		eventBroker.subscribe(MessageType.values(), messageListener)
		eventBroker.subscribe(RepositoryRegistryTopic.REPO_NOT_FOUND)[repoNotFound($1, eventBroker)]

		eventBroker.subscribe(TaskTopic.TASK_STARTED) [
			gitTaskHelper.submitTask($1, null)
		]

		eventBroker.subscribe(UserInteraction.CLONE)[println('clone')]
		eventBroker.subscribe(UserInteraction.BATCH_CLONE)[println('batch clone')]
		eventBroker.subscribe(UserInteraction.INIT_REPO)[println('init')]
		eventBroker.subscribe(UserInteraction.ADD_REPO)[opener.openRepo(this.stage, repoRegistry)]
		eventBroker.subscribe(UserInteraction.QUIT)[println('quit')]
		eventBroker.subscribe(UserInteraction.SHOW_ABOUT)[println('about')]

		this.eventBroker.publish(AppStatus.STARTING, System.currentTimeMillis)
	}

	override start(Stage stage) throws Exception {

		this.stage = stage

		stage.scene = new Scene(this.jGitFXMainFrame) => [stylesheets.add = 'style/default.css']
		stage.focusedProperty >> [if(it) this.eventBroker.publish(AppStatus.FOCUSED, System.currentTimeMillis)]

		stage.maximized = this.prefs.maximized
		stage.maximizedProperty > [
			this.prefs.maximized = stage.maximized
		]

		this.eventBroker.publish(AppStatus.STARTED, System.currentTimeMillis)

		stage.show
	}

	private def repoNotFound(File dir, EventPublisher publisher) {
		val title = 'Failed to load repository'
		val body = '''«dir.absolutePath» does not seem to contain a valid git repository.'''
		publisher.publish(MessageType.ERROR, new Message(title, body))
	}

}
