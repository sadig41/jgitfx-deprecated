package net.bbmsoft.jgitfx.event;

import org.eclipse.jgit.diff.DiffEntry;

import net.bbmsoft.jgitfx.event.EventBroker.Topic;

public enum DiffTopic implements Topic<DiffEntry> {

	DIFF_ENTRY_SELECTED;
}
