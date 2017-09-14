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

class Preferences {

	final static Gson gson = buildGson

	final File persistenceFile

	@BindableProperty boolean switchToRepositoryOverview = true

	new(File persistenceFile) {
		
		this.persistenceFile = persistenceFile

		this.switchToRepositoryOverviewProperty > [persistToFile(this.persistenceFile)]
	}

	def toJson() {
		gson.toJson(this)
	}

	def static Preferences loadFromFile(File persistenceFile) throws JsonSyntaxException, JsonIOException {
		if (persistenceFile.exists) {
			gson.fromJson(new FileReader(persistenceFile), Preferences)
		} else {
			new Preferences(persistenceFile) => [persistToFile(persistenceFile)]
		}
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
			registerTypeAdapter(BooleanProperty, new BooleanPropertyAdapter)
			setPrettyPrinting
		]).create
	}

	static class BooleanPropertyAdapter implements JsonSerializer<BooleanProperty>, JsonDeserializer<BooleanProperty> {

		override serialize(BooleanProperty src, Type typeOfSrc, JsonSerializationContext context) {
			new JsonPrimitive(src.get)
		}

		override deserialize(JsonElement json, Type typeOfT,
			JsonDeserializationContext context) throws JsonParseException {
			new SimpleBooleanProperty(json.asBoolean)
		}

	}
}
