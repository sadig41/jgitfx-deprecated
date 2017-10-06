package net.bbmsoft.jgitfx.utils;

import org.eclipse.jgit.diff.DiffEntry;

import javafx.util.StringConverter;

public class DiffListItemStringConverter extends StringConverter<DiffEntry> {

	@Override
	public String toString(DiffEntry object) {
		if(object == null) {
			return "";
		}
		return StagingHelper.getFilePath(object);
	}

	@Override
	public DiffEntry fromString(String string) {
		throw new UnsupportedOperationException("Can't convert from String to DiffEntry!");
	}

}
