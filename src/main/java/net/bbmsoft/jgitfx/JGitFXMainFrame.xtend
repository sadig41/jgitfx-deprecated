package net.bbmsoft.jgitfx

import java.io.File
import java.net.URL
import java.util.HashMap
import java.util.Map
import java.util.ResourceBundle
import javafx.application.Platform
import javafx.beans.InvalidationListener
import javafx.beans.Observable
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.fxml.FXML
import javafx.scene.control.Label
import javafx.scene.control.MenuItem
import javafx.scene.control.SelectionMode
import javafx.scene.control.TableColumn
import javafx.scene.control.TableView
import javafx.scene.control.TitledPane
import javafx.scene.control.ToggleButton
import javafx.scene.control.TreeItem
import javafx.scene.control.TreeView
import javafx.scene.layout.BorderPane
import net.bbmsoft.fxtended.annotations.app.FXMLRoot
import net.bbmsoft.fxtended.annotations.binding.BindableProperty
import net.bbmsoft.jgitfx.modules.CommitInfoAnimator
import net.bbmsoft.jgitfx.modules.Preferences
import net.bbmsoft.jgitfx.modules.RepositoryHandler
import net.bbmsoft.jgitfx.modules.RepositoryTableVisualizer
import net.bbmsoft.jgitfx.wrappers.RepositoryWrapper
import org.controlsfx.control.BreadCrumbBar
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.revwalk.RevCommit
import org.eclipse.jgit.storage.file.FileRepositoryBuilder

import static extension net.bbmsoft.fxtended.extensions.BindingOperatorExtensions.*

@FXMLRoot
class JGitFXMainFrame extends BorderPane {

	private static final String BREADCRUMB_ROOT_FILE = 'JGitFX'

	@FXML TableView<RevCommit> historyTable
	@FXML TableColumn<RevCommit, String> branchColumn
	@FXML TableColumn<RevCommit, String> commitMessageColumn
	@FXML TableColumn<RevCommit, String> authorColumn
	@FXML TableColumn<RevCommit, String> timeColumn
	@FXML TreeView<RepositoryWrapper> repositoryTree

	@FXML TitledPane repositoryOverview
	@FXML TitledPane repositoriesList

	@FXML BreadCrumbBar<RepositoryWrapper> breadcrumb
	
	@FXML Label commitMessageLabel
	@FXML Label authorLabel
	@FXML Label emailLabel
	@FXML Label timeLabel
	@FXML Label hashLabel
	@FXML Label parentHashLabel
	@FXML ToggleButton expandCommitMessageButton
	
	@FXML MenuItem undoContextMenuItem
	@FXML MenuItem redoContextMenuItem
	@FXML MenuItem pullContextMenuItem
	@FXML MenuItem pushContextMenuItem
	@FXML MenuItem branchContextMenuItem
	@FXML MenuItem stashContextMenuItem
	@FXML MenuItem popContextMenuItem

	@BindableProperty Runnable cloneAction
	@BindableProperty Runnable batchCloneAction
	@BindableProperty Runnable initAction
	@BindableProperty Runnable openAction
	@BindableProperty Runnable quitAction
	@BindableProperty Runnable aboutAction

	@BindableProperty ObservableList<File> registeredRepositories
	Map<File, Repository> repositoryMap

	InvalidationListener repositoriesListener
	InvalidationListener repositoryListener

	TreeItem<RepositoryWrapper> rootRepoTreeItem
	TreeItem<RepositoryWrapper> breadCrumbRoot

	@BindableProperty RepositoryHandler repositoryHandler

	RepositoryTableVisualizer historyVisualizer

	Preferences prefs
	
	new(Preferences prefs) {
		this()
		this.prefs = prefs
	}

	override initialize(URL location, ResourceBundle resources) {

		this.historyVisualizer = new RepositoryTableVisualizer(this.historyTable, this.branchColumn,
			this.commitMessageColumn, this.authorColumn, this.timeColumn)
		this.repositoryMap = new HashMap
		this.repositoriesListener = [updateRepoTree]
		this.repositoryListener = [updateRepo]
		this.registeredRepositories = FXCollections.observableArrayList
		this.registeredRepositoriesProperty >> [o, ov, nv|updateRepositoriesListener(ov, nv)]
		this.breadCrumbRoot = new TreeItem(new File(BREADCRUMB_ROOT_FILE))
		this.breadcrumb.selectedCrumb = this.breadCrumbRoot
		this.rootRepoTreeItem = new TreeItem
		this.repositoryTree.root = rootRepoTreeItem
		this.repositoryTree.showRoot = false
		this.repositoryTree.onMouseClicked = [
			if (clickCount == 2) {
				this.repositoryTree.selectionModel.selectedItem?.value?.repository?.open
			}
		]
		
		this.repositoryTree.selectionModel.selectionMode = SelectionMode.MULTIPLE
		this.repositoryTree.selectionModel.selectedItems > [updateRepositoryTreeContextMenu]
		
		this.historyTable.selectionModel.selectedItemProperty.addListener(new CommitInfoAnimator(this.commitMessageLabel, this.authorLabel, this.emailLabel, this.timeLabel, this.hashLabel, this.parentHashLabel, this.expandCommitMessageButton))

		Platform.runLater[this.repositoriesList.expanded = true]
	}
	
