package net.bbmsoft.jgitfx.wrappers

import javafx.beans.value.ChangeListener
import javafx.scene.control.TreeCell
import javafx.scene.control.TreeView
import javafx.scene.control.cell.TextFieldTreeCell
import javafx.util.Callback
import javafx.util.StringConverter
import net.bbmsoft.jgitfx.utils.RepositoryTreeItemStringConverter

import static extension net.bbmsoft.fxtended.extensions.BindingOperatorExtensions.*
import net.bbmsoft.fxtended.annotations.css.PseudoClasses

@PseudoClasses('unstaged-changes', 'staged-changes', 'children-out-of-sync')
class RepoTreeCellFactory implements Callback<TreeView<RepositoryWrapper>, TreeCell<RepositoryWrapper>> {

	StringConverter<RepositoryWrapper> converter = new RepositoryTreeItemStringConverter

	override call(TreeView<RepositoryWrapper> tree) {
		new RepoTextFieldTreeCell(this.converter)
	}

	static class RepoTextFieldTreeCell extends TextFieldTreeCell<RepositoryWrapper> {

		ChangeListener<Boolean> unstagedChangesListenr = [PSEUDO_CLASS_UNSTAGED_CHANGES.pseudoClassStateChanged = $2]
		ChangeListener<Boolean> stagedChangesListenr = [PSEUDO_CLASS_STAGED_CHANGES.pseudoClassStateChanged = $2]
		ChangeListener<Boolean> childrenOutOfSyncListener = [PSEUDO_CLASS_CHILDREN_OUT_OF_SYNC.pseudoClassStateChanged = $2]

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
				oldWrapper.unstagedChangesProperty >> this.unstagedChangesListenr
				oldWrapper.stagedChangesProperty >> this.stagedChangesListenr
				oldWrapper.childrenOutOfSyncProperty >> this.childrenOutOfSyncListener
				this.textProperty << newWrapper.longNameProperty
			}
		}
	}

}
