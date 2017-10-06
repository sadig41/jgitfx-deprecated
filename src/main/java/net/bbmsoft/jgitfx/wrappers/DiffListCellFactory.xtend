package net.bbmsoft.jgitfx.wrappers

import de.jensd.fx.glyphs.octicons.OctIconView
import java.util.function.Function
import javafx.scene.control.ListCell
import javafx.scene.control.ListView
import javafx.util.Callback
import net.bbmsoft.fxtended.annotations.css.PseudoClasses
import net.bbmsoft.jgitfx.utils.DiffListItemStringConverter
import net.bbmsoft.jgitfx.utils.IconListCell
import org.eclipse.jgit.diff.DiffEntry
import org.eclipse.jgit.diff.DiffEntry.ChangeType

@PseudoClasses('added', 'deleted', 'modified', 'renamed', 'copied')
class DiffListCellFactory implements Callback<ListView<DiffEntry>, ListCell<DiffEntry>> {
	
	Function<DiffEntry, String> converter = new DiffListItemStringConverter
	
	override call(ListView<DiffEntry> param) {
		new DiffTextFieldListCell(this.converter)
	}
	
	static class DiffTextFieldListCell extends IconListCell<DiffEntry> {
		
		new(Function<DiffEntry, String> converter) {
			super(new OctIconView, converter)
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