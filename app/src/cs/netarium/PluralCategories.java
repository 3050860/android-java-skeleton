package cs.netarium;

import android.icu.text.PluralRules;

import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;

import dev.b3nedikt.restring.PluralKeyword;

/**
 * Формы plurals: имена из CLDR ("one", "few", ...), которые используют файлы переводов и ICU,
 * соответствующие им значения PluralKeyword из Restring и то, какие формы нужны языку.
 */
final class PluralCategories {

	private PluralCategories() {
	}

	/** PluralKeyword.ONE -> "one". */
	static String toCategory(PluralKeyword keyword) {
		return keyword.name().toLowerCase(Locale.ROOT);
	}

	/** "one" -> PluralKeyword.ONE; для неизвестной формы возвращает null. */
	static PluralKeyword toKeyword(String category) {
		for (PluralKeyword keyword : PluralKeyword.values()) {
			if (toCategory(keyword).equals(category)) {
				return keyword;
			}
		}
		return null;
	}

	/** Формы, которые нужны языку по правилам ICU: для ru — one/few/many/other, для en — one/other. */
	static Set<PluralKeyword> requiredForms(Locale locale) {
		Set<PluralKeyword> result = EnumSet.noneOf(PluralKeyword.class);
		for (String category : PluralRules.forLocale(locale).getKeywords()) {
			PluralKeyword keyword = toKeyword(category);
			if (keyword != null) {
				result.add(keyword);
			}
		}
		return result;
	}
}
