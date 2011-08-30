package models.serializer;

import java.lang.reflect.Type;

import models.Tag;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class TagJSonSerializer implements JsonSerializer<Tag> {

	public static TagJSonSerializer instance;

	private TagJSonSerializer() {
	}

	public static TagJSonSerializer get() {
		if (instance == null) {
			instance = new TagJSonSerializer();
		}
		return instance;
	}

	public JsonElement serialize(Tag tag, Type type, JsonSerializationContext jsonSerializationContext) {
		JsonObject obj = new JsonObject();
		obj.addProperty("nom", tag.nom);
		return obj;
	}
}
