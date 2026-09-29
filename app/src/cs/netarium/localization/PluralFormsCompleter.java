package cs.netarium.localization;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import dev.b3nedikt.restring.PluralKeyword;

/**
 * Дополняет недостающие формы plurals формой other. Например, для ru нужны one/few/many/other,
 * и если перевод прислал только one и other, few и many получат текст other.
 * Так на экране будет этот язык, пусть и с неточной формой, а не строка из ресурсов APK.
 * Вызывается после TranslationValidator: к этому моменту у каждого ключа есть форма other.
 */
final class PluralFormsCompleter {

	private static final String TAG = "PluralFormsCompleter";

	private final Set<PluralKeyword> requiredForms;
	/** Имя файла, нужно только для сообщений в логе. */
	private final String fileName;

	PluralFormsCompleter(Locale locale, String fileName) {
		this.requiredForms = PluralCategories.requiredForms(locale);
		this.fileName = fileName;
	}

	void complete(Translations translations) {
		for (Map.Entry<String, Map<PluralKeyword, CharSequence>> entry : translations.plurals.entrySet()) {
			completeForms(entry.getKey(), entry.getValue());
		}
	}

	private void completeForms(String key, Map<PluralKeyword, CharSequence> forms) {
		CharSequence other = forms.get(PluralKeyword.OTHER);
		if (other == null) {
			// Такие ключи отсеивает TranslationValidator
			return;
		}
		List<String> filled = new ArrayList<>();
		for (PluralKeyword keyword : requiredForms) {
			if (!forms.containsKey(keyword)) {
				forms.put(keyword, other);
				filled.add(PluralCategories.toCategory(keyword));
			}
		}
		if (!filled.isEmpty()) {
			Log.w(TAG, fileName + ": plurals/" + key + " forms " + filled + " filled from \"other\"");
		}
	}
}
