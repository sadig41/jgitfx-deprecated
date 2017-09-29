package net.bbmsoft.jgitfx.modules.operations;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

import org.eclipse.jgit.api.AddCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.RmCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoFilepatternException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffEntry.ChangeType;
import org.eclipse.jgit.dircache.DirCache;

import com.google.common.collect.Iterables;

import net.bbmsoft.jgitfx.event.EventPublisher;
import net.bbmsoft.jgitfx.event.TaskTopic;
import net.bbmsoft.jgitfx.modules.RepositoryActionHandler;
import net.bbmsoft.jgitfx.modules.RepositoryHandler;
import net.bbmsoft.jgitfx.modules.RepositoryHandler.Task;

public class StageHandler extends RepositoryActionHandler<DirCache> {

	private static final List<ChangeType> ADD_TYPES = Arrays.asList(ChangeType.ADD, ChangeType.COPY, ChangeType.MODIFY,
			ChangeType.RENAME);
	private static final List<ChangeType> REMOVE_TYPES = Arrays.asList(ChangeType.DELETE, ChangeType.RENAME);

	public StageHandler(Runnable updateCallback, EventPublisher publisher) {
		super(updateCallback, publisher);
	}

	public void stage(Git git, List<DiffEntry> diffs) {

		Task<DirCache> stageTask = new StageTask(() -> doStage(git, diffs), git, diffs.size());

		publish(TaskTopic.StageTask.STARTED, stageTask);

	}

	private DirCache doStage(Git git, List<DiffEntry> diffs) {

		DirCache cache = null;

		if (Iterables.any(diffs, diff -> added(diff))) {
			cache = addFiles(git, diffs, cache);
		}

		if (Iterables.any(diffs, diff -> removed(diff))) {
			cache = removeFiles(git, diffs, cache);
		}

		return cache;
	}

	private boolean removed(DiffEntry diff) {
		return REMOVE_TYPES.contains(diff.getChangeType());
	}

	private boolean added(DiffEntry diff) {
		return ADD_TYPES.contains(diff.getChangeType());
	}

	private DirCache removeFiles(Git git, List<DiffEntry> diffs, DirCache cache) {
		RmCommand removeCommand = git.rm();
		for (DiffEntry diff : diffs) {
			if (removed(diff)) {
				removeCommand.addFilepattern(diff.getOldPath());
			}
		}
		try {
			cache = removeCommand.call();
		} catch (NoFilepatternException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (GitAPIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return cache;
	}

	private DirCache addFiles(Git git, List<DiffEntry> diffs, DirCache cache) {
		AddCommand addCommand = git.add();
		for (DiffEntry diff : diffs) {
			if (added(diff)) {
				addCommand.addFilepattern(diff.getNewPath());
			}
		}
		try {
			cache = addCommand.call();
		} catch (NoFilepatternException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (GitAPIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return cache;
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
