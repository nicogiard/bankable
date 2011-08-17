package templates;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Iterator;

import play.Logger;
import play.templates.BaseTemplate.RawData;
import play.templates.JavaExtensions;

public class BankableExtensions extends JavaExtensions {

	/**
	 * Concatenate items of a collection as a string separated with <tt>separator</tt>. Use the property of the object to concatenate
	 */
	public static String join(Collection items, String property, String separator) {
		if (items == null) {
			return "";
		}
		StringBuffer sb = new StringBuffer();
		Iterator ite = items.iterator();
		int i = 0;
		while (ite.hasNext()) {
			if (i++ > 0) {
				sb.append(separator);
			}

			String text = null;
			Object obj = ite.next();

			try {
				Field field = obj.getClass().getField(property);
				text = field.get(obj).toString();
			} catch (Exception e) {
				Logger.error("An error occurred : %s", e.getMessage());
			}

			sb.append(text != null ? text : obj.toString());
		}
		return sb.toString();
	}

	public static RawData joinWithRaw(Collection items, String property, String separator) {
		if (items == null) {
			return new RawData("");
		}
		StringBuffer sb = new StringBuffer();
		Iterator ite = items.iterator();
		int i = 0;
		while (ite.hasNext()) {
			if (i++ > 0) {
				sb.append(separator);
			}

			String text = null;
			Object obj = ite.next();

			try {
				Field field = obj.getClass().getField(property);
				text = field.get(obj).toString();
			} catch (Exception e) {
				Logger.error("An error occurred : %s", e.getMessage());
			}

			sb.append(text != null ? text : obj.toString());
		}
		return new RawData(sb.toString());
	}
}
