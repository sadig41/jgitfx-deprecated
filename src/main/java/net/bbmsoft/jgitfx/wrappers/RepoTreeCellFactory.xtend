package net.bbmsoft.jgitfx.wrappers

import javafx.beans.value.ChangeListener
import javafx.scene.control.TreeCell
import javafx.scene.control.TreeView
import javafx.scene.control.cell.TextFieldTreeCell
import javafx.util.Callback
import javafx.util.StringConverter
import net.bbmsoft.fxtended.annotations.css.PseudoClasses
import net.bbmsoft.jgitfx.utils.RepositoryTreeItemStringConverter

import static extension net.bbmsoft.fxtended.extensions.BindingOperatorExtensions.*

@PseudoClasses('unstaged-changes', 'staged-changes', 'children-out-of-sync', 'open')
class RepoTreeCellFactory implements Callback<TreeView<RepositoryWrapper>, TreeCell<RepositoryWrapper>> {

	StringConverter<RepositoryWrapper> converter = new RepositoryTreeItemStringConverter

	override call(TreeView<RepositoryWrapper> tree) {
		new RepoTextFieldTreeCell(this.converter)
	}

	static class RepoTextFieldTreeCell extends TextFieldTreeCell<RepositoryWrapper> {

		ChangeListener<Boolean> unstagedChangesListenr
		ChangeListener<Boolean> stagedChangesListenr
		ChangeListener<Boolean> childrenOutOfSyncListener
		ChangeListener<Boolean> openListener

		new(StringConverter<RepositoryWrapper> converter) {
			super(converter)
		}
		
		override updateItem(RepositoryWrapper newWrapper, boolean empty) {
			
			val oldWrapper =  getItem
			
			if (oldWrapper !== null) {
				oldWrapper.unstagedChangesProperty - this.unstagedChangesListenr
				oldWrapper.stagedChangesProperty - this.stagedChangesListenr
				oldWrapper.childrenOutOfSyncProperty - this.childrenOutOfSyncListener
				oldWrapper.openProperty - this.openListener
			}
			
			super.updateItem(item, empty)
			
			if (newWrapper !== null && !empty) {
				
				unstagedChangesListenr = newWrapper.unstagedChangesProperty >> [PSEUDO_CLASS_UNSTAGED_CHANGES.pseudoClassStateChanged = it]
				stagedChangesListenr = newWrapper.stagedChangesProperty >> [PSEUDO_CLASS_STAGED_CHANGES.pseudoClassStateChanged = it]
				childrenOutOfSyncListener = newWrapper.childrenOutOfSyncProperty >> [PSEUDO_CLASS_CHILDREN_OUT_OF_SYNC.pseudoClassStateChanged = it]
				openListener = newWrapper.openProperty >> [PSEUDO_CLASS_OPEN.pseudoClassStateChanged = it]
				
				newWrapper.longNameProperty >> [
					this.text = it
				]
			}
		}
	}

}
