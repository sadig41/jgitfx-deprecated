package net.bbmsoft.jgitfx.utils;

import org.eclipse.jgit.lib.Repository;

import net.bbmsoft.jgitfx.modules.RepositoryHandler;

public class RepoHelper {

	public static Repository fromHandler(RepositoryHandler repo) {
		return repo != null ? repo.getRepository() : null;
	}

	public static boolean equal(Repository a, Repository b) {

		if (a == b) {
			return true;
		}

		if (a == null || b == null) {
			return false;
		}

		String pathA = a.getDirectory().getAbsolutePath();
		String pathB = b.getDirectory().getAbsolutePath();

		return pathA.equals(pathB);
	}
}
