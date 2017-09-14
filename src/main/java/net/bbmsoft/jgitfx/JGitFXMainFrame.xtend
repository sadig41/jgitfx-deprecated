package net.bbmsoft.jgitfx

import java.io.File
import java.net.URL
import java.util.HashMap
import java.util.Map
import java.util.ResourceBundle
import javafx.beans.InvalidationListener
import javafx.beans.Observable
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.fxml.FXML
import javafx.scene.control.TreeItem
import javafx.scene.control.TreeView
import javafx.scene.layout.BorderPane
import net.bbmsoft.fxtended.annotations.app.FXMLRoot
import net.bbmsoft.fxtended.annotations.binding.BindableProperty
import net.bbmsoft.jgitfx.modules.RepositoryHandler
import net.bbmsoft.jgitfx.wrappers.RepositoryWrapper
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.storage.file.FileRepositoryBuilder

import static extension net.bbmsoft.fxtended.extensions.BindingOperatorExtensions.*

@FXMLRoot
class JGitFXMainFrame extends BorderPane {

	@FXML TreeView<RepositoryWrapper> repositoryTree

	@BindableProperty Runnable cloneAction
	@BindableProperty Runnable initAction
	@BindableProperty Runnable openAction
	@BindableProperty Runnable quitAction
	@BindableProperty Runnable aboutAction

	@BindableProperty ObservableList<File> registeredRepositories
	Map<File, Repository> repositoryMap

	InvalidationListener repositoriesListener
	InvalidationListener repositoryListener

	TreeItem<RepositoryWrapper> rootRepoTreeItem

	@BindableProperty RepositoryHandler repositoryHandler

	override initialize(URL location, ResourceBundle resources) {
		this.repositoryMap = new HashMap
		this.repositoriesListener = [updateRepoTree]
		this.repositoryListener = [updateRepo]
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

	def updateRepo(Observable repoHandler) {
		if (repoHandler instanceof RepositoryHandler) {
			// TODO
			println('''Updating view of «repoHandler?.repository»''')
		}
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
		this.repositoryHandler?.undo
	}

	def redo() {
		this.repositoryHandler?.redo
	}

	def pull() {
		this.repositoryHandler?.pull
	}

	def push() {
		this.repositoryHandler?.push
	}

	def branch() {
		this.repositoryHandler?.branch
	}

	def stash() {
		this.repositoryHandler?.stash
	}

	def pop() {
		this.repositoryHandler?.pop
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
		println('''Opening repo «repository.directory.absolutePath»''')
		this.repositoryHandler?.removeListener(this.repositoryListener)
		this.repositoryHandler = new RepositoryHandler(repository, this.repositoryListener)
		// TODO
		true
	}

}
