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
	
	public Message(CharSequence title, CharSequence header, CharSequence body) {
		this(title, header, body, null);
	}
	
	public Message(CharSequence title, CharSequence body, Throwable throwable) {
		this(title, ErrorHelper.getRoot(throwable, th -> th.getCause()).getMessage(), body, throwable);
	}
	
	public Message(CharSequence title, CharSequence header, CharSequence body, Throwable throwable) {
		this.title = title.toString();
		this.header = header.toString();
		this.body = body.toString();
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
