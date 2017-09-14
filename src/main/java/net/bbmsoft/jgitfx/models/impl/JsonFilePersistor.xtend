package net.bbmsoft.jgitfx.models.impl

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.io.Flushable
import java.io.Reader
import java.lang.reflect.Type
import java.util.Collections
import java.util.List
import java.util.function.Consumer
import java.util.function.Function
import net.bbmsoft.bbm.utils.Persistor
import net.bbmsoft.bbm.utils.Persistor.ExceptionHandler
import net.bbmsoft.jgitfx.modules.AppDirectoryProvider

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

		val builder = new GsonBuilder => [
			if(prettyPrint) setPrettyPrinting
			registerTypeAdapter(File, new FileAdaptor)
		]
		this.gson = builder.create

		if (reposFile !== null) {
			this.reposFile = reposFile
		} else {
			this.reposFile = AppDirectoryProvider.getFilePathFromAppDirectory('repositories.json')
		}
	}

	override load(Consumer<List<File>> loadCallback, ExceptionHandler exceptionHandler) {

		if (!this.reposFile.exists) {
			loadCallback.accept(Collections.emptyList)
		} else {
			val fileType = new TypeToken<List<File>>() {
			}.getType();
			val List<File> list = this.gson.fromJson(this.readerSupplier.apply(this.reposFile), fileType)
			loadCallback.accept(list ?: Collections.emptyList)
		}
	}

	override persist(List<File> object, ExceptionHandler exceptionHandler) {
		val appenable = this.appendableSupplier.apply(this.reposFile)
		this.gson.toJson(object, appenable)
		if(appenable instanceof Flushable) {
			appenable.flush
		}
	}

	static class FileAdaptor implements JsonSerializer<File>, JsonDeserializer<File> {

		override serialize(File src, Type typeOfSrc, JsonSerializationContext context) {
			new JsonPrimitive(src?.absolutePath ?: '')
		}

		override deserialize(JsonElement json, Type typeOfT,
			JsonDeserializationContext context) throws JsonParseException {
			new File(json.asString)
		}

	}

}
