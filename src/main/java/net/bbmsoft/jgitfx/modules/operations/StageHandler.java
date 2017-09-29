package net.bbmsoft.jgitfx.modules.operations;

import java.util.function.Supplier;

import org.eclipse.jgit.api.AddCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoFilepatternException;
import org.eclipse.jgit.dircache.DirCache;

import net.bbmsoft.jgitfx.event.EventPublisher;
import net.bbmsoft.jgitfx.event.TaskTopic;
import net.bbmsoft.jgitfx.modules.RepositoryActionHandler;
import net.bbmsoft.jgitfx.modules.RepositoryHandler;
import net.bbmsoft.jgitfx.modules.RepositoryHandler.Task;

public class StageHandler extends RepositoryActionHandler<DirCache> {

	public StageHandler(Runnable updateCallback, EventPublisher publisher) {
		super(updateCallback, publisher);
	}
	
	public void stage(Git git, String filesStrg) {

		String[] files = filesStrg.split(",");
		Task<DirCache> stageTask = new StageTask(() -> doStage(git, files), git, files.length);

		publish(TaskTopic.StageTask.STARTED, stageTask);
		
	}
	
	private DirCache doStage(Git git, String[] files) {
		AddCommand addCommand = git.add();
		for(String file : files) {
			addCommand.addFilepattern(file);
		}
		try {
			return addCommand.call();
		} catch (NoFilepatternException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (GitAPIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	@Override
	protected void evaluateResult(DirCache result) {
		// TODO Auto-generated method stub
		
	}

	static class StageTask extends RepositoryHandler.Task<DirCache> {

		public StageTask(Supplier<DirCache> resultSupplier, Git git, int size) {
			super(resultSupplier, git.getRepository());
			updateTitle("Staging " + size + " changes...");
			updateMessage("Pending...");
		}

		@Override
		protected DirCache call() {
			updateMessage("Staging changes ...");
			DirCache result = super.call();
			updateMessage("Done.");
			return result;
		}
	}

}
