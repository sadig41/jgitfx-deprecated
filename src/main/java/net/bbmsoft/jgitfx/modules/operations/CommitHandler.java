package net.bbmsoft.jgitfx.modules.operations;

import org.eclipse.jgit.api.Git;

import net.bbmsoft.jgitfx.event.EventPublisher;
import net.bbmsoft.jgitfx.messaging.Message;
import net.bbmsoft.jgitfx.messaging.MessageType;

public class CommitHandler {

	private final EventPublisher eventPublisher;

	public CommitHandler(EventPublisher eventPublisher) {
		this.eventPublisher = eventPublisher;
	}

	public void commit(Git git, String message) {

		if (message == null || message.trim().isEmpty()) {
			throw new IllegalArgumentException("Invalid commit message!");
		}

		try {
			git.commit().setMessage(message).call();
		} catch (Throwable th) {
			this.eventPublisher.publish(MessageType.ERROR,
					new Message("Commit failed", "Commit command terminated abnormally.", th));
		}

	}
}
