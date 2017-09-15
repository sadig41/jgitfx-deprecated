package net.bbmsoft.jgitfx

import java.io.File
import javafx.scene.Scene
import javafx.stage.Stage
import net.bbmsoft.fxtended.annotations.app.launcher.Subapplication
import net.bbmsoft.jgitfx.models.impl.JsonFilePersistor
import net.bbmsoft.jgitfx.models.impl.PersistingRepositoryRegistry
import net.bbmsoft.jgitfx.modules.AppDirectoryProvider
import net.bbmsoft.jgitfx.modules.Preferences
import net.bbmsoft.jgitfx.modules.RepositoryOpener
import static extension net.bbmsoft.fxtended.extensions.BindingOperatorExtensions.*
class JGitFX extends Subapplication {

	def static void main(String[] args) {
		launch
	}

	override start(Stage stage) throws Exception {

		val persistor = new JsonFilePersistor
		val repoRegistry = new PersistingRepositoryRegistry(persistor)
		val opener = new RepositoryOpener

		if (repoRegistry.registeredRepositories.isEmpty && System.getProperty('test.repo') !== null) {
			repoRegistry.registeredRepositories.add = new File(System.getProperty('test.repo'))
		}

		val prefs = Preferences.loadFromFile(AppDirectoryProvider.getFilePathFromAppDirectory('config.json'))

		stage.scene = new Scene(new JGitFXMainFrame(prefs) => [

			cloneAction = [println('clone')]
			batchCloneAction = [println('batch clone')]
			initAction = [println('init')]
			openAction = [opener.openRepo(stage, repoRegistry)[repo|open(repo)]]
			quitAction = [println('quit')]
			aboutAction = [println('about')]

			registeredRepositories = repoRegistry.registeredRepositories
			
			if(prefs.lastOpened !== null) {
				open(prefs.lastOpened)
			}
		])
		
		stage.maximized = prefs.maximized
		stage.maximizedProperty > [
			prefs.maximized = stage.maximized
		]

		stage.show
	}

}
