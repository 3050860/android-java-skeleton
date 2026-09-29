package cs.netarium.localization;

import android.content.Context;
import android.util.Log;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

import dev.b3nedikt.restring.Restring;

public class TranslationHelper {

	private static final String TAG = "LANGLOOP";

	public static void loadLanguage(Context context, String languageCode) {

		// Для русского и английского не используем динамическую систему переводов
		// (JSON из assets + Restring): для этих языков применяются штатные
		// строковые ресурсы Android (res/values, res/values-en).
		if (isNativeLanguage(languageCode)) {
			return;
		}

		Locale locale = createLocale(languageCode);
		String fileName = languageCode + ".json";

		String json;
		try {
			json = loadJsonFromAssets(context, fileName);
		} catch (IOException e) {
			// Файла нет: язык не загружаем, показываются строки из ресурсов APK
			Log.w(TAG, "~~~ loadLanguage(" + languageCode + ") assets/" + fileName + " not found", e);
			return;
		}

		Translations translations;
		try {
			translations = new TranslationParser(fileName).parse(json);
		} catch (JSONException e) {
			// Файл не разбирается как JSON: язык не загружаем, показываются строки из ресурсов APK
			Log.e(TAG, "~~~ loadLanguage(" + languageCode + ") FAILED: " + fileName + " is not valid JSON", e);
			return;
		}

		// Плохие ключи удаляются: для них останутся строки из ресурсов APK
		new TranslationValidator(context, fileName).validate(translations);
		// Недостающие формы plurals заполняются формой other
		new PluralFormsCompleter(locale, fileName).complete(translations);

		Restring.putStrings(locale, translations.strings);
		Restring.putStringArrays(locale, translations.arrays);
		Restring.putQuantityStrings(locale, translations.plurals);

		Log.d(TAG, "~~~ loadLanguage(" + languageCode + ") locale=" + locale
				+ " strings=" + translations.strings.size()
				+ " arrays=" + translations.arrays.size()
				+ " plurals=" + translations.plurals.size()
				+ " supportedLocales=" + Restring.getStringRepository().getSupportedLocales());
	}

	/**
	 * Возвращает true, если для языка не нужна динамическая система переводов,
	 * т.е. язык русский или английский. Для таких языков используются штатные
	 * ресурсы приложения, а система Restring не инициализируется и не оборачивает
	 * ресурсы/контекст вообще.
	 */
	public static boolean isNativeLanguage(String languageCode) {
		if (languageCode == null || languageCode.isEmpty()) {
			return false;
		}
		// Берём только код языка (без региона), приводим к нижнему регистру
		String lang = languageCode.split("[-_]")[0].toLowerCase(Locale.ROOT);
		return "ru".equals(lang) || "en".equals(lang);
	}

	/**
	 * Код языка -> Locale. Код — тег BCP 47 через дефис: "es", "zh-Hans", "es-419".
	 * forLanguageTag правильно разбирает письменность (Hans) и регион (419),
	 * а не считает вторую часть тега страной.
	 */
	public static Locale createLocale(String languageCode) {
		return Locale.forLanguageTag(languageCode);
	}

	private static String loadJsonFromAssets(Context context, String fileName) throws IOException {
		// Использование try-with-resources автоматически закроет потоки при ошибке или завершении
		try (InputStream is = context.getAssets().open(fileName);
		     BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {

			StringBuilder sb = new StringBuilder();
			String line;
			while ((line = reader.readLine()) != null) {
				sb.append(line);
			}
			return sb.toString();
		}
	}
}
