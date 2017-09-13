package net.bbmsoft.jgitfx

import javafx.scene.Scene
import javafx.stage.Stage
import net.bbmsoft.fxtended.annotations.app.launcher.Subapplication

class JGitFX extends Subapplication {
	
	def static void main(String[] args) {
		launch
	}
	
	override start(Stage it) throws Exception {
		
		scene = new Scene(new JGitFXMainFrame => [
			undoAction = [println('undo')]
			redoAction = [println('redo')]
			pullAction = [println('pull')]
			pushAction = [println('push')]
			branchAction = [println('branch')]
			stashAction = [println('stash')]
			popAction = [println('pop')]
		])
		
		show
	}
	
}