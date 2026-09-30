package cs.netarium;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.app.ViewPumpAppCompatDelegate;

import java.util.Date;
import java.util.List;
import java.util.Locale;

import cs.netarium.localization.Language;
import cs.netarium.localization.LanguageCatalog;
import cs.netarium.localization.LocaleFormats;
import dev.b3nedikt.restring.Restring;

public class MainActivity extends AppCompatActivity {

	private static final String TAG = "LANGLOOP";

	/**
	 * Числа для демо plurals. Вместе они дают все формы для целых чисел:
	 * ru — one (1, 21, 101), few (2, 22), many (5, 11, 25, 1000000);
	 * en — one/other; es и fr — ещё и many для миллиона (на новых версиях ICU).
	 * 0 выводится отдельной строкой, см. newMoviesText().
	 */
	private static final int[] PLURALS_DEMO_NUMBERS = {0, 1, 2, 5, 11, 21, 22, 25, 101, 1000000};

	/** Минуты для демо порядка слов*/
	private static final int[] WORD_ORDER_DEMO_MINUTES = {1, 2, 5};

	/** Дни для демо относительных дат: -1 — «вчера» (отдельное слово), 3 — «через 3 дня» (число). */
	private static final int RELATIVE_DEMO_PAST_DAYS = -1;
	private static final int RELATIVE_DEMO_FUTURE_DAYS = 3;

	/** Значения для демо чисел. 1 ч 30 мин — обход дробных чисел вместо «1,5 часа». */
	private static final double NUMBER_DEMO_VALUE = 1234567.891;
	private static final long COMPACT_DEMO_VALUE = 1234567;
	private static final double PERCENT_DEMO_FRACTION = 0.45;
	private static final int DURATION_DEMO_HOURS = 1;
	private static final int DURATION_DEMO_MINUTES = 30;

	private static int sInstance = 0;
	private final int instanceId = ++sInstance;

	private AppCompatDelegate viewPumpDelegate;
	private Resources restringResources;
	private Resources restringBaseResources;

	/** Даты и числа выбранного языка; создаются при первом обращении, см. formats(). */
	private LocaleFormats localeFormats;

	@Override
	protected void attachBaseContext(Context newBase) {
		// Локаль приложения не зависит от локали устройства: подменяем её в конфигурации
		// базового контекста. Для ru/en это выбирает values/values-en, для es/fr задаёт
		// корректные форматы, а строки подставит Restring.
		Configuration config = new Configuration(newBase.getResources().getConfiguration());
		config.setLocale(Language.toLocale(MyApplication.getLanguage()));
		super.attachBaseContext(newBase.createConfigurationContext(config));

		Log.d(TAG, ">>> [#" + instanceId + "] attachBaseContext lang=" + MyApplication.getLanguage()
				+ " configLocales=" + super.getResources().getConfiguration().getLocales());
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Подключаем нашу разметку
		setContentView(R.layout.activity_main);

		// Находим TextView, которым нужно задать текст из кода
		TextView text3 = findViewById(R.id.text3);
		TextView text4 = findViewById(R.id.text4);

		// Задаем текст из кода
		text3.setText(R.string.string3);
		text4.setText(R.string.string4);

		Button btnShowDialog = findViewById(R.id.btnShowDialog);
		btnShowDialog.setOnClickListener(v -> showActionsDialog());

		showArraysDemo();
		showPluralsDemo();
		showWordOrderDemo();
		showDatesDemo();
		showNumbersDemo();

		Log.d(TAG, "<<< [#" + instanceId + "] onCreate lang=" + MyApplication.getLanguage()
				+ " delegate=" + getDelegate().getClass().getSimpleName()
				+ " resources=" + getResources().getClass().getSimpleName()
				+ " restringLocale=" + Restring.getLocale()
				+ " configLocales=" + getResources().getConfiguration().getLocales()
				+ " savedState=" + (savedInstanceState != null)
				+ " | text1=" + ((TextView) findViewById(R.id.text1)).getText()
				+ " text3=" + text3.getText()
				+ " button=" + btnShowDialog.getText());
	}

	@Override
	protected void onDestroy() {
		Log.d(TAG, "xxx [#" + instanceId + "] onDestroy");
		super.onDestroy();
	}

	/** Демо массива строк: категории из R.array.tv_categories, по одной на строку. */
	private void showArraysDemo() {
		// Массив берём из кода, а не через android:entries в разметке:
		// Reword переводит атрибуты вроде text и hint, но не entries
		String[] categories = getResources().getStringArray(R.array.tv_categories);
		TextView demoArrays = findViewById(R.id.demoArrays);
		demoArrays.setText(String.join("\n", categories));
	}

	/** Демо множественного числа: фраза для каждого числа из PLURALS_DEMO_NUMBERS, по одной на строку. */
	private void showPluralsDemo() {
		String[] lines = new String[PLURALS_DEMO_NUMBERS.length];
		for (int i = 0; i < PLURALS_DEMO_NUMBERS.length; i++) {
			lines[i] = newMoviesText(PLURALS_DEMO_NUMBERS[i]);
		}
		TextView demoPlurals = findViewById(R.id.demoPlurals);
		demoPlurals.setText(String.join("\n", lines));
	}

	/**
	 * «N новых фильмов» в нужной форме. Для 0 — отдельная строка: в en и es число 0
	 * попадает в форму other («0 new movies»), а не в zero, поэтому «нет фильмов» так не написать.
	 * Число передаётся дважды: первый раз — чтобы выбрать форму, второй — уже отформатированным
	 * по языку, для %1$s («1 000 000»).
	 */
	private String newMoviesText(int count) {
		if (count == 0) {
			return getString(R.string.no_new_movies);
		}
		return getResources().getQuantityString(R.plurals.new_movies, count, formats().number(count));
	}

