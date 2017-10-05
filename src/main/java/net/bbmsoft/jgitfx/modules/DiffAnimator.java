package net.bbmsoft.jgitfx.modules;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.errors.CorruptObjectException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.AbbreviatedObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.xtext.xbase.lib.Pair;

import javafx.application.Platform;
import net.bbmsoft.jgitfx.event.DiffTopic;
import net.bbmsoft.jgitfx.event.EventBroker;
import net.bbmsoft.jgitfx.event.EventPublisher;
import net.bbmsoft.jgitfx.messaging.Message;
import net.bbmsoft.jgitfx.messaging.MessageType;
import net.bbmsoft.jgitfx.utils.RepoHelper;
import net.bbmsoft.jgitfx.utils.StagingHelper;

public class DiffAnimator {

	private final EventPublisher eventPublisher;
	private final List<AbbreviatedObjectId> blacklist;
	private final OutputStream diffOutputStream;

	private Repository repository;
	private DiffFormatter diffFormatter;

	public DiffAnimator(OutputStream diffOutputStream, EventBroker eventBroker) throws IOException {

		this.eventPublisher = eventBroker;
		this.blacklist = new ArrayList<>();
		this.diffOutputStream = diffOutputStream;

		eventBroker.subscribe(DiffTopic.DIFF_ENTRY_SELECTED, (topic, diff) -> updateDiff(diff));
	}

	private void updateDiff(Pair<Repository, List<DiffEntry>> diff) {
		
		Repository repo = diff.getKey();
		if(!RepoHelper.equal(repo, this.repository)) {
			this.setRepository(repo);
		}
		
		List<DiffEntry> diffs = diff.getValue();

		// can show only one diff, so choose the one that has been selected last
		DiffEntry actualDiff = diffs.isEmpty() ? null : diffs.get(diffs.size() - 1);
		
		setDiff(actualDiff);
	}

	private void setRepository(Repository repo) {

		if (this.diffFormatter != null) {
			this.diffFormatter.close();
		}

		if (repo == null) {
			this.repository = null;
			this.diffFormatter = null;
		} else {
			this.repository = repo;
			this.diffFormatter = new DiffFormatter(this.diffOutputStream);
			this.diffFormatter.setRepository(this.repository);
		}
	}

	private void setDiff(DiffEntry diff) {
		
		if (diff == null || this.blacklist.contains(StagingHelper.getID(diff))) {
			try {
				this.diffOutputStream.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return;
		}

		try {
			doSetDiff(diff);
		} catch (Throwable th) {
			this.blacklist.add(StagingHelper.getID(diff));
			Platform.runLater(() -> {
				this.eventPublisher.publish(MessageType.ERROR, new Message("Diff failed",
						String.format("Could not build detailed diff for file %s", StagingHelper.getFilePath(diff)),
						th));
			});
		}
		
		try {
			this.diffOutputStream.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void doSetDiff(DiffEntry diff) throws IOException, CorruptObjectException, MissingObjectException {
		this.diffFormatter.format(diff);
	}

}
