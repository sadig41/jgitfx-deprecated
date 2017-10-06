package net.bbmsoft.jgitfx.utils;

import java.util.function.Function;

import javafx.scene.Node;
import javafx.scene.control.ListCell;

public class IconListCell<T> extends ListCell<T> {

	private final Node iconView;
	private final Function<T, String> stringConverter;
	
	public IconListCell(Node iconView) {
		this(iconView, null);
	}
	
	public IconListCell(Node iconView, Function<T, String> stringConverter) {
		this.iconView = iconView;
		this.stringConverter = stringConverter != null ? stringConverter : i -> i != null ? i.toString() : "";
	}

	@Override
	protected void updateItem(T item, boolean empty) {
		super.updateItem(item, empty);

		if (this.isEmpty()) {
			this.setText(null);
			this.setGraphic(null);
		} else {
			this.setText(this.stringConverter.apply(item));
			this.setGraphic(this.iconView);
		}
	}
}
