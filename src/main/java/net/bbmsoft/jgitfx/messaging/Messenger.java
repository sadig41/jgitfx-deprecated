package net.bbmsoft.jgitfx.messaging;

public interface Messenger {

	public abstract void showMessage(MessageType type, String title, String body);
}
