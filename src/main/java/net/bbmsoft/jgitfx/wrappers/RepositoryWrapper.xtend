package net.bbmsoft.jgitfx.wrappers

import org.eclipse.jgit.lib.Repository
import net.bbmsoft.fxtended.annotations.binding.BindableProperty
import static extension net.bbmsoft.fxtended.extensions.BindingOperatorExtensions.*
import net.bbmsoft.fxtended.annotations.binding.ReadOnly

class RepositoryWrapper {
	
	final static private String PULL = '↓'
	final static private String PUSH = '↑'
	
	final Repository repository
	
	@ReadOnly @BindableProperty  String name
	@ReadOnly @BindableProperty String longName
	
	@BindableProperty int ahead
	@BindableProperty int behind
	@BindableProperty boolean unstagedChanges
	@BindableProperty boolean stagedChanges
	@BindableProperty boolean childrenOutOfSync


	new(Repository repository) {
		this(repository, null)
	}
	
	protected new(Repository repository, String name) {
		
		this.repository = repository
		this.name = name ?: this.repository?.workTree?.name
		
		this.longName = '''«this.name» («this.repository?.workTree?.absolutePath»)'''
		
		aheadProperty > [updateLongName]
		behindProperty > [updateLongName]
	}
	
	private def updateLongName() {
		this.longName = '''«this.name» («this.repository?.workTree?.absolutePath»)«IF behind > 0 || ahead > 0» «ENDIF»«IF behind > 0»«PULL»«behind»«ENDIF»«IF ahead > 0»«PUSH»«ahead»«ENDIF»'''
	}

	override int hashCode() {
		val int prime = 31
		var int result = 1
		result = prime * result + (if((repository === null)) 0 else repository.directory.absolutePath.hashCode )
		return result
	}

	override boolean equals(Object obj) {
		if(this === obj) return true
		if(obj === null) return false
		if(class !== obj.class) return false
		var RepositoryWrapper other = obj as RepositoryWrapper
		if (repository === null) {
			if(other.repository !== null) return false
		} else if(repository.directory.absolutePath != other.repository.directory.absolutePath) return false
		return true
	}

	def Repository getRepository() {
		this.repository
	}

	override String toString() {
		this.name
	}

	static class DummyWrapper extends RepositoryWrapper {
		
		new(String name) {
			super(null, name)
		}
	}
}