	/**
	 * Демо порядка слов: код всегда передаёт аргументы в одном порядке,
	 * а перевод ставит их туда, где нужно по грамматике (%1$d, %2$s).
	 */
	private void showWordOrderDemo() {
		String title = getString(R.string.demo_movie_title);
		String[] lines = new String[WORD_ORDER_DEMO_MINUTES.length + 1];
		for (int i = 0; i < WORD_ORDER_DEMO_MINUTES.length; i++) {
			lines[i] = timeLeftText(WORD_ORDER_DEMO_MINUTES[i], title);
		}
		lines[lines.length - 1] = getString(R.string.playlist_of, getString(R.string.demo_user_name));
		TextView demoWordOrder = findViewById(R.id.demoWordOrder);
		demoWordOrder.setText(String.join("\n", lines));
	}

	/**
	 * «До конца «title» осталось N минут»: минуты — %1$s, название — %2$s.
	 * Минуты передаются дважды: первый раз — чтобы выбрать форму, второй — уже отформатированными.
	 */
	private String timeLeftText(int minutes, String title) {
		return getResources().getQuantityString(R.plurals.time_left, minutes, formats().number(minutes), title);
	}

	/**
	 * Демо дат через ICU: текущая дата в форматах выбранного языка.
	 * Подписи — строки из ресурсов, сами даты строит ICU, в переводы они не попадают.
	 */
	private void showDatesDemo() {
		LocaleFormats formats = formats();
		Date now = new Date();
		String[] lines = {
				getString(R.string.demo_date_day_month, formats.dayAndMonth(now)),
				getString(R.string.demo_date_full, formats.fullDate(now)),
				getString(R.string.demo_date_time, formats.time(now)),
				getString(R.string.demo_date_month, formats.month(now)),
				getString(R.string.demo_date_relative,
						formats.relativeDays(RELATIVE_DEMO_PAST_DAYS),
						formats.relativeDays(RELATIVE_DEMO_FUTURE_DAYS))
		};
		TextView demoDates = findViewById(R.id.demoDates);
		demoDates.setText(String.join("\n", lines));
	}

	/** Демо чисел через ICU: разделители, краткая запись, проценты и длительность по правилам языка. */
	private void showNumbersDemo() {
		LocaleFormats formats = formats();
		String[] lines = {
				getString(R.string.demo_number_plain, formats.number(NUMBER_DEMO_VALUE)),
				getString(R.string.demo_number_compact, formats.compact(COMPACT_DEMO_VALUE)),
				getString(R.string.demo_number_percent, formats.percent(PERCENT_DEMO_FRACTION)),
				getString(R.string.demo_number_duration,
						formats.duration(DURATION_DEMO_HOURS, DURATION_DEMO_MINUTES))
		};
		TextView demoNumbers = findViewById(R.id.demoNumbers);
		demoNumbers.setText(String.join("\n", lines));
	}

	/**
	 * Даты и числа выбранного языка. Создаются один раз на экземпляр активити:
	 * язык за время его жизни не меняется, смена идёт через recreate().
	 */
	private LocaleFormats formats() {
		if (localeFormats == null) {
			localeFormats = new LocaleFormats(selectedLocale());
		}
		return localeFormats;
	}

	/** Локаль выбранного языка — та же, по которой Restring выбирает строки (AppLocaleProvider). */
	private Locale selectedLocale() {
		return Language.toLocale(MyApplication.getLanguage());
	}

	private void showActionsDialog() {
		// Языки: встроенные ru/en плюс список с сервера. Каждый назван на самом себе
		// («Español», «Čeština»), поэтому названия не зависят от языка интерфейса.
		List<Language> languages = LanguageCatalog.all(this);
		String[] names = new String[languages.size()];
		for (int i = 0; i < languages.size(); i++) {
			names[i] = languages.get(i).getName();
		}

		// Создаем AlertDialog
		new AlertDialog.Builder(this)
				.setTitle(getString(R.string.select_language))
				.setItems(names, (dialog, which) -> {
					// which - это индекс выбранного пункта в languages
					changeLanguage(languages.get(which).getCode());
				})
				.setNegativeButton(getString(R.string.cancel), (dialog, which) -> dialog.dismiss())
				.show();
	}

	public String getLanguage() {
		return MyApplication.getLanguage();
	}

	public void changeLanguage(String language) {
		if (language.equals(MyApplication.getLanguage())) {
			Log.d(TAG, "!!! [#" + instanceId + "] changeLanguage " + language + " - already active");
			return;
		}
		Log.d(TAG, "!!! [#" + instanceId + "] changeLanguage " + MyApplication.getLanguage()
				+ " -> " + language + ", recreate()");
		((MyApplication) getApplication()).setLanguage(language);
		recreate();
	}

	/**
	 * Restring подключён для всех языков. Для встроенных ru/en в него ничего не загружено,
	 * и он сам отдаёт строки из ресурсов APK (values/, values-en/).
	 */
	@NonNull
	@Override
	public AppCompatDelegate getDelegate() {
		if (viewPumpDelegate == null) {
			viewPumpDelegate = new ViewPumpAppCompatDelegate(
					super.getDelegate(),
					this,
					Restring::wrapContext
			);
		}
		return viewPumpDelegate;
	}

	@Override
	public Resources getResources() {
		Resources base = super.getResources();
		// Кешируем обёртку, пересоздаём только если сменились базовые ресурсы.
		if (restringResources == null || restringBaseResources != base) {
			restringBaseResources = base;
			restringResources = Restring.wrapResources(this, base);
		}
		return restringResources;
	}
}
