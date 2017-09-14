package net.bbmsoft.jgitfx

import java.io.File
import java.net.URL
import java.util.ResourceBundle
import javafx.beans.InvalidationListener
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

import static extension net.bbmsoft.fxtended.extensions.BindingOperatorExtensions.*
import org.eclipse.jgit.lib.Repository
import java.util.Map
import java.util.HashMap

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

	@BindableProperty ObservableList<File> registeredRepositories
	Map<File, Repository> repositoryMap

	InvalidationListener repositoriesListener

	TreeItem<RepositoryWrapper> rootRepoTreeItem

	override initialize(URL location, ResourceBundle resources) {
		this.repositoryMap = new HashMap
		this.repositoriesListener = [updateRepoTree]
		this.registeredRepositories = FXCollections.observableArrayList
		this.registeredRepositoriesProperty >> [o, ov, nv|updateRepositoriesListener(ov, nv)]
		this.rootRepoTreeItem = new TreeItem
		this.repositoryTree.root = rootRepoTreeItem
		this.repositoryTree.showRoot = false
		this.repositoryTree.onMouseClicked = [
			if (clickCount == 2) {
				this.repositoryTree.selectionModel.selectedItem?.value?.repository?.open
			}
		]
	}

	def updateRepositoriesListener(ObservableList<File> oldList, ObservableList<File> newList) {
		oldList?.removeListener(this.repositoriesListener)
		newList?.addListener(this.repositoriesListener)
		updateRepoTree
	}

	private def updateRepoTree() {
		this.repositoryMap.clear
		val repos = newArrayList
		this.registeredRepositories.forEach[repos.add = loadRepoTreeItem]
		this.rootRepoTreeItem.children.all = repos
	}

	private def TreeItem<RepositoryWrapper> loadRepoTreeItem(File repoDir) {
		new TreeItem(loadRepo(repoDir)) => [addSubrepos]
	}

	private def addSubrepos(TreeItem<RepositoryWrapper> repoItem) {
		// TODO add subrepos
	}

	private def RepositoryWrapper loadRepo(File dir) {
		val repository = new FileRepositoryBuilder().setGitDir(dir).readEnvironment.findGitDir.build
		this.repositoryMap.put(dir, repository)
		new RepositoryWrapper(repository)
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

	def boolean open(File repoDir) {
		val repo = this.repositoryMap.get(repoDir)
		if (repo !== null) {
			open(repo)
		} else {
			false
		}
	}

	def boolean open(Repository repository) {
		// TODO
		println('''Opening repo «repository.directory.absolutePath»''')
		true
	}

}
