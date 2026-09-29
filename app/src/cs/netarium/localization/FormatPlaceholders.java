package cs.netarium.localization;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Плейсхолдеры строки формата Java (%s, %1$d, %2$.1f и т.п.), которые подставляет String.format,
 * и проверка перевода по исходной строке.
 * Класс не зависит от Android, поэтому его можно проверять обычными JVM-тестами.
 */
final class FormatPlaceholders {

	/**
	 * Плейсхолдер: %[номер$][флаги][ширина][.точность]тип.
	 * Группы: 1 — номер, 2 — флаги, 3 — ширина, 4 — точность, 5 — тип.
	 * %% (знак процента) и %n (перевод строки) аргументов не требуют.
	 */
	private static final Pattern PLACEHOLDER =
			Pattern.compile("%(?:(\\d{1,3})\\$)?([-#+ 0,(]*)(\\d*)(?:\\.(\\d+))?([a-zA-Z%])");

	/**
	 * Аргументы в виде "номер:флаги,ширина,точность,тип", например "1:s", "2:.1f".
	 * Флаги сравниваются тоже: иначе "50 % de" в переводе совпало бы с "%d" из исходника,
	 * хотя Java читает "% d" как число с флагом «пробел» и выводит мусор.
	 */
	private final Set<String> args;
	/** Есть плейсхолдеры без номера (%s вместо %1$s). */
	private final boolean hasUnnumbered;
	/** Есть % без понятного продолжения: String.format на такой строке упадёт. */
	private final boolean malformed;

	private FormatPlaceholders(Set<String> args, boolean hasUnnumbered, boolean malformed) {
		this.args = args;
		this.hasUnnumbered = hasUnnumbered;
		this.malformed = malformed;
	}

	static FormatPlaceholders parse(String text) {
		Set<String> args = new HashSet<>();
		boolean hasUnnumbered = false;
		Matcher matcher = PLACEHOLDER.matcher(text);
		int unnumberedIndex = 0;
		int position = 0;
		while (true) {
			int percent = text.indexOf('%', position);
			if (percent < 0) {
				break;
			}
			matcher.region(percent, text.length());
			if (!matcher.lookingAt()) {
				return new FormatPlaceholders(args, hasUnnumbered, true);
			}
			String conversion = matcher.group(5);
			if (!"%".equals(conversion) && !"n".equals(conversion)) {
				int index;
				if (matcher.group(1) != null) {
					index = Integer.parseInt(matcher.group(1));
				} else {
					index = ++unnumberedIndex;
					hasUnnumbered = true;
				}
				args.add(toArg(index, matcher));
			}
			position = matcher.end();
		}
		return new FormatPlaceholders(args, hasUnnumbered, false);
	}

	/** Объединение аргументов нескольких строк: нужно для всех форм одного plural. */
	static FormatPlaceholders parseAll(List<String> texts) {
		Set<String> args = new HashSet<>();
		for (String text : texts) {
			args.addAll(parse(text).args);
		}
		return new FormatPlaceholders(args, false, false);
	}

	/**
	 * Проверяет перевод по этим (исходным) плейсхолдерам.
	 * Возвращает описание проблемы или null, если всё в порядке.
	 *
	 * @param allowMissing true — перевод может не использовать часть аргументов (формы plurals),
	 *                     false — набор аргументов должен совпадать с исходником (строки)
	 */
	String checkTranslation(String translation, boolean allowMissing) {
		if (args.isEmpty()) {
			// У исходной строки нет параметров: String.format к ней не применяется,
			// поэтому % в переводе — обычный символ
			return null;
		}
		FormatPlaceholders actual = parse(translation);
		if (actual.malformed) {
			return "broken placeholder, use %% for a percent sign";
		}
		if (args.size() > 1 && actual.hasUnnumbered) {
			return "placeholders must be numbered: %1$s, %2$d";
		}
		boolean ok = allowMissing ? args.containsAll(actual.args) : args.equals(actual.args);
		return ok ? null : "placeholders " + actual.args + " do not match " + args;
	}

	/** %s и %S — один и тот же аргумент, разница только в регистре вывода. */
	private static String toArg(int index, Matcher matcher) {
		String precision = matcher.group(4) != null ? "." + matcher.group(4) : "";
		return index + ":" + matcher.group(2) + matcher.group(3) + precision
				+ matcher.group(5).toLowerCase(Locale.ROOT);
	}
}
