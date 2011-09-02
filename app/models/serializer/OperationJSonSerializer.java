package models.serializer;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;

import models.Operation;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class OperationJSonSerializer implements JsonSerializer<Operation> {

	public static OperationJSonSerializer instance;

	private OperationJSonSerializer() {
	}

	public static OperationJSonSerializer get() {
		if (instance == null) {
			instance = new OperationJSonSerializer();
		}
		return instance;
	}

	public JsonElement serialize(Operation operation, Type type, JsonSerializationContext jsonSerializationContext) {
		JsonObject obj = new JsonObject();
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
		obj.addProperty("date", sdf.format(operation.date));
		obj.addProperty("libelle", operation.libelle);
		obj.addProperty("type", operation.type.name());
		obj.addProperty("montant", operation.montant);
		obj.addProperty("numero", operation.numero);
		obj.addProperty("detail", operation.detail);
		obj.addProperty("etat", operation.etat.name());
		obj.add("tags", jsonSerializationContext.serialize(operation.tags));
		return obj;
	}
}
