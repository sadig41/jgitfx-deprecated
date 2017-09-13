package net.bbmsoft.jgitfx

import java.io.File
import java.net.URL
import java.util.ResourceBundle
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.fxml.FXML
import javafx.scene.control.TreeItem
import javafx.scene.control.TreeView
import javafx.scene.layout.BorderPane
import net.bbmsoft.fxtended.annotations.app.FXMLRoot
import net.bbmsoft.fxtended.annotations.binding.BindableProperty
import net.bbmsoft.jgitfx.wrappers.RepositoryWrapper
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import org.eclipse.xtend.lib.annotations.Accessors

import static extension net.bbmsoft.fxtended.extensions.BindingOperatorExtensions.*

@FXMLRoot
class JGitFXMainFrame extends BorderPane {

	@FXML TreeView<RepositoryWrapper> repositoryTree

	@BindableProperty Runnable undoAction
	@BindableProperty Runnable redoAction
	@BindableProperty Runnable pullAction
	@BindableProperty Runnable pushAction
	@BindableProperty Runnable branchAction
	@BindableProperty Runnable stashAction
	@BindableProperty Runnable popAction
	@BindableProperty Runnable cloneAction
	@BindableProperty Runnable initAction
	@BindableProperty Runnable openAction
	@BindableProperty Runnable quitAction
	@BindableProperty Runnable aboutAction

	@Accessors(PUBLIC_GETTER) final ObservableList<File> registeredRepositories = FXCollections.observableArrayList
	
	TreeItem<RepositoryWrapper> rootRepoTreeItem

	override initialize(URL location, ResourceBundle resources) {
		this.rootRepoTreeItem = new TreeItem
		this.repositoryTree.root = rootRepoTreeItem
		this.repositoryTree.showRoot = false
		this.registeredRepositories >> [updateRepoTree]
	}

	private def updateRepoTree() {
		
		this.registeredRepositories.forEach[this.rootRepoTreeItem.children.add = loadRepoTreeItem]
	}
	
	private def TreeItem<RepositoryWrapper> loadRepoTreeItem(File repoDir) {
		new TreeItem(loadRepo(repoDir))	=> [addSubrepos]
	}
	
	private def addSubrepos(TreeItem<RepositoryWrapper> repoItem) {
		// TODO add subrepos
	}
	
	private def RepositoryWrapper loadRepo(File dir) {
		new RepositoryWrapper(new FileRepositoryBuilder().setGitDir(dir).readEnvironment.findGitDir.build)
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

	def cloneRepo() {
		this.cloneAction?.run
	}

	def initRepo() {
		this.initAction?.run
	}

	def openRepo() {
		this.openAction?.run
	}

	def quit() {
		this.quitAction?.run
	}

	def about() {
		this.aboutAction?.run
	}
}
