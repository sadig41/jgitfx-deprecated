package net.bbmsoft.jgitfx

import java.io.File
import java.net.URL
import java.util.ArrayList
import java.util.Arrays
import java.util.HashMap
import java.util.List
import java.util.Map
import java.util.ResourceBundle
import javafx.application.Platform
import javafx.concurrent.Task
import javafx.fxml.FXML
import javafx.scene.Parent
import javafx.scene.control.Alert
import javafx.scene.control.Alert.AlertType
import javafx.scene.control.Label
import javafx.scene.control.ListView
import javafx.scene.control.MenuItem
import javafx.scene.control.SelectionMode
import javafx.scene.control.SplitMenuButton
import javafx.scene.control.TableColumn
import javafx.scene.control.TableView
import javafx.scene.control.TextArea
import javafx.scene.control.TextField
import javafx.scene.control.TitledPane
import javafx.scene.control.TreeItem
import javafx.scene.control.TreeView
import javafx.scene.image.ImageView
import javafx.scene.input.ClipboardContent
import javafx.scene.input.DragEvent
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination
import javafx.scene.input.KeyCombination
import javafx.scene.input.KeyEvent
import javafx.scene.input.MouseEvent
import javafx.scene.input.TransferMode
import javafx.scene.layout.BorderPane
import javafx.scene.layout.Pane
import javax.inject.Inject
import net.bbmsoft.bbm.utils.concurrent.TaskHelper
import net.bbmsoft.fxtended.annotations.binding.BindableProperty
import net.bbmsoft.fxtended.annotations.layout.FXMLRoot
import net.bbmsoft.jgitfx.event.AppStatus
import net.bbmsoft.jgitfx.event.CommitMessageTopic
import net.bbmsoft.jgitfx.event.DiffTopic
import net.bbmsoft.jgitfx.event.EventBroker
import net.bbmsoft.jgitfx.event.RepositoryOperations
import net.bbmsoft.jgitfx.event.RepositoryTopic
import net.bbmsoft.jgitfx.event.TaskTopic
import net.bbmsoft.jgitfx.event.Topic
import net.bbmsoft.jgitfx.event.UserInteraction
import net.bbmsoft.jgitfx.messaging.Message
import net.bbmsoft.jgitfx.messaging.MessageType
import net.bbmsoft.jgitfx.modules.ChangedFilesAnimator
import net.bbmsoft.jgitfx.modules.CommitInfoAnimator
import net.bbmsoft.jgitfx.modules.DiffAnimator
import net.bbmsoft.jgitfx.modules.DiffTextFormatter
import net.bbmsoft.jgitfx.modules.Preferences
import net.bbmsoft.jgitfx.modules.RepositoryHandler
import net.bbmsoft.jgitfx.modules.RepositoryStatusMonitor
import net.bbmsoft.jgitfx.modules.RepositoryTableVisualizer
import net.bbmsoft.jgitfx.modules.StagingAnimator
import net.bbmsoft.jgitfx.registry.RepositoryRegistry
import net.bbmsoft.jgitfx.utils.RepoHelper
import net.bbmsoft.jgitfx.utils.StagingHelper
import net.bbmsoft.jgitfx.wrappers.HistoryEntry
import net.bbmsoft.jgitfx.wrappers.RepoTreeCellFactory
import net.bbmsoft.jgitfx.wrappers.RepositoryWrapper
import net.bbmsoft.jgitfx.wrappers.RepositoryWrapper.DummyWrapper
import org.controlsfx.control.BreadCrumbBar
import org.controlsfx.control.BreadCrumbBar.BreadCrumbActionEvent
import org.controlsfx.control.TaskProgressView
import org.eclipse.jgit.diff.DiffEntry
import org.eclipse.jgit.lib.Repository

import static extension net.bbmsoft.fxtended.extensions.BindingOperatorExtensions.*

@FXMLRoot
class JGitFXMainFrame extends BorderPane {

	private static final KeyCombination CONTROL_ENTER = new KeyCodeCombination(KeyCode.ENTER,
		KeyCombination.CONTROL_DOWN)

