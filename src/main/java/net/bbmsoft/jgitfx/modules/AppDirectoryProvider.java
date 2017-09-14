package net.bbmsoft.jgitfx.modules;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class AppDirectoryProvider {

	private static final Path path = Paths.get(System.getProperty("user.home"), ".jgitfx");

	static {
		File dir = getAppDirectory();
		if (!dir.exists()) {
			dir.mkdirs();
		}
	}

	public static Path getAppDirectoryPath() {
		return path;
	}

	public static File getAppDirectory() {
		return path.toFile();
	}

	public static File getFilePathFromAppDirectory(String name) {
		return path.resolve(name).toFile();
	}
}
