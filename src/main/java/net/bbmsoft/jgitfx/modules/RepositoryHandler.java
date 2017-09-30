package net.bbmsoft.jgitfx.modules;

import java.util.List;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.events.ConfigChangedEvent;
import org.eclipse.jgit.events.IndexChangedEvent;
import org.eclipse.jgit.events.RefsChangedEvent;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.CredentialsProvider;

import net.bbmsoft.jgitfx.event.EventBroker;
import net.bbmsoft.jgitfx.event.EventBroker.Topic;
import net.bbmsoft.jgitfx.event.EventPublisher;
import net.bbmsoft.jgitfx.event.RepositoryOperations;
import net.bbmsoft.jgitfx.event.RepositoryTopic;
import net.bbmsoft.jgitfx.modules.operations.CommitHandler;
import net.bbmsoft.jgitfx.modules.operations.PullHandler;
import net.bbmsoft.jgitfx.modules.operations.PushHandler;
import net.bbmsoft.jgitfx.modules.operations.StageHandler;

public class RepositoryHandler {

	private final Repository repository;
	private final Git git;

	private final PullHandler pullHandler;
	private final PushHandler pushHandler;
	private final CommitHandler commitHandler;
	private final StageHandler stageHandler;

	private final CredentialsProvider credentialsProvider;

	private boolean autoInvalidate;

	private EventPublisher eventPublisher;

	public RepositoryHandler(Repository repository, EventBroker eventBroker) {
		this.eventPublisher = eventBroker;
		this.repository = repository;
		this.git = Git.wrap(repository);
		this.pullHandler = new PullHandler(eventBroker);
		this.pushHandler = new PushHandler(eventBroker);
		this.commitHandler = new CommitHandler(eventBroker);
		this.stageHandler = new StageHandler();
		// TODO provide proper credentials provider
		this.credentialsProvider = new DialogUsernamePasswordProvider();
		eventBroker.subscribe(RepositoryOperations.values(), (topic, repo) -> evaluateRepositoryOperation(topic, repo));
		this.repository.getListenerList().addRefsChangedListener(this::refsChanged);
		this.repository.getListenerList().addIndexChangedListener(this::indexChanged);
		this.repository.getListenerList().addConfigChangedListener(this::configChanged);
		this.invalidate();
	}
	
	private void refsChanged(RefsChangedEvent e) {
		// TODO add more specific ref change handling
		this.invalidate();
	}
	
	private void indexChanged(IndexChangedEvent e) {
		// TODO add more specific index change handling
		this.invalidate();
	}
	
	private void configChanged(ConfigChangedEvent e) {
		// TODO add more specific config change handling
		this.invalidate();
	}

	private void evaluateRepositoryOperation(Topic<RepositoryHandler> topic, RepositoryHandler repo) {
		
		if (repo == this) {
			RepositoryOperations operation = (RepositoryOperations) topic;
			switch (operation) {
			case BRANCH:
				this.branch(operation.getMessage());
				break;
			case FETCH:
				this.fetch(operation.getMessage());
				break;
			case POP:
				this.pop();
				break;
			case PULL:
				this.pull();
				break;
			case PUSH:
				this.push();
				break;
			case REDO:
				this.redo();
				break;
			case STASH:
				this.stash(operation.getMessage());
				break;
			case UNDO:
				this.undo();
				break;
			case COMMIT:
				this.commit(operation.getMessage());
				break;
			case STAGE:
				this.stage(operation.getDiffs());
				break;
			case UNSTAGE:
				this.unstage(operation.getDiffs());
				break;
			default:
				throw new IllegalArgumentException("Unknown operation: " + topic);
			}
		}
	}

	private void stage(List<DiffEntry> diffs) {
		this.stageHandler.stage(git, diffs);
		this.invalidate();
	}

	private void unstage(List<DiffEntry> diffs) {
		this.stageHandler.unstage(git, diffs);
		this.invalidate();
	}

	public void invalidate() {
		if (this.autoInvalidate) {
			eventPublisher.publish(RepositoryTopic.REPO_UPDATED, this);
		}
	}

	private void commit(String message) {
		this.commitHandler.commit(git, message);
		this.invalidate();
	}

	private void undo() {
		System.out.println("Performing 'undo' on " + repository);
		this.invalidate();
	}

	private void fetch(String branch) {
		System.out.println("Performing 'undo' on " + repository);
		this.invalidate();
	}

	private void redo() {
		System.out.println("Performing 'redo' on " + repository);
		this.invalidate();
	}

	private void pull() {
		this.pullHandler.pull(git, Constants.DEFAULT_REMOTE_NAME, this.credentialsProvider);
	}

	private void push() {
		this.pushHandler.push(git, Constants.DEFAULT_REMOTE_NAME, this.credentialsProvider);
	}

	private void branch(String name) {
		System.out.println("Performing 'branch' on " + repository);
		this.invalidate();
	}

	private void stash(String name) {
		System.out.println("Performing 'stach' on " + repository);
		this.invalidate();
	}

	private void pop() {
		System.out.println("Performing 'pop' on " + repository);
		this.invalidate();
	}

	public Repository getRepository() {
		return repository;
	}

	public void setAutoInvalidate(boolean value) {
		this.autoInvalidate = value;
		invalidate();
	}
}
