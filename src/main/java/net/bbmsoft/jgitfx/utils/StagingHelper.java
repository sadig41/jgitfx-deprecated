package net.bbmsoft.jgitfx.utils;

import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffEntry.ChangeType;

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
}