	private def updateRepositoryTreeContextMenu() {
		
		val size = this.repositoryTree.selectionModel.selectedItems.size
		
		this.undoContextMenuItem.disable = size <= 0 || size > 1
		this.redoContextMenuItem.disable = size <= 0 || size > 1
		this.pullContextMenuItem.disable = size <= 0
		this.pushContextMenuItem.disable = size <= 0
		this.branchContextMenuItem.disable = size <= 0 || size > 1
		this.stashContextMenuItem.disable = size <= 0 || size > 1
		this.popContextMenuItem.disable = size <= 0 || size > 1
		
	}

	def updateRepo(Observable repoHandler) {
		if (repoHandler instanceof RepositoryHandler) {
			// TODO
			println('''Updating view of «repoHandler?.repository»''')
			this.historyVisualizer.repository = repoHandler.repository
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

		val builder = new FileRepositoryBuilder => [
			mustExist = true
			if ('.git'.equals(dir.name)) {
				gitDir = dir
			} else {
				workTree = dir
			}
			readEnvironment
		]

		val repository = builder.build
		this.repositoryMap.put(dir, repository)
		new RepositoryWrapper(repository)
	}

	def undo() {
		undo(this.repositoryHandler)
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
	
	private def undo(RepositoryHandler repos) {
		repos?.undo
	}

	private def redo(RepositoryHandler repos) {
		repos?.redo
	}

	private def pull(RepositoryHandler ... repos) {
		repos?.forEach[pull]
	}

	private def push(RepositoryHandler ... repos) {
		repos?.forEach[push]
	}

	private def branch(RepositoryHandler repos) {
		repos?.branch
	}

	private def stash(RepositoryHandler repos) {
		repos?.stash
	}

	private def pop(RepositoryHandler repos) {
		repos?.pop
	}
	
	private def RepositoryHandler getHandlerForSelected() {
		this.repositoryTree.selectionModel.selectedItem?.value?.repository?.getHandler
	}
	
	private def RepositoryHandler[] getHandlersForSelected() {
		this.repositoryTree.selectionModel.selectedItems.map[value?.repository?.getHandler]
	}
	
	private def RepositoryHandler getHandler(Repository repository) {
		getHandler(repository, null)
	}
	
	private def RepositoryHandler getHandler(Repository repository, InvalidationListener listener) {
		new RepositoryHandler(repository, listener)
	}
	
	def undoSelected() {
		undo(handlerForSelected)
	}

	def redoSelected() {
		redo(handlerForSelected)
	}

	def pullSelected() {
		pull(handlersForSelected)
	}

	def pushSelected() {
		push(handlersForSelected)
	}

	def branchSelected() {
		branch(handlerForSelected)
	}

	def stashSelected() {
		stash(handlerForSelected)
	}

	def popSelected() {
		pop(handlerForSelected)
	}

	def cloneRepo() {
		this.cloneAction?.run
	}

	def batchCloneRepo() {
		this.batchCloneAction?.run
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

	private def TreeItem<RepositoryWrapper> buildCrumb(Repository repo) {
		// TODO include hierarchy
		new TreeItem(new RepositoryWrapper(repo, false)) => [
			this.breadCrumbRoot.children.all = #[it]
		]
	}

	def boolean open(Repository repository) {
		println('''Opening repo «repository.directory.absolutePath»''')
		this.repositoryHandler?.removeListener(this.repositoryListener)
		this.repositoryHandler = new RepositoryHandler(repository, this.repositoryListener)
		val treeItem = buildCrumb(repository)
		this.breadcrumb.selectedCrumb = treeItem
		if (prefs.switchToRepositoryOverview) {
			this.repositoryOverview.expanded = true
		}
		true
	}

}
