package net.bbmsoft.jgitfx.messaging;

public class Message {

	private final String title;
	private final String body;
	private final Throwable throwable;

	public Message(String title, String body) {
		this(title, body, null);
	}
	
	public Message(String title, String body, Throwable throwable) {
		this.title = title;
		this.body = body;
		this.throwable = throwable;
	}

	public String getTitle() {
		return title;
	}

	public String getBody() {
		return body;
	}

	public Throwable getThrowable() {
		return throwable;
	}
}
