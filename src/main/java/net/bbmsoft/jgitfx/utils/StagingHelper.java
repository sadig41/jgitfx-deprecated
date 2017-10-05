package net.bbmsoft.jgitfx.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffEntry.ChangeType;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.lib.AbbreviatedObjectId;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.xtext.xbase.lib.IterableExtensions;

public class StagingHelper {

	public static String getFilePath(DiffEntry diff) {
		ChangeType changeType = diff.getChangeType();
		switch (changeType) {
		case ADD:
		case MODIFY:
			return diff.getNewPath();
		case DELETE:
			return diff.getOldPath();
		case COPY:
		case RENAME:
			new StringBuilder(diff.getOldPath()).append(" -> ").append(diff.getNewPath()).toString();
		default:
			throw new IllegalArgumentException("Unknown change type: " + changeType);
		}
	}
	
	public static AbbreviatedObjectId getID(DiffEntry diff) {
		ChangeType changeType = diff.getChangeType();
		switch (changeType) {
		case ADD:
		case MODIFY:
		case COPY:
		case RENAME:
			return diff.getNewId();
		case DELETE:
			return diff.getOldId();
		default:
			throw new IllegalArgumentException("Unknown change type: " + changeType);
		}
	}
	
	public static void applyToMatching(List<DiffEntry> source, List<DiffEntry> test, Consumer<DiffEntry> process) {
		findMatching(source, test).forEach(d -> process.accept(d));
	}
	
	public static void applyToMatching(List<DiffEntry> source, DiffEntry test, Consumer<DiffEntry> process) {
		process.accept(findMatching(source, test));
	}

	public static List<DiffEntry> findMatching(List<DiffEntry> source, List<DiffEntry> test) {
		List<DiffEntry> result = new ArrayList<>();
		for (DiffEntry diff : source) {
			if (hasMatch(test, diff)) {
				result.add(diff);
			}
		}
		return result;
	}
	
	public static DiffEntry findMatching(List<DiffEntry> source, DiffEntry test) {
		for (DiffEntry diff : source) {
			if (equal(test, diff)) {
				return test;
			}
		}
		return null;
	}

	private static boolean hasMatch(List<DiffEntry> diffs, DiffEntry diff) {
		return IterableExtensions.exists(diffs, d -> equal(diff, d));
	}

	public static boolean equal(DiffEntry a, DiffEntry b) {

		if (a == b) {
			return true;
		}

		if ((a == null) || (b == null)) {
			return false;
		}

		String pathA = StagingHelper.getFilePath(a);
		String pathB = StagingHelper.getFilePath(b);

		return pathA.equals(pathB);
	}
	

	public static AbstractTreeIterator getTree(RevCommit commit, Repository repo)
			throws IncorrectObjectTypeException, IOException {
		ObjectId treeId = commit.getTree().getId();
		try (ObjectReader reader = repo.newObjectReader()) {
			return new CanonicalTreeParser(null, reader, treeId);
		}
	}
}
