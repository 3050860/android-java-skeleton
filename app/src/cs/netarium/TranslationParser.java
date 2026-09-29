package cs.netarium;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import dev.b3nedikt.restring.PluralKeyword;

/**
 * Разбирает файл перевода вида {"strings": {...}, "arrays": {...}, "plurals": {...}}.
 * Неизвестные секции, значения не того типа и неизвестные формы plurals
 * пропускаются с предупреждением в лог.
 */
final class TranslationParser {

	private static final String TAG = "TranslationParser";

	private static final String SECTION_STRINGS = "strings";
	private static final String SECTION_ARRAYS = "arrays";
	private static final String SECTION_PLURALS = "plurals";

	/** Имя файла, нужно только для сообщений в логе. */
	private final String fileName;

	TranslationParser(String fileName) {
		this.fileName = fileName;
	}

	/**
	 * @param json содержимое файла
	 * @throws JSONException если текст не является JSON-объектом
	 */
	Translations parse(String json) throws JSONException {
		JSONObject root = new JSONObject(json);
		Translations result = new Translations();

		Iterator<String> names = root.keys();
		while (names.hasNext()) {
			String name = names.next();
			JSONObject section = root.optJSONObject(name);
			if (SECTION_STRINGS.equals(name) && section != null) {
				parseStrings(section, result.strings);
			} else if (SECTION_ARRAYS.equals(name) && section != null) {
				parseArrays(section, result.arrays);
			} else if (SECTION_PLURALS.equals(name) && section != null) {
				parsePlurals(section, result.plurals);
			} else {
				// Неизвестная секция или секция не того типа: пропускаем
				Log.w(TAG, fileName + ": section \"" + name + "\" skipped (unknown or not an object)");
			}
		}
		return result;
	}

	private void parseStrings(JSONObject section, Map<String, CharSequence> out) {
		Iterator<String> keys = section.keys();
		while (keys.hasNext()) {
			String key = keys.next();
			Object value = section.opt(key);
			if (value instanceof String) {
				out.put(key, (String) value);
			} else {
				Log.w(TAG, fileName + ": strings/" + key + " skipped (not a string)");
			}
		}
	}

	private void parseArrays(JSONObject section, Map<String, CharSequence[]> out) {
		Iterator<String> keys = section.keys();
		while (keys.hasNext()) {
			String key = keys.next();
			JSONArray items = section.optJSONArray(key);
			CharSequence[] array = items == null ? null : toStringArray(items);
			if (array != null) {
				out.put(key, array);
			} else {
				Log.w(TAG, fileName + ": arrays/" + key + " skipped (not an array of strings)");
			}
		}
	}

	/** Возвращает null, если в массиве есть элемент, который не является строкой. */
	private static CharSequence[] toStringArray(JSONArray items) {
		CharSequence[] result = new CharSequence[items.length()];
		for (int i = 0; i < items.length(); i++) {
			Object item = items.opt(i);
			if (!(item instanceof String)) {
				return null;
			}
			result[i] = (String) item;
		}
		return result;
	}

	private void parsePlurals(JSONObject section, Map<String, Map<PluralKeyword, CharSequence>> out) {
		Iterator<String> keys = section.keys();
		while (keys.hasNext()) {
			String key = keys.next();
			JSONObject forms = section.optJSONObject(key);
			if (forms == null) {
				Log.w(TAG, fileName + ": plurals/" + key + " skipped (not an object with forms)");
				continue;
			}
			Map<PluralKeyword, CharSequence> quantityStrings = parsePluralForms(key, forms);
			if (quantityStrings.isEmpty()) {
				Log.w(TAG, fileName + ": plurals/" + key + " skipped (no forms)");
			} else {
				out.put(key, quantityStrings);
			}
		}
	}

	/** Формы одного ключа plurals; неизвестные формы и значения не строкой пропускаются. */
	private Map<PluralKeyword, CharSequence> parsePluralForms(String key, JSONObject forms) {
		Map<PluralKeyword, CharSequence> result = new HashMap<>();
		Iterator<String> categories = forms.keys();
		while (categories.hasNext()) {
			String category = categories.next();
			PluralKeyword keyword = PluralCategories.toKeyword(category);
			Object value = forms.opt(category);
			if (keyword != null && value instanceof String) {
				result.put(keyword, (String) value);
			} else {
				Log.w(TAG, fileName + ": plurals/" + key + "/" + category
						+ " skipped (unknown form or not a string)");
			}
		}
		return result;
	}
}
