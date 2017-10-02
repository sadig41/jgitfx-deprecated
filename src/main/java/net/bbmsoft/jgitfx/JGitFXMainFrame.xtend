package net.bbmsoft.jgitfx

import java.net.URL
import java.util.HashMap
import java.util.List
import java.util.Map
import java.util.ResourceBundle
import javafx.application.Platform
import javafx.concurrent.Task
import javafx.fxml.FXML
import javafx.scene.Parent
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.MenuItem
import javafx.scene.control.SelectionMode
import javafx.scene.control.TableColumn
import javafx.scene.control.TableView
import javafx.scene.control.TextArea
import javafx.scene.control.TextField
import javafx.scene.control.TitledPane
import javafx.scene.control.TreeItem
import javafx.scene.control.TreeView
import javafx.scene.layout.BorderPane
import javafx.scene.layout.Pane
import javax.inject.Inject
import net.bbmsoft.bbm.utils.concurrent.TaskHelper
import net.bbmsoft.fxtended.annotations.app.FXMLRoot
import net.bbmsoft.fxtended.annotations.binding.BindableProperty
import net.bbmsoft.jgitfx.event.AppStatus
import net.bbmsoft.jgitfx.event.CommitMessageTopic
import net.bbmsoft.jgitfx.event.EventBroker
import net.bbmsoft.jgitfx.event.RepositoryOperations
import net.bbmsoft.jgitfx.event.RepositoryTopic
import net.bbmsoft.jgitfx.event.UserInteraction
import net.bbmsoft.jgitfx.messaging.Message
import net.bbmsoft.jgitfx.messaging.MessageType
import net.bbmsoft.jgitfx.modules.ChangedFilesAnimator
import net.bbmsoft.jgitfx.modules.CommitInfoAnimator
import net.bbmsoft.jgitfx.modules.DiffAnimator
import net.bbmsoft.jgitfx.modules.DiffTextFormatter
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

	@FXML TextField commitMessageTextField
	@FXML TextArea commitMessageTextArea
	@FXML Button commitButton

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

	@FXML Pane diffTextContainer

	@BindableProperty RepositoryHandler repositoryHandler

	RepositoryRegistry repositoryRegistry
	Map<String, TreeItem<RepositoryWrapper>> repositoryTreeItems
	TreeItem<RepositoryWrapper> rootRepoTreeItem
	RepositoryTableVisualizer historyVisualizer
	Preferences prefs
	EventBroker eventBroker

	StagingAnimator stagingAnimator

	@Inject
	private new(Preferences prefs, EventBroker eventBroker, RepositoryRegistry repoRegistry, TaskHelper taskHelper) {

		this()
		this.prefs = prefs
		this.eventBroker = eventBroker
		this.repositoryRegistry = repoRegistry
		this.historyTable.columns.forEach[col|col.visibleProperty >> [this.prefs.setColumnVisible(col.id, it)]]
		this.stagingAnimator = new StagingAnimator(this.unstagedFilesTable, this.unstagedTypeColum,
			this.unstagedFileColum, this.stagedFilesTable, this.stagedTypeColum, this.stagedFileColum, this.eventBroker)
		this.historyVisualizer = new RepositoryTableVisualizer(this.historyTable, this.refsColumn,
			this.commitMessageColumn, this.authorColumn, this.timeColumn, this.eventBroker)

		this.repositoryHandlerProperty >> stagingAnimator

		this.eventBroker.subscribe(AppStatus.STARTED) [
			this.repositoryRegistry.registeredRepositories.forEach[addRepoTreeItem]
			val lastOpened = prefs.lastOpened
			if (lastOpened !== null) {
				val lastOpenedHandler = repoRegistry.getRepositoryHandler(lastOpened)
				if (lastOpenedHandler !== null) {
					open(lastOpenedHandler.repository)
				}
			}
		]
		this.eventBroker.subscribe(#[RepositoryOperations.STAGE, RepositoryOperations.UNSTAGE]) [
			if($1 == repositoryHandler) commitMessageUpdated(this.commitMessageTextField.text)
		]
		this.eventBroker.subscribe(RepositoryTopic.REPO_LOADED) [
			addRepoTreeItem($1.repository)
			open($1.repository)
		]
		this.eventBroker.subscribe(RepositoryTopic.REPO_OPENED) [
			this.historyVisualizer.repository = $1.repository
			if (prefs.switchToRepositoryOverview) {
				// delay so it also works on startup
				Platform.runLater[this.repositoryOverview.expanded = true]
			}
		]

		this.tasksView.tasks >> [
			val tasksRunning = !this.tasksView.tasks.empty
			this.tasksView.visible = tasksRunning
			this.tasksView.managed = tasksRunning
		]

		this.historyTable.selectionModel.selectedItemProperty.addListener(
			new CommitInfoAnimator(this.commitMessageLabel, this.authorLabel, this.emailLabel, this.timeLabel,
				this.hashLabel, this.parentHashLabel))
		this.historyTable.selectionModel.selectedItemProperty.addListener(
			new ChangedFilesAnimator(this.wipOverview, this.changedFilesOverview, this.commitTypeColumn,
				this.commitFileColumn, [
					this.repositoryHandler?.repository
				], this.eventBroker))

		new DiffTextFormatter(this.diffTextContainer.children) => [
			new DiffAnimator(outputStream, it, it, this.eventBroker)
		]

		taskHelper.taskList = this.tasksView.tasks

		updateHistoryColumnsVisibility
	}

	private def addRepoTreeItem(Repository repository) {
		val treeItem = new TreeItem(new RepositoryWrapper(repository))
		this.rootRepoTreeItem.children.add(treeItem)
	}

	private def updateHistoryColumnsVisibility() {
		this.historyTable.columns.forEach[visible = this.prefs.visibleColumns.contains(id)]
	}

	override initialize(URL location, ResourceBundle resources) {

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

		this.unstagedFilesTable.selectionModel.selectionMode = SelectionMode.MULTIPLE
		this.stagedFilesTable.selectionModel.selectionMode = SelectionMode.MULTIPLE

		this.commitMessageTextField.textProperty >> [commitMessageUpdated]

		Platform.runLater[this.repositoriesList.expanded = true]
	}

	private def void commitMessageUpdated(String commitMessage) {
		this.commitButton.disable = commitMessage === null || commitMessage.trim.empty ||
			!this.stagingAnimator.hasStagedChanges
		Platform.runLater[this.eventBroker.publish(CommitMessageTopic.COMMIT_MESSAGE_UPDATED, commitMessage)]
	}

	private def updateTreeItemMap(List<? extends TreeItem<RepositoryWrapper>> added,
		List<? extends TreeItem<RepositoryWrapper>> removed) {
		added.forEach[this.repositoryTreeItems.put(value.repository.directory.absolutePath, it)]
		removed.forEach[this.repositoryTreeItems.remove(value.repository.directory.absolutePath)]
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

	def undo() {
		undo(this.repositoryHandler)
	}

	def redo() {
		this.repositoryHandler?.redo
	}

	def pull() {
		if (this.repositoryHandler !== null) {
			this.eventBroker.publish(RepositoryOperations.PULL, this.repositoryHandler)
		}
	}

	def push() {
		if (this.repositoryHandler !== null) {
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
		if (repo !== null) {
			this.eventBroker.publish(RepositoryOperations.UNDO, repo)
		}
	}

	private def redo(RepositoryHandler repo) {
		if (repo !== null) {
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
		if (repo !== null) {
			this.eventBroker.publish(RepositoryOperations.BRANCH, repo)
		}
	}

	private def stash(RepositoryHandler repo) {
		if (repo !== null) {
			this.eventBroker.publish(RepositoryOperations.STASH, repo)
		}
	}

	private def pop(RepositoryHandler repo) {
		if (repo !== null) {
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
		this.eventBroker.publish(UserInteraction.ADD_REPO, null)
	}

	def quit() {
		this.eventBroker.publish(UserInteraction.QUIT, null)
	}

	def about() {
		this.eventBroker.publish(UserInteraction.SHOW_ABOUT, null)
		this.scene.stylesheets.all = #['style/default.css']
	}

	def void open(Repository repository) {
		this.repositoryTreeItems.get(repository.directory.absolutePath)?.open
	}

	private def void open(TreeItem<RepositoryWrapper> repoItem) {
		try {
			val repository = repoItem.value.repository
			this.repositoryHandler?.setAutoInvalidate(false)
			this.repositoryHandler = getHandler(repository)
			this.repositoryHandler.setAutoInvalidate(true)
			this.breadcrumb.selectedCrumb = repoItem
			this.prefs.lastOpened = repository.directory
			this.eventBroker.publish(RepositoryTopic.REPO_OPENED, this.repositoryHandler)
		} catch (Throwable th) {
			val title = 'Failed to open repository'
			val body = '''An error occurded while opening the repository:  «IF th.message !== null»«th.message»«ELSE»«th.class.simpleName»«ENDIF»'''
			this.eventBroker.publish(MessageType.ERROR, new Message(title, body, th))
		}
	}

	def void commit() {
		if (this.repositoryHandler !== null) {
			RepositoryOperations.COMMIT.message = commitMessage
			// TODO some more validation
			this.commitMessageTextField.text = null
			this.commitMessageTextArea.text = null
			this.eventBroker.publish(RepositoryOperations.COMMIT, this.repositoryHandler)
		}
	}

	private def String getCommitMessage() {
		val details = this.commitMessageTextArea.text?.trim ?: ''
		'''«this.commitMessageTextField.text.trim»«IF !details.empty»
		
		«details»«ENDIF»'''
	}

	def void stageAll() {
		this.unstagedFilesTable.items.stage
	}

	def void unstageAll() {
		this.stagedFilesTable.items.unstage
	}

	def void stageSelected() {
		this.unstagedFilesTable.selectionModel.selectedItems.stage
	}

	def void discardSelectedUnstaged() {
		this.unstagedFilesTable.selectionModel.selectedItems.discard
	}

	def void unstageSelected() {
		this.stagedFilesTable.selectionModel.selectedItems.unstage
	}

	def void discardSelectedStaged() {
		this.stagedFilesTable.selectionModel.selectedItems.discard
	}

	private def stage(DiffEntry ... files) {
		RepositoryOperations.STAGE.diffs = files
		this.eventBroker.publish(RepositoryOperations.STAGE, this.repositoryHandler)
	}

	private def unstage(DiffEntry ... files) {
		RepositoryOperations.UNSTAGE.diffs = files
		this.eventBroker.publish(RepositoryOperations.UNSTAGE, this.repositoryHandler)
	}

	private def discard(DiffEntry ... files) {
		println("discard " + files)
	}

}
