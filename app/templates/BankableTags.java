package templates;

import groovy.lang.Closure;

import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import play.data.validation.Validation;
import play.mvc.Scope.Flash;
import play.templates.FastTags;
import play.templates.GroovyTemplate.ExecutableTemplate;
import play.templates.JavaExtensions;

@FastTags.Namespace("bankable")
public class BankableTags extends FastTags {
	/**
	 * The field tag is a helper, based on the spirit of Don't Repeat Yourself.
	 * 
	 * @param args
	 *            tag attributes
	 * @param body
	 *            tag inner body
	 * @param out
	 *            the output writer
	 * @param template
	 *            enclosing template
	 * @param fromLine
	 *            template line number where the tag is defined
	 */
	public static void _textField(Map<?, ?> args, Closure body, PrintWriter out, ExecutableTemplate template, int fromLine) {
		Map<String, Object> field = new HashMap<String, Object>();
		String _arg = args.get("arg").toString();
		field.put("name", _arg);
		field.put("id", _arg.replace('.', '_'));
		field.put("flash", Flash.current().get(_arg));
		field.put("flashArray", field.get("flash") != null && !StringUtils.isEmpty(field.get("flash").toString()) ? field.get("flash").toString().split(",") : new String[0]);
		field.put("error", Validation.error(_arg));
		field.put("errorClass", field.get("error") != null ? "hasError" : "");
		String[] pieces = _arg.split("\\.");
		Object obj = body.getProperty(pieces[0]);
		if (obj != null) {
			if (pieces.length > 1) {
				for (int i = 1; i < pieces.length; i++) {
					try {
						Field f = obj.getClass().getField(pieces[i]);
						if (i == (pieces.length - 1)) {
							try {
								Method getter = obj.getClass().getMethod("get" + JavaExtensions.capFirst(f.getName()));
								field.put("value", getter.invoke(obj, new Object[0]));
							} catch (NoSuchMethodException e) {
								field.put("value", f.get(obj).toString());
							}
						} else {
							obj = f.get(obj);
						}
					} catch (Exception e) {
						// if there is a problem reading the field we dont set any value
					}
				}
			} else {
				field.put("value", obj);
			}
		}
		body.setProperty("field", field);
		body.call();
	}
}
