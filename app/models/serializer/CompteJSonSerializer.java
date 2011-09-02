package models.serializer;

import java.lang.reflect.Type;

import models.Compte;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class CompteJSonSerializer implements JsonSerializer<Compte> {

	public static CompteJSonSerializer instance;

	private CompteJSonSerializer() {
	}

	public static CompteJSonSerializer get() {
		if (instance == null) {
			instance = new CompteJSonSerializer();
		}
		return instance;
	}

	public JsonElement serialize(Compte compte, Type type, JsonSerializationContext jsonSerializationContext) {
		JsonObject obj = new JsonObject();
		obj.addProperty("nom", compte.nom);
		obj.addProperty("solde", compte.solde);
		obj.addProperty("numero", compte.numero);
		obj.addProperty("etablissement", compte.etablissement);
		return obj;
	}
}
