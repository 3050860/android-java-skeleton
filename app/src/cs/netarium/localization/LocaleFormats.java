package cs.netarium.localization;

import android.icu.text.DateFormat;
import android.icu.text.RelativeDateTimeFormatter;
import android.icu.text.RelativeDateTimeFormatter.RelativeDateTimeUnit;

import java.util.Date;
import java.util.Locale;

/**
 * Даты на выбранном в приложении языке через ICU.
 * Локаль передаётся явно: Locale.getDefault() у нас — язык устройства, а не выбранный.
 *
 * Форматы заданы «скелетами» ICU: скелет перечисляет только поля (MMMM — месяц словом,
 * d — день, y — год), а порядок полей, предлоги, падежи и знаки препинания
 * для каждого языка ICU подставляет сам: «29 сентября», «September 29», «9月29日».
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

	private String formatDate(String skeleton, Date date) {
		return DateFormat.getInstanceForSkeleton(skeleton, locale).format(date);
	}
}