	private static final KeyCombination CONTROL_SHIFT_ENTER = new KeyCodeCombination(KeyCode.ENTER,
		KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN)

	@FXML TableView<HistoryEntry> historyTable
	@FXML TableColumn<HistoryEntry, String> refsColumn
	@FXML TableColumn<HistoryEntry, String> commitMessageColumn
	@FXML TableColumn<HistoryEntry, String> authorColumn
	@FXML TableColumn<HistoryEntry, String> timeColumn

	@FXML Parent wipOverview
	@FXML ListView<DiffEntry> unstagedFilesTable
	@FXML ListView<DiffEntry> stagedFilesTable
	@FXML ListView<DiffEntry> changedFilesOverview

	@FXML TreeView<RepositoryWrapper> repositoryTree

	@FXML TextField commitMessageTextField
	@FXML TextArea commitMessageTextArea
	@FXML SplitMenuButton commitButton
	@FXML MenuItem commitMenuItem
	@FXML MenuItem commitAndPushMenuItem

	@FXML TitledPane repositoryOverview
	@FXML TitledPane repositoriesList

	@FXML BreadCrumbBar<RepositoryWrapper> breadcrumb

	@FXML Label commitMessageLabel
	@FXML Label authorLabel
	@FXML Label emailLabel
	@FXML Label timeLabel
	@FXML Label hashLabel
	@FXML Label parentHashLabel

	@FXML MenuItem pullContextMenuItem
	@FXML MenuItem pushContextMenuItem
	@FXML MenuItem branchContextMenuItem
	@FXML MenuItem mergeContextMenuItem
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

	List<DiffEntry> dragDropBuffer

