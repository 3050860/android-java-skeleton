package cs.netarium.localization;

import android.icu.text.DisplayContext;
import android.icu.text.LocaleDisplayNames;

import java.util.Locale;

/** Язык приложения: код (тег BCP 47) и название для списка выбора. */
public final class Language {

	private final String code;
	private final String name;

	private Language(String code, String name) {
		this.code = code;
		this.name = name;
	}

	/**
	 * @param code код языка, тег BCP 47 через дефис: "es", "zh-Hans"
	 * @param name название с сервера; если его нет (null или пусто), берём из ICU
	 *             название языка на нём самом: «Español», «Čeština»
	 */
	static Language of(String code, String name) {
		return new Language(code, name == null || name.isEmpty() ? nativeName(code) : name);
	}

	public String getCode() {
		return code;
	}

	public String getName() {
		return name;
	}

	/**
	 * Код языка -> Locale. Код — тег BCP 47 через дефис: "es", "zh-Hans", "es-419".
	 * forLanguageTag правильно разбирает письменность (Hans) и регион (419),
	 * а не считает вторую часть тега страной.
	 */
	public static Locale toLocale(String code) {
		return Locale.forLanguageTag(code);
	}

	/**
	 * Название языка на нём самом, с заглавной буквы, как для пункта меню:
	 * «Русский», «English», «Čeština». Так же строят список языков системные настройки Android.
	 */
	private static String nativeName(String code) {
		Locale locale = toLocale(code);
		return LocaleDisplayNames.getInstance(locale, DisplayContext.CAPITALIZATION_FOR_UI_LIST_OR_MENU)
				.localeDisplayName(locale);
	}
}
