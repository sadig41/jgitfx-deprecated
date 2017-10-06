package net.bbmsoft.jgitfx.messaging;

import java.io.PrintWriter;
import java.io.StringWriter;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import net.bbmsoft.bbm.utils.concurrent.ThreadUtils;
import net.bbmsoft.jgitfx.event.Topic;

public class DialogMessenger implements Messenger {

	private boolean blocking;

	@Override
	public void showMessage(Topic<Message> topic, Message message) {

		try {
			ThreadUtils.runOnJavaFXThreadAndWait(() -> {
				Alert alert = createAlert(topic, message);
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

	private Alert createAlert(Topic<Message> topic, Message message) {
		
		Alert alert = new Alert(getAltertType(topic));
		alert.setTitle(message.getTitle());
		alert.setHeaderText(message.getHeader());
		alert.setContentText(message.getBody());
		
		if(message.getThrowable() == null) {
			return alert;
		}

		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		message.getThrowable().printStackTrace(pw);
		String exceptionText = sw.toString();

		TextArea textArea = new TextArea(exceptionText);
		textArea.setEditable(false);
		textArea.setWrapText(true);

		textArea.setMaxWidth(Double.MAX_VALUE);
		textArea.setMaxHeight(Double.MAX_VALUE);
		GridPane.setVgrow(textArea, Priority.ALWAYS);
		GridPane.setHgrow(textArea, Priority.ALWAYS);

		alert.getDialogPane().setExpandableContent(textArea);
		
		return alert;
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
