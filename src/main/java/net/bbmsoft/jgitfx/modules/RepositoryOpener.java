package net.bbmsoft.jgitfx.modules;

import java.io.File;

import javafx.stage.DirectoryChooser;
import javafx.stage.Window;
import net.bbmsoft.jgitfx.registry.RepositoryRegistry;

public class RepositoryOpener {

	private final DirectoryChooser dirChooser;

	private File lastOpened;

	public RepositoryOpener() {
		this.dirChooser = new DirectoryChooser();
	}

	public void openRepo(Window root, RepositoryRegistry registry) {

		if (this.lastOpened != null) {
			this.dirChooser.setInitialDirectory(this.lastOpened);
		}

		File dir = this.dirChooser.showDialog(root);

		if (dir != null && registry.registerRepository(dir, true)) {
			this.lastOpened = dir;
		}
	}
}
