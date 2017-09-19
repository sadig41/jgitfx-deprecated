package net.bbmsoft.jgitfx

import java.io.File
import java.net.URL
import java.util.HashMap
import java.util.Map
import java.util.ResourceBundle
import java.util.concurrent.ExecutorService
import javafx.application.Platform
import javafx.beans.InvalidationListener
import javafx.beans.Observable
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.concurrent.Task
import javafx.fxml.FXML
import javafx.scene.control.Label
import javafx.scene.control.MenuItem
import javafx.scene.control.SelectionMode
import javafx.scene.control.TableColumn
import javafx.scene.control.TableView
import javafx.scene.control.TitledPane
import javafx.scene.control.TreeItem
import javafx.scene.control.TreeView
import javafx.scene.layout.BorderPane
import net.bbmsoft.bbm.utils.Lockable
import net.bbmsoft.bbm.utils.concurrent.TaskHelper
import net.bbmsoft.fxtended.annotations.app.FXMLRoot
import net.bbmsoft.fxtended.annotations.binding.BindableProperty
import net.bbmsoft.jgitfx.messaging.Messenger
import net.bbmsoft.jgitfx.modules.ChangedFilesAnimator
import net.bbmsoft.jgitfx.modules.CommitInfoAnimator
import net.bbmsoft.jgitfx.modules.GitTaskHelper
import net.bbmsoft.jgitfx.modules.Preferences
import net.bbmsoft.jgitfx.modules.RepositoryHandler
import net.bbmsoft.jgitfx.modules.RepositoryTableVisualizer
import net.bbmsoft.jgitfx.modules.StagingAnimator
import net.bbmsoft.jgitfx.wrappers.RepositoryWrapper
import net.bbmsoft.jgitfx.wrappers.RepositoryWrapper.DummyWrapper
import org.controlsfx.control.BreadCrumbBar
import org.controlsfx.control.TaskProgressView
import org.eclipse.jgit.diff.DiffEntry
import org.eclipse.jgit.diff.DiffEntry.ChangeType
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.revwalk.RevCommit
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import org.eclipse.xtend.lib.annotations.Accessors

import static extension net.bbmsoft.fxtended.extensions.BindingOperatorExtensions.*

@FXMLRoot
class JGitFXMainFrame extends BorderPane {

	@FXML TableView<RevCommit> historyTable
	@FXML TableColumn<RevCommit, String> refsColumn
	@FXML TableColumn<RevCommit, String> commitMessageColumn
	@FXML TableColumn<RevCommit, String> authorColumn
	@FXML TableColumn<RevCommit, String> timeColumn

	@FXML TableView<DiffEntry> changedFilesOverview
	@FXML TableColumn<DiffEntry, ChangeType> commitTypeColumn
	@FXML TableColumn<DiffEntry, String> commitFileColumn

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

	@FXML MenuItem undoContextMenuItem
	@FXML MenuItem redoContextMenuItem
	@FXML MenuItem pullContextMenuItem
	@FXML MenuItem pushContextMenuItem
	@FXML MenuItem branchContextMenuItem
	@FXML MenuItem stashContextMenuItem
	@FXML MenuItem popContextMenuItem

	@FXML TaskProgressView<Task<?>> tasksView
	@Accessors(PUBLIC_GETTER) TaskHelper taskHelper

	@BindableProperty Runnable cloneAction
	@BindableProperty Runnable batchCloneAction
	@BindableProperty Runnable initAction
	@BindableProperty Runnable openAction
	@BindableProperty Runnable quitAction
	@BindableProperty Runnable aboutAction

	@BindableProperty ObservableList<File> registeredRepositories
	Map<File, TreeItem<RepositoryWrapper>> repositoryMap

	InvalidationListener repositoriesListener
	InvalidationListener repositoryListener

	TreeItem<RepositoryWrapper> rootRepoTreeItem

	@BindableProperty RepositoryHandler repositoryHandler

	RepositoryTableVisualizer historyVisualizer

	Preferences prefs
	Lockable locker
	Messenger messenger

	new(Preferences prefs, ExecutorService gitWorker, Messenger messenger) {
		this()
		this.messenger = messenger
		this.prefs = prefs
		this.taskHelper = new GitTaskHelper(gitWorker, this.tasksView);
		this.locker = new Lockable() {

			override lock() {}

			override unlock() {}
		}
		updateHistoryColumnsVisibility
		this.historyTable.columns.forEach[col|col.visibleProperty >> [this.prefs.setColumnVisible(col.id, it)]]
	}

	private def updateHistoryColumnsVisibility() {
		this.historyTable.columns.forEach[visible = this.prefs.visibleColumns.contains(id)]
	}

	override initialize(URL location, ResourceBundle resources) {

		this.historyVisualizer = new RepositoryTableVisualizer(this.historyTable, this.refsColumn,
			this.commitMessageColumn, this.authorColumn, this.timeColumn)
		this.repositoryMap = new HashMap
		this.repositoriesListener = [updateRepoTree]
		this.repositoryListener = [updateRepo]
		this.registeredRepositories = FXCollections.observableArrayList
		this.registeredRepositoriesProperty >> [o, ov, nv|updateRepositoriesListener(ov, nv)]
		this.rootRepoTreeItem = new TreeItem(new DummyWrapper('JGitFX'))
		this.breadcrumb.selectedCrumb = this.rootRepoTreeItem
		this.repositoryTree.root = rootRepoTreeItem
		this.repositoryTree.showRoot = false
		this.repositoryTree.onMouseClicked = [
			if (clickCount == 2) {
				this.repositoryTree.selectionModel.selectedItem?.open
			}
		]

		this.repositoryTree.selectionModel.selectionMode = SelectionMode.MULTIPLE
		this.repositoryTree.selectionModel.selectedItems > [updateRepositoryTreeContextMenu]

		this.historyTable.selectionModel.selectedItemProperty.addListener(
			new CommitInfoAnimator(this.commitMessageLabel, this.authorLabel, this.emailLabel, this.timeLabel,
				this.hashLabel, this.parentHashLabel))
		this.historyTable.selectionModel.selectedItemProperty.addListener(
			new ChangedFilesAnimator(this.changedFilesOverview, this.commitTypeColumn, this.commitFileColumn) [
				this.repositoryHandler?.repository
			])
		this.historyTable.selectionModel.selectedItemProperty.addListener(new StagingAnimator())

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

		// TODO show error message when opening the repo fails
		val repository = builder.build

		this.repositoryMap.put(dir, new TreeItem(new RepositoryWrapper(repository)))
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
		new RepositoryHandler(repository, this.taskHelper, this.locker, listener, this.messenger)
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

	def boolean open(TreeItem<RepositoryWrapper> repoItem) {
		val repository = repoItem.value.repository
		this.repositoryHandler?.removeListener(this.repositoryListener)
		this.repositoryHandler = new RepositoryHandler(repository, this.taskHelper, this.locker,
			this.repositoryListener, this.messenger)
		this.breadcrumb.selectedCrumb = repoItem
		this.prefs.lastOpened = repository.directory
		if (prefs.switchToRepositoryOverview) {
			// delay so it also works on startup
			Platform.runLater[this.repositoryOverview.expanded = true]
		}
		true
	}

}
