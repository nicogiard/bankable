package utils;

import models.Operation;
import models.Tag;

import org.apache.commons.lang.StringUtils;

public class OperationUtils {

	public static void extractTags(Operation operation, String tags) {
		if (StringUtils.isNotBlank(tags)) {
			String[] tabTags = tags.split(",");
			for (String stringTag : tabTags) {
				Tag tag = Tag.find("nom=?", stringTag.trim()).first();
				if (tag == null) {
					tag = new Tag();
					tag.nom = stringTag.trim();
					tag.user = operation.compte.user;
					tag.save();
				}
				operation.tags.add(tag);
			}
		}
	}
}
