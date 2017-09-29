package net.bbmsoft.jgitfx.modules.error;

public class PreconditionCheckFailedException extends Exception {

	private static final long serialVersionUID = 1126418851446069316L;

	private final Reason reason;

	public PreconditionCheckFailedException(String message, Reason reason) {
		super(message);
		this.reason = reason;
	}

	public Reason getReason() {
		return reason;
	}

	public enum Reason {
		INVALID_MESSAGE;
	}
}
