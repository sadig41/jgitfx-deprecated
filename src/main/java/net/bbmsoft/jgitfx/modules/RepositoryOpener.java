package net.bbmsoft.jgitfx.modules;

import java.io.File;
import java.util.function.Function;

import javafx.stage.DirectoryChooser;
import javafx.stage.Window;
import net.bbmsoft.jgitfx.models.RepositoryRegistry;

public class RepositoryOpener {

	private final DirectoryChooser dirChooser;

	private File lastOpened;

	public RepositoryOpener() {
		this.dirChooser = new DirectoryChooser();
	}

	public void openRepo(Window root, RepositoryRegistry registry, Function<File, Boolean> openAction) {

		if (this.lastOpened != null) {
			this.dirChooser.setInitialDirectory(this.lastOpened);
		}

		File dir = this.dirChooser.showDialog(root);

		if (dir != null) {
			registry.registerRepository(dir);
			if(openAction.apply(dir)) {
				this.lastOpened = dir;
			}
		}
	}
}
