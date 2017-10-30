package net.bbmsoft.jgitfx.event;

import net.bbmsoft.jgitfx.messaging.Message;

public interface UserInputTopic<T> extends Topic<Message>{

	public enum ConfirmationTopic implements UserInputTopic<Boolean> {
		CONFIRM;

		private boolean value;
		
		@Override
		public Boolean get() {
			return this.value;
		}
		
		@Override
		public void set(Boolean value) {
			this.value = value;
		}
	}
	
	public T get();

	public void set(T value);
}
