package net.bbmsoft.jgitfx

import java.net.URL
import java.util.ResourceBundle
import javafx.scene.layout.BorderPane
import net.bbmsoft.fxtended.annotations.app.FXMLRoot
import net.bbmsoft.fxtended.annotations.binding.BindableProperty

@FXMLRoot
class JGitFXMainFrame extends BorderPane{
	
	@BindableProperty Runnable undoAction
	@BindableProperty Runnable redoAction
	@BindableProperty Runnable pullAction
	@BindableProperty Runnable pushAction
	@BindableProperty Runnable branchAction
	@BindableProperty Runnable stashAction
	@BindableProperty Runnable popAction
	
	override initialize(URL location, ResourceBundle resources) {
	}
	
	
	def undo() {
		this.undoAction?.run
	}
	
	def redo() {
		this.redoAction?.run
	}
	
	def pull() {
		this.pullAction?.run
	}
	
	def push() {
		this.pushAction?.run
	}
	
	
	def branch() {
		this.branchAction?.run
	}
	
	def stash() {
		this.stashAction?.run
	}
	
	
	def pop() {
		this.popAction?.run
	}
}