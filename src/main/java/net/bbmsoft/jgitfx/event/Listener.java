package net.bbmsoft.jgitfx.event;

public interface Listener<T> {
	public void update(Topic<T> topic, T payload);
}