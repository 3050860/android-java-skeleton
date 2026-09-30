package cs.netarium.localization;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Разбирает список языков с сервера вида {"languages": [{"code": "es"}, {"code": "cs", "name": "Čeština"}]}.
 * code — тег BCP 47 через дефис, обязателен; name необязателен (без него название берётся из ICU).
 * Записи с ошибками и неизвестные поля пропускаются с предупреждением в лог.
 */
final class LanguageListParser {

	private static final String TAG = "LanguageListParser";

	private static final String FIELD_LANGUAGES = "languages";
	private static final String FIELD_CODE = "code";
	private static final String FIELD_NAME = "name";

	/** Имя файла, нужно только для сообщений в логе. */
	private final String fileName;

	LanguageListParser(String fileName) {
		this.fileName = fileName;
	}

	/**
	 * @param json содержимое файла
	 * @throws JSONException если текст не является JSON-объектом
	 */
	List<Language> parse(String json) throws JSONException {
		JSONObject root = new JSONObject(json);
		warnUnknownFields(root);

		List<Language> result = new ArrayList<>();
		JSONArray entries = root.optJSONArray(FIELD_LANGUAGES);
		if (entries == null) {
			Log.w(TAG, fileName + ": no \"" + FIELD_LANGUAGES + "\" array");
			return result;
		}
		for (int i = 0; i < entries.length(); i++) {
			Language language = parseEntry(i, entries.optJSONObject(i));
			if (language != null) {
				result.add(language);
			}
		}
		return result;
	}

	/** Язык из одной записи списка или null, если запись не годится. */
	private Language parseEntry(int index, JSONObject entry) {
		String where = fileName + ": languages[" + index + "]";
		if (entry == null) {
			Log.w(TAG, where + " skipped (not an object)");
			return null;
		}
		Object code = entry.opt(FIELD_CODE);
		// "zh_CN" с подчёркиванием — не BCP 47: forLanguageTag вернёт пустой язык
		if (!(code instanceof String) || Language.toLocale((String) code).getLanguage().isEmpty()) {
			Log.w(TAG, where + " skipped (no valid BCP 47 \"code\")");
			return null;
		}
		Object name = entry.opt(FIELD_NAME);
		if (name != null && !(name instanceof String)) {
			Log.w(TAG, where + ": \"name\" ignored (not a string)");
			name = null;
		}
		return Language.of((String) code, (String) name);
	}

	/** Неизвестные поля верхнего уровня не мешают, но о них стоит знать. */
	private void warnUnknownFields(JSONObject root) {
		Iterator<String> names = root.keys();
		while (names.hasNext()) {
			String name = names.next();
			if (!FIELD_LANGUAGES.equals(name)) {
				Log.w(TAG, fileName + ": field \"" + name + "\" skipped (unknown)");
			}
		}
	}
}
