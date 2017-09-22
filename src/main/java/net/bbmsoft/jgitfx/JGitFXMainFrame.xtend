package net.bbmsoft.jgitfx

import java.net.URL
import java.util.HashMap
import java.util.List
import java.util.Map
import java.util.ResourceBundle
import java.util.concurrent.ExecutorService
import javafx.application.Platform
import javafx.collections.ObservableList
import javafx.concurrent.Task
import javafx.fxml.FXML
import javafx.scene.Parent
import javafx.scene.control.Label
import javafx.scene.control.MenuItem
import javafx.scene.control.SelectionMode
import javafx.scene.control.TableColumn
import javafx.scene.control.TableView
import javafx.scene.control.TitledPane
import javafx.scene.control.TreeItem
import javafx.scene.control.TreeView
import javafx.scene.layout.BorderPane
import net.bbmsoft.fxtended.annotations.app.FXMLRoot
import net.bbmsoft.fxtended.annotations.binding.BindableProperty
import net.bbmsoft.jgitfx.event.EventBroker
import net.bbmsoft.jgitfx.event.UserInteraction
import net.bbmsoft.jgitfx.modules.ChangedFilesAnimator
import net.bbmsoft.jgitfx.modules.CommitInfoAnimator
import net.bbmsoft.jgitfx.modules.Preferences
import net.bbmsoft.jgitfx.modules.RepositoryHandler
import net.bbmsoft.jgitfx.modules.RepositoryTableVisualizer
import net.bbmsoft.jgitfx.modules.StagingAnimator
import net.bbmsoft.jgitfx.registry.RepositoryRegistry
import net.bbmsoft.jgitfx.wrappers.HistoryEntry
import net.bbmsoft.jgitfx.wrappers.RepositoryWrapper
import net.bbmsoft.jgitfx.wrappers.RepositoryWrapper.DummyWrapper
import org.controlsfx.control.BreadCrumbBar
import org.controlsfx.control.TaskProgressView
import org.eclipse.jgit.diff.DiffEntry
import org.eclipse.jgit.diff.DiffEntry.ChangeType
import org.eclipse.jgit.lib.Repository

import static extension net.bbmsoft.fxtended.extensions.BindingOperatorExtensions.*
import net.bbmsoft.jgitfx.event.RepositoryOperations

@FXMLRoot
class JGitFXMainFrame extends BorderPane {

	@FXML TableView<HistoryEntry> historyTable
	@FXML TableColumn<HistoryEntry, String> refsColumn
	@FXML TableColumn<HistoryEntry, String> commitMessageColumn
	@FXML TableColumn<HistoryEntry, String> authorColumn
	@FXML TableColumn<HistoryEntry, String> timeColumn

	@FXML Parent wipOverview
	@FXML TableView<DiffEntry> unstagedFilesTable
	@FXML TableColumn<DiffEntry, String> unstagedTypeColum
	@FXML TableColumn<DiffEntry, String> unstagedFileColum
	@FXML TableView<DiffEntry> stagedFilesTable
	@FXML TableColumn<DiffEntry, String> stagedTypeColum
	@FXML TableColumn<DiffEntry, String> stagedFileColum

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
	
	@BindableProperty RepositoryRegistry repositoryRegistry
	@BindableProperty RepositoryHandler repositoryHandler

	Map<Repository, TreeItem<RepositoryWrapper>> repositoryTreeItems
	TreeItem<RepositoryWrapper> rootRepoTreeItem
	RepositoryTableVisualizer historyVisualizer
	Preferences prefs
	EventBroker eventBroker

	new(Preferences prefs, ExecutorService gitWorker, EventBroker eventBroker) {
		this()
		this.prefs = prefs
		this.eventBroker = eventBroker
		updateHistoryColumnsVisibility
		this.historyTable.columns.forEach[col|col.visibleProperty >> [this.prefs.setColumnVisible(col.id, it)]]
	}

	def ObservableList<Task<?>> getTaskList() {
		this.tasksView.tasks
	}

	private def updateHistoryColumnsVisibility() {
		this.historyTable.columns.forEach[visible = this.prefs.visibleColumns.contains(id)]
	}

	override initialize(URL location, ResourceBundle resources) {

		this.historyVisualizer = new RepositoryTableVisualizer(this.historyTable, this.refsColumn,
			this.commitMessageColumn, this.authorColumn, this.timeColumn)
		this.repositoryTreeItems = new HashMap
		this.rootRepoTreeItem = new TreeItem(new DummyWrapper('JGitFX'))
		this.rootRepoTreeItem.children >> [updateTreeItemMap($0, $1)]
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
			new ChangedFilesAnimator(this.wipOverview, this.changedFilesOverview, this.commitTypeColumn,
				this.commitFileColumn) [
				this.repositoryHandler?.repository
			])

		val stagingAnimator = new StagingAnimator(this.unstagedFilesTable, this.unstagedTypeColum,
			this.unstagedFileColum, this.stagedFilesTable, this.stagedTypeColum, this.stagedFileColum)
		this.repositoryHandlerProperty >> stagingAnimator
		
