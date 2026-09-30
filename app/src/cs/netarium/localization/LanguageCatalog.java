package cs.netarium.localization;

import android.content.Context;
import android.util.Log;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Какие языки есть в приложении: встроенные (их переводы лежат в APK, в values/ и values-en/)
 * плюс языки с сервера (languages.json).
 */
public final class LanguageCatalog {

	private static final String TAG = "LanguageCatalog";

	/**
	 * Коды встроенных языков. Хранятся в коде, а не в ресурсах: isBundled() вызывается
	 * и из MainActivity.attachBaseContext(), когда у активити ещё нет доступа к ресурсам.
	 */
	private static final List<String> BUNDLED_CODES = Collections.unmodifiableList(Arrays.asList("ru", "en"));

	private LanguageCatalog() {
	}

	/** true, если переводы языка лежат в APK: "ru", "ru-RU", "en-XA" -> true; "es" -> false. */
	public static boolean isBundled(String languageCode) {
		if (languageCode == null || languageCode.isEmpty()) {
			return false;
		}
		return BUNDLED_CODES.contains(Language.toLocale(languageCode).getLanguage());
	}

	/**
	 * Все языки для выбора: сначала встроенные, потом с сервера.
	 * Если список с сервера не прочитался, остаются только встроенные.
	 */
	public static List<Language> all(Context context) {
		List<Language> result = new ArrayList<>();
		for (String code : BUNDLED_CODES) {
			result.add(Language.of(code, null));
		}
		for (Language language : remote(context)) {
			if (isBundled(language.getCode())) {
				Log.w(TAG, TranslationSource.LANGUAGE_LIST_FILE + ": \"" + language.getCode()
						+ "\" skipped (bundled language)");
				continue;
			}
			result.add(language);
		}
		return result;
	}

	private static List<Language> remote(Context context) {
		String json;
		try {
			json = new TranslationSource(context).languageList();
		} catch (IOException e) {
			Log.w(TAG, TranslationSource.LANGUAGE_LIST_FILE + " not found, only bundled languages", e);
			return Collections.emptyList();
		}
		try {
			return new LanguageListParser(TranslationSource.LANGUAGE_LIST_FILE).parse(json);
		} catch (JSONException e) {
			Log.e(TAG, TranslationSource.LANGUAGE_LIST_FILE + " is not valid JSON, only bundled languages", e);
			return Collections.emptyList();
		}
	}
}
