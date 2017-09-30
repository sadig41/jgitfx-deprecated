package net.bbmsoft.jgitfx.messaging;

import net.bbmsoft.jgitfx.utils.ErrorHelper;

public class Message {

	private final String title;
	private final String header;
	private final String body;
	private final Throwable throwable;

	public Message(String title, String body) {
		this(title, body, null, null);
	}
	
	public Message(String title, String header, String body) {
		this(title, header, body, null);
	}
	
	public Message(String title, String body, Throwable throwable) {
		this(title, ErrorHelper.getRoot(throwable, th -> th.getCause()).getMessage(), body, throwable);
	}
	
	public Message(String title, String header, String body, Throwable throwable) {
		this.title = title;
		this.header = header;
		this.body = body;
		this.throwable = throwable;
	}

	public String getTitle() {
		return title;
	}

	public String getHeader() {
		return header;
	}

	public String getBody() {
		return body;
	}

	public Throwable getThrowable() {
		return throwable;
	}
}
