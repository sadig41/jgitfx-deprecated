package net.bbmsoft.jgitfx.event;

import java.util.List;

import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.xtext.xbase.lib.Pair;

public enum DiffTopic implements Topic<Pair<Repository, List<DiffEntry>>> {

	UNSTAGED_CHANGES_FOUND, STAGED_CHANGES_FOUND;
	
}
