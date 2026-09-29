package cs.netarium;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
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

	/** Результат разбора: три набора в том виде, в каком их принимает Restring. */
	static final class Translations {
		final Map<String, CharSequence> strings = new HashMap<>();
		final Map<String, CharSequence[]> arrays = new HashMap<>();
		final Map<String, Map<PluralKeyword, CharSequence>> plurals = new HashMap<>();
	}

	private TranslationParser() {
	}

	/**
	 * @param fileName имя файла, нужно только для сообщений в логе
	 * @param json     содержимое файла
	 * @throws JSONException если текст не является JSON-объектом
	 */
	static Translations parse(String fileName, String json) throws JSONException {
		JSONObject root = new JSONObject(json);
		Translations result = new Translations();

		Iterator<String> names = root.keys();
		while (names.hasNext()) {
			String name = names.next();
			JSONObject section = root.optJSONObject(name);
			if (SECTION_STRINGS.equals(name) && section != null) {
				parseStrings(fileName, section, result.strings);
			} else if (SECTION_ARRAYS.equals(name) && section != null) {
				parseArrays(fileName, section, result.arrays);
			} else if (SECTION_PLURALS.equals(name) && section != null) {
				parsePlurals(fileName, section, result.plurals);
			} else {
				// Неизвестная секция или секция не того типа: пропускаем
				Log.w(TAG, fileName + ": section \"" + name + "\" skipped (unknown or not an object)");
			}
		}
		return result;
	}

	private static void parseStrings(String fileName, JSONObject section,
			Map<String, CharSequence> out) {
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

	private static void parseArrays(String fileName, JSONObject section,
			Map<String, CharSequence[]> out) {
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

	private static void parsePlurals(String fileName, JSONObject section,
			Map<String, Map<PluralKeyword, CharSequence>> out) {
		Iterator<String> keys = section.keys();
		while (keys.hasNext()) {
			String key = keys.next();
			JSONObject forms = section.optJSONObject(key);
			if (forms == null) {
				Log.w(TAG, fileName + ": plurals/" + key + " skipped (not an object with forms)");
				continue;
			}

			Map<PluralKeyword, CharSequence> quantityStrings = new HashMap<>();
			Iterator<String> categories = forms.keys();
			while (categories.hasNext()) {
				String category = categories.next();
				PluralKeyword keyword = toPluralKeyword(category);
				Object value = forms.opt(category);
				if (keyword != null && value instanceof String) {
					quantityStrings.put(keyword, (String) value);
				} else {
					Log.w(TAG, fileName + ": plurals/" + key + "/" + category
							+ " skipped (unknown form or not a string)");
				}
			}

			if (quantityStrings.isEmpty()) {
				Log.w(TAG, fileName + ": plurals/" + key + " skipped (no forms)");
			} else {
				out.put(key, quantityStrings);
			}
		}
	}

	/** "one" -> PluralKeyword.ONE; для неизвестной формы возвращает null. */
	private static PluralKeyword toPluralKeyword(String category) {
		for (PluralKeyword keyword : PluralKeyword.values()) {
			if (keyword.name().toLowerCase(Locale.ROOT).equals(category)) {
				return keyword;
			}
		}
		return null;
	}
}
