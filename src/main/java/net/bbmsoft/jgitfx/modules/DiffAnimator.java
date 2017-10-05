package net.bbmsoft.jgitfx.modules;

import java.io.IOException;

import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.treewalk.filter.PathFilter;

import net.bbmsoft.bbm.utils.io.StringOutputStream;
import net.bbmsoft.jgitfx.event.DetailedDiffTopic;
import net.bbmsoft.jgitfx.event.EventBroker;
import net.bbmsoft.jgitfx.utils.DiffDetails;

public class DiffAnimator {

	private StringOutputStream diffOutputStream;

	public DiffAnimator(EventBroker eventBroker, StringOutputStream diffOutputStream) throws IOException {
		this.diffOutputStream = diffOutputStream;
		eventBroker.subscribe(DetailedDiffTopic.DIFF_ENTRY_SELECTED, (topic, diff) -> updateDiff(diff));
	}

	private void updateDiff(DiffDetails diff) {
		
		if(diff == null) {
			this.diffOutputStream.reset();
			this.diffOutputStream.flush();
			return;
		}
		
		try(DiffFormatter formatter = new DiffFormatter(this.diffOutputStream)) {
			formatter.setRepository(diff.getRepository());
			formatter.setPathFilter(PathFilter.create(diff.getFilePath()));
			formatter.format(diff.getParent(), diff.getCommit());
			formatter.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
