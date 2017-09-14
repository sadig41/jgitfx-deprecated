package net.bbmsoft.jgitfx.models.impl

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.nio.file.Paths
import java.util.Collections
import java.util.List
import java.util.function.Consumer
import java.util.function.Function
import net.bbmsoft.bbm.utils.Persistor
import java.io.Reader
import com.google.gson.reflect.TypeToken

class JsonFilePersistor implements Persistor<List<File>> {

	final Function<File, Appendable> appendableSupplier
	final Function<File, Reader> readerSupplier

	final Gson gson
	final File reposFile

	new() {
		this([new FileWriter(it)], [new FileReader(it)], true, null)
	}

	// just for testing
	new(File reposFile, boolean prettyPrint, Function<File, Appendable> appendableSupplier) {
		this(appendableSupplier, [new FileReader(it)], prettyPrint, reposFile)
	}

	// just for testing
	new(File reposFile, Function<File, Reader> readerSupplier) {
		this([new FileWriter(it)], readerSupplier, false, reposFile)
	}

	// just for testing
	private new(Function<File, Appendable> appendableSupplier, Function<File, Reader> readerSupplier,
		boolean prettyPrint, File reposFile) {

		this.appendableSupplier = appendableSupplier
		this.readerSupplier = readerSupplier

		val builder = new GsonBuilder
		if(prettyPrint) builder.setPrettyPrinting
		this.gson = builder.create

		if (reposFile !== null) {
			this.reposFile = reposFile
		} else {
			val home = System.getProperty('user.home')
			val dir = Paths.get(home).resolve('.jgitfx')
			val jgitfxDir = dir.toFile
			if (!jgitfxDir.exists) {
				jgitfxDir.mkdirs
			}

			val file = dir.resolve('repositories')
			this.reposFile = file.toFile
		}
	}

	override load(Consumer<List<File>> loadCallback, ExceptionHandler exceptionHandler) {

		if (!this.reposFile.exists) {
			loadCallback.accept(Collections.emptyList)
		} else {
			val fileType = new TypeToken<List<File>>() {}.getType();
			val List<File> list = this.gson.fromJson(this.readerSupplier.apply(this.reposFile), fileType)
			loadCallback.accept(list)
		}
	}

	override persist(List<File> object, ExceptionHandler exceptionHandler) {
		this.gson.toJson(object, this.appendableSupplier.apply(this.reposFile))
	}

}
