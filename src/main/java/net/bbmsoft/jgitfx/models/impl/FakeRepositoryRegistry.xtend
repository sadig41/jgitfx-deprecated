package net.bbmsoft.jgitfx.models.impl

import java.io.File
import java.util.List
import net.bbmsoft.jgitfx.models.RepositoryRegistry

class FakeRepositoryRegistry implements RepositoryRegistry {
	
	private final List<File> fakeRepos = newArrayList(#['repo 1', 'repo 2', 'repo 3','repo 4', 'repo 5'].map[new File(it)])
	
	override getRegisteredRepositories() {
		return fakeRepos
	}
}
