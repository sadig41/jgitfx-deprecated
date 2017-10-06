package net.bbmsoft.jgitfx.event;

import org.eclipse.jgit.lib.Repository;
import org.eclipse.xtext.xbase.lib.Pair;

public enum OperationResultTopic implements Topic<Pair<Repository, Object>> {

	PULL_RESULT, PUSH_RESULT, COMMIT_RESULT, FETCH_RESULT, MERGE_RESULT, REBASE_RESULT;
}