		this.unstagedFilesTable.selectionModel.selectionMode = SelectionMode.MULTIPLE
		this.stagedFilesTable.selectionModel.selectionMode = SelectionMode.MULTIPLE

		Platform.runLater[this.repositoriesList.expanded = true]
	}

	private def updateTreeItemMap(List<? extends TreeItem<RepositoryWrapper>> added,
		List<? extends TreeItem<RepositoryWrapper>> removed) {
		added.forEach[this.repositoryTreeItems.put(value.repository, it)]
		removed.forEach[this.repositoryTreeItems.remove(value.repository)]
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

	def updateRepo(RepositoryHandler repoHandler) {
		// TODO
		println('''Updating view of «repoHandler?.repository»''')
		this.historyVisualizer.repository = repoHandler.repository
	}

	def getRepositoryTreeItems() {
		this.rootRepoTreeItem.children
	}

	def undo() {
		undo(this.repositoryHandler)
	}

	def redo() {
		this.repositoryHandler?.redo
	}

	def pull() {
		if(this.repositoryHandler !== null) {
			this.eventBroker.publish(RepositoryOperations.PULL, this.repositoryHandler)
		}
	}

	def push() {
		if(this.repositoryHandler !== null) {
			this.eventBroker.publish(RepositoryOperations.PUSH, this.repositoryHandler)
		}
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

	private def undo(RepositoryHandler repo) {
		if(repo !== null) {
			this.eventBroker.publish(RepositoryOperations.UNDO, repo)
		}
	}

	private def redo(RepositoryHandler repo) {
		if(repo !== null) {
			this.eventBroker.publish(RepositoryOperations.REDO, repo)
		}
	}

	private def pull(RepositoryHandler ... repos) {
		repos?.forEach[this.eventBroker.publish(RepositoryOperations.PULL, it)]
	}

	private def push(RepositoryHandler ... repos) {
		repos?.forEach[this.eventBroker.publish(RepositoryOperations.PUSH, it)]
	}

	private def branch(RepositoryHandler repo) {
		if(repo !== null) {
			this.eventBroker.publish(RepositoryOperations.BRANCH, repo)
		}
	}

	private def stash(RepositoryHandler repo) {
		if(repo !== null) {
			this.eventBroker.publish(RepositoryOperations.STASH, repo)
		}
	}

	private def pop(RepositoryHandler repo) {
		if(repo !== null) {
			this.eventBroker.publish(RepositoryOperations.POP, repo)
		}
	}

	private def RepositoryHandler getHandlerForSelected() {
		this.repositoryTree.selectionModel.selectedItem?.value?.repository?.getHandler
	}

	private def RepositoryHandler[] getHandlersForSelected() {
		this.repositoryTree.selectionModel.selectedItems.map[value?.repository?.getHandler]
	}

	private def RepositoryHandler getHandler(Repository repository) {
		this.repositoryRegistry.getRepositoryHandler(repository)
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
		this.eventBroker.publish(UserInteraction.CLONE, null)
	}

	def batchCloneRepo() {
		this.eventBroker.publish(UserInteraction.BATCH_CLONE, null)
	}

	def initRepo() {
		this.eventBroker.publish(UserInteraction.INIT_REPO, null)
	}

	def openRepo() {
		this.eventBroker.publish(UserInteraction.OPEN_REPO, null)
	}

	def quit() {
		this.eventBroker.publish(UserInteraction.QUIT, null)
	}

	def about() {
		this.eventBroker.publish(UserInteraction.SHOW_ABOUT, null)
	}

	def void open(Repository repository) {
		this.repositoryTreeItems.get(repository)?.open
	}

	private def void open(TreeItem<RepositoryWrapper> repoItem) {
		val repository = repoItem.value.repository
		this.repositoryHandler?.setAutoInvalidate(false)
		this.repositoryHandler = getHandler(repository)
		this.repositoryHandler.setAutoInvalidate(true)
		this.breadcrumb.selectedCrumb = repoItem
		this.prefs.lastOpened = repository.directory
		if (prefs.switchToRepositoryOverview) {
			// delay so it also works on startup
			Platform.runLater[this.repositoryOverview.expanded = true]
		}
	}

	def void stageAll() {
		println("stage " + this.unstagedFilesTable.items)
	}
	
	def void unstageAll() {
		println("unstage " + this.stagedFilesTable.items)
	}
	
	def void stageSelected() {
		println("stage " + this.unstagedFilesTable.selectionModel.selectedItems)
	}
	
	def void discardSelectedUnstaged() {
		println("discard " + this.unstagedFilesTable.selectionModel.selectedItems)
	}
	
	def void unstageSelected() {
		println("unstage " + this.stagedFilesTable.selectionModel.selectedItems)
	}
	
	def void discardSelectedStaged() {
		println("discard " + this.stagedFilesTable.selectionModel.selectedItems)
	}
	
}
