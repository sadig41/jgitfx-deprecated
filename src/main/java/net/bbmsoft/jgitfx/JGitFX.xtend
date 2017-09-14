package net.bbmsoft.jgitfx

import javafx.scene.Scene
import javafx.stage.Stage
import net.bbmsoft.fxtended.annotations.app.launcher.Subapplication
import net.bbmsoft.jgitfx.models.impl.JsonFilePersistor
import net.bbmsoft.jgitfx.models.impl.PersistingRepositoryRegistry
import java.io.File
import net.bbmsoft.jgitfx.modules.RepositoryOpener

class JGitFX extends Subapplication {
	
	def static void main(String[] args) {
		launch
	}
	
	override start(Stage stage) throws Exception {
		
		val persistor = new JsonFilePersistor
		val repoRegistry = new PersistingRepositoryRegistry(persistor)
		val opener = new RepositoryOpener
		
		if(repoRegistry.registeredRepositories.isEmpty && System.getProperty('test.repo') !== null) {
			repoRegistry.registeredRepositories.add = new File(System.getProperty('test.repo'))
		}
		
		stage.scene = new Scene(new JGitFXMainFrame => [
			
			undoAction = [println('undo')]
			redoAction = [println('redo')]
			pullAction = [println('pull')]
			pushAction = [println('push')]
			branchAction = [println('branch')]
			stashAction = [println('stash')]
			popAction = [println('pop')]
			cloneAction = [println('clone')]
			initAction = [println('init')]
			openAction = [opener.openRepo(stage, repoRegistry)[repo|open(repo)]]
			quitAction = [println('quit')]
			aboutAction = [println('about')]
			
			registeredRepositories = repoRegistry.registeredRepositories
		])
		
		stage.show
	}
	
}