package net.bbmsoft.jgitfx.utils;

import net.bbmsoft.jgitfx.wrappers.RepositoryWrapper;
import javafx.util.StringConverter;

public class RepositoryTreeItemStringConverter extends StringConverter<RepositoryWrapper> {
	
	private final boolean useShortName;
	
	public RepositoryTreeItemStringConverter() {
		this(false);
	}

	public RepositoryTreeItemStringConverter(boolean useShortName) {
		this.useShortName = useShortName;
	}

	@Override
	public String toString(RepositoryWrapper object) {
		return this.useShortName ? object.getName() : object.getLongName();
	}

	@Override
	public RepositoryWrapper fromString(String string) {
		throw new UnsupportedOperationException("Can't convert from String to RepositoryWrapper!");
	}

}
