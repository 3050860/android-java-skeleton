package cs.netarium.localization;

import android.content.Context;
import android.util.Log;

import org.json.JSONException;

import java.io.IOException;
import java.util.Locale;

import dev.b3nedikt.restring.Restring;

public class TranslationHelper {

	private static final String TAG = "LANGLOOP";

	public static void loadLanguage(Context context, String languageCode) {

		// Встроенные языки (ru, en) берутся из ресурсов APK (res/values, res/values-en),
		// файлов переводов для них нет, в Restring ничего не загружаем.
		if (LanguageCatalog.isBundled(languageCode)) {
			return;
		}

		Locale locale = Language.toLocale(languageCode);
		String fileName = TranslationSource.translationsFile(languageCode);

		String json;
		try {
			json = new TranslationSource(context).translations(languageCode);
		} catch (IOException e) {
			// Файла нет: язык не загружаем, показываются строки из ресурсов APK
			Log.w(TAG, "~~~ loadLanguage(" + languageCode + ") " + fileName + " not found", e);
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
}
