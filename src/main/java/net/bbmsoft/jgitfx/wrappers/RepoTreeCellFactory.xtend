package net.bbmsoft.jgitfx.wrappers

import java.util.function.Consumer
import javafx.beans.value.ChangeListener
import javafx.scene.control.TreeCell
import javafx.scene.control.TreeView
import javafx.scene.control.cell.TextFieldTreeCell
import javafx.util.Callback
import javafx.util.StringConverter
import net.bbmsoft.fxtended.annotations.css.PseudoClasses
import net.bbmsoft.jgitfx.utils.RepositoryTreeItemStringConverter

import static extension net.bbmsoft.fxtended.extensions.BindingOperatorExtensions.*

@PseudoClasses('unstaged-changes', 'staged-changes', 'children-out-of-sync')
class RepoTreeCellFactory implements Callback<TreeView<RepositoryWrapper>, TreeCell<RepositoryWrapper>> {

	StringConverter<RepositoryWrapper> converter = new RepositoryTreeItemStringConverter

	override call(TreeView<RepositoryWrapper> tree) {
		new RepoTextFieldTreeCell(this.converter)
	}

	static class RepoTextFieldTreeCell extends TextFieldTreeCell<RepositoryWrapper> {

		ChangeListener<Boolean> unstagedChangesListenr
		ChangeListener<Boolean> stagedChangesListenr
		ChangeListener<Boolean> childrenOutOfSyncListener
		
		Consumer<Boolean> unstagedChangesConsumer = [PSEUDO_CLASS_UNSTAGED_CHANGES.pseudoClassStateChanged = it]
		Consumer<Boolean> stagedChangesConsumer = [PSEUDO_CLASS_STAGED_CHANGES.pseudoClassStateChanged = it]
		Consumer<Boolean> childrenOutOfSyncConsumer = [PSEUDO_CLASS_CHILDREN_OUT_OF_SYNC.pseudoClassStateChanged = it]

		new(StringConverter<RepositoryWrapper> converter) {
			super(converter)
			itemProperty >> [observable, oldWrapper, newWrapper|updateItemListeners(oldWrapper, newWrapper)]
		}

		private def updateItemListeners(RepositoryWrapper oldWrapper, RepositoryWrapper newWrapper) {

			if (oldWrapper !== null) {
				oldWrapper.unstagedChangesProperty - this.unstagedChangesListenr
				oldWrapper.stagedChangesProperty - this.stagedChangesListenr
				oldWrapper.childrenOutOfSyncProperty - this.childrenOutOfSyncListener
			}

			if (newWrapper !== null) {
				unstagedChangesListenr = newWrapper.unstagedChangesProperty >> this.unstagedChangesConsumer
				stagedChangesListenr = newWrapper.stagedChangesProperty >> this.stagedChangesConsumer
				childrenOutOfSyncListener = newWrapper.childrenOutOfSyncProperty >> this.childrenOutOfSyncConsumer
				newWrapper.longNameProperty >> [this.text = it]
			}
		}
		
	}

}
