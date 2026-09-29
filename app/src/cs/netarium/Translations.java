package cs.netarium;

import java.util.HashMap;
import java.util.Map;

import dev.b3nedikt.restring.PluralKeyword;

/**
 * Переводы одного языка: три набора в том виде, в каком их принимает Restring
 * (putStrings, putStringArrays, putQuantityStrings).
 * Создаёт TranslationParser, проверяет TranslationValidator, формы plurals дополняет
 * PluralFormsCompleter, в Restring кладёт TranslationHelper.
 */
final class Translations {
	final Map<String, CharSequence> strings = new HashMap<>();
	final Map<String, CharSequence[]> arrays = new HashMap<>();
	final Map<String, Map<PluralKeyword, CharSequence>> plurals = new HashMap<>();
}
