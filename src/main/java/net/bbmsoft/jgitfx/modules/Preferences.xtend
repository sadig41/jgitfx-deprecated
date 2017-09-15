package net.bbmsoft.jgitfx.modules

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonIOException
import com.google.gson.JsonParseException
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import com.google.gson.JsonSyntaxException
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.io.IOException
import java.lang.reflect.Type
import javafx.beans.property.BooleanProperty
import javafx.beans.property.SimpleBooleanProperty
import net.bbmsoft.fxtended.annotations.binding.BindableProperty

import static extension net.bbmsoft.fxtended.extensions.BindingOperatorExtensions.*
import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleObjectProperty
import com.google.gson.reflect.TypeToken

class Preferences {

	final static Gson gson = buildGson

	transient File persistenceFile

	@BindableProperty boolean switchToRepositoryOverview = true
	@BindableProperty boolean maximized = false
	@BindableProperty File lastOpened = null

	new(File persistenceFile) {
		this.init(persistenceFile)
	}

	new() {
	}
	
	private def init(File persistenceFile) {
		this.persistenceFile = persistenceFile
		this.registerListeners
	}
	
	private def registerListeners() {
		this.switchToRepositoryOverviewProperty > [persist]
		this.maximizedProperty > [persist]
		this.lastOpenedProperty > [persist]
	}

	def toJson() {
		gson.toJson(this)
	}

	def static Preferences loadFromFile(File persistenceFile) throws JsonSyntaxException, JsonIOException {
		if (persistenceFile.exists) {
			gson.fromJson(new FileReader(persistenceFile), Preferences) => [init(persistenceFile)]
		} else {
			new Preferences(persistenceFile) => [persistToFile(persistenceFile)]
		}
	}

	private def persist() {
		this.persistenceFile?.persistToFile
	}

	def persistToFile(File persistenceFile) throws JsonIOException, IOException {
		val fileWriter = new FileWriter(persistenceFile)
		try {
			gson.toJson(this, fileWriter)
		} finally {
			fileWriter.close
		}
	}

	private static def buildGson() {
		(new GsonBuilder => [
			val filePropertyType = new TypeToken<ObjectProperty<File>>() {
			}.getType();
			registerTypeAdapter(BooleanProperty, new BooleanPropertyAdapter)
			registerTypeAdapter(filePropertyType, new FilePropertyAdaptor)
			setPrettyPrinting
		]).create
	}

	static class BooleanPropertyAdapter implements JsonSerializer<BooleanProperty>, JsonDeserializer<BooleanProperty> {

		override serialize(BooleanProperty src, Type typeOfSrc, JsonSerializationContext context) {
			new JsonPrimitive(src.get)
		}

		override deserialize(JsonElement json, Type typeOfT,
			JsonDeserializationContext context) throws JsonParseException {
			val value = try {
					json.asBoolean
				} catch (Throwable th) {
					false
				}
			new SimpleBooleanProperty(value)
		}

	}
	
	static class FilePropertyAdaptor implements JsonSerializer<ObjectProperty<File>>, JsonDeserializer<ObjectProperty<File>> {

		override serialize(ObjectProperty<File> src, Type typeOfSrc, JsonSerializationContext context) {
			new JsonPrimitive(src.get.absolutePath)
		}

		override deserialize(JsonElement json, Type typeOfT,
			JsonDeserializationContext context) throws JsonParseException {
			val value = try {
					new File(json.asString)
				} catch (Throwable th) {
					null
				}
			new SimpleObjectProperty<File>(value)
		}

	}
}
