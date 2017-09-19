package net.bbmsoft.jgitfx.messaging;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import net.bbmsoft.bbm.utils.concurrent.ThreadUtils;

public class DialogMessenger implements Messenger {

	private boolean blocking;

	@Override
	public void showMessage(MessageType type, String title, String body) {

		try {
			ThreadUtils.runOnJavaFXThreadAndWait(() -> {
				Alert alert = new Alert(getAltertType(type));
				alert.setTitle(title);
				alert.setHeaderText(null);
				alert.setContentText(body);
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

	private AlertType getAltertType(MessageType type) {
		switch (type) {
		case ERROR:
			return AlertType.ERROR;
		case INFO:
			return AlertType.INFORMATION;
		case SUCCESS:
			return AlertType.INFORMATION;
		default:
			throw new IllegalArgumentException("Unknown message type: " + type);

		}
	}

	public boolean isBlocking() {
		return blocking;
	}

	public void setBlocking(boolean blocking) {
		this.blocking = blocking;
	}
}