	@Inject
	private new(Preferences prefs, EventBroker eventBroker, RepositoryRegistry repoRegistry, TaskHelper taskHelper) {

		this()
		this.prefs = prefs
		this.eventBroker = eventBroker
		this.repositoryRegistry = repoRegistry
		this.stagingAnimator = new StagingAnimator(this.unstagedFilesTable, this.stagedFilesTable, this.eventBroker)
		this.historyVisualizer = new RepositoryTableVisualizer(this.historyTable, this.refsColumn,
			this.commitMessageColumn, this.authorColumn, this.timeColumn, this.eventBroker)

		this.eventBroker.subscribe(AppStatus.STARTED) [ Topic<Long> topic, Long value |
			this.repositoryRegistry.registeredRepositories.forEach[addRepoTreeItem]
			val lastOpened = prefs.lastOpened
			if (lastOpened !== null) {
				val lastOpenedHandler = repoRegistry.getRepositoryHandler(lastOpened)
				if (lastOpenedHandler !== null) {
					this.eventBroker.publish(RepositoryTopic.REPO_OPENED, lastOpenedHandler)
				}
			}
		]
		this.eventBroker.subscribe(#[RepositoryOperations.STAGE, RepositoryOperations.UNSTAGE]) [ Topic<RepositoryHandler> topic, RepositoryHandler handler |
			if(handler == repositoryHandler) commitMessageUpdated(this.commitMessageTextField.text)
		]
		this.eventBroker.subscribe(RepositoryTopic.REPO_LOADED) [ Topic<RepositoryHandler> topic, RepositoryHandler handler |
			addRepoTreeItem(handler.repository)
		]
		this.eventBroker.subscribe(RepositoryTopic.REPO_OPENED) [ Topic<RepositoryHandler> topic, RepositoryHandler handler |
			this.repositoryTreeItems.get(handler?.repository?.directory?.absolutePath).open
		]
		this.eventBroker.subscribe(RepositoryTopic.REPO_REMOVED) [ Topic<RepositoryHandler> topic, RepositoryHandler handler |
			val treeItem = this.repositoryTreeItems.remove(handler.repository.directory.absolutePath)
			this.rootRepoTreeItem.children.remove(treeItem)
			if (handler == this.repositoryHandler) {
				Platform.runLater[this.eventBroker.publish(RepositoryTopic.REPO_OPENED, null)]
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
			new ChangedFilesAnimator(this.wipOverview, this.changedFilesOverview, [
				this.repositoryHandler?.repository
			], this.eventBroker))

		new DiffTextFormatter(this.diffTextContainer.children) => [
			new DiffAnimator(this.eventBroker, outputStream)
		]

		new RepositoryStatusMonitor(eventBroker)[getWrapper]

		taskHelper.taskList = this.tasksView.tasks

		updateHistoryColumnsVisibility
		this.historyTable.columns.forEach[col|col.visibleProperty >> [this.prefs.setColumnVisible(col.id, it)]]

		this.prefs.commitAndPushProperty >> [this.updateCommitButton(it)]
	}

	private def updateCommitButton(boolean commitAndPush) {
		this.commitButton.items.all = #[if(commitAndPush) this.commitMenuItem else this.commitAndPushMenuItem]
		this.commitButton.text = '''Commit«IF commitAndPush» + Push«ENDIF»'''
	}

	private def RepositoryWrapper getWrapper(Repository repo) {
		this.repositoryTreeItems.get(repo.directory.absolutePath)?.value
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

		this.repositoryTree.selectionModel.selectionMode = SelectionMode.MULTIPLE
		this.repositoryTree.selectionModel.selectedItems > [updateRepositoryTreeContextMenu]

		this.unstagedFilesTable.selectionModel.selectionMode = SelectionMode.MULTIPLE
		this.stagedFilesTable.selectionModel.selectionMode = SelectionMode.MULTIPLE

		this.commitMessageTextField.textProperty >> [commitMessageUpdated]

		this.repositoryTree.cellFactory = new RepoTreeCellFactory

		Platform.runLater[this.repositoriesList.expanded = true]
	}
	
	def clicked(MouseEvent e) {
		if(e.source == this.unstagedFilesTable && e.clickCount == 2) {
			this.unstagedFilesTable.selectionModel.selectedItems.stage
		}
		if(e.source == this.stagedFilesTable && e.clickCount == 2) {
			this.stagedFilesTable.selectionModel.selectedItems.unstage
		}
		if(e.source == this.repositoryTree) {
			val selected = this.repositoryTree.selectionModel.selectedItem
			if (e.clickCount == 2 && selected !== null) {
				this.eventBroker.publish(RepositoryTopic.REPO_OPENED, getHandler(selected.value.repository))
			}
		}
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

		this.pullContextMenuItem.disable = size <= 0
		this.pushContextMenuItem.disable = size <= 0
//		this.branchContextMenuItem.disable = size <= 0
//		this.mergeContextMenuItem.disable = size <= 0
//		this.stashContextMenuItem.disable = size <= 0
//		this.popContextMenuItem.disable = size <= 0
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

		new Alert(AlertType.INFORMATION) => [
			title = 'About'
			headerText = 'JGitFX v0.0.1 (alpha)'
			contentText = 'A JGit based graphical git client built with JavaFX'
			graphic = new ImageView(JGitFXMainFrame.getResource('/logo/jgitfx-full-128.png').toExternalForm)
			initOwner(scene.window)
			showAndWait
		]
	}

	private def void open(TreeItem<RepositoryWrapper> repoItem) {
		try {
			val repository = repoItem?.value?.repository
			this.repositoryHandler?.setAutoInvalidate(false)
			this.repositoryHandler = if(repository !== null) getHandler(repository)
			this.repositoryHandler?.setAutoInvalidate(true)
			this.breadcrumb.selectedCrumb = repoItem ?: this.rootRepoTreeItem
			this.prefs.lastOpened = repository?.directory
			Platform.runLater [
				if (repository !== null && prefs.switchToRepositoryOverview) {
					this.repositoryOverview.expanded = true
				} else {
					this.repositoriesList.expanded = true
				}
			]
		} catch (Throwable th) {
			val title = 'Failed to open repository'
			val body = '''An error occurded while opening the repository:  «IF th.message !== null»«th.message»«ELSE»«th.class.simpleName»«ENDIF»'''
			this.eventBroker.publish(MessageType.ERROR, new Message(title, body, th))
		}
	}

	def void commit() {
		this.doCommit(false)
	}

	def commitAndPush() {
		this.doCommit(true)
	}

	private def doCommit(boolean andPush) {

		val commitMessage = commitMessage
		if (commitMessage === null || commitMessage.trim.empty || this.stagedFilesTable.items.empty) {
			return
		}

		if (this.repositoryHandler !== null) {
			RepositoryOperations.COMMIT.message = commitMessage
			// TODO some more validation
			this.commitMessageTextField.text = null
			this.commitMessageTextArea.text = null
			this.eventBroker.publish(RepositoryOperations.COMMIT, this.repositoryHandler)
		}

		if (andPush) {
			this.eventBroker.publish(RepositoryOperations.PUSH, this.repositoryHandler)
		}
	}

	private def String getCommitMessage() {
		val details = this.commitMessageTextArea.text?.trim ?: ''
		'''«this.commitMessageTextField.text?.trim ?: ''»«IF !details.empty»
		
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
		this.unstagedFilesTable.selectionModel.selectedItems.discard(false)
	}

	def void unstageSelected() {
		this.stagedFilesTable.selectionModel.selectedItems.unstage
	}

	def void discardSelectedStaged() {
		this.stagedFilesTable.selectionModel.selectedItems.discard(true)
	}

	private def stage(DiffEntry ... files) {
		RepositoryOperations.STAGE.diffs = files
		this.eventBroker.publish(RepositoryOperations.STAGE, this.repositoryHandler)
	}

	private def unstage(DiffEntry ... files) {
		RepositoryOperations.UNSTAGE.diffs = files
		this.eventBroker.publish(RepositoryOperations.UNSTAGE, this.repositoryHandler)
	}

	private def discard(DiffEntry[] files, boolean staged) {
		this.eventBroker.publish(if(staged) DiffTopic.DISCARD_STAGED else DiffTopic.DISCARD_UNSTAGED, Pair.of(this.repositoryHandler?.repository, Arrays.asList(files)))
	}

	def commitMessageEntered() {
		this.commitMessageTextArea.requestFocus
	}

	def void dragDetected(MouseEvent event) {

		if (event.source == this.unstagedFilesTable) {
			dragOnUnstagedFilesDeteced(event)
			return
		}

		if (event.source == this.stagedFilesTable) {
			dragOnStagedFilesDeteced(event)
			return
		}
	}

	private def dragOnUnstagedFilesDeteced(MouseEvent event) {

		event.consume

		if (this.unstagedFilesTable.selectionModel.selectedItems.empty) {
			return
		}

		this.unstagedFilesTable.startDragAndDrop(TransferMode.MOVE).content = new ClipboardContent => [
			this.dragDropBuffer = new ArrayList(this.unstagedFilesTable.selectionModel.selectedItems)
			putString(this.dragDropBuffer?.map[StagingHelper.getFilePath(it)].reduce['''«$0»,«$1»'''])
		]
	}

	private def dragOnStagedFilesDeteced(MouseEvent event) {

		event.consume

		if (this.stagedFilesTable.selectionModel.selectedItems.empty) {
			return
		}

		this.stagedFilesTable.startDragAndDrop(TransferMode.MOVE).content = new ClipboardContent => [
			this.dragDropBuffer = new ArrayList(this.stagedFilesTable.selectionModel.selectedItems)
			putString(this.dragDropBuffer?.map[StagingHelper.getFilePath(it)].reduce['''«$0»,«$1»'''])
		]
	}

	def void dragOver(DragEvent event) {

		if (event.source == this.repositoryTree) {
			dragOverRepoTree(event)
			return
		}

		if (event.source == this.stagedFilesTable || event.source == this.unstagedFilesTable) {
			dragOverStagedOrdUnstagedFiles(event)
			return
		}
	}

	private def dragOverRepoTree(DragEvent e) {
		if (e.gestureSource != this.repositoryTree && e.dragboard.hasFiles) {
			e.acceptTransferModes(TransferMode.COPY)
			e.consume
		}
	}

	private def dragOverStagedOrdUnstagedFiles(DragEvent e) {
		if (e.gestureSource != e.source &&
			e.dragboard.string == this.dragDropBuffer?.map[StagingHelper.getFilePath(it)].reduce['''«$0»,«$1»''']) {
			e.acceptTransferModes(TransferMode.MOVE)
			e.consume
		}
	}

	def void dragDropped(DragEvent event) {

		if (event.source == this.repositoryTree) {
			dragDroppedOnRepoTree(event)
		}

		if (event.source == this.stagedFilesTable) {
			dragDroppedOnStagedFiles(event)
		}

		if (event.source == this.unstagedFilesTable) {
			dragDroppedOnUnstagedFiles(event)
		}

	}

	private def dragDroppedOnUnstagedFiles(DragEvent event) {

		event.consume

		val db = event.dragboard
		if (db.string != this.dragDropBuffer?.map[StagingHelper.getFilePath(it)].reduce['''«$0»,«$1»''']) {
			return
		}

		val diffs = new ArrayList(this.dragDropBuffer)
		this.dragDropBuffer = null
		event.setDropCompleted(diffs !== null && !diffs.empty)

		diffs.unstage

	}

	private def dragDroppedOnStagedFiles(DragEvent event) {

		event.consume

		val db = event.dragboard
		if (db.string != this.dragDropBuffer?.map[StagingHelper.getFilePath(it)].reduce['''«$0»,«$1»''']) {
			return
		}

		val diffs = new ArrayList(this.dragDropBuffer)
		this.dragDropBuffer = null
		event.setDropCompleted(diffs !== null && !diffs.empty)

		diffs.stage

	}

	private def void dragDroppedOnRepoTree(DragEvent event) {

		val db = event.dragboard
		val files = if (db.hasFiles) {
				db.files
			}
		event.setDropCompleted(files !== null)
		event.consume

		val Task<?> task = new OpenReposTask(files, this.repositoryRegistry)
		this.eventBroker.publish(TaskTopic.TASK_STARTED, task)
	}

	def keyPressed(KeyEvent e) {

		if (e.source == this.commitMessageTextField || e.source == this.commitMessageTextArea) {
			if (CONTROL_SHIFT_ENTER.match(e)) {
				commitAndPush
				e.consume
				if(e.source == this.commitMessageTextArea) this.commitMessageTextField.requestFocus
			} else if (CONTROL_ENTER.match(e)) {
				commit
				e.consume
				if(e.source == this.commitMessageTextArea) this.commitMessageTextField.requestFocus
			}
		}

		if (e.source == this.repositoryTree && e.code == KeyCode.DELETE) {
			removeSelectedRepos
			e.consume
		}
		
		if(e.source == this.unstagedFilesTable && e.code == KeyCode.ENTER) {
			this.unstagedFilesTable.selectionModel.selectedItems.stage
		}
		
		if(e.source == this.stagedFilesTable && e.code == KeyCode.ENTER) {
			this.stagedFilesTable.selectionModel.selectedItems.unstage
		}
	}

	private def removeSelectedRepos() {
		new ArrayList(this.repositoryTree.selectionModel.selectedItems).forEach[removeRepo]
	}

	private def removeRepo(TreeItem<RepositoryWrapper> item) {
		val repository = item.value.repository
		this.repositoryRegistry.removeRepository(repository)
	}

	def breadCrumbAction(BreadCrumbActionEvent<RepositoryWrapper> event) {

		val repo = event.selectedCrumb.value.repository

		if (!RepoHelper.equal(repo, this.repositoryHandler?.repository)) {
			this.eventBroker.publish(RepositoryTopic.REPO_OPENED, repo?.handler)
		}
	}

	static class OpenReposTask extends Task<Void> {

		final List<File> repoDirs
		final RepositoryRegistry registry

		new(List<File> repoDirs, RepositoryRegistry registry) {
			this.repoDirs = repoDirs
			this.registry = registry
			updateTitle('''Adding «IF repoDirs.size > 1»«repoDirs.size» repositories«ELSE»repository«ENDIF»''')
		}

		override protected call() throws Exception {
			repoDirs?.forEach [ it, i |
				updateMessage('''Loading «absolutePath»''')
				registry.registerRepository(it, false)
				updateProgress(i, repoDirs.size)
			]
			null
		}

	}

}
