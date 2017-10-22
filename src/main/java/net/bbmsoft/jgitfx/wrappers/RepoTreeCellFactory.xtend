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
		ChangeListener<String> longNameListener

		RepositoryWrapper wrapper

		new(StringConverter<RepositoryWrapper> converter) {
			super(converter)
		}

		override updateItem(RepositoryWrapper newWrapper, boolean empty) {

			this.wrapper?.unstagedChangesProperty?.removeListener(this.unstagedChangesListenr)
			this.wrapper?.stagedChangesProperty?.removeListener(this.stagedChangesListenr)
			this.wrapper?.childrenOutOfSyncProperty?.removeListener(this.childrenOutOfSyncListener)
			this.wrapper?.openProperty?.removeListener(this.openListener)
			this.wrapper?.longNameProperty?.removeListener(this.longNameListener)

			super.updateItem(item, empty)
			
			this.wrapper = newWrapper
			
			if (this.wrapper !== null && !empty) {

				this.unstagedChangesListenr = this.wrapper.unstagedChangesProperty >> [
					PSEUDO_CLASS_UNSTAGED_CHANGES.pseudoClassStateChanged = it
				]
				this.stagedChangesListenr = this.wrapper.stagedChangesProperty >> [
					PSEUDO_CLASS_STAGED_CHANGES.pseudoClassStateChanged = it
				]
				this.childrenOutOfSyncListener = this.wrapper.childrenOutOfSyncProperty >> [
					PSEUDO_CLASS_CHILDREN_OUT_OF_SYNC.pseudoClassStateChanged = it
				]
				this.openListener = this.wrapper.openProperty >> [
					PSEUDO_CLASS_OPEN.pseudoClassStateChanged = it
				]
				this.longNameListener = this.wrapper.longNameProperty >> [
					this.text = it
				]
			}
		}
	}

}
