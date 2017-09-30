package net.bbmsoft.jgitfx.modules.operations;

import java.util.Arrays;
import java.util.List;

import org.eclipse.jgit.api.AddCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.api.RmCommand;
import org.eclipse.jgit.api.errors.CheckoutConflictException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoFilepatternException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffEntry.ChangeType;

import com.google.common.collect.Iterables;

import net.bbmsoft.jgitfx.utils.StagingHelper;

public class StageHandler  {

	private static final List<ChangeType> ADD_TYPES = Arrays.asList(ChangeType.ADD, ChangeType.COPY, ChangeType.MODIFY,
			ChangeType.RENAME);
	private static final List<ChangeType> REMOVE_TYPES = Arrays.asList(ChangeType.DELETE, ChangeType.RENAME);

	public void stage(Git git, List<DiffEntry> diffs) {
		
		if (Iterables.any(diffs, diff -> added(diff))) {
			addFiles(git, diffs);
		}

		if (Iterables.any(diffs, diff -> removed(diff))) {
			removeFiles(git, diffs);
		}
	}
	
	public void unstage(Git git, List<DiffEntry> diffs) {
		ResetCommand resetCommand = git.reset();
		diffs.forEach(diff -> resetCommand.addPath(StagingHelper.getFilePath(diff)));
		try {
			resetCommand.call();
		} catch (CheckoutConflictException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (GitAPIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	private boolean removed(DiffEntry diff) {
		return REMOVE_TYPES.contains(diff.getChangeType());
	}

	private boolean added(DiffEntry diff) {
		return ADD_TYPES.contains(diff.getChangeType());
	}

	private void removeFiles(Git git, List<DiffEntry> diffs) {
		RmCommand removeCommand = git.rm();
		for (DiffEntry diff : diffs) {
			if (removed(diff)) {
				removeCommand.addFilepattern(diff.getOldPath());
			}
		}
		try {
			removeCommand.call();
		} catch (NoFilepatternException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (GitAPIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void addFiles(Git git, List<DiffEntry> diffs) {
		AddCommand addCommand = git.add();
		for (DiffEntry diff : diffs) {
			if (added(diff)) {
				addCommand.addFilepattern(diff.getNewPath());
			}
		}
		try {
			addCommand.call();
		} catch (NoFilepatternException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (GitAPIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
