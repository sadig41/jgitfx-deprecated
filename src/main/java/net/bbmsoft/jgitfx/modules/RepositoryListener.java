package net.bbmsoft.jgitfx.modules;

import javafx.scene.control.TreeItem;
import net.bbmsoft.jgitfx.JGitFXMainFrame;
import net.bbmsoft.jgitfx.event.EventBroker;
import net.bbmsoft.jgitfx.event.EventBroker.Topic;
import net.bbmsoft.jgitfx.event.RepositoryTopic;
import net.bbmsoft.jgitfx.wrappers.RepositoryWrapper;

public class RepositoryListener implements EventBroker.Listener<RepositoryHandler> {
	
	private final JGitFXMainFrame mainFrame;
	
	private boolean appStarting;

	public RepositoryListener(JGitFXMainFrame mainFrame) {
		this.mainFrame = mainFrame;
	}

	@Override
	public void update(Topic<RepositoryHandler> topic, RepositoryHandler payload) {

		if(topic instanceof RepositoryTopic) {
			switch ((RepositoryTopic) topic) {
			case REPO_LOADED:
				TreeItem<RepositoryWrapper> treeItem = new TreeItem<>(new RepositoryWrapper(payload.getRepository()));
				this.mainFrame.getRepositoryTreeItems().add(treeItem);
				if(!appStarting) {
					this.mainFrame.open(payload.getRepository());
				}
				break;
			case REPO_REMOVED:
				// TODO
			case REPO_CLOSED:
			case REPO_OPENED:
			case REPO_UPDATED:
				this.mainFrame.updateRepo(payload);
				break;
			default:
				throw new IllegalStateException("Unknown topic: " + topic);
			
			}
		}
	}

	public boolean isAppStarting() {
		return appStarting;
	}

	public void setAppStarting(boolean appStarting) {
		this.appStarting = appStarting;
	}

}
