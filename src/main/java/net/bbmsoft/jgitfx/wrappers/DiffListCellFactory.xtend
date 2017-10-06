package net.bbmsoft.jgitfx.wrappers

import javafx.scene.control.ListCell
import javafx.scene.control.ListView
import javafx.scene.control.cell.TextFieldListCell
import javafx.util.Callback
import javafx.util.StringConverter
import net.bbmsoft.fxtended.annotations.css.PseudoClasses
import net.bbmsoft.jgitfx.utils.DiffListItemStringConverter
import org.eclipse.jgit.diff.DiffEntry
import org.eclipse.jgit.diff.DiffEntry.ChangeType

@PseudoClasses('added', 'deleted', 'modified', 'renamed', 'copied')
class DiffListCellFactory implements Callback<ListView<DiffEntry>, ListCell<DiffEntry>> {
	
	StringConverter<DiffEntry> converter = new DiffListItemStringConverter
	
	override call(ListView<DiffEntry> param) {
		new DiffTextFieldListCell(this.converter)
	}
	
	static class DiffTextFieldListCell extends TextFieldListCell<DiffEntry> {
		
		new(StringConverter<DiffEntry> converter) {
			super(converter)
		}
		
		override updateItem(DiffEntry item, boolean empty) {
			super.updateItem(item, empty)
			PSEUDO_CLASS_ADDED.pseudoClassStateChanged(!empty && item?.changeType == ChangeType.ADD)
			PSEUDO_CLASS_MODIFIED.pseudoClassStateChanged(!empty && item?.changeType == ChangeType.MODIFY)
			PSEUDO_CLASS_DELETED.pseudoClassStateChanged(!empty && item?.changeType == ChangeType.DELETE)
			PSEUDO_CLASS_RENAMED.pseudoClassStateChanged(!empty && item?.changeType == ChangeType.RENAME)
			PSEUDO_CLASS_COPIED.pseudoClassStateChanged(!empty && item?.changeType == ChangeType.COPY)
		}
		
	}
}