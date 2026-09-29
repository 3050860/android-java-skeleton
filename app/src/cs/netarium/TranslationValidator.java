package cs.netarium;

import android.content.Context;
import android.content.res.Resources;
import android.icu.text.PluralRules;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

import dev.b3nedikt.restring.PluralKeyword;

/**
 * Проверяет переводы перед тем, как отдать их в Restring. Сверяет их с исходными
 * строками из ресурсов приложения (values/). Плохой ключ удаляется из переводов:
 * для него останется строка из ресурсов APK, а в лог пишется предупреждение.
 *
 * Зачем: Restring форматирует строку через String.format без try/catch, поэтому
 * ошибка в плейсхолдере перевода роняет приложение; а если в переводе нет формы other,
 * Restring молча показывает строку из ресурсов, то есть другой язык.
 */
final class TranslationValidator {

	private static final String TAG = "TranslationValidator";

	private static final String NO_SUCH_RESOURCE = "no such resource in values/";

	private final Resources res;
	private final String packageName;
	/** Имя файла, нужно только для сообщений в логе. */
	private final String fileName;

	TranslationValidator(Context context, String fileName) {
		// Исходные строки берём из ресурсов приложения: они не обёрнуты Restring
		this.res = context.getApplicationContext().getResources();
		this.packageName = context.getPackageName();
		this.fileName = fileName;
	}

	/** Удаляет из translations ключи, которые не прошли проверку. */
	void validate(Translations translations) {
		removeInvalid("strings", translations.strings, this::checkString);
		removeInvalid("arrays", translations.arrays, this::checkArray);
		removeInvalid("plurals", translations.plurals, this::checkPlural);
	}

	/**
	 * Удаляет записи, для которых check вернул описание проблемы, и пишет его в лог.
	 * check получает ключ и перевод, возвращает описание проблемы или null.
	 */
	private <V> void removeInvalid(String section, Map<String, V> entries,
			BiFunction<String, V, String> check) {
		entries.entrySet().removeIf(entry -> {
			String problem = check.apply(entry.getKey(), entry.getValue());
			if (problem == null) {
				return false;
			}
			Log.w(TAG, fileName + ": " + section + "/" + entry.getKey() + " skipped (" + problem + ")");
			return true;
		});
	}

	private String checkString(String key, CharSequence translation) {
		int id = res.getIdentifier(key, "string", packageName);
		if (id == 0) {
			return NO_SUCH_RESOURCE;
		}
		return FormatPlaceholders.parse(res.getString(id))
				.checkTranslation(translation.toString(), false);
	}

	private String checkArray(String key, CharSequence[] translation) {
		int id = res.getIdentifier(key, "array", packageName);
		if (id == 0) {
			return NO_SUCH_RESOURCE;
		}
		int expectedLength = res.getStringArray(id).length;
		if (translation.length != expectedLength) {
			return "length " + translation.length + ", expected " + expectedLength;
		}
		return null;
	}

	private String checkPlural(String key, Map<PluralKeyword, CharSequence> forms) {
		int id = res.getIdentifier(key, "plurals", packageName);
		if (id == 0) {
			return NO_SUCH_RESOURCE;
		}
		if (!forms.containsKey(PluralKeyword.OTHER)) {
			return "no \"other\" form";
		}
		return checkPluralForms(sourcePluralPlaceholders(id), forms);
	}

	/**
	 * Форма может не содержать число (в английском "one" можно написать "One movie"),
	 * но не может содержать плейсхолдеры, которых нет в исходнике.
	 */
	private static String checkPluralForms(FormatPlaceholders expected,
			Map<PluralKeyword, CharSequence> forms) {
		for (Map.Entry<PluralKeyword, CharSequence> form : forms.entrySet()) {
			String problem = expected.checkTranslation(form.getValue().toString(), true);
			if (problem != null) {
				return PluralCategories.toCategory(form.getKey()) + ": " + problem;
			}
		}
		return null;
	}

	/**
	 * Плейсхолдеры всех форм исходного plural. У Android нет способа получить форму
	 * по имени (one, few), только по количеству. Поэтому для каждой формы берём число-пример
	 * из ICU по правилам языка ресурсов — по тем же правилам форму выбирает getQuantityString.
	 * Формы, которые целым числом не выбрать (other в русском — только дробные), пропускаются:
	 * getQuantityString их тоже никогда не покажет.
	 */
	private FormatPlaceholders sourcePluralPlaceholders(int id) {
		PluralRules rules = PluralRules.forLocale(res.getConfiguration().getLocales().get(0));
		List<String> forms = new ArrayList<>();
		for (String keyword : rules.getKeywords()) {
			Collection<Double> samples = rules.getSamples(keyword);
			if (samples != null && !samples.isEmpty()) {
				int quantity = samples.iterator().next().intValue();
				forms.add(res.getQuantityString(id, quantity));
			}
		}
		return FormatPlaceholders.parseAll(forms);
	}
}
