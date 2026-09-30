package cs.netarium.localization;

import android.icu.text.CompactDecimalFormat;
import android.icu.text.DateFormat;
import android.icu.text.MeasureFormat;
import android.icu.text.NumberFormat;
import android.icu.text.RelativeDateTimeFormatter;
import android.icu.text.RelativeDateTimeFormatter.RelativeDateTimeUnit;
import android.icu.util.Measure;
import android.icu.util.MeasureUnit;

import java.util.Date;
import java.util.Locale;

/**
 * Даты и числа на выбранном в приложении языке через ICU.
 * Локаль передаётся явно: Locale.getDefault() у нас — язык устройства, а не выбранный.
 *
 * Даты заданы «скелетами» ICU: скелет перечисляет только поля (MMMM — месяц словом,
 * d — день, y — год), а порядок полей, предлоги, падежи и знаки препинания
 * для каждого языка ICU подставляет сам: «29 сентября», «September 29», «9月29日».
 *
 * Для чисел разделители разрядов и дробной части, сокращения и единицы измерения
 * ICU тоже берёт из данных языка: «1 234 567,891», «1,234,567.891», «1,2 млн», «123万».
 */
public final class LocaleFormats {

	private static final String SKELETON_DAY_MONTH = "MMMMd";
	private static final String SKELETON_FULL_DATE = "yMMMMEEEEd";
	/** j — 12 или 24 часа, как принято в языке. */
	private static final String SKELETON_TIME = "jm";
	/** L — месяц в начальной форме («сентябрь»), а M в дате даёт форму «29 сентября». */
	private static final String SKELETON_MONTH_STANDALONE = "LLLL";

	private final Locale locale;

	public LocaleFormats(Locale locale) {
		this.locale = locale;
	}

	/** «29 сентября», «September 29». */
	public String dayAndMonth(Date date) {
		return formatDate(SKELETON_DAY_MONTH, date);
	}

	/** «вторник, 29 сентября 2026 г.», «Tuesday, September 29, 2026». */
	public String fullDate(Date date) {
		return formatDate(SKELETON_FULL_DATE, date);
	}

	/** «14:05», «2:05 PM». */
	public String time(Date date) {
		return formatDate(SKELETON_TIME, date);
	}

	/** Название месяца само по себе: «сентябрь», а не «сентября». */
	public String month(Date date) {
		return formatDate(SKELETON_MONTH_STANDALONE, date);
	}

	/**
	 * День относительно сегодня: -1 → «вчера», 3 → «через 3 дня».
	 * Там, где в языке есть отдельное слово («вчера», «послезавтра»), ICU подставляет его.
	 */
	public String relativeDays(int days) {
		return RelativeDateTimeFormatter.getInstance(locale).format(days, RelativeDateTimeUnit.DAY);
	}

	/**
	 * Число с разделителями по правилам языка: «1 234 567,891», «1,234,567.891».
	 * Числа подставляем в строки уже отформатированными (%1$s), а не через %d:
	 * String.format форматирует по Locale.getDefault(), то есть по языку устройства.
	 */
	public String number(double value) {
		return NumberFormat.getInstance(locale).format(value);
	}

	/** Краткая запись: «1,2 млн», «1.2M»; в китайском — «123万» (разряды по 10 000). */
	public String compact(long value) {
		return CompactDecimalFormat.getInstance(locale, CompactDecimalFormat.CompactStyle.SHORT)
				.format(value);
	}

	/** Доля в процентах: 0.45 → «45 %», «45%». */
	public String percent(double fraction) {
		return NumberFormat.getPercentInstance(locale).format(fraction);
	}

	/**
	 * Длительность: «1 ч 30 мин», «1 hr, 30 min»; формы единиц ICU выбирает сам.
	 * Это и обход дробных чисел: «1 ч 30 мин» вместо «1,5 часа».
	 */
	public String duration(int hours, int minutes) {
		return MeasureFormat.getInstance(locale, MeasureFormat.FormatWidth.SHORT)
				.formatMeasures(new Measure(hours, MeasureUnit.HOUR), new Measure(minutes, MeasureUnit.MINUTE));
	}

	private String formatDate(String skeleton, Date date) {
		return DateFormat.getInstanceForSkeleton(skeleton, locale).format(date);
	}
}
