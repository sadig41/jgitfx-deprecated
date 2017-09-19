package net.bbmsoft.jgitfx.messaging;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import net.bbmsoft.bbm.utils.concurrent.ThreadUtils;
import net.bbmsoft.jgitfx.event.EventBroker.Topic;

public class DialogMessenger implements Messenger {

	private boolean blocking;

	@Override
	public void showMessage(Topic<Message> topic, Message message) {

		try {
			ThreadUtils.runOnJavaFXThreadAndWait(() -> {
				Alert alert = new Alert(getAltertType(topic));
				alert.setTitle(message.getTitle());
				alert.setHeaderText(null);
				alert.setContentText(message.getBody());
				if (this.isBlocking()) {
					alert.showAndWait();
				} else {
					alert.show();
				}
			});
		} catch (InterruptedException e) {
			// ignore
		}
	}

	private AlertType getAltertType(Topic<Message> topic) {
		
		if(topic instanceof MessageType) {
			switch ((MessageType)topic) {
			case ERROR:
				return AlertType.ERROR;
			case INFO:
				return AlertType.INFORMATION;
			case SUCCESS:
				return AlertType.INFORMATION;
			default:
				throw new IllegalArgumentException("Unknown message type: " + topic);
			}
		} else {
			return AlertType.NONE;
		}
	}

	public boolean isBlocking() {
		return blocking;
	}

	public void setBlocking(boolean blocking) {
		this.blocking = blocking;
	}
}
