package net.bbmsoft.jgitfx.utils;

import java.util.function.Function;

import org.eclipse.jgit.diff.DiffEntry;

public class DiffListItemStringConverter implements Function<DiffEntry, String> {

	@Override
	public String apply(DiffEntry object) {
		if(object == null) {
			return "";
		}
		return StagingHelper.getFilePath(object);
	}

}
